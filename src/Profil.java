/**
 * Libraries
 */
import javax.swing.*;
import java.awt.*;

public class Profile extends JFrame {

    private String username;
    private String avatarPath;

    public Profile(String username, String avatarPath) {
        this.username = username;
        this.avatarPath = avatarPath;

        // Frame setup
        setTitle("Profile");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create the header panel
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(new Color(12, 73, 87));
        headerPanel.setPreferredSize(new Dimension(600, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Page name label (left side)
        JLabel pageNameLabel = new JLabel("Profile Page");
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
        }

        userInfoPanel.add(usernameLabel);
        userInfoPanel.add(avatarLabel);
        headerPanel.add(userInfoPanel, BorderLayout.EAST);

        // Create the profile panel
        JPanel profilePanel = new JPanel(new BorderLayout(10, 10));
        profilePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add components to the frame
        add(headerPanel, BorderLayout.NORTH);
        add(profilePanel, BorderLayout.CENTER);

        // Make the frame visible
        setVisible(true);
    }


    /**
     * Main Method to open the profile page.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run(){
                Profile profile = new Profile("User", "data/image/default_avatar.png");
            }
        });
    }
}
