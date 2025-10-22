package io.github.dariopipa.tdd.catalog.transactionmanger;

public interface TransactionManager {
	<T> T doInTransaction(TransactionCode<T> code);
}