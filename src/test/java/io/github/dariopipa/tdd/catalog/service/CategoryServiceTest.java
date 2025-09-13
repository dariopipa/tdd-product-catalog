package io.github.dariopipa.tdd.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.exceptions.CategoryNameAlreadyExistsExcpetion;
import io.github.dariopipa.tdd.catalog.exceptions.EntityNotFoundException;

public class CategoryServiceTest {

	private CategoryService categoryService;
	private CategoryRepository categoryRepository;

	private final long entityId = 1L;
	private final long nonExistingId = 999L;
	private final String newName = " new Name ";
	private final String normalizedName = "new name";

	@Before
	public void setup() {
		categoryRepository = mock(CategoryRepository.class);
		categoryService = new CategoryService(categoryRepository);
	}

	@Test
	public void test_whenCreatingCategory_shouldSaveAndReturnLongId() {
		when(categoryRepository.create(any(Category.class))).thenReturn(entityId);

		Long result = categoryService.createCategory("tech");

		assertThat(result).isEqualTo(entityId);
		verify(categoryRepository).create(any(Category.class));
	}

	@Test
	public void test_whenCategoryExists_shouldReturnCategoryEntity() {
		when(categoryRepository.create(any(Category.class))).thenReturn(entityId);
		when(categoryRepository.findById(entityId)).thenReturn(new Category("tech"));

		Category category = categoryService.findById(entityId);

		assertThat(category.getName()).isEqualTo("tech");
		verify(categoryRepository).findById(entityId);
	}

	@Test
	public void test_whenCategoryIdDoesntExist_shouldThrowEntityDoesNotExist() {
		when(categoryRepository.findById(nonExistingId)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.findById(nonExistingId)).isInstanceOf(EntityNotFoundException.class)
				.hasMessage("category with id:" + nonExistingId + "not found");
		verify(categoryRepository).findById(nonExistingId);
	}

