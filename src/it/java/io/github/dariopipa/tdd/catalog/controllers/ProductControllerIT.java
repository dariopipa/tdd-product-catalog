package io.github.dariopipa.tdd.catalog.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
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
import io.github.dariopipa.tdd.catalog.entities.Product;
import io.github.dariopipa.tdd.catalog.repository.CategoryRepository;
import io.github.dariopipa.tdd.catalog.repository.JpaCategoryRepositoryImpl;
import io.github.dariopipa.tdd.catalog.repository.JpaProductRepositoryImpl;
import io.github.dariopipa.tdd.catalog.repository.ProductRepository;
import io.github.dariopipa.tdd.catalog.service.CategoryService;
import io.github.dariopipa.tdd.catalog.service.ProductService;
import io.github.dariopipa.tdd.catalog.transactionManger.JPATransactionManager;
import io.github.dariopipa.tdd.catalog.views.CategoryView;
import io.github.dariopipa.tdd.catalog.views.ProductView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class ProductControllerIT {

	private static final String PRODUCT_NAME = "iphone";
	private static final String PRODUCT_UPDATED_NAME = "iphone-15";
	private static final String CATEGORY_NAME = "tech";

	private static final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(100);
	private static final BigDecimal PRODUCT_UPDATED_PRICE = BigDecimal.valueOf(129);

	@SuppressWarnings("resource")
	@ClassRule
	public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
			DockerImageName.parse("postgres:16-alpine")).withUsername("test").withPassword("test");

	private static EntityManagerFactory emf;

	private EntityManager em;
	private ProductRepository productRepository;
	private ProductService productService;
	private JPATransactionManager transactionManager;
	private ProductController productController;
	private CategoryRepository categoryRepository;
	private CategoryService categoryService;
	private CategoryController categoryController;
	private AutoCloseable closeable;

	@Mock
	private CategoryView categoryView;
	@Mock
	private ProductView productView;

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
		transactionManager = new JPATransactionManager(em);
		productRepository = new JpaProductRepositoryImpl(em);

		categoryRepository = new JpaCategoryRepositoryImpl(em);
		categoryService = new CategoryService(categoryRepository, transactionManager, productRepository);

		productService = new ProductService(productRepository, categoryService, transactionManager);
		productController = new ProductController(productService, productView);
		categoryController = new CategoryController(categoryService, categoryView);
	}

	@After
	public void tearDown() throws Exception {
		closeable.close();
		em.close();
	}

	@Test
	public void shouldSaveProductInDatabase_whenControllerCreatesProductIT() {
		categoryController.create(CATEGORY_NAME);

		Category savedCategory = categoryRepository.findByName(CATEGORY_NAME);
		assertThat(savedCategory).isNotNull();
		assertThat(savedCategory.getName()).isEqualTo(CATEGORY_NAME);

		productController.create("iphone", PRODUCT_PRICE, savedCategory.getId());

		Product savedProduct = productRepository.findByName("iphone");
		assertThat(savedProduct).isNotNull();
		assertThat(savedProduct.getName()).isEqualTo("iphone");
		assertThat(savedProduct.getPrice()).isEqualByComparingTo(PRODUCT_PRICE);
		assertThat(savedProduct.getCategory().getId()).isEqualTo(savedCategory.getId());

		verify(productView).addedProduct(savedProduct);
	}

	@Test
	public void shouldReturnAllProductsFromDB_whenControllerCallsFindAllIT() {
		categoryController.create(CATEGORY_NAME);

		Category exisitingCategory = categoryRepository.findByName(CATEGORY_NAME);
		assertThat(exisitingCategory).isNotNull();

		productController.create(PRODUCT_NAME, PRODUCT_PRICE, exisitingCategory.getId());
		productController.create(PRODUCT_UPDATED_NAME, PRODUCT_PRICE, exisitingCategory.getId());

		productController.findAll();
		List<Product> products = productService.findAll();

		assertThat(products).hasSize(2);
		verify(productView).findAllProducts(products);
	}

	@Test
	public void shouldDeleteProductInDatabase_whenControllerDeletesProductIT() {
		categoryController.create(CATEGORY_NAME);
		Category exisitingCategory = categoryRepository.findByName(CATEGORY_NAME);

		productController.create(PRODUCT_NAME, PRODUCT_PRICE, exisitingCategory.getId());

		Product beforeDelete = productRepository.findByName(PRODUCT_NAME);

		assertThat(beforeDelete).isNotNull();
		assertThat(beforeDelete.getName()).isEqualTo(PRODUCT_NAME);

		Long id = beforeDelete.getId();
		productController.delete(id);

		assertThatThrownBy(() -> productService.findById(id)).isInstanceOf(RuntimeException.class);
		verify(productView).deletedProduct(beforeDelete);
	}

	@Test
	public void shouldUpdateProductInDatabase_whenControllerUpdatesProductIT() {
		categoryController.create(CATEGORY_NAME);
		Category existingCategory = categoryRepository.findByName(CATEGORY_NAME);

		productController.create(PRODUCT_NAME, PRODUCT_PRICE, existingCategory.getId());

		Product beforeUpdate = productRepository.findByName(PRODUCT_NAME);
		assertThat(beforeUpdate).isNotNull();
		assertThat(beforeUpdate.getName()).isEqualTo(PRODUCT_NAME);

		Long id = beforeUpdate.getId();
		productController.update(id, PRODUCT_UPDATED_NAME, PRODUCT_UPDATED_PRICE, existingCategory.getId());

		Product updatedProduct = productService.findById(id);
		assertThat(updatedProduct).isNotNull();
		assertThat(updatedProduct.getId()).isEqualTo(id);
		assertThat(updatedProduct.getName()).isEqualTo(PRODUCT_UPDATED_NAME);
		assertThat(updatedProduct.getPrice()).isEqualByComparingTo(PRODUCT_UPDATED_PRICE);
		assertThat(updatedProduct.getCategory().getId()).isEqualTo(existingCategory.getId());

		verify(productView).updateProduct(updatedProduct);
	}

}
