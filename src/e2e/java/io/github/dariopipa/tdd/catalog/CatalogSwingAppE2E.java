package io.github.dariopipa.tdd.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

import java.math.BigDecimal;

import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@RunWith(GUITestRunner.class)
public class CatalogSwingAppE2E extends AssertJSwingJUnitTestCase { // NOSONAR we want the name this way

	private static final String CATEGORY_NAME = "category 1";
	private static final String NEW_CATEGORY_NAME = "new category";
	private static final String PRODUCT_A = "product a";
	private static final String PRODUCT_B = "product B";
	private static final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(100);
	private static final String NON_VALID_PRICE = "-1";
	private FrameFixture window;

	@SuppressWarnings("resource")
	@ClassRule
	public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
			DockerImageName.parse("postgres:16-alpine")).withDatabaseName("e2eTest").withUsername("E2Etest")
			.withPassword("E2Etest");

	@Override
	protected void onSetUp() {
		application("io.github.dariopipa.tdd.catalog.App").withArgs("--jdbc-url=" + postgres.getJdbcUrl(),
				"--jdbc-user=" + postgres.getUsername(), "--jdbc-password=" + postgres.getPassword()).start();

		// Get a reference to the JFrame
		window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "Catalog".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot());

		createCategoryViaUI(CATEGORY_NAME);
		createProductViaUI(PRODUCT_A, PRODUCT_PRICE, CATEGORY_NAME);
	}

	private void createCategoryViaUI(String name) {
		window.textBox("newName").deleteText().enterText(name);
		window.button("addCategoryButton").click();

		window.label("errorLabel").requireText("");

		assertThat(window.comboBox("productCategorySelectBox").contents()).contains(name);
	}

	private void createProductViaUI(String name, BigDecimal price, String category) {
		window.comboBox("productCategorySelectBox").selectItem(category);
		window.textBox("productNewName").deleteText().enterText(name);
		window.textBox("productNewPrice").deleteText().enterText(price.toString());
		window.button("addProductButton").click();
		window.label("errorLabel").requireText("");
	}

	@Test
	@GUITest
	public void test_onStartAllDatabaseElementsAreShown() {
		JComboBoxFixture comboBox = window.comboBox("productCategorySelectBox");
		assertThat(comboBox.contents()).contains(CATEGORY_NAME);

		String[][] productContents = window.table("productTable").contents();
		assertThat(productContents).anySatisfy(row -> {
			assertThat(row[1]).isEqualTo(PRODUCT_A);
			assertThat(new BigDecimal(row[2])).isEqualByComparingTo(PRODUCT_PRICE);
			assertThat(row[3]).contains(CATEGORY_NAME);
		});
	}

	@Test
	@GUITest
	public void test_addCategoryButton_shouldAppearAlsoOnProductCategorySelectBox() {
		window.textBox("newName").deleteText().enterText(NEW_CATEGORY_NAME);
		window.button("addCategoryButton").requireEnabled();
		window.button("addCategoryButton").click();

		String[][] rows = window.table("categoryTable").contents();
		assertThat(rows[1][1]).isEqualTo(NEW_CATEGORY_NAME);
		window.label("errorLabel").requireText("");

		window.comboBox("productCategorySelectBox").selectItem(NEW_CATEGORY_NAME);
	}

	@Test
	@GUITest
	public void test_addCategoryButton_withExistingName_shouldShowError() {
		window.textBox("newName").deleteText().enterText(CATEGORY_NAME);
		window.button("addCategoryButton").requireEnabled();
		window.button("addCategoryButton").click();

		window.label("errorLabel").requireText("Category name already exists");
	}

	@Test
	@GUITest
	public void test_updateCategoryButton() {
		window.table("categoryTable").selectRows(0);
		window.textBox("newName").deleteText().enterText(NEW_CATEGORY_NAME);
		window.button("updateCategoryButton").requireEnabled();
		window.button("updateCategoryButton").click();

		String[][] rows = window.table("categoryTable").contents();
		assertThat(rows[0][1]).isEqualTo(NEW_CATEGORY_NAME);
		window.label("errorLabel").requireText("");
		window.comboBox("productCategorySelectBox").selectItem(NEW_CATEGORY_NAME);
	}

	@Test
	@GUITest
	public void testUpdateCategoryButton_withExistingName_shouldShowError() {
		window.table("categoryTable").selectRows(0);
//		window.textBox("newName").enterText(CATEGORY_NAME);
		window.button("updateCategoryButton").requireEnabled();
		window.button("updateCategoryButton").click();

		window.label("errorLabel").requireText("Category name already exists");
	}

	@Test
	@GUITest
	public void test_deleteCategoryButton() {
		window.textBox("newName").deleteText().enterText(NEW_CATEGORY_NAME);
		window.button("addCategoryButton").click();

		window.table("categoryTable").selectRows(1);
		window.button("deleteCategoryButton").requireEnabled();
		window.button("deleteCategoryButton").click();

		window.label("errorLabel").requireText("");
		assertThat(window.comboBox("productCategorySelectBox").contents())
				.noneMatch(e -> e.contains(NEW_CATEGORY_NAME));
	}

	@Test
	@GUITest
	public void test_deleteCategoryButton_thatIsBeingUsedFromProduct_shouldShowError() {
		window.table("categoryTable").selectRows(0);
		window.button("deleteCategoryButton").requireEnabled();
		window.button("deleteCategoryButton").click();

		window.label("errorLabel").requireText("Category in use by existing products");
	}

	@Test
	@GUITest
	public void test_addProductButton() {
		window.comboBox("productCategorySelectBox").selectItem(CATEGORY_NAME);

		window.textBox("productNewName").deleteText().enterText(PRODUCT_B);
		window.textBox("productNewPrice").deleteText().enterText(PRODUCT_PRICE.toString());
		window.button("addProductButton").requireEnabled();
		window.button("addProductButton").click();

		String[][] rows = window.table("productTable").contents();
		assertThat(rows).anySatisfy(row -> {
			assertThat(row[1]).isEqualToIgnoringCase(PRODUCT_B);
			assertThat(new BigDecimal(row[2])).isEqualByComparingTo(PRODUCT_PRICE);
			assertThat(row[3]).containsIgnoringCase(CATEGORY_NAME);
		});

		window.label("errorLabel").requireText("");
	}

	@Test
	@GUITest
	public void test_addProductButton_withExistingName_shouldShowError() {
		window.comboBox("productCategorySelectBox").selectItem(CATEGORY_NAME);

		window.textBox("productNewName").deleteText().enterText(PRODUCT_A);
		window.textBox("productNewPrice").deleteText().enterText(PRODUCT_PRICE.toString());
		window.button("addProductButton").requireEnabled();
		window.button("addProductButton").click();

		window.label("errorLabel").requireText("Invalid input: Product name already exists");
	}

	@Test
	@GUITest
	public void test_addProductButton_withNonValidPrice_shouldShowError() {
		window.comboBox("productCategorySelectBox").selectItem(CATEGORY_NAME);

		window.textBox("productNewName").deleteText().enterText(PRODUCT_A);
		window.textBox("productNewPrice").deleteText().enterText(NON_VALID_PRICE);
		window.button("addProductButton").requireDisabled();

		window.label("errorLabel").requireText("Price must be an allowed number");
	}

	@Test
	@GUITest
	public void test_updateProductButton_succesfullyUpdatesProduct() {
		window.comboBox("productCategorySelectBox").selectItem(CATEGORY_NAME);
		window.table("productTable").selectRows(0);

		window.textBox("productNewName").deleteText().enterText(PRODUCT_B);
		window.textBox("productNewPrice").deleteText().enterText(PRODUCT_PRICE.toString());
		window.button("updateProductButton").requireEnabled();
		window.button("updateProductButton").click();

		String[][] rows = window.table("productTable").contents();
		assertThat(rows).anySatisfy(row -> {
			assertThat(row[1]).isEqualToIgnoringCase(PRODUCT_B);
			assertThat(new BigDecimal(row[2])).isEqualByComparingTo(PRODUCT_PRICE);
			assertThat(row[3]).containsIgnoringCase(CATEGORY_NAME);
		});

		window.label("errorLabel").requireText("");
	}

	@Test
	@GUITest
	public void test_updateProductButton_withNonValidPrice_shouldShowError() {
		window.comboBox("productCategorySelectBox").selectItem(CATEGORY_NAME);
		window.table("productTable").selectRows(0);

		window.textBox("productNewName").deleteText().enterText(PRODUCT_B);
		window.textBox("productNewPrice").deleteText().enterText(NON_VALID_PRICE);
		window.button("updateProductButton").requireDisabled();

		String[][] rows = window.table("productTable").contents();
		assertThat(rows).anySatisfy(row -> {
			assertThat(row[1]).isEqualToIgnoringCase(PRODUCT_A);
			assertThat(new BigDecimal(row[2])).isEqualByComparingTo(PRODUCT_PRICE);
			assertThat(row[3]).containsIgnoringCase(CATEGORY_NAME);
		});

		window.label("errorLabel").requireText("Price must be an allowed number");
	}

	@Test
	@GUITest
	public void test_deleteProduct() {
		window.table("productTable").selectRows(0);
		window.button("deleteProductButton").isEnabled();
		window.button("deleteProductButton").click();

		window.table("productTable").requireRowCount(0);
	}

}