package com.enoca.requestmanagement.repository;

import com.enoca.requestmanagement.entity.Department;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.department WHERE u.email = :email")
    Optional<User> findByEmailWithDepartment(@Param("email") String email);

    boolean existsByEmail(String email);

    List<User> findByRoleAndDepartmentAndActiveTrue(Role role, Department department);

    List<User> findByRoleAndActiveTrue(Role role);

    long countByRoleAndActiveTrue(Role role);
}
