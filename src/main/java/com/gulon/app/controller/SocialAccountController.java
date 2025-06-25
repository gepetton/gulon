package com.gulon.app.controller;

import com.gulon.app.dto.SocialAccountDto;
import com.gulon.app.entity.SocialAccount;
import com.gulon.app.service.SocialAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 소셜 계정 관리 컨트롤러
 */
@Tag(name = "소셜 계정 관리", description = "카카오, 네이버, 구글 등 소셜 계정 연동 및 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/social-accounts")
@RequiredArgsConstructor
public class SocialAccountController {
    
    private final SocialAccountService socialAccountService;
    
    @Operation(
        summary = "소셜 계정 연결",
        description = "사용자의 계정에 소셜 계정(카카오, 네이버, 구글 등)을 연결합니다. OAuth 토큰 정보를 저장합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "소셜 계정 연결 성공",
                    content = @Content(schema = @Schema(implementation = SocialAccountDto.Response.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 연결 정보"),
        @ApiResponse(responseCode = "409", description = "이미 연결된 소셜 계정")
    })
    @PostMapping
    public ResponseEntity<SocialAccountDto.Response> createSocialAccount(
            @Parameter(description = "연결할 소셜 계정 정보", required = true)
            @RequestBody SocialAccountDto.CreateRequest request) {
        log.info("소셜 계정 연결 API 호출: userPublicId={}, provider={}", 
                request.getUserPublicId(), request.getProvider());
        
        SocialAccountDto.Response response = socialAccountService.createSocialAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
        summary = "소셜 계정 조회",
        description = "ID를 통해 특정 소셜 계정의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SocialAccountDto.Response.class))),
        @ApiResponse(responseCode = "404", description = "소셜 계정을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SocialAccountDto.Response> getSocialAccountById(
            @Parameter(description = "조회할 소셜 계정 ID", required = true)
            @PathVariable Integer id) {
        log.info("소셜 계정 조회 API 호출: id={}", id);
        
        return socialAccountService.getSocialAccountById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(
        summary = "소셜 계정 토큰 업데이트",
        description = "기존 소셜 계정의 액세스 토큰, 리프레시 토큰 등을 업데이트합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "업데이트 성공",
                    content = @Content(schema = @Schema(implementation = SocialAccountDto.Response.class))),
        @ApiResponse(responseCode = "404", description = "소셜 계정을 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SocialAccountDto.Response> updateSocialAccount(
            @Parameter(description = "업데이트할 소셜 계정 ID", required = true)
            @PathVariable Integer id,
            @Parameter(description = "업데이트할 토큰 정보", required = true)
            @RequestBody SocialAccountDto.UpdateRequest request) {
        log.info("소셜 계정 토큰 업데이트 API 호출: id={}", id);
        
        SocialAccountDto.Response response = socialAccountService.updateSocialAccount(id, request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "소셜 계정 삭제",
        description = "사용자의 소셜 계정 연결을 해제합니다. 연결 해제된 계정은 복구할 수 없습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "소셜 계정을 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSocialAccount(
            @Parameter(description = "삭제할 소셜 계정 ID", required = true)
            @PathVariable Integer id) {
        log.info("소셜 계정 삭제 API 호출: id={}", id);
        
        socialAccountService.deleteSocialAccount(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
        summary = "플랫폼별 소셜 계정 조회",
        description = "특정 소셜 플랫폼(카카오, 네이버, 구글)에 연결된 모든 계정을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/provider/{provider}")
    public ResponseEntity<List<SocialAccountDto.Response>> getSocialAccountsByProvider(
            @Parameter(description = "소셜 플랫폼 (KAKAO, NAVER, GOOGLE)", required = true)
            @PathVariable SocialAccount.Provider provider) {
        log.info("플랫폼별 소셜 계정 조회 API 호출: provider={}", provider);
        
        List<SocialAccountDto.Response> accounts = socialAccountService.getSocialAccountsByProvider(provider);
        return ResponseEntity.ok(accounts);
    }
    
    @Operation(
        summary = "만료된 토큰 계정 조회",
        description = "토큰이 만료된 소셜 계정 목록을 조회합니다. 토큰 갱신이 필요한 계정들을 확인할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/expired-tokens")
    public ResponseEntity<List<SocialAccountDto.Response>> getExpiredTokenAccounts() {
        log.info("만료된 토큰 계정 조회 API 호출");
        
        List<SocialAccountDto.Response> expiredAccounts = socialAccountService.getExpiredTokenAccounts();
        return ResponseEntity.ok(expiredAccounts);
    }
    
    @Operation(
        summary = "최근 연결된 계정 조회",
        description = "최근에 연결된 소셜 계정 목록을 조회합니다. 연결 시간 순으로 정렬됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/recent")
    public ResponseEntity<List<SocialAccountDto.Summary>> getRecentlyLinkedAccounts(
            @Parameter(description = "조회할 계정 수 (최대 50)", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        log.info("최근 연결된 계정 조회 API 호출: limit={}", limit);
        
        List<SocialAccountDto.Summary> recentAccounts = socialAccountService.getRecentlyLinkedAccounts(limit);
        return ResponseEntity.ok(recentAccounts);
    }
    
    @Operation(
        summary = "기간별 연결된 계정 조회",
        description = "지정된 기간 동안 연결된 소셜 계정 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식")
    })
    @GetMapping("/date-range")
    public ResponseEntity<List<SocialAccountDto.Summary>> getAccountsByDateRange(
            @Parameter(description = "시작 날짜 (ISO 8601 형식)", required = true, example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜 (ISO 8601 형식)", required = true, example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("기간별 연결된 계정 조회 API 호출: start={}, end={}", startDate, endDate);
        
        List<SocialAccountDto.Summary> accounts = socialAccountService.getAccountsByDateRange(startDate, endDate);
        return ResponseEntity.ok(accounts);
    }
    
    @Operation(
        summary = "토큰 갱신",
        description = "소셜 계정의 액세스 토큰과 리프레시 토큰을 갱신합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = SocialAccountDto.Response.class))),
        @ApiResponse(responseCode = "404", description = "소셜 계정을 찾을 수 없음")
    })
    @PatchMapping("/{id}/refresh-token")
    public ResponseEntity<SocialAccountDto.Response> refreshTokens(
            @Parameter(description = "갱신할 소셜 계정 ID", required = true)
            @PathVariable Integer id,
            @Parameter(description = "새로운 액세스 토큰", required = true)
            @RequestParam String accessToken,
            @Parameter(description = "새로운 리프레시 토큰")
            @RequestParam(required = false) String refreshToken,
            @Parameter(description = "토큰 만료 시간")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime tokenExpiry) {
        log.info("토큰 갱신 API 호출: id={}", id);
        
        SocialAccountDto.Response response = socialAccountService.refreshTokens(id, accessToken, refreshToken, tokenExpiry);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "플랫폼별 연결된 계정 수 조회",
        description = "특정 소셜 플랫폼에 연결된 계정의 총 개수를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/stats/provider/{provider}/count")
    public ResponseEntity<Long> getProviderAccountCount(
            @Parameter(description = "소셜 플랫폼 (KAKAO, NAVER, GOOGLE)", required = true)
            @PathVariable SocialAccount.Provider provider) {
        log.info("플랫폼별 연결된 계정 수 조회 API 호출: provider={}", provider);
        
        long count = socialAccountService.getProviderAccountCount(provider);
        return ResponseEntity.ok(count);
    }
    
    // === 사용자별 소셜 계정 관리 API ===
    
    @Operation(
        summary = "사용자의 모든 소셜 계정 조회",
        description = "특정 사용자에게 연결된 모든 소셜 계정을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/users/{publicId}")
    public ResponseEntity<List<SocialAccountDto.Response>> getUserSocialAccounts(
            @Parameter(description = "조회할 사용자의 공개 ID", required = true)
            @PathVariable UUID publicId) {
        log.info("사용자 소셜 계정 조회 API 호출: publicId={}", publicId);
        
        List<SocialAccountDto.Response> accounts = socialAccountService.getUserSocialAccounts(publicId);
        return ResponseEntity.ok(accounts);
    }
    
    @Operation(
        summary = "사용자 소셜 계정 현황 조회",
        description = "사용자의 소셜 계정 연결 현황을 플랫폼별로 조회합니다. 각 플랫폼별 연결 여부를 포함합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SocialAccountDto.UserSocialAccounts.class))),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/users/{publicId}/status")
    public ResponseEntity<SocialAccountDto.UserSocialAccounts> getUserSocialAccountsStatus(
            @Parameter(description = "조회할 사용자의 공개 ID", required = true)
            @PathVariable UUID publicId) {
        log.info("사용자 소셜 계정 현황 조회 API 호출: publicId={}", publicId);
        
        SocialAccountDto.UserSocialAccounts status = socialAccountService.getUserSocialAccountsStatus(publicId);
        return ResponseEntity.ok(status);
    }
    
    @Operation(
        summary = "사용자에게 소셜 계정 연결",
        description = "특정 사용자에게 새로운 소셜 계정을 연결합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "연결 성공",
                    content = @Content(schema = @Schema(implementation = SocialAccountDto.Response.class))),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "이미 연결된 플랫폼")
    })
    @PostMapping("/users/{publicId}")
    public ResponseEntity<SocialAccountDto.Response> linkSocialAccount(
            @Parameter(description = "연결할 사용자의 공개 ID", required = true)
            @PathVariable UUID publicId,
            @Parameter(description = "연결할 소셜 계정 정보", required = true)
            @RequestBody SocialAccountDto.CreateRequest request) {
        log.info("사용자 소셜 계정 연결 API 호출: publicId={}, provider={}", publicId, request.getProvider());
        
        SocialAccountDto.Response response = socialAccountService.linkSocialAccount(publicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
        summary = "사용자 특정 플랫폼 계정 연결 해제",
        description = "사용자의 특정 소셜 플랫폼 계정 연결을 해제합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "연결 해제 성공"),
        @ApiResponse(responseCode = "404", description = "사용자 또는 소셜 계정을 찾을 수 없음")
    })
    @DeleteMapping("/users/{publicId}/provider/{provider}")
    public ResponseEntity<Void> unlinkSocialAccount(
            @Parameter(description = "연결 해제할 사용자의 공개 ID", required = true)
            @PathVariable UUID publicId,
            @Parameter(description = "연결 해제할 소셜 플랫폼", required = true)
            @PathVariable SocialAccount.Provider provider) {
        log.info("사용자 소셜 계정 연결 해제 API 호출: publicId={}, provider={}", publicId, provider);
        
        socialAccountService.unlinkSocialAccount(publicId, provider);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
        summary = "사용자 특정 플랫폼 연결 여부 확인",
        description = "사용자가 특정 소셜 플랫폼에 계정을 연결했는지 확인합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "확인 완료 (true: 연결됨, false: 연결되지 않음)")
    })
    @GetMapping("/users/{publicId}/provider/{provider}/linked")
    public ResponseEntity<Boolean> isAccountLinked(
            @Parameter(description = "확인할 사용자의 공개 ID", required = true)
            @PathVariable UUID publicId,
            @Parameter(description = "확인할 소셜 플랫폼", required = true)
            @PathVariable SocialAccount.Provider provider) {
        log.info("소셜 계정 연결 여부 확인 API 호출: publicId={}, provider={}", publicId, provider);
        
        boolean linked = socialAccountService.isAccountLinked(publicId, provider);
        return ResponseEntity.ok(linked);
    }
    
    @Operation(
        summary = "사용자 연결된 소셜 계정 수 조회",
        description = "특정 사용자에게 연결된 소셜 계정의 총 개수를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/users/{publicId}/count")
    public ResponseEntity<Long> getUserSocialAccountCount(
            @Parameter(description = "조회할 사용자의 공개 ID", required = true)
            @PathVariable UUID publicId) {
        log.info("사용자 연결된 소셜 계정 수 조회 API 호출: publicId={}", publicId);
        
        long count = socialAccountService.getUserSocialAccountCount(publicId);
        return ResponseEntity.ok(count);
    }
    
    @Operation(
        summary = "소셜 계정 통계 정보 조회",
        description = "전체 소셜 계정 관련 통계 정보를 조회합니다. 플랫폼별 연결 수, 최근 연결된 계정 등의 정보를 제공합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "통계 조회 성공",
                    content = @Content(schema = @Schema(implementation = SocialAccountStatsResponse.class)))
    })
    @GetMapping("/stats")
    public ResponseEntity<SocialAccountStatsResponse> getSocialAccountStats() {
        log.info("소셜 계정 통계 조회 API 호출");
        
        SocialAccountStatsResponse stats = new SocialAccountStatsResponse();
        stats.setKakaoCount(socialAccountService.getProviderAccountCount(SocialAccount.Provider.KAKAO));
        stats.setGoogleCount(socialAccountService.getProviderAccountCount(SocialAccount.Provider.GOOGLE));
        stats.setNaverCount(socialAccountService.getProviderAccountCount(SocialAccount.Provider.NAVER));
        stats.setTotalCount(stats.getKakaoCount() + stats.getGoogleCount() + stats.getNaverCount());
        stats.setRecentAccounts(socialAccountService.getRecentlyLinkedAccounts(10));
        
        return ResponseEntity.ok(stats);
    }
    
    @Schema(description = "소셜 계정 통계 응답 데이터")
    @lombok.Getter
    @lombok.Setter
    public static class SocialAccountStatsResponse {
        @Schema(description = "전체 연결된 소셜 계정 수", example = "1500")
        private long totalCount;
        @Schema(description = "카카오 연결 계정 수", example = "800")
        private long kakaoCount;
        @Schema(description = "구글 연결 계정 수", example = "450")
        private long googleCount;
        @Schema(description = "네이버 연결 계정 수", example = "250")
        private long naverCount;
        @Schema(description = "최근 연결된 계정 목록 (최대 10개)")
        private List<SocialAccountDto.Summary> recentAccounts;
    }
} 