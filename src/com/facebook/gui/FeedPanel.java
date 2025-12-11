package com.facebook.gui;

import com.facebook.Database;
import com.facebook.Main;
import com.facebook.Post;
import com.facebook.Comment;
import com.facebook.User;
import com.facebook.gui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Center feed panel showing posts
 * Features: Create post, view posts, like, comment
 */
public class FeedPanel extends JPanel {

    private FacebookGUI parent;
    private HomePage homePage;
    private JPanel postsContainer;

    public FeedPanel(FacebookGUI parent, HomePage homePage) {
        this.parent = parent;
        this.homePage = homePage;
        setBackground(FacebookGUI.FB_BACKGROUND);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(20, 0, 20, 0));
        initComponents();
    }

    private void initComponents() {
        // Create post card
        RoundedPanel createPostCard = new RoundedPanel(8);
        createPostCard.setBackground(Color.WHITE);
        createPostCard.setLayout(new BorderLayout(12, 12));
        createPostCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        createPostCard.setMaximumSize(new Dimension(600, 120));
        createPostCard.setPreferredSize(new Dimension(600, 120));
        createPostCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Top row with profile and input
        JPanel topRow = new JPanel(new BorderLayout(10, 0));
        topRow.setOpaque(false);

        // Profile circle
        JLabel profileCircle = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                g2.setColor(FacebookGUI.FB_BLUE);
                g2.fillOval(0, 0, 40, 40);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                String initial = Main.current.getFirstname().substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                int x = (40 - fm.stringWidth(initial)) / 2;
                int y = (40 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initial, x, y);

                g2.dispose();
            }
        };
        profileCircle.setPreferredSize(new Dimension(40, 40));

        // Create post button (opens dialog)
        JButton createPostBtn = new JButton("What's on your mind, " + Main.current.getFirstname() + "?");
        createPostBtn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        createPostBtn.setForeground(new Color(101, 103, 107));
        createPostBtn.setHorizontalAlignment(SwingConstants.LEFT);
        createPostBtn.setContentAreaFilled(false);
        createPostBtn.setBorderPainted(false);
        createPostBtn.setFocusPainted(false);
        createPostBtn.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        createPostBtn.setBackground(new Color(240, 242, 245));
        createPostBtn.setOpaque(true);
        createPostBtn.setBorder(new EmptyBorder(8, 12, 8, 12));

        createPostBtn.addActionListener(e -> showCreatePostDialog());

        topRow.add(profileCircle, BorderLayout.WEST);
        topRow.add(createPostBtn, BorderLayout.CENTER);

        createPostCard.add(topRow, BorderLayout.CENTER);

        add(createPostCard);
        add(Box.createVerticalStrut(15));

        // Posts container
        postsContainer = new JPanel();
        postsContainer.setLayout(new BoxLayout(postsContainer, BoxLayout.Y_AXIS));
        postsContainer.setBackground(FacebookGUI.FB_BACKGROUND);
        postsContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        refreshFeed();

        add(postsContainer);
    }

    public void refreshFeed() {
        postsContainer.removeAll();

        ArrayList<Post> posts = Database.Load_Feed();

        if (posts.isEmpty()) {
            JLabel noPosts = new JLabel("No posts to show");
            noPosts.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            noPosts.setForeground(FacebookGUI.FB_TEXT_SECONDARY);
            noPosts.setAlignmentX(Component.CENTER_ALIGNMENT);
            postsContainer.add(Box.createVerticalStrut(50));
            postsContainer.add(noPosts);
        } else {
            for (Post post : posts) {
                JPanel postCard = createPostCard(post);
                postsContainer.add(postCard);
                postsContainer.add(Box.createVerticalStrut(15));
            }
        }

        postsContainer.revalidate();
        postsContainer.repaint();
    }

    private JPanel createPostCard(Post post) {
        RoundedPanel card = new RoundedPanel(8);
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));
        card.setPreferredSize(new Dimension(600, 200));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Header (Author info)
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        User author = Database.LoadUser(post.getSender());
        if (author == null)
            return card;

        // Author profile circle
        JLabel authorCircle = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(FacebookGUI.FB_BLUE);
                g2.fillOval(0, 0, 40, 40);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String initial = author.getFirstname().substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                int x = (40 - fm.stringWidth(initial)) / 2;
                int y = (40 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initial, x, y);

                g2.dispose();
            }
        };
        authorCircle.setPreferredSize(new Dimension(40, 40));

        // Author name and time
        JPanel authorInfo = new JPanel();
        authorInfo.setLayout(new BoxLayout(authorInfo, BoxLayout.Y_AXIS));
        authorInfo.setOpaque(false);

        JLabel authorName = new JLabel(author.getFullName());
        authorName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        authorName.setForeground(FacebookGUI.FB_TEXT_PRIMARY);

        JLabel postTime = new JLabel("Just now");
        postTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        postTime.setForeground(FacebookGUI.FB_TEXT_SECONDARY);

        authorInfo.add(authorName);
        authorInfo.add(postTime);

        header.add(authorCircle, BorderLayout.WEST);
        header.add(authorInfo, BorderLayout.CENTER);

        // Content
        JLabel contentLabel = new JLabel("<html><div style='width: 550px;'>" + post.getContent() + "</div></html>");
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        contentLabel.setForeground(FacebookGUI.FB_TEXT_PRIMARY);
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentLabel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Like and comment counts
        ArrayList<String> likes = Database.Load_Post_Likes(post);
        ArrayList<Comment> comments = Database.Load_Post_Comments(post);

        JPanel countsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        countsPanel.setOpaque(false);
        countsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel likesLabel = new JLabel("❤️ " + likes.size());
        likesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        likesLabel.setForeground(FacebookGUI.FB_TEXT_SECONDARY);

        JLabel commentsLabel = new JLabel("     💬 " + comments.size() + " comments");
        commentsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        commentsLabel.setForeground(FacebookGUI.FB_TEXT_SECONDARY);

        countsPanel.add(likesLabel);
        countsPanel.add(commentsLabel);

        // Action buttons
        JSeparator sep1 = new JSeparator();
        sep1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JPanel actionsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        boolean hasLiked = Database.Has_Liked(post);

        JButton likeBtn = createActionButton(hasLiked ? "❤️" : "🤍", hasLiked ? "Liked" : "Like");
        JButton commentBtn = createActionButton("💬", "Comment");

        likeBtn.addActionListener(e -> {
            if (hasLiked) {
                Database.Remove_Like(post);
            } else {
                Database.Write_Like(post);
                if (!post.getSender().equals(Main.current.getCredentials().getUsername())) {
                    Database.Write_Notification(post.getSender(),
                            Main.Input_NotificationL());
                }
            }
            refreshFeed();
        });

        commentBtn.addActionListener(e -> showCommentDialog(post));

        actionsPanel.add(likeBtn);
        actionsPanel.add(commentBtn);

        // Add all to card
        card.add(header);
        card.add(contentLabel);
        card.add(countsPanel);
        card.add(Box.createVerticalStrut(8));
        card.add(sep1);
        card.add(Box.createVerticalStrut(5));
        card.add(actionsPanel);

        return card;
    }

    private JButton createActionButton(String icon, String text) {
        JButton btn = new JButton(icon + " " + text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(FacebookGUI.FB_TEXT_SECONDARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setOpaque(true);
                btn.setBackground(new Color(240, 242, 245));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setOpaque(false);
            }
        });

        return btn;
    }

    private void showCreatePostDialog() {
        JDialog dialog = new JDialog(parent, "Create Post", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Create post");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JTextArea contentArea = new JTextArea("What's on your mind, " + Main.current.getFirstname() + "?");
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        contentArea.setForeground(new Color(150, 150, 150));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        contentArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (contentArea.getForeground().equals(new Color(150, 150, 150))) {
                    contentArea.setText("");
                    contentArea.setForeground(FacebookGUI.FB_TEXT_PRIMARY);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(219, 223, 231)));

        AnimatedButton postBtn = new AnimatedButton("Post", FacebookGUI.FB_BLUE, FacebookGUI.FB_BLUE_HOVER);
        postBtn.setPreferredSize(new Dimension(0, 40));

        postBtn.addActionListener(e -> {
            String content = contentArea.getText().trim();
            if (!content.isEmpty() && !content.equals("What's on your mind, " + Main.current.getFirstname() + "?")) {
                Post newPost = new Post(content, Main.current.getCredentials().getUsername());
                String path = Database.Write_Post(newPost);
                Database.WriteFeed(path, Main.current.getCredentials().getUsername(), newPost);

                // Add to friends' feeds
                ArrayList<String> friends = Database.Load_Friends(Main.current.getCredentials().getUsername());
                Main.Add_in_Feed(friends, path, newPost, false);

                dialog.dispose();
                refreshFeed();
            }
        });

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(postBtn, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showCommentDialog(Post post) {
        JDialog dialog = new JDialog(parent, "Comments", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Comments list
        JPanel commentsList = new JPanel();
        commentsList.setLayout(new BoxLayout(commentsList, BoxLayout.Y_AXIS));
        commentsList.setBackground(Color.WHITE);

        ArrayList<Comment> comments = Database.Load_Post_Comments(post);
        for (Comment comment : comments) {
            JPanel commentItem = new JPanel(new BorderLayout(8, 0));
            commentItem.setBackground(new Color(240, 242, 245));
            commentItem.setBorder(new EmptyBorder(10, 10, 10, 10));
            commentItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

            JLabel commentText = new JLabel("<html>" + comment.getSender() + ": " + comment.getContent() + "</html>");
            commentText.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            commentItem.add(commentText);
            commentsList.add(commentItem);
            commentsList.add(Box.createVerticalStrut(8));
        }

        JScrollPane scrollPane = new JScrollPane(commentsList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Add comment input
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(Color.WHITE);

        ModernTextField commentField = new ModernTextField("Write a comment...");
        AnimatedButton sendBtn = new AnimatedButton("Send", FacebookGUI.FB_BLUE, FacebookGUI.FB_BLUE_HOVER);
        sendBtn.setPreferredSize(new Dimension(80, 40));

        sendBtn.addActionListener(e -> {
            String commentText = commentField.getText().trim();
            if (!commentText.isEmpty()) {
                Comment newComment = Main.Input_Comment();
                Database.Write_Comment(post, newComment);

                if (!post.getSender().equals(Main.current.getCredentials().getUsername())) {
                    Database.Write_Notification(post.getSender(), Main.Input_NotificationC());
                }

                dialog.dispose();
                refreshFeed();
            }
        });

        inputPanel.add(commentField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }
}
