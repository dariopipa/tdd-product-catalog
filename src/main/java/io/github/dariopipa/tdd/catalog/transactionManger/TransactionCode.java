package io.github.dariopipa.tdd.catalog.transactionManger;

@FunctionalInterface
public interface TransactionCode<T> {
	T execute() throws Exception;
}
