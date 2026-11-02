package com.ecommerce.service;

import com.ecommerce.dto.UserAnalytics;
import com.ecommerce.dto.user.ChangePasswordRequest;
import com.ecommerce.dto.user.UpdateProfileRequest;
import com.ecommerce.dto.user.UpdateRoleRequest;
import com.ecommerce.dto.user.UserProfileResponse;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.entity.UserStatus;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class UserService {

  private final FileUploadService uploadService;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  // --- User Self-Management ---

  public UserProfileResponse getCurrentUserProfile(String email) {
    User user = getUserByEmail(email);
    return mapToProfileResponse(user);
  }

  @Transactional
  public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
    User user = getUserByEmail(email);

    // Update First Name if provided and not empty
    if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
      user.setFirstName(request.getFirstName());
    }

    // Update Last Name if provided and not empty
    if (request.getLastName() != null && !request.getLastName().isBlank()) {
      user.setLastName(request.getLastName());
    }

    // Update Phone if provided and not empty
    if (request.getPhone() != null && !request.getPhone().isBlank()) {
      user.setPhone(request.getPhone());
    }

    // Handle Avatar File Upload if present
    if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
      final String fileName = uploadService.storeFile(request.getAvatar());

      String fileDownloadUri =
          ServletUriComponentsBuilder.fromCurrentContextPath()
              .path("/uploads/")
              .path(fileName)
              .toUriString();

      user.setAvator(fileDownloadUri);
    }

    User updatedUser = userRepository.save(user);
    return mapToProfileResponse(updatedUser);
  }

  @Transactional
  public void changePassword(String email, ChangePasswordRequest request) {
    User user = getUserByEmail(email);

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw new IllegalArgumentException("Current password does not match");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }

  // --- Admin Operations ---

  public Page<UserProfileResponse> getAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable).map(this::mapToProfileResponse);
  }

  public UserProfileResponse getUserById(Long id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    return mapToProfileResponse(user);
  }

  @Transactional
  public UserProfileResponse updateUserRole(Long id, UpdateRoleRequest request) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

    user.setRole(request.getRole());
    return mapToProfileResponse(userRepository.save(user));
  }

  @Transactional
  public UserProfileResponse toggleUserStatus(Long id, boolean enabled) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

    user.setEnabled(enabled);
    return mapToProfileResponse(userRepository.save(user));
  }

  @Transactional
  public void deleteUser(Long id) {
    if (!userRepository.existsById(id)) {
      throw new ResourceNotFoundException("User not found with id: " + id);
    }
    userRepository.deleteById(id);
  }

  /**
   * Retrieves the total count of registered users.
   *
   * @return total user count
   */
  public long getTotalUsersCount() {
    return userRepository.count();
  }

  /**
   * Retrieves summary analytics for users.
   *
   * @return object containing user analytics details
   */
  public UserAnalytics getUserAnalyticsSummary(LocalDate startDate) {
    LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;

    long totalUsers;
    long activeUsers;
    long suspendedUsers;
    long adminUsers;

    if (startDateTime != null) {
      totalUsers = userRepository.countByCreatedAtAfter(startDateTime);
      activeUsers = userRepository.countByStatusAndCreatedAtAfter(UserStatus.ACTIVE, startDateTime);
      suspendedUsers =
          userRepository.countByStatusAndCreatedAtAfter(UserStatus.SUSPENDED, startDateTime);
      adminUsers = userRepository.countByRoleAndCreatedAtAfter(Role.ROLE_ADMIN, startDateTime);
    } else {
      totalUsers = userRepository.count();
      activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
      suspendedUsers = userRepository.countByStatus(UserStatus.SUSPENDED);
      adminUsers = userRepository.countByRole(Role.ROLE_ADMIN);
    }

    return UserAnalytics.builder()
        .totalUsers(totalUsers)
        .activeUsers(activeUsers)
        .suspendedUsers(suspendedUsers)
        .adminUsers(adminUsers)
        .build();
  }

  // --- Helpers ---

  private User getUserByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
  }

  private UserProfileResponse mapToProfileResponse(User user) {
    return UserProfileResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getFirstName() + " " + user.getLastName())
        .role(user.getRole())
        .enabled(user.isEnabled())
        .phone(user.getPhone())
        .avatar(user.getAvator())
        .status(user.getStatus())
        .totalOrders(user.getTotalOrders())
        .totalSpent(user.getTotalSpent())
        .createdAt(user.getCreatedAt())
        .build();
  }

  public User findByEmail(String username) {
    return userRepository
        .findByEmail(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));
  }
}
