/**
 * Libraries
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Profile extends JFrame {

    // Database connection parameters
    private static final String DB_URL = "jdbc:mariadb://localhost:3307/homex_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "vj88nx35&*";

    // GUI components
    JPanel headerPanel, userInfoPanel, leftPanel, formPanel;
    JButton homeButton, personalWalletButton, profileButton, logOutButton, updateButton;
    JLabel pageNameLabel, appNameLabel, usernameLabel, avatarLabel;
    JTextField newUsernameField;
    JPasswordField newPasswordField, confirmPasswordField;
    private static Image app_logo = Toolkit.getDefaultToolkit().getImage("data/image/logo.png");

    /**
     * Constructor to initialize GUI components
     */
    public Profile() {
        // Frame setup
        setTitle("Profile");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setIconImage(app_logo);
        setLayout(new BorderLayout(0, 0));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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
        personalWalletButton.addActionListener(_ -> {
            new PersonalWallet(); // Navigate to Personal Wallet page
            dispose();
        });
        leftPanel.add(personalWalletButton);

        // Profile button
        profileButton = new JButton("Profile");
        profileButton.setEnabled(false); // Already on the Profile page
        leftPanel.add(profileButton);

        // Log Out button
        logOutButton = new JButton("Log Out");
        logOutButton.addActionListener(_ -> {
            new LogIn(); // Navigate to Log In page
            dispose();
        });
        leftPanel.add(logOutButton);

        add(leftPanel, BorderLayout.WEST); // Add the left panel to the profile page

        // Create the form panel for changing username and password
        formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50)); // Padding around form

        // Username field
        JLabel newUsernameLabel = new JLabel("New Username:");
        newUsernameField = new JTextField(20);
        formPanel.add(newUsernameLabel);
        formPanel.add(newUsernameField);

        // Password field
        JLabel newPasswordLabel = new JLabel("New Password:");
        newPasswordField = new JPasswordField(20);
        formPanel.add(newPasswordLabel);
        formPanel.add(newPasswordField);

        // Confirm Password field
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordField = new JPasswordField(20);
        formPanel.add(confirmPasswordLabel);
        formPanel.add(confirmPasswordField);

        // Update button
        updateButton = new JButton("Update Profile");
        formPanel.add(new JLabel()); // Empty label for alignment
        formPanel.add(updateButton);

        // Action listener for the update button
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newUsername = newUsernameField.getText();
                String newPassword = new String(newPasswordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());

                // Validate fields
                if (newUsername.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(Profile.this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                } else if (!newPassword.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(Profile.this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                } else if (!isValidPassword(newPassword)) {
                    JOptionPane.showMessageDialog(Profile.this,
                            "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, and one digit.",
                            "Invalid Password",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    // Update username and password in the database
                    if (updateUserInDatabase(User.getUsername(), newUsername, newPassword)) {
                        JOptionPane.showMessageDialog(Profile.this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        // Update User class fields
                        User.setUser(newUsername, User.getAvatarPath());  // Keep the current avatarPath
                        usernameLabel.setText(newUsername); // Update displayed username
                    } else {
                        JOptionPane.showMessageDialog(Profile.this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Add the form panel to the center of the frame
        add(formPanel, BorderLayout.CENTER);

        // Make the frame visible
        setVisible(true);
    }


    /**
     * Validates the password according to specified rules.
     *
     * @param password The password to validate.
     * @return True if the password meets the criteria, false otherwise.
     */
    public static boolean isValidPassword(String password) {
        // Check if the password meets the required criteria
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?!.*\\s).{8,}$";
        return password.matches(regex);
    }


    /**
     * Method to update the user's username and password in the database.
     * @param currentUsername: The current username.
     * @param newUsername: The new username.
     * @param newPassword: The new password.
     * @return true if update was successful, false otherwise.
     */
    private boolean updateUserInDatabase(String currentUsername, String newUsername, String newPassword) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            String sql = "UPDATE users SET username = ?, password = ? WHERE username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, newUsername);
                preparedStatement.setString(2, newPassword);
                preparedStatement.setString(3, currentUsername);

                int rowsAffected = preparedStatement.executeUpdate();
                return rowsAffected > 0; // Return true if the update was successful
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }


    /**
     * Main Method to open the profile page.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run(){
                new Profile();
            }
        });
    }
}
