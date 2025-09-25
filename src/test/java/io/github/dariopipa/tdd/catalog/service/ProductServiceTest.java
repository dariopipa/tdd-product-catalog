package io.github.dariopipa.tdd.catalog.service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.entities.Product;
import io.github.dariopipa.tdd.catalog.exceptions.EntityNotFoundException;
import io.github.dariopipa.tdd.catalog.exceptions.ProductNameAlreadyExistsExcpetion;
import io.github.dariopipa.tdd.catalog.repository.ProductRepository;
import io.github.dariopipa.tdd.catalog.transactionManger.TransactionCode;
import io.github.dariopipa.tdd.catalog.transactionManger.TransactionManager;

public class ProductServiceTest {

	private ProductService productService;
	private ProductRepository productRepository;
	private CategoryService categoryService;
	private TransactionManager transactionManager;

	private BigDecimal productPrice = new BigDecimal(111.11);
	private BigDecimal negativeProductPrice = new BigDecimal(-111111);
	private BigDecimal zeroPrice = new BigDecimal(0);
	private Long existingId = 1L;
	private Long nonExistingId = 999L;
	private Long negativeId = -1L;
	private String existingName = " EXISTING NAME ";
	private String newName = " new name ";
	private String normalizedName = "new name";
	private Category newCategory = new Category("category name");

	@Before
	public void setup() {
		this.productRepository = mock(ProductRepository.class);
		this.categoryService = mock(CategoryService.class);
		this.transactionManager = mock(TransactionManager.class);

		when(transactionManager.doInTransaction(any())).thenAnswer(answer((TransactionCode<?> code) -> code.execute()));

		this.productService = new ProductService(productRepository, categoryService, transactionManager);
	}

	@Test
	public void test_whenCreatingNewProductWithValidRequestBody_ReturnCreatedId() {
		Category category = new Category("Electronics");

		when(categoryService.findById(existingId)).thenReturn(category);
		when(productRepository.findByName("product name")).thenReturn(null);
		when(productRepository.create(any(Product.class))).thenReturn(1L);

		Long createdProduct = productService.create("Product Name", productPrice, existingId);

		assertThat(createdProduct).isEqualTo(1L);
		verify(categoryService).findByIdInternal(existingId);
		verify(productRepository).create(any(Product.class));
		verify(transactionManager).doInTransaction(any());
	}

