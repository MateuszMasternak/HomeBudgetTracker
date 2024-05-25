package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Iterable<Category> findAllByUser(User user) {
        return categoryRepository.findAllByUser(user);
    }

    public Category createCategory(User user, CategoryRequest categoryRequest) throws
            RecordAlreadyExistsException
    {
        try {
            Category category = Category.builder()
                    .name(categoryRequest.getName())
                    .user(user)
                    .build();

            return categoryRepository.save(category);
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
