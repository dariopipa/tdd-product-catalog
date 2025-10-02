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
import io.github.dariopipa.tdd.catalog.transactionManger.TransactionCode;
import io.github.dariopipa.tdd.catalog.transactionManger.TransactionManager;

public class CategoryServiceTest {

	private CategoryService categoryService;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private TransactionManager transactionManager;

	@Mock
	private ProductRepository productRepository;

	private final long entityId = 1L;
	private final long nonExistingId = 999L;
	private final String newName = " new Name ";
	private final String normalizedName = "new name";

	@Before
	public void setup() {
		MockitoAnnotations.openMocks(this);

		when(transactionManager.doInTransaction(any())).thenAnswer(answer((TransactionCode<?> code) -> code.execute()));

		categoryService = new CategoryService(categoryRepository, transactionManager, productRepository);
	}

	@Test
	public void test_whenCreatingCategory_shouldSaveAndReturnLongId() {
		when(categoryRepository.create(any(Category.class))).thenReturn(entityId);

		Long result = categoryService.createCategory("tech");

		assertThat(result).isEqualTo(entityId);
		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository).create(any(Category.class));
	}

	@Test
	public void test_whenCategoryExists_shouldReturnCategoryEntity() {
		when(categoryRepository.create(any(Category.class))).thenReturn(entityId);
		when(categoryRepository.findById(entityId)).thenReturn(new Category("tech"));

		Category category = categoryService.findById(entityId);

		assertThat(category.getName()).isEqualTo("tech");
		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository).findById(entityId);
	}

	@Test
	public void test_whenCategoryIdDoesntExist_shouldThrowEntityDoesNotExist() {
		when(categoryRepository.findById(nonExistingId)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.findById(nonExistingId)).isInstanceOf(EntityNotFoundException.class)
				.hasMessage("category with id:" + nonExistingId + " not found");

		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository).findById(nonExistingId);
	}

	@Test
	public void test_whenCategoryIdIsNull_shouldThrowIllegalArgumentExcpetion() {
		when(categoryRepository.findById(null)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.findById(null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("id must be provided");

		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository, never()).findById(null);
	}

	@Test
	public void test_whenCategoryIdIsNegative_shouldThrowIllegalArgumentExcpetion() {
		when(categoryRepository.findById(-1L)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.findById(-1L)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("id must be positive");

		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository, never()).findById(-1L);
	}

	@Test
	public void test_whenCategoryIdIsZero_shouldThrowIllegalArgumentExcpetion() {
		when(categoryRepository.findById(0L)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.findById(0L)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("id must be positive");

		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository, never()).findById(0L);
	}

	@Test
	public void test_whenCreatingEntityWithExistingName_shouldThrowCategoryNameAlreadyExistsExcpetion() {
		when(categoryRepository.findByName(normalizedName)).thenReturn(new Category(normalizedName));

		assertThatThrownBy(() -> categoryService.createCategory(normalizedName))
				.isInstanceOf(CategoryNameAlreadyExistsExcpetion.class);

		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository).findByName(normalizedName);
		verify(categoryRepository, never()).create(any(Category.class));
	}

	@Test
	public void test_whenCreatingEntity_withBlankName_shouldThrowIllegalArgumentExceptionWithNameMustBeValidMessage() {
		assertThatThrownBy(() -> categoryService.createCategory("\t\t\t    "))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be provided");

		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository, never()).create(any(Category.class));
	}

	@Test
	public void test_whenCreatingEntity_withEmptySpaces_shouldThrowIllegalArgumentExceptionWithNameMustBeValidMessage() {
		assertThatThrownBy(() -> categoryService.createCategory(" ")).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must be provided");

		verify(categoryRepository, never()).create(any(Category.class));
	}

	@Test
	public void test_whenCreatingEntity_withNonNormalizedName_shouldCreateEntityWithNormalizedName() {
		when(categoryRepository.findByName("new name")).thenReturn(null);
		when(categoryRepository.create(any(Category.class))).thenReturn(entityId);

		Long result = categoryService.createCategory(newName);

		assertThat(result).isEqualTo(entityId);
		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository).findByName("new name");
		verify(categoryRepository).create(any(Category.class));
	}

	@Test
	public void test_whenDeletingExistingCategory_shouldReturnDeletedMessage() {
		Category existingCategory = new Category(normalizedName);

		when(categoryRepository.findById(entityId)).thenReturn(existingCategory);
		when(categoryRepository.delete(existingCategory)).thenReturn("Deleted");

		String result = categoryService.delete(entityId);

		assertThat(result).isEqualTo("Deleted");

		InOrder inOrder = Mockito.inOrder(categoryRepository, transactionManager);
		verify(transactionManager, times(1)).doInTransaction(any());
		inOrder.verify(categoryRepository).findById(entityId);
		inOrder.verify(categoryRepository).delete(existingCategory);
	}

	@Test
	public void test_whenDeletingNonExistingCategory_throwEntityNotFoundException() {
		when(categoryRepository.findById(nonExistingId)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.delete(nonExistingId)).isInstanceOf(EntityNotFoundException.class)
				.hasMessage("category with id:" + nonExistingId + " not found");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(categoryRepository).findById(nonExistingId);
		verify(categoryRepository, never()).delete(any());
	}

	@Test
	public void test_whenUpdatingExistingCategory_shouldReturnUpdatedName() {
		Category existingCategory = new Category("Old Name");

		when(categoryRepository.findById(entityId)).thenReturn(existingCategory);
		when(categoryRepository.findByName(normalizedName)).thenReturn(null);
		when(categoryRepository.update(existingCategory)).thenReturn(new Category(normalizedName));

		Category categoryResult = categoryService.update(entityId, newName);

		assertThat(categoryResult.getName()).isEqualTo(normalizedName);
		assertThat(existingCategory.getName()).isEqualTo(normalizedName);

		InOrder inOrder = inOrder(categoryRepository, transactionManager);
		inOrder.verify(transactionManager).doInTransaction(any());
		inOrder.verify(categoryRepository).findByName(normalizedName);
		inOrder.verify(categoryRepository).update(existingCategory);
	}

	@Test
	public void test_whenUpdatingCategoryDoesntExist_shouldThrowEntityNotFoundExcpetion() {
		when(categoryRepository.findById(nonExistingId)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.update(nonExistingId, newName))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("category with id:" + nonExistingId + " not found");

		verify(categoryRepository).findById(nonExistingId);
		verify(categoryRepository, never()).findByName(newName);
		verify(categoryRepository, never()).update(new Category(newName));
	}

	@Test
	public void test_whenUpdatingCategoryWithAnExistingName_shouldThrowCategoryNameAlreadyExistsExcpetion() {
		when(categoryRepository.findById(entityId)).thenReturn(new Category(normalizedName));
		when(categoryRepository.findByName(normalizedName)).thenReturn(new Category(normalizedName));

		assertThatThrownBy(() -> categoryService.update(entityId, newName))
				.isInstanceOf(CategoryNameAlreadyExistsExcpetion.class);

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(categoryRepository).findById(entityId);
		verify(categoryRepository).findByName(normalizedName);
		verify(categoryRepository, never()).update(any(Category.class));
	}

	@Test
	public void test_whenUpdatingCategoryNullName_shouldThrowIllegalArgumentExcpetion() {
		when(categoryRepository.findById(entityId)).thenReturn(new Category(normalizedName));
		when(categoryRepository.findByName(newName)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.update(entityId, null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must be provided");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(categoryRepository).findById(entityId);
		verify(categoryRepository, never()).findByName(normalizedName);
		verify(categoryRepository, never()).update(any(Category.class));
	}

	@Test
	public void test_whenUpdatingCategoryBlankName_shouldThrowIllegalArgumentExcpetion() {
		when(categoryRepository.findById(entityId)).thenReturn(new Category(normalizedName));
		when(categoryRepository.findByName(newName)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.update(entityId, "\t \t \t "))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be provided");

		verify(transactionManager, times(1)).doInTransaction(any());
		verify(categoryRepository, never()).findByName(normalizedName);
		verify(categoryRepository, never()).update(any(Category.class));
	}

	@Test
	public void test_findAllWithElements_returnListOfProducts() {
		when(categoryRepository.findAll()).thenReturn(asList(new Category("category 1"), new Category("Category 2")));

		List<Category> result = categoryService.findAll();

		assertThat(result).hasSize(2);
		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository).findAll();
	}

	@Test
	public void test_findAllWithNoElements_returnEmptyListOfProducts() {
		when(categoryRepository.findAll()).thenReturn(new ArrayList<Category>());

		List<Category> result = categoryService.findAll();

		assertThat(result).isEqualTo(new ArrayList<Category>());
		assertThat(result).isEmpty();

		verify(transactionManager).doInTransaction(any());
		verify(categoryRepository).findAll();
	}

	@Test
	public void test_deleteCategoryThatIsUsedByProducts_shouldThrowCategoryInUseException() {
		Category existingCategory = new Category(normalizedName);
		existingCategory.setId(1L);

		when(categoryRepository.findById(entityId)).thenReturn(existingCategory);
		when(productRepository.countByCategoryId(entityId)).thenReturn(2L);

		assertThatThrownBy(() -> categoryService.delete(entityId)).isInstanceOf(CategoryInUseException.class);

		InOrder inOrder = Mockito.inOrder(categoryRepository, productRepository);
		inOrder.verify(categoryRepository).findById(entityId);
		inOrder.verify(productRepository).countByCategoryId(1L);
		inOrder.verify(categoryRepository, never()).delete(existingCategory);
	}

}
