package io.github.dariopipa.tdd.catalog.views;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Dimension;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import io.github.dariopipa.tdd.catalog.controllers.CategoryController;
import io.github.dariopipa.tdd.catalog.controllers.ProductController;
import io.github.dariopipa.tdd.catalog.repository.JpaCategoryRepositoryImpl;
import io.github.dariopipa.tdd.catalog.repository.JpaProductRepositoryImpl;
import io.github.dariopipa.tdd.catalog.service.CategoryService;
import io.github.dariopipa.tdd.catalog.service.ProductService;
import io.github.dariopipa.tdd.catalog.transactionManger.JPATransactionManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@RunWith(GUITestRunner.class)
public class CatalogSwingViewIT extends AssertJSwingJUnitTestCase {

	private static final String CATEGORY_NAME = "tech";
	private static final String CATEGORY_UPDATE_NAME = "books";

	private static final String PRODUCT_NAME = "laptop";
	private static final String PRODUCT_UPDATED_NAME = "iphone";

	private static final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(100);
	private static final BigDecimal PRODUCT_UPDATED_PRICE = BigDecimal.valueOf(120);

	private static final Long MISSING_ID = 999L;

	private static EntityManagerFactory emf;
	private EntityManager em;
	private JpaCategoryRepositoryImpl categoryRepository;
	private JpaProductRepositoryImpl productRepository;
	private JPATransactionManager transactionManager;
	private CategoryService categoryService;
	private ProductService productService;
	private CategoryController categoryController;
	private ProductController productController;
	private CatalogSwingView catalogSwingView;
	private FrameFixture frameFixture;

