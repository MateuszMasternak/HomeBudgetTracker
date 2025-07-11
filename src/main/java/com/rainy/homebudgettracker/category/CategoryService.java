package com.rainy.homebudgettracker.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    Page<CategoryResponse> findCurrentUserCategoriesAsResponses(Pageable pageable);
    List<CategoryResponse> findCurrentUserCategoriesAsResponses();
    List<Category> findCurrentUserCategories();
    CategoryResponse findCurrentUserCategoryAsResponse(String name);
    Category findCurrentUserCategory(String name);
    CategoryResponse createCategoryForCurrentUser(CategoryRequest categoryRequest);
    void deleteCurrentUserCategory(UUID categoryId);
}
