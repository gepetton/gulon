package com.gulon.app.service;

import com.gulon.app.dto.BookDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

@Service
@Slf4j
public class NaverBookApiService {
    
    @Value("${naver.api.base-url}")
    private String baseUrl;
    
    @Value("${naver.api.client-id}")
    private String clientId;
    
    @Value("${naver.api.client-secret}")
    private String clientSecret;
    
    @Value("${naver.api.book-search-url}")
    private String bookSearchUrl;
    
    @Value("${naver.api.book-detail-url}")
    private String bookDetailUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 네이버 도서 검색 API 호출
     */
    public BookDto.NaverApiResponse searchBooks(BookDto.SearchRequest request) {
        log.info("네이버 도서 검색 API 호출 - 검색어: {}, 페이지: {}, 개수: {}", 
                request.getQuery(), request.getStart(), request.getDisplay());
        
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + bookSearchUrl)
                    .queryParam("query", request.getQuery())
                    .queryParam("display", request.getDisplay())
                    .queryParam("start", request.getStart())
                    .queryParam("sort", request.getSort())
                    .toUriString();
            
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<BookDto.NaverApiResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, BookDto.NaverApiResponse.class);
            
            BookDto.NaverApiResponse result = response.getBody();
            log.info("네이버 API 검색 완료 - 총 {}건", 
                    result != null ? result.getTotal() : 0);
            
            return result != null ? result : createEmptyResponse();
            
        } catch (Exception e) {
            log.error("네이버 API 검색 실패: {}", e.getMessage());
            return createEmptyResponse();
        }
    }
    
    /**
     * 네이버 도서 상세 검색 API 호출 (ISBN 또는 제목)
     */
    public BookDto.NaverApiResponse searchBookDetail(BookDto.DetailSearchRequest request) {
        log.info("네이버 도서 상세 검색 API 호출 - 제목: {}, ISBN: {}", 
                request.getTitle(), request.getIsbn());
        
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + bookDetailUrl)
                    .queryParam("display", request.getDisplay())
                    .queryParam("start", request.getStart())
                    .queryParam("sort", request.getSort());
            
            if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
                builder = builder.queryParam("d_titl", request.getTitle());
            }
            if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty()) {
                builder = builder.queryParam("d_isbn", request.getIsbn());
            }
            
            String url = builder.toUriString();
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<BookDto.NaverApiResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, BookDto.NaverApiResponse.class);
            
            BookDto.NaverApiResponse result = response.getBody();
            log.info("네이버 API 상세 검색 완료 - 총 {}건", 
                    result != null ? result.getTotal() : 0);
            
            return result != null ? result : createEmptyResponse();
            
        } catch (Exception e) {
            log.error("네이버 API 상세 검색 실패: {}", e.getMessage());
            return createEmptyResponse();
        }
    }
    
    /**
     * ISBN으로 도서 검색
     */
    public BookDto.NaverApiResponse searchByIsbn(String isbn) {
        log.info("ISBN으로 도서 검색 - ISBN: {}", isbn);
        
        BookDto.DetailSearchRequest request = new BookDto.DetailSearchRequest();
        request.setIsbn(isbn);
        request.setDisplay(1);
        
        return searchBookDetail(request);
    }
    
    /**
     * 제목으로 도서 검색
     */
    public BookDto.NaverApiResponse searchByTitle(String title) {
        log.info("제목으로 도서 검색 - 제목: {}", title);
        
        BookDto.SearchRequest request = new BookDto.SearchRequest();
        request.setQuery(title);
        request.setDisplay(10);
        
        return searchBooks(request);
    }
    
    /**
     * 베스트셀러 검색
     */
    public BookDto.NaverApiResponse searchBestsellers() {
        log.info("베스트셀러 검색");
        
        BookDto.SearchRequest request = new BookDto.SearchRequest();
        request.setQuery("베스트셀러");
        request.setDisplay(20);
        request.setSort("date");
        
        return searchBooks(request);
    }
    
    /**
     * 신간 도서 검색
     */
    public BookDto.NaverApiResponse searchNewReleases() {
        log.info("신간 도서 검색");
        
        BookDto.SearchRequest request = new BookDto.SearchRequest();
        request.setQuery("신간");
        request.setDisplay(20);
        request.setSort("date");
        
        return searchBooks(request);
    }
    
    /**
     * API 연결 상태 확인
     */
    public boolean healthCheck() {
        try {
            BookDto.SearchRequest request = new BookDto.SearchRequest();
            request.setQuery("test");
            request.setDisplay(1);
            
            BookDto.NaverApiResponse response = searchBooks(request);
            boolean isHealthy = response != null && response.getTotal() != null;
            log.info("네이버 API 상태 확인: {}", isHealthy ? "정상" : "오류");
            return isHealthy;
            
        } catch (Exception e) {
            log.error("네이버 API 상태 확인 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * HTTP 헤더 생성
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        headers.set("Content-Type", "application/json");
        return headers;
    }
    
    /**
     * 빈 응답 생성 (API 호출 실패 시)
     */
    private BookDto.NaverApiResponse createEmptyResponse() {
        BookDto.NaverApiResponse response = new BookDto.NaverApiResponse();
        response.setTotal(0);
        response.setStart(1);
        response.setDisplay(0);
        response.setItems(Collections.emptyList());
        return response;
    }
} 