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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.entities.Product;
import io.github.dariopipa.tdd.catalog.exceptions.EntityNotFoundException;
import io.github.dariopipa.tdd.catalog.exceptions.ProductNameAlreadyExistsExcpetion;
import io.github.dariopipa.tdd.catalog.service.ProductService;
import io.github.dariopipa.tdd.catalog.views.ProductView;

public class ProductControllerTest {

	private static final long EXISTING_ID = 1L;
	private static final long MISSING_ID = 999L;

	private static final String CATEGORY_NAME = "tech";
	private static final String PRODUCT_NAME = "laptop";
	private static final String UPDATED_PRODUCT_NAME = "hp-laptop";

	private static final String BLANK_NAME = "      ";

	private static final BigDecimal DEFAULT_PRICE = BigDecimal.valueOf(100);
	private static final BigDecimal NEGATIVE_PRICE = BigDecimal.valueOf(-1);

	private ProductController productController;

	@Mock
	private ProductService productService;

	@Mock
	private ProductView productView;

	private AutoCloseable closeable;

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);

		productController = new ProductController(productService, productView);
	}

	@After
	public void tearDown() throws Exception {
		closeable.close();
	}

	@Test
	public void test_createNewProduct_shouldShowTheNewCreatedCategory() {
		Category category = new Category(PRODUCT_NAME);
		Product product = new Product(PRODUCT_NAME, DEFAULT_PRICE, category);

		when(productService.create(PRODUCT_NAME, DEFAULT_PRICE, EXISTING_ID)).thenReturn(EXISTING_ID);
		when(productService.findById(EXISTING_ID)).thenReturn(product);

		productController.create(PRODUCT_NAME, DEFAULT_PRICE, EXISTING_ID);

		verify(productService).create(PRODUCT_NAME, DEFAULT_PRICE, EXISTING_ID);
		verify(productService).findById(EXISTING_ID);
		verify(productView).addedProduct(product);
		verifyNoMoreInteractions(productService, productView);
	}

	@Test
	public void test_createNewProduct_withBlankName_shouldThrowExcpetion() {
		when(productService.create(BLANK_NAME, DEFAULT_PRICE, EXISTING_ID))
				.thenThrow(new IllegalArgumentException("name must be provided"));

		productController.create(BLANK_NAME, DEFAULT_PRICE, EXISTING_ID);

		verify(productService).create(BLANK_NAME, DEFAULT_PRICE, EXISTING_ID);
		verify(productView).showError("Invalid input: name must be provided");
		verifyNoMoreInteractions(productService, productView);
	}

	@Test
	public void test_createNewProduct_withNullName_shouldThrowExcpetion() {
		String name = null;

		when(productService.create(name, DEFAULT_PRICE, EXISTING_ID))
				.thenThrow(new IllegalArgumentException("name must be provided"));

		productController.create(name, DEFAULT_PRICE, EXISTING_ID);

		verify(productService).create(name, DEFAULT_PRICE, EXISTING_ID);
		verify(productView).showError("Invalid input: name must be provided");
		verifyNoMoreInteractions(productService, productView);
	}

	@Test
	public void test_createNewProduct_withExistingName_shouldThrowExcpetion() {
		when(productService.create(PRODUCT_NAME, DEFAULT_PRICE, EXISTING_ID))
				.thenThrow(new ProductNameAlreadyExistsExcpetion());

		productController.create(PRODUCT_NAME, DEFAULT_PRICE, EXISTING_ID);

		verify(productService).create(PRODUCT_NAME, DEFAULT_PRICE, EXISTING_ID);
		verify(productView).showError("Invalid input: Product name already exists");
		verifyNoMoreInteractions(productService, productView);
	}

	@Test
	public void test_createNewProduct_withNullPrice_shouldThrowExcpetion() {
		when(productService.create(PRODUCT_NAME, null, EXISTING_ID))
				.thenThrow(new IllegalArgumentException("price must be provided"));

		productController.create(PRODUCT_NAME, null, EXISTING_ID);

		verify(productService).create(PRODUCT_NAME, null, EXISTING_ID);
		verify(productView).showError("Invalid input: price must be provided");
		verifyNoMoreInteractions(productService, productView);
	}

	@Test
	public void test_createNewProduct_withNegativePrice_shouldThrowExcpetion() {
		when(productService.create(PRODUCT_NAME, NEGATIVE_PRICE, EXISTING_ID))
				.thenThrow(new IllegalArgumentException("price must be positive"));

		productController.create(PRODUCT_NAME, NEGATIVE_PRICE, EXISTING_ID);

		verify(productService).create(PRODUCT_NAME, NEGATIVE_PRICE, EXISTING_ID);
		verify(productView).showError("Invalid input: price must be positive");
		verifyNoMoreInteractions(productService, productView);
	}

	@Test
	public void test_createNewProduct_withNonExistentCategoryId_shouldThrowExcpetion() {
		when(productService.create(PRODUCT_NAME, DEFAULT_PRICE, MISSING_ID))
				.thenThrow(new EntityNotFoundException("category with id:" + MISSING_ID + "not found"));

		productController.create(PRODUCT_NAME, DEFAULT_PRICE, MISSING_ID);

		verify(productService).create(PRODUCT_NAME, DEFAULT_PRICE, MISSING_ID);
		verify(productView).showError("Invalid input: category with id:" + MISSING_ID + "not found");
		verifyNoMoreInteractions(productService, productView);
	}

	@Test
	public void test_deleteProduct_whenProductExists() {
		Category category = new Category(PRODUCT_NAME);
		Product product = new Product(PRODUCT_NAME, DEFAULT_PRICE, category);

		when(productService.findById(EXISTING_ID)).thenReturn(product);
		doNothing().when(productService).delete(EXISTING_ID);

		productController.delete(EXISTING_ID);

		verify(productService).findById(EXISTING_ID);
		verify(productService).delete(EXISTING_ID);
		verify(productView).deletedProduct(product);
		verifyNoMoreInteractions(productService, productView);
	}

	@Test
	public void test_deleteProduct_whenProductDoesNotExists() {
		when(productService.findById(MISSING_ID))
				.thenThrow(new EntityNotFoundException("category with id:" + MISSING_ID + "not found"));

		productController.delete(MISSING_ID);

		InOrder inOrder = inOrder(productService, productView);
		inOrder.verify(productService).findById(MISSING_ID);
		inOrder.verify(productView).showError("category with id:" + MISSING_ID + "not found");
	}

	@Test
	public void test_findAll_shouldReturnListOfProducts() {
		Category category = new Category(CATEGORY_NAME);
		Product productOne = new Product(PRODUCT_NAME, DEFAULT_PRICE, category);
		Product productTwo = new Product(UPDATED_PRODUCT_NAME, DEFAULT_PRICE, category);

		List<Product> products = Arrays.asList(productOne, productTwo);

		when(productService.findAll()).thenReturn(products);

		productController.findAll();

		verify(productService).findAll();
		verify(productView).findAllProducts(products);
	}

	@Test
	public void test_findAll_withNoCategories_shouldReturnEmptyList() {
		List<Product> emptyList = new ArrayList<>();

		when(productService.findAll()).thenReturn(emptyList);

		productController.findAll();

		verify(productService).findAll();
		verify(productView).findAllProducts(emptyList);
	}

	@Test
	public void test_updateProduct_shouldReturnUpdatedProduct() {
		Category category = new Category(UPDATED_PRODUCT_NAME);
		Product updatedProduct = new Product(UPDATED_PRODUCT_NAME, DEFAULT_PRICE, category);

		when(productService.update(EXISTING_ID, UPDATED_PRODUCT_NAME, DEFAULT_PRICE, EXISTING_ID))
				.thenReturn(updatedProduct);

		productController.update(EXISTING_ID, UPDATED_PRODUCT_NAME, DEFAULT_PRICE, EXISTING_ID);

		InOrder inOrder = inOrder(productService, productView);
		inOrder.verify(productService).update(EXISTING_ID, UPDATED_PRODUCT_NAME, DEFAULT_PRICE, EXISTING_ID);
		inOrder.verify(productView).updateProduct(updatedProduct);
	}

	@Test
	public void test_updateProduct_withBlankName_shouldThrowException() {
		when(productService.update(EXISTING_ID, BLANK_NAME, DEFAULT_PRICE, EXISTING_ID))
				.thenThrow(new IllegalArgumentException("name must contain valid characters"));

		productController.update(EXISTING_ID, BLANK_NAME, DEFAULT_PRICE, EXISTING_ID);

		InOrder inOrder = inOrder(productService, productView);
		inOrder.verify(productService).update(EXISTING_ID, BLANK_NAME, DEFAULT_PRICE, EXISTING_ID);
		inOrder.verify(productView).showError("Invalid input: name must contain valid characters");
	}

	@Test
	public void test_updateProduct_withNullName_shouldThrowException() {
		String name = null;

		when(productService.update(EXISTING_ID, name, DEFAULT_PRICE, EXISTING_ID))
				.thenThrow(new IllegalArgumentException("name must be provided"));

		productController.update(EXISTING_ID, name, DEFAULT_PRICE, EXISTING_ID);

		InOrder inOrder = inOrder(productService, productView);
		inOrder.verify(productService).update(EXISTING_ID, name, DEFAULT_PRICE, EXISTING_ID);
		inOrder.verify(productView).showError("Invalid input: name must be provided");
	}

	@Test
	public void test_updateProduct_withExistingName_shouldThrowExcpetion() {
		when(productService.update(EXISTING_ID, PRODUCT_NAME, DEFAULT_PRICE, EXISTING_ID))
				.thenThrow(new ProductNameAlreadyExistsExcpetion());

		productController.update(EXISTING_ID, PRODUCT_NAME, DEFAULT_PRICE, EXISTING_ID);

		InOrder inOrder = inOrder(productService, productView);
		inOrder.verify(productService).update(EXISTING_ID, PRODUCT_NAME, DEFAULT_PRICE, EXISTING_ID);
		inOrder.verify(productView).showError("Invalid input: Product name already exists");
	}

	@Test
	public void test_updateProduct_withNegativePrice_shouldThrowException() {
		when(productService.update(EXISTING_ID, PRODUCT_NAME, NEGATIVE_PRICE, EXISTING_ID))
				.thenThrow(new IllegalArgumentException("price must be positive"));

		productController.update(EXISTING_ID, PRODUCT_NAME, NEGATIVE_PRICE, EXISTING_ID);

		InOrder inOrder = inOrder(productService, productView);
		inOrder.verify(productService).update(EXISTING_ID, PRODUCT_NAME, NEGATIVE_PRICE, EXISTING_ID);
		inOrder.verify(productView).showError("Invalid input: price must be positive");
	}

	@Test
	public void test_updateProduct_withNullPrice_shouldThrowException() {
		when(productService.update(EXISTING_ID, PRODUCT_NAME, null, EXISTING_ID))
				.thenThrow(new IllegalArgumentException("price must be provided"));

		productController.update(EXISTING_ID, PRODUCT_NAME, null, EXISTING_ID);

		InOrder inOrder = inOrder(productService, productView);
		inOrder.verify(productService).update(EXISTING_ID, PRODUCT_NAME, null, EXISTING_ID);
		inOrder.verify(productView).showError("Invalid input: price must be provided");
	}

	@Test
	public void test_updateProduct_withNonExistingCategory_shouldThrowException() {
		when(productService.update(EXISTING_ID, PRODUCT_NAME, DEFAULT_PRICE, MISSING_ID))
				.thenThrow(new EntityNotFoundException("product with id: " + MISSING_ID + " not found"));

		productController.update(EXISTING_ID, PRODUCT_NAME, DEFAULT_PRICE, MISSING_ID);

		InOrder inOrder = inOrder(productService, productView);
		inOrder.verify(productService).update(EXISTING_ID, PRODUCT_NAME, DEFAULT_PRICE, MISSING_ID);
		inOrder.verify(productView).showError("Invalid input: product with id: 999 not found");
	}

}