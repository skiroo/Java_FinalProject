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
 * LogIn represents the user interface for logging in to the Household Expense Manager Application.
 * It allows users to log in to their existing accounts or create new accounts if they do not have one.
 */
public class LogIn extends JFrame {

    // Database connection parameters
    private static String DB_URL = "jdbc:mariadb://localhost:3307";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "vj88nx35&*";

    // GUI components
    private JButton logInButton, signUpButton;
    private JTextField usernameTextField;
    private JPasswordField passwordTextField;
    private JLabel usernameLabel, passwordLabel;
    private Image logo;


    /**
     * Constructor to initialize the GUI components.
     */
    public LogIn() {
        DB_URL = DB_URL + createDatabaseIfNotExists();  // Update DB_URL the once database is created
        createTableIfNotExists();   // Create the table if it does not exist

        // Set frame icon to application logo
        logo = Toolkit.getDefaultToolkit().getImage("data/logo.png");

        // Frame setup
        setTitle("Log In");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setIconImage(logo);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Username label and text field
        usernameLabel = new JLabel("Username:");
        usernameTextField = new JTextField(20);

        // Password label and text field
        passwordLabel = new JLabel("Password:");
        passwordTextField = new JPasswordField(20);

        // Log in and sign up buttons
        logInButton = new JButton("Log In");
        signUpButton = new JButton("Sign Up");

        // Add components to the frame
        add(usernameLabel);
        add(usernameTextField);
        add(passwordLabel);
        add(passwordTextField);
        add(logInButton);
        add(signUpButton);

        // Button events
        logInButton.addActionListener(new LogInListener());
        signUpButton.addActionListener(new SignUpListener());

        // Display frame
        setVisible(true);
    }


    /**
     * Method to create the database 'users_db' if it does not already exist.
     * @return String representing the path to the database, which is used to update the DB_URL.
     */
    private static String createDatabaseIfNotExists() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER,DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            // Create the database if it does not exist
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS users_db");

        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
        }

        return "/users_db";
    }


    /**
     * Method to create the 'user' table within the database if it does not already exist.
     * This table is used to store user account information.
     */
    private static void createTableIfNotExists() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            // Use the database
            statement.executeUpdate("USE users_db");

            // Create the employees table if it does not exist
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
     * If the log in is successful, it proceeds to the main application frame.
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

                    // Open the Home frame and close the current frame
                    SwingUtilities.invokeLater(() -> {
                        new Home();
                        LogIn.this.dispose(); // Close the current LogIn
                    });

                } else {
                    JOptionPane.showMessageDialog(LogIn.this, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }


        /**
         * Method to validate log in by checking if the username and password exist in the database.
         * @param username: String of username.
         * @param password: String of password.
         * @return boolean indicating if user exits in the database.
         */
        private boolean validateLogin(String username, String password) {
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

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

                    // Check for empty fields
                    if (newUsername.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                        JOptionPane.showMessageDialog(signUpDialog, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);

                    } else if (!newPassword.equals(confirmPassword)) {
                        JOptionPane.showMessageDialog(signUpDialog, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);

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
         * Method to insert a new user into the database.
         * @param username: String for the new username.
         * @param password: String for the new password.
         * @param avatarPath: Path to the selected avatar image.
         * @return boolean: true if insertion was successful, false otherwise.
         */
        private boolean insertUser(String username, String password, String avatarPath) {
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
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
