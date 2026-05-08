package com.project.repository;

import com.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ==========================================
    // FIND BY EMAIL
    // ==========================================
    Optional<User> findByEmail(String email);

    // ==========================================
    // CHECK IF EMAIL EXISTS
    // ==========================================
    boolean existsByEmail(String email);

    // ==========================================
    // FIND BY ROLE
    // ==========================================
    List<User> findByRole(User.Role role);

    // ==========================================
    // FIND BY INSTITUTION
    // ==========================================
    List<User> findByInstitutionName(String institutionName);

    // ==========================================
    // FIND ALL ACTIVE USERS
    // ==========================================
    List<User> findByIsActiveTrue();

    // ==========================================
    // FIND ALL ACTIVE STUDENTS
    // ==========================================
    List<User> findByRoleAndIsActiveTrue(User.Role role);

    // ==========================================
    // FIND BY EMAIL AND ACTIVE STATUS
    // ==========================================
    Optional<User> findByEmailAndIsActiveTrue(String email);

    // ==========================================
    // SEARCH USERS BY NAME OR EMAIL
    // ==========================================
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByNameOrEmail(@Param("keyword") String keyword);

    // ==========================================
    // COUNT USERS BY ROLE
    // ==========================================
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    Long countByRole(@Param("role") User.Role role);

    // ==========================================
    // FIND USERS BY INSTITUTION AND ROLE
    // ==========================================
    @Query("SELECT u FROM User u WHERE " +
            "u.institutionName = :institutionName AND u.role = :role")
    List<User> findByInstitutionAndRole(
            @Param("institutionName") String institutionName,
            @Param("role") User.Role role);
}