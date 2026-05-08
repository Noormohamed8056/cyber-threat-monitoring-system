package com.project.service;

import com.project.entity.ThreatHistory;
import com.project.entity.User;
import com.project.repository.ThreatHistoryRepository;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ThreatHistoryRepository threatHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    // ==========================================
    // REGISTER NEW USER
    // ==========================================
    @Transactional
    public User registerUser(String fullName,
                             String email,
                             String password,
                             String institutionName,
                             User.Role role) {

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered: " + email);
        }

        // Create new user
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setInstitutionName(institutionName);
        user.setRole(role);
        user.setActive(true);

        // Save user
        User savedUser = userRepository.save(user);

        // Log registration to threat history
        ThreatHistory history = ThreatHistory.forUser(
                ThreatHistory.ActionType.USER_REGISTERED,
                savedUser,
                "New user registered: " + savedUser.getEmail()
                        + " with role: " + savedUser.getRole()
        );
        threatHistoryRepository.save(history);

        return savedUser;
    }

    // ==========================================
    // FIND USER BY EMAIL
    // ==========================================
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ==========================================
    // FIND USER BY ID
    // ==========================================
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + id));
    }

    // ==========================================
    // GET ALL USERS (ADMIN)
    // ==========================================
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ==========================================
    // GET ALL ACTIVE USERS
    // ==========================================
    public List<User> getAllActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    // ==========================================
    // GET ALL STUDENTS
    // ==========================================
    public List<User> getAllStudents() {
        return userRepository.findByRole(User.Role.STUDENT);
    }

    // ==========================================
    // GET ALL ACTIVE STUDENTS
    // ==========================================
    public List<User> getAllActiveStudents() {
        return userRepository.findByRoleAndIsActiveTrue(User.Role.STUDENT);
    }

    // ==========================================
    // GET ALL ADMINS
    // ==========================================
    public List<User> getAllAdmins() {
        return userRepository.findByRole(User.Role.ADMIN);
    }

    // ==========================================
    // UPDATE USER PROFILE
    // ==========================================
    @Transactional
    public User updateProfile(Long userId,
                              String fullName,
                              String institutionName) {

        User user = findById(userId);
        user.setFullName(fullName);
        user.setInstitutionName(institutionName);
        return userRepository.save(user);
    }

    // ==========================================
    // CHANGE PASSWORD
    // ==========================================
    @Transactional
    public void changePassword(Long userId,
                               String currentPassword,
                               String newPassword) {

        User user = findById(userId);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ==========================================
    // DEACTIVATE USER (ADMIN)
    // ==========================================
    @Transactional
    public User deactivateUser(Long userId, User adminUser) {

        User user = findById(userId);

        // Prevent admin from deactivating themselves
        if (user.getId().equals(adminUser.getId())) {
            throw new RuntimeException(
                    "Admin cannot deactivate their own account");
        }

        user.setActive(false);
        User deactivatedUser = userRepository.save(user);

        // Log deactivation to threat history
        ThreatHistory history = ThreatHistory.forUser(
                ThreatHistory.ActionType.USER_DEACTIVATED,
                adminUser,
                "User deactivated: " + deactivatedUser.getEmail()
                        + " by admin: " + adminUser.getEmail()
        );
        threatHistoryRepository.save(history);

        return deactivatedUser;
    }

    // ==========================================
    // REACTIVATE USER (ADMIN)
    // ==========================================
    @Transactional
    public User reactivateUser(Long userId) {
        User user = findById(userId);
        user.setActive(true);
        return userRepository.save(user);
    }

    // ==========================================
    // SELF DEACTIVATE ACCOUNT (USER)
    // ==========================================
    @Transactional
    public User deactivateOwnAccount(Long userId) {
        User user = findById(userId);
        user.setActive(false);
        User deactivatedUser = userRepository.save(user);

        ThreatHistory history = ThreatHistory.forUser(
                ThreatHistory.ActionType.USER_DEACTIVATED,
                deactivatedUser,
                "User deactivated own account: "
                        + deactivatedUser.getEmail()
        );
        threatHistoryRepository.save(history);

        return deactivatedUser;
    }

    // ==========================================
    // SEARCH USERS BY NAME OR EMAIL
    // ==========================================
    public List<User> searchUsers(String keyword) {
        return userRepository.searchByNameOrEmail(keyword);
    }

    // ==========================================
    // GET USERS BY INSTITUTION
    // ==========================================
    public List<User> getUsersByInstitution(String institutionName) {
        return userRepository.findByInstitutionName(institutionName);
    }

    // ==========================================
    // VALIDATE USER LOGIN
    // ==========================================
    public User validateLogin(String email, String password) {

        // Find active user by email
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new RuntimeException(
                        "Invalid email or account is inactive"));

        // Check password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    // ==========================================
    // LOG USER LOGIN
    // ==========================================
    @Transactional
    public void logUserLogin(User user) {
        ThreatHistory history = ThreatHistory.forUser(
                ThreatHistory.ActionType.USER_LOGIN,
                user,
                "User logged in: " + user.getEmail()
        );
        threatHistoryRepository.save(history);
    }

    // ==========================================
    // GET DASHBOARD STATS
    // ==========================================
    public UserStats getDashboardStats() {
        Long totalStudents = userRepository
                .countByRole(User.Role.STUDENT);
        Long totalAdmins = userRepository
                .countByRole(User.Role.ADMIN);
        Long totalUsers = userRepository.count();

        return new UserStats(totalUsers, totalStudents, totalAdmins);
    }

    // ==========================================
    // USER STATS INNER CLASS
    // ==========================================
    public static class UserStats {
        public Long totalUsers;
        public Long totalStudents;
        public Long totalAdmins;

        public UserStats(Long totalUsers,
                         Long totalStudents,
                         Long totalAdmins) {
            this.totalUsers = totalUsers;
            this.totalStudents = totalStudents;
            this.totalAdmins = totalAdmins;
        }
    }
}
