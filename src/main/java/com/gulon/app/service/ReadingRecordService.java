package com.gulon.app.service;

import com.gulon.app.dto.ReadingRecordDto;
import com.gulon.app.entity.ReadingRecord;
import com.gulon.app.entity.User;
import com.gulon.app.entity.Book;
import com.gulon.app.mapper.ReadingRecordMapper;
import com.gulon.app.repository.ReadingRecordRepository;
import com.gulon.app.repository.UserRepository;
import com.gulon.app.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 독서 기록 서비스 - MapStruct 활용
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReadingRecordService {
    
    private final ReadingRecordRepository readingRecordRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReadingRecordMapper readingRecordMapper;
    
    @Transactional
    public ReadingRecordDto.Response createReadingRecord(ReadingRecordDto.CreateRequest request) {
        log.info("독서 기록 생성 요청: userPublicId={}, bookPublicId={}", 
                request.getUserPublicId(), request.getBookPublicId());
        
        // 사용자와 책 조회
        User user = findUserByPublicIdOrThrow(request.getUserPublicId());
        Book book = findBookByPublicIdOrThrow(request.getBookPublicId());
        
        // 이미 해당 사용자가 같은 책에 대한 독서 기록이 있는지 확인
        if (readingRecordRepository.existsByUserAndBook(user, book)) {
            throw new IllegalArgumentException("해당 사용자는 이미 이 책에 대한 독서 기록이 있습니다.");
        }
        
        // 입력값 검증
        validateCreateRequest(request);
        
        // MapStruct를 사용한 엔티티 변환
        ReadingRecord readingRecord = readingRecordMapper.toEntity(request);
        readingRecord.setUser(user);
        readingRecord.setBook(book);
        
        // startDate가 없으면 오늘 날짜로 설정
        if (readingRecord.getStartDate() == null) {
            readingRecord.setStartDate(LocalDate.now());
        }
        
        ReadingRecord savedRecord = readingRecordRepository.save(readingRecord);
        
        log.info("독서 기록 생성 완료: id={}, user={}, book={}", 
                savedRecord.getId(), user.getName(), book.getTitle());
        return readingRecordMapper.toResponseDto(savedRecord);
    }
    
    @Transactional
    public ReadingRecordDto.Response updateReadingRecord(Integer id, ReadingRecordDto.UpdateRequest request) {
        log.info("독서 기록 수정 요청: id={}", id);
        
        ReadingRecord existingRecord = findReadingRecordByIdOrThrow(id);
        
        // MapStruct를 사용한 엔티티 업데이트
        readingRecordMapper.updateEntityFromDto(request, existingRecord);
        
        // 상태가 COMPLETED로 변경되고 endDate가 없으면 오늘 날짜로 설정
        if (existingRecord.getStatus() == ReadingRecord.ReadingStatus.COMPLETED && 
            existingRecord.getEndDate() == null) {
            existingRecord.setEndDate(LocalDate.now());
        }
        
        ReadingRecord updatedRecord = readingRecordRepository.save(existingRecord);
        
        log.info("독서 기록 수정 완료: id={}", updatedRecord.getId());
        return readingRecordMapper.toResponseDto(updatedRecord);
    }
    
    @Transactional
    public ReadingRecordDto.Response updateProgress(Integer id, ReadingRecordDto.ProgressUpdateRequest request) {
        log.info("독서 진행률 업데이트 요청: id={}, currentPage={}", id, request.getCurrentPage());
        
        ReadingRecord existingRecord = findReadingRecordByIdOrThrow(id);
        
        // 진행률 업데이트
        readingRecordMapper.updateProgressFromDto(request, existingRecord);
        
        // 현재 페이지가 총 페이지와 같거나 크면 자동으로 COMPLETED 상태로 변경
        if (existingRecord.getCurrentPage() != null && existingRecord.getTotalPages() != null &&
            existingRecord.getCurrentPage() >= existingRecord.getTotalPages()) {
            existingRecord.setStatus(ReadingRecord.ReadingStatus.COMPLETED);
            if (existingRecord.getEndDate() == null) {
                existingRecord.setEndDate(LocalDate.now());
            }
        }
        
        ReadingRecord updatedRecord = readingRecordRepository.save(existingRecord);
        
        log.info("독서 진행률 업데이트 완료: id={}, progress={}%", 
                updatedRecord.getId(), 
                readingRecordMapper.calculateProgressPercentage(
                    updatedRecord.getCurrentPage(), updatedRecord.getTotalPages()));
        return readingRecordMapper.toResponseDto(updatedRecord);
    }
    
    public Optional<ReadingRecordDto.Response> getReadingRecordById(Integer id) {
        log.debug("독서 기록 조회: id={}", id);
        
        return readingRecordRepository.findById(id)
                .map(readingRecordMapper::toResponseDto);
    }
    
    public ReadingRecordDto.ListResponse getAllReadingRecords(int page, int size, String sort) {
        log.debug("모든 독서 기록 조회: page={}, size={}, sort={}", page, size, sort);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        Page<ReadingRecord> recordPage = readingRecordRepository.findAll(pageable);
        
        List<ReadingRecordDto.Summary> summaries = readingRecordMapper.toSummaryDtoList(recordPage.getContent());
        
        return createListResponse(summaries, recordPage);
    }
    
    public ReadingRecordDto.ListResponse getReadingRecordsByUser(UUID userPublicId, int page, int size) {
        log.debug("사용자별 독서 기록 조회: userPublicId={}", userPublicId);
        
        User user = findUserByPublicIdOrThrow(userPublicId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<ReadingRecord> recordPage = readingRecordRepository.findByUser(user, pageable);
        
        List<ReadingRecordDto.Summary> summaries = readingRecordMapper.toSummaryDtoList(recordPage.getContent());
        
        return createListResponse(summaries, recordPage);
    }
    
    public ReadingRecordDto.ListResponse getReadingRecordsByBook(UUID bookPublicId, int page, int size) {
        log.debug("책별 독서 기록 조회: bookPublicId={}", bookPublicId);
        
        Book book = findBookByPublicIdOrThrow(bookPublicId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<ReadingRecord> recordPage = readingRecordRepository.findByBook(book, pageable);
        
        List<ReadingRecordDto.Summary> summaries = readingRecordMapper.toSummaryDtoList(recordPage.getContent());
        
        return createListResponse(summaries, recordPage);
    }
    
    public ReadingRecordDto.ListResponse getReadingRecordsByStatus(ReadingRecord.ReadingStatus status, int page, int size) {
        log.debug("상태별 독서 기록 조회: status={}", status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<ReadingRecord> recordPage = readingRecordRepository.findByStatus(status, pageable);
        
        List<ReadingRecordDto.Summary> summaries = readingRecordMapper.toSummaryDtoList(recordPage.getContent());
        
        return createListResponse(summaries, recordPage);
    }
    
    public ReadingRecordDto.UserReadingStatus getUserReadingStatus(UUID userPublicId) {
        log.debug("사용자 독서 현황 조회: userPublicId={}", userPublicId);
        
        User user = findUserByPublicIdOrThrow(userPublicId);
        
        // 현재 읽고 있는 책들
        List<ReadingRecord> currentlyReading = readingRecordRepository
                .findByUserAndStatusOrderByUpdatedAtDesc(user, ReadingRecord.ReadingStatus.READING);
        
        // 최근 완독한 책들 (최근 30일)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        List<ReadingRecord> recentlyCompleted = readingRecordRepository
                .findByUserAndStatusAndUpdatedAtAfterOrderByUpdatedAtDesc(
                    user, ReadingRecord.ReadingStatus.COMPLETED, thirtyDaysAgo);
        
        // 통계 정보
        ReadingRecordDto.ProgressStats stats = getUserProgressStats(userPublicId);
        
        ReadingRecordDto.UserReadingStatus status = new ReadingRecordDto.UserReadingStatus();
        status.setUserPublicId(userPublicId);
        status.setUserName(user.getName());
        status.setCurrentlyReading(readingRecordMapper.toSummaryDtoList(currentlyReading));
        status.setRecentlyCompleted(readingRecordMapper.toSummaryDtoList(recentlyCompleted));
        status.setStats(stats);
        
        return status;
    }
    
    public ReadingRecordDto.ProgressStats getUserProgressStats(UUID userPublicId) {
        log.debug("사용자 독서 통계 조회: userPublicId={}", userPublicId);
        
        User user = findUserByPublicIdOrThrow(userPublicId);
        
        Long totalBooks = readingRecordRepository.countByUser(user);
        Long completedBooks = readingRecordRepository.countByUserAndStatus(user, ReadingRecord.ReadingStatus.COMPLETED);
        Long readingBooks = readingRecordRepository.countByUserAndStatus(user, ReadingRecord.ReadingStatus.READING);
        Long pausedBooks = readingRecordRepository.countByUserAndStatus(user, ReadingRecord.ReadingStatus.PAUSED);
        
        Double completionRate = totalBooks > 0 ? (completedBooks.doubleValue() / totalBooks.doubleValue()) * 100.0 : 0.0;
        
        // 읽은 총 페이지 수 계산
        Integer totalPagesRead = readingRecordRepository.getTotalPagesReadByUser(user);
        
        // 평균 진행률 계산
        Double averageProgress = readingRecordRepository.getAverageProgressByUser(user);
        
        ReadingRecordDto.ProgressStats stats = new ReadingRecordDto.ProgressStats();
        stats.setUserPublicId(userPublicId);
        stats.setUserName(user.getName());
        stats.setTotalBooks(totalBooks);
        stats.setCompletedBooks(completedBooks);
        stats.setReadingBooks(readingBooks);
        stats.setPausedBooks(pausedBooks);
        stats.setCompletionRate(completionRate);
        stats.setTotalPagesRead(totalPagesRead != null ? totalPagesRead : 0);
        stats.setAverageProgressPercentage(averageProgress != null ? averageProgress : 0.0);
        
        return stats;
    }
    
    public ReadingRecordDto.BookReadingStatus getBookReadingStatus(UUID bookPublicId) {
        log.debug("책별 독서 현황 조회: bookPublicId={}", bookPublicId);
        
        Book book = findBookByPublicIdOrThrow(bookPublicId);
        
        Long totalReaders = readingRecordRepository.countByBook(book);
        Long completedReaders = readingRecordRepository.countByBookAndStatus(book, ReadingRecord.ReadingStatus.COMPLETED);
        Double averageProgress = readingRecordRepository.getAverageProgressByBook(book);
        
        // 최근 독서 기록들
        List<ReadingRecord> recentRecords = readingRecordRepository
                .findTop10ByBookOrderByUpdatedAtDesc(book);
        
        ReadingRecordDto.BookReadingStatus status = new ReadingRecordDto.BookReadingStatus();
        status.setBookPublicId(bookPublicId);
        status.setBookTitle(book.getTitle());
        status.setBookAuthor(book.getAuthor());
        status.setTotalReaders(totalReaders);
        status.setCompletedReaders(completedReaders);
        status.setAverageProgress(averageProgress != null ? averageProgress : 0.0);
        status.setRecentRecords(readingRecordMapper.toSummaryDtoList(recentRecords));
        
        return status;
    }
    
    public ReadingRecordDto.Statistics getGlobalStatistics() {
        log.debug("전체 독서 통계 조회");
        
        Long totalRecords = readingRecordRepository.count();
        Long completedRecords = readingRecordRepository.countByStatus(ReadingRecord.ReadingStatus.COMPLETED);
        Long readingRecords = readingRecordRepository.countByStatus(ReadingRecord.ReadingStatus.READING);
        Long pausedRecords = readingRecordRepository.countByStatus(ReadingRecord.ReadingStatus.PAUSED);
        
        // 평균 완독 소요일 계산
        Double averageCompletionTime = readingRecordRepository.getAverageCompletionTimeInDays();
        
        // 일일 평균 페이지 수 계산
        Double averagePagesPerDay = readingRecordRepository.getAveragePagesPerDay();
        
        ReadingRecordDto.Statistics stats = new ReadingRecordDto.Statistics();
        stats.setTotalRecords(totalRecords);
        stats.setCompletedRecords(completedRecords);
        stats.setReadingRecords(readingRecords);
        stats.setPausedRecords(pausedRecords);
        stats.setAverageCompletionTime(averageCompletionTime != null ? averageCompletionTime : 0.0);
        stats.setAveragePagesPerDay(averagePagesPerDay != null ? averagePagesPerDay : 0.0);
        stats.setLastUpdated(LocalDateTime.now());
        
        return stats;
    }
    
    @Transactional
    public void deleteReadingRecord(Integer id) {
        log.info("독서 기록 삭제 요청: id={}", id);
        
        if (!readingRecordRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 독서 기록입니다: " + id);
        }
        
        readingRecordRepository.deleteById(id);
        log.info("독서 기록 삭제 완료: id={}", id);
    }
    
    public boolean existsReadingRecord(UUID userPublicId, UUID bookPublicId) {
        log.debug("독서 기록 존재 여부 확인: userPublicId={}, bookPublicId={}", userPublicId, bookPublicId);
        
        User user = findUserByPublicIdOrThrow(userPublicId);
        Book book = findBookByPublicIdOrThrow(bookPublicId);
        
        return readingRecordRepository.existsByUserAndBook(user, book);
    }
    
    // 헬퍼 메서드들
    private ReadingRecord findReadingRecordByIdOrThrow(Integer id) {
        return readingRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 독서 기록입니다: " + id));
    }
    
    private User findUserByPublicIdOrThrow(UUID publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + publicId));
    }
    
    private Book findBookByPublicIdOrThrow(UUID publicId) {
        return bookRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 책입니다: " + publicId));
    }
    
    private void validateCreateRequest(ReadingRecordDto.CreateRequest request) {
        if (request.getUserPublicId() == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (request.getBookPublicId() == null) {
            throw new IllegalArgumentException("책 ID는 필수입니다.");
        }
        if (request.getTotalPages() != null && request.getTotalPages() <= 0) {
            throw new IllegalArgumentException("총 페이지 수는 0보다 커야 합니다.");
        }
        if (request.getStartDate() != null && request.getStartDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("독서 시작일은 미래일 수 없습니다.");
        }
    }
    
    private ReadingRecordDto.ListResponse createListResponse(List<ReadingRecordDto.Summary> summaries, Page<ReadingRecord> page) {
        ReadingRecordDto.ListResponse response = new ReadingRecordDto.ListResponse();
        response.setRecords(summaries);
        response.setTotalElements((int) page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setCurrentPage(page.getNumber());
        response.setSize(page.getSize());
        response.setHasNext(page.hasNext());
        response.setHasPrevious(page.hasPrevious());
        return response;
    }
} 