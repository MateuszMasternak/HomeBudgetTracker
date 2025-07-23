package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.user.UserService;
import com.rainy.homebudgettracker.handler.exception.CategoryAssociatedWithTransactionException;
import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    public Page<CategoryResponse> findCurrentUserCategories(Pageable pageable) {
        String userSub = userService.getUserSub();
        return categoryRepository.findAllByUserSub(userSub, pageable)
                .map(category -> modelMapper.map(category, CategoryResponse.class));
    }

    @Override
    public List<CategoryResponse> findAllCurrentUserCategories() {
        String userSub = userService.getUserSub();
        List<Category> categories = categoryRepository.findAllByUserSubOrderByNameAsc(userSub);
        return categories.stream()
                .map(category -> modelMapper.map(category, CategoryResponse.class))
                .toList();
    }

    @Override
    public CategoryResponse createCategoryForCurrentUser(CategoryRequest categoryRequest) {
        String userSub = userService.getUserSub();

        if (categoryRepository.existsByUserSubAndName(userSub, categoryRequest.getName())) {
            throw new RecordAlreadyExistsException("Category with name " + categoryRequest.getName()
                    + " already exists.");
        }

        Category category = modelMapper.map(categoryRequest, Category.class, userSub);
        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryResponse.class);
    }

    @Transactional
    @Override
    public void deleteCurrentUserCategory(UUID categoryId) {
        String userSub = userService.getUserSub();

        Category category = categoryRepository.findByIdAndUserSub(categoryId, userSub)
                .orElseThrow(() -> new RecordDoesNotExistException("Category with id " + categoryId
                        + " not found or does not belong to user."));

        if (transactionRepository.existsByCategory(category)) {
            throw new CategoryAssociatedWithTransactionException("Category with id " + categoryId
                    + " is associated with transactions.");
        }

        categoryRepository.delete(category);
    }
}