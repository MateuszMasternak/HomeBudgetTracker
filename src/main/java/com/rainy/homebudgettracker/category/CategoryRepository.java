package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.user.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CategoryRepository extends CrudRepository<Category, Long> {
    Optional<Category> findByUserAndName(User user, String name);
    Iterable<Category> findAllByUser(User user);
}
