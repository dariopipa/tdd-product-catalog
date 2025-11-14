package io.github.dariopipa.tdd.catalog.service;

import java.math.BigDecimal;
import java.util.List;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.entities.Product;
import io.github.dariopipa.tdd.catalog.exceptions.EntityNotFoundException;
import io.github.dariopipa.tdd.catalog.exceptions.ProductNameAlreadyExistsExcpetion;
import io.github.dariopipa.tdd.catalog.repository.ProductRepository;
import io.github.dariopipa.tdd.catalog.transactionmanager.TransactionManager;

public class ProductService {

	private CategoryService categoryService;
	private TransactionManager<ProductRepository> transactionManager;

	public ProductService(CategoryService categoryService, TransactionManager<ProductRepository> transactionManager) {
		this.categoryService = categoryService;
		this.transactionManager = transactionManager;
	}

	public Long create(String name, BigDecimal price, Long categoryId) {
		validatePrice(price);
		String normalizedName = validateAndNormalizeName(name);
		Category category = categoryService.findById(categoryId);

		return transactionManager.doInTransaction(repo -> {
			ensureProductNameIsAvaiable(repo, normalizedName);

			return repo.create(new Product(normalizedName, price, category));
		});
	}

	public Product findById(Long id) {
		validateId(id);

		return transactionManager.doInTransaction(repo -> productExists(repo, id));
	}

	public List<Product> findAll() {
		return transactionManager.doInTransaction(repo -> repo.findAll());
	}

	public Product update(Long id, String name, BigDecimal price, Long categoryId) {
		validateId(id);
		validatePrice(price);
		String normalizedName = validateAndNormalizeName(name);
		Category category = categoryService.findById(categoryId);

		return transactionManager.doInTransaction(repo -> {
			Product existingProduct = productExists(repo, id);

			ensureProductNameIsAvaiable(repo, normalizedName);
			existingProduct.setName(normalizedName);
			existingProduct.setPrice(price);
			existingProduct.setCategory(category);

			return repo.update(existingProduct);
		});
	}

	public void delete(Long id) {
		transactionManager.doInTransaction(repo -> {
			Product existingProduct = productExists(repo, id);

			repo.delete(existingProduct);
			return null;
		});
	}

	private void validateId(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("id must be provided");
		}
		if (id <= 0) {
			throw new IllegalArgumentException("id must be positive");
		}
	}

	private String validateAndNormalizeName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name must be provided");
		}

		String normalized = name.strip().toLowerCase();
		if (normalized.isBlank()) {
			throw new IllegalArgumentException("name must contain valid characters");
		}
		return normalized;
	}

	private void validatePrice(BigDecimal price) {
		if (price == null) {
			throw new IllegalArgumentException("price must be provided");
		}
		if (price.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("price must be positive");
		}
	}

	private Product productExists(ProductRepository repo, Long id) {
		Product existingProduct = repo.findById(id);
		if (existingProduct == null) {
			throw new EntityNotFoundException("product with id: " + id + " not found");
		}

		return existingProduct;
	}

	private void ensureProductNameIsAvaiable(ProductRepository repo, String name) {
		Product existingProduct = repo.findByName(name);
		if (existingProduct != null) {
			throw new ProductNameAlreadyExistsExcpetion();
		}
	}
}
