package io.github.dariopipa.tdd.catalog.transactionManger;

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
		} catch (RuntimeException e) {
			entityTransaction.rollback();
			throw e;
		} catch (Exception e) {
			entityTransaction.rollback();
			throw new RuntimeException(e);
		}
	}
}
