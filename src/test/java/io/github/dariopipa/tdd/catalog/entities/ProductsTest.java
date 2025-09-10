package io.github.dariopipa.tdd.catalog.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.Test;

public class ProductsTest {

	@Test
	public void test_create_ProductEntity_WithNameAndPrice() {
		Product product = new Product("Laptop", new BigDecimal("1.11"));
		assertThat(product.getName()).isEqualTo("laptop");
		assertThat(product.getPrice()).isEqualByComparingTo("1.11");
	}

	@Test
	public void test_throw_IllegalArgumentException_WhenPriceIsNull() {
		assertThatThrownBy(() -> new Product("Laptop", null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("price must be added");
	}

	@Test
	public void test_throw_IllegalArgumentException_WhenPriceIsNegative() {
		assertThatThrownBy(() -> new Product("Laptop", new BigDecimal("-10")))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("price must be positive");
	}

	@Test
	public void test_create_Product_WithPriceZero() {
		Product product = new Product("Laptop", new BigDecimal("0"));
		assertThat(product.getName()).isEqualTo("laptop");
		assertThat(product.getPrice()).isEqualByComparingTo("0");
	}

	@Test
	public void test_throwIllegalArgumentException_WhenNameIsNull() {
		assertThatThrownBy(() -> new Product(null, new BigDecimal("1.11"))).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must be valid");
	}

	@Test
	public void test_throwIllegalArgumentException_WhenNameIsBlank() {
		assertThatThrownBy(() -> new Product(" ", new BigDecimal("1.11"))).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must be valid");
	}

	@Test
	public void test_throwIllegalArgumentException_WhenNameIsEmpty() {
		assertThatThrownBy(() -> new Product("", new BigDecimal("1.11"))).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must be valid");
	}

	@Test
	public void test_throwIllegalArgumentException_WhenNameIsOnlyTabs() {
		assertThatThrownBy(() -> new Product("\t", new BigDecimal("1.11"))).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must be valid");
	}

	@Test
	public void test_shouldReturn_NameFullyLowercase() {
		Product product = new Product("LapTop", new BigDecimal("1.11"));

		assertThat(product.getName()).isEqualTo("laptop");
	}

	@Test
	public void test_shouldReturn_NameFullyLowerCase_WithNoLeadingOrEndingWhiteSpaces() {
		Product product = new Product(" LapTop ", new BigDecimal("999.99"));

		assertThat(product.getName()).isEqualTo("laptop");
	}

	@Test
	public void test_shouldReturn_LowerCaseName_WhenTheNameHasWhiteSpaceInBetween() {
		Product product = new Product("Gaming Laptop", new BigDecimal("1.11"));

		assertThat(product.getName()).isEqualTo("gaming laptop");
	}

	@Test
	public void test_throwIllegalArgumentException_WhenPriceIsNull() {
		assertThatThrownBy(() -> new Product("Laptop", null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("price must be added");
	}

}
