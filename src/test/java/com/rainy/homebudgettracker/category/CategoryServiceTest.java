package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.user.UserService;
import com.rainy.homebudgettracker.handler.exception.CategoryAssociatedWithTransactionException;
import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceTest {
    @InjectMocks
    CategoryServiceImpl categoryService;
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    TransactionRepository transactionRepository;
    @Mock
    UserService userService;
    @Mock
    ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        var userSub = "550e8400-e29b-41d4-a716-446655440000";
        when(userService.getUserSub()).thenReturn(userSub);

        var category = Category.builder()
                .id(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))
                .name("Food")
                .userSub(userSub)
                .build();

        var categoryRequest = CategoryRequest.builder()
                .name("Food")
                .build();

        var categoryRequest2 = CategoryRequest.builder()
                .name("Healthcare")
                .build();

        var pageable = PageRequest.of(0, 10);
        var categoryPage = new PageImpl<>(List.of(category));

        when(modelMapper.map(category, CategoryResponse.class)).thenReturn(CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build());
        when(modelMapper.map(categoryRequest, Category.class)).thenReturn(category);
        when(modelMapper.map(categoryRequest2, Category.class)).thenReturn(Category.builder()
                .name("Healthcare")
                .userSub(userSub)
                .build());

        when(transactionRepository.existsByCategory(category)).thenReturn(true);
        when(transactionRepository.existsByCategory(any())).thenReturn(false);
        when(categoryRepository.save(any())).thenReturn(category);
        when(categoryRepository.findByUserSubAndName(userSub, "Food")).thenReturn(Optional.of(category));
        when(categoryRepository.findByUserSubAndName(userSub, "Healthcare")).thenReturn(Optional.empty());
        when(categoryRepository.findAllByUserSub(userSub, pageable)).thenReturn(categoryPage);
        when(categoryRepository.findAllByUserSub(userSub)).thenReturn(List.of(category));
        when(categoryRepository.save(Category.builder().name("Food").userSub(userSub).build())).thenReturn(category);
        when(categoryRepository.existsByUserSubAndName(userSub, "Food")).thenReturn(false);
        when(categoryRepository.existsByUserSubAndName(userSub, "Healthcare")).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"));
        when(categoryRepository.findById(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))).thenReturn(Optional.of(category));
        when(categoryRepository.findById(UUID.fromString("cb5f0153-5b1e-4f4b-9886-ae6791284043"))).thenReturn(Optional.empty());
        when(categoryRepository.findById(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))).thenReturn(Optional.of(Category.builder()
                .id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))
                .name("Healthcare")
                .userSub("550e8400-e29b-41d4-a716-446655440001")
                .build()));
        Category category2 = Category.builder()
                .id(UUID.fromString("c7e2fa7e-2267-4da5-ade1-5dc79948a773"))
                .name("Healthcare")
                .userSub(userSub)
                .build();
        when(categoryRepository.findById(UUID.fromString("c7e2fa7e-2267-4da5-ade1-5dc79948a773"))).thenReturn(Optional.of(category2));
        when(transactionRepository.existsByCategory(category2)).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldReturnPageWithCategoryResponse() {

        var categoryPage = categoryService.findCurrentUserCategoriesAsResponses(
                PageRequest.of(0, 10));

        var categoryResponse = CategoryResponse.builder()
                .id(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))
                .name("Food")
                .build();

        assertEquals(1, categoryPage.getTotalElements());
        assertEquals(categoryResponse, categoryPage.getContent().get(0));
    }

    @Test
    void shouldReturnListWithCategoryResponse() {
        var categoryList = categoryService.findCurrentUserCategoriesAsResponses();

        var categoryResponse = CategoryResponse.builder()
                .id(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))
                .name("Food")
                .build();

        assertEquals(1, categoryList.size());
        assertEquals(categoryResponse, categoryList.get(0));
    }

    @Test
    void shouldReturnCategoryResponse() throws RecordDoesNotExistException {
        var returnedCategoryResponse = categoryService.findCurrentUserCategoryAsResponse("Food");

        var categoryResponse = CategoryResponse.builder()
                .id(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))
                .name("Food")
                .build();

        assertEquals(categoryResponse, returnedCategoryResponse);
    }

    @Test
    void shouldThrowExceptionWhenCategoryResponseDoesNotExist() {
        assertThrows(RecordDoesNotExistException.class,
                () -> categoryService.findCurrentUserCategoryAsResponse("Healthcare"));
    }

    @Test
    void shouldReturnCategory() throws RecordDoesNotExistException {
        var returnedCategory = categoryService.findCurrentUserCategory("Food");

        var category = Category.builder()
                .id(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))
                .name("Food")
                .userSub("550e8400-e29b-41d4-a716-446655440000")
                .build();

        assertEquals(category, returnedCategory);
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        assertThrows(RecordDoesNotExistException.class,
                () -> categoryService.findCurrentUserCategory("Healthcare"));
    }

    @Test
    void shouldReturnCategoryResponseWhenCategoryIsCreated() throws RecordAlreadyExistsException {
        var categoryRequest = CategoryRequest.builder()
                .name("Food")
                .build();

        var returnedCategoryResponse = categoryService.createCategoryForCurrentUser(categoryRequest);

        var categoryResponse = CategoryResponse.builder()
                .id(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))
                .name("Food")
                .build();

        assertEquals(categoryResponse, returnedCategoryResponse);
    }

    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExists() {
        var categoryRequest = CategoryRequest.builder()
                .name("Healthcare")
                .build();

        assertThrows(RecordAlreadyExistsException.class,
                () -> categoryService.createCategoryForCurrentUser(categoryRequest));
    }

    @Test
    void shouldDeleteCategory() {
        assertDoesNotThrow(() -> categoryService.deleteCurrentUserCategory(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100")));
    }

    @Test
    void shouldThrowExceptionWhenCategoryIsAssociatedWithTransactionWhenDeleting() {
        assertThrows(CategoryAssociatedWithTransactionException.class,
                () -> categoryService.deleteCurrentUserCategory(UUID.fromString("c7e2fa7e-2267-4da5-ade1-5dc79948a773")));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotOwnerWhenDeleting() {
        assertThrows(UserIsNotOwnerException.class,
                () -> categoryService.deleteCurrentUserCategory(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903")));
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExistWhenDeleting() {
        assertThrows(RecordDoesNotExistException.class,
                () -> categoryService.deleteCurrentUserCategory(UUID.fromString("cb5f0153-5b1e-4f4b-9886-ae6791284043")));
    }
}