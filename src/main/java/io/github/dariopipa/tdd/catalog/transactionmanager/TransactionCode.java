package io.github.dariopipa.tdd.catalog.transactionmanager;

import io.github.dariopipa.tdd.catalog.exceptions.JPARepoException;

@FunctionalInterface
public interface TransactionCode<T> {
	T execute() throws JPARepoException;
}
