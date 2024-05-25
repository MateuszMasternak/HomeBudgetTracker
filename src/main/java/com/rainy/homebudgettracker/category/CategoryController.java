package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

   @GetMapping
    public ResponseEntity<Iterable<Category>> getAllCategoriesByUser(User user) {
        return ResponseEntity.ok(categoryService.findAllByUser(user));
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(User user, CategoryRequest categoryRequest)
            throws RecordAlreadyExistsException
    {
        return ResponseEntity.ok(categoryService.createCategory(user, categoryRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(User user, Long categoryId)
            throws RecordDoesNotExistException, UserIsNotOwnerException
    {
        categoryService.deleteCategory(user, categoryId);
        return ResponseEntity.noContent().build();
    }
}
