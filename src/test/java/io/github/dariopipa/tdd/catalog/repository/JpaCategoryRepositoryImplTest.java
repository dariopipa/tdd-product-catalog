package io.github.dariopipa.tdd.catalog.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.github.dariopipa.tdd.catalog.entities.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class JpaCategoryRepositoryImplTest {

	private static final String PU_NAME = "product-catalog-IM-PU";

	private static final long MISSING_ID = 999L;

	private static final String CATEGORY_NAME = "category";
	private static final String CATEGORY_NEW_NAME = "new category name";

	private EntityManagerFactory entityManagerFactory;
	private JpaCategoryRepositoryImpl jpaCategoryRepositoryImpl;
	private EntityManager entityManager;
	private EntityTransaction transaction;

	@Before
	public void setup() {
		entityManagerFactory = Persistence.createEntityManagerFactory(PU_NAME);
		entityManager = entityManagerFactory.createEntityManager();
		jpaCategoryRepositoryImpl = new JpaCategoryRepositoryImpl(entityManager);
		transaction = entityManager.getTransaction();
	}

	@Test
	public void test_createEntityInDatabase() {
		Category category = new Category(CATEGORY_NAME);

		transaction.begin();
		Long result = jpaCategoryRepositoryImpl.create(category);
		transaction.commit();

		assertThat(result).isNotNull();
		assertThat(category.getId()).isEqualTo(result);

		Category found = entityManager.find(Category.class, result);
		assertThat(found).isNotNull();
		assertThat(found.getName()).isEqualTo(CATEGORY_NAME);
	}

	@Test
	public void test_findByExistingId_shouldReturnEntity() {
		Category expectedCategory = new Category(CATEGORY_NAME);

		transaction.begin();
		entityManager.persist(expectedCategory);
		transaction.commit();

		Long categoryId = expectedCategory.getId();
		Category result = jpaCategoryRepositoryImpl.findById(categoryId);

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(CATEGORY_NAME);
		assertThat(result.getId()).isEqualTo(categoryId);
	}

	@Test
	public void test_findByNonExistingId_shouldReturnNull() {
		Category result = jpaCategoryRepositoryImpl.findById(MISSING_ID);

		assertThat(result).isNull();
	}

	@Test
	public void test_delete_shouldReturnDeletedMessage() {
		transaction.begin();
		Category expectedCategory = new Category(CATEGORY_NAME);
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
		Category existingCategory = new Category(CATEGORY_NAME);
		transaction.begin();
		entityManager.persist(existingCategory);
		transaction.commit();

		existingCategory.setName(CATEGORY_NEW_NAME);

		transaction.begin();
		Category result = jpaCategoryRepositoryImpl.update(existingCategory);
		transaction.commit();

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(CATEGORY_NEW_NAME);

		Category found = entityManager.find(Category.class, result.getId());
		assertThat(found).isNotNull();
		assertThat(found.getName()).isEqualTo(CATEGORY_NEW_NAME);
	}

	@Test
	public void test_findByName_shouldReturnFoundCategory() {
		Category category = new Category(CATEGORY_NAME);

		transaction.begin();
		entityManager.persist(category);
		transaction.commit();

		Category result = jpaCategoryRepositoryImpl.findByName(CATEGORY_NAME);

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(CATEGORY_NAME);
		assertThat(result.getId()).isEqualTo(category.getId());
	}

	@Test
	public void test_findAll_shouldReturnListOfCategories() {
		Category category1 = new Category(CATEGORY_NAME);
		Category category2 = new Category(CATEGORY_NEW_NAME);

		transaction.begin();
		entityManager.persist(category1);
		entityManager.persist(category2);
		transaction.commit();

		List<Category> result = jpaCategoryRepositoryImpl.findAll();

		assertThat(result).hasSize(2);
		assertThat(result).contains(category1, category2);
	}

}
