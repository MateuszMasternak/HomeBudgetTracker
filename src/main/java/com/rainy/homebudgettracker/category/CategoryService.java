package com.rainy.homebudgettracker.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    Page<CategoryResponse> findCurrentUserCategories(Pageable pageable);
    List<CategoryResponse> findAllCurrentUserCategories();
    CategoryResponse createCategoryForCurrentUser(CategoryRequest categoryRequest);
    void deleteCurrentUserCategory(UUID categoryId);
}