	@Test
	public void test_whenCategoryIdIsNull_shouldThrowIllegalArgumentExcpetion() {
		when(categoryRepository.findById(null)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.findById(null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("id must be provided");

		verify(categoryRepository, never()).findById(null);
	}

	@Test
	public void test_whenCategoryIdIsNegative_shouldThrowIllegalArgumentExcpetion() {
		when(categoryRepository.findById(-1L)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.findById(-1L)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("id must be positive");

		verify(categoryRepository, never()).findById(-1L);
	}

	@Test
	public void test_whenCreatingEntityWithExistingName_shouldThrowCategoryNameAlreadyExistsExcpetion() {
		when(categoryRepository.findByName(normalizedName)).thenReturn(new Category(normalizedName));

		assertThatThrownBy(() -> categoryService.createCategory(normalizedName))
				.isInstanceOf(CategoryNameAlreadyExistsExcpetion.class);

		verify(categoryRepository).findByName(normalizedName);
		verify(categoryRepository, never()).create(any(Category.class));
	}

	@Test
	public void test_whenCreatingEntity_withBlankName_shouldThrowIllegalArgumentExceptionWithNameMustBeValidMessage() {
		assertThatThrownBy(() -> categoryService.createCategory("\t\t\t    "))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be valid");

		verify(categoryRepository, never()).create(any(Category.class));
	}

	@Test
	public void test_whenCreatingEntity_withEmptySpaces_shouldThrowIllegalArgumentExceptionWithNameMustBeValidMessage() {
		assertThatThrownBy(() -> categoryService.createCategory(" ")).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must be valid");

		verify(categoryRepository, never()).create(any(Category.class));
	}

	@Test
	public void test_whenCreatingEntity_withNonNormalizedName_shouldCreateEntityWithNormalizedName() {
		when(categoryRepository.create(new Category(newName))).thenReturn(entityId);

		Long result = categoryService.createCategory(newName);

		assertThat(result).isEqualTo(entityId);
		verify(categoryRepository).create(new Category(newName));
	}

	@Test
	public void test_whenDeletingExistingCategory_shouldReturnDeletedMessage() {
		when(categoryRepository.findById(entityId)).thenReturn(new Category(normalizedName));
		when(categoryRepository.delete(entityId)).thenReturn("Deleted");

		String result = categoryService.delete(entityId);

		assertThat(result).isEqualTo("Deleted");

		InOrder inOrder = Mockito.inOrder(categoryRepository);
		inOrder.verify(categoryRepository).findById(entityId);
		inOrder.verify(categoryRepository).delete(entityId);

	}

	@Test
	public void test_whenDeletingNonExistingCategory_throwEntityNotFoundException() {
		when(categoryRepository.findById(nonExistingId)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.delete(nonExistingId)).isInstanceOf(EntityNotFoundException.class)
				.hasMessage("category with id:" + nonExistingId + "not found");
		;
		verify(categoryRepository).findById(nonExistingId);
		verify(categoryRepository, never()).delete(anyLong());
	}

	@Test
	public void test_whenUpdatingExistingCategory_shouldReturnUpdatedName() {
		when(categoryRepository.findById(entityId)).thenReturn(new Category("Old Name"));
		when(categoryRepository.findByName(normalizedName)).thenReturn(null);
		when(categoryRepository.update(entityId, normalizedName)).thenReturn(new Category(normalizedName));

		Category categoryResult = categoryService.update(entityId, newName);
		assertThat(categoryResult.getName()).isEqualTo(normalizedName);

		InOrder inOrder = inOrder(categoryRepository);
		inOrder.verify(categoryRepository).findById(entityId);
		inOrder.verify(categoryRepository).findByName(normalizedName);
		inOrder.verify(categoryRepository).update(entityId, normalizedName);
	}

	@Test
	public void test_whenUpdatingCategoryDoesntExist_shouldThrowEntityNotFoundExcpetion() {
		when(categoryRepository.findById(nonExistingId)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.update(nonExistingId, newName))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("category with id:" + nonExistingId + "not found");
		;

		verify(categoryRepository).findById(nonExistingId);
		verify(categoryRepository, never()).findByName(newName);
		verify(categoryRepository, never()).update(nonExistingId, newName);
	}

	@Test
	public void test_whenUpdatingCategoryWithAnExistingName_shouldThrowCategoryNameAlreadyExistsExcpetion() {
		when(categoryRepository.findById(entityId)).thenReturn(new Category(normalizedName));
		when(categoryRepository.findByName(normalizedName)).thenReturn(new Category(normalizedName));

		assertThatThrownBy(() -> categoryService.update(entityId, newName))
				.isInstanceOf(CategoryNameAlreadyExistsExcpetion.class);

		verify(categoryRepository).findById(entityId);
		verify(categoryRepository).findByName(normalizedName);
		verify(categoryRepository, never()).update(entityId, normalizedName);
	}

	@Test
	public void test_whenUpdatingCategoryNullName_shouldThrowIllegalArgumentExcpetion() {
		when(categoryRepository.findById(entityId)).thenReturn(new Category(normalizedName));
		when(categoryRepository.findByName(newName)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.update(entityId, null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must be provided");

		verify(categoryRepository).findById(entityId);
		verify(categoryRepository, never()).findByName(normalizedName);
		verify(categoryRepository, never()).update(entityId, newName);
	}

	@Test
	public void test_whenUpdatingCategoryBlankName_shouldThrowIllegalArgumentExcpetion() {
		when(categoryRepository.findById(entityId)).thenReturn(new Category(normalizedName));
		when(categoryRepository.findByName(newName)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.update(entityId, "\t \t \t "))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be provided");

		verify(categoryRepository).findById(entityId);
		verify(categoryRepository, never()).findByName(normalizedName);
		verify(categoryRepository, never()).update(entityId, newName);
	}
}
