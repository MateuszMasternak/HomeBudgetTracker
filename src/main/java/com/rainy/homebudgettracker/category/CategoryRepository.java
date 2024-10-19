package com.rainy.homebudgettracker.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByUserSubAndName(String userSub, String name);

    Page<Category> findAllByUserSub(String userSub, Pageable pageable);

    Iterable<Category> findAllByUserSub(String userSub);

    boolean existsByUserSubAndName(String userSub, String name);

    void deleteAllByUserSub(String sub);

    Optional<Category> findById(UUID uuid);

    void deleteById(UUID categoryId);
}
