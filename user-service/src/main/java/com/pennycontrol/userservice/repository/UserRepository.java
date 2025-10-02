package com.pennycontrol.userservice.repository;

import com.pennycontrol.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by ID
     */
    Optional<User> findById(Long id);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
}
