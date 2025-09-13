package io.github.dariopipa.tdd.catalog.service;

import java.math.BigDecimal;
import java.util.List;

import io.github.dariopipa.tdd.catalog.entities.Product;

public interface ProductRepository {
	Long create(Product Product);

	void delete(Long id);

	Product findById(Long id);

	Product findByName(String name);

	Product update(Long id, String name, BigDecimal price, Long categoryId);

	List<Product> findAll();
}
