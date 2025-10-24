package io.github.dariopipa.tdd.catalog.service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.entities.Product;
import io.github.dariopipa.tdd.catalog.exceptions.EntityNotFoundException;
import io.github.dariopipa.tdd.catalog.exceptions.ProductNameAlreadyExistsExcpetion;
import io.github.dariopipa.tdd.catalog.repository.ProductRepository;
import io.github.dariopipa.tdd.catalog.transactionmanager.TransactionCode;
import io.github.dariopipa.tdd.catalog.transactionmanager.TransactionManager;

public class ProductServiceTest {

	private ProductService productService;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private CategoryService categoryService;

	@Mock
	private TransactionManager transactionManager;

	private AutoCloseable closeable;

	private static final Long EXISTING_ID = 1L;
	private static final Long MISSING_ID = 999L;
	private static final Long NEGATIVE_ID = -1L;

	private static final String PRODUCT_NAME_RAW = " new name ";
	private static final String PRODUCT_NAME_NORMALIZED = "new name";
	private static final String CATEGORY_NAME = "category name";

	private static final BigDecimal PRODUCT_PRICE = new BigDecimal("111.11");
	private static final BigDecimal NEGATIVE_PRODUCT_PRICE = new BigDecimal("-111111");
	private static final BigDecimal ZERO_PRICE = BigDecimal.ZERO;

