package com.ecommerce.repository;

import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.entity.UserStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  long countByStatus(UserStatus active);

  long countByRole(Role role);

  long countByCreatedAtAfter(LocalDateTime dateTime);

  long countByStatusAndCreatedAtAfter(UserStatus status, LocalDateTime dateTime);

  long countByRoleAndCreatedAtAfter(Role role, LocalDateTime dateTime);
}
