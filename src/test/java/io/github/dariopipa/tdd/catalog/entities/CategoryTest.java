package io.github.dariopipa.tdd.catalog.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class CategoryTest {

	@Test
	public void test_create_CategoryEntity_WithName() {
		Category category = new Category("technology");

		assertThat(category.getName()).isEqualTo("technology");
	}

	@Test
	public void test_shouldThrowIllegalExcpetions_whenNameIsNull() {
		assertThatThrownBy(() -> new Category(null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must be valid");
	}

	@Test
	public void test_shouldThrowIllegalExcpetions_whenNameIsBlank() {
		assertThatThrownBy(() -> new Category(" ")).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must be valid");
	}

	@Test
	public void test_shouldReturn_NameFullyLowercase() {
		Category category = new Category("TeChNology");

		assertThat(category.getName()).isEqualTo("technology");
	}

	@Test
	public void test_shouldReturn_NameFullyLowerCase_WithNoLeadingOrEndingWhiteSpaces() {
		Category category = new Category("   TeChNology   ");

		assertThat(category.getName()).isEqualTo("technology");
	}

	@Test
	public void test_shouldThrowIllegalArgumentException_whenNameIsOnlyTabs() {
		assertThatThrownBy(() -> new Category("\t")).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("name must be valid");
	}

	@Test
	public void test_shouldReturn_LowerCaseName_WhenTheNameHasWhiteSpaceInBetween() {
		Category category = new Category("Electronic Devices");

		assertThat(category.getName()).isEqualTo("electronic devices");
	}

}
