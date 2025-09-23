package io.github.dariopipa.tdd.catalog.views;

import java.util.List;

import io.github.dariopipa.tdd.catalog.entities.Category;

public interface CategoryView {

	void addedCategory(Category category);

	void showError(String msg);

	void deletedCategory(Category category);

	void updateCategory(Category category);

	void findAllCategories(List<Category> categories);
}
