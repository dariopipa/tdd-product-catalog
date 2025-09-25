package io.github.dariopipa.tdd.catalog.views;

import static org.assertj.core.api.Assertions.assertThat;
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
	public void test_addButtonCategoryIsActive_whenTextHasBeenInputted() {
		frameFixture.textBox("newName").enterText("category name");
		frameFixture.button("addCategoryButton").requireEnabled();
	}

	@Test
	public void test_addButtonCategoryIsDisabled_whenTextFieldIsEmpty() {
		frameFixture.textBox("newName").enterText("     ");
		frameFixture.button("addCategoryButton").requireDisabled();
	}

	@Test
	public void test_updateAndDeleteButtonsCategoryAreActive_whenItemIsSelected() {
		GuiActionRunner.execute(() -> {
			DefaultTableModel model = (DefaultTableModel) frameFixture.table("categoryTable").target().getModel();
			model.addRow(new Object[] { 1L, "Test Category" });
		});

		frameFixture.table("categoryTable").selectRows(0);

		frameFixture.button("updateCategoryButton").requireEnabled();
		frameFixture.button("deleteCategoryButton").requireEnabled();
	}

	@Test
	public void test_addButtonProductIsActive_whenTextHasBeenInputted() {
		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(new Category("tech"));
			comboBox.addItem(new Category("electronics"));
			return null;
		});

		frameFixture.textBox("productNewName").enterText("product name");
		frameFixture.textBox("productNewPrice").enterText(BigDecimal.valueOf(100).toString());
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);

		frameFixture.button("addProductButton").requireEnabled();
	}

	@Test
	public void test_addButtonProductIsDisabled_whenTextGetsDeleted() {
		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(new Category("tech"));
			comboBox.addItem(new Category("electronics"));
			return null;
		});

		frameFixture.textBox("productNewName").enterText("product name");
		frameFixture.textBox("productNewPrice").enterText(BigDecimal.valueOf(100).toString());
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);

		frameFixture.button("addProductButton").requireEnabled();
		frameFixture.textBox("productNewName").deleteText();
		frameFixture.button("addProductButton").requireDisabled();
	}

	@Test
	public void test_addButtonProductIsDisabled_whenPriceIsInvalid_shouldShowError() {
		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(new Category("tech"));
			return null;
		});

		frameFixture.textBox("productNewName").enterText("product name");
		frameFixture.textBox("productNewPrice").enterText("string-not-a-number");
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);
		frameFixture.button("addProductButton").requireDisabled();
		frameFixture.label("errorLabel").requireVisible().requireText("Price must be an allowed number");
	}

	@Test
	public void test_addButtonProductIsDisabled_whenPriceIsNegative__shouldShowError() {
		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(new Category("tech"));
			return null;
		});

		frameFixture.textBox("productNewName").enterText("product name");
		frameFixture.textBox("productNewPrice").enterText("-1111");
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);
		frameFixture.button("addProductButton").requireDisabled();
		frameFixture.label("errorLabel").requireVisible().requireText("Price must be an allowed number");
	}

	@Test
	public void test_addButtonProductIsEnabled_whenPriceIsZero() {
		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(new Category("tech"));
		});

		frameFixture.textBox("productNewName").enterText("product name");
		frameFixture.textBox("productNewPrice").enterText("0");
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);

		frameFixture.button("addProductButton").requireEnabled();
	}

	@Test
	public void test_updateAndDeleteButtonsProductAreActive_whenItemIsSelected() {
		Category category = new Category("tech");

		GuiActionRunner.execute(() -> {
			DefaultTableModel model = (DefaultTableModel) frameFixture.table("productTable").target().getModel();
			model.addRow(new Object[] { 1L, "product1", 99, category });
			model.addRow(new Object[] { 2L, "product2", 99, category });
		});

		frameFixture.table("productTable").selectRows(0);

		frameFixture.button("updateProductButton").requireEnabled();
		frameFixture.button("deleteProductButton").requireEnabled();
	}

	@Test
	public void test_errorLabel_getsRemovedWhenValidPriceIsFixedAndEnteredCorrectly() {
		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(new Category("tech"));
			return null;
		});

		frameFixture.textBox("productNewName").enterText("product name");
		frameFixture.textBox("productNewPrice").enterText("invalid");
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);

		frameFixture.label("errorLabel").requireText("Price must be an allowed number");

		frameFixture.textBox("productNewPrice").deleteText().enterText("99.99");
		frameFixture.label("errorLabel").requireText("");
	}

	@Test
	public void test_addCategoryButton_addsCategoryToTable() {
		frameFixture.textBox("newName").enterText("books");
		frameFixture.button("addCategoryButton").click();

		Category category = new Category("books");
		category.setId(1L);
		GuiActionRunner.execute(() -> catalogSwingView.addedCategory(category));

		frameFixture.table("categoryTable").requireRowCount(1);
		String name = frameFixture.table("categoryTable").valueAt(TableCell.row(0).column(1));
		assertThat(name).isEqualTo("books");
	}

	// CategortView test.

	@Test
	public void test_addedCategory_addsCategoryToTable() {
		Category category = new Category("Books");
		category.setId(1l);

		GuiActionRunner.execute(() -> {
			catalogSwingView.addedCategory(category);
			return null;
		});

		frameFixture.table("categoryTable").requireRowCount(1);
		String idAsString = frameFixture.table("categoryTable").valueAt(TableCell.row(0).column(0));
		String name = frameFixture.table("categoryTable").valueAt(TableCell.row(0).column(1));

		assertThat(idAsString).isEqualTo("1");
		assertThat(name).isEqualTo("Books");
	}

	@Test
	public void test_deletedCategory_removesCategoryFromTable() {
		Category category = new Category("Books");
		category.setId(1L);

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
	public void test_showError_displaysErrorMessage() {
		GuiActionRunner.execute(() -> catalogSwingView.showError("Test error message"));

		frameFixture.label("errorLabel").requireText("Test error message");
	}

	@Test
	public void test_updateCategory_updatesCategoryAndResetsError() {
		Category category = new Category("tech");
		category.setId(1L);
		GuiActionRunner.execute(() -> {
			catalogSwingView.addedCategory(category);
		});

		frameFixture.table("categoryTable").selectRows(0);

		Category updatedCategory = new Category("tech updated");
		updatedCategory.setId(1L);
		GuiActionRunner.execute(() -> catalogSwingView.updateCategory(updatedCategory));

		String updatedName = frameFixture.table("categoryTable").valueAt(TableCell.row(0).column(1));
		assertThat(updatedName).isEqualTo("tech updated");
		frameFixture.label("errorLabel").requireText("");
	}

	@Test
	public void test_findAllCategories_returnsData() {
		Category techCategory = new Category("tech");
		Category carCategory = new Category("car");
		techCategory.setId(1L);
		carCategory.setId(2L);

		GuiActionRunner.execute(() -> catalogSwingView.addedCategory(techCategory));
		GuiActionRunner.execute(() -> catalogSwingView.addedCategory(carCategory));
		GuiActionRunner.execute(() -> catalogSwingView.findAllCategories(Arrays.asList(techCategory, carCategory)));

		frameFixture.table("categoryTable").requireRowCount(2);
		assertThat(frameFixture.table("categoryTable").valueAt(TableCell.row(0).column(1))).isEqualTo("tech");
		assertThat(frameFixture.table("categoryTable").valueAt(TableCell.row(1).column(1))).isEqualTo("car");
	}

	@Test
	public void test_findAllCategories_withEmptyList_returnsEmptyTable() {
		GuiActionRunner.execute(() -> catalogSwingView.findAllCategories(Arrays.asList()));

		frameFixture.table("categoryTable").requireRowCount(0);
		frameFixture.label("errorLabel").requireText("");
	}

	// Product view impl methods

	@Test
	public void test_addedProduct_addsProductToTable() {
		Category category = new Category("tech");
		category.setId(1L);
		Product product = new Product("laptop", new BigDecimal("100"), category);
		product.setId(1L);

		GuiActionRunner.execute(() -> {
			catalogSwingView.addedProduct(product);
		});

		frameFixture.table("productTable").requireRowCount(1);
		String idAsString = frameFixture.table("productTable").valueAt(TableCell.row(0).column(0));
		String name = frameFixture.table("productTable").valueAt(TableCell.row(0).column(1));
		String price = frameFixture.table("productTable").valueAt(TableCell.row(0).column(2));
		String productCategory = frameFixture.table("productTable").valueAt(TableCell.row(0).column(3));

		assertThat(idAsString).isEqualTo("1");
		assertThat(name).isEqualTo("laptop");
		assertThat(price).isEqualTo("100");
		assertThat(productCategory).isEqualTo(category.toString());

	}

	@Test
	public void test_deletedProduct_removesProductFromTable() {
		Category category = new Category("tech");
		category.setId(1L);
		Product product = new Product("laptop", new BigDecimal("100"), category);
		product.setId(1L);

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
	public void test_updateProduct_updatesProductAndResetsError() {
		Category category = new Category("tech");
		category.setId(1L);
		Product product = new Product("laptop", new BigDecimal("100"), category);
		product.setId(1L);

		GuiActionRunner.execute(() -> {
			catalogSwingView.addedProduct(product);
		});

		frameFixture.table("productTable").selectRows(0);

		Product updatedProduct = new Product("hp-laptop", new BigDecimal("129"), category);
		updatedProduct.setId(1L);
		GuiActionRunner.execute(() -> catalogSwingView.updateProduct(updatedProduct));

		String updatedName = frameFixture.table("productTable").valueAt(TableCell.row(0).column(1));
		String updatedPrice = frameFixture.table("productTable").valueAt(TableCell.row(0).column(2));

		assertThat(updatedName).isEqualTo("hp-laptop");
		assertThat(updatedPrice).isEqualTo("129");

		frameFixture.label("errorLabel").requireText("");
	}

	@Test
	public void test_findAllProducts_returnsData() {
		Category techCategory = new Category("tech");
		techCategory.setId(1L);

		Product techProduct = new Product("laptop", new BigDecimal("100"), techCategory);
		Product techProductPhone = new Product("smartphone", new BigDecimal("50"), techCategory);
		techProduct.setId(1L);
		techProductPhone.setId(2L);

		GuiActionRunner.execute(() -> catalogSwingView.addedProduct(techProduct));
		GuiActionRunner.execute(() -> catalogSwingView.addedProduct(techProductPhone));
		GuiActionRunner.execute(() -> catalogSwingView.findAllProducts(Arrays.asList(techProduct, techProductPhone)));

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
		assertThat(product1Name).isEqualTo("laptop");
		assertThat(product1Price).isEqualTo("100");
		assertThat(product1Category).isEqualTo(techCategory.toString());

		assertThat(product2Id).isEqualTo("2");
		assertThat(product2Name).isEqualTo("smartphone");
		assertThat(product2Price).isEqualTo("50");
		assertThat(product2Category).isEqualTo(techCategory.toString());
	}

	@Test
	public void test_findAllProducts_withEmptyList_returnsEmptyTable() {
		GuiActionRunner.execute(() -> catalogSwingView.findAllProducts(Arrays.asList()));

		frameFixture.table("productTable").requireRowCount(0);
		frameFixture.label("errorLabel").requireText("");
	}

	// Testing that controllers are invoked when called.

	@Test
	public void test_categoryAddButton_shouldDelegateToCategoryControllerCreate() {
		frameFixture.textBox("newName").enterText("tech");
		frameFixture.button("addCategoryButton").click();

		verify(categoryController, times(1)).create("tech");
	}

	@Test
	public void test_categoryDeleteButton_shouldDelegateToCategoryControllerDelete() {
		GuiActionRunner.execute(() -> {
			DefaultTableModel model = (DefaultTableModel) frameFixture.table("categoryTable").target().getModel();
			model.addRow(new Object[] { 1L, "category 1" });
			model.addRow(new Object[] { 2L, "category 2" });
		});

		frameFixture.table("categoryTable").selectCell(TableCell.row(0).column(0));
		frameFixture.button("deleteCategoryButton").click();

		verify(categoryController, times(1)).delete(1L);
	}

	@Test
	public void test_categoryUpdateButton_shouldDelegateToCategoryControllerUpdate() {
		GuiActionRunner.execute(() -> {
			DefaultTableModel model = (DefaultTableModel) frameFixture.table("categoryTable").target().getModel();
			model.addRow(new Object[] { 1L, "category 1" });
			model.addRow(new Object[] { 2L, "category 2" });
		});

		frameFixture.table("categoryTable").selectCell(TableCell.row(0).column(0));

		frameFixture.textBox("newName").enterText("new name");
		frameFixture.button("updateCategoryButton").click();

		verify(categoryController, times(1)).update(1L, "new name");
	}

	@Test
	public void test_productAddButton_shouldDelegateToProductControllerCreate() {
		Category category = new Category("tech");
		category.setId(1L);

		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(category);
			return null;
		});

		frameFixture.textBox("productNewName").enterText("tech");
		frameFixture.textBox("productNewPrice").enterText(BigDecimal.valueOf(111).toString());
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);

		frameFixture.button("addProductButton").click();

		verify(productController, times(1)).create("tech", BigDecimal.valueOf(111), 1L);
	}

	@Test
	public void test_productDeleteButton_shouldDelegateToProductControllerDelete() {
		Category category = new Category("tech");

		GuiActionRunner.execute(() -> {
			DefaultTableModel model = (DefaultTableModel) frameFixture.table("productTable").target().getModel();
			model.addRow(new Object[] { 1L, "product1", 99, category });
			model.addRow(new Object[] { 2L, "product2", 99, category });
		});

		frameFixture.table("productTable").selectRows(0);
		frameFixture.button("deleteProductButton").click();

		verify(productController, times(1)).delete(1L);
	}

	@Test
	public void test_productUpdateButton_shouldDelegateToProductControllerUpdate() {
		Category category = new Category("tech");
		category.setId(1L);
		Category newCategory = new Category("books");
		newCategory.setId(2L);

		GuiActionRunner.execute(() -> {
			DefaultTableModel model = (DefaultTableModel) frameFixture.table("productTable").target().getModel();
			model.addRow(new Object[] { 1L, "product1", 99, category });
			model.addRow(new Object[] { 2L, "product2", 99, category });
		});

		GuiActionRunner.execute(() -> {
			@SuppressWarnings("unchecked")
			JComboBox<Category> comboBox = frameFixture.comboBox("productCategorySelectBox").target();
			comboBox.addItem(category);
			comboBox.addItem(newCategory);
			return null;
		});

		frameFixture.table("productTable").selectRows(0);
		frameFixture.textBox("productNewName").enterText("iphone");
		frameFixture.textBox("productNewPrice").enterText(BigDecimal.valueOf(111).toString());
		frameFixture.comboBox("productCategorySelectBox").selectItem(1);

		frameFixture.button("updateProductButton").click();

		verify(productController, times(1)).update(1L, "iphone", BigDecimal.valueOf(111), 2L);
	}

	@Test
	public void test_findAllCategories_shoudlPopulateAlsoTheComboBox() {
		Category tech = new Category("tech");
		tech.setId(1L);
		Category books = new Category("books");
		books.setId(2L);
		List<Category> categories = Arrays.asList(tech, books);

		GuiActionRunner.execute(() -> {
			catalogSwingView.findAllCategories(categories);
			return null;
		});

		JComboBoxFixture comboBoxFixture = frameFixture.comboBox("productCategorySelectBox");

		assertThat(comboBoxFixture.target().getItemCount()).isEqualTo(2);
		assertThat(comboBoxFixture.contents()).containsExactly("tech", "books");

		Category firstItem = (Category) comboBoxFixture.target().getItemAt(0);
		Category secondItem = (Category) comboBoxFixture.target().getItemAt(1);

		assertThat(firstItem.getName()).isEqualTo("tech");
		assertThat(firstItem.getId()).isEqualTo(1L);
		assertThat(secondItem.getName()).isEqualTo("books");
		assertThat(secondItem.getId()).isEqualTo(2L);
	}
}
