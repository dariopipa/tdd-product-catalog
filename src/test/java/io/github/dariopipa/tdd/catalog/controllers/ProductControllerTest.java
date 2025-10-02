package io.github.dariopipa.tdd.catalog.controllers;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.entities.Product;
import io.github.dariopipa.tdd.catalog.exceptions.EntityNotFoundException;
import io.github.dariopipa.tdd.catalog.exceptions.ProductNameAlreadyExistsExcpetion;
import io.github.dariopipa.tdd.catalog.service.CategoryService;
import io.github.dariopipa.tdd.catalog.service.ProductService;
import io.github.dariopipa.tdd.catalog.views.ProductView;

public class ProductControllerTest {

	private ProductController productController;

	@Mock
	private ProductService productService;

	@Mock
	private ProductView productView;

	@Mock
	private CategoryService categoryService;

	@Before
	public void setup() {
		MockitoAnnotations.openMocks(this);

		productController = new ProductController(productService, productView);
	}

	@Test
	public void test_createNewProduct_shouldShowTheNewCreatedCategory() {
		String name = "new product";
		Category category = new Category(name);
		Product product = new Product(name, BigDecimal.valueOf(100), category);

		when(productService.create(name, BigDecimal.valueOf(100), 1L)).thenReturn(1L);
		when(productService.findById(1L)).thenReturn(product);

		productController.create(name, BigDecimal.valueOf(100), 1L);

		verify(productService).create(name, BigDecimal.valueOf(100), 1L);
		verify(productService).findById(1L);
		verify(productView).addedProduct(product);
		verifyNoMoreInteractions(productService, productView);
	}

	@Test
	public void test_createNewProduct_withBlankName_shouldThrowExcpetion() {
		String name = "      ";

		when(productService.create(name, BigDecimal.valueOf(100), 1L))
				.thenThrow(new IllegalArgumentException("name must be provided"));

		productController.create(name, BigDecimal.valueOf(100), 1L);

		verify(productService).create(name, BigDecimal.valueOf(100), 1L);
		verify(productView).showError("Invalid input: name must be provided");
		verifyNoMoreInteractions(productService, productView);
	}

	@Test
	public void test_createNewProduct_withNullName_shouldThrowExcpetion() {
		String name = null;

		when(productService.create(name, BigDecimal.valueOf(100), 1L))
				.thenThrow(new IllegalArgumentException("name must be provided"));

		productController.create(name, BigDecimal.valueOf(100), 1L);

		verify(productService).create(name, BigDecimal.valueOf(100), 1L);
		verify(productView).showError("Invalid input: name must be provided");
		verifyNoMoreInteractions(productService, productView);
	}

	@Test
	public void test_createNewProduct_withExistingName_shouldThrowExcpetion() {
		String name = "existing name";

		when(productService.create(name, BigDecimal.valueOf(100), 1L))
				.thenThrow(new ProductNameAlreadyExistsExcpetion());

		productController.create(name, BigDecimal.valueOf(100), 1L);

		verify(productService).create(name, BigDecimal.valueOf(100), 1L);
		verify(productView).showError("Invalid input: Product name already exists");
		verifyNoMoreInteractions(productService, productView);
	}

	@Test
	public void test_createNewProduct_withNullPrice_shouldThrowExcpetion() {
		String name = "product";

		when(productService.create(name, null, 1L)).thenThrow(new IllegalArgumentException("price must be provided"));

		productController.create(name, null, 1L);

		verify(productService).create(name, null, 1L);
		verify(productView).showError("Invalid input: price must be provided");
		verifyNoMoreInteractions(productService, productView);
	}

	@Test
	public void test_createNewProduct_withNegativePrice_shouldThrowExcpetion() {
		String name = "product";

		when(productService.create(name, BigDecimal.valueOf(-1), 1L))
				.thenThrow(new IllegalArgumentException("price must be positive"));

		productController.create(name, BigDecimal.valueOf(-1), 1L);

		verify(productService).create(name, BigDecimal.valueOf(-1), 1L);
		verify(productView).showError("Invalid input: price must be positive");
		verifyNoMoreInteractions(productService, productView);
	}

	@Test
	public void test_createNewProduct_withNonExistentCategoryId_shouldThrowExcpetion() {
		String name = "product";

		when(productService.create(name, BigDecimal.valueOf(1), 999L))
				.thenThrow(new EntityNotFoundException("category with id:" + 999L + "not found"));

		productController.create(name, BigDecimal.valueOf(1), 999L);

		verify(productService).create(name, BigDecimal.valueOf(1), 999L);
		verify(productView).showError("Invalid input: category with id:" + 999L + "not found");
		verifyNoMoreInteractions(productService, productView);
	}

	@Test
	public void test_deleteProduct_whenProductExists() {
		String name = "product";
		Category category = new Category(name);
		Product product = new Product(name, BigDecimal.valueOf(100), category);

		when(productService.findById(1L)).thenReturn(product);
		doNothing().when(productService).delete(1L);

		productController.delete(1L);

		verify(productService).findById(1L);
		verify(productService).delete(1L);
		verify(productView).deletedProduct(product);
		verifyNoMoreInteractions(productService, productView);
	}

