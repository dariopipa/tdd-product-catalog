package io.github.dariopipa.tdd.catalog.transactionmanager;

import java.util.function.Function;

@FunctionalInterface
public interface TransactionCode<R, T> extends Function<R, T> {
}
