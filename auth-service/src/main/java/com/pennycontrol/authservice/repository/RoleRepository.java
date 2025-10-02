package com.pennycontrol.authservice.repository;

import com.pennycontrol.authservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * Find role by name
     */
    Optional<Role> findByName(String name);
}
