package io.github.dariopipa.tdd.catalog.controllers;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
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

	private static final long EXISTING_ID = 1L;
	private static final long MISSING_ID = 999L;

	private static final String CATEGORY_NAME = "new category";
	private static final String CATEGORY_UPDATED_NAME = "updated category name";
	private static final String BLANK_NAME = "     ";
	private static final String BLANK_TABS_NAME = "      \t\t\t\t         ";

	private CategoryController categoryController;

	@Mock
	private CategoryService categoryService;

	@Mock
	private CategoryView categoryView;

	private AutoCloseable closeable;

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		categoryController = new CategoryController(categoryService, categoryView);
	}

	@After
	public void tearDown() throws Exception {
		closeable.close();
	}

	@Test
	public void test_createNewCategory_shouldShowTheNewCreatedCategory() {
		Category createdCategory = new Category(CATEGORY_NAME);

		when(categoryService.createCategory(CATEGORY_NAME)).thenReturn(EXISTING_ID);
		when(categoryService.findById(EXISTING_ID)).thenReturn(createdCategory);

		categoryController.create(CATEGORY_NAME);

		verify(categoryService).createCategory(CATEGORY_NAME);
		verify(categoryService).findById(EXISTING_ID);
		verify(categoryView).addedCategory(createdCategory);
		verifyNoMoreInteractions(categoryService, categoryView);
	}

	@Test
	public void test_createNewCategory_withBlankName_shouldThrowException() {
		when(categoryService.createCategory(BLANK_NAME))
				.thenThrow(new IllegalArgumentException("name must be provided"));

		categoryController.create(BLANK_NAME);

		verify(categoryService).createCategory(BLANK_NAME);
		verify(categoryView).showError("Invalid input: name must be provided");
		verifyNoMoreInteractions(categoryService, categoryView);
	}

	@Test
	public void test_createNewCategory_withEmptyName_shouldThrowException() {
		when(categoryService.createCategory(null)).thenThrow(new IllegalArgumentException("name must be provided"));

		categoryController.create(null);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).createCategory(null);
		inOrder.verify(categoryView).showError("Invalid input: name must be provided");
		verifyNoMoreInteractions(categoryService, categoryView);
	}

	@Test
	public void test_createNewCategory_withExistingName_shouldThrowException() {
		when(categoryService.createCategory(CATEGORY_NAME)).thenThrow(new CategoryNameAlreadyExistsExcpetion());

		categoryController.create(CATEGORY_NAME);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).createCategory(CATEGORY_NAME);
		inOrder.verify(categoryView).showError("Category name already exists");
		verifyNoMoreInteractions(categoryService, categoryView);
	}

	@Test
	public void test_deleteCategory_whenCategoryExists() {
		Category existingCategory = new Category(CATEGORY_NAME);

		when(categoryService.findById(EXISTING_ID)).thenReturn(existingCategory);
		when(categoryService.delete(EXISTING_ID)).thenReturn("Deleted");

		categoryController.delete(EXISTING_ID);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).findById(EXISTING_ID);
		inOrder.verify(categoryService).delete(EXISTING_ID);
		inOrder.verify(categoryView).deletedCategory(existingCategory);
	}

	@Test
	public void test_deleteCategory_whenCategoryDoesNotExists() {
		when(categoryService.findById(MISSING_ID))
				.thenThrow(new EntityNotFoundException("category with id:" + MISSING_ID + "not found"));

		categoryController.delete(MISSING_ID);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).findById(MISSING_ID);
		inOrder.verify(categoryView).showError("category with id:" + MISSING_ID + "not found");
	}

	@Test
	public void test_updateCategory_shouldReturnUpdatedCategory() {
		Category updatedCategory = new Category(CATEGORY_UPDATED_NAME);

		when(categoryService.update(EXISTING_ID, CATEGORY_UPDATED_NAME)).thenReturn(updatedCategory);

		categoryController.update(EXISTING_ID, CATEGORY_UPDATED_NAME);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).update(EXISTING_ID, CATEGORY_UPDATED_NAME);
		inOrder.verify(categoryView).updateCategory(updatedCategory);
	}

	@Test
	public void test_updateCategory_withNULL_NAME_shouldThrowExcpetion() {
		when(categoryService.update(EXISTING_ID, null))
				.thenThrow(new IllegalArgumentException("name must be provided"));

		categoryController.update(EXISTING_ID, null);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).update(EXISTING_ID, null);
		inOrder.verify(categoryView).showError("Invalid input: name must be provided");
	}

	@Test
	public void test_updateCategory_withBlankName_shouldThrowExcpetion() {
		when(categoryService.update(EXISTING_ID, BLANK_TABS_NAME))
				.thenThrow(new IllegalArgumentException("name must be provided"));

		categoryController.update(EXISTING_ID, BLANK_TABS_NAME);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).update(EXISTING_ID, BLANK_TABS_NAME);
		inOrder.verify(categoryView).showError("Invalid input: name must be provided");
	}

	@Test
	public void test_updateCategory_withExistingName_shouldThrowExcpetion() {
		when(categoryService.update(EXISTING_ID, CATEGORY_NAME)).thenThrow(new CategoryNameAlreadyExistsExcpetion());

		categoryController.update(EXISTING_ID, CATEGORY_NAME);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).update(EXISTING_ID, CATEGORY_NAME);
		inOrder.verify(categoryView).showError("Category name already exists");
		verifyNoMoreInteractions(categoryService);
	}

	@Test
	public void test_updateCategory_whenCategoryDoesNotExists_shouldThrowExcpetion() {
		when(categoryService.update(MISSING_ID, CATEGORY_UPDATED_NAME))
				.thenThrow(new EntityNotFoundException("category with id:" + MISSING_ID + " not found"));

		categoryController.update(MISSING_ID, CATEGORY_UPDATED_NAME);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).update(MISSING_ID, CATEGORY_UPDATED_NAME);
		inOrder.verify(categoryView).showError("category with id:" + MISSING_ID + " not found");
	}

	@Test
	public void test_findAll_shouldReturnListOfCateegories() {
		List<Category> categories = Arrays.asList(new Category(CATEGORY_NAME), new Category("tech"),
				new Category("books"));

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
		Category existingCategory = new Category(CATEGORY_NAME);

		when(categoryService.findById(EXISTING_ID)).thenReturn(existingCategory);
		when(categoryService.delete(EXISTING_ID)).thenThrow(new CategoryInUseException());

		categoryController.delete(EXISTING_ID);

		InOrder inOrder = inOrder(categoryService, categoryView);
		inOrder.verify(categoryService).findById(EXISTING_ID);
		inOrder.verify(categoryService).delete(EXISTING_ID);
		inOrder.verify(categoryView).showError("Category in use by existing products");
	}
}
