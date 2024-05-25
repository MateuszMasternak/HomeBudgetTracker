package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.user.User;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, Long> {
    Iterable<Category> findAllByUser(User user);
}
