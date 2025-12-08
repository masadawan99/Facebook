package com.facebook;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML
    private Label nameLabel;

    @FXML
    private Label bioLabel;

    @FXML
    private Label introBioLabel;

    @FXML
    private Label friendsCountLabel;

    @FXML
    private VBox friendsGrid;

    @FXML
    private VBox postsContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ProfileController initializing...");
        try {
            if (Main.current != null) {
                System.out.println("Loading profile for: " + Main.current.getCredentials().getUsername());
                loadProfileData();
                System.out.println("Profile data loaded.");
                loadFriends();
                System.out.println("Friends loaded.");
                loadPosts();
                System.out.println("Posts loaded.");
            } else {
                System.out.println("Main.current is null!");
            }
        } catch (Exception e) {
            System.out.println("Error in ProfileController.initialize: ");
            e.printStackTrace();
        }
    }

    private void loadProfileData() {
        String fullName = Main.current.getFirstname() + " " + Main.current.getLastname();
        nameLabel.setText(fullName);
        bioLabel.setText(Main.current.getBio());
        introBioLabel.setText(Main.current.getBio());
    }

    private void loadFriends() {
        friendsGrid.getChildren().clear();
        ArrayList<String> friends = Database.Load_Friends(Main.current.getCredentials().getUsername());
        friendsCountLabel.setText(friends.size() + " friends");

        // Display up to 6 friends in a simple list for now (grid logic can be complex
        // in VBox)
        int count = 0;
        for (String friend : friends) {
            if (count >= 6)
                break;
            Label friendLabel = new Label("👤 " + friend);
            friendLabel.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");
            friendsGrid.getChildren().add(friendLabel);
            count++;
        }
    }

    private void loadPosts() {
        postsContainer.getChildren().clear();
        ArrayList<Post> posts = Database.Load_User_Posts(Main.current.getCredentials().getUsername());

        if (posts.isEmpty()) {
            Label noPosts = new Label("No posts yet.");
            noPosts.setStyle("-fx-text-fill: #65676b; -fx-font-size: 14px; -fx-padding: 20px;");
            postsContainer.getChildren().add(noPosts);
            return;
        }

        for (Post p : posts) {
            addPostToView(p);
        }
    }

    private void addPostToView(Post p) {
        VBox postBox = new VBox(10);
        postBox.getStyleClass().add("feed-post");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label avatar = new Label("👤");
        avatar.setStyle(
                "-fx-background-color: #ddd; -fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; -fx-alignment: center;");

        VBox meta = new VBox(0);
        Label nameLabel = new Label(p.getSender());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #050505;");
        Label timeLabel = new Label(p.getTime().toString()); // Simplified time
        timeLabel.setStyle("-fx-text-fill: #65676b; -fx-font-size: 12px;");
        meta.getChildren().addAll(nameLabel, timeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button options = new Button("...");
        options.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676b;");

        header.getChildren().addAll(avatar, meta, spacer, options);

        // Content
        Label contentLabel = new Label(p.getText());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: #050505; -fx-font-size: 14px;");

        // Actions
        HBox actions = new HBox(0);
        actions.setAlignment(Pos.CENTER);
        actions.setSpacing(5);

        Button likeBtn = createActionButton("👍 Like");
        Button commentBtn = createActionButton("💬 Comment");
        Button shareBtn = createActionButton("↗ Share");

        likeBtn.setMaxWidth(Double.MAX_VALUE);
        commentBtn.setMaxWidth(Double.MAX_VALUE);
        shareBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(likeBtn, Priority.ALWAYS);
        HBox.setHgrow(commentBtn, Priority.ALWAYS);
        HBox.setHgrow(shareBtn, Priority.ALWAYS);

        actions.getChildren().addAll(likeBtn, commentBtn, shareBtn);

        postBox.getChildren().addAll(header, contentLabel, new Separator(), actions);
        postsContainer.getChildren().add(postBox);
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

    @FXML
    void onBackClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("home-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Facebook - Home");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onLogoutClick(ActionEvent event) {
        try {
            Main.current = null;
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

    @FXML
    void onEditProfileClick(ActionEvent event) {
        // Placeholder for edit profile functionality
        System.out.println("Edit Profile Clicked");
    }
}
