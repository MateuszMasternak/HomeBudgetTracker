package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByUserAndName(User user, String name);
    Page<Category> findAllByUser(User user, Pageable pageable);
    Iterable<Category> findAllByUser(User user);
    boolean existsByUserAndName(User user, String name);
}
