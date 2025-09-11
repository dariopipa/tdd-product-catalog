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

	public String delete(long id) {
		findById(id);

		return categoryRepository.delete(id);
	}

	public Category findById(long id) {
		Category result = categoryRepository.findById(id);
		if (result == null) {
			throw new EntityNotFoundException("category with id:" + id + "not found");
		}

		return result;
	}

	public Category update(long entity_Id, String name) {
		findById(entity_Id);
		findByName(name);

		Category result = categoryRepository.update(entity_Id, name);

		return result;
	}

	void findByName(String name) {
		Category result = categoryRepository.findByName(name);
		if (result != null) {
			throw new CategoryNameAlreadyExistsExcpetion();
		}
	}

}
