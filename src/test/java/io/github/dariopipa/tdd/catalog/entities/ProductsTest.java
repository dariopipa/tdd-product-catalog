package io.github.dariopipa.tdd.catalog.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.Test;

public class ProductsTest {

	private static final Long CATEGORY_ID = 1L;
	private static final BigDecimal VALID_PRICE = new BigDecimal("1.11");
	private static final String VALID_NAME = "Laptop";

	@Test
	public void test_create_ProductEntity_WithNameAndPrice() {
		Product product = new Product(VALID_NAME, VALID_PRICE, CATEGORY_ID);

		assertThat(product.getName()).isEqualTo("laptop");
		assertThat(product.getPrice()).isEqualByComparingTo("1.11");
	}

	@Test
	public void test_throw_IllegalArgumentException_WhenPriceIsNull() {
		assertThatThrownBy(() -> new Product(VALID_NAME, null, CATEGORY_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("price must be added");
	}

	@Test
	public void test_throw_IllegalArgumentException_WhenPriceIsNegative() {
		BigDecimal negativePrice = new BigDecimal("-10");

		assertThatThrownBy(() -> new Product(VALID_NAME, negativePrice, CATEGORY_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("price must be positive");
	}

	@Test
	public void test_create_Product_WithPriceZero() {
		BigDecimal zeroPrice = new BigDecimal("0");
		Product product = new Product(VALID_NAME, zeroPrice, CATEGORY_ID);

		assertThat(product.getName()).isEqualTo("laptop");
		assertThat(product.getPrice()).isEqualByComparingTo("0");
	}

	@Test
	public void test_throwIllegalArgumentException_WhenNameIsNull() {
		assertThatThrownBy(() -> new Product(null, VALID_PRICE, CATEGORY_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be valid");
	}

	@Test
	public void test_throwIllegalArgumentException_WhenNameIsBlank() {
		assertThatThrownBy(() -> new Product(" ", VALID_PRICE, CATEGORY_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be valid");
	}

	@Test
	public void test_throwIllegalArgumentException_WhenNameIsEmpty() {
		assertThatThrownBy(() -> new Product("", VALID_PRICE, CATEGORY_ID)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must be valid");
	}

	@Test
	public void test_throwIllegalArgumentException_WhenNameIsOnlyTabs() {
		assertThatThrownBy(() -> new Product("\t", VALID_PRICE, CATEGORY_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name must be valid");
	}

	@Test
	public void test_shouldReturn_NameFullyLowercase() {
		Product product = new Product("LapTop", VALID_PRICE, CATEGORY_ID);

		assertThat(product.getName()).isEqualTo("laptop");
	}

	@Test
	public void test_shouldReturn_NameFullyLowerCase_WithNoLeadingOrEndingWhiteSpaces() {
		Product product = new Product(" LapTop ", new BigDecimal("999.99"), CATEGORY_ID);

		assertThat(product.getName()).isEqualTo("laptop");
	}

	@Test
	public void test_shouldReturn_LowerCaseName_WhenTheNameHasWhiteSpaceInBetween() {
		Product product = new Product("Gaming Laptop", VALID_PRICE, CATEGORY_ID);

		assertThat(product.getName()).isEqualTo("gaming laptop");
	}

	@Test
	public void test_throwIllegalArgumentException_WhenPriceIsNull() {
		assertThatThrownBy(() -> new Product(VALID_NAME, null, CATEGORY_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("price must be added");
	}

	@Test
	public void test_throwIllegalArgumentExcpetion_WhenCategoryIdIsNotProvided() {
		assertThatThrownBy(() -> new Product(VALID_NAME, VALID_PRICE, null))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("categoryId must be provided");
	}

	@Test
	public void test_throwIllegalArgumentExcpetion_WhenCategoryIdIsNegative() {
		assertThatThrownBy(() -> new Product(VALID_NAME, VALID_PRICE, -1L)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("categoryId must be positive");
	}
}