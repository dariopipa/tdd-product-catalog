package io.github.dariopipa.tdd.catalog.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
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

	@Mock
	private CategoryView categoryView;
	@Mock
	private ProductView productView;

	private BigDecimal price = BigDecimal.valueOf(100);

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

	@Test
	public void shouldSaveProductInDatabase_whenControllerCreatesProductIT() {
		String categoryName = "tech";
		categoryController.create(categoryName);

		Category savedCategory = categoryRepository.findByName(categoryName);
		assertThat(savedCategory).isNotNull();
		assertThat(savedCategory.getName()).isEqualTo(categoryName);

		productController.create("iphone", price, savedCategory.getId());

		Product savedProduct = productRepository.findByName("iphone");
		assertThat(savedProduct).isNotNull();
		assertThat(savedProduct.getName()).isEqualTo("iphone");
		assertThat(savedProduct.getPrice()).isEqualByComparingTo(price);
		assertThat(savedProduct.getCategory().getId()).isEqualTo(savedCategory.getId());

		verify(productView).addedProduct(savedProduct);
	}

	@Test
	public void shouldReturnAllProductsFromDB_whenControllerCallsFindAllIT() {
		String categoryName = "integration-tech";
		categoryController.create(categoryName);

		Category exisitingCategory = categoryRepository.findByName(categoryName);
		assertThat(exisitingCategory).isNotNull();

		productController.create("iphone", price, exisitingCategory.getId());
		productController.create("pc", price, exisitingCategory.getId());

		productController.findAll();
		List<Product> products = productService.findAll();

		assertThat(products).hasSize(2);
		verify(productView).findAllProducts(products);
	}

	@Test
	public void shouldDeleteProductInDatabase_whenControllerDeletesProductIT() {
		String categoryName = "category";
		categoryController.create(categoryName);
		Category exisitingCategory = categoryRepository.findByName(categoryName);

		String productName = "product to delete";
		productController.create(productName, price, exisitingCategory.getId());

		Product beforeDelete = productRepository.findByName(productName);

		assertThat(beforeDelete).isNotNull();
		assertThat(beforeDelete.getName()).isEqualTo(productName);

		Long id = beforeDelete.getId();
		productController.delete(id);

		assertThatThrownBy(() -> productService.findById(id)).isInstanceOf(RuntimeException.class);
		verify(productView).deletedProduct(beforeDelete);
	}

	@Test
	public void shouldUpdateProductInDatabase_whenControllerUpdatesProductIT() {
		String categoryName = "tech";
		categoryController.create(categoryName);
		Category existingCategory = categoryRepository.findByName(categoryName);

		String name = "iphone";
		productController.create("iphone", price, existingCategory.getId());

		Product beforeUpdate = productRepository.findByName(name);
		assertThat(beforeUpdate).isNotNull();
		assertThat(beforeUpdate.getName()).isEqualTo(name);

		Long id = beforeUpdate.getId();
		String updatedName = "iphone-15";
		BigDecimal updatedPrice = BigDecimal.valueOf(129);

		productController.update(id, updatedName, updatedPrice, existingCategory.getId());

		Product updatedProduct = productService.findById(id);
		assertThat(updatedProduct).isNotNull();
		assertThat(updatedProduct.getId()).isEqualTo(id);
		assertThat(updatedProduct.getName()).isEqualTo(updatedName);
		assertThat(updatedProduct.getPrice()).isEqualByComparingTo(updatedPrice);
		assertThat(updatedProduct.getCategory().getId()).isEqualTo(existingCategory.getId());

		verify(productView).updateProduct(updatedProduct);
	}

}
