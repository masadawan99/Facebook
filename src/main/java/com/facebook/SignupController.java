package com.facebook;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class SignupController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private DatePicker dobPicker;
    @FXML
    private TextArea bioArea;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;

    @FXML
    protected void onSignupButtonClick(ActionEvent event) {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        LocalDate dob = dobPicker.getValue();
        String bio = bioArea.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || dob == null || bio.isEmpty() || username.isEmpty()
                || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        if (username.length() < 8 || username.length() > 12) {
            errorLabel.setText("Username must be 8–12 characters.");
            return;
        }
        if (username.contains(" ")) {
            errorLabel.setText("No spaces allowed in username.");
            return;
        }
        if (Database.LoadUser(username) != null) {
            errorLabel.setText("Username already exists!");
            return;
        }

        if (password.length() < 8 || password.length() > 15) {
            errorLabel.setText("Password must be 8–15 characters.");
            return;
        }
        if (password.contains(" ")) {
            errorLabel.setText("Password cannot contain spaces.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords don't match.");
            return;
        }

        Credentials credentials = new Credentials(username, password);
        User newUser = new User(firstName, lastName, dob, bio, credentials);
        Database.Write_new_account(newUser);

        errorLabel.setText("Account Created! Please Login.");
        // Optionally clear fields or navigate back
    }

    @FXML
    protected void onBackButtonClick(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Facebook Login");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }
}
