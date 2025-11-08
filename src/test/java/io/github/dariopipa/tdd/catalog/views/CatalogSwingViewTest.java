package io.github.dariopipa.tdd.catalog.views;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.Dimension;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.table.DefaultTableModel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.dariopipa.tdd.catalog.controllers.CategoryController;
import io.github.dariopipa.tdd.catalog.controllers.ProductController;
import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.entities.Product;

@RunWith(GUITestRunner.class)
public class CatalogSwingViewTest extends AssertJSwingJUnitTestCase {

	private static final long EXISTING_ID = 1L;
	private static final long EXISTING_ID_2 = 2L;
	private static final long MISSING_ID = 999L;

	private static final String CATEGORY_NAME = "tech";
	private static final String UPDATED_CATEGORY_NAME = "books";
	private static final String PRODUCT_NAME = "laptop";
	private static final String UPDATED_PRODUCT_NAME = "hp-laptop";

	private static final String PRICE_DEFAULT = "100";
	private static final String PRICE_UPDATE = "129";
	private static final String PRICE_ZERO = "0";
	private static final String PRICE_NEGATIVE = "-1111";

	private FrameFixture frameFixture;
	private CatalogSwingView catalogSwingView;

	@Mock
	private CategoryController categoryController;

	@Mock
	private ProductController productController;

	@Override
	protected void onSetUp() {
		MockitoAnnotations.openMocks(this);

		GuiActionRunner.execute(() -> {
			catalogSwingView = new CatalogSwingView();
			catalogSwingView.setCategoryController(categoryController);
			catalogSwingView.setProductController(productController);
			return catalogSwingView;
		});

		frameFixture = new FrameFixture(robot(), catalogSwingView);
		frameFixture.show();
		frameFixture.resizeTo(new Dimension(600, 500));
	}

	@Test
	@GUITest
	public void testGeneralUIComponents() {
		frameFixture.panel("categoryPanel").requireVisible();
		frameFixture.panel("productPanel").requireVisible();
		frameFixture.panel("titlePanel").requireVisible();
		frameFixture.label("titleLabel").requireVisible();
		frameFixture.label("errorLabel").requireVisible();

		frameFixture.label("categoryName").requireVisible();
		frameFixture.label("newNameLabel").requireVisible();

		frameFixture.button("updateCategoryButton").requireVisible();
		frameFixture.button("deleteCategoryButton").requireVisible();
		frameFixture.table("categoryTable").requireVisible();

		frameFixture.textBox("newName").requireVisible();
		frameFixture.textBox("productNewName").requireVisible();
		frameFixture.comboBox("productCategorySelectBox").requireVisible();
		frameFixture.textBox("productNewPrice").requireVisible();

		frameFixture.label("productName").requireVisible();
		frameFixture.button("addProductButton").requireVisible();
		frameFixture.button("updateProductButton").requireVisible();
		frameFixture.button("deleteProductButton").requireVisible();
		frameFixture.table("productTable").requireVisible();
	}

	@Test
	@GUITest
	public void test_initialState() {
		frameFixture.button("addCategoryButton").requireDisabled();
		frameFixture.button("updateCategoryButton").requireDisabled();
		frameFixture.button("deleteCategoryButton").requireDisabled();
		frameFixture.button("addProductButton").requireDisabled();
		frameFixture.button("updateProductButton").requireDisabled();
		frameFixture.button("deleteProductButton").requireDisabled();

		frameFixture.textBox("newName").requireText("");
		frameFixture.textBox("productNewName").requireText("");
		frameFixture.textBox("productNewPrice").requireText("");
		frameFixture.comboBox("productCategorySelectBox").requireVisible();
		frameFixture.comboBox("productCategorySelectBox").requireEnabled();

		frameFixture.table("categoryTable").requireRowCount(0);
		frameFixture.table("productTable").requireRowCount(0);
	}

	@Test
	@GUITest
	public void test_addButtonCategoryIsActive_whenTextHasBeenInputted() {
		frameFixture.textBox("newName").enterText(CATEGORY_NAME);
		frameFixture.button("addCategoryButton").requireEnabled();
	}

	@Test
	@GUITest
	public void test_addButtonCategoryIsDisabled_whenTextFieldIsEmpty() {
		frameFixture.textBox("newName").enterText("     ");
		frameFixture.button("addCategoryButton").requireDisabled();
	}

