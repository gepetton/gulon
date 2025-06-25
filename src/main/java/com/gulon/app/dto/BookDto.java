package com.gulon.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BookDto {

    @Getter
    @Setter
    public static class SearchRequest {
        private String query;
        private Integer display = 10;
        private Integer start = 1;
        private String sort = "sim"; // sim, date
    }

    @Getter
    @Setter
    public static class DetailSearchRequest {
        private String title;
        private String isbn;
        private Integer display = 10;
        private Integer start = 1;
        private String sort = "sim";
    }

    @Getter
    @Setter
    public static class CreateRequest {
        private String title;
        private String author;
        private String isbn;
        private String publisher;
        private LocalDate publishedDate;
        private String description;
    }

    @Getter
    @Setter
    public static class UpdateRequest {
        private String title;
        private String author;
        private String publisher;
        private LocalDate publishedDate;
        private String description;
    }

    @Getter
    @Setter
    public static class Response {
        private UUID publicId;
        private String title;
        private String author;
        private String isbn;
        private LocalDate publishedDate;
        private String publisher;
        private String imageUrl;
        private String description;
        private String naverLink;
        private Integer price;
        private Integer discountPrice;
        private Boolean isCached;
        private LocalDateTime lastSyncedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    public static class Summary {
        private UUID publicId;
        private String title;
        private String author;
        private String imageUrl;
        private String publisher;
        private LocalDate publishedDate;
    }

    @Getter
    @Setter
    public static class SearchResult {
        private UUID publicId;
        private String title;
        private String author;
        private String isbn;
        private String publisher;
        private String imageUrl;
        private Integer price;
        private Integer discountPrice;
        private String description;
        private String naverLink;
        private LocalDate publishedDate;
        private Boolean isFromCache;
    }

    @Getter
    @Setter
    public static class Statistics {
        private Long totalBooks;
        private Long cachedBooks;
        private Long recentlyAdded; // 최근 7일
        private Double cacheHitRate;
        private LocalDateTime lastUpdated;
    }

    // 네이버 API 응답 DTO
    @Getter
    @Setter
    public static class NaverApiResponse {
        private String lastBuildDate;
        private Integer total;
        private Integer start;
        private Integer display;
        private List<NaverBookItem> items;
    }

    @Getter
    @Setter
    public static class NaverBookItem {
        private String title;
        private String link;
        private String image;
        private String author;
        private String discount;
        private String publisher;
        private String isbn;
        private String description;
        private String pubdate;
        private String price;
        
        // 네이버 API 응답에서 HTML 태그 제거
        public String getCleanTitle() {
            return title != null ? title.replaceAll("<[^>]*>", "") : null;
        }
        
        public String getCleanDescription() {
            return description != null ? description.replaceAll("<[^>]*>", "") : null;
        }
        
        public Integer getPriceAsInteger() {
            try {
                return price != null && !price.isEmpty() ? Integer.parseInt(price) : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        public Integer getDiscountAsInteger() {
            try {
                return discount != null && !discount.isEmpty() ? Integer.parseInt(discount) : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    @Getter
    @Setter
    public static class CacheStatus {
        private String isbn;
        private Boolean isCached;
        private LocalDateTime lastSyncedAt;
        private Boolean needsUpdate;
        private String cacheSource; // "database", "redis", "api"
    }

    @Getter
    @Setter
    public static class BookListResponse {
        private List<Summary> books;
        private Integer totalElements;
        private Integer totalPages;
        private Integer currentPage;
        private Integer size;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }
} 