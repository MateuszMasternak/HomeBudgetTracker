package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.auth.UserDetailsServiceImpl;
import com.rainy.homebudgettracker.handler.exception.CategoryAssociatedWithTransactionException;
import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.TransactionRepository;
import com.rainy.homebudgettracker.user.Role;
import com.rainy.homebudgettracker.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceTest {
    private CategoryService categoryService;
    private User user;
    private Category category;
    private CategoryRequest categoryRequest;
    private CategoryRequest categoryRequest2;

    @BeforeEach
    void setUp() {
        var categoryRepository = mock(CategoryRepository.class);
        var transactionRepository = mock(TransactionRepository.class);
        var userDetailsService = mock(UserDetailsServiceImpl.class);
        var modelMapper = mock(ModelMapper.class);

        user = User.builder()
                .id(1L)
                .email("mail@mail.com")
                .password("password")
                .role(Role.USER)
                .build();
        when(userDetailsService.getCurrentUser()).thenReturn(user);

        category = Category.builder()
                .id(1L)
                .name("Food")
                .user(user)
                .build();

        categoryRequest = CategoryRequest.builder()
                .name("Food")
                .build();

        categoryRequest2 = CategoryRequest.builder()
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

        categoryService = new CategoryServiceImpl(
                categoryRepository, transactionRepository, userDetailsService, modelMapper);
    }

    @Test
    void shouldReturnPageWithCategoryResponse() {
        var categoryPage = categoryService.findAllByCurrentUser(
                PageRequest.of(0, 10));
        assertEquals(1, categoryPage.getTotalElements());
        assertEquals(category.getName(), categoryPage.getContent().get(0).getName());
    }

    @Test
    void shouldReturnListWithCategoryResponse() {
        var categoryList = categoryService.findAllByCurrentUser();
        assertEquals(1, categoryList.size());
        assertEquals(category.getName(), categoryList.get(0).getName());
    }

    @Test
    void shouldReturnCategoryResponse() throws RecordDoesNotExistException {
        var categoryResponse = categoryService.findOneAsResponseByCurrentUserAndName("Food");
        assertEquals(category.getName(), categoryResponse.getName());
    }

    @Test
    void shouldThrowExceptionWhenCategoryResponseDoesNotExist() {
        assertThrows(RecordDoesNotExistException.class,
                () -> categoryService.findOneAsResponseByCurrentUserAndName("Healthcare"));
    }

    @Test
    void shouldReturnCategory() throws RecordDoesNotExistException {
        var category = categoryService.findOneByCurrentUserAndName("Food");
        assertEquals(this.category.getName(), category.getName());
        assertEquals(user.getId(), category.getUser().getId());
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        assertThrows(RecordDoesNotExistException.class,
                () -> categoryService.findOneByCurrentUserAndName("Healthcare"));
    }

    @Test
    void shouldReturnCategoryResponseWhenCategoryIsCreated() throws RecordAlreadyExistsException {
        var categoryResponse = categoryService.createCategoryForCurrentUser(categoryRequest);
        assertEquals(category.getName(), categoryResponse.getName());
    }

    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExists() {
        assertThrows(RecordAlreadyExistsException.class,
                () -> categoryService.createCategoryForCurrentUser(categoryRequest2));
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