	@Test
	@GUITest
	public void test_updateAndDeleteButtonsCategoryAreActive_whenItemIsSelected() {
		GuiActionRunner.execute(() -> {
			DefaultTableModel model = (DefaultTableModel) frameFixture.table("categoryTable").target().getModel();
			model.addRow(new Object[] { EXISTING_ID, CATEGORY_NAME });
		});

		frameFixture.table("categoryTable").selectRows(0);

		frameFixture.button("updateCategoryButton").requireEnabled();
		frameFixture.button("deleteCategoryButton").requireEnabled();
	}

	@Test
	@GUITest
	public void test_addButtonProductIsActive_whenTextHasBeenInputted() {
		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(new Category(CATEGORY_NAME));
			comboBox.addItem(new Category(UPDATED_CATEGORY_NAME));
			return null;
		});

		frameFixture.textBox("productNewName").enterText(PRODUCT_NAME);
		frameFixture.textBox("productNewPrice").enterText(PRICE_DEFAULT);
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);

		frameFixture.button("addProductButton").requireEnabled();
	}

	@Test
	@GUITest
	public void test_addButtonProductIsDisabled_whenTextGetsDeleted() {
		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(new Category(CATEGORY_NAME));
			comboBox.addItem(new Category(UPDATED_CATEGORY_NAME));
			return null;
		});

		frameFixture.textBox("productNewName").enterText(PRODUCT_NAME);
		frameFixture.textBox("productNewPrice").enterText(PRICE_DEFAULT);
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);

		frameFixture.button("addProductButton").requireEnabled();
		frameFixture.textBox("productNewName").deleteText();
		frameFixture.button("addProductButton").requireDisabled();
	}

	@Test
	@GUITest
	public void test_addButtonProductIsDisabled_whenPriceIsInvalid_shouldShowError() {
		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(new Category(CATEGORY_NAME));
			return null;
		});

		frameFixture.textBox("productNewName").enterText(PRODUCT_NAME);
		frameFixture.textBox("productNewPrice").enterText("string-not-a-number");
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);
		frameFixture.button("addProductButton").requireDisabled();
		frameFixture.label("errorLabel").requireVisible().requireText("Price must be an allowed number");
	}

	@Test
	@GUITest
	public void test_addButtonProductIsDisabled_whenPriceIsNegative__shouldShowError() {
		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(new Category(CATEGORY_NAME));
			return null;
		});

		frameFixture.textBox("productNewName").enterText(PRODUCT_NAME);
		frameFixture.textBox("productNewPrice").enterText(PRICE_NEGATIVE);
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);
		frameFixture.button("addProductButton").requireDisabled();
		frameFixture.label("errorLabel").requireVisible().requireText("Price must be an allowed number");
	}

	@Test
	@GUITest
	public void test_addButtonProductIsEnabled_whenPriceIsZero() {
		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(new Category(CATEGORY_NAME));
		});

		frameFixture.textBox("productNewName").enterText(PRODUCT_NAME);
		frameFixture.textBox("productNewPrice").enterText(PRICE_ZERO);
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);

		frameFixture.button("addProductButton").requireEnabled();
	}

	@Test
	@GUITest
	public void test_updateProductButtonIsActive_whenItemIsSelected_andPriceAndTextAreNotEmpty() {
		Category category = new Category(CATEGORY_NAME);
		category.setId(EXISTING_ID);

		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(category);
		});

		GuiActionRunner.execute(() -> {
			DefaultTableModel model = (DefaultTableModel) frameFixture.table("productTable").target().getModel();
			model.addRow(new Object[] { EXISTING_ID, PRODUCT_NAME, EXISTING_ID, category });
			model.addRow(new Object[] { EXISTING_ID_2, UPDATED_PRODUCT_NAME, EXISTING_ID, category });
		});

		frameFixture.textBox("productNewName").setText(PRODUCT_NAME);
		frameFixture.textBox("productNewPrice").setText(PRICE_DEFAULT);
		frameFixture.table("productTable").selectRows(0);

		frameFixture.button("updateProductButton").requireEnabled();
	}

	@Test
	@GUITest
	public void test_updateProductButtonIsDisabled_whenItemIsSelected_andPriceIsInvalid_shouldShowError() {
		Category category = new Category(CATEGORY_NAME);
		category.setId(EXISTING_ID);

		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> combo = frameFixture.comboBox("productCategorySelectBox").target();
			combo.addItem(category);
		});

		GuiActionRunner.execute(() -> {
			DefaultTableModel model = (DefaultTableModel) frameFixture.table("productTable").target().getModel();
			model.addRow(new Object[] { EXISTING_ID, PRODUCT_NAME, EXISTING_ID, category });
		});

		frameFixture.table("productTable").selectRows(0);
		frameFixture.textBox("productNewName").enterText(PRODUCT_NAME);
		frameFixture.textBox("productNewPrice").enterText(PRICE_NEGATIVE);
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);

		frameFixture.button("updateProductButton").requireDisabled();
		frameFixture.label("errorLabel").requireText("Price must be an allowed number");
	}

	@Test
	@GUITest
	public void test_deleteProductButtonIsActive_whenItemIsSelected() {
		Category category = new Category(CATEGORY_NAME);

		GuiActionRunner.execute(() -> {
			DefaultTableModel model = (DefaultTableModel) frameFixture.table("productTable").target().getModel();
			model.addRow(new Object[] { EXISTING_ID, PRODUCT_NAME, MISSING_ID, category });
			model.addRow(new Object[] { EXISTING_ID_2, UPDATED_PRODUCT_NAME, MISSING_ID, category });
		});

		frameFixture.table("productTable").selectRows(0);
		frameFixture.button("deleteProductButton").requireEnabled();
	}

	@Test
	@GUITest
	public void test_updateButtonProductIsDisabled_whenItemIsSelectedAndPriceOrTextAreEmpty() {
		Category category = new Category(CATEGORY_NAME);
		Category category2 = new Category(UPDATED_CATEGORY_NAME);

		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> combo = frameFixture.comboBox("productCategorySelectBox").target();
			combo.addItem(category);
			combo.addItem(category2);
		});

		GuiActionRunner.execute(() -> {
			DefaultTableModel model = (DefaultTableModel) frameFixture.table("productTable").target().getModel();
			model.addRow(new Object[] { EXISTING_ID, PRODUCT_NAME, MISSING_ID, category });
			model.addRow(new Object[] { EXISTING_ID_2, UPDATED_PRODUCT_NAME, MISSING_ID, category });
		});

		frameFixture.table("productTable").selectRows(0);
		frameFixture.textBox("productNewName").setText("");
		frameFixture.comboBox("productCategorySelectBox").selectItem(1);

		frameFixture.button("updateProductButton").requireDisabled();
		frameFixture.button("deleteProductButton").requireEnabled();
	}

	@Test
	@GUITest
	public void test_errorLabel_getsRemovedWhenValidPriceIsFixedAndEnteredCorrectly() {
		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(new Category(CATEGORY_NAME));
			return null;
		});

		frameFixture.textBox("productNewName").enterText(PRODUCT_NAME);
		frameFixture.textBox("productNewPrice").enterText("invalid");
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);

		frameFixture.label("errorLabel").requireText("Price must be an allowed number");

		frameFixture.textBox("productNewPrice").deleteText().enterText(PRICE_DEFAULT);
		frameFixture.label("errorLabel").requireText("");
	}

	@Test
	@GUITest
	public void test_addProductButton_isDisabled_whenCategoryNotSelected_evenIfNameAndPriceValid() {
		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.removeAllItems();
			comboBox.addItem(new Category(CATEGORY_NAME));
			comboBox.setSelectedItem(null);
			return null;
		});

		frameFixture.textBox("productNewName").enterText(PRODUCT_NAME);
		frameFixture.textBox("productNewPrice").enterText(PRICE_DEFAULT);

		frameFixture.button("addProductButton").requireDisabled();
	}

	@Test
	@GUITest
	public void test_addButtonProductIsDisabled_whenNameIsEmpty_evenIfPriceIsValid() {
		Category category = new Category(CATEGORY_NAME);
		category.setId(EXISTING_ID);
		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(category);
			return null;
		});

		frameFixture.textBox("productNewPrice").enterText(PRICE_DEFAULT);
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);
		frameFixture.button("addProductButton").requireDisabled();
	}

	@Test
	@GUITest
	public void test_prouductAddButtonIsDisabled_whenCategoryBecomesNull() {
		Category category = new Category(CATEGORY_NAME);
		category.setId(EXISTING_ID);
		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(category);
			comboBox.setSelectedItem(category);
			return null;
		});

		frameFixture.textBox("productNewName").enterText(PRODUCT_NAME);
		frameFixture.textBox("productNewPrice").enterText(PRICE_DEFAULT);
		frameFixture.button("addProductButton").requireEnabled();

		GuiActionRunner.execute(() -> frameFixture.comboBox("productCategorySelectBox").target().setSelectedItem(null));
		frameFixture.button("addProductButton").requireDisabled();
	}

	@Test
	@GUITest
	public void test_addCategoryButton_addsCategoryToTable() {
		frameFixture.textBox("newName").enterText(UPDATED_CATEGORY_NAME);
		frameFixture.button("addCategoryButton").click();

		Category category = new Category(UPDATED_CATEGORY_NAME);
		category.setId(EXISTING_ID);
		GuiActionRunner.execute(() -> catalogSwingView.addedCategory(category));

		frameFixture.table("categoryTable").requireRowCount(1);
		String name = frameFixture.table("categoryTable").valueAt(TableCell.row(0).column(1));
		assertThat(name).isEqualTo(UPDATED_CATEGORY_NAME);
	}

	@Test
	@GUITest
	public void test_addedCategory_addsCategoryToTable() {
		Category category = new Category(UPDATED_CATEGORY_NAME);
		category.setId(EXISTING_ID);

		GuiActionRunner.execute(() -> {
			catalogSwingView.addedCategory(category);
			return null;
		});

		frameFixture.table("categoryTable").requireRowCount(1);
		String idAsString = frameFixture.table("categoryTable").valueAt(TableCell.row(0).column(0));
		String name = frameFixture.table("categoryTable").valueAt(TableCell.row(0).column(1));

		assertThat(idAsString).isEqualTo("1");
		assertThat(name).isEqualTo(UPDATED_CATEGORY_NAME);
	}

	@Test
	@GUITest
	public void test_deletedCategory_removesCategoryFromTable() {
		Category category = new Category(UPDATED_CATEGORY_NAME);
		category.setId(EXISTING_ID);

		GuiActionRunner.execute(() -> {
			catalogSwingView.addedCategory(category);
		});

		frameFixture.table("categoryTable").requireRowCount(1);
		frameFixture.table("categoryTable").selectRows(0);

		GuiActionRunner.execute(() -> {
			catalogSwingView.deletedCategory(category);
			return null;
		});

		frameFixture.table("categoryTable").requireRowCount(0);
		frameFixture.label("errorLabel").requireText("");
	}

	@Test
	@GUITest
	public void test_deletedCategory_doesNothing_whenNoRowIsSelected() {
		Category category = new Category(UPDATED_CATEGORY_NAME);
		category.setId(EXISTING_ID);

		GuiActionRunner.execute(() -> catalogSwingView.addedCategory(category));
		frameFixture.table("categoryTable").requireRowCount(1);

		GuiActionRunner.execute(() -> frameFixture.table("categoryTable").target().clearSelection());
		frameFixture.table("categoryTable").requireNoSelection();

		GuiActionRunner.execute(() -> catalogSwingView.deletedCategory(category));
		frameFixture.table("categoryTable").requireRowCount(1);
	}

	@Test
	@GUITest
	public void test_showError_displaysErrorMessage() {
		GuiActionRunner.execute(() -> catalogSwingView.showError("Test error message"));

		frameFixture.label("errorLabel").requireText("Test error message");
	}

	@Test
	@GUITest
	public void test_updateCategory_updatesCategoryAndResetsError() {
		Category category = new Category(CATEGORY_NAME);
		category.setId(EXISTING_ID);
		GuiActionRunner.execute(() -> {
			catalogSwingView.addedCategory(category);
		});

		frameFixture.table("categoryTable").selectRows(0);

		Category updatedCategory = new Category(UPDATED_CATEGORY_NAME);
		updatedCategory.setId(EXISTING_ID);
		GuiActionRunner.execute(() -> catalogSwingView.updateCategory(updatedCategory));

		String updatedName = frameFixture.table("categoryTable").valueAt(TableCell.row(0).column(1));
		assertThat(updatedName).isEqualTo(UPDATED_CATEGORY_NAME);
		frameFixture.label("errorLabel").requireText("");
	}

	@Test
	@GUITest
	public void test_updateCategoryButton_doesNothingWhenNoRowIsSelected() {
		frameFixture.textBox("newName").enterText(CATEGORY_NAME);

		frameFixture.table("categoryTable").requireNoSelection();
		frameFixture.button("updateCategoryButton").click();

		verify(categoryController, times(0)).update(any(), any());
	}

	@Test
	@GUITest
	public void test_findAllCategories_returnsData() {
		Category categoryOne = new Category(CATEGORY_NAME);
		Category categoryTwo = new Category(UPDATED_CATEGORY_NAME);
		categoryOne.setId(EXISTING_ID);
		categoryTwo.setId(EXISTING_ID_2);

		GuiActionRunner.execute(() -> catalogSwingView.addedCategory(categoryOne));
		GuiActionRunner.execute(() -> catalogSwingView.addedCategory(categoryTwo));
		GuiActionRunner.execute(() -> catalogSwingView.findAllCategories(Arrays.asList(categoryOne, categoryTwo)));

		frameFixture.table("categoryTable").requireRowCount(2);
		assertThat(frameFixture.table("categoryTable").valueAt(TableCell.row(0).column(1))).isEqualTo(CATEGORY_NAME);
		assertThat(frameFixture.table("categoryTable").valueAt(TableCell.row(1).column(1)))
				.isEqualTo(UPDATED_CATEGORY_NAME);
	}

	@Test
	@GUITest
	public void test_findAllCategories_withEmptyList_returnsEmptyTable() {
		GuiActionRunner.execute(() -> catalogSwingView.findAllCategories(Arrays.asList()));

		frameFixture.table("categoryTable").requireRowCount(0);
		frameFixture.label("errorLabel").requireText("");
	}

	@Test
	@GUITest
	public void test_addedProduct_addsProductToTable() {
		Category category = new Category(CATEGORY_NAME);
		category.setId(EXISTING_ID);
		Product product = new Product(PRODUCT_NAME, new BigDecimal(PRICE_DEFAULT), category);
		product.setId(EXISTING_ID);

		GuiActionRunner.execute(() -> {
			catalogSwingView.addedProduct(product);
		});

		frameFixture.table("productTable").requireRowCount(1);
		String idAsString = frameFixture.table("productTable").valueAt(TableCell.row(0).column(0));
		String name = frameFixture.table("productTable").valueAt(TableCell.row(0).column(1));
		String price = frameFixture.table("productTable").valueAt(TableCell.row(0).column(2));
		String productCategory = frameFixture.table("productTable").valueAt(TableCell.row(0).column(3));

		assertThat(idAsString).isEqualTo("1");
		assertThat(name).isEqualTo(PRODUCT_NAME);
		assertThat(price).isEqualTo(PRICE_DEFAULT);
		assertThat(productCategory).isEqualTo(category.toString());

	}

	@Test
	@GUITest
	public void test_deletedProduct_removesProductFromTable() {
		Category category = new Category(CATEGORY_NAME);
		category.setId(EXISTING_ID);
		Product product = new Product(PRODUCT_NAME, new BigDecimal(PRICE_DEFAULT), category);
		product.setId(EXISTING_ID);

		GuiActionRunner.execute(() -> {
			catalogSwingView.addedProduct(product);
		});

		frameFixture.table("productTable").requireRowCount(1);
		frameFixture.table("productTable").selectRows(0);

		GuiActionRunner.execute(() -> {
			catalogSwingView.deletedProduct(product);
			return null;
		});

		frameFixture.table("productTable").requireRowCount(0);
		frameFixture.label("errorLabel").requireText("");
	}

	@Test
	@GUITest
	public void test_deletedProduct_doesNothing_whenNoRowIsSelected() {
		Category category = new Category(CATEGORY_NAME);
		category.setId(EXISTING_ID);

		Product product = new Product(PRODUCT_NAME, new BigDecimal(PRICE_DEFAULT), category);
		product.setId(EXISTING_ID);

		GuiActionRunner.execute(() -> catalogSwingView.addedProduct(product));
		frameFixture.table("productTable").requireRowCount(1);

		GuiActionRunner.execute(() -> frameFixture.table("productTable").target().clearSelection());
		frameFixture.table("productTable").requireNoSelection();

		GuiActionRunner.execute(() -> catalogSwingView.deletedProduct(product));
		frameFixture.table("productTable").requireRowCount(1);
	}

	@Test
	@GUITest
	public void test_updateProduct_updatesProductAndResetsError() {
		Category category = new Category(CATEGORY_NAME);
		category.setId(EXISTING_ID);
		Product product = new Product(PRODUCT_NAME, new BigDecimal(PRICE_DEFAULT), category);
		product.setId(EXISTING_ID);

		GuiActionRunner.execute(() -> {
			catalogSwingView.addedProduct(product);
		});

		frameFixture.table("productTable").selectRows(0);

		Product updatedProduct = new Product(UPDATED_PRODUCT_NAME, new BigDecimal(PRICE_UPDATE), category);
		updatedProduct.setId(EXISTING_ID);
		GuiActionRunner.execute(() -> catalogSwingView.updateProduct(updatedProduct));

		String updatedName = frameFixture.table("productTable").valueAt(TableCell.row(0).column(1));
		String updatedPrice = frameFixture.table("productTable").valueAt(TableCell.row(0).column(2));

		assertThat(updatedName).isEqualTo(UPDATED_PRODUCT_NAME);
		assertThat(updatedPrice).isEqualTo(PRICE_UPDATE);

		frameFixture.label("errorLabel").requireText("");
	}

	@Test
	@GUITest
	public void test_updateProduct_doesNothing_whenNoRowIsSelected() {
		Category category = new Category(CATEGORY_NAME);
		category.setId(EXISTING_ID);
		Product product = new Product(PRODUCT_NAME, new BigDecimal(PRICE_DEFAULT), category);
		product.setId(EXISTING_ID);

		GuiActionRunner.execute(() -> catalogSwingView.addedProduct(product));
		frameFixture.table("productTable").requireRowCount(1);

		GuiActionRunner.execute(() -> frameFixture.table("productTable").target().clearSelection());
		frameFixture.table("productTable").requireNoSelection();

		Product updated = new Product(UPDATED_PRODUCT_NAME, new BigDecimal(PRICE_UPDATE), category);
		updated.setId(EXISTING_ID);

		GuiActionRunner.execute(() -> catalogSwingView.updateProduct(updated));

		frameFixture.table("productTable").requireRowCount(1);
	}

	@Test
	@GUITest
	public void test_findAllProducts_returnsData() {
		Category category = new Category(CATEGORY_NAME);
		category.setId(EXISTING_ID);

		Product productOne = new Product(PRODUCT_NAME, new BigDecimal(PRICE_DEFAULT), category);
		Product productTwo = new Product(UPDATED_PRODUCT_NAME, new BigDecimal(PRICE_DEFAULT), category);
		productOne.setId(EXISTING_ID);
		productTwo.setId(EXISTING_ID_2);

		GuiActionRunner.execute(() -> catalogSwingView.addedProduct(productOne));
		GuiActionRunner.execute(() -> catalogSwingView.addedProduct(productTwo));
		GuiActionRunner.execute(() -> catalogSwingView.findAllProducts(Arrays.asList(productOne, productTwo)));

		frameFixture.table("productTable").requireRowCount(2);

		String product1Id = frameFixture.table("productTable").valueAt(TableCell.row(0).column(0));
		String product1Name = frameFixture.table("productTable").valueAt(TableCell.row(0).column(1));
		String product1Price = frameFixture.table("productTable").valueAt(TableCell.row(0).column(2));
		String product1Category = frameFixture.table("productTable").valueAt(TableCell.row(0).column(3));

		String product2Id = frameFixture.table("productTable").valueAt(TableCell.row(1).column(0));
		String product2Name = frameFixture.table("productTable").valueAt(TableCell.row(1).column(1));
		String product2Price = frameFixture.table("productTable").valueAt(TableCell.row(1).column(2));
		String product2Category = frameFixture.table("productTable").valueAt(TableCell.row(1).column(3));

		assertThat(product1Id).isEqualTo("1");
		assertThat(product1Name).isEqualTo(PRODUCT_NAME);
		assertThat(product1Price).isEqualTo(PRICE_DEFAULT);
		assertThat(product1Category).isEqualTo(category.toString());

		assertThat(product2Id).isEqualTo("2");
		assertThat(product2Name).isEqualTo(UPDATED_PRODUCT_NAME);
		assertThat(product2Price).isEqualTo(PRICE_DEFAULT);
		assertThat(product2Category).isEqualTo(category.toString());
	}

	@Test
	@GUITest
	public void test_findAllProducts_withEmptyList_returnsEmptyTable() {
		GuiActionRunner.execute(() -> catalogSwingView.findAllProducts(Arrays.asList()));

		frameFixture.table("productTable").requireRowCount(0);
		frameFixture.label("errorLabel").requireText("");
	}

	@Test
	@GUITest
	public void test_categoryAddButton_shouldDelegateToCategoryControllerCreate() {
		frameFixture.textBox("newName").enterText(CATEGORY_NAME);
		frameFixture.button("addCategoryButton").click();

		verify(categoryController, times(1)).create(CATEGORY_NAME);
	}

	@Test
	@GUITest
	public void test_categoryDeleteButton_shouldDelegateToCategoryControllerDelete() {
		GuiActionRunner.execute(() -> {
			DefaultTableModel model = (DefaultTableModel) frameFixture.table("categoryTable").target().getModel();
			model.addRow(new Object[] { EXISTING_ID, CATEGORY_NAME });
			model.addRow(new Object[] { EXISTING_ID_2, UPDATED_CATEGORY_NAME });
		});

		frameFixture.table("categoryTable").selectCell(TableCell.row(0).column(0));
		frameFixture.button("deleteCategoryButton").click();

		verify(categoryController, times(1)).delete(EXISTING_ID);
	}

	@Test
	@GUITest
	public void test_categoryUpdateButton_shouldDelegateToCategoryControllerUpdate() {
		GuiActionRunner.execute(() -> {
			DefaultTableModel model = (DefaultTableModel) frameFixture.table("categoryTable").target().getModel();
			model.addRow(new Object[] { EXISTING_ID, CATEGORY_NAME });
			model.addRow(new Object[] { EXISTING_ID_2, UPDATED_CATEGORY_NAME });
		});

		frameFixture.table("categoryTable").selectCell(TableCell.row(0).column(0));

		frameFixture.textBox("newName").enterText(UPDATED_CATEGORY_NAME);
		frameFixture.button("updateCategoryButton").click();

		verify(categoryController, times(1)).update(EXISTING_ID, UPDATED_CATEGORY_NAME);
	}

	@Test
	@GUITest
	public void test_productAddButton_shouldDelegateToProductControllerCreate() {
		Category category = new Category(CATEGORY_NAME);
		category.setId(EXISTING_ID);

		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(category);
			return null;
		});

		frameFixture.textBox("productNewName").enterText(PRODUCT_NAME);
		frameFixture.textBox("productNewPrice").enterText(PRICE_DEFAULT);
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);

		frameFixture.button("addProductButton").click();

		verify(productController, times(1)).create(PRODUCT_NAME, new BigDecimal(PRICE_DEFAULT), EXISTING_ID);
	}

	@Test
	@GUITest
	public void test_productDeleteButton_shouldDelegateToProductControllerDelete() {
		Category category = new Category(CATEGORY_NAME);

		GuiActionRunner.execute(() -> {
			DefaultTableModel model = (DefaultTableModel) frameFixture.table("productTable").target().getModel();
			model.addRow(new Object[] { EXISTING_ID, PRODUCT_NAME, MISSING_ID, category });
			model.addRow(new Object[] { EXISTING_ID_2, UPDATED_PRODUCT_NAME, MISSING_ID, category });
		});

		frameFixture.table("productTable").selectRows(0);
		frameFixture.button("deleteProductButton").click();

		verify(productController, times(1)).delete(EXISTING_ID);
	}

	@Test
	@GUITest
	public void test_productUpdateButton_shouldDelegateToProductControllerUpdate() {
		Category categoryOne = new Category(CATEGORY_NAME);
		categoryOne.setId(EXISTING_ID);
		Category categoryTwo = new Category(UPDATED_CATEGORY_NAME);
		categoryTwo.setId(EXISTING_ID_2);

		GuiActionRunner.execute(() -> {
			DefaultTableModel model = (DefaultTableModel) frameFixture.table("productTable").target().getModel();
			model.addRow(new Object[] { EXISTING_ID, PRODUCT_NAME, MISSING_ID, categoryOne });
			model.addRow(new Object[] { EXISTING_ID_2, UPDATED_PRODUCT_NAME, MISSING_ID, categoryOne });
		});

		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(categoryOne);
			comboBox.addItem(categoryTwo);
			return null;
		});

		frameFixture.table("productTable").selectRows(0);
		frameFixture.textBox("productNewName").enterText(UPDATED_PRODUCT_NAME);
		frameFixture.textBox("productNewPrice").enterText(PRICE_DEFAULT);
		frameFixture.comboBox("productCategorySelectBox").selectItem(1);

		frameFixture.button("updateProductButton").click();

		verify(productController, times(1)).update(EXISTING_ID, UPDATED_PRODUCT_NAME, new BigDecimal(PRICE_DEFAULT),
				EXISTING_ID_2);
	}

	@Test
	@GUITest
	public void test_findAllCategories_shoudlPopulateAlsoTheComboBox() {
		Category categoryOne = new Category(CATEGORY_NAME);
		categoryOne.setId(EXISTING_ID);
		Category categoryTwo = new Category(UPDATED_CATEGORY_NAME);
		categoryTwo.setId(EXISTING_ID_2);
		List<Category> categories = Arrays.asList(categoryOne, categoryTwo);

		GuiActionRunner.execute(() -> {
			catalogSwingView.findAllCategories(categories);
			return null;
		});

		JComboBoxFixture comboBoxFixture = frameFixture.comboBox("productCategorySelectBox");

		assertThat(comboBoxFixture.target().getItemCount()).isEqualTo(2);
		assertThat(comboBoxFixture.contents()).containsExactly(CATEGORY_NAME, UPDATED_CATEGORY_NAME);

		Category firstItem = (Category) comboBoxFixture.target().getItemAt(0);
		Category secondItem = (Category) comboBoxFixture.target().getItemAt(1);

		assertThat(firstItem.getName()).isEqualTo(CATEGORY_NAME);
		assertThat(firstItem.getId()).isEqualTo(EXISTING_ID);
		assertThat(secondItem.getName()).isEqualTo(UPDATED_CATEGORY_NAME);
		assertThat(secondItem.getId()).isEqualTo(EXISTING_ID_2);
	}

	@Test
	@GUITest
	public void test_productUpdateButton_shouldNotCallProductController() {
		GuiActionRunner.execute(() -> {
			frameFixture.button("updateProductButton").target().setEnabled(true);
			frameFixture.table("productTable").target().clearSelection();
			return null;
		});

		frameFixture.button("updateProductButton").click();

		verify(productController, times(0)).update(any(), any(), any(), any());
	}

	@Test
	@GUITest
	public void test_categoryDeleteButton_shouldNotCallCategoryController() {
		GuiActionRunner.execute(() -> {
			frameFixture.button("deleteCategoryButton").target().setEnabled(true);
			frameFixture.table("categoryTable").target().clearSelection();
			return null;
		});

		frameFixture.button("deleteCategoryButton").click();

		verify(categoryController, times(0)).delete(any());
	}

	@Test
	@GUITest
	public void test_categoryUpdateButton_shouldNotCallCategoryController() {
		GuiActionRunner.execute(() -> {
			frameFixture.button("updateCategoryButton").target().setEnabled(true);
			frameFixture.table("categoryTable").target().clearSelection();
			return null;
		});

		frameFixture.button("updateCategoryButton").click();

		verify(categoryController, times(0)).update(any(), any());
	}

	@Test
	@GUITest
	public void test_productDeleteButton_shouldNotCallProductController() {
		GuiActionRunner.execute(() -> {
			frameFixture.button("deleteProductButton").target().setEnabled(true);
			frameFixture.table("productTable").target().clearSelection();
			return null;
		});

		frameFixture.button("deleteProductButton").click();

		verify(productController, times(0)).delete(any());
	}

	@Test
	@GUITest
	public void test_updateCategory_withNoSelection_shouldNotUpdateCategory() {
		Category category = new Category(CATEGORY_NAME);
		category.setId(EXISTING_ID);
		GuiActionRunner.execute(() -> catalogSwingView.addedCategory(category));

		frameFixture.table("categoryTable").requireRowCount(1);

		GuiActionRunner.execute(() -> {
			frameFixture.table("categoryTable").target().clearSelection();
			return null;
		});

		frameFixture.table("categoryTable").requireNoSelection();

		Category updatedCategory = new Category(UPDATED_CATEGORY_NAME);
		updatedCategory.setId(EXISTING_ID);
		GuiActionRunner.execute(() -> catalogSwingView.updateCategory(updatedCategory));

		String name = frameFixture.table("categoryTable").valueAt(TableCell.row(0).column(1));
		assertThat(name).isEqualTo(CATEGORY_NAME);
	}

	@Test
	@GUITest
	public void test_getSelectedIdFromCategoryTypes_whenNoItemSelected_showsError() {
		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.removeAllItems();
			comboBox.setSelectedItem(null);
			return null;
		});

		Long result = GuiActionRunner.execute(() -> catalogSwingView.getSelectedIdFromCategoryTypes());

		assertThat(result).isNull();
		frameFixture.label("errorLabel").requireText("Please select a category");
	}

	@Test
	@GUITest
	public void test_getSelectedIdFromCategoryTypes_whenCategoryHasNullId_showsError() {
		Category categoryWithNullId = new Category(CATEGORY_NAME);

		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(categoryWithNullId);
			comboBox.setSelectedItem(categoryWithNullId);
			return null;
		});

		Long result = GuiActionRunner.execute(() -> catalogSwingView.getSelectedIdFromCategoryTypes());

		assertThat(result).isNull();
		frameFixture.label("errorLabel").requireText("Please select a category");
	}

	@Test
	@GUITest
	public void test_getSelectedIdFromCategoryTypes_whenValidCategory_returnsId() {
		Category category = new Category(CATEGORY_NAME);
		category.setId(EXISTING_ID);

		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(category);
			comboBox.setSelectedItem(category);
			return null;
		});

		Long result = GuiActionRunner.execute(() -> catalogSwingView.getSelectedIdFromCategoryTypes());

		assertThat(result).isEqualTo(EXISTING_ID);
	}

	@Test
	@GUITest
	public void test_deleteCategoryInUseFromProducts_shouldShowErrorMessage() {
		Category category = new Category(UPDATED_CATEGORY_NAME);
		category.setId(EXISTING_ID);

		GuiActionRunner.execute(() -> {
			catalogSwingView.addedCategory(category);
		});

		frameFixture.table("categoryTable").selectRows(0);
		frameFixture.button("deleteCategoryButton").click();

		verify(categoryController, times(1)).delete(EXISTING_ID);

		GuiActionRunner.execute(() -> {
			catalogSwingView.showError("Category in use by existing products");
			return null;
		});

		frameFixture.label("errorLabel").requireText("Category in use by existing products");
	}
}