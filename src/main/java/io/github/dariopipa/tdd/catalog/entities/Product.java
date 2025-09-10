package io.github.dariopipa.tdd.catalog.entities;

import java.math.BigDecimal;
import java.util.Objects;

public class Product {

	private String name;
	private BigDecimal price;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, price);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Product other = (Product) obj;
		return Objects.equals(name, other.name) && Objects.equals(price, other.price);
	}

	public Product(String name, BigDecimal price) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name must be valid");
		}
		if (price == null) {
			throw new IllegalArgumentException("price must be added");
		}
		if (price.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("price must be positive");
		}

		this.name = name.toLowerCase().trim();
		this.price = price;
	}
}
