package io.github.dariopipa.tdd.catalog.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import io.github.dariopipa.tdd.catalog.entities.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class JpaCategoryRepositoryImplTest {

	private EntityManagerFactory entityManagerFactory;
	private JpaCategoryRepositoryImpl jpaCategoryRepositoryImpl;
	private EntityManager entityManager;
	private EntityTransaction transaction;
	private Long nonExistentId = 999L;

	@Before
	public void setup() {
		entityManagerFactory = Persistence.createEntityManagerFactory("product-catalog-IM-PU");
		entityManager = entityManagerFactory.createEntityManager();
		jpaCategoryRepositoryImpl = new JpaCategoryRepositoryImpl(entityManager);
		transaction = entityManager.getTransaction();
	}

	@Test
	public void test_createEntityInDatabase() {
		Category category = new Category("name");

		transaction.begin();
		Long result = jpaCategoryRepositoryImpl.create(category);
		transaction.commit();

		assertThat(result).isNotNull();
		assertThat(category.getId()).isEqualTo(result);

		Category found = entityManager.find(Category.class, result);
		assertThat(found).isNotNull();
		assertThat(found.getName()).isEqualTo("name");
	}

	@Test
	public void test_findByExistingId_shouldReturnEntity() {
		Category expectedCategory = new Category("name");

		transaction.begin();
		entityManager.persist(expectedCategory);
		transaction.commit();

		Long categoryId = expectedCategory.getId();
		Category result = jpaCategoryRepositoryImpl.findById(categoryId);

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("name");
		assertThat(result.getId()).isEqualTo(categoryId);
	}

	@Test
	public void test_findByNonExistingId_shouldReturnNull() {
		Category result = jpaCategoryRepositoryImpl.findById(nonExistentId);

		assertThat(result).isNull();
	}

	@Test
	public void test_deleteById_shouldReturnDeletedMessage() {
		transaction.begin();
		Category expectedCategory = new Category("electronics");
		entityManager.persist(expectedCategory);
		transaction.commit();

		transaction.begin();
		String result = jpaCategoryRepositoryImpl.delete(expectedCategory);
		transaction.commit();

		assertThat(result).isEqualTo("Deleted");
		Category found = entityManager.find(Category.class, expectedCategory.getId());
		assertThat(found).isNull();
	}

	@Test
	public void test_updateCategory_shouldReturnUpdatedCategory() {
		Category existingCategory = new Category("old-name");
		transaction.begin();
		entityManager.persist(existingCategory);
		transaction.commit();

		existingCategory.setName("new name");

		transaction.begin();
		Category result = jpaCategoryRepositoryImpl.update(existingCategory);
		transaction.commit();

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("new name");

		Category found = entityManager.find(Category.class, result.getId());
		assertThat(found).isNotNull();
		assertThat(found.getName()).isEqualTo("new name");
	}

	@Test
	public void test_findByName_shouldReturnFoundCategory() {
		String categoryName = "electronics";
		Category category = new Category(categoryName);

		transaction.begin();
		entityManager.persist(category);
		transaction.commit();

		Category result = jpaCategoryRepositoryImpl.findByName(categoryName);

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(categoryName);
		assertThat(result.getId()).isEqualTo(category.getId());
	}

	@Test
	public void test_findByNonExistentName_shouldReturnNull() {
		String nonExistentCategoryName = "does not exist";

		Category result = jpaCategoryRepositoryImpl.findByName(nonExistentCategoryName);

		assertThat(result).isNull();
	}

}
