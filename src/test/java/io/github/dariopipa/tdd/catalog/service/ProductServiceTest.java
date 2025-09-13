package io.github.dariopipa.tdd.catalog.service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import io.github.dariopipa.tdd.catalog.entities.Product;
import io.github.dariopipa.tdd.catalog.exceptions.EntityNotFoundException;
import io.github.dariopipa.tdd.catalog.exceptions.ProductNameAlreadyExistsExcpetion;

public class ProductServiceTest {

	private ProductService productService;
	private ProductRepository productRepository;
	private CategoryService categoryService;

	private BigDecimal productPrice = new BigDecimal(111.11);
	private BigDecimal negativeProductPrice = new BigDecimal(-111111);
	private Long existingId = 1L;
	private Long nonExistingId = 999L;
	private Long negativeId = -1L;
	private String existingName = " EXISTING NAME ";
	private String newName = " new name ";
	private String normalizedName = "new name";

	@Before
	public void setup() {
		this.productRepository = mock(ProductRepository.class);
		this.categoryService = mock(CategoryService.class);
		this.productService = new ProductService(productRepository, categoryService);
	}

	@Test
	public void test_whenCreatingNewProductWithValidRequestBody_ReturnCreatedId() {
		when(productRepository.create(any(Product.class))).thenReturn(1L);

		Long createdProduct = productService.create("Product Name", productPrice, existingId);

		assertThat(createdProduct).isEqualTo(1L);
		verify(productRepository).create(any(Product.class));
	}

