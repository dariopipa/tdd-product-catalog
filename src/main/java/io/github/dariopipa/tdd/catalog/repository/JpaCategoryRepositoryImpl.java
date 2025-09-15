package io.github.dariopipa.tdd.catalog.repository;

import io.github.dariopipa.tdd.catalog.entities.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class JpaCategoryRepositoryImpl implements CategoryRepository {

	private EntityManager entityManager;

	public JpaCategoryRepositoryImpl(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public Long create(Category category) {
		entityManager.persist(category);
		return category.getId();
	}

	@Override
	public String delete(Category existingCategory) {
		entityManager.remove(existingCategory);

		return "Deleted";
	}

	@Override
	public Category findById(Long id) {
		return entityManager.find(Category.class, id);
	}

	@Override
	public Category findByName(String name) {
		TypedQuery<Category> query = entityManager.createQuery("SELECT c FROM Category c WHERE c.name = :name",
				Category.class);

		query.setParameter("name", name);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Category update(Category category) {

		return entityManager.merge(category);
	}

}
