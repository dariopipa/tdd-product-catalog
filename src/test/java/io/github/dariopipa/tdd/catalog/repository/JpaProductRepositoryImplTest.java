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

	private static final String PU_NAME = "product-catalog-IM-PU";

	private static final long MISSING_ID = 999L;
	private static final BigDecimal PRICE_100 = BigDecimal.valueOf(100);
	private static final BigDecimal PRICE_ZERO = BigDecimal.ZERO;

	private static final String PRODUCT_NAME = "laptop";
	private static final String IPHONE_PRODUCT_NAME = "iphone";
	private static final String CATEGORY_NAME = "electronics";

	private EntityManagerFactory entityManagerFactory;
	private JpaProductRepositoryImpl jpaProductRepositoryImpl;
	private EntityManager entityManager;
	private EntityTransaction transaction;
	private Category category;

	@Before
	public void setup() {
		entityManagerFactory = Persistence.createEntityManagerFactory(PU_NAME);
		entityManager = entityManagerFactory.createEntityManager();
		jpaProductRepositoryImpl = new JpaProductRepositoryImpl(entityManager);
		transaction = entityManager.getTransaction();

		category = new Category(CATEGORY_NAME);
		transaction.begin();
		entityManager.persist(category);
		transaction.commit();
	}

	@Test
	public void test_createProductEntityInDatabase() {
		Product product = new Product(PRODUCT_NAME, PRICE_100, category);

		transaction.begin();
		Long result = jpaProductRepositoryImpl.create(product);
		transaction.commit();

		assertThat(result).isNotNull();
		assertThat(product.getId()).isEqualTo(result);

		Product found = entityManager.find(Product.class, result);
		assertThat(found).isNotNull();
		assertThat(found.getName()).isEqualTo(PRODUCT_NAME);
		assertThat(found.getPrice()).isEqualTo(PRICE_100);
	}

	@Test
	public void test_findByExistingId_shouldReturnEntity() {
		Product product = new Product(PRODUCT_NAME, PRICE_100, category);
		transaction.begin();
		entityManager.persist(product);
		transaction.commit();

		Long productId = product.getId();
		Product result = jpaProductRepositoryImpl.findById(productId);

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(PRODUCT_NAME);
		assertThat(result.getPrice()).isEqualTo(PRICE_100);
		assertThat(result.getId()).isEqualTo(productId);
		assertThat(result.getCategory().getName()).isEqualTo(CATEGORY_NAME);
	}

	@Test
	public void test_findByNonExistingId_shouldReturnNull() {
		Product result = jpaProductRepositoryImpl.findById(MISSING_ID);

		assertThat(result).isNull();
	}

	@Test
	public void test_deleteById_shouldReturnVoid() {
		Product product = new Product(PRODUCT_NAME, PRICE_100, category);

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
		Product product = new Product(PRODUCT_NAME, PRICE_ZERO, category);

		transaction.begin();
		entityManager.persist(product);
		transaction.commit();
		Long productId = product.getId();

		product.setName("updated product name");
		product.setPrice(PRICE_100);

		transaction.begin();
		Product result = jpaProductRepositoryImpl.update(product);
		transaction.commit();

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("updated product name");
		assertThat(result.getPrice()).isEqualTo(PRICE_100);

		Product found = entityManager.find(Product.class, productId);
		assertThat(found).isNotNull();
		assertThat(found.getName()).isEqualTo("updated product name");
		assertThat(found.getPrice()).isEqualTo(PRICE_100);
	}

	@Test
	public void test_findByName_shouldReturnFoundProduct() {
		Product product = new Product(PRODUCT_NAME, PRICE_100, category);
		transaction.begin();
		entityManager.persist(product);
		transaction.commit();

		Product result = jpaProductRepositoryImpl.findByName(PRODUCT_NAME);

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(PRODUCT_NAME);
		assertThat(result.getPrice()).isEqualTo(PRICE_100);
		assertThat(result.getId()).isEqualTo(product.getId());
		assertThat(result.getCategory().getName()).isEqualTo(CATEGORY_NAME);
	}

	@Test
	public void test_findByNonExistentName_shouldReturnNull() {
		Product result = jpaProductRepositoryImpl.findByName("does not exist");

		assertThat(result).isNull();
	}

	@Test
	public void test_findAll_shouldReturnListOfProducts() {
		Product product1 = new Product(PRODUCT_NAME, PRICE_100, category);
		Product product2 = new Product(IPHONE_PRODUCT_NAME, PRICE_100, category);

		transaction.begin();
		entityManager.persist(product1);
		entityManager.persist(product2);
		transaction.commit();

		List<Product> result = jpaProductRepositoryImpl.findAll();

		assertThat(result).hasSize(2).contains(product1, product2);
	}

}
