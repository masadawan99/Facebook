package com.facebook.gui;

import com.facebook.Database;
import com.facebook.Main;
import com.facebook.User;
import com.facebook.gui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Right Sidebar showing friends list and group chats
 * Features: Clickable friends to open chat, online indicators
 */
public class RightSidebar extends JPanel {

    private FacebookGUI parent;
    private HomePage homePage;
    private JPanel friendsListPanel;
    private JPanel groupChatsPanel;

    public RightSidebar(FacebookGUI parent, HomePage homePage) {
        this.parent = parent;
        this.homePage = homePage;
        setBackground(Color.WHITE);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        initComponents();
    }

    private void initComponents() {
        // Header
        JLabel contactsLabel = new JLabel("Contacts");
        contactsLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        contactsLabel.setForeground(FacebookGUI.FB_TEXT_SECONDARY);
        contactsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(contactsLabel);
        add(Box.createVerticalStrut(15));

        // Friends list
        friendsListPanel = new JPanel();
        friendsListPanel.setLayout(new BoxLayout(friendsListPanel, BoxLayout.Y_AXIS));
        friendsListPanel.setBackground(Color.WHITE);
        friendsListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        loadFriendsList();

        add(friendsListPanel);
        add(Box.createVerticalStrut(20));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(219, 223, 231));
        add(sep);
        add(Box.createVerticalStrut(15));

        // Group chats header
        JLabel groupChatsLabel = new JLabel("Group chats");
        groupChatsLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        groupChatsLabel.setForeground(FacebookGUI.FB_TEXT_SECONDARY);
        groupChatsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(groupChatsLabel);
        add(Box.createVerticalStrut(15));

        // Group chats list
        groupChatsPanel = new JPanel();
        groupChatsPanel.setLayout(new BoxLayout(groupChatsPanel, BoxLayout.Y_AXIS));
        groupChatsPanel.setBackground(Color.WHITE);
        groupChatsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        loadGroupChats();

        add(groupChatsPanel);

        // Create group chat button
        add(Box.createVerticalStrut(10));
        JPanel createGroupPanel = createCreateGroupChatButton();
        add(createGroupPanel);

        add(Box.createVerticalGlue());
    }

    private void loadFriendsList() {
        friendsListPanel.removeAll();

        ArrayList<String> friends = Database.Load_Friends(Main.current.getCredentials().getUsername());

        if (friends.isEmpty()) {
            JLabel noFriends = new JLabel("No friends yet");
            noFriends.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            noFriends.setForeground(FacebookGUI.FB_TEXT_SECONDARY);
            friendsListPanel.add(noFriends);
        } else {
            for (String friendUsername : friends) {
                User friend = Database.LoadUser(friendUsername);
                if (friend != null) {
                    JPanel friendItem = createFriendItem(friend);
                    friendsListPanel.add(friendItem);
                }
            }
        }

        friendsListPanel.revalidate();
        friendsListPanel.repaint();
    }

    private JPanel createFriendItem(User friend) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(6, 8, 6, 8));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Profile circle with online indicator
        JLabel profileCircle = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                // Draw circle
                g2.setColor(FacebookGUI.FB_BLUE);
                g2.fillOval(0, 0, 36, 36);

                // Draw initial
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String initial = friend.getFirstname().substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                int x = (36 - fm.stringWidth(initial)) / 2;
                int y = (36 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initial, x, y);

                // Online indicator
                boolean isOnline = Database.Check_Online(friend.getCredentials().getUsername());
                if (isOnline) {
                    g2.setColor(new Color(66, 183, 42));
                    g2.fillOval(26, 26, 10, 10);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawOval(26, 26, 10, 10);
                }

                g2.dispose();
            }
        };
        profileCircle.setPreferredSize(new Dimension(36, 36));

        // Name label
        JLabel nameLabel = new JLabel(friend.getFullName());
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        nameLabel.setForeground(FacebookGUI.FB_TEXT_PRIMARY);

        panel.add(profileCircle, BorderLayout.WEST);
        panel.add(nameLabel, BorderLayout.CENTER);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(new Color(240, 242, 245));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                homePage.openChatWithFriend(friend.getCredentials().getUsername());
            }
        });

        return panel;
    }

    private void loadGroupChats() {
        groupChatsPanel.removeAll();

        // Load group chats from inbox
        ArrayList<com.facebook.Chat> chats = Database.LoadInbox();
        int groupCount = 0;

        for (com.facebook.Chat chat : chats) {
            if (chat instanceof com.facebook.Group_chat) {
                com.facebook.Group_chat gc = (com.facebook.Group_chat) chat;
                JPanel groupItem = createGroupChatItem(gc);
                groupChatsPanel.add(groupItem);
                groupCount++;
            }
        }

        if (groupCount == 0) {
            JLabel noGroups = new JLabel("No group chats");
            noGroups.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            noGroups.setForeground(FacebookGUI.FB_TEXT_SECONDARY);
            groupChatsPanel.add(noGroups);
        }

        groupChatsPanel.revalidate();
        groupChatsPanel.repaint();
    }

    private JPanel createGroupChatItem(com.facebook.Group_chat groupChat) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(6, 8, 6, 8));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Group icon (multiple circles)
        JLabel groupIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw overlapping circles
                g2.setColor(FacebookGUI.FB_BLUE);
                g2.fillOval(0, 6, 24, 24);
                g2.setColor(FacebookGUI.FB_GREEN);
                g2.fillOval(12, 6, 24, 24);

                g2.dispose();
            }
        };
        groupIcon.setPreferredSize(new Dimension(36, 36));

        // Group name
        JLabel nameLabel = new JLabel(groupChat.getGroupName());
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        nameLabel.setForeground(FacebookGUI.FB_TEXT_PRIMARY);

        panel.add(groupIcon, BorderLayout.WEST);
        panel.add(nameLabel, BorderLayout.CENTER);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(new Color(240, 242, 245));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO: Open group chat
            }
        });

        return panel;
    }

    private JPanel createCreateGroupChatButton() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(6, 8, 6, 8));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel plusIcon = new JLabel("+");
        plusIcon.setFont(new Font("Segoe UI", Font.BOLD, 20));
        plusIcon.setForeground(FacebookGUI.FB_TEXT_SECONDARY);

        JLabel textLabel = new JLabel("Create group chat");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        textLabel.setForeground(FacebookGUI.FB_TEXT_SECONDARY);

        panel.add(plusIcon);
        panel.add(textLabel);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(new Color(240, 242, 245));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO: Create group chat dialog
            }
        });

        return panel;
    }

    public void refresh() {
        loadFriendsList();
        loadGroupChats();
    }
}
