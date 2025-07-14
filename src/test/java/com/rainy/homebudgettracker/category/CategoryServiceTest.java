package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.handler.exception.CategoryAssociatedWithTransactionException;
import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.repository.TransactionRepository;
import com.rainy.homebudgettracker.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;

    @Nested
    @DisplayName("Tests for finding categories")
    class FindingCategoriesTests {

        @Test
        @DisplayName("should return a paginated list of categories")
        void findCurrentUserCategories_shouldReturnPageOfCategories() {
            String userSub = TestData.USER_SUB;
            Category category = TestData.CATEGORY_FOOD;
            CategoryResponse categoryResponse = TestData.CATEGORY_RESPONSE_FOOD;
            Pageable pageable = PageRequest.of(0, 10);
            Page<Category> categoryPage = new PageImpl<>(List.of(category), pageable, 1);

            when(userService.getUserSub()).thenReturn(userSub);
            when(categoryRepository.findAllByUserSub(userSub, pageable)).thenReturn(categoryPage);
            when(modelMapper.map(any(Category.class), eq(CategoryResponse.class))).thenReturn(categoryResponse);

            Page<CategoryResponse> result = categoryService.findCurrentUserCategories(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0)).isEqualTo(categoryResponse);
        }

        @Test
        @DisplayName("should return a full sorted list of categories")
        void findAllCurrentUserCategories_shouldReturnFullSortedList() {
            String userSub = TestData.USER_SUB;
            Category category = TestData.CATEGORY_FOOD;
            CategoryResponse categoryResponse = TestData.CATEGORY_RESPONSE_FOOD;

            when(userService.getUserSub()).thenReturn(userSub);
            when(categoryRepository.findAllByUserSubOrderByNameAsc(userSub)).thenReturn(List.of(category));
            when(modelMapper.map(category, CategoryResponse.class)).thenReturn(categoryResponse);

            List<CategoryResponse> result = categoryService.findAllCurrentUserCategories();

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(categoryResponse);
            verify(categoryRepository).findAllByUserSubOrderByNameAsc(userSub);
        }
    }

    @Nested
    @DisplayName("Tests for creating a category")
    class CreatingCategoryTests {

        @Test
        @DisplayName("should create and return a new category")
        void createCategoryForCurrentUser_shouldCreateAndReturnNewCategory() {
            String userSub = TestData.USER_SUB;
            CategoryRequest request = TestData.CATEGORY_REQUEST_TRANSPORT;
            Category transientCategory = TestData.CATEGORY_TRANSPORT;
            transientCategory.setId(null);
            Category savedCategory = TestData.CATEGORY_TRANSPORT;
            CategoryResponse expectedResponse = TestData.CATEGORY_RESPONSE_TRANSPORT;

            when(userService.getUserSub()).thenReturn(userSub);
            when(categoryRepository.existsByUserSubAndName(userSub, request.getName())).thenReturn(false);
            when(modelMapper.map(request, Category.class, userSub)).thenReturn(transientCategory);
            when(categoryRepository.save(transientCategory)).thenReturn(savedCategory);
            when(modelMapper.map(savedCategory, CategoryResponse.class)).thenReturn(expectedResponse);

            CategoryResponse result = categoryService.createCategoryForCurrentUser(request);

            assertThat(result).isEqualTo(expectedResponse);
            verify(categoryRepository).save(transientCategory);
        }

        @Test
        @DisplayName("should throw exception when category with the same name already exists")
        void createCategoryForCurrentUser_shouldThrowException_whenNameExists() {
            String userSub = TestData.USER_SUB;
            CategoryRequest request = TestData.CATEGORY_REQUEST_TRANSPORT;

            when(userService.getUserSub()).thenReturn(userSub);
            when(categoryRepository.existsByUserSubAndName(userSub, request.getName())).thenReturn(true);

            assertThatThrownBy(() -> categoryService.createCategoryForCurrentUser(request))
                    .isInstanceOf(RecordAlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("Tests for deleting a category")
    class DeletingCategoryTests {

        @Test
        @DisplayName("should delete category successfully")
        void deleteCurrentUserCategory_shouldDeleteSuccessfully() {
            String userSub = TestData.USER_SUB;
            Category category = TestData.CATEGORY_FOOD;

            when(userService.getUserSub()).thenReturn(userSub);
            when(categoryRepository.findByIdAndUserSub(category.getId(), userSub)).thenReturn(Optional.of(category));
            when(transactionRepository.existsByCategory(category)).thenReturn(false);

            categoryService.deleteCurrentUserCategory(category.getId());

            verify(categoryRepository, times(1)).delete(category);
        }

        @Test
        @DisplayName("should throw exception when category to delete is not found or doesn't belong to user")
        void deleteCurrentUserCategory_shouldThrowException_whenCategoryNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            String userSub = TestData.USER_SUB;
            when(userService.getUserSub()).thenReturn(userSub);
            when(categoryRepository.findByIdAndUserSub(nonExistentId, userSub)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.deleteCurrentUserCategory(nonExistentId))
                    .isInstanceOf(RecordDoesNotExistException.class);
        }

        @Test
        @DisplayName("should throw exception when category is associated with a transaction")
        void deleteCurrentUserCategory_shouldThrowException_whenAssociatedWithTransaction() {
            String userSub = TestData.USER_SUB;
            Category category = TestData.CATEGORY_FOOD;

            when(userService.getUserSub()).thenReturn(userSub);
            when(categoryRepository.findByIdAndUserSub(category.getId(), userSub)).thenReturn(Optional.of(category));
            when(transactionRepository.existsByCategory(category)).thenReturn(true);

            assertThatThrownBy(() -> categoryService.deleteCurrentUserCategory(category.getId()))
                    .isInstanceOf(CategoryAssociatedWithTransactionException.class);
        }
    }
}