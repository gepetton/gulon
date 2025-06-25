package com.gulon.app.service;

import com.gulon.app.dto.UserDto;
import com.gulon.app.entity.User;
import com.gulon.app.mapper.UserMapper;
import com.gulon.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 사용자 서비스 - MapStruct 활용
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Transactional
    public UserDto.Response createUser(UserDto.CreateRequest request) {
        log.info("사용자 생성 요청: email={}", request.getEmail());
        
        // 이메일 중복 검증
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + request.getEmail());
        }
        
        // 입력값 검증
        validateCreateRequest(request);
        
        // MapStruct를 사용한 엔티티 변환
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        
        log.info("사용자 생성 완료: id={}, publicId={}, email={}", 
                savedUser.getId(), savedUser.getPublicId(), savedUser.getEmail());
        return userMapper.toResponseDto(savedUser);
    }
    
    @Transactional
    public UserDto.Response updateUser(Integer id, UserDto.UpdateRequest request) {
        log.info("사용자 수정 요청: id={}", id);
        
        User existingUser = findUserByIdOrThrow(id);
        
        // 이메일 변경 시 중복 검증
        if (request.getEmail() != null && 
            !request.getEmail().equals(existingUser.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + request.getEmail());
        }
        
        // MapStruct를 사용한 엔티티 업데이트
        userMapper.updateUserFromDto(request, existingUser);
        
        User updatedUser = userRepository.save(existingUser);
        
        log.info("사용자 수정 완료: id={}", updatedUser.getId());
        return userMapper.toResponseDto(updatedUser);
    }
    
    @Transactional
    public UserDto.Response updateUserByPublicId(UUID publicId, UserDto.UpdateRequest request) {
        log.info("사용자 수정 요청: publicId={}", publicId);
        
        User existingUser = findUserByPublicIdOrThrow(publicId);
        
        // 이메일 변경 시 중복 검증
        if (request.getEmail() != null && 
            !request.getEmail().equals(existingUser.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + request.getEmail());
        }
        
        // MapStruct를 사용한 엔티티 업데이트
        userMapper.updateUserFromDto(request, existingUser);
        
        User updatedUser = userRepository.save(existingUser);
        
        log.info("사용자 수정 완료: publicId={}", updatedUser.getPublicId());
        return userMapper.toResponseDto(updatedUser);
    }
    
    public Optional<UserDto.Response> getUserById(Integer id) {
        log.debug("사용자 조회: id={}", id);
        return userRepository.findById(id)
                .map(userMapper::toResponseDto);
    }
    
    public Optional<UserDto.Response> getUserByPublicId(UUID publicId) {
        log.debug("사용자 조회: publicId={}", publicId);
        return userRepository.findByPublicId(publicId)
                .map(userMapper::toResponseDto);
    }
    
    public List<UserDto.Summary> getAllUsers() {
        log.debug("모든 사용자 조회");
        List<User> users = userRepository.findAll();
        return userMapper.toSummaryDtoList(users);
    }
    
    public Page<UserDto.Summary> getUsersWithPaging(Pageable pageable) {
        log.debug("페이지네이션 사용자 조회: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable)
                .map(userMapper::toSummaryDto);
    }
    
    public Optional<UserDto.Response> getUserByEmail(String email) {
        log.debug("이메일로 사용자 조회: email={}", email);
        return userRepository.findByEmail(email)
                .map(userMapper::toResponseDto);
    }
    
    public boolean isEmailExists(String email) {
        log.debug("이메일 중복 확인: email={}", email);
        return userRepository.existsByEmail(email);
    }
    
    public List<UserDto.Summary> getUsersByStatus(User.UserStatus status) {
        log.debug("상태별 사용자 조회: status={}", status);
        List<User> users = userRepository.findByStatus(status);
        return userMapper.toSummaryDtoList(users);
    }
    
    public List<UserDto.Summary> searchUsersByName(String name) {
        log.debug("이름으로 사용자 검색: name={}", name);
        List<User> users = userRepository.findByNameContainingIgnoreCase(name);
        return userMapper.toSummaryDtoList(users);
    }
    
    public List<UserDto.Summary> getUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("기간별 사용자 조회: start={}, end={}", startDate, endDate);
        List<User> users = userRepository.findByCreatedAtBetween(startDate, endDate);
        return userMapper.toSummaryDtoList(users);
    }
    
    @Transactional
    public UserDto.Response changeUserStatus(Integer id, User.UserStatus status) {
        log.info("사용자 상태 변경: id={}, status={}", id, status);
        
        User user = findUserByIdOrThrow(id);
        user.setStatus(status);
        User updatedUser = userRepository.save(user);
        
        log.info("사용자 상태 변경 완료: id={}, status={}", updatedUser.getId(), updatedUser.getStatus());
        return userMapper.toResponseDto(updatedUser);
    }
    
    @Transactional
    public UserDto.Response changeUserStatusByPublicId(UUID publicId, User.UserStatus status) {
        log.info("사용자 상태 변경: publicId={}, status={}", publicId, status);
        
        User user = findUserByPublicIdOrThrow(publicId);
        user.setStatus(status);
        User updatedUser = userRepository.save(user);
        
        log.info("사용자 상태 변경 완료: publicId={}, status={}", updatedUser.getPublicId(), updatedUser.getStatus());
        return userMapper.toResponseDto(updatedUser);
    }
    
    public long getActiveUserCount() {
        log.debug("활성 사용자 수 조회");
        return userRepository.countByStatus(User.UserStatus.ACTIVE);
    }
    
    public List<UserDto.Summary> getRecentUsers(int limit) {
        log.debug("최근 가입 사용자 조회: limit={}", limit);
        List<User> users = userRepository.findRecentUsers().stream()
                .limit(limit)
                .toList();
        return userMapper.toSummaryDtoList(users);
    }
    
    @Transactional
    public void deleteById(Integer id) {
        log.info("사용자 삭제 요청: id={}", id);
        
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다: " + id);
        }
        
        userRepository.deleteById(id);
        log.info("사용자 삭제 완료: id={}", id);
    }
    
    @Transactional
    public void deleteByPublicId(UUID publicId) {
        log.info("사용자 삭제 요청: publicId={}", publicId);
        
        User user = findUserByPublicIdOrThrow(publicId);
        userRepository.delete(user);
        log.info("사용자 삭제 완료: publicId={}", publicId);
    }
    
    public long count() {
        return userRepository.count();
    }
    
    /**
     * ID로 사용자 조회, 없으면 예외 발생
     */
    private User findUserByIdOrThrow(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + id));
    }
    
    /**
     * PublicId로 사용자 조회, 없으면 예외 발생
     */
    private User findUserByPublicIdOrThrow(UUID publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + publicId));
    }
    
    /**
     * 사용자 생성 요청 검증
     */
    private void validateCreateRequest(UserDto.CreateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 이름은 필수입니다");
        }
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 이메일은 필수입니다");
        }
        
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다");
        }
    }
} 