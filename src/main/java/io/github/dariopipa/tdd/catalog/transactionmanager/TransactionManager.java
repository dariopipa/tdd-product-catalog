package io.github.dariopipa.tdd.catalog.transactionmanager;

public interface TransactionManager {
	<T> T doInTransaction(TransactionCode<T> code);
}