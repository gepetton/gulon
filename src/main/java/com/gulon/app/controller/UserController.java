package com.gulon.app.controller;

import com.gulon.app.dto.UserDto;
import com.gulon.app.entity.User;
import com.gulon.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 사용자 관리 컨트롤러
 */
@Tag(name = "사용자 관리", description = "사용자 회원가입, 조회, 수정, 삭제 및 계정 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @Operation(
        summary = "사용자 생성",
        description = "새로운 사용자를 생성합니다. 이메일은 중복될 수 없으며, 비밀번호는 암호화되어 저장됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "사용자 생성 성공",
                    content = @Content(schema = @Schema(implementation = UserDto.Response.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
    @PostMapping
    public ResponseEntity<UserDto.Response> createUser(
            @Parameter(description = "생성할 사용자 정보", required = true)
            @RequestBody UserDto.CreateRequest request) {
        log.info("사용자 생성 API 호출: email={}", request.getEmail());
        
        UserDto.Response response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
        summary = "사용자 조회 (Public ID)",
        description = "공개 ID를 통해 특정 사용자의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserDto.Response.class))),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{publicId}")
    public ResponseEntity<UserDto.Response> getUserByPublicId(
            @Parameter(description = "사용자 공개 ID", required = true)
            @PathVariable UUID publicId) {
        log.info("사용자 조회 API 호출: publicId={}", publicId);
        
        return userService.getUserByPublicId(publicId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(
        summary = "모든 사용자 조회",
        description = "등록된 모든 사용자를 조회합니다. 페이징과 정렬 기능을 지원합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 페이징 파라미터")
    })
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준 (id, email, name, createdAt 등)", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "정렬 방향 (asc, desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("사용자 목록 조회 API 호출: page={}, size={}", page, size);
        
        // 페이지네이션이 요청된 경우
        if (page > 0 || size != 20) {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<UserDto.Summary> pagedUsers = userService.getUsersWithPaging(pageable);
            return ResponseEntity.ok(pagedUsers);
        }
        
        // 전체 조회
        List<UserDto.Summary> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @Operation(
        summary = "사용자 수정",
        description = "기존 사용자의 정보를 수정합니다. 수정 가능한 필드는 이름, 비밀번호, 프로필 이미지 등입니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = UserDto.Response.class))),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "잘못된 수정 정보")
    })
    @PutMapping("/{publicId}")
    public ResponseEntity<UserDto.Response> updateUser(
            @Parameter(description = "수정할 사용자의 공개 ID", required = true)
            @PathVariable UUID publicId,
            @Parameter(description = "수정할 사용자 정보", required = true)
            @RequestBody UserDto.UpdateRequest request) {
        log.info("사용자 수정 API 호출: publicId={}", publicId);
        
        UserDto.Response response = userService.updateUserByPublicId(publicId, request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "사용자 삭제",
        description = "지정된 사용자를 삭제합니다. 삭제된 사용자의 데이터는 복구할 수 없습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "삭제할 사용자의 공개 ID", required = true)
            @PathVariable UUID publicId) {
        log.info("사용자 삭제 API 호출: publicId={}", publicId);
        
        userService.deleteByPublicId(publicId);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
        summary = "이메일로 사용자 조회",
        description = "이메일 주소를 통해 사용자 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserDto.Response.class))),
        @ApiResponse(responseCode = "404", description = "해당 이메일의 사용자를 찾을 수 없음")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto.Response> getUserByEmail(
            @Parameter(description = "조회할 사용자의 이메일", required = true, example = "user@example.com")
            @PathVariable String email) {
        log.info("이메일로 사용자 조회 API 호출: email={}", email);
        
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(
        summary = "이메일 중복 확인",
        description = "회원가입 시 이메일 중복 여부를 확인합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "확인 완료 (true: 중복됨, false: 사용 가능)")
    })
    @GetMapping("/email/{email}/exists")
    public ResponseEntity<Boolean> checkEmailExists(
            @Parameter(description = "중복 확인할 이메일", required = true, example = "user@example.com")
            @PathVariable String email) {
        log.info("이메일 중복 확인 API 호출: email={}", email);
        
        boolean exists = userService.isEmailExists(email);
        return ResponseEntity.ok(exists);
    }
    
    @Operation(
        summary = "상태별 사용자 조회",
        description = "특정 상태(활성, 비활성, 차단 등)의 사용자 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<UserDto.Summary>> getUsersByStatus(
            @Parameter(description = "사용자 상태 (ACTIVE, INACTIVE, BANNED)", required = true)
            @PathVariable User.UserStatus status) {
        log.info("상태별 사용자 조회 API 호출: status={}", status);
        
        List<UserDto.Summary> users = userService.getUsersByStatus(status);
        return ResponseEntity.ok(users);
    }
    
    @Operation(
        summary = "이름으로 사용자 검색",
        description = "사용자 이름으로 검색하여 일치하는 사용자 목록을 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공")
    })
    @GetMapping("/search")
    public ResponseEntity<List<UserDto.Summary>> searchUsers(
            @Parameter(description = "검색할 사용자 이름", required = true, example = "홍길동")
            @RequestParam String name) {
        log.info("사용자 검색 API 호출: name={}", name);
        
        List<UserDto.Summary> users = userService.searchUsersByName(name);
        return ResponseEntity.ok(users);
    }
    
    @Operation(
        summary = "기간별 가입자 조회",
        description = "지정된 기간 동안 가입한 사용자 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식")
    })
    @GetMapping("/date-range")
    public ResponseEntity<List<UserDto.Summary>> getUsersByDateRange(
            @Parameter(description = "시작 날짜 (ISO 8601 형식)", required = true, example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜 (ISO 8601 형식)", required = true, example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("기간별 사용자 조회 API 호출: start={}, end={}", startDate, endDate);
        
        List<UserDto.Summary> users = userService.getUsersByDateRange(startDate, endDate);
        return ResponseEntity.ok(users);
    }
    
    @Operation(
        summary = "사용자 상태 변경",
        description = "사용자의 계정 상태를 변경합니다. (활성화, 비활성화, 차단 등)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = UserDto.Response.class))),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PatchMapping("/{publicId}/status")
    public ResponseEntity<UserDto.Response> changeUserStatus(
            @Parameter(description = "상태를 변경할 사용자의 공개 ID", required = true)
            @PathVariable UUID publicId,
            @Parameter(description = "새로운 사용자 상태", required = true)
            @RequestParam User.UserStatus status) {
        log.info("사용자 상태 변경 API 호출: publicId={}, status={}", publicId, status);
        
        UserDto.Response response = userService.changeUserStatusByPublicId(publicId, status);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "통계 정보 조회",
        description = "사용자 관련 전체 통계 정보를 조회합니다. 총 사용자 수, 활성 사용자 수, 최근 가입자 등의 정보를 제공합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "통계 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserStatsResponse.class)))
    })
    @GetMapping("/stats")
    public ResponseEntity<UserStatsResponse> getUserStats() {
        log.info("사용자 통계 조회 API 호출");
        
        UserStatsResponse stats = new UserStatsResponse();
        stats.setTotalUsers(userService.count());
        stats.setActiveUsers(userService.getActiveUserCount());
        stats.setInactiveUsers(userService.getUsersByStatus(User.UserStatus.INACTIVE).size());
        stats.setBannedUsers(userService.getUsersByStatus(User.UserStatus.BANNED).size());
        stats.setRecentUsers(userService.getRecentUsers(10));
        
        return ResponseEntity.ok(stats);
    }
    
    @Schema(description = "사용자 통계 응답 데이터")
    @lombok.Getter
    @lombok.Setter
    public static class UserStatsResponse {
        @Schema(description = "전체 사용자 수", example = "1500")
        private long totalUsers;
        @Schema(description = "활성 사용자 수", example = "1200")
        private long activeUsers;
        @Schema(description = "비활성 사용자 수", example = "280")
        private long inactiveUsers;
        @Schema(description = "차단된 사용자 수", example = "20")
        private long bannedUsers;
        @Schema(description = "최근 가입한 사용자 목록 (최대 10명)")
        private List<UserDto.Summary> recentUsers;
    }
}
