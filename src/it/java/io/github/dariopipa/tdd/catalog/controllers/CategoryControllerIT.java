package io.github.dariopipa.tdd.catalog.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.repository.CategoryRepository;
import io.github.dariopipa.tdd.catalog.repository.JpaCategoryRepositoryImpl;
import io.github.dariopipa.tdd.catalog.repository.JpaProductRepositoryImpl;
import io.github.dariopipa.tdd.catalog.repository.ProductRepository;
import io.github.dariopipa.tdd.catalog.service.CategoryService;
import io.github.dariopipa.tdd.catalog.transactionmanger.JPATransactionManager;
import io.github.dariopipa.tdd.catalog.views.CategoryView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class CategoryControllerIT {

	private static final String CATEGORY_NAME = "tech";
	private static final String CATEGORY_UPDATED_NAME = "books";

	@SuppressWarnings("resource")
	@ClassRule
	public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
			DockerImageName.parse("postgres:16-alpine")).withUsername("test").withPassword("test");

	private static EntityManagerFactory emf;

	private EntityManager em;
	private CategoryRepository categoryRepository;
	private CategoryService categoryService;
	private JPATransactionManager transactionManager;
	private CategoryController categoryController;
	private ProductRepository productRepository;

	private AutoCloseable closeable;

	@Mock
	private CategoryView categoryView;

	@BeforeClass
	public static void beforeAll() {

		Map<String, Object> props = new HashMap<>();
		props.put("jakarta.persistence.jdbc.url", postgres.getJdbcUrl());
		props.put("jakarta.persistence.jdbc.user", postgres.getUsername());
		props.put("jakarta.persistence.jdbc.password", postgres.getPassword());
		props.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
		props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		props.put("hibernate.hbm2ddl.auto", "create-drop");

		emf = Persistence.createEntityManagerFactory("product-catalog-IM-PU", props);
	}

	@Before
	public void setUp() {
		closeable = MockitoAnnotations.openMocks(this);

		// this will make sure that the db is empty every time
		EntityManager tempEm = emf.createEntityManager();
		tempEm.getTransaction().begin();
		tempEm.createQuery("DELETE FROM Product").executeUpdate();
		tempEm.createQuery("DELETE FROM Category").executeUpdate();
		tempEm.getTransaction().commit();
		tempEm.close();

		em = emf.createEntityManager();
		categoryRepository = new JpaCategoryRepositoryImpl(em);
		productRepository = new JpaProductRepositoryImpl(em);
		transactionManager = new JPATransactionManager(em);
		categoryService = new CategoryService(categoryRepository, transactionManager, productRepository);
		categoryController = new CategoryController(categoryService, categoryView);
	}

	@After
	public void tearDown() throws Exception {
		closeable.close();
		em.close();
	}

	@Test
	public void shouldSaveCategoryInDatabase_whenControllerCreatesCategoryIT() {
		categoryController.create(CATEGORY_NAME);
		Category saved = categoryRepository.findByName(CATEGORY_NAME);

		assertThat(saved).isNotNull();
		assertThat(saved.getName()).isEqualTo(CATEGORY_NAME);
		verify(categoryView).addedCategory(saved);
	}

	@Test
	public void shouldReturnAllCategoriesFromDB_whenControllerCallsFindAllIT() {
		categoryController.create(CATEGORY_NAME);
		categoryController.create(CATEGORY_UPDATED_NAME);

		categoryController.findAll();

		List<Category> categories = categoryService.findAll();

		assertThat(categories).hasSize(2);
		verify(categoryView).findAllCategories(categories);
	}

	@Test
	public void shouldDeleteCategoryInDatabase_whenControllerDeletesCategoryIT() {
		categoryController.create(CATEGORY_NAME);
		Category beforeDelete = categoryRepository.findByName(CATEGORY_NAME);

		assertThat(beforeDelete).isNotNull();
		assertThat(beforeDelete.getName()).isEqualTo(CATEGORY_NAME);

		Long id = beforeDelete.getId();
		categoryController.delete(id);

		assertThatThrownBy(() -> categoryService.findById(id)).isInstanceOf(RuntimeException.class);
		verify(categoryView).deletedCategory(beforeDelete);
	}

	@Test
	public void shouldUpdateCategoryInDatabase_whenControllerUpdatesCategoryIT() {
		categoryController.create(CATEGORY_NAME);
		Category beforeUpdate = categoryRepository.findByName(CATEGORY_NAME);

		assertThat(beforeUpdate).isNotNull();
		assertThat(beforeUpdate.getName()).isEqualTo(CATEGORY_NAME);

		Long id = beforeUpdate.getId();

		categoryController.update(id, CATEGORY_UPDATED_NAME);
		Category updatedCategory = categoryService.findById(id);

		assertThat(updatedCategory).isNotNull();
		assertThat(updatedCategory.getName()).isEqualTo(CATEGORY_UPDATED_NAME);
		assertThat(updatedCategory.getId()).isEqualTo(id);

		verify(categoryView).updateCategory(updatedCategory);
	}
}
