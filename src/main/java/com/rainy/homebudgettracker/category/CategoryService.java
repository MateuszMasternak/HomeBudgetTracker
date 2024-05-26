package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Iterable<CategoryResponse> findAllByUser(User user) {
        Iterable<Category> categoryIterable = categoryRepository.findAllByUser(user);
        List<CategoryResponse> categoryResponseIterable = new ArrayList<>();
        categoryIterable.forEach(category -> categoryResponseIterable.add(CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build()));

        return categoryResponseIterable;
    }

    public CategoryResponse findByUserAndName(User user, String name) throws RecordDoesNotExistException {
        try {
            Category categories = categoryRepository.findByUserAndName(user, name).orElseThrow();
            return CategoryResponse.builder()
                    .id(categories.getId())
                    .name(categories.getName())
                    .build();
        } catch (NoSuchElementException e) {
            throw new RecordDoesNotExistException("Category with name " + name + " does not exist.");
        }
    }

    public CategoryResponse createCategory(User user, CategoryRequest categoryRequest) throws
            RecordAlreadyExistsException
    {
        try {
            Category category = Category.builder()
                    .name(categoryRequest.getName().toUpperCase())
                    .user(user)
                    .build();

            Category savedCategory = categoryRepository.save(category);
            return CategoryResponse.builder()
                    .id(savedCategory.getId())
                    .name(savedCategory.getName())
                    .build();
        } catch (Exception e) {
            throw new RecordAlreadyExistsException(
                    "Category with name " + categoryRequest.getName() + " already exists.");
        }
    }

    public void deleteCategory(User user, Long categoryId) throws
            RecordDoesNotExistException,
            UserIsNotOwnerException
    {
        if (!categoryRepository.existsById(categoryId)) {
            throw new RecordDoesNotExistException("Category with id " + categoryId + " does not exist.");
        } else if (!categoryRepository.findById(categoryId).get().getUser().equals(user)) {
            throw new UserIsNotOwnerException("Category with id " + categoryId + " does not belong to user.");
        } else {
            categoryRepository.deleteById(categoryId);
        }
    }
}
