package com.gulon.app.controller;

import com.gulon.app.dto.GroupDto;
import com.gulon.app.service.GroupService;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Tag(name = "그룹 관리", description = "독서 그룹 생성, 조회, 수정, 삭제 및 그룹 멤버 관리 API")
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {
    
    private final GroupService groupService;
    
    @Operation(
        summary = "그룹 생성",
        description = "새로운 독서 그룹을 생성합니다. 그룹명, 설명, 공개 여부 등을 설정할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "그룹 생성 성공",
                    content = @Content(schema = @Schema(implementation = GroupDto.CreateResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 그룹 정보"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 그룹명")
    })
    @PostMapping
    public ResponseEntity<GroupDto.CreateResponse> createGroup(
            @Parameter(description = "생성할 그룹 정보", required = true)
            @RequestBody GroupDto.CreateRequest request) {
        log.info("Creating group: {}", request.getName());
        
        GroupDto.CreateResponse response = groupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
        summary = "그룹 상세 조회",
        description = "특정 그룹의 상세 정보를 조회합니다. 멤버 목록, 진행 중인 독서 등의 정보를 포함합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GroupDto.Response.class))),
        @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "비공개 그룹 접근 권한 없음")
    })
    @GetMapping("/{groupPublicId}")
    public ResponseEntity<GroupDto.Response> getGroup(
            @Parameter(description = "조회할 그룹의 공개 ID", required = true)
            @PathVariable UUID groupPublicId,
            @Parameter(description = "요청 사용자의 공개 ID (권한 확인용)")
            @RequestParam(required = false) UUID requestUserPublicId) {
        log.info("Getting group: {}", groupPublicId);
        
        Optional<GroupDto.Response> response = groupService.getGroup(groupPublicId, requestUserPublicId);
        if (response.isPresent()) {
            return ResponseEntity.ok(response.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(
        summary = "그룹 수정",
        description = "기존 그룹의 정보를 수정합니다. 그룹 관리자만 수정할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = GroupDto.Response.class))),
        @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "수정 권한 없음 (관리자만 가능)")
    })
    @PutMapping("/{groupPublicId}")
    public ResponseEntity<GroupDto.Response> updateGroup(
            @Parameter(description = "수정할 그룹의 공개 ID", required = true)
            @PathVariable UUID groupPublicId,
            @Parameter(description = "수정할 그룹 정보", required = true)
            @RequestBody GroupDto.UpdateRequest request,
            @Parameter(description = "요청 사용자의 공개 ID (권한 확인용)", required = true)
            @RequestParam UUID requestUserPublicId) {
        log.info("Updating group: {}", groupPublicId);
        
        GroupDto.Response response = groupService.updateGroup(groupPublicId, request, requestUserPublicId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "그룹 삭제",
        description = "그룹을 삭제합니다. 그룹 관리자만 삭제할 수 있으며, 삭제된 그룹의 데이터는 복구할 수 없습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (관리자만 가능)")
    })
    @DeleteMapping("/{groupPublicId}")
    public ResponseEntity<Void> deleteGroup(
            @Parameter(description = "삭제할 그룹의 공개 ID", required = true)
            @PathVariable UUID groupPublicId,
            @Parameter(description = "요청 사용자의 공개 ID (권한 확인용)", required = true)
            @RequestParam UUID requestUserPublicId) {
        log.info("Deleting group: {}", groupPublicId);
        
        groupService.deleteGroup(groupPublicId, requestUserPublicId);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
        summary = "공개 그룹 목록 조회",
        description = "모든 공개 그룹의 목록을 페이징하여 조회합니다. 정렬 기준과 방향을 지정할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GroupDto.SearchResult.class)))
    })
    @GetMapping("/public")
    public ResponseEntity<GroupDto.SearchResult> getPublicGroups(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준 (created_at, updated_at, name, member_count)", example = "created_at")
            @RequestParam(defaultValue = "created_at") String sortBy,
            @Parameter(description = "정렬 방향 (asc, desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDirection) {
        log.info("Getting public groups - page: {}, size: {}", page, size);
        
        GroupDto.SearchResult result = groupService.getPublicGroups(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(result);
    }
    
    @Operation(
        summary = "그룹 검색",
        description = "키워드를 통해 그룹을 검색합니다. 그룹명, 설명 등에서 키워드를 찾아 결과를 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(schema = @Schema(implementation = GroupDto.SearchResult.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<GroupDto.SearchResult> searchGroups(
            @Parameter(description = "검색 키워드 (그룹명, 설명에서 검색)", example = "자바 스터디")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        log.info("Searching groups with keyword: {}", keyword);
        
        GroupDto.SearchResult result = groupService.searchGroups(keyword, page, size);
        return ResponseEntity.ok(result);
    }
    
    @Operation(
        summary = "사용자별 소유 그룹 조회",
        description = "특정 사용자가 관리자로 있는 그룹 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/owned")
    public ResponseEntity<List<GroupDto.Summary>> getOwnedGroups(
            @Parameter(description = "조회할 사용자의 공개 ID", required = true)
            @RequestParam UUID userPublicId) {
        log.info("Getting owned groups for user: {}", userPublicId);
        
        List<GroupDto.Summary> groups = groupService.getOwnedGroups(userPublicId);
        return ResponseEntity.ok(groups);
    }
    
    @Operation(
        summary = "사용자별 가입 그룹 조회",
        description = "특정 사용자가 멤버로 참여 중인 그룹 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/memberships")
    public ResponseEntity<List<GroupDto.Summary>> getUserGroups(
            @Parameter(description = "조회할 사용자의 공개 ID", required = true)
            @RequestParam UUID userPublicId) {
        log.info("Getting user groups for: {}", userPublicId);
        
        List<GroupDto.Summary> groups = groupService.getUserGroups(userPublicId);
        return ResponseEntity.ok(groups);
    }
    
    @Operation(
        summary = "그룹 통계 조회",
        description = "그룹의 상세 통계 정보를 조회합니다. 멤버 수, 활동량, 독서 진행률 등의 정보를 제공합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GroupDto.Statistics.class))),
        @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "통계 조회 권한 없음")
    })
    @GetMapping("/{groupPublicId}/statistics")
    public ResponseEntity<GroupDto.Statistics> getGroupStatistics(
            @Parameter(description = "통계를 조회할 그룹의 공개 ID", required = true)
            @PathVariable UUID groupPublicId,
            @Parameter(description = "요청 사용자의 공개 ID (권한 확인용)", required = true)
            @RequestParam UUID requestUserPublicId) {
        log.info("Getting group statistics for: {}", groupPublicId);
        
        GroupDto.Statistics statistics = groupService.getGroupStatistics(groupPublicId, requestUserPublicId);
        return ResponseEntity.ok(statistics);
    }
    
    @Operation(
        summary = "그룹 존재 여부 확인",
        description = "특정 공개 ID를 가진 그룹이 존재하는지 확인합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "확인 완료 (true: 존재함, false: 존재하지 않음)")
    })
    @GetMapping("/{groupPublicId}/exists")
    public ResponseEntity<Boolean> checkGroupExists(
            @Parameter(description = "존재 여부를 확인할 그룹의 공개 ID", required = true)
            @PathVariable UUID groupPublicId) {
        log.info("Checking if group exists: {}", groupPublicId);
        
        Optional<GroupDto.Response> response = groupService.getGroup(groupPublicId, null);
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