package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.handler.exception.CategoryAssociatedWithTransactionException;
import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    Page<CategoryResponse> findCurrentUserCategoriesAsResponses(Pageable pageable);
    List<CategoryResponse> findCurrentUserCategoriesAsResponses();
    List<Category> findCurrentUserCategories();
    CategoryResponse findCurrentUserCategoryAsResponse(String name) throws RecordDoesNotExistException;
    Category findCurrentUserCategory(String name) throws RecordDoesNotExistException;
    CategoryResponse createCategoryForCurrentUser(CategoryRequest categoryRequest) throws RecordAlreadyExistsException;
    void deleteCurrentUserCategory(UUID categoryId) throws
            RecordDoesNotExistException,
            UserIsNotOwnerException,
            CategoryAssociatedWithTransactionException;
}
