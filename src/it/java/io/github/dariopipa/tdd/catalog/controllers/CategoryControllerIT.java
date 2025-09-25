package io.github.dariopipa.tdd.catalog.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import io.github.dariopipa.tdd.catalog.service.CategoryService;
import io.github.dariopipa.tdd.catalog.transactionManger.JPATransactionManager;
import io.github.dariopipa.tdd.catalog.views.CategoryView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class CategoryControllerIT {

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
		MockitoAnnotations.openMocks(this);

		// this will make sure that the db is empty every time
		em = emf.createEntityManager();
		em.getTransaction().begin();
		em.createQuery("DELETE FROM Category").executeUpdate();
		em.getTransaction().commit();
		em.close();

		em = emf.createEntityManager();
		categoryRepository = new JpaCategoryRepositoryImpl(em);
		transactionManager = new JPATransactionManager(em);
		categoryService = new CategoryService(categoryRepository, transactionManager);
		categoryView = mock(CategoryView.class);
		categoryController = new CategoryController(categoryService, categoryView);
	}

	@Test
	public void shouldSaveCategoryInDatabase_whenControllerCreatesCategoryIT() {
		String categoryName = "integration-category";

		categoryController.create(categoryName);
		Category saved = categoryRepository.findByName(categoryName);

		assertThat(saved).isNotNull();
		assertThat(categoryName).isEqualTo(saved.getName());
		verify(categoryView).addedCategory(saved);
	}

	@Test
	public void shouldReturnAllCategoriesFromDB_whenControllerCallsFindAllIT() {
		String bookCategory = "integration-books";
		String techCategory = "integration-tech";

		categoryController.create(bookCategory);
		categoryController.create(techCategory);

		categoryController.findAll();

		List<Category> categories = categoryService.findAll();

		assertThat(categories).hasSize(2);
		verify(categoryView).findAllCategories(categories);
	}

	@Test
	public void shouldDeleteCategoryInDatabase_whenControllerDeletesCategoryIT() {
		String categoryName = "category";

		categoryController.create(categoryName);
		Category beforeDelete = categoryRepository.findByName(categoryName);

		assertThat(beforeDelete).isNotNull();
		assertThat(beforeDelete.getName()).isEqualTo(categoryName);

		Long id = beforeDelete.getId();
		categoryController.delete(id);

		assertThatThrownBy(() -> categoryService.findById(id)).isInstanceOf(RuntimeException.class);
		verify(categoryView).deletedCategory(beforeDelete);
	}

	@Test
	public void shouldUpdateCategoryInDatabase_whenControllerUpdatesCategoryIT() {
		String name = "tech";
		String updatedName = "books";

		categoryController.create(name);
		Category beforeUpdate = categoryRepository.findByName(name);

		assertThat(beforeUpdate).isNotNull();
		assertThat(beforeUpdate.getName()).isEqualTo(name);

		Long id = beforeUpdate.getId();

		categoryController.update(id, updatedName);
		Category updatedCategory = categoryService.findById(id);

		assertThat(updatedCategory).isNotNull();
		assertThat(updatedCategory.getName()).isEqualTo(updatedName);
		assertThat(updatedCategory.getId()).isEqualTo(id);

		verify(categoryView).updateCategory(updatedCategory);
	}
}
