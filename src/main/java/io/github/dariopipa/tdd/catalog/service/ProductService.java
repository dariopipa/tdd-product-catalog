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

	private ProductRepository productRepository;
	private CategoryService categoryService;
	private TransactionManager transactionManager;

	public ProductService(ProductRepository productRepository, CategoryService categoryService,
			TransactionManager transactionManager) {

		this.productRepository = productRepository;
		this.categoryService = categoryService;
		this.transactionManager = transactionManager;
	}

	public Long create(String name, BigDecimal price, Long categoryId) {
		return transactionManager.doInTransaction(() -> {
			Category category = categoryService.findByIdInternal(categoryId);
			String normalized = validateAndNormalizeName(name);

			findByName(normalized);
			validatePrice(price);

			return productRepository.create(new Product(normalized, price, category));
		});
	}

	public Product findById(Long id) {
		validateId(id);

		return transactionManager.doInTransaction(() -> {
			Product resultProduct = productRepository.findById(id);
			if (resultProduct == null) {
				throw new EntityNotFoundException("product with id: " + id + " not found");
			}

			return resultProduct;
		});
	}

	public List<Product> findAll() {
		return transactionManager.doInTransaction(() -> productRepository.findAll());
	}

	public Product update(Long id, String name, BigDecimal price, Long categoryId) {
		return transactionManager.doInTransaction(() -> {
			Product existingProduct = this.findByIdInternal(id);
			Category category = categoryService.findByIdInternal(categoryId);

			String normalized = validateAndNormalizeName(name);
			findByName(normalized);
			validatePrice(price);

			existingProduct.setName(normalized);
			existingProduct.setPrice(price);
			existingProduct.setCategory(category);

			return productRepository.update(existingProduct);
		});
	}

	public void delete(Long id) {
		transactionManager.doInTransaction(() -> {
			Product existingProduct = findByIdInternal(id);

			productRepository.delete(existingProduct);
			return null;
		});
	}

	Product findByIdInternal(Long id) {
		validateId(id);
		Product p = productRepository.findById(id);
		if (p == null)
			throw new EntityNotFoundException("product with id: " + id + " not found");
		return p;
	}

	private void validateId(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("id must be provided");
		}
		if (id <= 0) {
			throw new IllegalArgumentException("id must be positive");
		}
	}

	private void findByName(String name) {
		Product result = productRepository.findByName(name);

		if (result != null) {
			throw new ProductNameAlreadyExistsExcpetion();
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

}
