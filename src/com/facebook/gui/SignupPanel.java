package com.facebook.gui;

import com.facebook.*;
import com.facebook.gui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.Calendar;

/**
 * Facebook Signup Panel - Exact replica of Facebook's signup page
 * Features: All form fields, validations, animations, hover effects
 */
public class SignupPanel extends JPanel {

    private FacebookGUI parent;

    // Form fields
    private ModernTextField firstNameField;
    private ModernTextField lastNameField;
    private ModernComboBox<Integer> dayCombo;
    private ModernComboBox<String> monthCombo;
    private ModernComboBox<Integer> yearCombo;
    private GenderRadioPanel genderPanel;
    private ModernTextField emailField;
    private ModernPasswordField passwordField;
    private AnimatedButton signupButton;
    private LinkLabel loginLink;

    // Error label
    private JLabel errorLabel;

    // Animation
    private Timer fadeInTimer;
    private float opacity = 0f;

    // Months
    private final String[] MONTHS = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

    public SignupPanel(FacebookGUI parent) {
        this.parent = parent;
        setBackground(FacebookGUI.FB_BACKGROUND);
        setLayout(new GridBagLayout());
        initComponents();
        startFadeInAnimation();
    }

    private void initComponents() {
        // ==================== Main Container ====================
        JPanel containerPanel = new JPanel(new BorderLayout(0, 30));
        containerPanel.setOpaque(false);

        // ==================== TOP - Logo ====================
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("facebook") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(24, 119, 242),
                        getWidth(), 0, new Color(66, 103, 178));
                g2.setPaint(gradient);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 0, fm.getAscent());
                g2.dispose();
            }
        };
        logoLabel.setFont(new Font("Helvetica Neue", Font.BOLD, 52));
        logoLabel.setForeground(FacebookGUI.FB_BLUE);
        logoPanel.add(logoLabel);

        // ==================== CENTER - Signup Card ====================
        RoundedPanel signupCard = new RoundedPanel(8);
        signupCard.setBackground(Color.WHITE);
        signupCard.setLayout(new BoxLayout(signupCard, BoxLayout.Y_AXIS));
        signupCard.setBorder(new EmptyBorder(20, 25, 25, 25));

        // Fixed size to prevent overflow - increased height
        int cardWidth = 450;
        int cardHeight = 650;
        signupCard.setPreferredSize(new Dimension(cardWidth, cardHeight));
        signupCard.setMinimumSize(new Dimension(cardWidth, cardHeight));
        signupCard.setMaximumSize(new Dimension(cardWidth, cardHeight));

        // Title
        JLabel titleLabel = new JLabel("Create a new account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(FacebookGUI.FB_TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitleLabel = new JLabel("It's quick and easy.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(FacebookGUI.FB_TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Separator after title
        JSeparator titleSep = new JSeparator();
        titleSep.setForeground(new Color(218, 220, 224));
        titleSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        titleSep.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ==================== Name Row ====================
        JPanel nameRow = new JPanel(new GridLayout(1, 2, 12, 0));
        nameRow.setOpaque(false);
        nameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        firstNameField = new ModernTextField("First name");
        lastNameField = new ModernTextField("Surname");

        nameRow.add(firstNameField);
        nameRow.add(lastNameField);

        // ==================== Date of Birth Section ====================
        JPanel dobLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dobLabelPanel.setOpaque(false);
        dobLabelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        dobLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dobLabel = new JLabel("Date of birth ");
        dobLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dobLabel.setForeground(FacebookGUI.FB_TEXT_SECONDARY);

        // Info icon - simple circle with ?
        JLabel infoIcon = new JLabel("?") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(150, 150, 150));
                g2.fillOval(0, 0, 16, 16);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString("?", 5, 12);
                g2.dispose();
            }
        };
        infoIcon.setPreferredSize(new Dimension(16, 16));
        infoIcon.setToolTipText(
                "<html>Providing your date of birth helps make sure<br>you get the right Facebook experience for your age.</html>");
        infoIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

        dobLabelPanel.add(dobLabel);
        dobLabelPanel.add(infoIcon);

        // Date combo boxes
        JPanel dobRow = new JPanel(new GridLayout(1, 3, 12, 0));
        dobRow.setOpaque(false);
        dobRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        dobRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create day combo (1-31)
        Integer[] days = new Integer[31];
        for (int i = 0; i < 31; i++)
            days[i] = i + 1;
        dayCombo = new ModernComboBox<>(days);
        dayCombo.setPreferredSize(new Dimension(0, 45));

        // Set current day
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        dayCombo.setSelectedItem(currentDay);

        // Create month combo
        monthCombo = new ModernComboBox<>(MONTHS);
        monthCombo.setPreferredSize(new Dimension(0, 45));

        // Set current month
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        monthCombo.setSelectedIndex(currentMonth);

        // Create year combo (1905 - current year)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Integer[] years = new Integer[currentYear - 1905 + 1];
        for (int i = 0; i < years.length; i++)
            years[i] = currentYear - i;
        yearCombo = new ModernComboBox<>(years);
        yearCombo.setPreferredSize(new Dimension(0, 45));

        dobRow.add(dayCombo);
        dobRow.add(monthCombo);
        dobRow.add(yearCombo);

        // ==================== Gender Section ====================
        JPanel genderLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        genderLabelPanel.setOpaque(false);
        genderLabelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        genderLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel genderLabel = new JLabel("Gender ");
        genderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        genderLabel.setForeground(FacebookGUI.FB_TEXT_SECONDARY);

        JLabel genderInfoIcon = new JLabel("?") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(150, 150, 150));
                g2.fillOval(0, 0, 16, 16);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString("?", 5, 12);
                g2.dispose();
            }
        };
        genderInfoIcon.setPreferredSize(new Dimension(16, 16));
        genderInfoIcon.setToolTipText("<html>You can change who sees your gender on<br>your profile later.</html>");
        genderInfoIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

        genderLabelPanel.add(genderLabel);
        genderLabelPanel.add(genderInfoIcon);

        genderPanel = new GenderRadioPanel();
        genderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        genderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ==================== Email/Phone ====================
        emailField = new ModernTextField("Mobile number or email address");
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ==================== Password ====================
        passwordField = new ModernPasswordField("New password");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performSignup();
                }
            }
        });

        // ==================== Terms Text ====================
        JLabel termsLabel = new JLabel("<html><div style='width: 380px; font-size: 11px; color: #65676b;'>" +
                "People who use our service may have uploaded your contact information to Facebook. " +
                "<a href='#' style='color: #1877f2;'>Learn more</a>." +
                "</div></html>");
        termsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        termsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel policyLabel = new JLabel("<html><div style='width: 380px; font-size: 11px; color: #65676b;'>" +
                "By clicking Sign Up, you agree to our <a href='#' style='color: #1877f2;'>Terms</a>, " +
                "<a href='#' style='color: #1877f2;'>Privacy Policy</a> and " +
                "<a href='#' style='color: #1877f2;'>Cookies Policy</a>. " +
                "You may receive SMS notifications from us and can opt out at any time." +
                "</div></html>");
        policyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        policyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ==================== Error Label ====================
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        errorLabel.setForeground(FacebookGUI.FB_ERROR);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setBorder(new EmptyBorder(5, 0, 0, 0));

        // ==================== Signup Button ====================
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonContainer.setOpaque(false);
        buttonContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        signupButton = new AnimatedButton("Sign Up", FacebookGUI.FB_GREEN, FacebookGUI.FB_GREEN_HOVER);
        signupButton.setPreferredSize(new Dimension(200, 40));
        signupButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        signupButton.addActionListener(e -> performSignup());

        buttonContainer.add(signupButton);

        // ==================== Already have account link ====================
        JPanel loginLinkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginLinkPanel.setOpaque(false);
        loginLinkPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        loginLink = new LinkLabel("Already have an account?");
        loginLink.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginLink.addActionListener(e -> parent.showLoginPanel());

        loginLinkPanel.add(loginLink);

        // ==================== Add all to card ====================
        signupCard.add(titleLabel);
        signupCard.add(Box.createVerticalStrut(5));
        signupCard.add(subtitleLabel);
        signupCard.add(Box.createVerticalStrut(12));
        signupCard.add(titleSep);
        signupCard.add(Box.createVerticalStrut(15));
        signupCard.add(nameRow);
        signupCard.add(Box.createVerticalStrut(12));
        signupCard.add(dobLabelPanel);
        signupCard.add(Box.createVerticalStrut(5));
        signupCard.add(dobRow);
        signupCard.add(Box.createVerticalStrut(12));
        signupCard.add(genderLabelPanel);
        signupCard.add(Box.createVerticalStrut(5));
        signupCard.add(genderPanel);
        signupCard.add(Box.createVerticalStrut(12));
        signupCard.add(emailField);
        signupCard.add(Box.createVerticalStrut(10));
        signupCard.add(passwordField);
        signupCard.add(Box.createVerticalStrut(10));
        signupCard.add(termsLabel);
        signupCard.add(Box.createVerticalStrut(5));
        signupCard.add(policyLabel);
        signupCard.add(errorLabel);
        signupCard.add(Box.createVerticalStrut(12));
        signupCard.add(buttonContainer);
        signupCard.add(Box.createVerticalStrut(10));
        signupCard.add(loginLinkPanel);

        // ==================== Add to container ====================
        containerPanel.add(logoPanel, BorderLayout.NORTH);
        containerPanel.add(signupCard, BorderLayout.CENTER);

        add(containerPanel);
    }

    private void performSignup() {
        // Reset error states
        firstNameField.setError(false);
        lastNameField.setError(false);
        emailField.setError(false);
        passwordField.setError(false);
        errorLabel.setText(" ");

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = emailField.getText().trim();
        String password = passwordField.getPasswordText();

        // ==================== VALIDATIONS ====================

        // First name validation
        if (firstName.isEmpty()) {
            firstNameField.setError(true);
            showError("What's your name?");
            shakeComponent(firstNameField);
            return;
        }

        // Last name validation
        if (lastName.isEmpty()) {
            lastNameField.setError(true);
            showError("What's your surname?");
            shakeComponent(lastNameField);
            return;
        }

        // Gender validation
        if (!genderPanel.isGenderSelected()) {
            showError("Please select your gender.");
            shakeComponent(genderPanel);
            return;
        }

        // Username validation (8-12 characters, no spaces)
        if (username.isEmpty()) {
            emailField.setError(true);
            showError("You'll use this when you log in.");
            shakeComponent(emailField);
            return;
        }

        if (username.length() < 8 || username.length() > 12) {
            emailField.setError(true);
            showError("Username must be 8-12 characters long.");
            shakeComponent(emailField);
            return;
        }

        if (username.contains(" ")) {
            emailField.setError(true);
            showError("Username cannot contain spaces.");
            shakeComponent(emailField);
            return;
        }

        // Check if username exists
        if (Database.LoadUser(username) != null) {
            emailField.setError(true);
            showError("This username is already taken. Try another one.");
            shakeComponent(emailField);
            return;
        }

        // Password validation (8-15 characters, no spaces)
        if (password.isEmpty()) {
            passwordField.setError(true);
            showError("Enter a password.");
            shakeComponent(passwordField);
            return;
        }

        if (password.length() < 8 || password.length() > 15) {
            passwordField.setError(true);
            showError("Password must be 8-15 characters long.");
            shakeComponent(passwordField);
            return;
        }

        if (password.contains(" ")) {
            passwordField.setError(true);
            showError("Password cannot contain spaces.");
            shakeComponent(passwordField);
            return;
        }

        // ==================== Create Account ====================
        try {
            // Get date of birth
            int day = (Integer) dayCombo.getSelectedItem();
            int month = monthCombo.getSelectedIndex() + 1; // 1-based month
            int year = (Integer) yearCombo.getSelectedItem();
            LocalDate birthDate = LocalDate.of(year, month, day);

            // Get gender
            String genderStr = genderPanel.getSelectedGender();
            Gender gender = genderStr.equals("Female") ? Gender.FEMALE : Gender.MALE;

            // Create credentials
            Credentials credentials = new Credentials(username, password);

            // Create user with gender
            String bio = "Hey there! I'm using Facebook.";
            User newUser = new User(firstName, lastName, birthDate, bio, credentials, gender);

            // Save to database
            Database.Write_new_account(newUser);

            // Show success animation
            animateSuccess(() -> {
                JOptionPane.showMessageDialog(parent,
                        "Your account has been created successfully!\nYou can now log in.",
                        "Account Created",
                        JOptionPane.INFORMATION_MESSAGE);
                parent.showLoginPanel();
            });

        } catch (Exception e) {
            showError("Invalid date of birth. Please check your selection.");
        }
    }

    private void showError(String message) {
        errorLabel.setForeground(FacebookGUI.FB_ERROR);
        errorLabel.setText(message);

        // Fade in animation
        Timer fadeTimer = new Timer(20, null);
        final float[] alpha = { 0f };
        fadeTimer.addActionListener(e -> {
            alpha[0] += 0.1f;
            if (alpha[0] >= 1f) {
                fadeTimer.stop();
            }
            errorLabel.setForeground(new Color(220, 53, 69, (int) (alpha[0] * 255)));
        });
        fadeTimer.start();
    }

    private void shakeComponent(JComponent component) {
        Point originalLocation = component.getLocation();
        Timer shakeTimer = new Timer(20, null);
        final int[] shakeCount = { 0 };
        final int[] direction = { 1 };

        shakeTimer.addActionListener(e -> {
            if (shakeCount[0] >= 6) {
                component.setLocation(originalLocation);
                shakeTimer.stop();
                return;
            }

            int offset = 5 * direction[0];
            component.setLocation(originalLocation.x + offset, originalLocation.y);
            direction[0] *= -1;
            shakeCount[0]++;
        });
        shakeTimer.start();
    }

    private void animateSuccess(Runnable onComplete) {
        Timer successTimer = new Timer(100, null);
        final int[] count = { 0 };

        successTimer.addActionListener(e -> {
            count[0]++;
            if (count[0] >= 3) {
                successTimer.stop();
                onComplete.run();
            }
        });
        successTimer.start();
    }

    private void startFadeInAnimation() {
        fadeInTimer = new Timer(20, e -> {
            opacity += 0.05f;
            if (opacity >= 1f) {
                opacity = 1f;
                fadeInTimer.stop();
            }
            repaint();
        });
        fadeInTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g2.dispose();
    }

    public void resetFields() {
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        genderPanel.clearSelection();

        firstNameField.setError(false);
        lastNameField.setError(false);
        emailField.setError(false);
        passwordField.setError(false);
        errorLabel.setText(" ");

        // Reset date to current
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        dayCombo.setSelectedItem(currentDay);
        monthCombo.setSelectedIndex(currentMonth);
        yearCombo.setSelectedItem(currentYear);

        firstNameField.requestFocus();
    }
}
