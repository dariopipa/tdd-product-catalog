package io.github.dariopipa.tdd.catalog.repository;

import java.util.List;

import io.github.dariopipa.tdd.catalog.entities.Product;

public interface ProductRepository {
	Long create(Product product);

	void delete(Product product);

	Product findById(Long id);

	Product findByName(String name);

	Product update(Product product);

	List<Product> findAll();

}