	@SuppressWarnings("resource")
	@ClassRule
	public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
			DockerImageName.parse("postgres:16-alpine")).withUsername("test").withPassword("test");

	@BeforeClass
	public static void beforeAll() {

		Map<String, Object> props = new HashMap<>();
		props.put("jakarta.persistence.jdbc.url", postgres.getJdbcUrl());
		props.put("jakarta.persistence.jdbc.user", postgres.getUsername());
		props.put("jakarta.persistence.jdbc.password", postgres.getPassword());
		props.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
		props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		props.put("hibernate.hbm2ddl.auto", "create-drop");

		emf = Persistence.createEntityManagerFactory("product-catalog-IM-PU", props);
	}

	@Override
	protected void onSetUp() {
		em = emf.createEntityManager();
		transactionManager = new JPATransactionManager(em);
		categoryRepository = new JpaCategoryRepositoryImpl(em);
		productRepository = new JpaProductRepositoryImpl(em);
		categoryService = new CategoryService(categoryRepository, transactionManager, productRepository);
		productService = new ProductService(productRepository, categoryService, transactionManager);
		categoryController = new CategoryController(categoryService, catalogSwingView);
		productController = new ProductController(productService, catalogSwingView);

		// Clean DB
		em.getTransaction().begin();
		em.createQuery("DELETE FROM Product").executeUpdate();
		em.createQuery("DELETE FROM Category").executeUpdate();
		em.getTransaction().commit();

		GuiActionRunner.execute(() -> {
			catalogSwingView = new CatalogSwingView();
			categoryController = new CategoryController(categoryService, catalogSwingView);
			productController = new ProductController(productService, catalogSwingView);

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
	public void test_addCategoryIT() {
		frameFixture.textBox("newName").enterText(CATEGORY_NAME);
		frameFixture.button("addCategoryButton").click();
		frameFixture.table("categoryTable").requireRowCount(1);

		String[][] tableContent = frameFixture.table("categoryTable").contents();
		assertThat(categoryRepository.findByName(CATEGORY_NAME).getName()).isEqualTo(CATEGORY_NAME);
		assertThat(tableContent[0][1]).isEqualTo(CATEGORY_NAME);
	}

	@Test
	@GUITest
	public void test_findAllCategoriesIT() {
		GuiActionRunner.execute(() -> categoryController.create(CATEGORY_NAME));
		GuiActionRunner.execute(() -> categoryController.create(CATEGORY_UPDATE_NAME));

		frameFixture.table("categoryTable").requireRowCount(2);
		String[][] tableContent = frameFixture.table("categoryTable").contents();

		assertThat(categoryRepository.findByName(CATEGORY_NAME)).isNotNull();
		assertThat(categoryRepository.findByName(CATEGORY_UPDATE_NAME)).isNotNull();

		assertThat(tableContent[0][1]).isEqualTo(CATEGORY_NAME);
		assertThat(tableContent[1][1]).isEqualTo(CATEGORY_UPDATE_NAME);
	}

	@Test
	@GUITest
	public void test_deleteCategoryIT() {
		GuiActionRunner.execute(() -> categoryController.create("delete-category"));

		frameFixture.table("categoryTable").selectRows(0);
		frameFixture.button("deleteCategoryButton").click();

		frameFixture.table("categoryTable").requireRowCount(0);
	}

	@Test
	@GUITest
	public void test_deleteCategoryThatDoesNotExist_shouldShowErrorIT() {
		GuiActionRunner.execute(() -> categoryController.delete(MISSING_ID));

		frameFixture.label("errorLabel").requireText("category with id:" + MISSING_ID + " not found");
	}

	@Test
	@GUITest
	public void test_updateCategoryThatExistsIT() {
		GuiActionRunner.execute(() -> categoryController.create(CATEGORY_NAME));

		frameFixture.table("categoryTable").selectRows(0);

		frameFixture.textBox("newName").enterText(CATEGORY_UPDATE_NAME);
		frameFixture.button("updateCategoryButton").click();

		GuiActionRunner.execute(() -> categoryController.findAll());

		String[][] tableContent = frameFixture.table("categoryTable").contents();
		assertThat(tableContent[0][1]).isEqualTo(CATEGORY_UPDATE_NAME);
	}

	@Test
	@GUITest
	public void test_updateCategoryThatDoesNotExist_shouldShowErrorIT() {
		GuiActionRunner.execute(() -> categoryController.update(MISSING_ID, CATEGORY_UPDATE_NAME));

		frameFixture.label("errorLabel").requireText("category with id:" + MISSING_ID + " not found");
	}

	@Test
	@GUITest
	public void test_addProductIT() {
		GuiActionRunner.execute(() -> categoryController.create(CATEGORY_NAME));
		GuiActionRunner.execute(() -> categoryController.findAll());

		frameFixture.textBox("productNewName").enterText(PRODUCT_NAME);
		frameFixture.textBox("productNewPrice").enterText(PRODUCT_PRICE.toString());
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);
		frameFixture.button("addProductButton").click();

		String[][] tableContent = frameFixture.table("productTable").contents();
		assertThat(tableContent[0][1]).isEqualTo(PRODUCT_NAME);
		assertThat(tableContent[0][2]).isEqualTo(PRODUCT_PRICE.toString());
		assertThat(tableContent[0][3]).contains(CATEGORY_NAME);
	}

	@Test
	@GUITest
	public void test_findAllProductsIT() {
		GuiActionRunner.execute(() -> {
			categoryController.create(CATEGORY_NAME);
			categoryController.create(CATEGORY_UPDATE_NAME);
		});

		Long techId = categoryRepository.findByName(CATEGORY_NAME).getId();
		Long categoryId = categoryRepository.findByName(CATEGORY_UPDATE_NAME).getId();

		GuiActionRunner.execute(() -> {
			productController.create(PRODUCT_NAME, PRODUCT_PRICE, techId);
			productController.create(PRODUCT_UPDATED_NAME, PRODUCT_UPDATED_PRICE, categoryId);
		});

		GuiActionRunner.execute(() -> productController.findAll());

		frameFixture.table("productTable").requireRowCount(2);
		String[][] tableContent = frameFixture.table("productTable").contents();

		assertThat(tableContent[0][1]).isEqualTo(PRODUCT_NAME);
		assertThat(tableContent[0][2]).isEqualTo(PRODUCT_PRICE.toString());
		assertThat(tableContent[0][3]).contains(CATEGORY_NAME);

		assertThat(tableContent[1][1]).isEqualTo(PRODUCT_UPDATED_NAME);
		assertThat(tableContent[1][2]).isEqualTo(PRODUCT_UPDATED_PRICE.toString());
		assertThat(tableContent[1][3]).contains(CATEGORY_UPDATE_NAME);
	}

	@Test
	@GUITest
	public void test_updateProductThatExistsIT() {
		GuiActionRunner.execute(() -> {
			categoryController.create(CATEGORY_NAME);
			categoryController.findAll();
		});
		Long techId = categoryRepository.findByName(CATEGORY_NAME).getId();

		GuiActionRunner.execute(() -> productController.create(PRODUCT_NAME, PRODUCT_PRICE, techId));
		GuiActionRunner.execute(() -> productController.findAll());

		frameFixture.table("productTable").selectRows(0);

		frameFixture.comboBox("productCategorySelectBox").selectItem(0);
		frameFixture.textBox("productNewName").enterText(PRODUCT_UPDATED_NAME);
		frameFixture.textBox("productNewPrice").enterText(PRODUCT_UPDATED_PRICE.toString());
		frameFixture.button("updateProductButton").click();

		GuiActionRunner.execute(() -> productController.findAll());

		String[][] productTable = frameFixture.table("productTable").contents();
		assertThat(productTable[0][1]).isEqualTo(PRODUCT_UPDATED_NAME);
		assertThat(productTable[0][2]).isEqualTo(PRODUCT_UPDATED_PRICE.toString());
		assertThat(productTable[0][3]).contains(CATEGORY_NAME);
	}

	@Test
	@GUITest
	public void test_deleteProductIT() {
		GuiActionRunner.execute(() -> {
			categoryController.create(CATEGORY_NAME);
		});

		Long techId = categoryRepository.findByName(CATEGORY_NAME).getId();
		GuiActionRunner.execute(() -> {
			productController.create(PRODUCT_NAME, PRODUCT_PRICE, techId);
		});

		GuiActionRunner.execute(() -> productController.findAll());

		frameFixture.table("productTable").requireRowCount(1);
		frameFixture.table("productTable").selectRows(0);
		frameFixture.button("deleteProductButton").click();
		frameFixture.table("productTable").requireRowCount(0);
	}

	@Test
	@GUITest
	public void test_deleteProductThatDoesNotExist_shouldShowErrorIT() {
		GuiActionRunner.execute(() -> productController.delete(MISSING_ID));
		frameFixture.label("errorLabel").requireText("product with id: " + MISSING_ID + " not found");
	}

	@Test
	@GUITest
	public void test_updateProductThatDoesNotExist_shouldShowErrorIT() {
		GuiActionRunner.execute(() -> {
			categoryController.create(CATEGORY_NAME);
		});

		Long techId = categoryRepository.findByName(CATEGORY_NAME).getId();
		GuiActionRunner.execute(() -> {
			productController.update(MISSING_ID, PRODUCT_UPDATED_NAME, PRODUCT_UPDATED_PRICE, techId);
		});

		frameFixture.label("errorLabel").requireText("Invalid input: product with id: " + MISSING_ID + " not found");
	}

	@Test
	@GUITest
	public void test_deleteCategoryThatIsUsedByProducts_shouldShowErrorIt() {
		GuiActionRunner.execute(() -> {
			categoryController.create(CATEGORY_NAME);
		});

		Long categoryId = categoryRepository.findByName(CATEGORY_NAME).getId();
		GuiActionRunner.execute(() -> {
			productController.create(PRODUCT_NAME, PRODUCT_PRICE, categoryId);
			productController.create(PRODUCT_UPDATED_NAME, PRODUCT_UPDATED_PRICE, categoryId);
		});

		frameFixture.table("categoryTable").selectRows(0);
		frameFixture.button("deleteCategoryButton").click();

		frameFixture.table("categoryTable").requireRowCount(1);
		frameFixture.label("errorLabel").requireText("Category in use by existing products");
	}

}
