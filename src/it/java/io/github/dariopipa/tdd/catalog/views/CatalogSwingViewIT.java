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
		categoryService = new CategoryService(categoryRepository, transactionManager);
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
		frameFixture.textBox("newName").enterText("tech");
		frameFixture.button("addCategoryButton").click();
		frameFixture.table("categoryTable").requireRowCount(1);

		String[][] tableContent = frameFixture.table("categoryTable").contents();
		assertThat(categoryRepository.findByName("tech").getName()).isEqualTo("tech");
		assertThat(tableContent[0][1]).isEqualTo("tech");
	}

	@Test
	@GUITest
	public void test_findAllCategoriesIT() {
		GuiActionRunner.execute(() -> categoryController.create("tech"));
		GuiActionRunner.execute(() -> categoryController.create("books"));

		frameFixture.table("categoryTable").requireRowCount(2);
		String[][] tableContent = frameFixture.table("categoryTable").contents();

		assertThat(categoryRepository.findByName("tech")).isNotNull();
		assertThat(categoryRepository.findByName("books")).isNotNull();

		assertThat(tableContent[0][1]).isEqualTo("tech");
		assertThat(tableContent[1][1]).isEqualTo("books");
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
		GuiActionRunner.execute(() -> categoryController.delete(999L));

		frameFixture.label("errorLabel").requireText("category with id:999 not found");
	}

	@Test
	@GUITest
	public void test_updateCategoryThatExistsIT() {
		GuiActionRunner.execute(() -> categoryController.create("tech"));

		frameFixture.table("categoryTable").selectRows(0);

		frameFixture.textBox("newName").enterText("books");
		frameFixture.button("updateCategoryButton").click();

		GuiActionRunner.execute(() -> categoryController.findAll());

		String[][] tableContent = frameFixture.table("categoryTable").contents();
		assertThat(tableContent[0][1]).isEqualTo("books");
	}

	@Test
	@GUITest
	public void test_updateCategoryThatDoesNotExist_shouldShowErrorIT() {
		GuiActionRunner.execute(() -> categoryController.update(999L, "book"));

		frameFixture.label("errorLabel").requireText("category with id:999 not found");
	}

	@Test
	@GUITest
	public void test_addProductIT() {
		GuiActionRunner.execute(() -> categoryController.create("tech"));
		GuiActionRunner.execute(() -> categoryController.findAll());

		frameFixture.textBox("productNewName").enterText("laptop");
		frameFixture.textBox("productNewPrice").enterText("1000");
		frameFixture.comboBox("productCategorySelectBox").selectItem(0);
		frameFixture.button("addProductButton").click();

		String[][] tableContent = frameFixture.table("productTable").contents();
		assertThat(tableContent[0][1]).isEqualTo("laptop");
		assertThat(tableContent[0][2]).isEqualTo("1000");
		assertThat(tableContent[0][3]).contains("tech");
	}

	@Test
	@GUITest
	public void test_findAllProductsIT() {
		GuiActionRunner.execute(() -> {
			categoryController.create("tech");
			categoryController.create("books");
		});

		Long techId = categoryRepository.findByName("tech").getId();
		Long booksId = categoryRepository.findByName("books").getId();

		GuiActionRunner.execute(() -> {
			productController.create("laptop", new BigDecimal("1000"), techId);
			productController.create("novel", new BigDecimal("15"), booksId);
		});

		GuiActionRunner.execute(() -> productController.findAll());

		frameFixture.table("productTable").requireRowCount(2);
		String[][] tableContent = frameFixture.table("productTable").contents();

		assertThat(tableContent[0][1]).isEqualTo("laptop");
		assertThat(tableContent[0][2]).isEqualTo("1000");
		assertThat(tableContent[0][3]).contains("tech");

		assertThat(tableContent[1][1]).isEqualTo("novel");
		assertThat(tableContent[1][2]).isEqualTo("15");
		assertThat(tableContent[1][3]).contains("books");
	}

	@Test
	@GUITest
	public void test_updateProductThatExistsIT() {
		GuiActionRunner.execute(() -> {
			categoryController.create("tech");
			categoryController.findAll();
		});
		Long techId = categoryRepository.findByName("tech").getId();

		GuiActionRunner.execute(() -> {
			productController.create("laptop", new BigDecimal("1000"), techId);
		});

		GuiActionRunner.execute(() -> productController.findAll());

		frameFixture.table("productTable").selectRows(0);
		frameFixture.textBox("productNewName").setText("iphone");
		frameFixture.textBox("productNewPrice").setText("1200");
		frameFixture.button("updateProductButton").click();

		GuiActionRunner.execute(() -> productController.findAll());

		String[][] productTable = frameFixture.table("productTable").contents();
		assertThat(productTable[0][1]).isEqualTo("iphone");
		assertThat(productTable[0][2]).isEqualTo("1200");
		assertThat(productTable[0][3]).contains("tech");
	}

	@Test
	@GUITest
	public void test_deleteProductIT() {
		GuiActionRunner.execute(() -> {
			categoryController.create("tech");
		});

		Long techId = categoryRepository.findByName("tech").getId();
		productController.create("laptop", new BigDecimal("1000"), techId);

		GuiActionRunner.execute(() -> productController.findAll());

		frameFixture.table("productTable").requireRowCount(1);
		frameFixture.table("productTable").selectRows(0);
		frameFixture.button("deleteProductButton").click();
		frameFixture.table("productTable").requireRowCount(0);
	}

	@Test
	@GUITest
	public void test_deleteProductThatDoesNotExist_shouldShowErrorIT() {
		GuiActionRunner.execute(() -> productController.delete(999L));
		frameFixture.label("errorLabel").requireText("product with id: 999 not found");
	}

	@Test
	@GUITest
	public void test_updateProductThatDoesNotExist_shouldShowErrorIT() {
		GuiActionRunner.execute(() -> {
			categoryController.create("tech");
		});

		Long techId = categoryRepository.findByName("tech").getId();
		productController.update(999L, "updated", new BigDecimal("1"), techId);

		frameFixture.label("errorLabel").requireText("Invalid input: product with id: 999 not found");
	}

}