	@Test
	public void test_deleteProduct_whenProductDoesNotExists() {
		when(productService.findById(999L))
				.thenThrow(new EntityNotFoundException("category with id:" + 999L + "not found"));

		productController.delete(999L);

		InOrder inOrder = inOrder(productService, productView);
		inOrder.verify(productService).findById(999L);
		inOrder.verify(productView).showError("category with id:" + 999L + "not found");
	}

	@Test
	public void test_findAll_shouldReturnListOfProducts() {
		Category category = new Category("tech");
		Product product1 = new Product("iphone", BigDecimal.valueOf(111), category);
		Product product2 = new Product("android", BigDecimal.valueOf(100), category);

		List<Product> products = Arrays.asList(product1, product2);

		when(productService.findAll()).thenReturn(products);

		productController.findAll();

		verify(productService).findAll();
		verify(productView).findAllProducts(products);
		verifyNoMoreInteractions(categoryService, productView);
	}

	@Test
	public void test_findAll_withNoCategories_shouldReturnEmptyList() {
		List<Product> emptyList = new ArrayList<>();

		when(productService.findAll()).thenReturn(emptyList);

		productController.findAll();

		verify(productService).findAll();
		verify(productView).findAllProducts(emptyList);
		verifyNoMoreInteractions(categoryService, productView);
	}

	@Test
	public void test_updateProduct_shouldReturnUpdatedProduct() {
		String newName = "updated category name";
		Category category = new Category(newName);
		Product updatedProduct = new Product(newName, BigDecimal.valueOf(199), category);

		when(productService.update(1L, newName, BigDecimal.valueOf(199), 1L)).thenReturn(updatedProduct);

		productController.update(1L, newName, BigDecimal.valueOf(199), 1L);

		InOrder inOrder = inOrder(productService, productView);
		inOrder.verify(productService).update(1L, newName, BigDecimal.valueOf(199), 1L);
		inOrder.verify(productView).updateProduct(updatedProduct);
	}

	@Test
	public void test_updateProduct_withBlankName_shouldThrowException() {
		String name = "    ";

		when(productService.update(1L, name, BigDecimal.valueOf(199), 1L))
				.thenThrow(new IllegalArgumentException("name must contain valid characters"));

		productController.update(1L, name, BigDecimal.valueOf(199), 1L);

		InOrder inOrder = inOrder(productService, productView);
		inOrder.verify(productService).update(1L, name, BigDecimal.valueOf(199), 1L);
		inOrder.verify(productView).showError("Invalid input: name must contain valid characters");
	}

	@Test
	public void test_updateProduct_withNullName_shouldThrowException() {
		String name = null;

		when(productService.update(1L, name, BigDecimal.valueOf(199), 1L))
				.thenThrow(new IllegalArgumentException("name must be provided"));

		productController.update(1L, name, BigDecimal.valueOf(199), 1L);

		InOrder inOrder = inOrder(productService, productView);
		inOrder.verify(productService).update(1L, name, BigDecimal.valueOf(199), 1L);
		inOrder.verify(productView).showError("Invalid input: name must be provided");
	}

	@Test
	public void test_updateProduct_withExistingName_shouldThrowExcpetion() {
		String name = "existing-name";

		when(productService.update(1L, name, BigDecimal.valueOf(199), 1L))
				.thenThrow(new ProductNameAlreadyExistsExcpetion());

		productController.update(1L, name, BigDecimal.valueOf(199), 1L);

		InOrder inOrder = inOrder(productService, productView);
		inOrder.verify(productService).update(1L, name, BigDecimal.valueOf(199), 1L);
		inOrder.verify(productView).showError("Invalid input: Product name already exists");
	}

	@Test
	public void test_updateProduct_withNegativePrice_shouldThrowException() {
		String name = "product";

		when(productService.update(1L, name, BigDecimal.valueOf(-1L), 1L))
				.thenThrow(new IllegalArgumentException("price must be positive"));

		productController.update(1L, name, BigDecimal.valueOf(-1L), 1L);

		InOrder inOrder = inOrder(productService, productView);
		inOrder.verify(productService).update(1L, name, BigDecimal.valueOf(-1L), 1L);
		inOrder.verify(productView).showError("Invalid input: price must be positive");
	}

	@Test
	public void test_updateProduct_withNullPrice_shouldThrowException() {
		String name = "product";

		when(productService.update(1L, name, null, 1L))
				.thenThrow(new IllegalArgumentException("price must be provided"));

		productController.update(1L, name, null, 1L);

		InOrder inOrder = inOrder(productService, productView);
		inOrder.verify(productService).update(1L, name, null, 1L);
		inOrder.verify(productView).showError("Invalid input: price must be provided");
	}

	@Test
	public void test_updateProduct_withNonExistingCategory_shouldThrowException() {
		String name = "product";

		when(productService.update(1L, name, BigDecimal.valueOf(1), 999L))
				.thenThrow(new EntityNotFoundException("product with id: " + 999L + " not found"));

		productController.update(1L, name, BigDecimal.valueOf(1), 999L);

		InOrder inOrder = inOrder(productService, productView);
		inOrder.verify(productService).update(1L, name, BigDecimal.valueOf(1), 999L);
		inOrder.verify(productView).showError("Invalid input: product with id: 999 not found");
	}

}
