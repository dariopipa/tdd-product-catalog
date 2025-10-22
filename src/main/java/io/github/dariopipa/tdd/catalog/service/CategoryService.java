package io.github.dariopipa.tdd.catalog.service;

import java.util.List;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.exceptions.CategoryInUseException;
import io.github.dariopipa.tdd.catalog.exceptions.CategoryNameAlreadyExistsExcpetion;
import io.github.dariopipa.tdd.catalog.exceptions.EntityNotFoundException;
import io.github.dariopipa.tdd.catalog.repository.CategoryRepository;
import io.github.dariopipa.tdd.catalog.repository.ProductRepository;
import io.github.dariopipa.tdd.catalog.transactionmanger.TransactionManager;

public class CategoryService {

	private CategoryRepository categoryRepository;
	private TransactionManager transactionManager;
	private ProductRepository productRepository;

	public CategoryService(CategoryRepository categoryRepository, TransactionManager transactionManager,
			ProductRepository productRepository) {
		this.categoryRepository = categoryRepository;
		this.transactionManager = transactionManager;
		this.productRepository = productRepository;
	}

	public Long createCategory(String name) {
		return transactionManager.doInTransaction(() -> {
			String normalizedName = validateAndNormalizeName(name);

			findByName(normalizedName);

			Category category = new Category(normalizedName);
			return categoryRepository.create(category);
		});
	}

	public String delete(Long id) {
		return transactionManager.doInTransaction(() -> {
			Category existingCategory = findByIdInternal(id);

			long used = productRepository.countByCategoryId(existingCategory.getId());
			if (used > 0) {
				throw new CategoryInUseException();
			}

			return categoryRepository.delete(existingCategory);
		});
	}

	public List<Category> findAll() {
		return transactionManager.doInTransaction(() -> categoryRepository.findAll());
	}

	public Category findById(Long id) {
		return transactionManager.doInTransaction(() -> findByIdInternal(id));
	}

	Category findByIdInternal(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("id must be provided");
		}
		if (id <= 0) {
			throw new IllegalArgumentException("id must be positive");
		}

		Category result = categoryRepository.findById(id);
		if (result == null) {
			throw new EntityNotFoundException("category with id:" + id + " not found");
		}

		return result;
	}

	public Category update(Long entityId, String name) {
		Category existingCategory = findByIdInternal(entityId);

		return transactionManager.doInTransaction(() -> {
			String normalizedName = validateAndNormalizeName(name);
			findByName(normalizedName);

			existingCategory.setName(normalizedName);

			return categoryRepository.update(existingCategory);
		});
	}

	void findByName(String name) {
		Category result = categoryRepository.findByName(name);
		if (result != null) {
			throw new CategoryNameAlreadyExistsExcpetion();
		}
	}

	private String validateAndNormalizeName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name must be provided");
		}

		String normalized = name.strip().toLowerCase();

		if (normalized.isBlank()) {
			throw new IllegalArgumentException("name must be provided");
		}
		return normalized;
	}

}
