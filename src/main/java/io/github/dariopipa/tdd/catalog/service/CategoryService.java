package io.github.dariopipa.tdd.catalog.service;

import java.util.List;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.exceptions.CategoryInUseException;
import io.github.dariopipa.tdd.catalog.exceptions.CategoryNameAlreadyExistsExcpetion;
import io.github.dariopipa.tdd.catalog.exceptions.EntityNotFoundException;
import io.github.dariopipa.tdd.catalog.repository.CategoryRepository;
import io.github.dariopipa.tdd.catalog.transactionmanager.TransactionManager;

public class CategoryService {

	private TransactionManager<CategoryRepository> transactionManager;

	public CategoryService(TransactionManager<CategoryRepository> transactionManager) {
		this.transactionManager = transactionManager;
	}

	public Long createCategory(String name) {
		String normalizedName = validateAndNormalizeName(name);

		return transactionManager.doInTransaction(repo -> {
			ensureCategoryNameIsAvaiable(repo, normalizedName);

			Category category = new Category(normalizedName);
			return repo.create(category);
		});
	}

	public String delete(Long id) {
		validateId(id);
		return transactionManager.doInTransaction(repo -> {
			Category existingCategory = categoryExists(repo, id);

			long used = repo.countProductsByCategoryId(id);
			if (used > 0) {
				throw new CategoryInUseException();
			}

			return repo.delete(existingCategory);
		});
	}

	public List<Category> findAll() {
		return transactionManager.doInTransaction(repo -> repo.findAll());
	}

	public Category findById(Long id) {
		validateId(id);
		return transactionManager.doInTransaction(repo -> categoryExists(repo, id));
	}

	public Category update(Long entityId, String name) {
		String normalizedName = validateAndNormalizeName(name);

		return transactionManager.doInTransaction(repo -> {
			Category existingCategory = categoryExists(repo, entityId);
			ensureCategoryNameIsAvaiable(repo, normalizedName);

			existingCategory.setName(normalizedName);
			return repo.update(existingCategory);
		});
	}

	private Category categoryExists(CategoryRepository repo, Long id) {
		Category categoryExists = repo.findById(id);
		if (categoryExists == null) {
			throw new EntityNotFoundException("category with id:" + id + " not found");
		}
		return categoryExists;
	}

	private void ensureCategoryNameIsAvaiable(CategoryRepository repo, String name) {
		Category categoryExists = repo.findByName(name);
		if (categoryExists != null) {
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

	private void validateId(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("id must be provided");
		}
		if (id <= 0) {
			throw new IllegalArgumentException("id must be positive");
		}
	}

}