	private static final String BLANK_TABS_NAME = "\t\t\t \n   ";
	private static final String BLANK_NAME = " \t \t ";

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);

		when(transactionManager.doInTransaction(any())).thenAnswer(answer((TransactionCode<?> code) -> code.execute()));

		this.productService = new ProductService(productRepository, categoryService, transactionManager);
	}

	@After
	public void tearDown() throws Exception {
		closeable.close();
	}

	@Test
	public void test_whenCreatingNewProductWithValidRequestBody_ReturnCreatedId() {
		Category category = new Category("Electronics");

		when(categoryService.findById(EXISTING_ID)).thenReturn(category);
		when(productRepository.findByName(PRODUCT_NAME_NORMALIZED)).thenReturn(null);
		when(productRepository.create(any(Product.class))).thenReturn(1L);

		Long createdProduct = productService.create(PRODUCT_NAME_RAW, PRODUCT_PRICE, EXISTING_ID);

		assertThat(createdProduct).isEqualTo(1L);
		verify(categoryService).findByIdInternal(EXISTING_ID);
		verify(productRepository).create(any(Product.class));
		verify(transactionManager).doInTransaction(any());
	}

	@Test
	public void test_createNewProductWithMissingName_ShouldThrowExcpetion() {
		assertThatThrownBy(() -> productService.create(null, PRODUCT_PRICE, EXISTING_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be provided");

		verify(productRepository, never()).create(any(Product.class));
		verify(transactionManager).doInTransaction(any());
	}

	@Test
	public void test_createNewProductWithBlankName_ShouldThrowExcpetion() {
		assertThatThrownBy(() -> productService.create(BLANK_TABS_NAME, PRODUCT_PRICE, EXISTING_ID))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must contain valid characters");

		verify(productRepository, never()).create(any(Product.class));
	}

	@Test
	public void test_createNewProductWithBlankPrice_ShouldThrowExcpetion() {
		assertThatThrownBy(() -> productService.create(PRODUCT_NAME_RAW, null, EXISTING_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("price must be provided");

		verify(productRepository, never()).create(any(Product.class));
	}

	@Test
	public void test_createNewProductWithNegativePrice_ShouldThrowExcpetion() {
		assertThatThrownBy(() -> productService.create(PRODUCT_NAME_RAW, NEGATIVE_PRODUCT_PRICE, EXISTING_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("price must be positive");

		verify(productRepository, never()).create(any(Product.class));
	}

	@Test
	public void test_createNewProductWithZeroPrice_ShouldBeCreated() {
		when(productRepository.create(any(Product.class))).thenReturn(1L);

		Long createdProduct = productService.create(PRODUCT_NAME_RAW, ZERO_PRICE, EXISTING_ID);

		assertThat(createdProduct).isEqualTo(1L);
		verify(transactionManager).doInTransaction(any());
		verify(productRepository).create(any(Product.class));

	}

	@Test
	public void test_createNewProductWithExistingName_shouldThrowProductNameAlreadyExistsExcpetion() {
		when(productRepository.findByName(PRODUCT_NAME_NORMALIZED))
				.thenReturn(new Product(PRODUCT_NAME_NORMALIZED, PRODUCT_PRICE, new Category(CATEGORY_NAME)));

		assertThatThrownBy(() -> productService.create(PRODUCT_NAME_RAW, PRODUCT_PRICE, EXISTING_ID))
				.isInstanceOf(ProductNameAlreadyExistsExcpetion.class);

		verify(productRepository).findByName(PRODUCT_NAME_NORMALIZED);
		verify(transactionManager).doInTransaction(any());
		verify(productRepository, never()).create(any(Product.class));
	}

	@Test
	public void test_createWithNonExistingCategory_ShouldThrowCategoryDoesNotExistException() {
		when(categoryService.findByIdInternal(MISSING_ID))
				.thenThrow(new EntityNotFoundException("category with id:" + MISSING_ID + "not found"));

		assertThatThrownBy(() -> productService.create(PRODUCT_NAME_RAW, PRODUCT_PRICE, MISSING_ID))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessageContaining("category with id:" + MISSING_ID + "not found");

		verify(categoryService).findByIdInternal(MISSING_ID);
		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository, never()).create(any(Product.class));

	}

	@Test
	public void test_createWithNegativeCategoryId_ShouldThrowIllegalArgumentExcpetion() {
		when(categoryService.findByIdInternal(NEGATIVE_ID))
				.thenThrow(new IllegalArgumentException("id must be positive"));

		assertThatThrownBy(() -> productService.create(PRODUCT_NAME_RAW, PRODUCT_PRICE, NEGATIVE_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("id must be positive");

		verify(categoryService).findByIdInternal(NEGATIVE_ID);
		verify(transactionManager).doInTransaction(any());
		verify(productRepository, never()).create(any(Product.class));
	}

	@Test
	public void test_findById_withExistingId_returnsProductEntity() {
		Product existingProduct = new Product(PRODUCT_NAME_RAW, PRODUCT_PRICE, new Category(CATEGORY_NAME));
		when(productRepository.findById(EXISTING_ID)).thenReturn(existingProduct);

		Product response = productService.findById(EXISTING_ID);

		assertThat(response).isEqualTo(existingProduct);
		verify(transactionManager).doInTransaction(any());
		verify(productRepository).findById(EXISTING_ID);
	}

	@Test
	public void test_findById_withNonExistingId_throwsEntityNotFoundExcpetion() {
		when(productRepository.findById(MISSING_ID)).thenReturn(null);

		assertThatThrownBy(() -> productService.findById(MISSING_ID)).isInstanceOf(EntityNotFoundException.class)
				.hasMessage("product with id: " + MISSING_ID + " not found");

		verify(productRepository).findById(MISSING_ID);
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
		when(productRepository.findById(NEGATIVE_ID)).thenReturn(null);

		assertThatThrownBy(() -> productService.findById(NEGATIVE_ID)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("id must be positive");

		verify(productRepository, never()).findById(NEGATIVE_ID);
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
		when(productRepository.findAll())
				.thenReturn(asList(new Product(PRODUCT_NAME_RAW, PRODUCT_PRICE, new Category(CATEGORY_NAME)),
						new Product("Product 2", PRODUCT_PRICE, new Category(CATEGORY_NAME))));

		List<Product> result = productService.findAll();

		assertThat(result).hasSize(2);
		verify(transactionManager).doInTransaction(any());
		verify(productRepository).findAll();
	}

	@Test
	public void test_findAllWithNoElements_returnEmptyListOfProducts() {
		when(productRepository.findAll()).thenReturn(new ArrayList<Product>());

		List<Product> result = productService.findAll();

		assertThat(result).isEmpty();

		verify(transactionManager).doInTransaction(any());
		verify(productRepository).findAll();
	}

	@Test
	public void test_updateWithValidRequestBody_returnNewUpdatedProduct() {
		Product existingProduct = new Product("oldName", new BigDecimal("9.99"), new Category("old category"));
		Category existingCategory = new Category(CATEGORY_NAME);

		when(productRepository.findById(EXISTING_ID)).thenReturn(existingProduct);
		when(categoryService.findByIdInternal(EXISTING_ID)).thenReturn(existingCategory);
		when(productRepository.findByName(PRODUCT_NAME_NORMALIZED)).thenReturn(null);
		when(productRepository.update(any(Product.class)))
				.thenReturn(new Product(PRODUCT_NAME_NORMALIZED, PRODUCT_PRICE, existingCategory));

		Product updatedProduct = productService.update(EXISTING_ID, PRODUCT_NAME_RAW, PRODUCT_PRICE, EXISTING_ID);

		assertThat(updatedProduct.getName()).isEqualTo(PRODUCT_NAME_NORMALIZED);
		assertThat(existingProduct.getName()).isEqualTo(PRODUCT_NAME_NORMALIZED);
		assertThat(existingProduct.getPrice()).isEqualTo(PRODUCT_PRICE);
		assertThat(existingProduct.getCategory()).isEqualTo(existingCategory);

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository).update(any(Product.class));
	}

	@Test
	public void test_updateWithPriceIsZero_returnNewUpdatedProduct() {
		when(productRepository.findById(EXISTING_ID))
				.thenReturn(new Product("oldName", PRODUCT_PRICE, new Category(CATEGORY_NAME)));
		when(productRepository.findByName(PRODUCT_NAME_NORMALIZED)).thenReturn(null);
		when(categoryService.findById(EXISTING_ID)).thenReturn(new Category(CATEGORY_NAME));
		when(productRepository.update(any(Product.class)))
				.thenReturn(new Product(PRODUCT_NAME_NORMALIZED, ZERO_PRICE, new Category(CATEGORY_NAME)));

		Product updatedProduct = productService.update(EXISTING_ID, PRODUCT_NAME_RAW, ZERO_PRICE, EXISTING_ID);

		assertThat(updatedProduct.getName()).isEqualTo(PRODUCT_NAME_NORMALIZED);
		assertThat(updatedProduct.getPrice()).isEqualTo(ZERO_PRICE);

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository).update(any(Product.class));
	}

	@Test
	public void test_updateWithIdThatDoesntExist_shouldThrowEntityNotFoundExcpetion() {
		when(productRepository.findById(MISSING_ID)).thenReturn(null);

		assertThatThrownBy(() -> productService.update(MISSING_ID, PRODUCT_NAME_RAW, PRODUCT_PRICE, EXISTING_ID))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("product with id: " + MISSING_ID + " not found");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository).findById(MISSING_ID);
	}

	@Test
	public void test_updateWithNullId_shouldIllegalArgumentExcpetion() {
		when(productRepository.findById(null)).thenReturn(null);

		assertThatThrownBy(() -> productService.update(null, PRODUCT_NAME_RAW, PRODUCT_PRICE, EXISTING_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessage("id must be provided");

		verify(productRepository, never()).findById(null);
	}

	@Test
	public void test_updateWithNegativeId_shouldIllegalArgumentExcpetion() {
		when(productRepository.findById(NEGATIVE_ID)).thenReturn(null);

		assertThatThrownBy(() -> productService.update(NEGATIVE_ID, PRODUCT_NAME_RAW, PRODUCT_PRICE, EXISTING_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessage("id must be positive");

		verify(transactionManager).doInTransaction(any());
		verify(productRepository, never()).findById(NEGATIVE_ID);
	}

	@Test
	public void test_whenUpdatingProductWithAnExistingName_shouldThrowProductNameAlreadyExistsExcpetion() {
		Product existingProduct = new Product("oldName", PRODUCT_PRICE, new Category(CATEGORY_NAME));
		Product duplicateProduct = new Product(PRODUCT_NAME_NORMALIZED, PRODUCT_PRICE, new Category(CATEGORY_NAME));

		when(productRepository.findById(EXISTING_ID)).thenReturn(existingProduct);
		when(categoryService.findById(EXISTING_ID)).thenReturn(new Category(CATEGORY_NAME));
		when(productRepository.findByName(PRODUCT_NAME_NORMALIZED)).thenReturn(duplicateProduct);

		assertThatThrownBy(() -> productService.update(EXISTING_ID, PRODUCT_NAME_RAW, PRODUCT_PRICE, EXISTING_ID))
				.isInstanceOf(ProductNameAlreadyExistsExcpetion.class);

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository).findById(EXISTING_ID);
		verify(categoryService).findByIdInternal(EXISTING_ID);
		verify(productRepository).findByName(PRODUCT_NAME_NORMALIZED);
		verify(productRepository, never()).update(any(Product.class));
	}

	@Test
	public void test_whenUpdatingProductWithNullName_shouldThrowIllegalArgumentExcpetion() {
		when(productRepository.findById(EXISTING_ID))
				.thenReturn(new Product(PRODUCT_NAME_RAW, PRODUCT_PRICE, new Category(CATEGORY_NAME)));

		assertThatThrownBy(() -> productService.update(EXISTING_ID, null, PRODUCT_PRICE, EXISTING_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be provided");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository).findById(EXISTING_ID);
		verify(productRepository, never()).findByName(PRODUCT_NAME_RAW);
		verify(productRepository, never()).update(any(Product.class));
	}

	@Test
	public void test_whenUpdatingProductWithBlankName_shouldThrowIllegalArgumentExcpetion() {
		when(productRepository.findById(EXISTING_ID))
				.thenReturn(new Product(PRODUCT_NAME_RAW, PRODUCT_PRICE, new Category(CATEGORY_NAME)));

		assertThatThrownBy(() -> productService.update(EXISTING_ID, BLANK_NAME, PRODUCT_PRICE, EXISTING_ID))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must contain valid characters");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository).findById(EXISTING_ID);
		verify(productRepository, never()).findByName(PRODUCT_NAME_RAW);
		verify(productRepository, never()).update(any(Product.class));
	}

	@Test
	public void test_whenUpdatingProductWithWhiteSpaces_shouldBeNormalizedAndUpdated() {
		when(productRepository.findById(EXISTING_ID))
				.thenReturn(new Product(" EXISTING NAME ", PRODUCT_PRICE, new Category(CATEGORY_NAME)));
		when(productRepository.findByName(PRODUCT_NAME_NORMALIZED)).thenReturn(null);
		when(productRepository.update(any(Product.class)))
				.thenReturn(new Product(PRODUCT_NAME_NORMALIZED, PRODUCT_PRICE, new Category(CATEGORY_NAME)));

		Product result = productService.update(EXISTING_ID, PRODUCT_NAME_RAW, PRODUCT_PRICE, EXISTING_ID);

		assertThat(result.getName()).isEqualTo(PRODUCT_NAME_NORMALIZED);

		InOrder inOrder = Mockito.inOrder(productRepository, categoryService, transactionManager);
		inOrder.verify(transactionManager, times(1)).doInTransaction(any());
		inOrder.verify(productRepository).findById(EXISTING_ID);
		inOrder.verify(productRepository).findByName(PRODUCT_NAME_NORMALIZED);
		inOrder.verify(productRepository).update(any(Product.class));

	}

	@Test
	public void test_whenUpdatingWithNonExistingCategory_shouldThrowCategoryDoesNotExistException() {
		when(productRepository.findById(EXISTING_ID))
				.thenReturn(new Product(" EXISTING NAME ", PRODUCT_PRICE, new Category(CATEGORY_NAME)));
		when(categoryService.findByIdInternal(MISSING_ID))
				.thenThrow(new EntityNotFoundException("category with id:" + MISSING_ID + "not found"));

		assertThatThrownBy(() -> productService.update(EXISTING_ID, PRODUCT_NAME_RAW, PRODUCT_PRICE, MISSING_ID))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessageContaining("category with id:" + MISSING_ID + "not found");

	}

	@Test
	public void test_whenUpdatingWithNegativeCategoryId_shouldThrowIllegalArgumentExcpetion() {
		when(productRepository.findById(EXISTING_ID))
				.thenReturn(new Product(" EXISTING NAME ", PRODUCT_PRICE, new Category(CATEGORY_NAME)));
		when(categoryService.findByIdInternal(NEGATIVE_ID))
				.thenThrow(new IllegalArgumentException("id must be positive"));

		assertThatThrownBy(() -> productService.update(EXISTING_ID, PRODUCT_NAME_RAW, PRODUCT_PRICE, NEGATIVE_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("id must be positive");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(categoryService).findByIdInternal(NEGATIVE_ID);
		verify(productRepository, never()).update(any(Product.class));
	}

	@Test
	public void test_whenUpdatingWithPriceNull_shouldThrowIllegalArgumentExcpetion() {
		when(productRepository.findById(EXISTING_ID))
				.thenReturn(new Product(" EXISTING NAME ", PRODUCT_PRICE, new Category(CATEGORY_NAME)));

		assertThatThrownBy(() -> productService.update(EXISTING_ID, PRODUCT_NAME_RAW, null, NEGATIVE_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("price must be provided");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository, never()).update(any(Product.class));
	}

	@Test
	public void test_whenUpdatingWithNegativePrice_shouldThrowIllegalArgumentExcpetion() {
		when(productRepository.findById(EXISTING_ID))
				.thenReturn(new Product(" EXISTING NAME ", PRODUCT_PRICE, new Category(CATEGORY_NAME)));

		assertThatThrownBy(
				() -> productService.update(EXISTING_ID, PRODUCT_NAME_RAW, NEGATIVE_PRODUCT_PRICE, NEGATIVE_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("price must be positive");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository, never()).update(any(Product.class));
	}

	@Test
	public void test_deleteWhenIdExists_shouldReturnVoid() {
		Product existingProduct = new Product(" EXISTING NAME ", PRODUCT_PRICE, new Category(CATEGORY_NAME));

		when(productRepository.findById(EXISTING_ID)).thenReturn(existingProduct);
		doNothing().when(productRepository).delete(existingProduct);

		productService.delete(EXISTING_ID);

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository, times(1)).delete(existingProduct);
	}

	@Test
	public void test_deleteWhenProductIdDoenstExist_shouldThrowEntityNotFoundExcpetion() {
		when(productRepository.findById(MISSING_ID)).thenReturn(null);

		assertThatThrownBy(() -> productService.delete(MISSING_ID)).isInstanceOf(EntityNotFoundException.class)
				.hasMessageContaining("product with id: " + MISSING_ID + " not found");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(productRepository, never()).delete(null);
	}

}
