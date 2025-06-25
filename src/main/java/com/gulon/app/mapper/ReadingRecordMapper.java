package com.gulon.app.mapper;

import com.gulon.app.dto.ReadingRecordDto;
import com.gulon.app.entity.ReadingRecord;
import com.gulon.app.entity.User;
import com.gulon.app.entity.Book;
import org.mapstruct.*;

import java.util.List;

/**
 * 독서 기록 MapStruct 매퍼
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReadingRecordMapper {

    /**
     * CreateRequest를 엔티티로 변환
     * User와 Book은 서비스에서 별도로 설정
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "currentPage", constant = "0")
    @Mapping(target = "status", constant = "READING")
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ReadingRecord toEntity(ReadingRecordDto.CreateRequest request);

    /**
     * 엔티티를 Response DTO로 변환
     */
    @Mapping(source = "user", target = "user")
    @Mapping(source = "book", target = "book")
    @Mapping(target = "progressPercentage", expression = "java(calculateProgressPercentage(entity.getCurrentPage(), entity.getTotalPages()))")
    @Mapping(target = "remainingPages", expression = "java(calculateRemainingPages(entity.getCurrentPage(), entity.getTotalPages()))")
    ReadingRecordDto.Response toResponseDto(ReadingRecord entity);

    /**
     * User를 UserInfo로 변환
     */
    @Mapping(source = "publicId", target = "publicId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "email", target = "email")
    ReadingRecordDto.Response.UserInfo toUserInfo(User user);

    /**
     * Book을 BookInfo로 변환
     */
    @Mapping(source = "publicId", target = "publicId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "author", target = "author")
    @Mapping(source = "imageUrl", target = "imageUrl")
    @Mapping(source = "publisher", target = "publisher")
    @Mapping(source = "publishedDate", target = "publishedDate")
    ReadingRecordDto.Response.BookInfo toBookInfo(Book book);

    /**
     * 엔티티를 Summary DTO로 변환
     */
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "book.title", target = "bookTitle")
    @Mapping(source = "book.author", target = "bookAuthor")
    @Mapping(source = "book.imageUrl", target = "bookImageUrl")
    @Mapping(target = "progressPercentage", expression = "java(calculateProgressPercentage(entity.getCurrentPage(), entity.getTotalPages()))")
    ReadingRecordDto.Summary toSummaryDto(ReadingRecord entity);

    /**
     * 엔티티 리스트를 Summary DTO 리스트로 변환
     */
    List<ReadingRecordDto.Summary> toSummaryDtoList(List<ReadingRecord> entities);

    /**
     * 엔티티 리스트를 Response DTO 리스트로 변환
     */
    List<ReadingRecordDto.Response> toResponseDtoList(List<ReadingRecord> entities);

    /**
     * UpdateRequest로 엔티티 업데이트
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ReadingRecordDto.UpdateRequest request, @MappingTarget ReadingRecord entity);

    /**
     * ProgressUpdateRequest로 엔티티 업데이트
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "totalPages", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateProgressFromDto(ReadingRecordDto.ProgressUpdateRequest request, @MappingTarget ReadingRecord entity);

    /**
     * 진행률 계산
     */
    default Double calculateProgressPercentage(Integer currentPage, Integer totalPages) {
        if (currentPage == null || totalPages == null || totalPages == 0) {
            return 0.0;
        }
        return Math.min(100.0, (currentPage.doubleValue() / totalPages.doubleValue()) * 100.0);
    }

    /**
     * 남은 페이지 계산
     */
    default Integer calculateRemainingPages(Integer currentPage, Integer totalPages) {
        if (currentPage == null || totalPages == null) {
            return totalPages != null ? totalPages : 0;
        }
        return Math.max(0, totalPages - currentPage);
    }
} 