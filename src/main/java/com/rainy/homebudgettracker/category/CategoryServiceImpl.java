package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.user.UserService;
import com.rainy.homebudgettracker.handler.exception.CategoryAssociatedWithTransactionException;
import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.TransactionRepository;
import com.rainy.homebudgettracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements  CategoryService {
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    public Page<CategoryResponse> findAllByCurrentUser(Pageable pageable) {
        User user = userService.getCurrentUser();
        Page<Category> categories = categoryRepository.findAllByUser(user, pageable);
        return categories.map(category -> modelMapper.map(category, CategoryResponse.class));
    }

    @Override
    public List<CategoryResponse> findAllByCurrentUser() {
        User user = userService.getCurrentUser();
        Iterable<Category> categories = categoryRepository.findAllByUser(user);
        return mapIterableCategoryToResponseCategoryList(categories);
    }

    @Override
    public CategoryResponse findOneAsResponseByCurrentUserAndName(String name) throws RecordDoesNotExistException {
        User user = userService.getCurrentUser();
        Category category = categoryRepository.findByUserAndName(user, name).orElseThrow(
                () -> new RecordDoesNotExistException("Category with name " + name + " does not exist.")
        );
        return modelMapper.map(category, CategoryResponse.class);
    }

    @Override
    public Category findOneByCurrentUserAndName(String name) throws RecordDoesNotExistException {
        User user = userService.getCurrentUser();
        return categoryRepository.findByUserAndName(user, name).orElseThrow(
                () -> new RecordDoesNotExistException("Category with name " + name + " does not exist.")
        );
    }

    @Override
    public CategoryResponse createCategoryForCurrentUser(CategoryRequest categoryRequest)
            throws RecordAlreadyExistsException {
        Category category = modelMapper.map(categoryRequest, Category.class);
        if (categoryRepository.existsByUserAndName(category.getUser(), category.getName())) {
            throw new RecordAlreadyExistsException(
                    "Category with name " + categoryRequest.getName() + " already exists.");
        } else {
            Category savedCategory = categoryRepository.save(category);
            return modelMapper.map(savedCategory, CategoryResponse.class);
        }
    }

    @Override
    public void deleteCurrentUserCategory(Long categoryId) throws
            RecordDoesNotExistException,
            UserIsNotOwnerException,
            CategoryAssociatedWithTransactionException
    {
        User user = userService.getCurrentUser();
        Optional<Category> category = categoryRepository.findById(categoryId);
        if (category.isEmpty()) {
            throw new RecordDoesNotExistException("Category with id " + categoryId + " does not exist.");
        } else if (!category.get().getUser().getEmail().equals(user.getEmail())) {
            throw new UserIsNotOwnerException("Category with id " + categoryId + " does not belong to user.");
        } else if (transactionRepository.existsByCategory(category.get())) {
            throw new CategoryAssociatedWithTransactionException("Category with id " + categoryId + " is associated with transactions.");
        } else {
            categoryRepository.deleteById(categoryId);
        }
    }

    private List<CategoryResponse> mapIterableCategoryToResponseCategoryList(Iterable<Category> categories) {
        List<CategoryResponse> responseCategoryList = new ArrayList<>();
        categories.forEach(c -> responseCategoryList.add(modelMapper.map(c, CategoryResponse.class)));
        responseCategoryList.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
        return responseCategoryList;
    }
}
