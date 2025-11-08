package io.github.dariopipa.tdd.catalog.exceptions;

public class JPARepoException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public JPARepoException(String errorMessage) {
		super(errorMessage);
	}
}
