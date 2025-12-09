package com.facebook;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;

public class ForgotPasswordController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private DatePicker dobPicker;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button verifyButton;

    @FXML
    private Button changePasswordButton;

    private User verifiedUser = null;

    @FXML
    void onVerifyIdentityClick(ActionEvent event) {
        String username = usernameField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        LocalDate dob = dobPicker.getValue();

        errorLabel.setVisible(false);
        statusLabel.setVisible(false);

        // Validation: Check if all fields are filled
        if (username.isEmpty()) {
            showError("Please enter your username!");
            shakeNode(usernameField);
            return;
        }

        if (firstName.isEmpty()) {
            showError("Please enter your first name!");
            shakeNode(firstNameField);
            return;
        }

        if (lastName.isEmpty()) {
            showError("Please enter your last name!");
            shakeNode(lastNameField);
            return;
        }

        if (dob == null) {
            showError("Please select your date of birth!");
            shakeNode(dobPicker);
            return;
        }

        // Check if username exists
        if (!Main.Username_Already_Exists(username)) {
            showError("Invalid Username! User not found.");
            shakeNode(usernameField);
            return;
        }

        // Load user and verify identity
        User temp = Database.LoadUser(username);
        if (temp.getFirstname().equalsIgnoreCase(firstName) &&
                temp.getLastname().equalsIgnoreCase(lastName) &&
                temp.getBirth().equals(dob)) {

            verifiedUser = temp;
            showSuccess("✓ Identity Verified! You can now change your password.");

            // Enable password fields with animation
            enablePasswordFields();

        } else {
            showError("Identity verification failed! Please check your details.");
            shakeNode(verifyButton);
        }
    }

    @FXML
    void onChangePasswordClick(ActionEvent event) {
        if (verifiedUser == null) {
            showError("Please verify your identity first!");
            return;
        }

        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        errorLabel.setVisible(false);
        statusLabel.setVisible(false);

        // Validation: Check if password fields are filled
        if (newPassword.isEmpty()) {
            showError("Please enter a new password!");
            shakeNode(newPasswordField);
            return;
        }

        if (confirmPassword.isEmpty()) {
            showError("Please confirm your new password!");
            shakeNode(confirmPasswordField);
            return;
        }

        if (newPassword.length() < 6) {
            showError("Password must be at least 6 characters long!");
            shakeNode(newPasswordField);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords don't match! Please try again.");
            shakeNode(confirmPasswordField);
            return;
        }

        // Change password
        verifiedUser.getCredentials().setPassword(newPassword);
        Database.WriteUser(verifiedUser);

        showSuccess("✓ Password changed successfully! Redirecting to login...");

        // Disable buttons to prevent multiple clicks
        changePasswordButton.setDisable(true);

        // Redirect to login after 2 seconds
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> {
                    try {
                        onBackClick(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    void onBackClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setMaximized(true);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setStyle(
                "-fx-text-fill: #c92a2a; -fx-font-weight: bold; -fx-background-color: #ffe0e0; -fx-padding: 10px; -fx-background-radius: 5px;");

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), errorLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setStyle(
                "-fx-text-fill: #2b8a3e; -fx-font-weight: bold; -fx-background-color: #d3f9d8; -fx-padding: 10px; -fx-background-radius: 5px;");

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), statusLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        // Scale animation
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), statusLabel);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
    }

    private void shakeNode(Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(100), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void enablePasswordFields() {
        newPasswordField.setDisable(false);
        confirmPasswordField.setDisable(false);
        changePasswordButton.setDisable(false);

        // Fade in animation for password section
        FadeTransition fade1 = new FadeTransition(Duration.millis(400), newPasswordField);
        fade1.setFromValue(0.3);
        fade1.setToValue(1.0);
        fade1.play();

        FadeTransition fade2 = new FadeTransition(Duration.millis(400), confirmPasswordField);
        fade2.setFromValue(0.3);
        fade2.setToValue(1.0);
        fade2.play();

        FadeTransition fade3 = new FadeTransition(Duration.millis(400), changePasswordButton);
        fade3.setFromValue(0.3);
        fade3.setToValue(1.0);
        fade3.play();
    }

    @FXML
    void initialize() {
        // Disable password fields initially
        newPasswordField.setDisable(true);
        confirmPasswordField.setDisable(true);
        changePasswordButton.setDisable(true);

        // Set initial opacity for disabled fields
        newPasswordField.setOpacity(0.5);
        confirmPasswordField.setOpacity(0.5);
        changePasswordButton.setOpacity(0.5);
    }
}
