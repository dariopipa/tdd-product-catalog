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

	private final long Entity_Id = 1L;
	private final long NonExistingEntity_Id = 999L;
	private final String newName = " new Name ";

	@Before
	public void setup() {
		categoryRepository = mock(CategoryRepository.class);
		categoryService = new CategoryService(categoryRepository);
	}

	@Test
	public void test_whenCreatingCategory_shouldSaveAndReturnLongId() {
		when(categoryRepository.create(any(Category.class))).thenReturn(Entity_Id);

		Long result = categoryService.createCategory("tech");

		assertThat(result).isEqualTo(Entity_Id);
		verify(categoryRepository).create(any(Category.class));
	}

	@Test
	public void test_whenCategoryExists_shouldReturnCategoryEntity() {
		when(categoryRepository.create(any(Category.class))).thenReturn(Entity_Id);
		when(categoryRepository.findById(Entity_Id)).thenReturn(new Category("tech"));

		Category category = categoryService.findById(Entity_Id);

		assertThat(category.getName()).isEqualTo("tech");
		verify(categoryRepository).findById(Entity_Id);
	}

	@Test
	public void test_whenCategoryIdDoesntExist_shouldThrowEntityDoesNotExist() {
		when(categoryRepository.findById(NonExistingEntity_Id)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.findById(NonExistingEntity_Id))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("category with id:" + NonExistingEntity_Id + "not found");
		verify(categoryRepository).findById(NonExistingEntity_Id);
	}

	@Test
	public void test_whenCreatingEntityWithExistingName_shouldThrowCategoryNameAlreadyExistsExcpetion() {
		when(categoryRepository.findByName("tech")).thenReturn(new Category("tech"));

		assertThatThrownBy(() -> categoryService.createCategory("tech"))
				.isInstanceOf(CategoryNameAlreadyExistsExcpetion.class);

		verify(categoryRepository).findByName("tech");
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
	public void test_whenDeletingExistingCategory_shouldReturnDeletedMessage() {
		when(categoryRepository.findById(Entity_Id)).thenReturn(new Category("tech"));
		when(categoryRepository.delete(Entity_Id)).thenReturn("Deleted");

		String result = categoryService.delete(Entity_Id);

		assertThat(result).isEqualTo("Deleted");

		InOrder inOrder = Mockito.inOrder(categoryRepository);
		inOrder.verify(categoryRepository).findById(Entity_Id);
		inOrder.verify(categoryRepository).delete(Entity_Id);

	}

	@Test
	public void test_whenDeletingNonExistingCategory_throwEntityNotFoundException() {
		when(categoryRepository.findById(NonExistingEntity_Id)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.delete(NonExistingEntity_Id))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("category with id:" + NonExistingEntity_Id + "not found");
		;
		verify(categoryRepository).findById(NonExistingEntity_Id);
		verify(categoryRepository, never()).delete(anyLong());
	}

	@Test
	public void test_whenUpdatingExistingCategory_shouldReturnUpdatedName() {
		when(categoryRepository.findById(Entity_Id)).thenReturn(new Category("Old Name"));
		when(categoryRepository.findByName(newName)).thenReturn(null);
		when(categoryRepository.update(Entity_Id, newName)).thenReturn(new Category(newName));

		Category categoryResult = categoryService.update(Entity_Id, newName);
		assertThat(categoryResult.getName()).isEqualTo("new name");

		InOrder inOrder = inOrder(categoryRepository);
		inOrder.verify(categoryRepository).findById(Entity_Id);
		inOrder.verify(categoryRepository).findByName(newName);
		inOrder.verify(categoryRepository).update(Entity_Id, newName);
	}

	@Test
	public void test_whenUpdatingCategoryDoesntExist_shouldThrowEntityNotFoundExcpetion() {
		when(categoryRepository.findById(NonExistingEntity_Id)).thenReturn(null);

		assertThatThrownBy(() -> categoryService.update(NonExistingEntity_Id, newName))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("category with id:" + NonExistingEntity_Id + "not found");
		;

		verify(categoryRepository).findById(NonExistingEntity_Id);
		verify(categoryRepository, never()).findByName(newName);
		verify(categoryRepository, never()).update(NonExistingEntity_Id, newName);
	}

	@Test
	public void test_whenUpdatingCategoryWithAnExistingName_shouldThrowCategoryNameAlreadyExistsExcpetion() {
		when(categoryRepository.findById(Entity_Id)).thenReturn(new Category(newName));
		when(categoryRepository.findByName(newName)).thenReturn(new Category(newName));

		assertThatThrownBy(() -> categoryService.update(Entity_Id, newName))
				.isInstanceOf(CategoryNameAlreadyExistsExcpetion.class);

		verify(categoryRepository).findById(Entity_Id);
		verify(categoryRepository).findByName(newName);
		verify(categoryRepository, never()).update(Entity_Id, newName);
	}
}
