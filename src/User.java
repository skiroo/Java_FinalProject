/**
 * Class to save user's name and pathway to avatar image
 */
public class User {
    private static String username;
    private static String avatarPath;

    // Setter
    public static void setUser(String username, String avatarPath) {
        User.username = username;
        User.avatarPath = avatarPath;
    }

    // Getters
    public static String getUsername() {
        return username;
    }

    public static String getAvatarPath() {
        return avatarPath;
    }
}