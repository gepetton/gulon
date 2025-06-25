package com.gulon.app.controller;

import com.gulon.app.dto.ReadingRecordDto;
import com.gulon.app.entity.ReadingRecord;
import com.gulon.app.service.ReadingRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/**
 * 독서 기록 관리 컨트롤러
 */
@Tag(name = "독서 기록 관리", description = "독서 기록 생성, 조회, 수정, 삭제 및 독서 통계 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/reading-records")
@RequiredArgsConstructor
@Validated
public class ReadingRecordController {
    
    private final ReadingRecordService readingRecordService;
    
    @Operation(
        summary = "독서 기록 생성",
        description = "새로운 독서 기록을 생성합니다. 사용자가 특정 책에 대한 독서를 시작할 때 사용하며, " +
                     "시작일이 지정되지 않으면 현재 날짜로 자동 설정됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "독서 기록 생성 성공",
                    content = @Content(schema = @Schema(implementation = ReadingRecordDto.Response.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "사용자 또는 책을 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "이미 해당 책에 대한 독서 기록이 존재함")
    })
    @PostMapping
    public ResponseEntity<ReadingRecordDto.Response> createReadingRecord(
            @Parameter(description = "생성할 독서 기록 정보", required = true)
            @Valid @RequestBody ReadingRecordDto.CreateRequest request) {
        log.info("독서 기록 생성 API 호출: userPublicId={}, bookPublicId={}", 
                request.getUserPublicId(), request.getBookPublicId());
        
        ReadingRecordDto.Response response = readingRecordService.createReadingRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
        summary = "독서 기록 상세 조회",
        description = "ID를 통해 특정 독서 기록의 상세 정보를 조회합니다. 진행률, 남은 페이지 등의 계산된 정보도 포함됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ReadingRecordDto.Response.class))),
        @ApiResponse(responseCode = "404", description = "독서 기록을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReadingRecordDto.Response> getReadingRecord(
            @Parameter(description = "조회할 독서 기록 ID", required = true)
            @PathVariable Integer id) {
        log.info("독서 기록 조회 API 호출: id={}", id);
        
        Optional<ReadingRecordDto.Response> response = readingRecordService.getReadingRecordById(id);
        if (response.isPresent()) {
            return ResponseEntity.ok(response.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(
        summary = "독서 기록 수정",
        description = "기존 독서 기록의 정보를 수정합니다. 현재 페이지, 상태, 시작/종료일 등을 업데이트할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ReadingRecordDto.Response.class))),
        @ApiResponse(responseCode = "404", description = "독서 기록을 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "잘못된 수정 정보")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReadingRecordDto.Response> updateReadingRecord(
            @Parameter(description = "수정할 독서 기록 ID", required = true)
            @PathVariable Integer id,
            @Parameter(description = "수정할 독서 기록 정보", required = true)
            @Valid @RequestBody ReadingRecordDto.UpdateRequest request) {
        log.info("독서 기록 수정 API 호출: id={}", id);
        
        ReadingRecordDto.Response response = readingRecordService.updateReadingRecord(id, request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "독서 진행률 업데이트",
        description = "독서 진행률을 빠르게 업데이트합니다. 현재 페이지와 상태만 변경할 때 사용하며, " +
                     "현재 페이지가 총 페이지에 도달하면 자동으로 완료 상태로 변경됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "진행률 업데이트 성공",
                    content = @Content(schema = @Schema(implementation = ReadingRecordDto.Response.class))),
        @ApiResponse(responseCode = "404", description = "독서 기록을 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "잘못된 진행률 정보")
    })
    @PatchMapping("/{id}/progress")
    public ResponseEntity<ReadingRecordDto.Response> updateProgress(
            @Parameter(description = "업데이트할 독서 기록 ID", required = true)
            @PathVariable Integer id,
            @Parameter(description = "진행률 업데이트 정보", required = true)
            @Valid @RequestBody ReadingRecordDto.ProgressUpdateRequest request) {
        log.info("독서 진행률 업데이트 API 호출: id={}, currentPage={}", id, request.getCurrentPage());
        
        ReadingRecordDto.Response response = readingRecordService.updateProgress(id, request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "독서 기록 삭제",
        description = "지정된 독서 기록을 삭제합니다. 삭제된 데이터는 복구할 수 없습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "독서 기록을 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReadingRecord(
            @Parameter(description = "삭제할 독서 기록 ID", required = true)
            @PathVariable Integer id) {
        log.info("독서 기록 삭제 API 호출: id={}", id);
        
        readingRecordService.deleteReadingRecord(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
        summary = "모든 독서 기록 조회 (페이징)",
        description = "전체 독서 기록을 페이징하여 조회합니다. 관리자용 기능으로 모든 사용자의 독서 기록을 확인할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ReadingRecordDto.ListResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ReadingRecordDto.ListResponse> getAllReadingRecords(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) Integer size,
            @Parameter(description = "정렬 기준 (updatedAt, createdAt, startDate 등)", example = "updatedAt")
            @RequestParam(defaultValue = "updatedAt") String sort) {
        log.info("전체 독서 기록 조회 API 호출: page={}, size={}, sort={}", page, size, sort);
        
        ReadingRecordDto.ListResponse response = readingRecordService.getAllReadingRecords(page, size, sort);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "사용자별 독서 기록 조회",
        description = "특정 사용자의 독서 기록 목록을 페이징하여 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ReadingRecordDto.ListResponse.class))),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/users/{userPublicId}")
    public ResponseEntity<ReadingRecordDto.ListResponse> getReadingRecordsByUser(
            @Parameter(description = "조회할 사용자의 공개 ID", required = true)
            @PathVariable UUID userPublicId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        log.info("사용자별 독서 기록 조회 API 호출: userPublicId={}", userPublicId);
        
        ReadingRecordDto.ListResponse response = readingRecordService.getReadingRecordsByUser(userPublicId, page, size);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "책별 독서 기록 조회",
        description = "특정 책에 대한 모든 사용자의 독서 기록을 페이징하여 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ReadingRecordDto.ListResponse.class))),
        @ApiResponse(responseCode = "404", description = "책을 찾을 수 없음")
    })
    @GetMapping("/books/{bookPublicId}")
    public ResponseEntity<ReadingRecordDto.ListResponse> getReadingRecordsByBook(
            @Parameter(description = "조회할 책의 공개 ID", required = true)
            @PathVariable UUID bookPublicId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        log.info("책별 독서 기록 조회 API 호출: bookPublicId={}", bookPublicId);
        
        ReadingRecordDto.ListResponse response = readingRecordService.getReadingRecordsByBook(bookPublicId, page, size);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "상태별 독서 기록 조회",
        description = "특정 독서 상태(읽는 중, 완료, 일시중단)의 독서 기록들을 페이징하여 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ReadingRecordDto.ListResponse.class)))
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<ReadingRecordDto.ListResponse> getReadingRecordsByStatus(
            @Parameter(description = "독서 상태 (READING, COMPLETED, PAUSED)", required = true)
            @PathVariable ReadingRecord.ReadingStatus status,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        log.info("상태별 독서 기록 조회 API 호출: status={}", status);
        
        ReadingRecordDto.ListResponse response = readingRecordService.getReadingRecordsByStatus(status, page, size);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "사용자 독서 현황 조회",
        description = "사용자의 전체적인 독서 현황을 조회합니다. 현재 읽고 있는 책, 최근 완독한 책, 독서 통계 등을 포함합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ReadingRecordDto.UserReadingStatus.class))),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/users/{userPublicId}/status")
    public ResponseEntity<ReadingRecordDto.UserReadingStatus> getUserReadingStatus(
            @Parameter(description = "조회할 사용자의 공개 ID", required = true)
            @PathVariable UUID userPublicId) {
        log.info("사용자 독서 현황 조회 API 호출: userPublicId={}", userPublicId);
        
        ReadingRecordDto.UserReadingStatus response = readingRecordService.getUserReadingStatus(userPublicId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "사용자 독서 통계 조회",
        description = "사용자의 독서 통계 정보를 조회합니다. 완독률, 평균 진행률, 총 읽은 페이지 수 등의 정보를 제공합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ReadingRecordDto.ProgressStats.class))),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/users/{userPublicId}/stats")
    public ResponseEntity<ReadingRecordDto.ProgressStats> getUserProgressStats(
            @Parameter(description = "조회할 사용자의 공개 ID", required = true)
            @PathVariable UUID userPublicId) {
        log.info("사용자 독서 통계 조회 API 호출: userPublicId={}", userPublicId);
        
        ReadingRecordDto.ProgressStats response = readingRecordService.getUserProgressStats(userPublicId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "책별 독서 현황 조회",
        description = "특정 책에 대한 전체적인 독서 현황을 조회합니다. 총 독자 수, 완독자 수, 평균 진행률 등의 정보를 제공합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ReadingRecordDto.BookReadingStatus.class))),
        @ApiResponse(responseCode = "404", description = "책을 찾을 수 없음")
    })
    @GetMapping("/books/{bookPublicId}/status")
    public ResponseEntity<ReadingRecordDto.BookReadingStatus> getBookReadingStatus(
            @Parameter(description = "조회할 책의 공개 ID", required = true)
            @PathVariable UUID bookPublicId) {
        log.info("책별 독서 현황 조회 API 호출: bookPublicId={}", bookPublicId);
        
        ReadingRecordDto.BookReadingStatus response = readingRecordService.getBookReadingStatus(bookPublicId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "전체 독서 통계 조회",
        description = "플랫폼 전체의 독서 통계를 조회합니다. 총 독서 기록 수, 완독률, 평균 완독 소요일 등의 정보를 제공합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ReadingRecordDto.Statistics.class)))
    })
    @GetMapping("/statistics")
    public ResponseEntity<ReadingRecordDto.Statistics> getGlobalStatistics() {
        log.info("전체 독서 통계 조회 API 호출");
        
        ReadingRecordDto.Statistics response = readingRecordService.getGlobalStatistics();
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "독서 기록 존재 여부 확인",
        description = "특정 사용자가 특정 책에 대한 독서 기록을 가지고 있는지 확인합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "확인 완료 (true: 존재함, false: 존재하지 않음)"),
        @ApiResponse(responseCode = "404", description = "사용자 또는 책을 찾을 수 없음")
    })
    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkReadingRecordExists(
            @Parameter(description = "사용자 공개 ID", required = true)
            @RequestParam UUID userPublicId,
            @Parameter(description = "책 공개 ID", required = true)
            @RequestParam UUID bookPublicId) {
        log.info("독서 기록 존재 여부 확인 API 호출: userPublicId={}, bookPublicId={}", userPublicId, bookPublicId);
        
        boolean exists = readingRecordService.existsReadingRecord(userPublicId, bookPublicId);
        return ResponseEntity.ok(exists);
    }
} 