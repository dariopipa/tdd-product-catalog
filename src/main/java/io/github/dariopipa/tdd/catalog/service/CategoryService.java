package io.github.dariopipa.tdd.catalog.service;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.exceptions.CategoryNameAlreadyExistsExcpetion;
import io.github.dariopipa.tdd.catalog.exceptions.EntityNotFoundException;

public class CategoryService {

	private CategoryRepository categoryRepository;

	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	public Long createCategory(String name) {
		findByName(name);

		Category category = new Category(name);
		return categoryRepository.create(category);
	}

	public String delete(Long id) {
		findById(id);

		return categoryRepository.delete(id);
	}

	public Category findById(Long id) {
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
	}

	public Category update(Long entity_Id, String name) {
		findById(entity_Id);
		String normalizedName = validateAndNormalizeName(name);
		findByName(normalizedName);

		Category result = categoryRepository.update(entity_Id, normalizedName);

		return result;
	}

	void findByName(String name) {
		Category result = categoryRepository.findByName(name);
		if (result != null) {
			throw new CategoryNameAlreadyExistsExcpetion();
		}
	}

	private String validateAndNormalizeName(String raw) {
		if (raw == null) {
			throw new IllegalArgumentException("name must be provided");
		}

		String normalized = raw.strip().toLowerCase();

		if (normalized.isBlank()) {
			throw new IllegalArgumentException("name must be provided");
		}
		return normalized;
	}

}
