package io.github.dariopipa.tdd.catalog.controllers;

import java.math.BigDecimal;
import java.util.List;

import io.github.dariopipa.tdd.catalog.entities.Product;
import io.github.dariopipa.tdd.catalog.exceptions.EntityNotFoundException;
import io.github.dariopipa.tdd.catalog.exceptions.ProductNameAlreadyExistsExcpetion;
import io.github.dariopipa.tdd.catalog.service.ProductService;
import io.github.dariopipa.tdd.catalog.views.ProductView;

public class ProductController {

	private ProductService productService;
	private ProductView productView;

	public ProductController(ProductService productService, ProductView productView) {
		this.productService = productService;
		this.productView = productView;
	}

	public void create(String name, BigDecimal price, Long categoryId) {
		try {
			Long id = productService.create(name, price, categoryId);
			Product product = productService.findById(id);
			productView.addedProduct(product);
		} catch (ProductNameAlreadyExistsExcpetion e) {
			productView.showError("Invalid input: Product name already exists");
		} catch (Exception e) {
			productView.showError("Invalid input: " + e.getMessage());
		}
	}

	public void delete(Long id) {
		try {
			Product deletedProduct = productService.findById(id);
			productService.delete(id);
			productView.deletedProduct(deletedProduct);
		} catch (EntityNotFoundException e) {
			productView.showError(e.getMessage());
		}
	}

	public void findAll() {
		List<Product> products = productService.findAll();
		productView.findAllProducts(products);
	}

	public void update(Long id, String newName, BigDecimal valueOf, Long categoryId) {
		try {
			Product updatedProduct = productService.update(id, newName, valueOf, categoryId);
			productView.updateProduct(updatedProduct);
		} catch (ProductNameAlreadyExistsExcpetion e) {
			productView.showError("Invalid input: Product name already exists");
		} catch (Exception e) {
			productView.showError("Invalid input: " + e.getMessage());
		}
	}

}
