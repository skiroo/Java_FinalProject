/**
 * Libraries
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Home is the main frame of the application
 */
public class Home extends JFrame {

    private String username;
    private String avatarPath;

    // GUI components
    private static Image app_logo = Toolkit.getDefaultToolkit().getImage("data/image/logo.png");

    /**
     * Constructor to initialize the GUI components.
     * @param username The name of the logged-in user.
     * @param avatarPath The path to the user's avatar image.
     */
    public Home(String username, String avatarPath) {
        this.username = username;
        this.avatarPath = avatarPath;

        // Frame setup
        setTitle("Home");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setIconImage(app_logo);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Create the header panel
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(new Color(12, 73, 87)); // Dark blue color
        headerPanel.setPreferredSize(new Dimension(1000, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Page name label (left side)
        JLabel pageNameLabel = new JLabel("Home");
        pageNameLabel.setForeground(Color.WHITE);
        pageNameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(pageNameLabel, BorderLayout.WEST);

        // App name label (center)
        JLabel appNameLabel = new JLabel("Household Expense Manager", SwingConstants.CENTER);
        appNameLabel.setForeground(Color.WHITE);
        appNameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(appNameLabel, BorderLayout.CENTER);

        // User information panel (right side)
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userInfoPanel.setOpaque(false); // Make the panel transparent

        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel avatarLabel = new JLabel();
        if (avatarPath != null) {
            ImageIcon avatarIcon = new ImageIcon(avatarPath);
            Image scaledImage = avatarIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            avatarLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            avatarLabel.setText("No Avatar");
            avatarLabel.setForeground(Color.WHITE);
        }

        // Add mouse listener to avatar label to open profile page and close home
        avatarLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new Profile(username, avatarPath); // Open profile page with username and avatar path
                dispose(); // Close the home frame
            }
        });

        userInfoPanel.add(usernameLabel);
        userInfoPanel.add(avatarLabel);
        headerPanel.add(userInfoPanel, BorderLayout.EAST);

        // Add the header panel to the frame
        add(headerPanel, BorderLayout.NORTH);

        // Display frame
        setVisible(true);
    }

    /**
     * Main method to open the Home frame
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Example username and avatarPath for testing
                new Home("User", "data/image/default_avatar.png");
            }
        });
    }
}
