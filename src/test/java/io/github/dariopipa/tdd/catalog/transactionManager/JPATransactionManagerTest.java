package io.github.dariopipa.tdd.catalog.transactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Before;
import org.junit.Test;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.transactionManger.JPATransactionManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPATransactionManagerTest {

	private JPATransactionManager jpaTransactionManager;
	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;

	@Before
	public void setup() {
		entityManagerFactory = Persistence.createEntityManagerFactory("product-catalog-IM-PU");
		entityManager = entityManagerFactory.createEntityManager();

		jpaTransactionManager = new JPATransactionManager(entityManager);
	}

	@Test
	public void test_doInTransaction_shouldSucesfullySaveAndReturnTheResult() {
		Category category = new Category("new category");

		Long result = jpaTransactionManager.doInTransaction(() -> {
			entityManager.persist(category);
			return category.getId();
		});

		assertThat(result).isEqualTo(category.getId());

		EntityManager verifyEntityManager = entityManagerFactory.createEntityManager();
		Category foundCategory = verifyEntityManager.find(Category.class, result);

		assertThat(foundCategory).isNotNull();
		assertThat(foundCategory.getName()).isEqualTo("new category");
		assertThat(entityManager.getTransaction().isActive()).isFalse();
	}

	@Test
	public void test_doInTransaction_withRuntimeException_shouldRollbackTransaction() {
		Category existingCategory = new Category("existing category");
		EntityManager setupEM = entityManagerFactory.createEntityManager();
		setupEM.getTransaction().begin();
		setupEM.persist(existingCategory);
		setupEM.getTransaction().commit();
		setupEM.close();

		Long existingCategoryId = existingCategory.getId();

		assertThatThrownBy(() -> {
			jpaTransactionManager.doInTransaction(() -> {
				Category categoryToUpdate = entityManager.find(Category.class, existingCategoryId);
				categoryToUpdate.setName("new name");

				throw new RuntimeException("Persistence failed.");
			});
		}).isInstanceOf(RuntimeException.class).hasMessage("Persistence failed.");

		EntityManager verifyEntityManager = entityManagerFactory.createEntityManager();
		Category foundCategory = verifyEntityManager.find(Category.class, existingCategoryId);

		assertThat(foundCategory).isNotNull();
		assertThat(foundCategory.getName()).isEqualTo("existing category");
		assertThat(entityManager.getTransaction().isActive()).isFalse();
	}

	@Test
	public void test_doInTransaction_withException_shouldRollbackTransaction() {
		Category existingCategory = new Category("existing category");
		EntityManager setupEM = entityManagerFactory.createEntityManager();
		setupEM.getTransaction().begin();
		setupEM.persist(existingCategory);
		setupEM.getTransaction().commit();
		setupEM.close();

		Long existingCategoryId = existingCategory.getId();

		assertThatThrownBy(() -> {
			jpaTransactionManager.doInTransaction(() -> {
				Category categoryToUpdate = entityManager.find(Category.class, existingCategoryId);
				categoryToUpdate.setName("new name");

				throw new Exception("Persistence failed.");
			});
		}).isInstanceOf(Exception.class);

		EntityManager verifyEntityManager = entityManagerFactory.createEntityManager();
		Category foundCategory = verifyEntityManager.find(Category.class, existingCategoryId);

		assertThat(foundCategory).isNotNull();
		assertThat(foundCategory.getName()).isEqualTo("existing category");
		assertThat(entityManager.getTransaction().isActive()).isFalse();
	}

}
