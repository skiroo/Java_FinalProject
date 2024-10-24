/**
 * Libraries
 */
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.io.*;

/**
 * The LogIn class represents the user interface for logging in to the HomeEx application.
 * Users can log in with their existing accounts or create new accounts.
 * It provides functionality for creating the database and tables if they do not exist,
 * validating user credentials, and navigating to the Home page after successful login.
 */
public class LogIn extends JFrame {

    // Database connection parameters (base URL without the database)
    private static final String DB_URL = "jdbc:mariadb://localhost:3306";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";

    // GUI components
    private JButton logInButton, signUpButton;
    private JTextField usernameTextField;
    private JPasswordField passwordTextField;
    private JLabel usernameLabel, passwordLabel;
    // Set frame icon to application logo
    private static Image app_logo = Toolkit.getDefaultToolkit().getImage("data/image/logo.png");


    /**
     * Constructor to initialize the GUI components.
     */
    public LogIn() {
        createDatabaseIfNotExists();  // Create the database if it does not exist
        createTableIfNotExists();   // Create the table if it does not exist

        // Frame setup
        setTitle("Log In");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setIconImage(app_logo);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Set up layout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Logo
        ImageIcon originalLogo = new ImageIcon("data/image/logo.png");
        Image scaledLogo = originalLogo.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH); // Adjust width and height as needed
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(logoLabel, gbc);

        // Username label and text field
        usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(usernameLabel, gbc);

        usernameTextField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(usernameTextField, gbc);

        // Password label and text field
        passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(passwordLabel, gbc);

        passwordTextField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        add(passwordTextField, gbc);

        // Log in and sign up buttons
        logInButton = new JButton("Log In");
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(logInButton, gbc);

