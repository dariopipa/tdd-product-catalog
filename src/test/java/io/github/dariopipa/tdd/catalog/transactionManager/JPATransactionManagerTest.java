package io.github.dariopipa.tdd.catalog.transactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.exceptions.JPARepoException;
import io.github.dariopipa.tdd.catalog.repository.CategoryRepository;
import io.github.dariopipa.tdd.catalog.repository.JpaCategoryRepositoryImpl;
import io.github.dariopipa.tdd.catalog.transactionmanager.JPATransactionManager;
import io.github.dariopipa.tdd.catalog.transactionmanager.TransactionManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPATransactionManagerTest {

	private static final String PU_NAME = "product-catalog-IM-PU";
	private static final String CATEGORY_NAME = "new category";
	private static final String CATEGORY_UPDATED_NAME = "new name";

	private TransactionManager<CategoryRepository> jpaTransactionManager;
	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;

	@Before
	public void setup() {
		entityManagerFactory = Persistence.createEntityManagerFactory(PU_NAME);
		entityManager = entityManagerFactory.createEntityManager();

		jpaTransactionManager = new JPATransactionManager<>(entityManager, JpaCategoryRepositoryImpl::new);
	}

	@After
	public void tearDown() {
		entityManager.close();
		entityManagerFactory.close();
	}

	@Test
	public void test_doInTransaction_shouldSucesfullySaveAndReturnTheResult() {
		Category category = new Category(CATEGORY_NAME);

		Long result = jpaTransactionManager.doInTransaction(repo -> repo.create(category));

		assertThat(result).isEqualTo(category.getId());

		EntityManager verifyEntityManager = entityManagerFactory.createEntityManager();
		Category foundCategory = verifyEntityManager.find(Category.class, result);

		assertThat(foundCategory).isNotNull();
		assertThat(foundCategory.getName()).isEqualTo(CATEGORY_NAME);
		assertThat(entityManager.getTransaction().isActive()).isFalse();
	}

	@Test
	public void test_doInTransaction_withRuntimeException_shouldRollbackTransaction() {
		Category existingCategory = new Category(CATEGORY_NAME);
		EntityManager setupEM = entityManagerFactory.createEntityManager();
		setupEM.getTransaction().begin();
		setupEM.persist(existingCategory);
		setupEM.getTransaction().commit();
		setupEM.close();

		Long existingCategoryId = existingCategory.getId();

		assertThatThrownBy(() -> jpaTransactionManager.doInTransaction(repo -> {
			Category categoryToUpdate = repo.findById(existingCategoryId);
			categoryToUpdate.setName(CATEGORY_UPDATED_NAME);
			throw new RuntimeException("Persistence failed.");
		})).isInstanceOf(RuntimeException.class).hasMessage("Persistence failed.");

		EntityManager verifyEntityManager = entityManagerFactory.createEntityManager();
		Category foundCategory = verifyEntityManager.find(Category.class, existingCategoryId);

		assertThat(foundCategory).isNotNull();
		assertThat(foundCategory.getName()).isEqualTo(CATEGORY_NAME);
		assertThat(entityManager.getTransaction().isActive()).isFalse();
	}

	@Test
	public void test_doInTransaction_withException_shouldRollbackTransaction() {
		Category existingCategory = new Category(CATEGORY_NAME);
		EntityManager setupEM = entityManagerFactory.createEntityManager();
		setupEM.getTransaction().begin();
		setupEM.persist(existingCategory);
		setupEM.getTransaction().commit();
		setupEM.close();

		Long existingCategoryId = existingCategory.getId();

		assertThatThrownBy(() -> {
			jpaTransactionManager.doInTransaction(repo -> {
				Category categoryToUpdate = repo.findById(existingCategoryId);
				categoryToUpdate.setName(CATEGORY_UPDATED_NAME);

				throw new JPARepoException("Persistence failed.");
			});
		}).isInstanceOf(JPARepoException.class).hasMessage("Persistence failed.");

		EntityManager verifyEntityManager = entityManagerFactory.createEntityManager();
		Category foundCategory = verifyEntityManager.find(Category.class, existingCategoryId);

		assertThat(foundCategory).isNotNull();
		assertThat(foundCategory.getName()).isEqualTo(CATEGORY_NAME);
		assertThat(entityManager.getTransaction().isActive()).isFalse();
	}

}
