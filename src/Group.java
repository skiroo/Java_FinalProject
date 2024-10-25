/**
 * Libraries
 */
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;


/**
 * The Group class represents the user interface for managing expenses within a group.
 * It allows users to add, edit, and remove expenses in a specific group,
 * and view the details of the group's expenses.
 *
 * This class handles interaction with the expenses table in the database and
 * displays a list of expenses for the selected group.
 */
public class Group extends JFrame {

    // Database connection parameters
    private static final String DB_URL = "jdbc:mariadb://localhost:3307/homex_db";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";

    // GUI components
    JPanel headerPanel, userInfoPanel, leftPanel, centerPanel;
    JButton homeButton, personalWalletButton, profileButton, logOutButton, addExpenseButton, editExpenseButton, removeExpenseButton;
    JLabel pageNameLabel, appNameLabel, usernameLabel, avatarLabel;
    JTable expenseTable;
    DefaultTableModel tableModel;
    private static Image app_logo = Toolkit.getDefaultToolkit().getImage("data/image/logo.png");

    private int groupId;

    /**
     * Constructor to initialize GUI components
     * @param groupID: ID of the group accessed
     */
    public Group(int groupID) {
        this.groupId = groupID;

        // Create the expenses table if it does not exist
        createExpenseTableIfNotExists();

        // Frame setup
        setTitle("Group Details");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setIconImage(app_logo);
        setLayout(new BorderLayout(0, 0));  // No gaps between regions
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Get the group name from the database using the groupID
        String groupName = getGroupNameById(groupID);

        // Create the header panel
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(12, 73, 87));
        headerPanel.setPreferredSize(new Dimension(1000, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Page name label (left side) now shows the group name
        pageNameLabel = new JLabel(groupName != null ? groupName : "Group");
        pageNameLabel.setForeground(Color.WHITE);
        pageNameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(pageNameLabel, BorderLayout.WEST);

        // App name label (center)
        appNameLabel = new JLabel("HomeEx", SwingConstants.CENTER);
        appNameLabel.setForeground(Color.WHITE);
        appNameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(appNameLabel, BorderLayout.CENTER);

        // User information panel (right side)
        userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userInfoPanel.setOpaque(false); // Make the panel transparent

        usernameLabel = new JLabel(User.getUsername());
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));

        avatarLabel = new JLabel();
        if (User.getAvatarPath() != null) {
            ImageIcon avatarIcon = new ImageIcon(User.getAvatarPath());
            Image scaledImage = avatarIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            avatarLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            avatarLabel.setText("No Avatar");
            avatarLabel.setForeground(Color.WHITE);
        }

        userInfoPanel.add(usernameLabel);
        userInfoPanel.add(avatarLabel);
        headerPanel.add(userInfoPanel, BorderLayout.EAST);

        // Add the header panel to the frame
        add(headerPanel, BorderLayout.NORTH);

