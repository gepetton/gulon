package com.gulon.app.controller;

import com.gulon.app.dto.MessageDto;
import com.gulon.app.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Tag(name = "메시지 관리", description = "그룹 채팅 메시지 전송, 조회, 수정, 삭제 및 채팅 히스토리 관리 API")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    
    private final MessageService messageService;
    
    @Operation(
        summary = "메시지 전송",
        description = "그룹 채팅방에 새로운 메시지를 전송합니다. 텍스트, 이미지, 파일 등 다양한 형태의 메시지를 지원합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "메시지 전송 성공",
                    content = @Content(schema = @Schema(implementation = MessageDto.SendResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 메시지 데이터"),
        @ApiResponse(responseCode = "403", description = "메시지 전송 권한 없음 (그룹 멤버가 아님)")
    })
    @PostMapping
    public ResponseEntity<MessageDto.SendResponse> sendMessage(
            @Parameter(description = "전송할 메시지 정보", required = true)
            @RequestBody MessageDto.SendRequest request) {
        log.info("Sending message to group: {}", request.getGroupPublicId());
        
        MessageDto.SendResponse response = messageService.sendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
        summary = "메시지 조회",
        description = "특정 메시지의 상세 정보를 조회합니다. 메시지 내용, 전송자, 전송 시간 등의 정보를 제공합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MessageDto.Response.class))),
        @ApiResponse(responseCode = "404", description = "메시지를 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "메시지 조회 권한 없음")
    })
    @GetMapping("/{messagePublicId}")
    public ResponseEntity<MessageDto.Response> getMessage(
            @Parameter(description = "조회할 메시지의 공개 ID", required = true)
            @PathVariable UUID messagePublicId,
            @Parameter(description = "요청 사용자의 공개 ID (권한 확인용)")
            @RequestParam(required = false) UUID requestUserPublicId) {
        log.info("Getting message: {}", messagePublicId);
        
        Optional<MessageDto.Response> response = messageService.getMessage(messagePublicId, requestUserPublicId);
        if (response.isPresent()) {
            return ResponseEntity.ok(response.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(
        summary = "메시지 수정",
        description = "이미 전송된 메시지의 내용을 수정합니다. 메시지 작성자만 수정할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = MessageDto.Response.class))),
        @ApiResponse(responseCode = "404", description = "메시지를 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "수정 권한 없음 (작성자만 가능)")
    })
    @PutMapping("/{messagePublicId}")
    public ResponseEntity<MessageDto.Response> editMessage(
            @Parameter(description = "수정할 메시지의 공개 ID", required = true)
            @PathVariable UUID messagePublicId,
            @Parameter(description = "수정할 메시지 내용", required = true)
            @RequestBody MessageDto.EditRequest request,
            @Parameter(description = "요청 사용자의 공개 ID (권한 확인용)", required = true)
            @RequestParam UUID requestUserPublicId) {
        log.info("Editing message: {}", messagePublicId);
        
        MessageDto.Response response = messageService.editMessage(messagePublicId, request, requestUserPublicId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "메시지 삭제",
        description = "메시지를 삭제합니다. 메시지 작성자 또는 그룹 관리자만 삭제할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "메시지를 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "삭제 권한 없음")
    })
    @DeleteMapping("/{messagePublicId}")
    public ResponseEntity<Void> deleteMessage(
            @Parameter(description = "삭제할 메시지의 공개 ID", required = true)
            @PathVariable UUID messagePublicId,
            @Parameter(description = "요청 사용자의 공개 ID (권한 확인용)", required = true)
            @RequestParam UUID requestUserPublicId) {
        log.info("Deleting message: {}", messagePublicId);
        
        messageService.deleteMessage(messagePublicId, requestUserPublicId);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
        summary = "그룹 채팅 히스토리 조회",
        description = "특정 그룹의 채팅 히스토리를 페이징하여 조회합니다. 최신 메시지부터 시간 역순으로 정렬됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MessageDto.HistoryResponse.class))),
        @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "채팅 히스토리 조회 권한 없음")
    })
    @GetMapping("/groups/{groupPublicId}/history")
    public ResponseEntity<MessageDto.HistoryResponse> getGroupChatHistory(
            @Parameter(description = "채팅 히스토리를 조회할 그룹의 공개 ID", required = true)
            @PathVariable UUID groupPublicId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)", example = "50")
            @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "요청 사용자의 공개 ID (권한 확인용)")
            @RequestParam(required = false) UUID requestUserPublicId) {
        log.info("Getting chat history for group: {}, page: {}, size: {}", groupPublicId, page, size);
        
        MessageDto.HistoryResponse response = messageService.getGroupChatHistory(
                groupPublicId, page, size, requestUserPublicId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "메시지 검색",
        description = "키워드, 작성자, 날짜 범위 등의 조건으로 메시지를 검색합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(schema = @Schema(implementation = MessageDto.SearchResult.class)))
    })
    @PostMapping("/search")
    public ResponseEntity<MessageDto.SearchResult> searchMessages(
            @Parameter(description = "검색 조건 (키워드, 작성자, 날짜 범위 등)", required = true)
            @RequestBody MessageDto.SearchFilter filter,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        log.info("Searching messages with keyword: {}", filter.getKeyword());
        
        MessageDto.SearchResult result = messageService.searchMessages(filter, page, size);
        return ResponseEntity.ok(result);
    }
    
    @Operation(
        summary = "그룹 채팅 현황 조회",
        description = "그룹의 채팅 현황 정보를 조회합니다. 온라인 멤버 수, 최신 메시지, 읽지 않은 메시지 수 등을 제공합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MessageDto.GroupChatStatus.class))),
        @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "채팅 현황 조회 권한 없음")
    })
    @GetMapping("/groups/{groupPublicId}/status")
    public ResponseEntity<MessageDto.GroupChatStatus> getGroupChatStatus(
            @Parameter(description = "채팅 현황을 조회할 그룹의 공개 ID", required = true)
            @PathVariable UUID groupPublicId,
            @Parameter(description = "요청 사용자의 공개 ID (권한 확인용)")
            @RequestParam(required = false) UUID requestUserPublicId) {
        log.info("Getting chat status for group: {}", groupPublicId);
        
        MessageDto.GroupChatStatus status = messageService.getGroupChatStatus(groupPublicId, requestUserPublicId);
        return ResponseEntity.ok(status);
    }
    
    @Operation(
        summary = "메시지 통계 조회",
        description = "그룹의 메시지 관련 통계 정보를 조회합니다. 총 메시지 수, 일일 메시지 수, 활발한 멤버 등의 정보를 제공합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MessageDto.Statistics.class))),
        @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "통계 조회 권한 없음")
    })
    @GetMapping("/groups/{groupPublicId}/statistics")
    public ResponseEntity<MessageDto.Statistics> getMessageStatistics(
            @Parameter(description = "통계를 조회할 그룹의 공개 ID", required = true)
            @PathVariable UUID groupPublicId,
            @Parameter(description = "요청 사용자의 공개 ID (권한 확인용)", required = true)
            @RequestParam UUID requestUserPublicId) {
        log.info("Getting message statistics for group: {}", groupPublicId);
        
        MessageDto.Statistics statistics = messageService.getMessageStatistics(groupPublicId, requestUserPublicId);
        return ResponseEntity.ok(statistics);
    }
    
    @Operation(
        summary = "그룹의 최근 메시지 조회 (간단한 API)",
        description = "그룹의 최신 메시지들을 간단히 조회합니다. 채팅방 미리보기 등에 사용할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MessageDto.HistoryResponse.class)))
    })
    @GetMapping("/groups/{groupPublicId}/recent")
    public ResponseEntity<MessageDto.HistoryResponse> getRecentMessages(
            @Parameter(description = "최근 메시지를 조회할 그룹의 공개 ID", required = true)
            @PathVariable UUID groupPublicId,
            @Parameter(description = "조회할 메시지 수 (최대 50)", example = "20")
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "요청 사용자의 공개 ID (권한 확인용)")
            @RequestParam(required = false) UUID requestUserPublicId) {
        log.info("Getting recent messages for group: {}, limit: {}", groupPublicId, limit);
        
        // 첫 페이지만 조회하여 최근 메시지 반환
        MessageDto.HistoryResponse response = messageService.getGroupChatHistory(
                groupPublicId, 0, limit, requestUserPublicId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "메시지 존재 여부 확인",
        description = "특정 공개 ID를 가진 메시지가 존재하는지 확인합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "확인 완료 (true: 존재함, false: 존재하지 않음)")
    })
    @GetMapping("/{messagePublicId}/exists")
    public ResponseEntity<Boolean> checkMessageExists(
            @Parameter(description = "존재 여부를 확인할 메시지의 공개 ID", required = true)
            @PathVariable UUID messagePublicId) {
        log.info("Checking if message exists: {}", messagePublicId);
        
        Optional<MessageDto.Response> response = messageService.getMessage(messagePublicId, null);
        return ResponseEntity.ok(response.isPresent());
    }
    
    // 에러 핸들링
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse("BAD_REQUEST", e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error: ", e);
        ErrorResponse errorResponse = new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    @Schema(description = "에러 응답 데이터")
    public static class ErrorResponse {
        @Schema(description = "에러 코드", example = "BAD_REQUEST")
        private String code;
        @Schema(description = "에러 메시지", example = "잘못된 요청입니다.")
        private String message;
        
        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
    }
} 