package io.github.dariopipa.tdd.catalog.transactionmanger;

import io.github.dariopipa.tdd.catalog.exceptions.JPARepoException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class JPATransactionManager implements TransactionManager {

	private final EntityManager entityManager;

	public JPATransactionManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public <T> T doInTransaction(TransactionCode<T> code) {
		EntityTransaction entityTransaction = entityManager.getTransaction();

		try {
			entityTransaction.begin();
			T result = code.execute();
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
