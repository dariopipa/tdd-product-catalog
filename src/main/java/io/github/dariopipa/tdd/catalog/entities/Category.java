package io.github.dariopipa.tdd.catalog.entities;

import java.util.Objects;

public class Category {

	private Long id;

	private String name;

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Category [name=" + name + "]";
	}

	public Category(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name must be valid");
		}

		this.name = name.toLowerCase().trim();
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Category other = (Category) obj;
		return Objects.equals(id, other.id) && Objects.equals(name, other.name);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
