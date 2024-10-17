package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.user.UserService;
import com.rainy.homebudgettracker.handler.exception.CategoryAssociatedWithTransactionException;
import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.TransactionRepository;
import com.rainy.homebudgettracker.user.Role;
import com.rainy.homebudgettracker.user.User;
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

        var user = User.builder()
                .id(1L)
                .email("mail@mail.com")
                .password("password")
                .role(Role.USER)
                .build();
        when(userService.getCurrentUser()).thenReturn(user);

        var category = Category.builder()
                .id(1L)
                .name("Food")
                .user(user)
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
                .user(user)
                .build());

        when(transactionRepository.existsByCategory(category)).thenReturn(true);
        when(transactionRepository.existsByCategory(any())).thenReturn(false);
        when(categoryRepository.save(any())).thenReturn(category);
        when(categoryRepository.findByUserAndName(user, "Food")).thenReturn(Optional.of(category));
        when(categoryRepository.findByUserAndName(user, "Healthcare")).thenReturn(Optional.empty());
        when(categoryRepository.findAllByUser(user, pageable)).thenReturn(categoryPage);
        when(categoryRepository.findAllByUser(user)).thenReturn(List.of(category));
        when(categoryRepository.save(Category.builder().name("Food").user(user).build())).thenReturn(category);
        when(categoryRepository.existsByUserAndName(user, "Food")).thenReturn(false);
        when(categoryRepository.existsByUserAndName(user, "Healthcare")).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findById(4L)).thenReturn(Optional.empty());
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(Category.builder()
                .id(3L)
                .name("Healthcare")
                .user(User.builder()
                        .id(2L)
                        .email("2lmail@mail.com")
                        .password("password")
                        .role(Role.USER)
                        .build()).build()));
        Category category2 = Category.builder()
                .id(2L)
                .name("Healthcare")
                .user(user)
                .build();
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category2));
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
                .id(1L)
                .name("Food")
                .build();

        assertEquals(1, categoryPage.getTotalElements());
        assertEquals(categoryResponse, categoryPage.getContent().get(0));

        verify(categoryRepository, times(1)).findAllByUser(any(User.class), any(Pageable.class));
    }

    @Test
    void shouldReturnListWithCategoryResponse() {
        var categoryList = categoryService.findCurrentUserCategoriesAsResponses();

        var categoryResponse = CategoryResponse.builder()
                .id(1L)
                .name("Food")
                .build();

        assertEquals(1, categoryList.size());
        assertEquals(categoryResponse, categoryList.get(0));

        verify(categoryRepository, times(1)).findAllByUser(any(User.class));
    }

    @Test
    void shouldReturnCategoryResponse() throws RecordDoesNotExistException {
        var returnedCategoryResponse = categoryService.findCurrentUserCategoryAsResponse("Food");

        var categoryResponse = CategoryResponse.builder()
                .id(1L)
                .name("Food")
                .build();

        assertEquals(categoryResponse, returnedCategoryResponse);

        verify(categoryRepository, times(1)).findByUserAndName(any(User.class), eq("Food"));
    }

    @Test
    void shouldThrowExceptionWhenCategoryResponseDoesNotExist() {
        assertThrows(RecordDoesNotExistException.class,
                () -> categoryService.findCurrentUserCategoryAsResponse("Healthcare"));

        verify(categoryRepository, times(1)).findByUserAndName(any(User.class), eq("Healthcare"));
    }

    @Test
    void shouldReturnCategory() throws RecordDoesNotExistException {
        var returnedCategory = categoryService.findCurrentUserCategory("Food");

        var category = Category.builder()
                .id(1L)
                .name("Food")
                .user(User.builder()
                        .id(1L)
                        .email("mail@mail.com")
                        .password("password")
                        .role(Role.USER)
                        .build()
                ).build();

        assertEquals(category, returnedCategory);

        verify(userService, times(1)).getCurrentUser();
        verify(categoryRepository, times(1)).findByUserAndName(any(User.class), eq("Food"));
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        assertThrows(RecordDoesNotExistException.class,
                () -> categoryService.findCurrentUserCategory("Healthcare"));

        verify(userService, times(1)).getCurrentUser();
        verify(categoryRepository, times(1)).findByUserAndName(any(User.class), eq("Healthcare"));
    }

    @Test
    void shouldReturnCategoryResponseWhenCategoryIsCreated() throws RecordAlreadyExistsException {
        var categoryRequest = CategoryRequest.builder()
                .name("Food")
                .build();

        var returnedCategoryResponse = categoryService.createCategoryForCurrentUser(categoryRequest);

        var categoryResponse = CategoryResponse.builder()
                .id(1L)
                .name("Food")
                .build();

        assertEquals(categoryResponse, returnedCategoryResponse);

        verify(categoryRepository, times(1)).existsByUserAndName(any(User.class), eq("Food"));
    }

    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExists() {
        var categoryRequest = CategoryRequest.builder()
                .name("Healthcare")
                .build();

        assertThrows(RecordAlreadyExistsException.class,
                () -> categoryService.createCategoryForCurrentUser(categoryRequest));

        verify(categoryRepository, times(1)).existsByUserAndName(any(User.class), eq("Healthcare"));
    }

    @Test
    void shouldDeleteCategory() {
        assertDoesNotThrow(() -> categoryService.deleteCurrentUserCategory(1L));
    }

    @Test
    void shouldThrowExceptionWhenCategoryIsAssociatedWithTransactionWhenDeleting() {
        assertThrows(CategoryAssociatedWithTransactionException.class,
                () -> categoryService.deleteCurrentUserCategory(2L));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotOwnerWhenDeleting() {
        assertThrows(UserIsNotOwnerException.class,
                () -> categoryService.deleteCurrentUserCategory(3L));
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExistWhenDeleting() {
        assertThrows(RecordDoesNotExistException.class,
                () -> categoryService.deleteCurrentUserCategory(4L));
    }
}