package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.handler.exception.CategoryAssociatedWithTransactionException;
import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
@Tag(name = "Category")
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(
            summary = "Get all categories by user",
            description = "Get all categories by user with pagination"
    )
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAllCategoriesByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(categoryService.findAllByUser(user, pageable));
    }

    @Operation(
            summary = "Get all categories by user without pagination",
            description = "Get all categories by user without pagination"
    )
    @GetMapping("/without-pagination")
    public ResponseEntity<List<CategoryResponse>> getAllCategoriesByUserWithoutPagination() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(categoryService.findAllByUser(user));
    }

    @Operation(
            summary = "Create a new category",
            description = "Create a new category"
    )
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody @Valid CategoryRequest categoryRequest
    )
            throws RecordAlreadyExistsException
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(categoryService.createCategory(user, categoryRequest));
    }

    @Operation(
            summary = "Delete a category",
            description = "Delete a category"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id)
            throws RecordDoesNotExistException, UserIsNotOwnerException, CategoryAssociatedWithTransactionException
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        categoryService.deleteCategory(user, id);
        return ResponseEntity.noContent().build();
    }
}
