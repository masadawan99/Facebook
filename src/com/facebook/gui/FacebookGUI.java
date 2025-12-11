package com.facebook.gui;

import com.facebook.Database;
import com.facebook.Main;

import javax.swing.*;
import java.awt.*;

/**
 * Main entry point for Facebook GUI Application
 * Professional Swing-based UI with animations and transitions
 */
public class FacebookGUI extends JFrame {

    // Facebook Brand Colors
    public static final Color FB_BLUE = new Color(24, 119, 242);
    public static final Color FB_BLUE_HOVER = new Color(22, 100, 200);
    public static final Color FB_GREEN = new Color(66, 183, 42);
    public static final Color FB_GREEN_HOVER = new Color(54, 160, 35);
    public static final Color FB_BACKGROUND = new Color(240, 242, 245);
    public static final Color FB_WHITE = new Color(255, 255, 255);
    public static final Color FB_TEXT_PRIMARY = new Color(28, 30, 33);
    public static final Color FB_TEXT_SECONDARY = new Color(101, 103, 107);
    public static final Color FB_LINK_BLUE = new Color(24, 119, 242);
    public static final Color FB_BORDER = new Color(219, 223, 231);
    public static final Color FB_ERROR = new Color(220, 53, 69);
    public static final Color FB_INPUT_FOCUS = new Color(24, 119, 242);

    // Fonts
    public static final Font FONT_LOGO = new Font("Helvetica Neue", Font.BOLD, 56);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_SUBHEADING = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 17);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_LINK = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_TAGLINE = new Font("Segoe UI", Font.PLAIN, 28);

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private LoginPanel loginPanel;
    private SignupPanel signupPanel;
    private HomePage homePage;

    public FacebookGUI() {
        initializeFrame();
        initializePanels();
        setVisible(true);
    }

    private void initializeFrame() {
        setTitle("Facebook");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setBackground(FB_BACKGROUND);

        // Set system look and feel for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Custom cursor
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    private void initializePanels() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(FB_BACKGROUND);

        loginPanel = new LoginPanel(this);
        signupPanel = new SignupPanel(this);
        // HomePage will be created after login when Main.current is not null

        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(signupPanel, "SIGNUP");

        add(mainPanel);
        showLoginPanel();
    }

    public void showLoginPanel() {
        addDecoration(); // Add title bar back for login
        loginPanel.resetFields();
        cardLayout.show(mainPanel, "LOGIN");
    }

    public void showSignupPanel() {
        signupPanel.resetFields();
        cardLayout.show(mainPanel, "SIGNUP");
    }

    public void onLoginSuccess() {
        // Create HomePage after login when Main.current is available
        if (homePage == null) {
            homePage = new HomePage(this);
            mainPanel.add(homePage, "HOME");
        }
        removeDecoration(); // Remove title bar for home page
        cardLayout.show(mainPanel, "HOME");
    }

    public void removeDecoration() {
        dispose();
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    public void addDecoration() {
        dispose();
        setUndecorated(false);
        setExtendedState(JFrame.NORMAL);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        // Check database connection first
        if (!Database.Check_Database()) {
            JOptionPane.showMessageDialog(null,
                    "Database cannot be found!\nERROR 404",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Run on EDT
        SwingUtilities.invokeLater(() -> {
            new FacebookGUI();
        });
    }
}