	@Test
	public void test_createNewProductWithMissingName_ShouldThrowExcpetion() {
		assertThatThrownBy(() -> productService.create(null, productPrice, existingId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be provided");

		verify(productRepository, never()).create(any(Product.class));
		verify(transactionManager).doInTransaction(any());
	}

	@Test
	public void test_createNewProductWithBlankName_ShouldThrowExcpetion() {
		assertThatThrownBy(() -> productService.create("\t\t\t \n   ", productPrice, existingId))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must contain valid characters");

		verify(productRepository, never()).create(any(Product.class));
	}

	@Test
	public void test_createNewProductWithBlankPrice_ShouldThrowExcpetion() {
		assertThatThrownBy(() -> productService.create("ProductName", null, existingId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("price must be provided");

		verify(productRepository, never()).create(any(Product.class));
	}

	@Test
	public void test_createNewProductWithNegativePrice_ShouldThrowExcpetion() {
		assertThatThrownBy(() -> productService.create("ProductName", negativeProductPrice, existingId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("price must be positive");

		verify(productRepository, never()).create(any(Product.class));
	}

	@Test
	public void test_createNewProductWithZeroPrice_ShouldBeCreated() {
		when(productRepository.create(any(Product.class))).thenReturn(1L);

		Long createdProduct = productService.create("Product Name", zeroPrice, existingId);

		assertThat(createdProduct).isEqualTo(1L);
		verify(transactionManager).doInTransaction(any());
		verify(productRepository).create(any(Product.class));

	}

	@Test
	public void test_createNewProductWithExistingName_shouldThrowProductNameAlreadyExistsExcpetion() {
		String normalizedName = "new name";

		when(productRepository.findByName(normalizedName))
				.thenReturn(new Product(normalizedName, productPrice, newCategory));

		assertThatThrownBy(() -> productService.create(newName, productPrice, existingId))
				.isInstanceOf(ProductNameAlreadyExistsExcpetion.class);

		verify(productRepository).findByName(normalizedName);
		verify(transactionManager).doInTransaction(any());
		verify(productRepository, never()).create(any(Product.class));
	}

	@Test
	public void test_createWithNonExistingCategory_ShouldThrowCategoryDoesNotExistException() {
		when(categoryService.findByIdInternal(nonExistingId))
				.thenThrow(new EntityNotFoundException("category with id:" + nonExistingId + "not found"));

		assertThatThrownBy(() -> productService.create("name", productPrice, nonExistingId))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessageContaining("category with id:" + nonExistingId + "not found");

		verify(categoryService).findByIdInternal(nonExistingId);
		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository, never()).create(any(Product.class));

	}

	@Test
	public void test_createWithNegativeCategoryId_ShouldThrowIllegalArgumentExcpetion() {
		when(categoryService.findByIdInternal(negativeId))
				.thenThrow(new IllegalArgumentException("id must be positive"));

		assertThatThrownBy(() -> productService.create("name", productPrice, negativeId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("id must be positive");

		verify(categoryService).findByIdInternal(negativeId);
		verify(transactionManager).doInTransaction(any());
		verify(productRepository, never()).create(any(Product.class));
	}

	@Test
	public void test_findById_withExistingId_returnsProductEntity() {
		Product existingProduct = new Product("name", productPrice, newCategory);
		when(productRepository.findById(existingId)).thenReturn(existingProduct);

		Product response = productService.findById(existingId);

		assertThat(response).isEqualTo(existingProduct);
		verify(transactionManager).doInTransaction(any());
		verify(productRepository).findById(existingId);
	}

	@Test
	public void test_findById_withNonExistingId_throwsEntityNotFoundExcpetion() {
		when(productRepository.findById(nonExistingId)).thenReturn(null);

		assertThatThrownBy(() -> productService.findById(nonExistingId)).isInstanceOf(EntityNotFoundException.class)
				.hasMessage("product with id: " + nonExistingId + " not found");

		verify(productRepository).findById(nonExistingId);
	}

	@Test
	public void test_findById_withNullId_throwsEntityNotFoundExcpetion() {
		when(productRepository.findById(null)).thenReturn(null);

		assertThatThrownBy(() -> productService.findById(null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("id must be provided");

		verify(transactionManager, never()).doInTransaction(any());
		verify(productRepository, never()).findById(null);
	}

	@Test
	public void test_findById_withNegativeId_throwsIllegalArgumentException() {
		when(productRepository.findById(negativeId)).thenReturn(null);

		assertThatThrownBy(() -> productService.findById(negativeId)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("id must be positive");

		verify(productRepository, never()).findById(negativeId);
	}

	@Test
	public void test_findById_withZeroId_throwsIllegalArgumentException() {
		when(productRepository.findById(0L)).thenReturn(null);

		assertThatThrownBy(() -> productService.findById(0L)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("id must be positive");

		verify(transactionManager, never()).doInTransaction(any());
		verify(productRepository, never()).findById(0L);

	}

	@Test
	public void test_findAllWithElements_returnListOfProducts() {
		when(productRepository.findAll()).thenReturn(asList(new Product("Product 1", productPrice, newCategory),
				new Product("Product 2", productPrice, newCategory)));

		List<Product> result = productService.findAll();

		assertThat(result).hasSize(2);
		verify(transactionManager).doInTransaction(any());
		verify(productRepository).findAll();
	}

	@Test
	public void test_findAllWithNoElements_returnEmptyListOfProducts() {
		when(productRepository.findAll()).thenReturn(new ArrayList<Product>());

		List<Product> result = productService.findAll();

		assertThat(result).isEqualTo(new ArrayList<Product>());
		assertThat(result).isEmpty();

		verify(transactionManager).doInTransaction(any());
		verify(productRepository).findAll();
	}

	@Test
	public void test_updateWithValidRequestBody_returnNewUpdatedProduct() {
		String normalized = "new name";
		Product existingProduct = new Product("oldName", new BigDecimal("9.99"), new Category("old category"));

		when(productRepository.findById(existingId)).thenReturn(existingProduct);
		when(categoryService.findByIdInternal(existingId)).thenReturn(newCategory);
		when(productRepository.findByName(normalized)).thenReturn(null);
		when(productRepository.update(any(Product.class)))
				.thenReturn(new Product(normalized, productPrice, newCategory));

		Product updatedProduct = productService.update(existingId, newName, productPrice, existingId);

		assertThat(updatedProduct.getName()).isEqualTo(normalized);
		assertThat(existingProduct.getName()).isEqualTo(normalized);
		assertThat(existingProduct.getPrice()).isEqualTo(productPrice);
		assertThat(existingProduct.getCategory()).isEqualTo(newCategory);

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository).update(any(Product.class));
	}

	@Test
	public void test_updateWithPriceIsZero_returnNewUpdatedProduct() {
		String normalized = "new name";

		when(productRepository.findById(existingId)).thenReturn(new Product("oldName", productPrice, newCategory));
		when(productRepository.findByName(normalized)).thenReturn(null);
		when(categoryService.findById(existingId)).thenReturn(newCategory);
		when(productRepository.update(any(Product.class))).thenReturn(new Product(normalized, zeroPrice, newCategory));

		Product updatedProduct = productService.update(existingId, newName, zeroPrice, existingId);

		assertThat(updatedProduct.getName()).isEqualTo(normalized);
		assertThat(updatedProduct.getPrice()).isEqualTo(zeroPrice);

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository).update(any(Product.class));
	}

	@Test
	public void test_updateWithIdThatDoesntExist_shouldThrowEntityNotFoundExcpetion() {
		when(productRepository.findById(nonExistingId)).thenReturn(null);

		assertThatThrownBy(() -> productService.update(nonExistingId, newName, productPrice, existingId))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("product with id: " + nonExistingId + " not found");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository).findById(nonExistingId);
	}

	@Test
	public void test_updateWithNullId_shouldIllegalArgumentExcpetion() {
		when(productRepository.findById(null)).thenReturn(null);

		assertThatThrownBy(() -> productService.update(null, newName, productPrice, existingId))
				.isInstanceOf(IllegalArgumentException.class).hasMessage("id must be provided");

		verify(productRepository, never()).findById(null);
	}

	@Test
	public void test_updateWithNegativeId_shouldIllegalArgumentExcpetion() {
		when(productRepository.findById(negativeId)).thenReturn(null);

		assertThatThrownBy(() -> productService.update(negativeId, newName, productPrice, existingId))
				.isInstanceOf(IllegalArgumentException.class).hasMessage("id must be positive");

		verify(transactionManager).doInTransaction(any());
		verify(productRepository, never()).findById(negativeId);
	}

	@Test
	public void test_whenUpdatingProductWithAnExistingName_shouldThrowProductNameAlreadyExistsExcpetion() {
		Product existingProduct = new Product("oldName", productPrice, newCategory);
		Product duplicateProduct = new Product(normalizedName, productPrice, newCategory);

		when(productRepository.findById(existingId)).thenReturn(existingProduct);
		when(categoryService.findById(existingId)).thenReturn(newCategory);
		when(productRepository.findByName(normalizedName)).thenReturn(duplicateProduct);

		assertThatThrownBy(() -> productService.update(existingId, newName, productPrice, existingId))
				.isInstanceOf(ProductNameAlreadyExistsExcpetion.class);

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository).findById(existingId);
		verify(categoryService).findByIdInternal(existingId);
		verify(productRepository).findByName(normalizedName);
		verify(productRepository, never()).update(any(Product.class));
	}

	@Test
	public void test_whenUpdatingProductWithNullName_shouldThrowIllegalArgumentExcpetion() {
		when(productRepository.findById(existingId)).thenReturn(new Product(newName, productPrice, newCategory));

		assertThatThrownBy(() -> productService.update(existingId, null, productPrice, existingId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be provided");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository).findById(existingId);
		verify(productRepository, never()).findByName(newName);
		verify(productRepository, never()).update(any(Product.class));
	}

	@Test
	public void test_whenUpdatingProductWithBlankName_shouldThrowIllegalArgumentExcpetion() {
		when(productRepository.findById(existingId)).thenReturn(new Product(newName, productPrice, newCategory));

		assertThatThrownBy(() -> productService.update(existingId, " \t \t ", productPrice, existingId))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must contain valid characters");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository).findById(existingId);
		verify(productRepository, never()).findByName(newName);
		verify(productRepository, never()).update(any(Product.class));
	}

	@Test
	public void test_whenUpdatingProductWithWhiteSpaces_shouldBeNormalizedAndUpdated() {
		when(productRepository.findById(existingId)).thenReturn(new Product(existingName, productPrice, newCategory));
		when(productRepository.findByName(normalizedName)).thenReturn(null);
		when(productRepository.update(any(Product.class)))
				.thenReturn(new Product(normalizedName, productPrice, newCategory));

		Product result = productService.update(existingId, newName, productPrice, existingId);

		assertThat(result.getName()).isEqualTo(normalizedName);

		InOrder inOrder = Mockito.inOrder(productRepository, categoryService, transactionManager);
		inOrder.verify(transactionManager, times(1)).doInTransaction(any());
		inOrder.verify(productRepository).findById(existingId);
		inOrder.verify(productRepository).findByName(normalizedName);
		inOrder.verify(productRepository).update(any(Product.class));

	}

	@Test
	public void test_whenUpdatingWithNonExistingCategory_shouldThrowCategoryDoesNotExistException() {
		when(productRepository.findById(existingId)).thenReturn(new Product(existingName, productPrice, newCategory));
		when(categoryService.findByIdInternal(nonExistingId))
				.thenThrow(new EntityNotFoundException("category with id:" + nonExistingId + "not found"));

		assertThatThrownBy(() -> productService.update(existingId, newName, productPrice, nonExistingId))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessageContaining("category with id:" + nonExistingId + "not found");

	}

	@Test
	public void test_whenUpdatingWithNegativeCategoryId_shouldThrowIllegalArgumentExcpetion() {
		when(productRepository.findById(existingId)).thenReturn(new Product(existingName, productPrice, newCategory));
		when(categoryService.findByIdInternal(negativeId))
				.thenThrow(new IllegalArgumentException("id must be positive"));

		assertThatThrownBy(() -> productService.update(existingId, newName, productPrice, negativeId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("id must be positive");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(categoryService).findByIdInternal(negativeId);
		verify(productRepository, never()).update(any(Product.class));
	}

	@Test
	public void test_whenUpdatingWithPriceNull_shouldThrowIllegalArgumentExcpetion() {
		when(productRepository.findById(existingId)).thenReturn(new Product(existingName, productPrice, newCategory));

		assertThatThrownBy(() -> productService.update(existingId, newName, null, negativeId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("price must be provided");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository, never()).update(any(Product.class));
	}

	@Test
	public void test_whenUpdatingWithNegativePrice_shouldThrowIllegalArgumentExcpetion() {
		when(productRepository.findById(existingId)).thenReturn(new Product(existingName, productPrice, newCategory));

		assertThatThrownBy(() -> productService.update(existingId, newName, negativeProductPrice, negativeId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("price must be positive");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository, never()).update(any(Product.class));
	}

	@Test
	public void test_deleteWhenIdExists_shouldReturnVoid() {
		Product existingProduct = new Product(existingName, productPrice, newCategory);

		when(productRepository.findById(existingId)).thenReturn(existingProduct);
		doNothing().when(productRepository).delete(existingProduct);

		productService.delete(existingId);

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository, times(1)).delete(existingProduct);
	}

	@Test
	public void test_deleteWhenProductIdDoenstExist_shouldThrowEntityNotFoundExcpetion() {
		when(productRepository.findById(nonExistingId)).thenReturn(null);

		assertThatThrownBy(() -> productService.delete(nonExistingId)).isInstanceOf(EntityNotFoundException.class)
				.hasMessageContaining("product with id: " + nonExistingId + " not found");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository, never()).delete(null);
	}

}
