package com.facebook;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

    @FXML
    private Button homeButton;

    @FXML
    private Button friendsNavButton;

    @FXML
    private Button postButton;

    @FXML
    private Button findFriendsSidebarButton;

    @FXML
    private Button notificationsButton;

    @FXML
    private Button messagesButton;

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
        if (postButton != null) {
            postButton.setOnAction(e -> createPost());
        }

        // Setup Sidebar Actions
        friendsButton.setOnAction(e -> showFriendsView());
        if (findFriendsSidebarButton != null) {
            findFriendsSidebarButton.setOnAction(e -> showFindFriendsView());
        }

        // Setup Navbar Actions
        if (homeButton != null) {
            homeButton.setOnAction(e -> {
                loadFeed();
            });
        }
        if (friendsNavButton != null) {
            friendsNavButton.setOnAction(e -> showFriendsView());
        }
        if (notificationsButton != null) {
            notificationsButton.setOnAction(e -> showNotificationsDropdown());
        }
        if (messagesButton != null) {
            messagesButton.setOnAction(e -> showMessagesDropdown());
            updateMessagesBadge();

            // Poll for new messages every 2 seconds
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), event -> updateMessagesBadge()));
            timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
            timeline.play();
        }

        // Setup Logout
        if (logoutButton != null) {
            logoutButton.setOnAction(this::logout);
        }

        // Setup Profile Button
        if (profileButton != null) {
            profileButton.setOnAction(this::openProfile);
        }
        if (sidebarProfile != null) {
            sidebarProfile.setOnAction(this::openProfile);
        }
    }

    private void updateMessagesBadge() {
        if (Main.current != null) {
            Database.Compute_Read_Unread();
            ArrayList<Notification> notifications = Database.Load_Unread_Notification();
            long unreadMessages = notifications.stream()
                    .filter(n -> n.getType() == Notification.Type.MESSAGE)
                    .count();

            if (unreadMessages > 0) {
                messagesButton.setText("💬 (" + unreadMessages + ")");
                messagesButton.setStyle(
                        "-fx-background-color: #e7f3ff; -fx-text-fill: #1877f2; -fx-font-weight: bold; -fx-background-radius: 50%;");
            } else {
                messagesButton.setText("💬");
                messagesButton.getStyleClass().add("round-button");
                messagesButton.setStyle("");
            }
        }
    }

    private void showMessagesDropdown() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);");

        if (Main.current != null) {
            ArrayList<Chat> chats = Database.LoadInbox();

            if (chats.isEmpty()) {
                MenuItem emptyItem = new MenuItem("No active chats");
                emptyItem.setDisable(true);
                contextMenu.getItems().add(emptyItem);
            } else {
                for (Chat chat : chats) {
                    if (chat instanceof DM_chat) {
                        DM_chat dm = (DM_chat) chat;
                        String friendName = dm.getR_username();

                        HBox content = new HBox(10);
                        content.setAlignment(Pos.CENTER_LEFT);
                        content.setPadding(new Insets(10));
                        content.setPrefWidth(250);

                        Label avatar = new Label("👤");
                        avatar.setStyle(
                                "-fx-background-color: #ddd; -fx-background-radius: 50%; -fx-min-width: 30px; -fx-min-height: 30px; -fx-alignment: center;");

                        Label name = new Label(friendName);
                        name.setStyle("-fx-font-weight: bold; -fx-text-fill: #050505; -fx-font-size: 14px;");

                        content.getChildren().addAll(avatar, name);

                        MenuItem item = new MenuItem();
                        item.setGraphic(content);
                        item.setOnAction(e -> openChat(friendName));
                        contextMenu.getItems().add(item);
                    }
                }
            }

            contextMenu.getItems().add(new SeparatorMenuItem());
            MenuItem newMessageItem = new MenuItem("New Message");
            newMessageItem.setOnAction(e -> showFindFriendsView());
            contextMenu.getItems().add(newMessageItem);
        }

        contextMenu.show(messagesButton, Side.BOTTOM, 0, 0);
    }

    private void showNotificationsDropdown() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);");

        if (Main.current != null) {
            // 1. Load Friend Requests
            List<String> friendRequests = Database.Load_Requests_Recieved();
            if (!friendRequests.isEmpty()) {
                for (String requestSender : friendRequests) {
                    VBox content = new VBox(5);
                    content.setPadding(new Insets(10));
                    content.setPrefWidth(300);
                    content.setStyle("-fx-background-color: #e7f3ff; -fx-background-radius: 5px;"); // Highlight
                                                                                                    // requests

                    Label text = new Label(requestSender + " sent you a friend request");
                    text.setWrapText(true);
                    text.setStyle("-fx-font-weight: bold; -fx-text-fill: #050505;");

                    HBox actions = new HBox(10);
                    actions.setAlignment(Pos.CENTER_LEFT);

                    Button confirmBtn = new Button("Confirm");
                    confirmBtn.setStyle(
                            "-fx-background-color: #1877f2; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 5 10 5 10;");
                    confirmBtn.setOnAction(e -> {
                        Database.WriteFriend(requestSender);
                        Database.Delete_FriendRequest_Recieved(requestSender);
                        showNotificationsDropdown(); // Refresh
                    });

                    Button deleteBtn = new Button("Delete");
                    deleteBtn.setStyle(
                            "-fx-background-color: #e4e6eb; -fx-text-fill: #050505; -fx-font-size: 10px; -fx-padding: 5 10 5 10;");
                    deleteBtn.setOnAction(e -> {
                        Database.Delete_FriendRequest_Recieved(requestSender);
                        showNotificationsDropdown(); // Refresh
                    });

                    actions.getChildren().addAll(confirmBtn, deleteBtn);
                    content.getChildren().addAll(text, actions);

                    MenuItem item = new MenuItem();
                    item.setGraphic(content);
                    contextMenu.getItems().add(item);
                }
                contextMenu.getItems().add(new SeparatorMenuItem());
            }

            // 2. Load Notifications
            Database.Compute_Read_Unread(); // Refresh notifications
            ArrayList<Notification> notifications = Database.Load_Unread_Notification();

            if (notifications.isEmpty() && friendRequests.isEmpty()) {
                MenuItem emptyItem = new MenuItem("No new notifications");
                emptyItem.setDisable(true);
                contextMenu.getItems().add(emptyItem);
            } else {
                for (Notification notif : notifications) {
                    VBox content = new VBox(5);
                    content.setPadding(new Insets(10));
                    content.setPrefWidth(300);

                    Label text = new Label(notif.getText());
                    text.setWrapText(true);
                    text.setStyle("-fx-font-weight: bold; -fx-text-fill: #050505;");

                    Label time = new Label(
                            notif.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")));
                    time.setStyle("-fx-font-size: 10px; -fx-text-fill: #65676b;");

                    content.getChildren().addAll(text, time);

                    MenuItem item = new MenuItem();
                    item.setGraphic(content);
                    contextMenu.getItems().add(item);
                }
            }
        }

        contextMenu.show(notificationsButton, Side.BOTTOM, 0, 0);
    }

    private void openProfile(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("profile-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Facebook - Profile");
            stage.setMaximized(true);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logout(ActionEvent event) {
        try {
            Main.current = null;
            Database.Delete_Online();
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Facebook - Login");
            stage.setMaximized(true);
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

        contactBtn.setOnAction(e -> openChat(name));

        contactsList.getChildren().add(contactBtn);
    }

    private void openChat(String friendName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("chat-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 600);

            ChatController controller = fxmlLoader.getController();
            controller.initializeChat(friendName);

            Stage stage = (Stage) contactsList.getScene().getWindow();
            stage.setTitle("Facebook - Chat with " + friendName);
            stage.setMaximized(true);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                        messageBtn.setOnAction(e -> openChat(friendName));

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

        if (Main.current == null)
            return;

        Post post = new Post(content, Main.current.getCredentials().getUsername());
        post.setTagged(new ArrayList<>());

        // Save Post to DB
        String path = Database.Write_Post(post);

        // Add to Own Feed
        Database.WriteFeed(path, Main.current.getCredentials().getUsername(), post);

        // Add to Friends' Feeds (Equivalent to "Friends Only" option)
        ArrayList<String> friends = Database.Load_Friends(Main.current.getCredentials().getUsername());
        if (friends != null) {
            for (String friend : friends) {
                // Privacy Check: Ensure they are actually friends (Double check)
                // Note: Load_Friends should strictly return friends, but we add this for
                // safety.
                // We use Database.LoadUser(friend) to get the User object for Already_Friend
                // check.
                // However, Already_Friend checks if 'friend' is in 'Main.current's friend list.
                // which is exactly what Load_Friends returns.
                // So this check is redundant unless Load_Friends is broken.
                // But let's add it if we can.
                // User u = Database.LoadUser(friend);
                // if (!Database.Already_Friend(u)) continue;

                Database.WriteFeed(path, friend, post);

                // Send Notification (TAG type as per CLI convention for new posts/tags)
                // Using Input_NotificationT() logic: "Tagged you in a post"
                // Or maybe we should create a new Notification type or text?
                // CLI uses Input_NotificationT() for "Friends Only" posts in Add_in_Feed.
                // So we stick to that.
                Notification notif = new Notification(Notification.Type.TAG, Main.current.getCredentials().getUsername()
                        + " posted: " + (content.length() > 20 ? content.substring(0, 20) + "..." : content));
                Database.Write_Notification(friend, notif);
            }
        }

        postInput.clear();
        loadFeed();
    }

    private void showFindFriendsView() {
        feedContainer.getChildren().clear();

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Find Friends");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #050505;");

        Button sentRequestsBtn = new Button("Sent Requests");
        sentRequestsBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #1877f2; -fx-font-weight: bold; -fx-cursor: hand;");
        sentRequestsBtn.setOnAction(e -> showSentRequestsView());

        Button receivedRequestsBtn = new Button("Received Requests");
        receivedRequestsBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #1877f2; -fx-font-weight: bold; -fx-cursor: hand;");
        receivedRequestsBtn.setOnAction(e -> showReceivedRequestsView());

        header.getChildren().addAll(title, new Region(), sentRequestsBtn, receivedRequestsBtn);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS); // Spacer

        feedContainer.getChildren().add(header);

        if (Main.current != null) {
            try {
                ArrayList<User> allUsers = Database.LoadUsers();
                String currentUsername = Main.current.getCredentials().getUsername();

                for (User user : allUsers) {
                    String username = user.getCredentials().getUsername();

                    // Skip self
                    if (username.equals(currentUsername))
                        continue;

                    // Skip existing friends
                    if (Database.Already_Friend(user))
                        continue;

                    // Skip if request already sent (optional, but good UX)
                    boolean requestSent = Database.F_Request_Already_sent(username);

                    HBox userRow = new HBox(10);
                    userRow.setAlignment(Pos.CENTER_LEFT);
                    userRow.setStyle(
                            "-fx-background-color: white; -fx-padding: 10px; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);");

                    Label avatar = new Label("👤");
                    avatar.setStyle(
                            "-fx-background-color: #ddd; -fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; -fx-alignment: center;");

                    VBox info = new VBox(2);
                    Label name = new Label(user.getFirstname() + " " + user.getLastname());
                    name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    Label bio = new Label(user.getBio());
                    bio.setStyle("-fx-text-fill: #65676b; -fx-font-size: 12px;");
                    info.getChildren().addAll(name, bio);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button actionBtn = new Button(requestSent ? "Request Sent" : "Add Friend");
                    actionBtn.getStyleClass().add("button");
                    if (requestSent) {
                        actionBtn.setDisable(true);
                        actionBtn.setStyle("-fx-background-color: #e4e6eb; -fx-text-fill: #bcc0c4;");
                    } else {
                        actionBtn.setStyle(
                                "-fx-background-color: #e7f3ff; -fx-text-fill: #1877f2; -fx-font-weight: bold;");
                        actionBtn.setOnAction(e -> {
                            Database.WriteFriendRequestRecieved(username);
                            Database.WriteFriendRequestSent(username);
                            actionBtn.setText("Request Sent");
                            actionBtn.setDisable(true);
                            actionBtn.setStyle("-fx-background-color: #e4e6eb; -fx-text-fill: #bcc0c4;");
                        });
                    }

                    userRow.getChildren().addAll(avatar, info, spacer, actionBtn);
                    feedContainer.getChildren().add(userRow);
                }
            } catch (Exception e) {
                System.out.println("Error loading find friends view: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showSentRequestsView() {
        feedContainer.getChildren().clear();

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Sent Requests");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #050505;");

        Button backBtn = new Button("Back to Find Friends");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1877f2; -fx-cursor: hand;");
        backBtn.setOnAction(e -> showFindFriendsView());

        header.getChildren().addAll(title, new Region(), backBtn);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        feedContainer.getChildren().add(header);

        ArrayList<String> sentRequests = Database.Load_Requests_Sent();
        if (sentRequests.isEmpty()) {
            feedContainer.getChildren().add(new Label("No sent requests."));
            return;
        }

        for (String username : sentRequests) {
            User user = Database.LoadUser(username);

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                    "-fx-background-color: white; -fx-padding: 10px; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);");

            Label avatar = new Label("👤");
            avatar.setStyle(
                    "-fx-background-color: #ddd; -fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; -fx-alignment: center;");

            Label name = new Label(user.getFirstname() + " " + user.getLastname());
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button cancelBtn = new Button("Cancel Request");
            cancelBtn.getStyleClass().add("button");
            cancelBtn.setStyle("-fx-background-color: #e4e6eb; -fx-text-fill: #050505;");
            cancelBtn.setOnAction(e -> {
                Database.Delete_FriendRequest_Sent(username);
                showSentRequestsView(); // Refresh
            });

            row.getChildren().addAll(avatar, name, spacer, cancelBtn);
            feedContainer.getChildren().add(row);
        }
    }

    private void showReceivedRequestsView() {
        feedContainer.getChildren().clear();

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Received Requests");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #050505;");

        Button backBtn = new Button("Back to Find Friends");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1877f2; -fx-cursor: hand;");
        backBtn.setOnAction(e -> showFindFriendsView());

        header.getChildren().addAll(title, new Region(), backBtn);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        feedContainer.getChildren().add(header);

        List<String> receivedRequests = Database.Load_Requests_Recieved();
        if (receivedRequests.isEmpty()) {
            feedContainer.getChildren().add(new Label("No received requests."));
            return;
        }

        for (String username : receivedRequests) {
            User user = Database.LoadUser(username);

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                    "-fx-background-color: white; -fx-padding: 10px; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);");

            Label avatar = new Label("👤");
            avatar.setStyle(
                    "-fx-background-color: #ddd; -fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; -fx-alignment: center;");

            Label name = new Label(user.getFirstname() + " " + user.getLastname());
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button confirmBtn = new Button("Confirm");
            confirmBtn.getStyleClass().add("button");
            confirmBtn.setStyle("-fx-background-color: #1877f2; -fx-text-fill: white;");
            confirmBtn.setOnAction(e -> {
                Database.WriteFriend(username);
                Database.Delete_FriendRequest_Recieved(username);
                showReceivedRequestsView(); // Refresh
            });

            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("button");
            deleteBtn.setStyle("-fx-background-color: #e4e6eb; -fx-text-fill: #050505;");
            deleteBtn.setOnAction(e -> {
                Database.Delete_FriendRequest_Recieved(username);
                showReceivedRequestsView(); // Refresh
            });

            row.getChildren().addAll(avatar, name, spacer, confirmBtn, deleteBtn);
            feedContainer.getChildren().add(row);
        }
    }
}
