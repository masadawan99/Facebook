package com.facebook.gui;

import com.facebook.Main;
import com.facebook.gui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Top Navigation Bar for Facebook Home Page
 * Features: Logo, Search, Icons for
 * Home/Friends/Messages/Games/Notifications/Profile
 */
public class TopNavBar extends JPanel {

    private FacebookGUI parent;
    private HomePage homePage;
    private JTextField searchField;
    private IconButton homeBtn, friendsBtn, watchBtn, marketplaceBtn, groupsBtn;
    private IconButton messagesBtn, notificationsBtn, gamesBtn;
    private JButton profileBtn;

    // Notification badges
    private int messageCount = 0;
    private int notificationCount = 0;

    public TopNavBar(FacebookGUI parent, HomePage homePage) {
        this.parent = parent;
        this.homePage = homePage;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(0, 56));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(219, 223, 231)));
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // ==================== LEFT SECTION (Logo + Search) ====================
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        // Facebook Logo
        JLabel logoLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw filled circle
                g2.setColor(FacebookGUI.FB_BLUE);
                g2.fillOval(0, 0, 40, 40);

                // Draw 'f' letter
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Helvetica Neue", Font.BOLD, 28));
                g2.drawString("f", 14, 30);

                g2.dispose();
            }
        };
        logoLabel.setPreferredSize(new Dimension(40, 40));
        logoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                homePage.refreshFeed();
            }
        });

        // Search Field
        searchField = new JTextField("Search Facebook") {
            private boolean showPlaceholder = true;

            {
                setForeground(new Color(150, 150, 150));
                addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        if (showPlaceholder) {
                            setText("");
                            setForeground(FacebookGUI.FB_TEXT_PRIMARY);
                            showPlaceholder = false;
                        }
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        if (getText().isEmpty()) {
                            setText("Search Facebook");
                            setForeground(new Color(150, 150, 150));
                            showPlaceholder = true;
                        }
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(240, 242, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        searchField.setPreferredSize(new Dimension(240, 40));
        searchField.setOpaque(false);
        searchField.setBorder(new EmptyBorder(10, 40, 10, 15));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JPanel searchPanel = new JPanel(null);
        searchPanel.setPreferredSize(new Dimension(240, 40));
        searchPanel.setOpaque(false);

        // Search icon
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchIcon.setBounds(12, 10, 20, 20);

        searchField.setBounds(0, 0, 240, 40);
        searchPanel.add(searchIcon);
        searchPanel.add(searchField);

        leftPanel.add(Box.createHorizontalStrut(5));
        leftPanel.add(logoLabel);
        leftPanel.add(Box.createHorizontalStrut(5));
        leftPanel.add(searchPanel);

        // ==================== CENTER SECTION (Navigation Icons) ====================
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 8));
        centerPanel.setOpaque(false);

        homeBtn = new IconButton("🏠", "Home", true);
        friendsBtn = new IconButton("👥", "Friends", false);
        watchBtn = new IconButton("📺", "Watch", false);
        marketplaceBtn = new IconButton("🛒", "Marketplace", false);
        groupsBtn = new IconButton("👨‍👩‍👧‍👦", "Groups", false);

        homeBtn.addActionListener(e -> {
            setActiveButton(homeBtn);
            homePage.refreshFeed();
        });

        friendsBtn.addActionListener(e -> {
            setActiveButton(friendsBtn);
            // TODO: Show friends page
        });

        watchBtn.addActionListener(e -> {
            setActiveButton(watchBtn);
            // TODO: Show watch page
        });

        marketplaceBtn.addActionListener(e -> {
            setActiveButton(marketplaceBtn);
            // TODO: Show marketplace
        });

        groupsBtn.addActionListener(e -> {
            setActiveButton(groupsBtn);
            // TODO: Show groups
        });

        centerPanel.add(homeBtn);
        centerPanel.add(friendsBtn);
        centerPanel.add(watchBtn);
        centerPanel.add(marketplaceBtn);
        centerPanel.add(groupsBtn);

        // ==================== RIGHT SECTION (Actions) ====================
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rightPanel.setOpaque(false);

        // Games button
        gamesBtn = new IconButton("🎮", "Games", false);
        gamesBtn.addActionListener(e -> homePage.openGamesDialog());

        // Messages button
        messagesBtn = new IconButton("💬", "Messenger", false);
        messagesBtn.setBadgeCount(messageCount);
        messagesBtn.addActionListener(e -> showMessengerDialog());

        // Notifications button
        notificationsBtn = new IconButton("🔔", "Notifications", false);
        notificationsBtn.setBadgeCount(notificationCount);
        notificationsBtn.addActionListener(e -> showNotificationsDialog());

        // Profile button with dropdown
        profileBtn = createProfileButton();

        rightPanel.add(gamesBtn);
        rightPanel.add(messagesBtn);
        rightPanel.add(notificationsBtn);
        rightPanel.add(profileBtn);
        rightPanel.add(Box.createHorizontalStrut(10));

        // ==================== Add to main panel ====================
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private void setActiveButton(IconButton activeBtn) {
        homeBtn.setActive(false);
        friendsBtn.setActive(false);
        watchBtn.setActive(false);
        marketplaceBtn.setActive(false);
        groupsBtn.setActive(false);
        activeBtn.setActive(true);
    }

    private JButton createProfileButton() {
        JButton btn = new JButton() {
            private boolean isHovered = false;

            {
                setPreferredSize(new Dimension(40, 40));
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }
                });

                addActionListener(e -> showProfileMenu());
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw background
                if (isHovered) {
                    g2.setColor(new Color(240, 242, 245));
                    g2.fillOval(0, 0, 40, 40);
                }

                // Draw profile initial
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

        return btn;
    }

    private void showProfileMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createLineBorder(new Color(219, 223, 231)));

        JMenuItem profileItem = new JMenuItem(Main.current.getFirstname() + " " + Main.current.getLastname());
        profileItem.setFont(new Font("Segoe UI", Font.BOLD, 14));
        profileItem.setIcon(createProfileIcon());

        JMenuItem settingsItem = new JMenuItem("Settings & Privacy");
        JMenuItem logoutItem = new JMenuItem("Log Out");

        settingsItem.addActionListener(e -> {
            // TODO: Show settings
        });

        logoutItem.addActionListener(e -> homePage.logout());

        menu.add(profileItem);
        menu.addSeparator();
        menu.add(settingsItem);
        menu.add(logoutItem);

        menu.show(profileBtn, 0, profileBtn.getHeight());
    }

    private Icon createProfileIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(FacebookGUI.FB_BLUE);
                g2.fillOval(x, y, 36, 36);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String initial = Main.current.getFirstname().substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (36 - fm.stringWidth(initial)) / 2;
                int textY = y + (36 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initial, textX, textY);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 36;
            }

            @Override
            public int getIconHeight() {
                return 36;
            }
        };
    }

    private void showMessengerDialog() {
        JDialog dialog = new JDialog(parent, "Messenger", false);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Chats");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(15, 15, 15, 15));

        // List friends
        JPanel friendsList = new JPanel();
        friendsList.setLayout(new BoxLayout(friendsList, BoxLayout.Y_AXIS));
        friendsList.setBackground(Color.WHITE);

        java.util.ArrayList<String> friends = com.facebook.Database
                .Load_Friends(Main.current.getCredentials().getUsername());
        for (String friendUsername : friends) {
            com.facebook.User friend = com.facebook.Database.LoadUser(friendUsername);
            if (friend != null) {
                JPanel friendItem = createFriendChatItem(friend, dialog);
                friendsList.add(friendItem);
            }
        }

        JScrollPane scrollPane = new JScrollPane(friendsList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private JPanel createFriendChatItem(com.facebook.User friend, JDialog parentDialog) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(Color.WHITE);
        item.setBorder(new EmptyBorder(10, 15, 10, 15));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Profile circle
        JLabel profileCircle = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(FacebookGUI.FB_BLUE);
                g2.fillOval(0, 0, 40, 40);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String initial = friend.getFirstname().substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                int x = (40 - fm.stringWidth(initial)) / 2;
                int y = (40 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initial, x, y);

                // Online indicator
                if (com.facebook.Database.Check_Online(friend.getCredentials().getUsername())) {
                    g2.setColor(new Color(66, 183, 42));
                    g2.fillOval(28, 28, 12, 12);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawOval(28, 28, 12, 12);
                }

                g2.dispose();
            }
        };
        profileCircle.setPreferredSize(new Dimension(40, 40));

        JLabel nameLabel = new JLabel(friend.getFullName());
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        item.add(profileCircle, BorderLayout.WEST);
        item.add(nameLabel, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parentDialog.dispose();
                homePage.openChatWithFriend(friend.getCredentials().getUsername());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(240, 242, 245));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(Color.WHITE);
            }
        });

        return item;
    }

    private void showNotificationsDialog() {
        JDialog dialog = new JDialog(parent, "Notifications", false);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Notifications");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel notifList = new JPanel();
        notifList.setLayout(new BoxLayout(notifList, BoxLayout.Y_AXIS));
        notifList.setBackground(Color.WHITE);

        // Load notifications
        java.util.ArrayList<com.facebook.Notification> notifications = com.facebook.Database
                .Load_Unread_Notification();

        if (notifications.isEmpty()) {
            JLabel noNotif = new JLabel("No new notifications");
            noNotif.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            noNotif.setForeground(FacebookGUI.FB_TEXT_SECONDARY);
            noNotif.setBorder(new EmptyBorder(20, 15, 20, 15));
            notifList.add(noNotif);
        } else {
            for (com.facebook.Notification notif : notifications) {
                JPanel notifItem = createNotificationItem(notif);
                notifList.add(notifItem);
            }
        }

        JScrollPane scrollPane = new JScrollPane(notifList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private JPanel createNotificationItem(com.facebook.Notification notif) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(new Color(240, 242, 245));
        item.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel iconLabel = new JLabel(getNotificationIcon(notif.getType()));
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));

        JLabel messageLabel = new JLabel("<html>" + notif.getMessage() + "</html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        item.add(iconLabel, BorderLayout.WEST);
        item.add(messageLabel, BorderLayout.CENTER);

        return item;
    }

    private String getNotificationIcon(com.facebook.Notification.Type type) {
        return switch (type) {
            case LIKE -> "❤️";
            case COMMENT -> "💬";
            case TAG -> "🏷️";
            case MESSAGE -> "✉️";
            case GAME -> "🎮";
            default -> "🔔";
        };
    }

    public void updateMessageCount(int count) {
        this.messageCount = count;
        messagesBtn.setBadgeCount(count);
    }

    public void updateNotificationCount(int count) {
        this.notificationCount = count;
        notificationsBtn.setBadgeCount(count);
    }
}
