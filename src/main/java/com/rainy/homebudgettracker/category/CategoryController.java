package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.handler.exception.CategoryAssociatedWithTransactionException;
import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

   @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAllCategoriesByUser(
           @RequestParam(defaultValue = "0") int page,
           @RequestParam(defaultValue = "10") int size
   ) {
       User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       Pageable pageable = Pageable.ofSize(size).withPage(page);
       return ResponseEntity.ok(categoryService.findAllByUser(user, pageable));
    }

    @GetMapping("/without-pagination")
    public ResponseEntity<List<CategoryResponse>> getAllCategoriesByUserWithoutPagination() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(categoryService.findAllByUser(user));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody @Valid CategoryRequest categoryRequest
    )
            throws RecordAlreadyExistsException
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(categoryService.createCategory(user, categoryRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id)
            throws RecordDoesNotExistException, UserIsNotOwnerException, CategoryAssociatedWithTransactionException
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        categoryService.deleteCategory(user, id);
        return ResponseEntity.noContent().build();
    }
}
