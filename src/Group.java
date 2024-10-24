/**
 * Libraries
 */
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Group extends JFrame {

    // Database connection parameters
    private static final String DB_URL = "jdbc:mariadb://localhost:3307/homex_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "vj88nx35&*";

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
                        c.setForeground(new Color(157, 253, 111));  // Gain
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
     * Loads the expenses for the group from the database and displays them in the expenseTable.
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
     * Opens a dialog to add a new expense.
     */
    private void openAddExpenseDialog(int groupId) {
        JDialog addExpenseDialog = new JDialog(this, "Add Expense", true);
        addExpenseDialog.setLayout(new GridLayout(4, 2, 10, 10));
        addExpenseDialog.setSize(400, 200);
        addExpenseDialog.setLocationRelativeTo(this);

        // Expense name field
        JTextField expenseNameField = new JTextField();
        addExpenseDialog.add(new JLabel("Expense Name:"));
        addExpenseDialog.add(expenseNameField);

        // Amount field
        JTextField amountField = new JTextField();
        addExpenseDialog.add(new JLabel("Amount:"));
        addExpenseDialog.add(amountField);

        // Gain or Loss option (radio buttons)
        JRadioButton gainRadioButton = new JRadioButton("Gain");
        JRadioButton lossRadioButton = new JRadioButton("Loss");
        ButtonGroup group = new ButtonGroup();
        group.add(gainRadioButton);
        group.add(lossRadioButton);
        JPanel gainLossPanel = new JPanel(new FlowLayout());
        gainLossPanel.add(gainRadioButton);
        gainLossPanel.add(lossRadioButton);
        addExpenseDialog.add(new JLabel("Gain or Loss:"));
        addExpenseDialog.add(gainLossPanel);

        // Date field
        JTextField dateField = new JTextField();
        addExpenseDialog.add(new JLabel("Date (YYYY-MM-DD):"));
        addExpenseDialog.add(dateField);

        // Add button
        JButton addButton = new JButton("Add");
        addButton.addActionListener(_ -> {
            String expenseName = expenseNameField.getText();
            String amountStr = amountField.getText();
            String date = dateField.getText();

            // Ensure amount is a valid number
            try {
                Double.parseDouble(amountStr); // Try to parse amount
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Amount must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                return; // Exit if the amount is not valid
            }

            // Check if gain or loss is selected and adjust the amount
            if (lossRadioButton.isSelected()) {
                amountStr = "-" + amountStr;
            }

            // Insert the expense into the database with the username
            if (!expenseName.isEmpty() && !amountStr.isEmpty() && !date.isEmpty()) {
                addExpenseToDatabase(groupId, expenseName, amountStr, date, User.getUsername());
                addExpenseDialog.dispose();
                loadExpenses(groupId); // Refresh the displayed expenses
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        addExpenseDialog.add(addButton);

        addExpenseDialog.setVisible(true);
    }


    /**
     *
     * @param groupId
     * @param expenseName
     * @param amount
     * @param date
     * @param username
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
     * Opens a dialog to edit an existing expense.
     */
    private void openEditExpenseDialog(int expenseId, String currentName, String currentAmount, String currentDate) {
        JDialog editExpenseDialog = new JDialog(this, "Edit Expense", true);
        editExpenseDialog.setLayout(new GridLayout(4, 2, 10, 10));
        editExpenseDialog.setSize(400, 200);
        editExpenseDialog.setLocationRelativeTo(this);

        // Fields pre-filled with current values
        JTextField expenseNameField = new JTextField(currentName);
        editExpenseDialog.add(new JLabel("Expense Name:"));
        editExpenseDialog.add(expenseNameField);

        JTextField amountField = new JTextField(currentAmount);
        editExpenseDialog.add(new JLabel("Amount:"));
        editExpenseDialog.add(amountField);

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
        JPanel gainLossPanel = new JPanel(new FlowLayout());
        gainLossPanel.add(gainRadioButton);
        gainLossPanel.add(lossRadioButton);
        editExpenseDialog.add(new JLabel("Gain or Loss:"));
        editExpenseDialog.add(gainLossPanel);

        JTextField dateField = new JTextField(currentDate);
        editExpenseDialog.add(new JLabel("Date (YYYY-MM-DD):"));
        editExpenseDialog.add(dateField);

        // Update button
        JButton updateButton = new JButton("Update");
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
                loadExpenses(groupId); // Refresh displayed expenses
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        editExpenseDialog.add(updateButton);

        editExpenseDialog.setVisible(true);
    }


    /**
     *
     * @param expenseId
     * @param newName
     * @param newAmount
     * @param newDate
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
     * Opens a dialog to confirm removing an expense.
     */
    private void openRemoveExpenseDialog(int expenseId) {
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this expense?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            removeExpenseFromDatabase(expenseId);
            loadExpenses(groupId); // Refresh the displayed expenses
        }
    }


    /**
     *
     * @param expenseId
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
     * Retrieve the group name from the database using the group ID.
     * @param groupID The ID of the group.
     * @return The group name, or null if not found.
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
