package io.github.dariopipa.tdd.catalog.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.entities.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class JpaProductRepositoryImplTest {

	private EntityManagerFactory entityManagerFactory;
	private JpaProductRepositoryImpl jpaProductRepositoryImpl;
	private EntityManager entityManager;
	private EntityTransaction transaction;
	private Category category;
	private Long nonExistentId = 999L;
	private BigDecimal productPrice = BigDecimal.valueOf(100);
	private String productName = "laptop";

	@Before
	public void setup() {
		entityManagerFactory = Persistence.createEntityManagerFactory("product-catalog-IM-PU");
		entityManager = entityManagerFactory.createEntityManager();
		jpaProductRepositoryImpl = new JpaProductRepositoryImpl(entityManager);
		transaction = entityManager.getTransaction();

		category = new Category("electronics");
		transaction.begin();
		entityManager.persist(category);
		transaction.commit();
	}

	@Test
	public void test_createProductEntityInDatabase() {
		Product product = new Product(productName, productPrice, category);

		transaction.begin();
		Long result = jpaProductRepositoryImpl.create(product);
		transaction.commit();

		assertThat(result).isNotNull();
		assertThat(product.getId()).isEqualTo(result);

		Product found = entityManager.find(Product.class, result);
		assertThat(found).isNotNull();
		assertThat(found.getName()).isEqualTo("laptop");
		assertThat(found.getPrice()).isEqualTo(productPrice);
	}

	@Test
	public void test_findByExistingId_shouldReturnEntity() {
		Product product = new Product(productName, productPrice, category);
		transaction.begin();
		entityManager.persist(product);
		transaction.commit();

		Long productId = product.getId();
		Product result = jpaProductRepositoryImpl.findById(productId);

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("laptop");
		assertThat(result.getPrice()).isEqualTo(productPrice);
		assertThat(result.getId()).isEqualTo(productId);
		assertThat(result.getCategory().getName()).isEqualTo("electronics");
	}

	@Test
	public void test_findByNonExistingId_shouldReturnNull() {
		Product result = jpaProductRepositoryImpl.findById(nonExistentId);

		assertThat(result).isNull();
	}

	@Test
	public void test_deleteById_shouldReturnVoid() {
		Product product = new Product(productName, productPrice, category);

		transaction.begin();
		entityManager.persist(product);
		transaction.commit();
		Long productId = product.getId();

		transaction.begin();
		jpaProductRepositoryImpl.delete(product);
		transaction.commit();

		Product found = entityManager.find(Product.class, productId);
		assertThat(found).isNull();
	}

	@Test
	public void test_updateCategory_shouldReturnUpdatedProduct() {
		Product product = new Product(productName, BigDecimal.ZERO, category);

		transaction.begin();
		entityManager.persist(product);
		transaction.commit();
		Long productId = product.getId();

		product.setName("updated product name");
		product.setPrice(productPrice);

		transaction.begin();
		Product result = jpaProductRepositoryImpl.update(product);
		transaction.commit();

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("updated product name");
		assertThat(result.getPrice()).isEqualTo(productPrice);

		Product found = entityManager.find(Product.class, productId);
		assertThat(found).isNotNull();
		assertThat(found.getName()).isEqualTo("updated product name");
		assertThat(found.getPrice()).isEqualTo(productPrice);
	}

	@Test
	public void test_findByName_shouldReturnFoundProduct() {
		Product product = new Product(productName, productPrice, category);
		transaction.begin();
		entityManager.persist(product);
		transaction.commit();

		Product result = jpaProductRepositoryImpl.findByName(productName);

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(productName);
		assertThat(result.getPrice()).isEqualTo(productPrice);
		assertThat(result.getId()).isEqualTo(product.getId());
		assertThat(result.getCategory().getName()).isEqualTo("electronics");
	}

	@Test
	public void test_findByNonExistentName_shouldReturnNull() {
		String nonExistentProductName = "does not exist";

		Product result = jpaProductRepositoryImpl.findByName(nonExistentProductName);

		assertThat(result).isNull();
	}

	@Test
	public void test_findAll_shouldReturnListOfProducts() {
		Product product1 = new Product(productName, productPrice, category);
		Product product2 = new Product("iphone", BigDecimal.valueOf(1.111111), category);

		transaction.begin();
		entityManager.persist(product1);
		entityManager.persist(product2);
		transaction.commit();

		List<Product> result = jpaProductRepositoryImpl.findAll();

		assertThat(result).hasSize(2);
		assertThat(result).contains(product1, product2);
	}
}