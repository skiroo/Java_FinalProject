// Libraries
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.io.*;


/**
 * Home is the main frame of the application
 */
public class Home extends JFrame {


    public Home() {

        // Frame setup
        setTitle("Home");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Display frame
        setVisible(true);
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
