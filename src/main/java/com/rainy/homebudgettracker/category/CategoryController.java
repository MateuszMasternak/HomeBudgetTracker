package com.rainy.homebudgettracker.category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getCurrentUserCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(categoryService.findCurrentUserCategories(pageable));
    }

    @GetMapping("/without-pagination")
    public ResponseEntity<List<CategoryResponse>> getCurrentUserCategoriesWithoutPagination() {
        return ResponseEntity.ok(categoryService.findAllCurrentUserCategories());
    }


    @PostMapping
    public ResponseEntity<CategoryResponse> createCategoryForCurrentUser(
            @RequestBody @Valid CategoryRequest categoryRequest
    ) {
        CategoryResponse newCategory = categoryService.createCategoryForCurrentUser(categoryRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newCategory.getId())
                .toUri();

        return ResponseEntity.created(location).body(newCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurrentUserCategory(@PathVariable UUID id) {
        categoryService.deleteCurrentUserCategory(id);
        return ResponseEntity.noContent().build();
    }
}