	@Test
	public void test_createNewProductWithMissingName_ShouldThrowExcpetion() {
		assertThatThrownBy(() -> productService.create(null, productPrice, existingId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be provided");

		verify(productRepository, never()).create(any(Product.class));
	}

	@Test
	public void test_createNewProductWithBlankName_ShouldThrowExcpetion() {
		assertThatThrownBy(() -> productService.create("\t\t\t \n   ", productPrice, existingId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be provided");

		verify(productRepository, never()).create(any(Product.class));
	}

	@Test
	public void test_createNewProductWithBlankPrice_ShouldThrowExcpetion() {
		assertThatThrownBy(() -> productService.create("ProductName", null, existingId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("price must be added");

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

		Long createdProduct = productService.create("Product Name", BigDecimal.ZERO, existingId);

		assertThat(createdProduct).isEqualTo(1L);
		verify(productRepository).create(new Product("Product Name", BigDecimal.ZERO, 1L));
	}

	@Test
	public void test_createNewProductWithExistingName_shouldThrowProductNameAlreadyExistsExcpetion() {
		String normalizedName = "new name";
		when(productRepository.findByName(normalizedName))
				.thenReturn(new Product(normalizedName, productPrice, existingId));

		assertThatThrownBy(() -> productService.create(newName, productPrice, existingId))
				.isInstanceOf(ProductNameAlreadyExistsExcpetion.class);

		verify(productRepository).findByName(normalizedName);
		verify(productRepository, never()).create(any(Product.class));
	}

	@Test
	public void test_createWithNonExistingCategory_ShouldThrowCategoryDoesNotExistException() {
		when(categoryService.findById(nonExistingId))
				.thenThrow(new EntityNotFoundException("category with id:" + nonExistingId + "not found"));

		assertThatThrownBy(() -> productService.create("name", productPrice, nonExistingId))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessageContaining("category with id:" + nonExistingId + "not found");

		verify(categoryService).findById(nonExistingId);
		verify(productRepository, never()).create(any(Product.class));

	}

	@Test
	public void test_createWithNegativeCategoryId_ShouldThrowIllegalArgumentExcpetion() {
		assertThatThrownBy(() -> productService.create("name", productPrice, negativeId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("categoryId must be positive");

		verify(productRepository, never()).create(any(Product.class));

	}

	@Test
	public void test_findById_withExistingId_returnsProductEntity() {
		when(productRepository.findById(existingId)).thenReturn(new Product("name", productPrice, existingId));

		Product response = productService.findById(existingId);

		assertThat(response).isEqualTo(new Product("name", productPrice, existingId));
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

		verify(productRepository, never()).findById(null);
	}

	@Test
	public void test_findById_withNegativeId_throwsEntityNotFoundExcpetion() {
		when(productRepository.findById(negativeId)).thenReturn(null);

		assertThatThrownBy(() -> productService.findById(negativeId)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("id must be positive");

		verify(productRepository, never()).findById(negativeId);
	}

	@Test
	public void test_findAllWithElements_returnListOfProducts() {
		when(productRepository.findAll()).thenReturn(asList(new Product("Product 1", productPrice, existingId),
				new Product("Product 2", productPrice, existingId)));

		List<Product> result = productService.findAll();

		assertThat(result).hasSize(2);
		verify(productRepository).findAll();
	}

	@Test
	public void test_findAllWithNoElements_returnEmptyListOfProducts() {
		when(productRepository.findAll()).thenReturn(new ArrayList<Product>());

		List<Product> result = productService.findAll();

		assertThat(result).isEqualTo(new ArrayList<Product>());
		assertThat(result).isEmpty();

		verify(productRepository).findAll();
	}

	@Test
	public void test_updateWithIdThatExists_returnNewUpdatedProduct() {
		String normalized = "new name";

		when(productRepository.findById(existingId)).thenReturn(new Product("oldName", productPrice, existingId));
		when(productRepository.findByName(normalized)).thenReturn(null);
		when(productRepository.update(existingId, normalized, productPrice, existingId))
				.thenReturn(new Product(normalized, productPrice, existingId));

		Product updatedProduct = productService.update(existingId, newName, productPrice, existingId);

		assertThat(updatedProduct.getName()).isEqualTo(normalized);
		verify(productRepository).update(existingId, normalized, productPrice, existingId);

	}

	@Test
	public void test_updateWithIdThatDoesntExist_shouldThrowEntityNotFoundExcpetion() {
		when(productRepository.findById(existingId)).thenReturn(null);

		assertThatThrownBy(() -> productService.update(nonExistingId, newName, productPrice, existingId))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("product with id: " + nonExistingId + " not found");
		;

		verify(productRepository).findById(nonExistingId);
	}

	@Test
	public void test_updateWithNullId_shouldIllegalArgumentExcpetion() {
		when(productRepository.findById(null)).thenReturn(null);

		assertThatThrownBy(() -> productService.update(null, newName, productPrice, existingId))
				.isInstanceOf(IllegalArgumentException.class).hasMessage("id must be provided");
		;

		verify(productRepository, never()).findById(null);
	}

	@Test
	public void test_updateWithNegativeId_shouldIllegalArgumentExcpetion() {
		when(productRepository.findById(negativeId)).thenReturn(null);

		assertThatThrownBy(() -> productService.update(negativeId, newName, productPrice, existingId))
				.isInstanceOf(IllegalArgumentException.class).hasMessage("id must be positive");
		;

		verify(productRepository, never()).findById(null);
	}

	@Test
	public void test_whenUpdatingProductWithAnExistingName_shouldThrowProductNameAlreadyExistsExcpetion() {
		when(productRepository.findById(existingId)).thenReturn(new Product("oldName", productPrice, existingId));
		when(productRepository.findByName(normalizedName)).thenReturn(null);
		when(productRepository.update(existingId, normalizedName, productPrice, existingId))
				.thenReturn(new Product(normalizedName, productPrice, existingId));

		Product updatedProduct = productService.update(existingId, newName, productPrice, existingId);

		assertThat(updatedProduct.getName()).isEqualTo(normalizedName);
		verify(productRepository).update(existingId, normalizedName, productPrice, existingId);
	}

	@Test
	public void test_whenUpdatingProductWithNullName_shouldThrowIllegalArgumentExcpetion() {
		when(productRepository.findById(existingId)).thenReturn(new Product(newName, productPrice, existingId));

		assertThatThrownBy(() -> productService.update(existingId, null, productPrice, existingId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be provided");

		verify(productRepository).findById(existingId);
		verify(productRepository, never()).findByName(newName);
		verify(productRepository, never()).update(existingId, newName, productPrice, existingId);
	}

	@Test
	public void test_whenUpdatingProductWithBlankName_shouldThrowIllegalArgumentExcpetion() {
		when(productRepository.findById(existingId)).thenReturn(new Product(newName, productPrice, existingId));

		assertThatThrownBy(() -> productService.update(existingId, " \t \t ", productPrice, existingId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be provided");

		verify(productRepository).findById(existingId);
		verify(productRepository, never()).findByName(newName);
		verify(productRepository, never()).update(existingId, newName, productPrice, existingId);
	}

	@Test
	public void test_whenUpdatingProductWithWhiteSpaces_shouldBeNormalizedAndUpdated() {
		when(productRepository.findById(existingId)).thenReturn(new Product(existingName, productPrice, existingId));

		when(productRepository.findByName(normalizedName)).thenReturn(null);
		when(productRepository.update(existingId, normalizedName, productPrice, existingId))
				.thenReturn(new Product(normalizedName, productPrice, existingId));
		Product result = productService.update(existingId, newName, productPrice, existingId);

		assertThat(result.getName()).isEqualTo(normalizedName);

		InOrder inOrder = Mockito.inOrder(productRepository);
		inOrder.verify(productRepository).findById(existingId);
		inOrder.verify(productRepository).findByName(normalizedName);
		inOrder.verify(productRepository).update(existingId, normalizedName, productPrice, existingId);
	}

	@Test
	public void test_whenUpdatingWithNonExistingCategory_shouldThrowCategoryDoesNotExistException() {
		when(productRepository.findById(existingId)).thenReturn(new Product(existingName, productPrice, existingId));
		when(categoryService.findById(nonExistingId))
				.thenThrow(new EntityNotFoundException("category with id:" + nonExistingId + "not found"));

		assertThatThrownBy(() -> productService.update(existingId, newName, productPrice, nonExistingId))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessageContaining("category with id:" + nonExistingId + "not found");

	}

	@Test
	public void test_whenUpdatingWithNegativeCategoryId_shouldThrowIllegalArgumentExcpetion() {
		when(productRepository.findById(existingId)).thenReturn(new Product(existingName, productPrice, existingId));
		when(categoryService.findById(negativeId)).thenThrow(new IllegalArgumentException("id must be positive"));

		assertThatThrownBy(() -> productService.update(existingId, newName, productPrice, negativeId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("id must be positive");

		verify(categoryService).findById(negativeId);
		verify(productRepository, never()).update(existingId, existingName, productPrice, existingId);
	}

	@Test
	public void test_whenUpdatingWithPriceNull_shouldThrowIllegalArgumentExcpetion() {
		when(productRepository.findById(existingId)).thenReturn(new Product(existingName, productPrice, existingId));

		assertThatThrownBy(() -> productService.update(existingId, newName, null, negativeId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("price must be provided");

		verify(productRepository, never()).update(existingId, existingName, negativeProductPrice, existingId);
	}

	@Test
	public void test_whenUpdatingWithNegativePrice_shouldThrowIllegalArgumentExcpetion() {
		when(productRepository.findById(existingId)).thenReturn(new Product(existingName, productPrice, existingId));

		assertThatThrownBy(() -> productService.update(existingId, newName, negativeProductPrice, negativeId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("price must be positive");

		verify(productRepository, never()).update(existingId, existingName, negativeProductPrice, existingId);
	}

	@Test
	public void test_deleteWhenIdExists_shouldReturnVoid() {
		when(productRepository.findById(existingId)).thenReturn(new Product(existingName, productPrice, existingId));
		doNothing().when(productRepository).delete(existingId);

		productService.delete(existingId);

		verify(productRepository, times(1)).delete(existingId);
	}

	@Test
	public void test_deleteWhenProductIdDoenstExist_shouldThrowEntityNotFoundExcpetion() {
		when(productRepository.findById(nonExistingId)).thenReturn(null);

		assertThatThrownBy(() -> productService.delete(nonExistingId)).isInstanceOf(EntityNotFoundException.class)
				.hasMessageContaining("product with id: " + nonExistingId + " not found");

		verify(productRepository, never()).delete(nonExistingId);
	}

}
