package io.github.dariopipa.tdd.catalog.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import io.github.dariopipa.tdd.catalog.entities.Category;
import io.github.dariopipa.tdd.catalog.entities.Product;

public class CatalogSwingView extends JFrame implements CategoryView, ProductView {

	private static final long serialVersionUID = 1L;
	private JTable productTable;
	private JTable categoryTable;
	private JTextField newName;
	private JTextField productNewName;
	private JTextField productNewPrice;
	private JComboBox<Category> productCategorySelectBox;
	private JLabel errorLabel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CatalogSwingView frame = new CatalogSwingView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public CatalogSwingView() {
		setFont(new Font("Arial", Font.BOLD, 12));
		setTitle("Catalog");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(450, 450, 566, 450);
		getContentPane().setLayout(null);

		JPanel categoryPanel = new JPanel();
		categoryPanel.setMaximumSize(new Dimension(40000, 40000));
		categoryPanel.setMinimumSize(new Dimension(14, 15));
		categoryPanel.setName("categoryPanel");
		categoryPanel.setBounds(10, 37, 228, 329);
		getContentPane().add(categoryPanel);
		categoryPanel.setLayout(null);

		JLabel categoryName = new JLabel("Categories");
		categoryName.setFont(new Font("Tahoma", Font.BOLD, 16));
		categoryName.setPreferredSize(new Dimension(58, 14));
		categoryName.setBounds(70, 0, 95, 35);
		categoryName.setName("categoryName");
		categoryPanel.add(categoryName);

		JButton addCategoryButton = new JButton("Add New Category");
		addCategoryButton.setEnabled(false);
		addCategoryButton.setName("addCategoryButton");
		addCategoryButton.setBounds(52, 229, 123, 23);
		categoryPanel.add(addCategoryButton);

		JButton deleteCategoryButton = new JButton("Delete Selected");
		deleteCategoryButton.setEnabled(false);
		deleteCategoryButton.setName("deleteCategoryButton");
		deleteCategoryButton.setBounds(52, 303, 123, 23);
		categoryPanel.add(deleteCategoryButton);

		JButton updateCategoryButton = new JButton("Update Selected");
		updateCategoryButton.setEnabled(false);
		updateCategoryButton.setName("updateCategoryButton");
		updateCategoryButton.setBounds(52, 263, 123, 23);
		categoryPanel.add(updateCategoryButton);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(52, 34, 132, 134);
		categoryPanel.add(scrollPane_1);

		categoryTable = new JTable();
		categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		categoryTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int selectedRow = categoryTable.getSelectedRow();
				boolean isRowSelected = selectedRow >= 0;
				updateCategoryButton.setEnabled(isRowSelected);
				deleteCategoryButton.setEnabled(isRowSelected);
			}
		});
		categoryTable.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Id", "Name" }) {
			Class[] columnTypes = new Class[] { Long.class, String.class };

			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		categoryTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		categoryTable.getColumnModel().getColumn(1).setPreferredWidth(96);
		categoryTable.setName("categoryTable");
		scrollPane_1.setViewportView(categoryTable);

		newName = new JTextField();
		newName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String text = newName.getText().trim();
				addCategoryButton.setEnabled(!text.isEmpty());
			}
		});
		newName.setName("newName");
		newName.setBounds(119, 198, 86, 20);
		categoryPanel.add(newName);
		newName.setColumns(10);

		JLabel newNameLabel = new JLabel("New Name");
		newNameLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
		newNameLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		newNameLabel.setName("newNameLabel");
		newNameLabel.setBounds(10, 200, 86, 14);
		categoryPanel.add(newNameLabel);

		JPanel productPanel = new JPanel();
		productPanel.setBorder(null);
		productPanel.setName("productPanel");
		productPanel.setBounds(236, 37, 292, 329);
		getContentPane().add(productPanel);
		productPanel.setLayout(null);

		JLabel lblProducts = new JLabel("Products");
		lblProducts.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblProducts.setBounds(88, 0, 77, 30);
		lblProducts.setPreferredSize(new Dimension(58, 14));
		lblProducts.setName("productName");
		productPanel.add(lblProducts);

		JButton addProductButton = new JButton("Add New Product");
		addProductButton.setEnabled(false);
		addProductButton.setName("addProductButton");
		addProductButton.setBounds(10, 275, 123, 23);
		productPanel.add(addProductButton);

		JButton deleteProductButton = new JButton("Delete Selected Product");
		deleteProductButton.setEnabled(false);
		deleteProductButton.setName("deleteProductButton");
		deleteProductButton.setBounds(90, 303, 123, 23);
		productPanel.add(deleteProductButton);

		JButton updateProductButton = new JButton("Update Selected Product");
		updateProductButton.setEnabled(false);
		updateProductButton.setName("updateProductButton");
		updateProductButton.setBounds(159, 273, 123, 23);
		productPanel.add(updateProductButton);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setFocusable(false);
		scrollPane.setName("productTable");
		scrollPane.setBounds(38, 28, 223, 138);
		productPanel.add(scrollPane);

		productTable = new JTable();
		productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		productTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int selectedRow = productTable.getSelectedRow();
				boolean isRowSelected = selectedRow >= 0;
				updateProductButton.setEnabled(isRowSelected);
				deleteProductButton.setEnabled(isRowSelected);
			}
		});
		productTable
				.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Id", "Name", "Price", "Category" }) {
					Class[] columnTypes = new Class[] { Integer.class, String.class, Long.class, Object.class };

					public Class getColumnClass(int columnIndex) {
						return columnTypes[columnIndex];
					}

					boolean[] columnEditables = new boolean[] { false, false, false, false };

					public boolean isCellEditable(int row, int column) {
						return columnEditables[column];
					}
				});
		productTable.setName("productTable");
		scrollPane.setViewportView(productTable);

		JLabel productNameLabel = new JLabel("Name");
		productNameLabel.setName("productNameLabel");
		productNameLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		productNameLabel.setBounds(20, 177, 57, 25);
		productPanel.add(productNameLabel);

		JLabel productPriceLabel = new JLabel("Price");
		productPriceLabel.setName("productPriceLabel");
		productPriceLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		productPriceLabel.setBounds(20, 212, 57, 23);
		productPanel.add(productPriceLabel);

		JLabel productCategoryLabel = new JLabel("Category");
		productCategoryLabel.setName("productCategoryLabel");
		productCategoryLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		productCategoryLabel.setBounds(20, 234, 57, 30);
		productPanel.add(productCategoryLabel);

		productNewName = new JTextField();
		productNewName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				addProductButton.setEnabled(
						!productNewName.getText().trim().isEmpty() && validatePrice(productNewPrice.getText())
								&& productCategorySelectBox.getSelectedItem() != null);
			}
		});
		productNewName.setName("productNewName");
		productNewName.setBounds(88, 181, 86, 20);
		productPanel.add(productNewName);
		productNewName.setColumns(10);

		productNewPrice = new JTextField();
		productNewPrice.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				addProductButton.setEnabled(
						!productNewName.getText().trim().isEmpty() && validatePrice(productNewPrice.getText())
								&& productCategorySelectBox.getSelectedItem() != null);
			}
		});
		productNewPrice.setName("productNewPrice");
		productNewPrice.setColumns(10);
		productNewPrice.setBounds(88, 212, 86, 20);
		productPanel.add(productNewPrice);

		productCategorySelectBox = new JComboBox<Category>();
		productCategorySelectBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addProductButton.setEnabled(
						!productNewName.getText().trim().isEmpty() && validatePrice(productNewPrice.getText())
								&& productCategorySelectBox.getSelectedItem() != null);
			}
		});
		productCategorySelectBox.setName("productCategorySelectBox");
		productCategorySelectBox.setBounds(88, 240, 86, 22);
		productPanel.add(productCategorySelectBox);

		JPanel titlePanel = new JPanel();
		titlePanel.setName("titlePanel");
		titlePanel.setBounds(10, 11, 530, 23);
		getContentPane().add(titlePanel);

		JLabel title = new JLabel("Catalog");
		title.setFont(new Font("Tahoma", Font.BOLD, 16));
		title.setName("titleLabel");
		titlePanel.add(title);

		errorLabel = new JLabel("");
		errorLabel.setName("errorLabel");
		errorLabel.setForeground(new Color(255, 0, 0));
		errorLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		errorLabel.setBounds(32, 366, 496, 23);
		getContentPane().add(errorLabel);
	}

	private Boolean validatePrice(String priceString) {
		try {
			BigDecimal price = new BigDecimal(priceString);
			boolean isValid = price.compareTo(BigDecimal.ZERO) >= 0;

			if (isValid) {
				errorLabel.setText("");
			} else {
				errorLabel.setText("Price must be an allowed number");
			}

			return isValid;
		} catch (NumberFormatException e) {
			errorLabel.setText("Price must be an allowed number");
			return false;
		}
	}

	private DefaultTableModel categoryModel() {
		return (DefaultTableModel) categoryTable.getModel();
	}

	private DefaultTableModel productModel() {
		return (DefaultTableModel) productTable.getModel();
	}

	@Override
	public void addedProduct(Product product) {
		productModel()
				.addRow(new Object[] { product.getId(), product.getName(), product.getPrice(), product.getCategory() });

		showError("");
	}

	@Override
	public void deletedProduct(Product product) {
		int selectedRow = productTable.getSelectedRow();
		if (selectedRow != -1) {
			productModel().removeRow(selectedRow);
		}
	}

	@Override
	public void updateProduct(Product product) {
		int selectedRow = productTable.getSelectedRow();
		if (selectedRow != -1) {
			productModel().setValueAt(product.getName(), selectedRow, 1);
			productModel().setValueAt(product.getPrice(), selectedRow, 2);
			productModel().setValueAt(product.getCategory(), selectedRow, 3);
		}
		showError("");
	}

	@Override
	public void findAllProducts(List<Product> products) {
		productModel().setRowCount(0);
		products.forEach(product -> productModel().addRow(
				new Object[] { product.getId(), product.getName(), product.getPrice(), product.getCategory() }));
		showError("");
	}

	@Override
	public void addedCategory(Category category) {
		categoryModel().addRow(new Object[] { category.getId(), category.getName() });

		showError("");
	}

	@Override
	public void showError(String msg) {
		errorLabel.setText(msg);
	}

	@Override
	public void deletedCategory(Category category) {
		int selectedRow = categoryTable.getSelectedRow();
		if (selectedRow != -1) {
			categoryModel().removeRow(selectedRow);
		}
	}

	@Override
	public void updateCategory(Category category) {
		int selectedRow = categoryTable.getSelectedRow();
		if (selectedRow != -1) {
			categoryModel().setValueAt(category.getName(), selectedRow, 1);
		}
		showError("");
	}

	@Override
	public void findAllCategories(List<Category> categories) {
		categoryModel().setRowCount(0);
		categories.forEach(category -> categoryModel().addRow(new Object[] { category.getId(), category.getName() }));
		showError("");
	}

}
