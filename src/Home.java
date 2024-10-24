import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Home extends JFrame {

    // Database connection parameters
    private static final String DB_URL = "jdbc:mariadb://localhost:3307/homex_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "vj88nx35&*";

    // GUI components
    JPanel headerPanel, userInfoPanel, leftPanel, centerPanel;
    JButton homeButton, personalWalletButton, profileButton, logOutButton, createGroupButton, joinGroupButton;
    JLabel pageNameLabel, appNameLabel, usernameLabel, avatarLabel;
    private static Image app_logo = Toolkit.getDefaultToolkit().getImage("data/image/logo.png");
    JTable groupTable;
    DefaultTableModel tableModel;

    public Home() {
        // Create the groups table if it does not exist
        createTableIfNotExists();

        // Frame setup
        setTitle("Home");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setIconImage(app_logo);
        setLayout(new BorderLayout(0, 0));  // No gaps between regions
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Create the header panel
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(12, 73, 87));
        headerPanel.setPreferredSize(new Dimension(1000, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Page name label (left side)
        pageNameLabel = new JLabel("Home");
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
        homeButton.setEnabled(false);   // Already on the Home page
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

        // Center panel for groups and buttons
        centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding
        add(centerPanel, BorderLayout.CENTER);

        // Create a non-editable table to display groups
        tableModel = new DefaultTableModel(new String[]{"Group ID", "Group Name"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        groupTable = new JTable(tableModel);
        groupTable.setDefaultRenderer(Object.class, new StripedTableCellRenderer());  // Apply striped rendering

        // Hide the "Group ID" column from the user
        groupTable.getColumnModel().getColumn(0).setMinWidth(0);
        groupTable.getColumnModel().getColumn(0).setMaxWidth(0);
        groupTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Add mouse listener to detect double-clicks
        groupTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {  // Check for double-click
                    int row = groupTable.getSelectedRow();
                    if (row != -1) {
                        int groupId = (int) tableModel.getValueAt(row, 0);  // Get group ID from the selected row
                        new Group(groupId);  // Call new Group with the group ID
                        dispose();  // Close the home frame
                    }
                }
            }
        });

        loadExpenseGroups(); // Load and display groups in the table

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(groupTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons to create and join groups
        JPanel groupActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        createGroupButton = new JButton("Create Group");
        createGroupButton.addActionListener(_ -> openCreateGroupDialog());
        joinGroupButton = new JButton("Join Group");
        joinGroupButton.addActionListener(_ -> openJoinGroupDialog());
        groupActionPanel.add(createGroupButton);
        groupActionPanel.add(joinGroupButton);
        centerPanel.add(groupActionPanel, BorderLayout.SOUTH);

        // Display frame
        setVisible(true);
    }

    /**
     * Custom renderer to color alternate rows in the table.
     */
    private static class StripedTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                if (row % 2 == 0) {
                    c.setBackground(new Color(0, 250, 200));
                } else {
                    c.setBackground(Color.WHITE);  // White for odd rows
                }
            } else {
                c.setBackground(table.getSelectionBackground());
            }

            return c;
        }
    }

    /**
     * Method to create the 'groups' table within the database if it does not already exist.
     */
    private static void createTableIfNotExists() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            // Switch to the 'homex_db' database dynamically
            statement.executeUpdate("USE homex_db");

            // Create the groups table if it does not exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS groups (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "groupname VARCHAR(255) UNIQUE, " +
                    "password VARCHAR(255))";
            statement.executeUpdate(createTableSQL);

        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
        }
    }

    /**
     * Loads the user's expense groups from the database and displays them in the groupTable.
     */
    private void loadExpenseGroups() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT id, groupname FROM groups";  // Fetch both the group ID and group name
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            tableModel.setRowCount(0);  // Clear existing rows

            while (resultSet.next()) {
                int groupId = resultSet.getInt("id");
                String groupName = resultSet.getString("groupname");
                tableModel.addRow(new Object[]{groupId, groupName});  // Add both group ID and group name
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Opens a dialog to create a new expense group.
     */
    private void openCreateGroupDialog() {
        JDialog createGroupDialog = new JDialog(this, "Create Group", true);
        createGroupDialog.setLayout(new GridLayout(3, 2, 10, 10));
        createGroupDialog.setSize(400, 200);
        createGroupDialog.setLocationRelativeTo(this);

        // Group name field
        JTextField groupNameField = new JTextField();
        createGroupDialog.add(new JLabel("Group Name:"));
        createGroupDialog.add(groupNameField);

        // Group password field
        JPasswordField groupPasswordField = new JPasswordField();
        createGroupDialog.add(new JLabel("Group Password:"));
        createGroupDialog.add(groupPasswordField);

        // Create button
        JButton createButton = new JButton("Create");
        createButton.addActionListener(_ -> {
            String groupName = groupNameField.getText();
            String groupPassword = new String(groupPasswordField.getPassword());

            if (!groupName.isEmpty() && !groupPassword.isEmpty()) {
                createGroupInDatabase(groupName, groupPassword);
                createGroupDialog.dispose();
                loadExpenseGroups(); // Refresh the displayed groups
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        createGroupDialog.add(createButton);

        createGroupDialog.setVisible(true);
    }

    /**
     * Creates a new expense group in the database.
     */
    private void createGroupInDatabase(String groupName, String groupPassword) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO groups (groupname, password) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, groupName);
            preparedStatement.setString(2, groupPassword);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Opens a dialog to join an existing group.
     */
    private void openJoinGroupDialog() {
        JDialog joinGroupDialog = new JDialog(this, "Join Group", true);
        joinGroupDialog.setLayout(new GridLayout(3, 2, 10, 10));
        joinGroupDialog.setSize(400, 200);
        joinGroupDialog.setLocationRelativeTo(this);

        // Group name field
        JTextField groupNameField = new JTextField();
        joinGroupDialog.add(new JLabel("Group Name:"));
        joinGroupDialog.add(groupNameField);

        // Group password field
        JPasswordField groupPasswordField = new JPasswordField();
        joinGroupDialog.add(new JLabel("Group Password:"));
        joinGroupDialog.add(groupPasswordField);

        // Join button
        JButton joinButton = new JButton("Join");
        joinButton.addActionListener(_ -> {
            String groupName = groupNameField.getText();
            String groupPassword = new String(groupPasswordField.getPassword());

            if (!groupName.isEmpty() && !groupPassword.isEmpty()) {
                joinGroupInDatabase(groupName, groupPassword);
                joinGroupDialog.dispose();
                loadExpenseGroups(); // Refresh the displayed groups
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        joinGroupDialog.add(joinButton);

        joinGroupDialog.setVisible(true);
    }

    /**
     * Validates the group name and password for joining an existing group.
     */
    private void joinGroupInDatabase(String groupName, String groupPassword) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM groups WHERE groupname = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, groupName);
            preparedStatement.setString(2, groupPassword);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                JOptionPane.showMessageDialog(this, "Successfully joined the group!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid group name or password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Main method to open the Home frame
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Home();
            }
        });
    }
}
