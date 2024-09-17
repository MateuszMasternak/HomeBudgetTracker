package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.handler.exception.CategoryAssociatedWithTransactionException;
import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAllCategoriesByCurrentUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(categoryService.findAllByCurrentUser(pageable));
    }

    @GetMapping("/without-pagination")
    public ResponseEntity<List<CategoryResponse>> getAllCategoriesByCurrentUserWithoutPagination() {
        return ResponseEntity.ok(categoryService.findAllByCurrentUser());
    }


    @PostMapping
    public ResponseEntity<CategoryResponse> createCategoryForCurrentUser(
            @RequestBody @Valid CategoryRequest categoryRequest) throws RecordAlreadyExistsException
    {
        return ResponseEntity.ok(categoryService.createCategoryForCurrentUser(categoryRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurrentUserCategory(@PathVariable Long id)
            throws RecordDoesNotExistException, UserIsNotOwnerException, CategoryAssociatedWithTransactionException {
        categoryService.deleteCurrentUserCategory(id);
        return ResponseEntity.noContent().build();
    }
}
