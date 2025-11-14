package io.github.dariopipa.tdd.catalog.service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import io.github.dariopipa.tdd.catalog.exceptions.CategoryInUseException;
import io.github.dariopipa.tdd.catalog.exceptions.CategoryNameAlreadyExistsExcpetion;
import io.github.dariopipa.tdd.catalog.exceptions.EntityNotFoundException;
import io.github.dariopipa.tdd.catalog.repository.CategoryRepository;
import io.github.dariopipa.tdd.catalog.repository.ProductRepository;
import io.github.dariopipa.tdd.catalog.transactionmanager.TransactionCode;
import io.github.dariopipa.tdd.catalog.transactionmanager.TransactionManager;

public class CategoryServiceTest {

	private CategoryService categoryService;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private TransactionManager<CategoryRepository> transactionManager;

	@Mock
	private ProductRepository productRepository;

	private AutoCloseable closeable;

	private static final long EXISTING_ID = 1L;
	private static final long MISSING_ID = 999L;

	private static final String CATEGORY_NAME_RAW = " new Name ";
	private static final String CATEGORY_NAME_NORMALIZED = "new name";

	private static final String BLANK_NAME = " ";
	private static final String BLANK_TABS_NAME = "\t\t\t    ";

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);

		when(transactionManager.doInTransaction(any())).thenAnswer(
				answer((TransactionCode<CategoryRepository, Object> code) -> code.apply(categoryRepository)));

		categoryService = new CategoryService(transactionManager);
	}

	@After
	public void tearDown() throws Exception {
		closeable.close();
	}

	@Test
	public void test_whenCreatingCategory_shouldSaveAndReturnLongId() {
		when(categoryRepository.create(any(Category.class))).thenReturn(EXISTING_ID);

		Long result = categoryService.createCategory(CATEGORY_NAME_RAW);

		assertThat(result).isEqualTo(EXISTING_ID);
		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository).create(any(Category.class));
	}

	@Test
	public void test_whenCategoryExists_shouldReturnCategoryEntity() {
		when(categoryRepository.create(any(Category.class))).thenReturn(EXISTING_ID);
		when(categoryRepository.findById(EXISTING_ID)).thenReturn(new Category(CATEGORY_NAME_NORMALIZED));

		Category category = categoryService.findById(EXISTING_ID);

		assertThat(category.getName()).isEqualTo(CATEGORY_NAME_NORMALIZED);
		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository).findById(EXISTING_ID);
	}

	@Test
	public void test_whenCategoryIdDoesntExist_shouldThrowEntityDoesNotExist() {
		when(categoryRepository.findById(MISSING_ID)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.findById(MISSING_ID)).isInstanceOf(EntityNotFoundException.class)
				.hasMessage("category with id:" + MISSING_ID + " not found");

		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository).findById(MISSING_ID);
	}

	@Test
	public void test_whenCategoryIdIsNull_shouldThrowIllegalArgumentExcpetion() {
		when(categoryRepository.findById(null)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.findById(null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("id must be provided");

		verify(transactionManager, never()).doInTransaction(any());
		verify(categoryRepository, never()).findById(null);
	}

	@Test
	public void test_whenCategoryIdIsNegative_shouldThrowIllegalArgumentExcpetion() {
		when(categoryRepository.findById(-1L)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.findById(-1L)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("id must be positive");

		verify(transactionManager, never()).doInTransaction(any());
		verify(categoryRepository, never()).findById(-1L);
	}

	@Test
	public void test_whenCategoryIdIsZero_shouldThrowIllegalArgumentExcpetion() {
		when(categoryRepository.findById(0L)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.findById(0L)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("id must be positive");

		verify(transactionManager, never()).doInTransaction(any());
		verify(categoryRepository, never()).findById(0L);
	}

	@Test
	public void test_whenCreatingEntityWithExistingName_shouldThrowCategoryNameAlreadyExistsExcpetion() {
		when(categoryRepository.findByName(CATEGORY_NAME_NORMALIZED))
				.thenReturn(new Category(CATEGORY_NAME_NORMALIZED));

		assertThatThrownBy(() -> categoryService.createCategory(CATEGORY_NAME_NORMALIZED))
				.isInstanceOf(CategoryNameAlreadyExistsExcpetion.class);

		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository).findByName(CATEGORY_NAME_NORMALIZED);
		verify(categoryRepository, never()).create(any(Category.class));
	}

	@Test
	public void test_whenCreatingEntity_withBlankName_shouldThrowIllegalArgumentExceptionWithNameMustBeValidMessage() {
		assertThatThrownBy(() -> categoryService.createCategory(BLANK_TABS_NAME))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be provided");

		verify(transactionManager, never()).doInTransaction(any());
		verify(categoryRepository, never()).create(any(Category.class));
	}

	@Test
	public void test_whenCreatingEntity_withEmptySpaces_shouldThrowIllegalArgumentExceptionWithNameMustBeValidMessage() {
		assertThatThrownBy(() -> categoryService.createCategory(BLANK_NAME))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be provided");

		verify(categoryRepository, never()).create(any(Category.class));
	}

	@Test
	public void test_whenCreatingEntity_withNonNormalizedName_shouldCreateEntityWithNormalizedName() {
		when(categoryRepository.findByName(CATEGORY_NAME_NORMALIZED)).thenReturn(null);
		when(categoryRepository.create(any(Category.class))).thenReturn(EXISTING_ID);

		Long result = categoryService.createCategory(CATEGORY_NAME_RAW);

		assertThat(result).isEqualTo(EXISTING_ID);
		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository).findByName(CATEGORY_NAME_NORMALIZED);
		verify(categoryRepository).create(any(Category.class));
	}

	@Test
	public void test_whenDeletingExistingCategory_shouldReturnDeletedMessage() {
		Category existingCategory = new Category(CATEGORY_NAME_NORMALIZED);

		when(categoryRepository.findById(EXISTING_ID)).thenReturn(existingCategory);
		when(categoryRepository.delete(existingCategory)).thenReturn("Deleted");

		String result = categoryService.delete(EXISTING_ID);

		assertThat(result).isEqualTo("Deleted");

		InOrder inOrder = Mockito.inOrder(categoryRepository, transactionManager);
		verify(transactionManager, times(1)).doInTransaction(any());
		inOrder.verify(categoryRepository).findById(EXISTING_ID);
		inOrder.verify(categoryRepository).delete(existingCategory);
	}

	@Test
	public void test_whenDeletingNonExistingCategory_throwEntityNotFoundException() {
		when(categoryRepository.findById(MISSING_ID)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.delete(MISSING_ID)).isInstanceOf(EntityNotFoundException.class)
				.hasMessage("category with id:" + MISSING_ID + " not found");

		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository).findById(MISSING_ID);
		verify(categoryRepository, never()).delete(any());
	}

	@Test
	public void test_whenUpdatingExistingCategory_shouldReturnUpdatedName() {
		Category existingCategory = new Category(CATEGORY_NAME_RAW);

		when(categoryRepository.findById(EXISTING_ID)).thenReturn(existingCategory);
		when(categoryRepository.findByName(CATEGORY_NAME_NORMALIZED)).thenReturn(null);
		when(categoryRepository.update(existingCategory)).thenReturn(new Category(CATEGORY_NAME_NORMALIZED));

		Category categoryResult = categoryService.update(EXISTING_ID, CATEGORY_NAME_RAW);

		assertThat(categoryResult.getName()).isEqualTo(CATEGORY_NAME_NORMALIZED);
		assertThat(existingCategory.getName()).isEqualTo(CATEGORY_NAME_NORMALIZED);

		InOrder inOrder = inOrder(categoryRepository, transactionManager);
		inOrder.verify(transactionManager).doInTransaction(any());
		inOrder.verify(categoryRepository).findByName(CATEGORY_NAME_NORMALIZED);
		inOrder.verify(categoryRepository).update(existingCategory);
	}

	@Test
	public void test_whenUpdatingCategoryDoesntExist_shouldThrowEntityNotFoundExcpetion() {
		when(categoryRepository.findById(MISSING_ID)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.update(MISSING_ID, CATEGORY_NAME_RAW))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("category with id:" + MISSING_ID + " not found");

		verify(categoryRepository).findById(MISSING_ID);
		verify(categoryRepository, never()).findByName(CATEGORY_NAME_RAW);
		verify(categoryRepository, never()).update(new Category(CATEGORY_NAME_RAW));
	}

	@Test
	public void test_whenUpdatingCategoryWithAnExistingName_shouldThrowCategoryNameAlreadyExistsExcpetion() {
		when(categoryRepository.findById(EXISTING_ID)).thenReturn(new Category(CATEGORY_NAME_NORMALIZED));
		when(categoryRepository.findByName(CATEGORY_NAME_NORMALIZED))
				.thenReturn(new Category(CATEGORY_NAME_NORMALIZED));

		assertThatThrownBy(() -> categoryService.update(EXISTING_ID, CATEGORY_NAME_RAW))
				.isInstanceOf(CategoryNameAlreadyExistsExcpetion.class);

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(categoryRepository).findById(EXISTING_ID);
		verify(categoryRepository).findByName(CATEGORY_NAME_NORMALIZED);
		verify(categoryRepository, never()).update(any(Category.class));
	}

	@Test
	public void test_whenUpdatingCategoryNullName_shouldThrowIllegalArgumentExcpetion() {
		when(categoryRepository.findById(EXISTING_ID)).thenReturn(new Category(CATEGORY_NAME_NORMALIZED));
		when(categoryRepository.findByName(CATEGORY_NAME_RAW)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.update(EXISTING_ID, null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must be provided");

		verify(transactionManager, never()).doInTransaction(any());
		verify(categoryRepository, never()).findById(EXISTING_ID);
		verify(categoryRepository, never()).findByName(CATEGORY_NAME_NORMALIZED);
		verify(categoryRepository, never()).update(any(Category.class));
	}

	@Test
	public void test_whenUpdatingCategoryBlankName_shouldThrowIllegalArgumentExcpetion() {
		when(categoryRepository.findById(EXISTING_ID)).thenReturn(new Category(CATEGORY_NAME_NORMALIZED));
		when(categoryRepository.findByName(CATEGORY_NAME_RAW)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.update(EXISTING_ID, BLANK_TABS_NAME))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be provided");

		verify(transactionManager, never()).doInTransaction(any());
		verify(categoryRepository, never()).findByName(CATEGORY_NAME_NORMALIZED);
		verify(categoryRepository, never()).update(any(Category.class));
	}

	@Test
	public void test_findAllWithElements_returnListOfProducts() {
		when(categoryRepository.findAll()).thenReturn(asList(new Category("category 1"), new Category("category 2")));
		List<Category> result = categoryService.findAll();

		assertThat(result).hasSize(2);
		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository).findAll();
	}

	@Test
	public void test_findAllWithNoElements_returnEmptyListOfProducts() {
		when(categoryRepository.findAll()).thenReturn(new ArrayList<Category>());

		List<Category> result = categoryService.findAll();
		assertThat(result).isEmpty();

		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository).findAll();
	}

	@Test
	public void test_deleteCategoryThatIsUsedByProducts_shouldThrowCategoryInUseException() {
		Category existingCategory = new Category(CATEGORY_NAME_NORMALIZED);
		existingCategory.setId(EXISTING_ID);

		when(categoryRepository.findById(EXISTING_ID)).thenReturn(existingCategory);
		when(categoryRepository.countProductsByCategoryId(EXISTING_ID)).thenReturn(2L);

		assertThatThrownBy(() -> categoryService.delete(EXISTING_ID)).isInstanceOf(CategoryInUseException.class);

		InOrder inOrder = Mockito.inOrder(categoryRepository, productRepository);
		inOrder.verify(categoryRepository).findById(EXISTING_ID);
		inOrder.verify(categoryRepository).countProductsByCategoryId(EXISTING_ID);
		inOrder.verify(categoryRepository, never()).delete(existingCategory);
	}

	@Test
	public void test_deleteWithNullId_shouldThrowIllegalArgumentException() {
		assertThatThrownBy(() -> categoryService.delete(null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("id must be provided");

		verify(transactionManager, never()).doInTransaction(any());
		verify(categoryRepository, never()).findById(any());
	}

	@Test
	public void test_deleteWithNegativeId_shouldThrowIllegalArgumentException() {
		assertThatThrownBy(() -> categoryService.delete(-1L)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("id must be positive");

		verify(transactionManager, never()).doInTransaction(any());
		verify(categoryRepository, never()).findById(any());
	}

	@Test
	public void test_deleteWithZeroId_shouldThrowIllegalArgumentException() {
		assertThatThrownBy(() -> categoryService.delete(0L)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("id must be positive");

		verify(transactionManager, never()).doInTransaction(any());
		verify(categoryRepository, never()).findById(any());
	}

}
