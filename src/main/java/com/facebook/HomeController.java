package com.facebook;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private Button profileButton;

    @FXML
    private Button sidebarProfile;

    @FXML
    private Button friendsButton;

    @FXML
    private Button logoutButton;

    @FXML
    private VBox feedContainer;

    @FXML
    private TextField postInput;

    @FXML
    private VBox contactsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load User Data
        if (Main.current != null) {
            String fullName = Main.current.getFirstname() + " " + Main.current.getLastname();
            sidebarProfile.setText(fullName);
        } else {
            sidebarProfile.setText("Guest User");
        }

        // Load Feed
        loadFeed();

        // Load Contacts
        loadContacts();

        // Setup Post Input
        postInput.setOnAction(e -> createPost());

        // Setup Sidebar Actions
        friendsButton.setOnAction(e -> showFriendsView());

        // Setup Logout
        if (logoutButton != null) {
            logoutButton.setOnAction(this::logout);
        }
    }

    private void logout(ActionEvent event) {
        try {
            Main.current = null; // Clear current user
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Facebook - Login");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFeed() {
        feedContainer.getChildren().clear();

        boolean loadedFromDB = false;
        try {
            ArrayList<Post> posts = Database.Load_Feed();
            if (posts != null && !posts.isEmpty()) {
                for (Post p : posts) {
                    // Calculate time difference for display (simplified)
                    String timeDisplay = p.getTime().toString();
                    addPostToFeed(p.getSender(), timeDisplay, p.getText());
                }
                loadedFromDB = true;
            }
        } catch (Exception e) {
            System.out.println("Error loading feed: " + e.getMessage());
        }

        if (!loadedFromDB) {
            addPostToFeed("System", "Now", "No posts found in database or database not connected.");
        }
    }

    private void loadContacts() {
        contactsList.getChildren().clear();

        if (Main.current != null) {
            try {
                ArrayList<String> friends = Database.Load_Friends(Main.current.getCredentials().getUsername());
                if (friends != null && !friends.isEmpty()) {
                    for (String friend : friends) {
                        addContactItem(friend);
                    }
                } else {
                    addContactItem("No friends yet");
                }
            } catch (Exception e) {
                System.out.println("Error loading contacts: " + e.getMessage());
            }
        }
    }

    private void addContactItem(String name) {
        Button contactBtn = new Button("🟢 " + name);
        contactBtn.getStyleClass().add("contact-item");
        contactBtn.setMaxWidth(Double.MAX_VALUE);
        contactBtn.setAlignment(Pos.BASELINE_LEFT);
        contactsList.getChildren().add(contactBtn);
    }

    private void showFriendsView() {
        feedContainer.getChildren().clear();

        Label title = new Label("Friends");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #050505;");
        feedContainer.getChildren().add(title);

        if (Main.current != null) {
            try {
                ArrayList<String> friends = Database.Load_Friends(Main.current.getCredentials().getUsername());
                if (friends != null) {
                    for (String friendName : friends) {
                        HBox friendRow = new HBox(10);
                        friendRow.setAlignment(Pos.CENTER_LEFT);
                        friendRow.setStyle(
                                "-fx-background-color: white; -fx-padding: 10px; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);");

                        Label avatar = new Label("👤");
                        avatar.setStyle(
                                "-fx-font-size: 24px; -fx-background-color: #e4e6eb; -fx-background-radius: 50%; -fx-min-width: 50px; -fx-min-height: 50px; -fx-alignment: center;");

                        Label name = new Label(friendName);
                        name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #050505;");

                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);

                        Button messageBtn = new Button("Message");
                        messageBtn.getStyleClass().add("button");
                        messageBtn.getStyleClass().add("secondary");

                        friendRow.getChildren().addAll(avatar, name, spacer, messageBtn);
                        feedContainer.getChildren().add(friendRow);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error loading friends view: " + e.getMessage());
            }
        }
    }

    private void addPostToFeed(String author, String time, String content) {
        VBox postBox = new VBox(10);
        postBox.getStyleClass().add("feed-post");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label avatar = new Label(author.substring(0, 1));
        avatar.setStyle(
                "-fx-background-color: #ddd; -fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; -fx-alignment: center; -fx-text-fill: #050505;");

        VBox meta = new VBox(0);
        Label nameLabel = new Label(author);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #050505;");
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-text-fill: #65676b; -fx-font-size: 12px;");
        meta.getChildren().addAll(nameLabel, timeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button options = new Button("...");
        options.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676b;");

        header.getChildren().addAll(avatar, meta, spacer, options);

        // Content
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: #050505; -fx-font-size: 14px;");

        // Actions
        HBox actions = new HBox(0);
        actions.setAlignment(Pos.CENTER);
        actions.setSpacing(5);

        Button likeBtn = createActionButton("👍 Like");
        Button commentBtn = createActionButton("💬 Comment");
        Button shareBtn = createActionButton("↗ Share");

        // Make buttons expand
        likeBtn.setMaxWidth(Double.MAX_VALUE);
        commentBtn.setMaxWidth(Double.MAX_VALUE);
        shareBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(likeBtn, Priority.ALWAYS);
        HBox.setHgrow(commentBtn, Priority.ALWAYS);
        HBox.setHgrow(shareBtn, Priority.ALWAYS);

        actions.getChildren().addAll(likeBtn, commentBtn, shareBtn);

        postBox.getChildren().addAll(header, contentLabel, new javafx.scene.control.Separator(), actions);
        feedContainer.getChildren().add(postBox);
    }

    private Button createActionButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-font-weight: bold;");
        btn.setOnMouseEntered(
                e -> btn.setStyle("-fx-background-color: #f0f2f5; -fx-text-fill: #65676b; -fx-font-weight: bold;"));
        btn.setOnMouseExited(
                e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-font-weight: bold;"));
        return btn;
    }

    private void createPost() {
        String content = postInput.getText();
        if (content.isEmpty())
            return;

        addPostToFeed(Main.current != null ? Main.current.getFirstname() + " " + Main.current.getLastname() : "Me",
                "Just now", content);
        postInput.clear();

        // In real app, save to Database
        // Database.Write_Post(new Post(content, ...));
    }
}
