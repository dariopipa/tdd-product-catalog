package io.github.dariopipa.tdd.catalog.controllers;

import java.util.List;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.exceptions.CategoryNameAlreadyExistsExcpetion;
import io.github.dariopipa.tdd.catalog.exceptions.EntityNotFoundException;
import io.github.dariopipa.tdd.catalog.service.CategoryService;
import io.github.dariopipa.tdd.catalog.views.CategoryView;

public class CategoryController {

	private CategoryService categoryService;
	private CategoryView categoryView;

	public CategoryController(CategoryService categoryService, CategoryView categoryView) {
		this.categoryService = categoryService;
		this.categoryView = categoryView;
	}

	public void create(String name) {
		try {
			Long id = categoryService.createCategory(name);
			Category createdCategory = categoryService.findById(id);
			categoryView.addedCategory(createdCategory);

		} catch (IllegalArgumentException e) {
			categoryView.showError("Invalid input: " + e.getMessage());
		} catch (CategoryNameAlreadyExistsExcpetion e) {
			categoryView.showError("Category name already exists");
		}
	}

	public void delete(Long id) {
		try {
			Category deletedCategory = categoryService.findById(id);
			categoryService.delete(id);
			categoryView.deletedCategory(deletedCategory);
		} catch (EntityNotFoundException e) {
			categoryView.showError(e.getMessage());
		}
	}

	public void update(Long id, String newName) {
		try {
			Category updatedCategory = categoryService.update(id, newName);
			categoryView.updateCategory(updatedCategory);

		} catch (IllegalArgumentException e) {
			categoryView.showError("Invalid input: " + e.getMessage());
		} catch (EntityNotFoundException e) {
			categoryView.showError(e.getMessage());
		} catch (CategoryNameAlreadyExistsExcpetion e) {
			categoryView.showError("Category name already exists");
		}
	}

	public void findAll() {
		List<Category> categories = categoryService.findAll();
		categoryView.findAllCategories(categories);
	}

}
