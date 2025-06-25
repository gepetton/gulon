package com.gulon.app.mapper;

import com.gulon.app.dto.BookDto;
import com.gulon.app.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {
    
    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);
    
    // Entity to DTO
    BookDto.Response toResponse(Book book);
    BookDto.Summary toSummary(Book book);
    
    @Mapping(target = "isFromCache", ignore = true)
    BookDto.SearchResult toSearchResult(Book book);
    
    List<BookDto.Response> toResponseList(List<Book> books);
    List<BookDto.Summary> toSummaryList(List<Book> books);
    List<BookDto.SearchResult> toSearchResultList(List<Book> books);
    
    // DTO to Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isCached", ignore = true)
    @Mapping(target = "lastSyncedAt", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "naverLink", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "discountPrice", ignore = true)
    Book fromCreateRequest(BookDto.CreateRequest createRequest);
    
    // Update mapping
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "isbn", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isCached", ignore = true)
    @Mapping(target = "lastSyncedAt", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "naverLink", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "discountPrice", ignore = true)
    void updateFromRequest(BookDto.UpdateRequest updateRequest, @MappingTarget Book book);
    
    // 네이버 API 응답을 SearchResult로 변환
    @Mapping(target = "publicId", ignore = true)
    @Mapping(source = "cleanTitle", target = "title")
    @Mapping(source = "cleanDescription", target = "description")
    @Mapping(source = "link", target = "naverLink")
    @Mapping(source = "image", target = "imageUrl")
    @Mapping(source = "priceAsInteger", target = "price")
    @Mapping(source = "discountAsInteger", target = "discountPrice")
    @Mapping(target = "publishedDate", ignore = true)
    @Mapping(target = "isFromCache", constant = "false")
    BookDto.SearchResult fromNaverApiItem(BookDto.NaverBookItem naverItem);
    
    List<BookDto.SearchResult> fromNaverApiItems(List<BookDto.NaverBookItem> naverItems);
    
    // Page 변환
    default BookDto.BookListResponse toBookListResponse(Page<Book> bookPage) {
        BookDto.BookListResponse response = new BookDto.BookListResponse();
        response.setBooks(toSummaryList(bookPage.getContent()));
        response.setTotalElements((int) bookPage.getTotalElements());
        response.setTotalPages(bookPage.getTotalPages());
        response.setCurrentPage(bookPage.getNumber());
        response.setSize(bookPage.getSize());
        response.setHasNext(bookPage.hasNext());
        response.setHasPrevious(bookPage.hasPrevious());
        return response;
    }
    
    // SearchResult를 Book으로 변환 (네이버 API 결과 저장용)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isCached", constant = "true")
    @Mapping(target = "lastSyncedAt", expression = "java(java.time.LocalDateTime.now())")
    Book fromSearchResult(BookDto.SearchResult searchResult);
    
    // CacheStatus 생성
    default BookDto.CacheStatus toCacheStatus(Book book, String cacheSource) {
        BookDto.CacheStatus status = new BookDto.CacheStatus();
        status.setIsbn(book.getIsbn());
        status.setIsCached(book.getIsCached());
        status.setLastSyncedAt(book.getLastSyncedAt());
        status.setNeedsUpdate(book.isCacheExpired(120)); // 2시간 기준
        status.setCacheSource(cacheSource);
        return status;
    }
} 