package com.facebook;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class SnakeGame extends Game {
    public SnakeGame() {
        super("SnakeGame", "Single Player");
    }

    @Override
    public void Game_launch() {
        SwingUtilities.invokeLater(() -> new GameFrame());
    }
}

class GameFrame extends JFrame {
    GamePanel panel;

    GameFrame() {
        this.setTitle("Snake Game - Premium");
        this.setUndecorated(true); // Minimalistic
        panel = new GamePanel(this);
        this.setContentPane(panel);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (panel.timer != null)
                    panel.timer.stop();
                dispose();
            }
        });

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}

class GamePanel extends JPanel implements ActionListener {

    // Dimensions
    static final int SCREEN_WIDTH = 800;
    static final int SCREEN_HEIGHT = 700;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;

    // Game Logic
    int delay = 80;
    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];
    int bodyParts = 5;
    int applesEaten;
    int appleX;
    int appleY;
    char direction = 'R';
    float appleScale = 1.0f;
    boolean appleGrowing = true;

    // Enhanced Logic
    int scoreMultiplier = 1;
    int highScore = 0;

    // Logic States
    enum State {
        MENU, RUNNING, PAUSED
    }

    State currentState = State.MENU;
    boolean gameStartedOnce = false;

    Timer timer;
    Random random;

    // UI Helpers
    private JFrame parentFrame;
    private JButton btnPause;

    // Menu Components
    private JPanel menuPanel;

    // Colors
    private final Color CLR_BG = Color.decode("#121212");
    private final Color CLR_PANEL = Color.decode("#1E1E1E");
    private final Color CLR_ACCENT = Color.decode("#FF4500"); // OrangeRed
    private final Color CLR_ACCENT_SEC = Color.decode("#FFA500"); // Orange
    private final Color CLR_TEXT = Color.decode("#E0E0E0");
    private final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 48);
    private final Font FONT_BTN = new Font("Segoe UI", Font.BOLD, 18);

    GamePanel(JFrame parent) {
        this.parentFrame = parent;
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(CLR_BG);
        this.setFocusable(true);
        this.setLayout(null);
        this.addKeyListener(new MyKeyAdapter());

        setupUI();
        showMenu();

        timer = new Timer(delay, this);
    }

    private void setupUI() {
        // Persistent Pause Button
        btnPause = new JButton("II") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnPause.setBounds(SCREEN_WIDTH - 60, 20, 40, 40);
        btnPause.setBackground(CLR_PANEL);
        btnPause.setForeground(CLR_TEXT);
        btnPause.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnPause.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPause.setBorderPainted(false);
        btnPause.setFocusPainted(false);
        btnPause.setVisible(false);
        btnPause.addActionListener(e -> pauseGame());

        this.add(btnPause);

        // Menu Panel
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(CLR_BG);
        menuPanel.setBounds(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        JLabel title = new JLabel("SNAKE GAME");
        title.setFont(FONT_HEADER);
        title.setForeground(CLR_ACCENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        menuPanel.add(Box.createVerticalGlue());
        menuPanel.add(title);
        menuPanel.add(Box.createVerticalStrut(50));

        addMenuButton("NEW GAME", e -> showDifficultyDialog());
        addMenuButton("CONTINUE", e -> continueGame());
        addMenuButton("SCOREBOARD", e -> showFriendScoreboard());
        addMenuButton("EXIT", e -> parentFrame.dispose());

        menuPanel.add(Box.createVerticalGlue());

        this.add(menuPanel);
    }

    private void addMenuButton(String text, ActionListener al) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(CLR_ACCENT);
                } else {
                    g2.setColor(CLR_PANEL);
                }

                Shape shape = new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, 15, 15);
                g2.fill(shape);

                g2.setColor(CLR_TEXT);
                g2.setStroke(new BasicStroke(3f));
                g2.draw(shape);

                g2.setColor(CLR_TEXT);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 4;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(CLR_TEXT);
        btn.setBackground(CLR_PANEL);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(300, 60));
        btn.setPreferredSize(new Dimension(300, 60));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(al);

        // Hide Continue if game not started
        if (text.equals("CONTINUE")) {
            btn.setVisible(false); // Initially hidden
            btn.setName("BTN_CONTINUE");
        }

        menuPanel.add(btn);
        menuPanel.add(Box.createVerticalStrut(20));
    }

    private void showMenu() {
        currentState = State.MENU;
        menuPanel.setVisible(true);
        btnPause.setVisible(false);

        // Toggle Continue Button
        for (Component c : menuPanel.getComponents()) {
            if (c instanceof JButton && "BTN_CONTINUE".equals(c.getName())) {
                c.setVisible(gameStartedOnce);
            }
        }
        repaint();
    }

    private void startNewGameWithDifficulty(int speedDelay, int multiplier) {
        menuPanel.setVisible(false);
        bodyParts = 5;
        applesEaten = 0;
        direction = 'R';
        delay = speedDelay;
        scoreMultiplier = multiplier;
        highScore = Database.Load_HighScore();
        currentState = State.RUNNING;
        gameStartedOnce = true;
        btnPause.setVisible(true);

        // Center Snake
        int centerX = (SCREEN_WIDTH / UNIT_SIZE) / 2 * UNIT_SIZE;
        int centerY = (SCREEN_HEIGHT / UNIT_SIZE) / 2 * UNIT_SIZE;

        for (int i = 0; i < bodyParts; i++) {
            x[i] = centerX - (i * UNIT_SIZE);
            y[i] = centerY;
        }

        newApple();
        timer.setDelay(delay);
        timer.start();
        requestFocusInWindow();
    }

    private void continueGame() {
        if (!gameStartedOnce)
            return;
        menuPanel.setVisible(false);
        currentState = State.RUNNING;
        btnPause.setVisible(true);
        timer.start();
        requestFocusInWindow();
    }

    private void pauseGame() {
        currentState = State.PAUSED;
        timer.stop();
        showPauseDialog();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentState != State.MENU) {
            drawGame(g);
        }
    }

    public void drawGame(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw Score Header
        g2.setColor(CLR_PANEL);
        g2.fill(new RoundRectangle2D.Float(20, 20, 150, 70, 10, 10)); // Taller for 2 lines
        g2.setColor(CLR_TEXT);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2.drawString("Score: " + applesEaten, 35, 46);

        g2.setColor(Color.GRAY);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g2.drawString("Best: " + Math.max(applesEaten, highScore), 35, 68);

        // Draw Apple (Pulse)
        int aSize = (int) (UNIT_SIZE * appleScale);
        int offset = (UNIT_SIZE - aSize) / 2;
        g2.setColor(Color.RED);
        g2.fillOval(appleX + offset, appleY + offset, aSize, aSize);
        // Shine
        g2.setColor(new Color(255, 150, 150));
        g2.fillOval(appleX + offset + 4, appleY + offset + 4, aSize / 3, aSize / 3);

        // Draw Snake
        for (int i = 0; i < bodyParts; i++) {
            if (i == 0) { // Head
                g2.setColor(CLR_ACCENT);
                g2.fillRoundRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 12, 12);
            } else { // Body
                g2.setColor(CLR_ACCENT_SEC);
                g2.fillRoundRect(x[i] + 2, y[i] + 2, UNIT_SIZE - 4, UNIT_SIZE - 4, 8, 8);
            }
        }

        // Pause Overlay
        if (currentState == State.PAUSED) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }
    }

    public void newApple() {
        appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
        switch (direction) {
            case 'U' -> y[0] = y[0] - UNIT_SIZE;
            case 'D' -> y[0] = y[0] + UNIT_SIZE;
            case 'L' -> x[0] = x[0] - UNIT_SIZE;
            case 'R' -> x[0] = x[0] + UNIT_SIZE;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten += scoreMultiplier;
            newApple();
            if (applesEaten > highScore) {
                Database.Write_HighsSore(applesEaten);
                highScore = applesEaten;
            }
        }
    }

    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                gameOver();
            }
        }
        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            gameOver();
        }
    }

    private void gameOver() {
        timer.stop();
        gameStartedOnce = false;
        showGameOverDialog();
    }

    private void animate() {
        if (appleGrowing) {
            appleScale += 0.05f;
            if (appleScale >= 1.2f)
                appleGrowing = false;
        } else {
            appleScale -= 0.05f;
            if (appleScale <= 0.9f)
                appleGrowing = true;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentState == State.RUNNING) {
            move();
            checkApple();
            checkCollisions();
            animate();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (currentState != State.RUNNING)
                return;
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> {
                    if (direction != 'R')
                        direction = 'L';
                }
                case KeyEvent.VK_RIGHT -> {
                    if (direction != 'L')
                        direction = 'R';
                }
                case KeyEvent.VK_UP -> {
                    if (direction != 'D')
                        direction = 'U';
                }
                case KeyEvent.VK_DOWN -> {
                    if (direction != 'U')
                        direction = 'D';
                }
                case KeyEvent.VK_ESCAPE -> pauseGame();
                case KeyEvent.VK_P -> pauseGame();
            }
        }
    }

    // ==========================================
    // Custom Dialogs & Logic
    // ==========================================
    private JButton createDialogButton(String text) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());

                Shape shape = new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, 15, 15);
                g2.fill(shape);

                g2.setColor(CLR_TEXT);
                g2.setStroke(new BasicStroke(3f));
                g2.draw(shape);

                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 4;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(CLR_TEXT);
        btn.setBackground(CLR_PANEL);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBorder(null); // No Border
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 45));
        btn.setMaximumSize(new Dimension(180, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(CLR_ACCENT);
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(CLR_PANEL);
            }
        });
        return btn;
    }

    private void showDifficultyDialog() {
        JDialog d = new JDialog(parentFrame, "Select Difficulty", true);
        d.setUndecorated(true);
        d.setSize(400, 300);
        d.setLocationRelativeTo(parentFrame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CLR_PANEL);
        p.setBorder(new LineBorder(CLR_ACCENT, 2));

        JLabel l = new JLabel("SELECT DIFFICULTY");
        l.setFont(new Font("Segoe UI", Font.BOLD, 28));
        l.setForeground(CLR_ACCENT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnEasy = createDialogButton("EASY");
        JButton btnHard = createDialogButton("HARD");
        JButton btnBack = createDialogButton("BACK");

        btnEasy.addActionListener(e -> {
            d.dispose();
            startNewGameWithDifficulty(140, 1);
        });

        btnHard.addActionListener(e -> {
            d.dispose();
            startNewGameWithDifficulty(50, 2);
        });

        btnBack.addActionListener(e -> d.dispose());

        p.add(Box.createVerticalStrut(30));
        p.add(l);
        p.add(Box.createVerticalStrut(30));
        p.add(btnEasy);
        p.add(Box.createVerticalStrut(15));
        p.add(btnHard);
        p.add(Box.createVerticalStrut(15));
        p.add(btnBack);

        d.add(p);
        d.setVisible(true);
    }

    private void showGameOverDialog() {
        JDialog d = new JDialog(parentFrame, "Game Over", true);
        d.setUndecorated(true);
        d.setSize(400, 300);
        d.setLocationRelativeTo(parentFrame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CLR_BG); // Darker
        p.setBorder(new LineBorder(CLR_ACCENT, 2));

        JLabel l = new JLabel("GAME OVER");
        l.setFont(new Font("Segoe UI", Font.BOLD, 36));
        l.setForeground(Color.RED);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel s = new JLabel("Score: " + applesEaten);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        s.setForeground(CLR_TEXT);
        s.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnReplay = createDialogButton("RETRY");
        JButton btnMenu = createDialogButton("MENU");

        btnReplay.addActionListener(e -> {
            d.dispose();
            showDifficultyDialog();
        });

        btnMenu.addActionListener(e -> {
            d.dispose();
            showMenu();
        });

        p.add(Box.createVerticalStrut(30));
        p.add(l);
        p.add(Box.createVerticalStrut(10));
        p.add(s);
        p.add(Box.createVerticalStrut(30));
        p.add(btnReplay);
        p.add(Box.createVerticalStrut(15));
        p.add(btnMenu);
        p.add(Box.createVerticalGlue());

        d.add(p);
        d.setVisible(true);
    }

    private void showPauseDialog() {
        JDialog d = new JDialog(parentFrame, "Paused", true);
        d.setUndecorated(true);
        d.setSize(300, 250);
        d.setLocationRelativeTo(parentFrame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CLR_BG);
        p.setBorder(new LineBorder(CLR_ACCENT, 2));

        JLabel l = new JLabel("PAUSED");
        l.setFont(new Font("Segoe UI", Font.BOLD, 28));
        l.setForeground(CLR_TEXT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnResume = createDialogButton("RESUME");
        JButton btnMenu = createDialogButton("MENU");

        btnResume.addActionListener(e -> {
            d.dispose();
            currentState = State.RUNNING;
            timer.start();
        });

        btnMenu.addActionListener(e -> {
            d.dispose();
            showMenu();
        });

        p.add(Box.createVerticalStrut(30));
        p.add(l);
        p.add(Box.createVerticalStrut(30));
        p.add(btnResume);
        p.add(Box.createVerticalStrut(15));
        p.add(btnMenu);
        p.add(Box.createVerticalGlue());

        d.add(p);
        d.setVisible(true);
    }

    // ==========================================
    // Friend Scoreboard Logic
    // ==========================================
    private class Info {
        String name;
        int score;

        Info(String n, int s) {
            name = n;
            score = s;
        }
    }

    private void showFriendScoreboard() {
        JDialog d = new JDialog(parentFrame, "Scoreboard", true);
        d.setUndecorated(true);
        d.setSize(400, 500);
        d.setLocationRelativeTo(parentFrame);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(CLR_BG);
        main.setBorder(new LineBorder(CLR_ACCENT, 2));

        JLabel head = new JLabel("FRIEND HIGHSCORES", SwingConstants.CENTER);
        head.setFont(new Font("Segoe UI", Font.BOLD, 24));
        head.setForeground(CLR_ACCENT);
        head.setBorder(new EmptyBorder(20, 0, 20, 0));
        main.add(head, BorderLayout.NORTH);

        DefaultListModel<Info> model = new DefaultListModel<>();

        // 1. Get Current User Score
        String curr = Main.current.getCredentials().getUsername();
        int myScore = Database.Load_HighScore();
        model.addElement(new Info("You (" + Main.Get_Fullname(curr) + ")", myScore));

        // 2. Get Friends Scores
        ArrayList<String> friends = Database.Load_Friends(curr);
        for (String f : friends) {
            File fldr = new File(Database.Snakegamefldr, f);
            File file = new File(fldr, "HighScore");
            int s = 0;
            if (file.exists()) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                    s = (int) in.readObject();
                } catch (Exception e) {
                }
                model.addElement(new Info(Main.Get_Fullname(f), s));
            }
        }

        // Sort
        ArrayList<Info> list = new ArrayList<>();
        for (int i = 0; i < model.size(); i++)
            list.add(model.get(i));
        Collections.sort(list, (a, b) -> b.score - a.score);
        model.clear();
        for (Info i : list)
            model.addElement(i);

        JList<Info> jlist = new JList<>(model);
        jlist.setBackground(CLR_BG);
        jlist.setCellRenderer(new ListCellRenderer<Info>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Info> list, Info value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JPanel p = new JPanel(new BorderLayout());
                p.setBackground(isSelected ? CLR_PANEL : CLR_BG);
                p.setBorder(new EmptyBorder(10, 20, 10, 20));

                JLabel n = new JLabel((index + 1) + ". " + value.name);
                n.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                n.setForeground(CLR_TEXT);

                JLabel s = new JLabel(String.valueOf(value.score));
                s.setFont(new Font("Segoe UI", Font.BOLD, 16));
                s.setForeground(Color.ORANGE);

                p.add(n, BorderLayout.WEST);
                p.add(s, BorderLayout.EAST);
                return p;
            }
        });

        JScrollPane scroll = new JScrollPane(jlist);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        main.add(scroll, BorderLayout.CENTER);

        JButton btnClose = createDialogButton("CLOSE");
        btnClose.setBorder(null);
        btnClose.addActionListener(e -> d.dispose());
        JPanel btnP = new JPanel();
        btnP.setBackground(CLR_BG);
        btnP.add(btnClose);
        main.add(btnP, BorderLayout.SOUTH);

        d.add(main);
        d.setVisible(true);
    }
}