        signUpButton = new JButton("Sign Up");
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 135, 0, 0);
        gbc.anchor = GridBagConstraints.WEST;
        add(signUpButton, gbc);

        // Button events
        logInButton.addActionListener(new LogInListener());
        signUpButton.addActionListener(new SignUpListener());

        // Display frame
        setVisible(true);
    }


    /**
     * Creates the database 'homex_db' if it does not already exist.
     * This method establishes a connection to the MariaDB server and creates the database.
     */
    private static void createDatabaseIfNotExists() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            // Create the database if it does not exist
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS homex_db");

        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
        }
    }


    /**
     * Creates the 'users' table in the 'homex_db' database if it does not already exist.
     * This table stores user information such as username, password, and avatar path.
     */
    private static void createTableIfNotExists() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            // Switch to the 'homex_db' database dynamically
            statement.executeUpdate("USE homex_db");

            // Create the user's table if it does not exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(255) UNIQUE, " +
                    "password VARCHAR(255), " +
                    "avatar VARCHAR(255))";
            statement.executeUpdate(createTableSQL);

        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
        }
    }


    /**
     * Listener for the Log In button.
     * Handles user log in by checking if the username and password are in the database.
     */
    private class LogInListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Get user input
            String username = usernameTextField.getText();
            String password = new String(passwordTextField.getPassword());

            // Check if fields are empty
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(LogIn.this, "Please enter both username and password.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                // Login validation
                if (validateLogin(username, password)) {
                    JOptionPane.showMessageDialog(LogIn.this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Get the avatar path of the logged-in user
                    String avatarPath = getAvatarPath(username);

                    // Save user information for access in other classes
                    User.setUser(username, avatarPath);

                    // Open the Home page and close the current frame
                    SwingUtilities.invokeLater(() -> {
                        new Home();  // Use User class to fetch username and avatar
                        LogIn.this.dispose(); // Close the current LogIn
                    });
                } else {
                    JOptionPane.showMessageDialog(LogIn.this, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }


        /**
         * Retrieves the avatar path for the given username from the database.
         * If no avatar is set for the user, the default avatar is used.
         *
         * @param username The username of the logged-in user.
         * @return The path to the user's avatar image.
         */
        private String getAvatarPath(String username) {
            String avatarPath = "data/image/default_avatar.png"; // Default avatar path
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

                // Switch to 'homex_db' database
                Statement statement = connection.createStatement();
                statement.executeUpdate("USE homex_db");

                String sql = "SELECT avatar FROM users WHERE username = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, username);

                ResultSet result = preparedStatement.executeQuery();

                if (result.next()) {
                    avatarPath = result.getString("avatar");
                    if (avatarPath == null || avatarPath.isEmpty()) {
                        avatarPath = "data/image/default_avatar.png"; // Use default avatar if none is set
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(LogIn.this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return avatarPath;
        }


        /**
         * Validates the user's login by checking the database for a matching username and password.
         *
         * @param username The username entered by the user.
         * @param password The password entered by the user.
         * @return True if the username and password are correct, false otherwise.
         */
        private boolean validateLogin(String username, String password) {
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

                // Switch to 'homex_db' database
                Statement statement = connection.createStatement();
                statement.executeUpdate("USE homex_db");

                String sql = "SELECT password FROM users WHERE username = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, username);

                ResultSet result = preparedStatement.executeQuery();

                if (result.next()) {
                    String storedPassword = result.getString("password");

                    return password.equals(storedPassword);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(LogIn.this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
    }


    /**
     * Listener for the Sign-Up button.
     * Opens a dialog that allows the user to create a new account.
     */
    private class SignUpListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Create a new dialog for sign-up
            JDialog signUpDialog = new JDialog(LogIn.this, "Sign Up", true);
            signUpDialog.setLayout(new BorderLayout());
            signUpDialog.setSize(400, 300);
            signUpDialog.setLocationRelativeTo(LogIn.this);

            // Create a panel with padding
            JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding to the panel

            // Username field
            JTextField newUsernameField = new JTextField(20);
            panel.add(new JLabel("Username:"));
            panel.add(newUsernameField);

            // Password field
            JPasswordField newPasswordField = new JPasswordField(20);
            panel.add(new JLabel("Password:"));
            panel.add(newPasswordField);

            // Confirm Password field
            JPasswordField confirmPasswordField = new JPasswordField(20);
            panel.add(new JLabel("Confirm Password:"));
            panel.add(confirmPasswordField);

            // Avatar selection button
            JButton selectAvatarButton = new JButton("Choose Avatar");
            panel.add(selectAvatarButton);
            JLabel selectedAvatarLabel = new JLabel("No avatar selected");
            panel.add(selectedAvatarLabel);

            // Set padding for the label
            selectedAvatarLabel.setBorder(new EmptyBorder(5, 5, 5, 5));

            // Avatar image path
            final String[] selectedAvatarPath = {null};

            // Action listener for choosing predefined avatars
            selectAvatarButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    // Open a new dialog to choose an avatar
                    JDialog avatarDialog = new JDialog(signUpDialog, "Select Avatar", true);
                    avatarDialog.setLayout(new GridLayout(3, 3, 10, 10)); // 3x3 grid for 9 images
                    avatarDialog.setSize(400, 400);
                    avatarDialog.setLocationRelativeTo(signUpDialog);

                    // Load avatar images from the data/avatar_img directory
                    File avatarDir = new File("data/image/avatar_img");
                    File[] avatarFiles = avatarDir.listFiles((_, name) -> name.toLowerCase().endsWith(".png"));

                    if (avatarFiles != null) {
                        for (File avatarFile : avatarFiles) {
                            // Display the avatar icons
                            ImageIcon avatarIcon = new ImageIcon(avatarFile.getAbsolutePath());
                            Image scaledImage = avatarIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                            ImageIcon scaledIcon = new ImageIcon(scaledImage);
                            JLabel avatarLabel = new JLabel(scaledIcon);
                            avatarLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                            avatarLabel.setBorder(new EmptyBorder(5, 5, 5, 5)); // Add padding for avatar label
                            avatarLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                            avatarLabel.addMouseListener(new MouseAdapter() {
                                public void mouseClicked(MouseEvent me) {
                                    // Set the selected avatar path and close the dialog
                                    selectedAvatarPath[0] = avatarFile.getAbsolutePath();
                                    selectedAvatarLabel.setText("Selected: " + avatarFile.getName());
                                    avatarDialog.dispose(); // Close the avatar selection dialog
                                }
                            });

                            avatarDialog.add(avatarLabel);
                        }
                    }

                    avatarDialog.setVisible(true); // Display the avatar selection dialog

                    // After the dialog is closed, check if an avatar was selected
                    if (selectedAvatarPath[0] == null || selectedAvatarPath[0].isEmpty()) {
                        selectedAvatarPath[0] = "data/image/default_avatar.png"; // Set to default avatar if none is selected
                        selectedAvatarLabel.setText("Selected: Default Avatar"); // Update label to indicate the default avatar
                    }
                }
            });

            // Choose Your Own Image button
            JButton selectOwnImageButton = new JButton("Choose Your Own Image");
            panel.add(selectOwnImageButton);

            // Action listener for choosing a custom image
            selectOwnImageButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    // Use JFileChooser to allow user to select their own image
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Select Your Avatar Image");
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg", "gif"));

                    int userSelection = fileChooser.showOpenDialog(signUpDialog);
                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        selectedAvatarPath[0] = selectedFile.getAbsolutePath(); // Save the selected file path
                        selectedAvatarLabel.setText("Selected: " + selectedFile.getName()); // Update label to show the selected file
                    }
                }
            });

            // Sign Up Button
            JButton signUpConfirmButton = new JButton("Sign Up");
            panel.add(signUpConfirmButton);

            // Action listener for the sign-up confirmation button
            signUpConfirmButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    String newUsername = newUsernameField.getText();
                    String newPassword = new String(newPasswordField.getPassword());
                    String confirmPassword = new String(confirmPasswordField.getPassword());
                    if (!isValidPassword(newPassword)) {
                        JOptionPane.showMessageDialog(LogIn.this,
                                "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, and one digit.",
                                "Invalid Password",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Check for empty fields
                    if (newUsername.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                        JOptionPane.showMessageDialog(signUpDialog, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;

                    } else if (!newPassword.equals(confirmPassword)) {
                        JOptionPane.showMessageDialog(signUpDialog, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;

                    } else {
                        // Insert the new user into the database
                        if (insertUser(newUsername, newPassword, selectedAvatarPath[0])) {
                            JOptionPane.showMessageDialog(signUpDialog, "Account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            signUpDialog.dispose(); // Close the sign-up dialog
                        } else {
                            JOptionPane.showMessageDialog(signUpDialog, "User already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });

            // Add the panel to the dialog
            signUpDialog.add(panel, BorderLayout.CENTER);

            // Display the dialog
            signUpDialog.setVisible(true);
        }


        /**
         * Validates the password according to the specified rules:
         * at least 8 characters long, contains at least one uppercase letter, one lowercase letter, and one digit.
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
         * Inserts a new user into the 'users' table in the database.
         * The method saves the user's username, password, and avatar path.
         *
         * @param username The username of the new user.
         * @param password The password of the new user.
         * @param avatarPath The path to the user's selected avatar image.
         * @return True if the user was successfully inserted, false otherwise.
         */
        private boolean insertUser(String username, String password, String avatarPath) {
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Switch to 'homex_db' database
                Statement statement = connection.createStatement();
                statement.executeUpdate("USE homex_db");

                String sql = "INSERT INTO users (username, password, avatar) VALUES (?, ?, ?)";

                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, username);
                    preparedStatement.setString(2, password);
                    preparedStatement.setString(3, avatarPath);

                    int rowsAffected = preparedStatement.executeUpdate();
                    return rowsAffected > 0; // Return true if user was inserted
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(LogIn.this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return false; // Return false if insertion failed
        }
    }


    /**
     * Main method to open the Log In frame
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LogIn();
            }
        });
    }
}
