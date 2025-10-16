package io.github.dariopipa.tdd.catalog;

import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import io.github.dariopipa.tdd.catalog.controllers.CategoryController;
import io.github.dariopipa.tdd.catalog.controllers.ProductController;
import io.github.dariopipa.tdd.catalog.repository.CategoryRepository;
import io.github.dariopipa.tdd.catalog.repository.JpaCategoryRepositoryImpl;
import io.github.dariopipa.tdd.catalog.repository.JpaProductRepositoryImpl;
import io.github.dariopipa.tdd.catalog.repository.ProductRepository;
import io.github.dariopipa.tdd.catalog.service.CategoryService;
import io.github.dariopipa.tdd.catalog.service.ProductService;
import io.github.dariopipa.tdd.catalog.transactionManger.JPATransactionManager;
import io.github.dariopipa.tdd.catalog.views.CatalogSwingView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true)
public class App implements Callable<Void> {

	@Option(names = { "--jdbc-url" }, description = "JDBC URL")
	private String jdbcUrl = "jdbc:postgresql://localhost:5432/tdd_product_catalog";

	@Option(names = { "--jdbc-user" }, description = "Database user")
	private String jdbcUser = "postgres";

	@Option(names = { "--jdbc-password" }, description = "Database password")
	private String jdbcPassword = "postgres";

	private static final String JDBC_DRIVER = "org.postgresql.Driver";
	private static final String HIBERNATE_DIALECT = "org.hibernate.dialect.PostgreSQLDialect";
	private static final String HIBERNATE_HBM2DDL_AUTO = "create-drop";

	public static void main(String[] args) {
		System.out.println("app started");
		new CommandLine(new App()).execute(args);
	}

	@Override
	public Void call() throws Exception {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Map<String, Object> props = new HashMap<>();

					props.put("jakarta.persistence.jdbc.url", jdbcUrl);
					props.put("jakarta.persistence.jdbc.user", jdbcUser);
					props.put("jakarta.persistence.jdbc.password", jdbcPassword);
					props.put("jakarta.persistence.jdbc.driver", JDBC_DRIVER);
					props.put("hibernate.dialect", HIBERNATE_DIALECT);
					props.put("hibernate.hbm2ddl.auto", HIBERNATE_HBM2DDL_AUTO);

					EntityManagerFactory emf = Persistence.createEntityManagerFactory("product-catalogPU", props);
					EntityManager em = emf.createEntityManager();

					JPATransactionManager transactionManager = new JPATransactionManager(em);
					CategoryRepository categoryRepo = new JpaCategoryRepositoryImpl(em);
					ProductRepository productRepo = new JpaProductRepositoryImpl(em);

					CategoryService categoryService = new CategoryService(categoryRepo, transactionManager,
							productRepo);
					ProductService productService = new ProductService(productRepo, categoryService,
							transactionManager);

					CatalogSwingView view = new CatalogSwingView();
					CategoryController categoryController = new CategoryController(categoryService, view);
					ProductController productController = new ProductController(productService, view);

					view.setCategoryController(categoryController);
					view.setProductController(productController);

					productController.findAll();
					view.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return null;
	}
}
