package io.github.dariopipa.tdd.catalog.transactionmanager;

import java.util.function.Function;

import io.github.dariopipa.tdd.catalog.exceptions.JPARepoException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class JPATransactionManager<R> implements TransactionManager<R> {

	private final EntityManager entityManager;
	private final Function<EntityManager, R> repoFactory;

	public JPATransactionManager(EntityManager entityManager, Function<EntityManager, R> repoFactory) {
		this.entityManager = entityManager;
		this.repoFactory = repoFactory;
	}

	@Override
	public <T> T doInTransaction(TransactionCode<R, T> code) {
		EntityTransaction entityTransaction = entityManager.getTransaction();

		try {
			entityTransaction.begin();

			R repo = repoFactory.apply(entityManager);
			T result = code.apply(repo);

			entityTransaction.commit();
			return result;
		} catch (JPARepoException e) {
			entityTransaction.rollback();
			throw new JPARepoException(e.getMessage());
		} catch (RuntimeException e) {
			entityTransaction.rollback();
			throw e;
		}
	}
}
