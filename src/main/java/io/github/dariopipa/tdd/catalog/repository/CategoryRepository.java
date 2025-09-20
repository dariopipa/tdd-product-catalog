package io.github.dariopipa.tdd.catalog.repository;

import java.util.List;

import io.github.dariopipa.tdd.catalog.entities.Category;

public interface CategoryRepository {

	Long create(Category category);

	String delete(Category category);

	Category findById(Long id);

	Category findByName(String name);

	Category update(Category category);

	List<Category> findAll();
}
