package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.handler.exception.CategoryAssociatedWithTransactionException;
import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    Page<CategoryResponse> findAllByCurrentUser(Pageable pageable);
    List<CategoryResponse> findAllByCurrentUser();
    CategoryResponse findOneAsResponseByCurrentUserAndName(String name) throws RecordDoesNotExistException;
    Category findOneByCurrentUserAndName(String name) throws RecordDoesNotExistException;
    CategoryResponse createCategoryForCurrentUser(CategoryRequest categoryRequest) throws RecordAlreadyExistsException;
    void deleteCurrentUserCategory(Long categoryId) throws
            RecordDoesNotExistException,
            UserIsNotOwnerException,
            CategoryAssociatedWithTransactionException;
}
