package com.rainy.homebudgettracker.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Page<Category> findAllByUserSub(String userSub, Pageable pageable);
    List<Category> findAllByUserSubOrderByNameAsc(String userSub);
    Optional<Category> findByUserSubAndName(String userSub, String name);
    Optional<Category> findByIdAndUserSub(UUID id, String userSub);
    boolean existsByUserSubAndName(String userSub, String name);
    @Modifying
    void deleteAllByUserSub(String userSub);
}
