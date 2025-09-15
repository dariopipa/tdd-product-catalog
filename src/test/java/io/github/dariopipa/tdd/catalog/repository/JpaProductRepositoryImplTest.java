package io.github.dariopipa.tdd.catalog.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.entities.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class JpaProductRepositoryImplTest {

	private JpaProductRepositoryImpl jpaProductRepositoryImpl;
	private EntityManager entityManager;
	private Category category;
	private TypedQuery<Product> query;

	@Before
	public void setup() {
		entityManager = mock(EntityManager.class);
		jpaProductRepositoryImpl = new JpaProductRepositoryImpl(entityManager);
		category = new Category("Category");
		query = mock(TypedQuery.class);

	}

	@Test
	public void test_createProductEntityInDatabase() {
		Product product = new Product("product", BigDecimal.ZERO, category);
		product.setId(12L);

		Long result = jpaProductRepositoryImpl.create(product);

		verify(entityManager, times(1)).persist(product);
		assertThat(result).isEqualTo(12L);
	}

	@Test
	public void test_findByExistingId_shouldReturnEntity() {
		Long productId = 12L;
		Product expectedProduct = new Product("product", BigDecimal.ZERO, category);
		expectedProduct.setId(productId);

		when(entityManager.find(Product.class, productId)).thenReturn(expectedProduct);
		Product result = jpaProductRepositoryImpl.findById(productId);

		assertThat(result).isEqualTo(expectedProduct);
		assertThat(result.getId()).isEqualTo(productId);
		verify(entityManager).find(Product.class, productId);
	}

	@Test
	public void test_findByNonExistingId_shouldReturnNull() {
		Long nonExistentId = 999L;

		when(entityManager.find(Product.class, nonExistentId)).thenReturn(null);
		Product result = jpaProductRepositoryImpl.findById(nonExistentId);

		assertThat(result).isNull();
		verify(entityManager).find(Product.class, nonExistentId);
	}

	@Test
	public void test_deleteById_shouldReturnVoid() {
		Long existentId = 1L;
		Product expectedProduct = new Product("product", BigDecimal.ZERO, category);

		when(entityManager.find(Product.class, existentId)).thenReturn(expectedProduct);
		jpaProductRepositoryImpl.delete(expectedProduct);

		verify(entityManager).remove(expectedProduct);
	}

	@Test
	public void test_updateCategory_shouldReturnUpdatedProduct() {
		Product updatedProduct = new Product("product", BigDecimal.ZERO, category);

		when(entityManager.merge(updatedProduct)).thenReturn(updatedProduct);

		Product result = jpaProductRepositoryImpl.update(updatedProduct);

		assertThat(result).isEqualTo(updatedProduct);
		verify(entityManager, times(1)).merge(updatedProduct);
	}

	@Test
	public void test_findByName_shouldReturnFoundProduct() {
		String productName = "product";
		Product expectedProduct = new Product("product", BigDecimal.ZERO, category);

		when(entityManager.createQuery("SELECT p FROM Product p WHERE p.name = :name", Product.class))
				.thenReturn(query);
		when(query.setParameter("name", productName)).thenReturn(query);
		when(query.getSingleResult()).thenReturn(expectedProduct);

		Product result = jpaProductRepositoryImpl.findByName(productName);

		assertThat(result).isEqualTo(expectedProduct);
		assertThat(result.getName()).isEqualTo(productName);

		verify(entityManager).createQuery("SELECT p FROM Product p WHERE p.name = :name", Product.class);
		verify(query).setParameter("name", productName);
		verify(query).getSingleResult();
	}

	@Test
	public void test_findByNonExistentName_shouldReturnNull() {
		String nonExistentProductName = "does not exist";

		when(entityManager.createQuery("SELECT p FROM Product p WHERE p.name = :name", Product.class))
				.thenReturn(query);
		when(query.setParameter("name", nonExistentProductName)).thenReturn(query);
		when(query.getSingleResult()).thenThrow(NoResultException.class);

		Product result = jpaProductRepositoryImpl.findByName(nonExistentProductName);

		assertThat(result).isEqualTo(null);
	}

	@Test
	public void test_findAll_shouldReturnListOfProducts() {
		List<Product> expected = Arrays.asList(new Product("product1", BigDecimal.ZERO, category),
				new Product("product2", BigDecimal.ZERO, category));

		when(entityManager.createQuery("SELECT p FROM Product p", Product.class)).thenReturn(query);
		when(query.getResultList()).thenReturn(expected);

		List<Product> result = jpaProductRepositoryImpl.findAll();

		assertThat(result).isSameAs(expected);
		verify(entityManager).createQuery("SELECT p FROM Product p", Product.class);
		verify(query).getResultList();

	}
}