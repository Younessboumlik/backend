package com.myshop.service;

import com.myshop.domain.entity.Category;
import com.myshop.dto.request.CategoryRequest;
import com.myshop.dto.response.CategoryResponse;
import com.myshop.repository.CategoryRepository;
import com.myshop.repository.ProductRepository;
import com.myshop.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electronique")
                .description("Appareils électroniques")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void testCreateCategory_Success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Vêtements");
        request.setDescription("Vêtements et accessoires");

        when(categoryRepository.existsByNameIgnoreCase("Vêtements")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        verify(categoryRepository, times(1)).existsByNameIgnoreCase("Vêtements");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testCreateCategory_NameAlreadyExists() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronique");

        when(categoryRepository.existsByNameIgnoreCase("Electronique")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> categoryService.createCategory(request));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void testGetCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        CategoryResponse response = categoryService.getCategory(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void testDeleteCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).delete(testCategory);
    }

    @Test
    void testDeleteCategory_WithProducts() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> categoryService.deleteCategory(1L));
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void testGetAllCategories_Success() {
        Category category2 = Category.builder()
                .id(2L)
                .name("Vêtements")
                .createdAt(Instant.now())
                .build();

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(testCategory, category2));

        List<CategoryResponse> responses = categoryService.getAllCategories();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(categoryRepository, times(1)).findAll();
    }
}


