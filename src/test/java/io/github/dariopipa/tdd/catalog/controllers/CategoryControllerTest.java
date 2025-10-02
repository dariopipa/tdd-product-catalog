package io.github.dariopipa.tdd.catalog.controllers;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.exceptions.CategoryInUseException;
import io.github.dariopipa.tdd.catalog.exceptions.CategoryNameAlreadyExistsExcpetion;
import io.github.dariopipa.tdd.catalog.exceptions.EntityNotFoundException;
import io.github.dariopipa.tdd.catalog.service.CategoryService;
import io.github.dariopipa.tdd.catalog.views.CategoryView;

public class CategoryControllerTest {

	private CategoryController categoryController;

	@Mock
	private CategoryService categoryService;
	@Mock
	private CategoryView categoryView;

	@Before
	public void setup() {
		MockitoAnnotations.openMocks(this);

		categoryController = new CategoryController(categoryService, categoryView);
	}

	@Test
	public void test_createNewCategory_shouldShowTheNewCreatedCategory() {
		String name = "new category";
		Category createdCategory = new Category(name);

		when(categoryService.createCategory(name)).thenReturn(1L);
		when(categoryService.findById(1L)).thenReturn(createdCategory);

		categoryController.create(name);

		verify(categoryService).createCategory(name);
		verify(categoryService).findById(1L);
		verify(categoryView).addedCategory(createdCategory);
		verifyNoMoreInteractions(categoryService, categoryView);
	}

	@Test
	public void test_createNewCategory_withBlankName_shouldThrowException() {
		String blankName = "     ";

		when(categoryService.createCategory(blankName))
				.thenThrow(new IllegalArgumentException("name must be provided"));

		categoryController.create(blankName);

		verify(categoryService).createCategory(blankName);
		verify(categoryView).showError("Invalid input: name must be provided");
		verifyNoMoreInteractions(categoryService, categoryView);
	}

	@Test
	public void test_createNewCategory_withEmptyName_shouldThrowException() {
		String nullName = null;

		when(categoryService.createCategory(nullName)).thenThrow(new IllegalArgumentException("name must be provided"));

		categoryController.create(nullName);
		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).createCategory(nullName);
		inOrder.verify(categoryView).showError("Invalid input: name must be provided");
		verifyNoMoreInteractions(categoryService, categoryView);
	}

	@Test
	public void test_createNewCategory_withExistingName_shouldThrowException() {
		String name = "new name";

		when(categoryService.createCategory(name)).thenThrow(new CategoryNameAlreadyExistsExcpetion());

		categoryController.create(name);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).createCategory(name);
		inOrder.verify(categoryView).showError("Category name already exists");
		verifyNoMoreInteractions(categoryService, categoryView);
	}

	@Test
	public void test_deleteCategory_whenCategoryExists() {
		Category existingCategory = new Category("Electronics");

		when(categoryService.findById(1L)).thenReturn(existingCategory);
		when(categoryService.delete(1L)).thenReturn("Deleted");

		categoryController.delete(1L);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).findById(1L);
		inOrder.verify(categoryService).delete(1L);
		inOrder.verify(categoryView).deletedCategory(existingCategory);
	}

	@Test
	public void test_deleteCategory_whenCategoryDoesNotExists() {
		when(categoryService.findById(999L))
				.thenThrow(new EntityNotFoundException("category with id:" + 999L + "not found"));

		categoryController.delete(999L);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).findById(999L);
		inOrder.verify(categoryView).showError("category with id:" + 999L + "not found");
	}

	@Test
	public void test_updateCategory_shouldReturnUpdatedCategory() {
		String newName = "updated category name";
		Category updatedCategory = new Category(newName);

		when(categoryService.update(1L, newName)).thenReturn(updatedCategory);

		categoryController.update(1L, newName);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).update(1L, newName);
		inOrder.verify(categoryView).updateCategory(updatedCategory);
	}

	@Test
	public void test_updateCategory_withNullName_shouldThrowExcpetion() {
		String nullName = null;

		when(categoryService.update(1L, nullName)).thenThrow(new IllegalArgumentException("name must be provided"));

		categoryController.update(1L, nullName);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).update(1L, nullName);
		inOrder.verify(categoryView).showError("Invalid input: name must be provided");
	}

	@Test
	public void test_updateCategory_withBlankName_shouldThrowExcpetion() {
		String blankName = "      \t\t\t\t         ";

		when(categoryService.update(1L, blankName)).thenThrow(new IllegalArgumentException("name must be provided"));

		categoryController.update(1L, blankName);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).update(1L, blankName);
		inOrder.verify(categoryView).showError("Invalid input: name must be provided");
	}

	@Test
	public void test_updateCategory_withExistingName_shouldThrowExcpetion() {
		when(categoryService.update(1L, "electronics")).thenThrow(new CategoryNameAlreadyExistsExcpetion());

		categoryController.update(1L, "electronics");

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).update(1L, "electronics");
		inOrder.verify(categoryView).showError("Category name already exists");
		verifyNoMoreInteractions(categoryService);
	}

	@Test
	public void test_updateCategory_whenCategoryDoesNotExists_shouldThrowExcpetion() {
		when(categoryService.update(999L, "books"))
				.thenThrow(new EntityNotFoundException("category with id:" + 999L + " not found"));

		categoryController.update(999L, "books");

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).update(999L, "books");
		inOrder.verify(categoryView).showError("category with id:" + 999L + " not found");
	}

	@Test
	public void test_findAll_shouldReturnListOfCateegories() {
		List<Category> categories = Arrays.asList(new Category("electronics"), new Category("tech"),
				new Category("categories"));

		when(categoryService.findAll()).thenReturn(categories);

		categoryController.findAll();

		verify(categoryService).findAll();
		verify(categoryView).findAllCategories(categories);
		verifyNoMoreInteractions(categoryService, categoryView);
	}

	@Test
	public void test_findAll_withNoCategories_shouldReturnEmptyList() {
		List<Category> emptyList = new ArrayList<>();

		when(categoryService.findAll()).thenReturn(emptyList);

		categoryController.findAll();

		verify(categoryService).findAll();
		verify(categoryView).findAllCategories(emptyList);
		verifyNoMoreInteractions(categoryService, categoryView);
	}

	@Test
	public void test_deleteCategoryInUseByProducts_shouldThrowExcpetion() {
		Category existingCategory = new Category("Electronics");

		when(categoryService.findById(1L)).thenReturn(existingCategory);
		when(categoryService.delete(1L)).thenThrow(new CategoryInUseException());

		categoryController.delete(1L);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).findById(1L);
		inOrder.verify(categoryService).delete(1L);
		inOrder.verify(categoryView).showError("Category in use by existing products");
	}

}
