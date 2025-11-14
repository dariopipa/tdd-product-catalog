package io.github.dariopipa.tdd.catalog.repository;

import java.util.List;

import io.github.dariopipa.tdd.catalog.entities.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class JpaProductRepositoryImpl implements ProductRepository {

	private EntityManager entityManager;

	public JpaProductRepositoryImpl(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public Long create(Product product) {
		entityManager.persist(product);
		return product.getId();
	}

	@Override
	public void delete(Product product) {
		entityManager.remove(product);
	}

	@Override
	public Product findById(Long id) {
		return entityManager.find(Product.class, id);
	}

	@Override
	public Product findByName(String name) {
		TypedQuery<Product> query = entityManager.createQuery("SELECT p FROM Product p WHERE p.name = :name",
				Product.class);

		query.setParameter("name", name);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Product update(Product product) {
		return entityManager.merge(product);
	}

	@Override
	public List<Product> findAll() {
		return entityManager.createQuery("SELECT p FROM Product p", Product.class).getResultList();
	}

}
