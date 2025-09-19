package io.github.dariopipa.tdd.catalog.transactionManger;

public interface TransactionManager {
	<T> T doInTransaction(TransactionCode<T> code);
}