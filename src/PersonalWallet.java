import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class PersonalWallet extends JFrame {

    // Database connection parameters
    private static final String DB_URL = "jdbc:mariadb://localhost:3307/homex_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "vj88nx35&*";

    private String username;
    private String avatarPath;
    private double userBudget = 1000.00; // Default budget

    // GUI components
    JPanel headerPanel, userInfoPanel, leftPanel, budgetPanel;
    JButton homeButton, personalWalletButton, profileButton, logOutButton;
    JLabel pageNameLabel, appNameLabel, usernameLabel, avatarLabel, budgetLabel, totalExpensesLabel, remainingBudgetLabel;
    private static Image app_logo = Toolkit.getDefaultToolkit().getImage("data/image/logo.png");

    public PersonalWallet() {
        this.username = User.getUsername();
        this.avatarPath = User.getAvatarPath();

        // Frame setup
        setTitle("Personal Wallet");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setIconImage(app_logo);
        setLayout(new BorderLayout(0, 0));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create the header panel
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(12, 73, 87));
        headerPanel.setPreferredSize(new Dimension(600, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Page name label (left side)
        pageNameLabel = new JLabel("Personal Wallet");
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

        usernameLabel = new JLabel(username);
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));

        avatarLabel = new JLabel();
        if (avatarPath != null) {
            ImageIcon avatarIcon = new ImageIcon(avatarPath);
            Image scaledImage = avatarIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            avatarLabel.setIcon(new ImageIcon(scaledImage));
        }

        userInfoPanel.add(usernameLabel);
        userInfoPanel.add(avatarLabel);
        headerPanel.add(userInfoPanel, BorderLayout.EAST);

        // Add the header panel to the frame
        add(headerPanel, BorderLayout.NORTH);

        // Create the left-side panel
        leftPanel = new JPanel();
        leftPanel.setBackground(new Color(12, 73, 87, 179));
        leftPanel.setLayout(new GridLayout(4, 1, 10, 10));
        leftPanel.setPreferredSize(new Dimension(200, 0));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding around buttons

        // Home button
        homeButton = new JButton("Home");
        homeButton.addActionListener(_ -> {
            new Home(); // Navigate to the Home page
            dispose();
        });
        leftPanel.add(homeButton);

        // Personal Wallet button
        personalWalletButton = new JButton("Personal Wallet");
        personalWalletButton.setEnabled(false);     // Already on Personal Wallet page
        leftPanel.add(personalWalletButton);

        // Profile button
        profileButton = new JButton("Profile");
        profileButton.addActionListener(_ -> {
            new Profile(); // Navigate to the profile page
            dispose();
        });
        leftPanel.add(profileButton);

        // Log Out button
        logOutButton = new JButton("Log Out");
        logOutButton.addActionListener(_ -> {
            new LogIn(); // Navigate to Log In page
            dispose();
        });
        leftPanel.add(logOutButton);

        add(leftPanel, BorderLayout.WEST); // Add the left panel to the profile page

        // Create the budget panel to display budget details
        budgetPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        budgetPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding around the panel

        // Total budget label
        budgetLabel = new JLabel("Total Budget: $" + userBudget);
        budgetLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        budgetPanel.add(budgetLabel);

        // Calculate total expenses
        double totalExpenses = getTotalExpensesForUser(username);

        // Total expenses label
        totalExpensesLabel = new JLabel("Total Expenses: $" + totalExpenses);
        totalExpensesLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        budgetPanel.add(totalExpensesLabel);

        // Remaining budget label
        double remainingBudget = userBudget - totalExpenses;
        remainingBudgetLabel = new JLabel("Remaining Budget: $" + remainingBudget);
        remainingBudgetLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        budgetPanel.add(remainingBudgetLabel);

        // Add the budget panel to the center of the frame
        add(budgetPanel, BorderLayout.CENTER);

        // Make the frame visible
        setVisible(true);
    }

    /**
     * Method to calculate the total expenses for the user from the database.
     */
    private double getTotalExpensesForUser(String username) {
        double totalExpenses = 0.0;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT SUM(amount) FROM expenses WHERE username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                totalExpenses = resultSet.getDouble(1); // Get the sum of expenses
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return totalExpenses;
    }

    /**
     * Main Method to open the profile page.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PersonalWallet());
    }
}