        // Create the left-side panel with buttons
        leftPanel = new JPanel();
        leftPanel.setBackground(new Color(12, 73, 87, 179));
        leftPanel.setLayout(new GridLayout(4, 1, 10, 10));
        leftPanel.setPreferredSize(new Dimension(200, 0));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));   // Add padding around the buttons
        add(leftPanel, BorderLayout.WEST);  // Add to the left side

        // Home button
        homeButton = new JButton("Home");
        homeButton.addActionListener(_ -> {
            new Home();
            dispose();
        });
        leftPanel.add(homeButton);

        // Personal Wallet button
        personalWalletButton = new JButton("Personal Wallet");
        personalWalletButton.addActionListener(_ -> {
            new PersonalWallet();
            dispose();
        });
        leftPanel.add(personalWalletButton);

        // Profile button
        profileButton = new JButton("Profile");
        profileButton.addActionListener(_ -> {
            new Profile();
            dispose();
        });
        leftPanel.add(profileButton);

        // Log Out button
        logOutButton = new JButton("Log Out");
        logOutButton.addActionListener(_ -> {
            new LogIn();
            dispose();
        });
        leftPanel.add(logOutButton);

        // Center panel for managing expenses
        centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding
        add(centerPanel, BorderLayout.CENTER);

        // Create a table to display expenses
        tableModel = new DefaultTableModel(new String[]{"Expense ID", "Expense Name", "Amount", "Date", "Added By"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        expenseTable = new JTable(tableModel);
        expenseTable.setDefaultRenderer(Object.class, new StripedTableCellRenderer());

        // Hide the "Expense ID" column from the user
        expenseTable.getColumnModel().getColumn(0).setMinWidth(0);
        expenseTable.getColumnModel().getColumn(0).setMaxWidth(0);
        expenseTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        loadExpenses(groupId); // Load and display expenses in the table

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Add/Edit/Remove buttons for expenses
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        addExpenseButton = new JButton("Add Expense");
        editExpenseButton = new JButton("Edit Expense");
        removeExpenseButton = new JButton("Remove Expense");

        buttonPanel.add(addExpenseButton);
        buttonPanel.add(editExpenseButton);
        buttonPanel.add(removeExpenseButton);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Action listeners for buttons
        addExpenseButton.addActionListener(_ -> openAddExpenseDialog(groupId));
        editExpenseButton.addActionListener(_ -> {
            int selectedRow = expenseTable.getSelectedRow();
            if (selectedRow != -1) {
                int expenseId = (int) tableModel.getValueAt(selectedRow, 0);
                String expenseName = (String) tableModel.getValueAt(selectedRow, 1);
                String amount = (String) tableModel.getValueAt(selectedRow, 2);
                String date = (String) tableModel.getValueAt(selectedRow, 3);
                openEditExpenseDialog(expenseId, expenseName, amount, date);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an expense to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        removeExpenseButton.addActionListener(_ -> {
            int selectedRow = expenseTable.getSelectedRow();
            if (selectedRow != -1) {
                int expenseId = (int) tableModel.getValueAt(selectedRow, 0);
                openRemoveExpenseDialog(expenseId);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an expense to remove.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Display frame
        setVisible(true);
    }


    /**
     * Custom renderer to color alternate rows in the table and apply color based on gain or loss.
     */
    private static class StripedTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                // Apply color for gain or loss in the "Amount" column
                if (column == 2) { // Assuming column 2 is the "Amount" column
                    double amount = Double.parseDouble(value.toString());
                    if (amount >= 0) {
                        c.setForeground(new Color(76, 250, 0));  // Gain
                    } else {
                        c.setForeground(new Color(255, 82, 82));  // Loss
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }
            } else {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            }

            return c;
        }
    }


    /**
     * Method to create the 'expenses' table within the database if it does not already exist.
     */
    private static void createExpenseTableIfNotExists() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            // Switch to the 'homex_db' database dynamically
            statement.executeUpdate("USE homex_db");

            // Create the expenses table if it does not exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS expenses (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "groupID INT, " +
                    "expenseName VARCHAR(255), " +
                    "amount DECIMAL(10, 2), " +
                    "date DATE, " +
                    "username VARCHAR(255), " +  // New column to store who added the expense
                    "FOREIGN KEY (groupID) REFERENCES groups(id))";
            statement.executeUpdate(createTableSQL);

        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
        }
    }


    /**
     * Loads the expenses for a specific group from the database and displays them in the expense table.
     *
     * @param groupId The ID of the group whose expenses should be loaded.
     */
    private void loadExpenses(int groupId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT id, expenseName, amount, date, username FROM expenses WHERE groupID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, groupId);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Clear the existing rows in the table
            tableModel.setRowCount(0);

            // Add each expense to the table
            while (resultSet.next()) {
                int expenseId = resultSet.getInt("id");
                String expenseName = resultSet.getString("expenseName");
                String amount = resultSet.getString("amount");
                String date = resultSet.getString("date");
                String username = resultSet.getString("username"); // Fetch username
                tableModel.addRow(new Object[]{expenseId, expenseName, amount, date, username});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Opens a dialog to allow the user to add a new expense for the group.
     * The dialog prompts the user for the expense name, amount, gain or loss, and the date.
     *
     * @param groupId The ID of the group for which the expense is being added.
     */
    private void openAddExpenseDialog(int groupId) {
        JDialog addExpenseDialog = new JDialog(this, "Add Expense", true);
        addExpenseDialog.setLayout(new GridBagLayout());  // Use GridBagLayout for better control over the layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);  // Add padding between components
        gbc.fill = GridBagConstraints.HORIZONTAL;  // Make components fill horizontally

        // Expense name field
        gbc.gridx = 0;  // First column
        gbc.gridy = 0;  // First row
        addExpenseDialog.add(new JLabel("Expense Name:"), gbc);

        gbc.gridx = 1;  // Second column
        JTextField expenseNameField = new JTextField(15);
        addExpenseDialog.add(expenseNameField, gbc);

        // Amount field
        gbc.gridx = 0;  // First column
        gbc.gridy = 1;  // Second row
        addExpenseDialog.add(new JLabel("Amount:"), gbc);

        gbc.gridx = 1;  // Second column
        JTextField amountField = new JTextField(15);
        addExpenseDialog.add(amountField, gbc);

        // Gain or Loss option (radio buttons)
        gbc.gridx = 0;  // First column
        gbc.gridy = 2;  // Third row
        addExpenseDialog.add(new JLabel("Gain or Loss:"), gbc);

        gbc.gridx = 1;  // Second column
        JRadioButton gainRadioButton = new JRadioButton("Gain");
        JRadioButton lossRadioButton = new JRadioButton("Loss");
        ButtonGroup group = new ButtonGroup();
        group.add(gainRadioButton);
        group.add(lossRadioButton);

        JPanel gainLossPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));  // Group radio buttons in a panel
        gainLossPanel.add(gainRadioButton);
        gainLossPanel.add(lossRadioButton);
        addExpenseDialog.add(gainLossPanel, gbc);

        // Date field
        gbc.gridx = 0;  // First column
        gbc.gridy = 3;  // Fourth row
        addExpenseDialog.add(new JLabel("Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;  // Second column
        JTextField dateField = new JTextField(15);
        addExpenseDialog.add(dateField, gbc);

        // Add button
        gbc.gridx = 0;
        gbc.gridy = 4;  // Fifth row
        gbc.gridwidth = 2;  // Span across two columns
        gbc.anchor = GridBagConstraints.CENTER;
        JButton addButton = new JButton("Add");
        addExpenseDialog.add(addButton, gbc);

        // Action listener for the Add button
        addButton.addActionListener(_ -> {
            String expenseName = expenseNameField.getText();
            String amountStr = amountField.getText();
            String date = dateField.getText();

            // Validate that amount is a number
            try {
                Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Amount must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Determine if it is a gain or loss
            if (lossRadioButton.isSelected()) {
                amountStr = "-" + amountStr;  // Prepend minus sign for loss
            }

            if (!expenseName.isEmpty() && !amountStr.isEmpty() && !date.isEmpty()) {
                // Add the expense to the database with the username
                addExpenseToDatabase(groupId, expenseName, amountStr, date, User.getUsername());
                addExpenseDialog.dispose();  // Close the dialog
                loadExpenses(groupId);  // Refresh the displayed expenses
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Display the dialog
        addExpenseDialog.pack();  // Adjust dialog size based on components
        addExpenseDialog.setLocationRelativeTo(this);  // Center the dialog
        addExpenseDialog.setVisible(true);
    }


    /**
     * Adds a new expense to the database for a specific group.
     *
     * @param groupId The ID of the group to which the expense belongs.
     * @param expenseName The name of the expense.
     * @param amount The amount of the expense (positive for gains, negative for losses).
     * @param date The date of the expense.
     * @param username The username of the user who added the expense.
     */
    private void addExpenseToDatabase(int groupId, String expenseName, String amount, String date, String username) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO expenses (groupID, expenseName, amount, date, username) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, groupId);
            preparedStatement.setString(2, expenseName);
            preparedStatement.setString(3, amount);
            preparedStatement.setString(4, date);
            preparedStatement.setString(5, username); // Store the username of the user adding the expense

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Expense added successfully.");
            } else {
                System.out.println("Failed to add expense.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Opens a dialog to edit an existing expense for the group.
     * The dialog allows the user to modify the expense name, amount (gain or loss), and date.
     *
     * @param expenseId The ID of the expense to be edited.
     * @param currentName The current name of the expense.
     * @param currentAmount The current amount of the expense.
     * @param currentDate The current date of the expense.
     */
    private void openEditExpenseDialog(int expenseId, String currentName, String currentAmount, String currentDate) {
        JDialog editExpenseDialog = new JDialog(this, "Edit Expense", true);
        editExpenseDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Expense name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        editExpenseDialog.add(new JLabel("Expense Name:"), gbc);

        gbc.gridx = 1;
        JTextField expenseNameField = new JTextField(currentName, 15);
        editExpenseDialog.add(expenseNameField, gbc);

        // Amount field
        gbc.gridx = 0;
        gbc.gridy = 1;
        editExpenseDialog.add(new JLabel("Amount:"), gbc);

        gbc.gridx = 1;
        JTextField amountField = new JTextField(currentAmount, 15);
        editExpenseDialog.add(amountField, gbc);

        // Gain or Loss option
        gbc.gridx = 0;
        gbc.gridy = 2;
        editExpenseDialog.add(new JLabel("Gain or Loss:"), gbc);

        gbc.gridx = 1;
        JRadioButton gainRadioButton = new JRadioButton("Gain");
        JRadioButton lossRadioButton = new JRadioButton("Loss");
        if (Double.parseDouble(currentAmount) < 0) {
            lossRadioButton.setSelected(true);
        } else {
            gainRadioButton.setSelected(true);
        }
        ButtonGroup group = new ButtonGroup();
        group.add(gainRadioButton);
        group.add(lossRadioButton);

        JPanel gainLossPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gainLossPanel.add(gainRadioButton);
        gainLossPanel.add(lossRadioButton);
        editExpenseDialog.add(gainLossPanel, gbc);

        // Date field
        gbc.gridx = 0;
        gbc.gridy = 3;
        editExpenseDialog.add(new JLabel("Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        JTextField dateField = new JTextField(currentDate, 15);
        editExpenseDialog.add(dateField, gbc);

        // Update button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton updateButton = new JButton("Update");
        editExpenseDialog.add(updateButton, gbc);

        updateButton.addActionListener(_ -> {
            String newName = expenseNameField.getText();
            String newAmount = amountField.getText();
            String newDate = dateField.getText();

            if (!newName.isEmpty() && !newAmount.isEmpty() && !newDate.isEmpty()) {
                if (lossRadioButton.isSelected() && !newAmount.startsWith("-")) {
                    newAmount = "-" + newAmount;
                } else if (gainRadioButton.isSelected() && newAmount.startsWith("-")) {
                    newAmount = newAmount.substring(1);
                }
                updateExpenseInDatabase(expenseId, newName, newAmount, newDate);
                editExpenseDialog.dispose();
                loadExpenses(groupId);  // Refresh displayed expenses
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        editExpenseDialog.pack();
        editExpenseDialog.setLocationRelativeTo(this);
        editExpenseDialog.setVisible(true);
    }


    /**
     * Updates an existing expense in the database with new values.
     *
     * @param expenseId The ID of the expense to be updated.
     * @param newName The new name of the expense.
     * @param newAmount The new amount of the expense (positive for gains, negative for losses).
     * @param newDate The new date of the expense.
     */
    private void updateExpenseInDatabase(int expenseId, String newName, String newAmount, String newDate) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "UPDATE expenses SET expenseName = ?, amount = ?, date = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newName);
            preparedStatement.setString(2, newAmount);
            preparedStatement.setString(3, newDate);
            preparedStatement.setInt(4, expenseId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Opens a confirmation dialog to allow the user to remove an expense from the group.
     * If the user confirms, the expense is removed from the database.
     *
     * @param expenseId The ID of the expense to be removed.
     */
    private void openRemoveExpenseDialog(int expenseId) {
        int response = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to remove this expense?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION
        );
        if (response == JOptionPane.YES_OPTION) {
            removeExpenseFromDatabase(expenseId);
            loadExpenses(groupId);  // Refresh the displayed expenses
        }
    }


    /**
     * Removes an expense from the database based on the expense ID.
     *
     * @param expenseId The ID of the expense to be removed.
     */
    private void removeExpenseFromDatabase(int expenseId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "DELETE FROM expenses WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, expenseId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Retrieves the name of a group from the database based on the group ID.
     *
     * @param groupID The ID of the group.
     * @return The name of the group, or null if the group is not found.
     */
    private String getGroupNameById(int groupID) {
        String groupName = null;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT groupname FROM groups WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, groupID);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                groupName = resultSet.getString("groupname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groupName;
    }
}
