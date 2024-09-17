//package com.rainy.homebudgettracker.controller;
//
//import com.rainy.homebudgettracker.auth.JwtService;
//import com.rainy.homebudgettracker.category.*;
//import com.rainy.homebudgettracker.handler.exception.CategoryAssociatedWithTransactionException;
//import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
//import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
//import com.rainy.homebudgettracker.user.Role;
//import com.rainy.homebudgettracker.user.User;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.MediaType;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(CategoryController.class)
//public class CategoryControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private CategoryService categoryService;
//
//    @MockBean
//    private JwtService jwtService;
//
//    private User user;
//
//    @BeforeEach
//    public void setup() {
//        user = User.builder()
//                .id(1L)
//                .email("test@test.com")
//                .password("password")
//                .enabled(true)
//                .role(Role.USER)
//                .build();
//
//        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
//        Authentication authentication = new UsernamePasswordAuthenticationToken(
//                user, user.getPassword(), user.getAuthorities());
//        securityContext.setAuthentication(authentication);
//        SecurityContextHolder.setContext(securityContext);
//    }
//
//    @AfterAll
//    public static void tearDown() {
//        SecurityContextHolder.clearContext();
//    }
//
//    @Test
//    public void getAllCategoriesByUserReturn200() throws Exception {
//        when(categoryService.findAllByUser(any(User.class), any(Pageable.class))).thenReturn(Page.empty());
//
//        mockMvc.perform(get("/api/v1/category?page=0&size=10")
//                        .with(user(user))
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void getAllCategoriesByUserReturn401IfNotLoggedIn() throws Exception {
//        SecurityContextHolder.clearContext();
//
//        mockMvc.perform(get("/api/v1/category?page=0&size=10")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    public void createCategoryReturn200() throws Exception {
//        when(categoryService.createCategory(any(User.class), any(CategoryRequest.class)))
//                .thenReturn(CategoryResponse.builder().name("category").build());
//
//        mockMvc.perform(post("/api/v1/category")
//                        .with(user(user))
//                        .with(csrf())
//                        .content("{\"name\": \"category\"}")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void createCategoryReturn401IfNotLoggedIn() throws Exception {
//        SecurityContextHolder.clearContext();
//
//        mockMvc.perform(post("/api/v1/category")
//                        .with(csrf())
//                        .content("{\"name\": \"category\"}")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    public void deleteCategoryReturn204() throws Exception {
//        doNothing().when(categoryService).deleteCategory(any(User.class), any(Long.class));
//
//        mockMvc.perform(delete("/api/v1/category/1")
//                        .with(user(user))
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNoContent());
//    }
//
//    @Test
//    public void deleteCategoryReturn401IfNotLoggedIn() throws Exception {
//        SecurityContextHolder.clearContext();
//
//        mockMvc.perform(delete("/api/v1/category/1")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    public void deleteCategoryReturn403IfUserIsNotOwner() throws Exception {
//        doThrow(UserIsNotOwnerException.class)
//                .when(categoryService).deleteCategory(any(User.class), eq(1L));
//
//        mockMvc.perform(delete("/api/v1/category/1")
//                        .with(user(user))
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    public void deleteCategoryReturn404IfCategoryDoesNotExist() throws Exception {
//        doThrow(RecordDoesNotExistException.class)
//                .when(categoryService).deleteCategory(any(User.class), eq(1L));
//
//        mockMvc.perform(delete("/api/v1/category/1")
//                        .with(user(user))
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    public void deleteCategoryReturn400IfCategoryIsAssociatedWithTransaction() throws Exception {
//        doThrow(CategoryAssociatedWithTransactionException.class)
//                .when(categoryService).deleteCategory(any(User.class), eq(1L));
//
//        mockMvc.perform(delete("/api/v1/category/1")
//                        .with(user(user))
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest());
//    }
//}
