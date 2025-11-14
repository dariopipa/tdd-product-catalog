package io.github.dariopipa.tdd.catalog.transactionmanager;

public interface TransactionManager<R> {
	<T> T doInTransaction(TransactionCode<R, T> code);
}