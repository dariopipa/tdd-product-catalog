package io.github.dariopipa.tdd.catalog.service;

import io.github.dariopipa.tdd.catalog.entities.Category;

public interface CategoryRepository {

	Long create(Category category);

	String delete(Long id);

	Category findById(Long id);

	Category findByName(String name);

	Category update(Long id, String name);
}
