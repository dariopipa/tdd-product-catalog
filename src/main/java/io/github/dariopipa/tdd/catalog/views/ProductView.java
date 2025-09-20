package io.github.dariopipa.tdd.catalog.views;

import java.util.List;

import io.github.dariopipa.tdd.catalog.entities.Product;

public interface ProductView {
	void addedProduct(Product product);

	void showError(String msg);

	void deletedProduct(Product product);

	void updateProduct(Product product);

	void findAll(List<Product> products);
}
