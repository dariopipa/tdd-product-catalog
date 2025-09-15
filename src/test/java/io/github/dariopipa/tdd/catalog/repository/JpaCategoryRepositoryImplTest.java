package io.github.dariopipa.tdd.catalog.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import io.github.dariopipa.tdd.catalog.entities.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class JpaCategoryRepositoryImplTest {

	private JpaCategoryRepositoryImpl jpaCategoryRepositoryImpl;
	private EntityManager entityManager;
	private TypedQuery<Category> query;

	@Before
	public void setup() {
		entityManager = mock(EntityManager.class);
		jpaCategoryRepositoryImpl = new JpaCategoryRepositoryImpl(entityManager);
		query = mock(TypedQuery.class);

	}

	@Test
	public void test_createEntityInDatabase() {
		Category category = new Category("name");
		category.setId(12L);

		Long result = jpaCategoryRepositoryImpl.create(category);

		verify(entityManager, times(1)).persist(category);
		assertThat(result).isEqualTo(12L);
	}

	@Test
	public void test_findByExistingId_shouldReturnEntity() {
		Long categoryId = 1L;
		Category expectedCategory = new Category("electronics");
		expectedCategory.setId(categoryId);

		when(entityManager.find(Category.class, categoryId)).thenReturn(expectedCategory);
		Category result = jpaCategoryRepositoryImpl.findById(categoryId);

		assertThat(result).isEqualTo(expectedCategory);
		assertThat(result.getName()).isEqualTo("electronics");
		assertThat(result.getId()).isEqualTo(categoryId);
		verify(entityManager).find(Category.class, categoryId);
	}

	@Test
	public void test_findByNonExistingId_shouldReturnNull() {
		Long nonExistentId = 999L;

		when(entityManager.find(Category.class, nonExistentId)).thenReturn(null);
		Category result = jpaCategoryRepositoryImpl.findById(nonExistentId);

		assertThat(result).isNull();
		verify(entityManager).find(Category.class, nonExistentId);
	}

	@Test
	public void test_deleteById_shouldReturnDeletedMessage() {
		Long existentId = 1L;
		Category expectedCategory = new Category("electronics");

		when(entityManager.find(Category.class, existentId)).thenReturn(expectedCategory);
		String result = jpaCategoryRepositoryImpl.delete(expectedCategory);

		assertThat(result).isEqualTo("Deleted");
		verify(entityManager).remove(expectedCategory);
	}

	@Test
	public void test_updateCategory_shouldReturnUpdatedCategory() {
		Category updatedCategory = new Category("technology");

		when(entityManager.merge(updatedCategory)).thenReturn(updatedCategory);

		Category result = jpaCategoryRepositoryImpl.update(updatedCategory);

		assertThat(result).isEqualTo(updatedCategory);
		verify(entityManager, times(1)).merge(updatedCategory);
	}

	@Test
	public void test_findByName_shouldReturnFoundCategory() {
		String categoryName = "electronics";
		Category expectedCategory = new Category("electronics");

		when(entityManager.createQuery("SELECT c FROM Category c WHERE c.name = :name", Category.class))
				.thenReturn(query);
		when(query.setParameter("name", categoryName)).thenReturn(query);
		when(query.getSingleResult()).thenReturn(expectedCategory);

		Category result = jpaCategoryRepositoryImpl.findByName(categoryName);

		assertThat(result).isEqualTo(expectedCategory);
		assertThat(result.getName()).isEqualTo(categoryName);

		verify(entityManager).createQuery("SELECT c FROM Category c WHERE c.name = :name", Category.class);
		verify(query).setParameter("name", categoryName);
		verify(query).getSingleResult();
	}

	@Test
	public void test_findByNonExistentName_shouldReturnNull() {
		String nonExistentCategoryName = "does not exist";

		when(entityManager.createQuery("SELECT c FROM Category c WHERE c.name = :name", Category.class))
				.thenReturn(query);
		when(query.setParameter("name", nonExistentCategoryName)).thenReturn(query);
		when(query.getSingleResult()).thenThrow(NoResultException.class);

		Category result = jpaCategoryRepositoryImpl.findByName(nonExistentCategoryName);

		assertThat(result).isEqualTo(null);
	}

}
