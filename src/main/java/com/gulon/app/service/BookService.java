package com.gulon.app.service;

import com.gulon.app.config.BookCacheConfig;
import com.gulon.app.dto.BookDto;
import com.gulon.app.entity.Book;
import com.gulon.app.mapper.BookMapper;
import com.gulon.app.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final NaverBookApiService naverBookApiService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BookCacheConfig cacheProperties;

    private static final String BOOK_CACHE_PREFIX = "book:";
    private static final String SEARCH_CACHE_PREFIX = "search:";
    private static final String IMAGE_CACHE_PREFIX = "image:";

    /**
     * 도서 검색 (네이버 API + 로컬 DB)
     */
    public List<BookDto.SearchResult> searchBooks(BookDto.SearchRequest request) {
        log.info("도서 검색 - 검색어: {}", request.getQuery());
        
        // 캐시 확인
        String cacheKey = SEARCH_CACHE_PREFIX + request.getQuery() + ":" + request.getStart() + ":" + request.getDisplay();
        List<BookDto.SearchResult> cachedResults = getCachedSearchResults(cacheKey);
        if (cachedResults != null) {
            log.info("캐시에서 검색 결과 반환");
            return cachedResults;
        }

        List<BookDto.SearchResult> results = new ArrayList<>();

        // 1. 로컬 DB에서 검색
        try {
            Pageable pageable = PageRequest.of(0, request.getDisplay(), Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Book> localBooks = bookRepository.searchByKeyword(request.getQuery(), pageable);
            
            List<BookDto.SearchResult> localResults = bookMapper.toSearchResultList(localBooks.getContent());
            localResults.forEach(result -> result.setIsFromCache(true));
            results.addAll(localResults);
            
            log.info("로컬 DB에서 {}건 검색됨", localResults.size());
        } catch (Exception e) {
            log.error("로컬 DB 검색 실패: {}", e.getMessage());
        }

        // 2. 네이버 API에서 검색 (로컬 결과가 부족한 경우)
        if (results.size() < request.getDisplay()) {
            try {
                BookDto.NaverApiResponse apiResponse = naverBookApiService.searchBooks(request);
                if (apiResponse != null && apiResponse.getItems() != null) {
                    List<BookDto.SearchResult> apiResults = bookMapper.fromNaverApiItems(apiResponse.getItems());
                    
                    // 중복 제거 (ISBN 기준)
                    List<BookDto.SearchResult> uniqueResults = removeDuplicates(results, apiResults);
                    results.addAll(uniqueResults);
                    
                    // 새로운 도서 정보 DB에 저장
                    saveNewBooksFromApi(apiResults);
                    
                    log.info("네이버 API에서 {}건 검색됨", apiResults.size());
                }
            } catch (Exception e) {
                log.error("네이버 API 검색 실패: {}", e.getMessage());
            }
        }

        // 결과 캐싱
        cacheSearchResults(cacheKey, results);

        return results;
    }

    /**
     * 도서 상세 조회 (publicId 기준)
     */
    public Optional<BookDto.Response> getBookByPublicId(UUID publicId) {
        log.info("도서 상세 조회 - publicId: {}", publicId);
        
        return bookRepository.findByPublicId(publicId)
                .map(book -> {
                    // 캐시 만료 확인 및 업데이트
                    if (book.isCacheExpired(cacheProperties.getDetailExpiry() / 60)) {
                        updateBookFromApi(book);
                    }
                    return bookMapper.toResponse(book);
                });
    }

    /**
     * ISBN으로 도서 조회
     */
    public Optional<BookDto.Response> getBookByIsbn(String isbn) {
        log.info("ISBN으로 도서 조회 - ISBN: {}", isbn);
        
        Optional<Book> bookOpt = bookRepository.findByIsbn(isbn);
        
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            // 캐시 만료 확인 및 업데이트
            if (book.isCacheExpired(cacheProperties.getDetailExpiry() / 60)) {
                updateBookFromApi(book);
            }
            return Optional.of(bookMapper.toResponse(book));
        } else {
            // DB에 없으면 네이버 API에서 검색
            return searchAndSaveBookByIsbn(isbn);
        }
    }

    /**
     * 도서 생성
     */
    @Transactional
    public BookDto.Response createBook(BookDto.CreateRequest request) {
        log.info("도서 생성 - 제목: {}", request.getTitle());
        
        // ISBN 중복 확인
        if (request.getIsbn() != null && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new IllegalArgumentException("이미 존재하는 ISBN입니다: " + request.getIsbn());
        }
        
        Book book = bookMapper.fromCreateRequest(request);
        Book savedBook = bookRepository.save(book);
        
        log.info("도서 생성 완료 - publicId: {}", savedBook.getPublicId());
        return bookMapper.toResponse(savedBook);
    }

    /**
     * 도서 수정
     */
    @Transactional
    public Optional<BookDto.Response> updateBook(UUID publicId, BookDto.UpdateRequest request) {
        log.info("도서 수정 - publicId: {}", publicId);
        
        return bookRepository.findByPublicId(publicId)
                .map(book -> {
                    bookMapper.updateFromRequest(request, book);
                    Book savedBook = bookRepository.save(book);
                    log.info("도서 수정 완료 - publicId: {}", publicId);
                    return bookMapper.toResponse(savedBook);
                });
    }

    /**
     * 도서 삭제
     */
    @Transactional
    public boolean deleteBook(UUID publicId) {
        log.info("도서 삭제 - publicId: {}", publicId);
        
        return bookRepository.findByPublicId(publicId)
                .map(book -> {
                    bookRepository.delete(book);
                    log.info("도서 삭제 완료 - publicId: {}", publicId);
                    return true;
                })
                .orElse(false);
    }

    /**
     * 도서 목록 조회 (페이징)
     */
    public BookDto.BookListResponse getBooks(int page, int size, String sort) {
        log.info("도서 목록 조회 - 페이지: {}, 크기: {}", page, size);
        
        Sort sortBy = createSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortBy);
        Page<Book> bookPage = bookRepository.findAll(pageable);
        
        return bookMapper.toBookListResponse(bookPage);
    }

    /**
     * 키워드로 도서 검색 (페이징)
     */
    public BookDto.BookListResponse searchBooksByKeyword(String keyword, int page, int size) {
        log.info("키워드 도서 검색 - 키워드: {}, 페이지: {}, 크기: {}", keyword, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Book> bookPage = bookRepository.searchByKeyword(keyword, pageable);
        
        return bookMapper.toBookListResponse(bookPage);
    }

    /**
     * 저자별 도서 목록
     */
    public BookDto.BookListResponse getBooksByAuthor(String author, int page, int size) {
        log.info("저자별 도서 조회 - 저자: {}", author);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findByAuthorOrderByPublishedDateDesc(author, pageable);
        
        return bookMapper.toBookListResponse(bookPage);
    }

    /**
     * 출판사별 도서 목록
     */
    public BookDto.BookListResponse getBooksByPublisher(String publisher, int page, int size) {
        log.info("출판사별 도서 조회 - 출판사: {}", publisher);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findByPublisherOrderByPublishedDateDesc(publisher, pageable);
        
        return bookMapper.toBookListResponse(bookPage);
    }

    /**
     * 베스트셀러 목록
     */
    public BookDto.BookListResponse getBestsellers(int page, int size) {
        log.info("베스트셀러 조회");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findBestsellers(pageable);
        
        return bookMapper.toBookListResponse(bookPage);
    }

    /**
     * 신간 도서 목록
     */
    public BookDto.BookListResponse getNewReleases(int page, int size) {
        log.info("신간 도서 조회");
        
        LocalDate recentDate = LocalDate.now().minusMonths(3); // 3개월 이내
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findNewReleases(recentDate, pageable);
        
        return bookMapper.toBookListResponse(bookPage);
    }

    /**
     * 도서 통계
     */
    public BookDto.Statistics getStatistics() {
        log.info("도서 통계 조회");
        
        BookDto.Statistics stats = new BookDto.Statistics();
        stats.setTotalBooks(bookRepository.count());
        stats.setCachedBooks(bookRepository.countByIsCachedTrue());
        stats.setRecentlyAdded(bookRepository.countRecentlyAdded(LocalDateTime.now().minusDays(7)));
        
        if (stats.getTotalBooks() > 0) {
            stats.setCacheHitRate((double) stats.getCachedBooks() / stats.getTotalBooks() * 100);
        } else {
            stats.setCacheHitRate(0.0);
        }
        
        stats.setLastUpdated(LocalDateTime.now());
        
        return stats;
    }

    /**
     * 네이버 API에서 ISBN으로 검색하여 저장
     */
    private Optional<BookDto.Response> searchAndSaveBookByIsbn(String isbn) {
        try {
            BookDto.NaverApiResponse response = naverBookApiService.searchByIsbn(isbn);
            if (response != null && response.getItems() != null && !response.getItems().isEmpty()) {
                BookDto.NaverBookItem item = response.getItems().get(0);
                
                Book book = Book.fromNaverApi(
                        item.getCleanTitle(),
                        item.getAuthor(),
                        item.getIsbn(),
                        item.getPublisher(),
                        item.getPubdate(),
                        item.getImage(),
                        item.getCleanDescription(),
                        item.getLink(),
                        item.getPriceAsInteger(),
                        item.getDiscountAsInteger()
                );
                
                Book savedBook = bookRepository.save(book);
                log.info("네이버 API에서 도서 정보 저장 완료 - ISBN: {}", isbn);
                
                return Optional.of(bookMapper.toResponse(savedBook));
            }
        } catch (Exception e) {
            log.error("네이버 API에서 도서 검색 및 저장 실패 - ISBN: {}, 오류: {}", isbn, e.getMessage());
        }
        
        return Optional.empty();
    }

    /**
     * 도서 정보 API 업데이트
     */
    @Transactional
    private void updateBookFromApi(Book book) {
        if (book.getIsbn() == null) return;
        
        try {
            BookDto.NaverApiResponse response = naverBookApiService.searchByIsbn(book.getIsbn());
            if (response != null && response.getItems() != null && !response.getItems().isEmpty()) {
                BookDto.NaverBookItem item = response.getItems().get(0);
                
                // 업데이트 가능한 필드들만 업데이트
                book.setImageUrl(item.getImage());
                book.setDescription(item.getCleanDescription());
                book.setNaverLink(item.getLink());
                book.setPrice(item.getPriceAsInteger());
                book.setDiscountPrice(item.getDiscountAsInteger());
                book.updateCache();
                
                bookRepository.save(book);
                log.info("도서 정보 API 업데이트 완료 - ISBN: {}", book.getIsbn());
            }
        } catch (Exception e) {
            log.error("도서 정보 API 업데이트 실패 - ISBN: {}, 오류: {}", book.getIsbn(), e.getMessage());
        }
    }

    /**
     * 네이버 API 결과에서 새로운 도서 저장
     */
    @Transactional
    private void saveNewBooksFromApi(List<BookDto.SearchResult> apiResults) {
        for (BookDto.SearchResult result : apiResults) {
            if (result.getIsbn() != null && !bookRepository.existsByIsbn(result.getIsbn())) {
                try {
                    Book book = bookMapper.fromSearchResult(result);
                    bookRepository.save(book);
                    log.debug("새로운 도서 저장 - ISBN: {}", result.getIsbn());
                } catch (Exception e) {
                    log.warn("도서 저장 실패 - ISBN: {}, 오류: {}", result.getIsbn(), e.getMessage());
                }
            }
        }
    }

    /**
     * 중복 결과 제거 (ISBN 기준)
     */
    private List<BookDto.SearchResult> removeDuplicates(List<BookDto.SearchResult> existing, List<BookDto.SearchResult> newResults) {
        List<String> existingIsbns = existing.stream()
                .map(BookDto.SearchResult::getIsbn)
                .filter(isbn -> isbn != null)
                .toList();

        return newResults.stream()
                .filter(result -> result.getIsbn() == null || !existingIsbns.contains(result.getIsbn()))
                .toList();
    }

    /**
     * 정렬 조건 생성
     */
    private Sort createSort(String sort) {
        return switch (sort) {
            case "title" -> Sort.by(Sort.Direction.ASC, "title");
            case "author" -> Sort.by(Sort.Direction.ASC, "author");
            case "publishedDate" -> Sort.by(Sort.Direction.DESC, "publishedDate");
            case "createdAt" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "updatedAt");
        };
    }

    /**
     * 검색 결과 캐싱
     */
    private void cacheSearchResults(String cacheKey, List<BookDto.SearchResult> results) {
        try {
            redisTemplate.opsForValue().set(cacheKey, results, cacheProperties.getSearchExpiry(), TimeUnit.SECONDS);
            log.debug("검색 결과 캐시 저장 - 키: {}", cacheKey);
        } catch (Exception e) {
            log.warn("검색 결과 캐시 저장 실패: {}", e.getMessage());
        }
    }

    /**
     * 캐시된 검색 결과 조회
     */
    @SuppressWarnings("unchecked")
    private List<BookDto.SearchResult> getCachedSearchResults(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof List<?>) {
                return (List<BookDto.SearchResult>) cached;
            }
        } catch (Exception e) {
            log.warn("검색 결과 캐시 조회 실패: {}", e.getMessage());
        }
        return null;
    }
}