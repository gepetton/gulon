package com.gulon.app.dto;

import com.gulon.app.entity.ReadingRecord;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 독서 기록 관련 DTO 모음
 */
public class ReadingRecordDto {

    /**
     * 독서 기록 생성 요청 DTO
     */
    @Getter
    @Setter
    public static class CreateRequest {
        private UUID userPublicId;
        private UUID bookPublicId;
        private Integer totalPages;
        private LocalDate startDate;
    }

    /**
     * 독서 기록 수정 요청 DTO
     */
    @Getter
    @Setter
    public static class UpdateRequest {
        private Integer currentPage;
        private Integer totalPages;
        private ReadingRecord.ReadingStatus status;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    /**
     * 독서 기록 진행 상태 업데이트 DTO
     */
    @Getter
    @Setter
    public static class ProgressUpdateRequest {
        private Integer currentPage;
        private ReadingRecord.ReadingStatus status;
    }

    /**
     * 독서 기록 상세 응답 DTO
     */
    @Getter
    @Setter
    public static class Response {
        private Integer id;
        private UserInfo user;
        private BookInfo book;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer currentPage;
        private Integer totalPages;
        private ReadingRecord.ReadingStatus status;
        private Double progressPercentage;
        private Integer remainingPages;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        @Getter
        @Setter
        public static class UserInfo {
            private UUID publicId;
            private String name;
            private String email;
        }

        @Getter
        @Setter
        public static class BookInfo {
            private UUID publicId;
            private String title;
            private String author;
            private String imageUrl;
            private String publisher;
            private LocalDate publishedDate;
        }
    }

    /**
     * 독서 기록 요약 정보 DTO (목록용)
     */
    @Getter
    @Setter
    public static class Summary {
        private Integer id;
        private String userName;
        private String bookTitle;
        private String bookAuthor;
        private String bookImageUrl;
        private Integer currentPage;
        private Integer totalPages;
        private ReadingRecord.ReadingStatus status;
        private Double progressPercentage;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime updatedAt;
    }

    /**
     * 독서 진행률 통계 DTO
     */
    @Getter
    @Setter
    public static class ProgressStats {
        private UUID userPublicId;
        private String userName;
        private Long totalBooks;
        private Long completedBooks;
        private Long readingBooks;
        private Long pausedBooks;
        private Double completionRate;
        private Integer totalPagesRead;
        private Double averageProgressPercentage;
    }

    /**
     * 독서 기록 목록 응답 DTO
     */
    @Getter
    @Setter
    public static class ListResponse {
        private List<Summary> records;
        private Integer totalElements;
        private Integer totalPages;
        private Integer currentPage;
        private Integer size;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }

    /**
     * 독서 통계 DTO
     */
    @Getter
    @Setter
    public static class Statistics {
        private Long totalRecords;
        private Long completedRecords;
        private Long readingRecords;
        private Long pausedRecords;
        private Double averageCompletionTime; // 평균 완독 소요일
        private Double averagePagesPerDay;
        private LocalDateTime lastUpdated;
    }

    /**
     * 사용자별 독서 현황 DTO
     */
    @Getter
    @Setter
    public static class UserReadingStatus {
        private UUID userPublicId;
        private String userName;
        private List<Summary> currentlyReading;
        private List<Summary> recentlyCompleted;
        private ProgressStats stats;
    }

    /**
     * 책별 독서 기록 현황 DTO
     */
    @Getter
    @Setter
    public static class BookReadingStatus {
        private UUID bookPublicId;
        private String bookTitle;
        private String bookAuthor;
        private Long totalReaders;
        private Long completedReaders;
        private Double averageProgress;
        private List<Summary> recentRecords;
    }
} 