package com.rainy.homebudgettracker.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    @Modifying()
    @Query("UPDATE User u SET u.password = :password WHERE u = :user")
    void updatePassword(User user, String password);
}
