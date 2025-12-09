package com.facebook;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    protected void onLoginButtonClick(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password.");
            return;
        }

        // Manual Login Fallback
        if (username.equals("admin") && password.equals("admin")) {
            System.out.println("Manual login bypass used.");
            // Create dummy user
            Main.current = new User("Admin", "User", null, "Admin Bio", new Credentials("admin", "admin"));
            navigateToHome(event);
            return;
        }

        // Logic from Main.java
        if (Database.Check_Database()) { // Ensure DB is ready
            User user = Database.LoadUser(username);
            if (user != null) {
                if (user.getCredentials().p_Verify(password)) {
                    Main.current = user; // Set current user
                    Database.Write_Online(); // Mark online
                    System.out.println("User logged in: " + username);
                    navigateToHome(event);
                } else {
                    showError("Invalid Password!");
                }
            } else {
                showError("User not found!");
            }
        } else {
            showError("Database error.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        // Simple fade out effect could be added here with FadeTransition
        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(javafx.util.Duration.seconds(3),
                errorLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(javafx.util.Duration.seconds(2));
        fadeOut.play();
    }

    private void navigateToHome(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("home-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Facebook");
            stage.setMaximized(true);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load Home Page.");
        }
    }

    @FXML
    protected void onSignupButtonClick(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("signup-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 500);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Facebook Sign Up");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void onForgotPasswordClick(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("forgot-password-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 550);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Forgot Password");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }
}
