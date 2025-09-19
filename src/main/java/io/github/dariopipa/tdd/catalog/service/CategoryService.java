package io.github.dariopipa.tdd.catalog.service;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.exceptions.CategoryNameAlreadyExistsExcpetion;
import io.github.dariopipa.tdd.catalog.exceptions.EntityNotFoundException;
import io.github.dariopipa.tdd.catalog.repository.CategoryRepository;
import io.github.dariopipa.tdd.catalog.transactionManger.TransactionManager;

public class CategoryService {

	private CategoryRepository categoryRepository;
	private TransactionManager transactionManager;

	public CategoryService(CategoryRepository categoryRepository, TransactionManager transactionManager) {
		this.categoryRepository = categoryRepository;
		this.transactionManager = transactionManager;
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
			Category existingCategory = findById(id);
			return categoryRepository.delete(existingCategory);
		});
	}

	public Category findById(Long id) {
		return transactionManager.doInTransaction(() -> {
			if (id == null) {
				throw new IllegalArgumentException("id must be provided");
			}
			if (id <= 0) {
				throw new IllegalArgumentException("id must be positive");
			}

			Category result = categoryRepository.findById(id);
			if (result == null) {
				throw new EntityNotFoundException("category with id:" + id + "not found");
			}

			return result;
		});
	}

	public Category update(Long entity_Id, String name) {
		Category existingCategory = findById(entity_Id);

		return transactionManager.doInTransaction(() -> {
			String normalizedName = validateAndNormalizeName(name);
			findByName(normalizedName);

			existingCategory.setName(normalizedName);

			Category result = categoryRepository.update(existingCategory);
			return result;
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
