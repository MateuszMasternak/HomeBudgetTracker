package com.rainy.homebudgettracker.category;

import com.rainy.homebudgettracker.handler.exception.*;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.TransactionRepository;
import com.rainy.homebudgettracker.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(userService.getUserSub()).thenReturn(TestData.USER_SUB);

        when(categoryRepository.findById(TestData.ASSOCIATED_CATEGORY_ID))
                .thenReturn(Optional.of(TestData.CATEGORY_ASSOCIATED));

        when(categoryRepository.findByUserSubAndName(TestData.USER_SUB, "Food"))
                .thenReturn(Optional.of(TestData.CATEGORY_FOOD));

        when(categoryRepository.findByUserSubAndName(TestData.USER_SUB, "Healthcare"))
                .thenReturn(Optional.empty());

        when(categoryRepository.findById(TestData.CATEGORY_ID_FOOD))
                .thenReturn(Optional.of(TestData.CATEGORY_FOOD));

        when(categoryRepository.findById(TestData.OTHER_USER_CATEGORY_ID))
                .thenReturn(Optional.of(TestData.CATEGORY_OTHER_USER));

        when(categoryRepository.findAllByUserSub(TestData.USER_SUB, TestData.PAGEABLE))
                .thenReturn(new PageImpl<>(List.of(TestData.CATEGORY_FOOD)));

        when(transactionRepository.existsByCategory(TestData.CATEGORY_FOOD)).thenReturn(false);
        when(transactionRepository.existsByCategory(TestData.CATEGORY_ASSOCIATED)).thenReturn(true);

        when(categoryRepository.save(TestData.CATEGORY_TRANSPORT)).thenReturn(TestData.CATEGORY_TRANSPORT);
        doNothing().when(categoryRepository).deleteById(TestData.CATEGORY_ID_FOOD);

        when(categoryRepository.findAllByUserSub(TestData.USER_SUB))
                .thenReturn(List.of(TestData.CATEGORY_FOOD));

        when(categoryRepository.existsByUserSubAndName(TestData.USER_SUB, "Food")).thenReturn(true);
        when(categoryRepository.existsByUserSubAndName(TestData.USER_SUB, "Healthcare")).thenReturn(true);
        when(categoryRepository.existsByUserSubAndName(TestData.USER_SUB, "Transport")).thenReturn(false);

        when(modelMapper.map(TestData.CATEGORY_FOOD, CategoryResponse.class))
                .thenReturn(TestData.CATEGORY_RESPONSE_FOOD);
        when(modelMapper.map(TestData.CATEGORY_REQUEST_TRANSPORT, Category.class, TestData.USER_SUB)).thenReturn(TestData.CATEGORY_TRANSPORT);
        when(modelMapper.map(TestData.CATEGORY_TRANSPORT, CategoryResponse.class)).thenReturn(TestData.CATEGORY_RESPONSE_TRANSPORT);
    }

    @Test
    void shouldReturnPageWithCategoryResponse() {
        var categoryPage = categoryService.findCurrentUserCategoriesAsResponses(TestData.PAGEABLE);

        assertEquals(1, categoryPage.getTotalElements());
        assertEquals(TestData.CATEGORY_RESPONSE_FOOD, categoryPage.getContent().get(0));
    }

    @Test
    void shouldReturnListWithCategoryResponse() {
        var categoryList = categoryService.findCurrentUserCategoriesAsResponses();

        assertEquals(1, categoryList.size());
        assertEquals(TestData.CATEGORY_RESPONSE_FOOD, categoryList.get(0));
    }

    @Test
    void shouldReturnCategoryResponse() throws RecordDoesNotExistException {
        var returnedCategoryResponse = categoryService.findCurrentUserCategoryAsResponse("Food");

        assertEquals(TestData.CATEGORY_RESPONSE_FOOD, returnedCategoryResponse);
    }

    @Test
    void shouldThrowExceptionWhenCategoryResponseDoesNotExist() {
        assertThrows(RecordDoesNotExistException.class,
                () -> categoryService.findCurrentUserCategoryAsResponse("Healthcare"));
    }

    @Test
    void shouldReturnCategory() throws RecordDoesNotExistException {
        var returnedCategory = categoryService.findCurrentUserCategory("Food");

        assertEquals(TestData.CATEGORY_FOOD, returnedCategory);
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        assertThrows(RecordDoesNotExistException.class,
                () -> categoryService.findCurrentUserCategory("Healthcare"));
    }

    @Test
    void shouldReturnCategoryResponseWhenCategoryIsCreated() throws RecordAlreadyExistsException {
        var categoryRequest = new CategoryRequest("Transport");

        var returnedCategoryResponse = categoryService.createCategoryForCurrentUser(categoryRequest);

        assertEquals(TestData.CATEGORY_RESPONSE_TRANSPORT, returnedCategoryResponse);
    }

    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExists() {
        var categoryRequest = new CategoryRequest("Healthcare");

        assertThrows(RecordAlreadyExistsException.class,
                () -> categoryService.createCategoryForCurrentUser(categoryRequest));
    }

    @Test
    void shouldDeleteCategory() {
        assertDoesNotThrow(() -> categoryService.deleteCurrentUserCategory(TestData.CATEGORY_ID_FOOD));
    }

    @Test
    void shouldThrowExceptionWhenCategoryIsAssociatedWithTransactionWhenDeleting() {
        assertThrows(CategoryAssociatedWithTransactionException.class,
                () -> categoryService.deleteCurrentUserCategory(TestData.ASSOCIATED_CATEGORY_ID));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotOwnerWhenDeleting() {
        assertThrows(UserIsNotOwnerException.class,
                () -> categoryService.deleteCurrentUserCategory(TestData.OTHER_USER_CATEGORY_ID));
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExistWhenDeleting() {
        assertThrows(RecordDoesNotExistException.class,
                () -> categoryService.deleteCurrentUserCategory(TestData.NON_EXISTENT_CATEGORY_ID));
    }
}
