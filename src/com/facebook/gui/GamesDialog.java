package com.facebook.gui;

import com.facebook.Game;
import com.facebook.Main;
import com.facebook.TicTacToe;
import com.facebook.Hangman;
import com.facebook.SnakeGame;
import com.facebook.gui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Games Dialog showing available games from CLI
 * Features: TicTacToe, Hangman, Snake Game
 */
public class GamesDialog extends JDialog {

    private FacebookGUI parent;
    private ArrayList<Game> games;

    public GamesDialog(FacebookGUI parent) {
        super(parent, "Games", true);
        this.parent = parent;
        this.games = Main.Get_ALL_games();

        setSize(500, 400);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Title
        JLabel titleLabel = new JLabel("🎮 Play Games");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(FacebookGUI.FB_TEXT_PRIMARY);

        // Description
        JLabel descLabel = new JLabel("Choose a game to play with your friends!");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        descLabel.setForeground(FacebookGUI.FB_TEXT_SECONDARY);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(descLabel);

        // Games list
        JPanel gamesPanel = new JPanel();
        gamesPanel.setLayout(new BoxLayout(gamesPanel, BoxLayout.Y_AXIS));
        gamesPanel.setBackground(Color.WHITE);

        for (int i = 0; i < games.size(); i++) {
            Game game = games.get(i);
            JPanel gameCard = createGameCard(game);
            gamesPanel.add(gameCard);
            if (i < games.size() - 1) {
                gamesPanel.add(Box.createVerticalStrut(15));
            }
        }

        JScrollPane scrollPane = new JScrollPane(gamesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createGameCard(Game game) {
        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(new Color(248, 249, 250));
        card.setLayout(new BorderLayout(15, 0));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Game icon
        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, FacebookGUI.FB_BLUE,
                        70, 70, new Color(100, 150, 255));
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, 70, 70, 12, 12);

                // Draw game icon
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
                String icon = getGameIcon(game.getName());
                FontMetrics fm = g2.getFontMetrics();
                int x = (70 - fm.stringWidth(icon)) / 2;
                int y = (70 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(icon, x, y);

                g2.dispose();
            }
        };
        iconLabel.setPreferredSize(new Dimension(70, 70));

        // Game info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(game.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(FacebookGUI.FB_TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel(getGameDescription(game.getName()));
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(FacebookGUI.FB_TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel playersLabel = new JLabel("👥 Multiplayer • 🎯 Challenge friends");
        playersLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        playersLabel.setForeground(new Color(150, 150, 150));
        playersLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(descLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(playersLabel);

        // Play button
        AnimatedButton playBtn = new AnimatedButton("Play", FacebookGUI.FB_GREEN, FacebookGUI.FB_GREEN_HOVER);
        playBtn.setPreferredSize(new Dimension(100, 40));
        playBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        playBtn.addActionListener(e -> launchGame(game));

        card.add(iconLabel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(playBtn, BorderLayout.EAST);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            private Color originalColor = card.getBackground();

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(240, 245, 250));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(originalColor);
            }
        });

        return card;
    }

    private String getGameIcon(String gameName) {
        return switch (gameName) {
            case "TicTacToe" -> "⭕";
            case "Hangman" -> "💭";
            case "Snake" -> "🐍";
            default -> "🎮";
        };
    }

    private String getGameDescription(String gameName) {
        return switch (gameName) {
            case "TicTacToe" -> "Classic X and O game - First to 3 in a row wins!";
            case "Hangman" -> "Guess the word before hangman is complete!";
            case "Snake" -> "Control the snake and eat food to grow!";
            default -> "Have fun playing!";
        };
    }

    private void launchGame(Game game) {
        dispose();

        // Launch based on game name
        SwingUtilities.invokeLater(() -> {
            try {
                String gameName = game.getName();

                JOptionPane.showMessageDialog(parent,
                        "Launching " + gameName + "...\nThe game will open in console mode.",
                        "Game Launch",
                        JOptionPane.INFORMATION_MESSAGE);

                // Launch the game in a separate thread
                new Thread(() -> {
                    try {
                        if (gameName.equals("TicTacToe")) {
                            TicTacToe ticTacToe = new TicTacToe();
                            ticTacToe.Start_game();
                        } else if (gameName.equals("Hangman")) {
                            Hangman hangman = new Hangman();
                            hangman.Start_game();
                        } else if (gameName.equals("Snake")) {
                            SnakeGame snake = new SnakeGame();
                            snake.Start_game();
                        }
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent,
                                "Error launching game: " + e.getMessage(),
                                "Game Error",
                                JOptionPane.ERROR_MESSAGE));
                    }
                }).start();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent,
                        "Failed to launch game: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
