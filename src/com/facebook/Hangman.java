package com.facebook;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public class Hangman extends Game implements Serializable {
    private static final long serialVersionUID = 1L;

    // Logic Variables
    private String word = "";
    private char[] letters;
    private boolean[] found;
    private int tries = 6;
    private String filename;
    private String[] players = new String[2];
    private boolean isVsComputer = false;

    // Animation
    private float drawProgress = 0f;
    private int animatingLimbIndex = -1; // Which limb is currently drawing? 6=none, 5=head, etc.

    // GUI
    private transient JFrame frame;
    private transient HangmanPanel gamePanel;
    private transient Timer onlineTimer;
    private transient Timer animTimer;
    private transient JPanel keyboardPanel;
    private transient JButton[] keyButtons;

    // Colors
    private final Color CLR_BG = Color.decode("#121212");
    private final Color CLR_PANEL = Color.decode("#1E1E1E");
    private final Color CLR_BTN = Color.decode("#252525");
    private final Color CLR_BTN_HOVER = Color.decode("#303030");
    private final Color CLR_ACCENT = Color.decode("#FF4500");
    private final Color CLR_ACCENT_GLOW = new Color(255, 69, 0, 100);
    private final Color CLR_TEXT = Color.decode("#E0E0E0");

    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 40);
    private final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 22);
    private final Font FONT_UI = new Font("Segoe UI", Font.PLAIN, 16);
    private final Font FONT_BTN = new Font("Segoe UI", Font.BOLD, 14);

    // Word Lists
    private final String[] EASY_WORDS = { "JAVA", "CODE", "GAME", "BYTE", "LOOP", "BUG", "WEB", "APP", "GUI", "KEY" };
    private final String[] MEDIUM_WORDS = { "PYTHON", "SERVER", "CLIENT", "SCRIPT", "OBJECT", "STRING", "MEMORY",
            "SOCKET" };
    private final String[] HARD_WORDS = { "ALGORITHM", "FRAMEWORK", "INTERFACE", "EXCEPTION", "DATABASE", "ENCRYPTION",
            "POLYMORPHISM" };

    public Hangman() {
        super("HANG MAN", "Dual Player");
    }

    @Override
    public void Game_launch() {
        SwingUtilities.invokeLater(this::showMainMenu);
    }

    // ==========================================
    // UI Helpers
    // ==========================================
    private void setupFrame(String title) {
        if (frame != null)
            frame.dispose();
        frame = new JFrame(title);
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.getContentPane().setBackground(CLR_BG);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (onlineTimer != null && onlineTimer.isRunning())
                    onlineTimer.stop();
                if (animTimer != null && animTimer.isRunning())
                    animTimer.stop();
            }
        });
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());

                Shape shape = new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, 15, 15);
                g2.fill(shape);
                g2.setColor(CLR_TEXT);
                g2.setStroke(new BasicStroke(3f));
                g2.draw(shape);
                super.paintComponent(g2);
                g2.dispose();

            }

            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    g2.setColor(Color.RED);
                } else {
                    g2.setColor(CLR_BG);
                }

                Shape shape = new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, 15, 15);
                g2.fill(shape);

                // Border
                g2.setColor(CLR_ACCENT);
                g2.setStroke(new BasicStroke(3f));
                g2.draw(shape);

                // Text
                if (getModel().isRollover()) {
                    g2.setColor(Color.BLACK);
                } else {
                    g2.setColor(Color.LIGHT_GRAY);
                }
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
        btn.setBackground(CLR_BTN);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBorder(null);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(250, 50));
        btn.setMaximumSize(new Dimension(250, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(CLR_BTN_HOVER);
                btn.setBorder(BorderFactory.createEmptyBorder());
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(CLR_BTN);
            }
        });
        return btn;
    }

    // ==========================================
    // Menu
    // ==========================================
    private void showMainMenu() {
        setupFrame("Hangman - Premium");
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CLR_BG);
        content.setBorder(new EmptyBorder(50, 50, 50, 50));

        JLabel title = new JLabel("HANGMAN");
        title.setFont(FONT_TITLE);
        title.setForeground(CLR_ACCENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(60));

        addButtonToMenu(content, "VS PLAYER (LOCAL)", e -> showSetWordDialog());
        addButtonToMenu(content, "VS COMPUTER", e -> showDifficultyDialog());
        addButtonToMenu(content, "EXIT", e -> frame.dispose());

        frame.add(content);
        frame.setVisible(true);
    }

    private void addButtonToMenu(JPanel p, String text, ActionListener al) {
        JButton btn = createStyledButton(text);
        btn.addActionListener(al);
        p.add(btn);
        p.add(Box.createVerticalStrut(20));
    }

    // ==========================================
    // Game Screen
    // ==========================================
    private void showGameScreen(String title, boolean isOnline) {
        frame.getContentPane().removeAll();
        frame.setTitle(title);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(CLR_BG);

        // Drawing Area
        gamePanel = new HangmanPanel();
        main.add(gamePanel, BorderLayout.CENTER);

        // Keyboard Area
        keyboardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Fluid layout
        keyboardPanel.setBackground(CLR_BG);
        keyboardPanel.setBorder(new EmptyBorder(30, 50, 30, 50));
        keyboardPanel.setPreferredSize(new Dimension(900, 200));

        keyButtons = new JButton[26];
        for (int i = 0; i < 26; i++) {
            char c = (char) ('A' + i);
            JButton kBtn = new JButton(String.valueOf(c)) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (!isEnabled()) {
                        g2.setColor(new Color(30, 30, 30)); // Disabled
                    } else if (getModel().isRollover()) {
                        g2.setColor(CLR_ACCENT);
                    } else {
                        g2.setColor(CLR_PANEL);
                    }
                    g2.fillOval(0, 0, getWidth(), getHeight());

                    if (isEnabled())
                        g2.setColor(CLR_TEXT);
                    else
                        g2.setColor(Color.GRAY);

                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(getText())) / 2;
                    int y = (getHeight() + fm.getAscent()) / 2 - 3;
                    g2.drawString(getText(), x, y);
                    g2.dispose();
                }
            };
            kBtn.setPreferredSize(new Dimension(50, 50));
            kBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            kBtn.setFocusPainted(false);
            kBtn.setBorderPainted(false);
            kBtn.setContentAreaFilled(false);
            kBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            kBtn.addActionListener(e -> {
                handleGuess(c, kBtn);
            });
            keyButtons[i] = kBtn;
            keyboardPanel.add(kBtn);
        }
        main.add(keyboardPanel, BorderLayout.SOUTH);

        // Header Panel with centered title
        JPanel topObj = new JPanel(new BorderLayout());
        topObj.setBackground(CLR_BG);

        // Wrapper panel to truly center the title
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        JLabel turnLabel = new JLabel(title, SwingConstants.CENTER);
        turnLabel.setFont(FONT_HEADER);
        turnLabel.setForeground(CLR_ACCENT);
        centerWrapper.add(turnLabel);

        topObj.add(centerWrapper, BorderLayout.CENTER);

        // Forfeit button on the right
        JPanel menuP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        menuP.setOpaque(false);
        JButton btnForfeit = createStyledButton("FORFEIT");
        btnForfeit.setPreferredSize(new Dimension(100, 40));
        btnForfeit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnForfeit.addActionListener(e -> handleForfeit());
        menuP.add(btnForfeit);

        topObj.add(menuP, BorderLayout.EAST);

        main.add(topObj, BorderLayout.NORTH);

        frame.add(main);

        // Keyboard Support
        main.setFocusable(true);
        main.requestFocusInWindow();
        main.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char c = Character.toUpperCase(e.getKeyChar());
                if (c >= 'A' && c <= 'Z') {
                    int idx = c - 'A';
                    if (idx >= 0 && idx < 26) {
                        JButton btn = keyButtons[idx];
                        if (btn.isEnabled()) {
                            btn.doClick();
                        }
                    }
                }
            }
        });

        frame.revalidate();
        frame.repaint();
    }

    private void handleForfeit() {
        if (onlineTimer != null && onlineTimer.isRunning())
            onlineTimer.stop();
        if (animTimer != null && animTimer.isRunning())
            animTimer.stop();
        String message = isVsComputer ? "Computer Won! Word: " + word : "You Lost! Word: " + word;
        showEndDialog(message, false);
    }

    private class HangmanPanel extends JPanel {
        public HangmanPanel() {
            setBackground(CLR_BG);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int centerX = w / 2;
            int startY = 100;

            // Minimalist Gallows (Just a line from top)
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(centerX, 0, centerX, startY);

            // Draw Man (Neon Style)
            g2.setColor(CLR_ACCENT);
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Parts Logic:
            // 6=None, 5=Head, 4=Body, 3=L Arm, 2=R Arm, 1=L Leg, 0=R Leg

            // Draw FULLY established parts (those with index > tries)
            if (tries < 6)
                drawHead(g2, centerX, startY, 1.0f);
            if (tries < 5)
                drawBody(g2, centerX, startY, 1.0f);
            if (tries < 4)
                drawLArm(g2, centerX, startY, 1.0f);
            if (tries < 3)
                drawRArm(g2, centerX, startY, 1.0f);
            if (tries < 2)
                drawLLeg(g2, centerX, startY, 1.0f);
            if (tries < 1)
                drawRLeg(g2, centerX, startY, 1.0f);

            // Draw ANIMATING part
            if (animatingLimbIndex != -1) {
                if (animatingLimbIndex == 5)
                    drawHead(g2, centerX, startY, drawProgress);
                else if (animatingLimbIndex == 4)
                    drawBody(g2, centerX, startY, drawProgress);
                else if (animatingLimbIndex == 3)
                    drawLArm(g2, centerX, startY, drawProgress);
                else if (animatingLimbIndex == 2)
                    drawRArm(g2, centerX, startY, drawProgress);
                else if (animatingLimbIndex == 1)
                    drawLLeg(g2, centerX, startY, drawProgress);
                else if (animatingLimbIndex == 0)
                    drawRLeg(g2, centerX, startY, drawProgress);
            }

            // Draw Word
            g2.setFont(new Font("Monospaced", Font.BOLD, 42));
            g2.setColor(CLR_TEXT);

            if (letters != null) {
                StringBuilder display = new StringBuilder();
                for (int i = 0; i < letters.length; i++) {
                    if (found[i])
                        display.append(letters[i]).append(" ");
                    else
                        display.append("_ ");
                }
                String s = display.toString();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(s, (w - fm.stringWidth(s)) / 2, h - 80);
            }

            // Info
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            g2.setColor(Color.GRAY);
            g2.drawString("Mistakes Allowed: " + tries, 20, 30);
        }

        private void drawHead(Graphics2D g2, int cx, int sy, float p) {
            int r = 25; // Radius
            // Draw arc based on progress (0 to 360)
            int angle = (int) (360 * p);
            g2.drawArc(cx - r, sy, r * 2, r * 2, 90, angle);
            // Glow
            g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(CLR_ACCENT_GLOW);
            g2.drawArc(cx - r, sy, r * 2, r * 2, 90, angle);
            g2.setStroke(new BasicStroke(3));
            g2.setColor(CLR_ACCENT);
        }

        private void drawBody(Graphics2D g2, int cx, int sy, float p) {
            int startY = sy + 50;
            int endY = startY + (int) (100 * p);
            g2.drawLine(cx, startY, cx, endY);
            // Glow
            g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(CLR_ACCENT_GLOW);
            g2.drawLine(cx, startY, cx, endY);
            g2.setStroke(new BasicStroke(3));
            g2.setColor(CLR_ACCENT);
        }

        private void drawLArm(Graphics2D g2, int cx, int sy, float p) {
            int bodyY = sy + 70;
            int endX = cx - (int) (50 * p);
            int endY = bodyY + (int) (30 * p);
            g2.drawLine(cx, bodyY, endX, endY);
        }

        private void drawRArm(Graphics2D g2, int cx, int sy, float p) {
            int bodyY = sy + 70;
            int endX = cx + (int) (50 * p);
            int endY = bodyY + (int) (30 * p);
            g2.drawLine(cx, bodyY, endX, endY);
        }

        private void drawLLeg(Graphics2D g2, int cx, int sy, float p) {
            int bodyEnd = sy + 150;
            int endX = cx - (int) (50 * p);
            int endY = bodyEnd + (int) (60 * p);
            g2.drawLine(cx, bodyEnd, endX, endY);
        }

        private void drawRLeg(Graphics2D g2, int cx, int sy, float p) {
            int bodyEnd = sy + 150;
            int endX = cx + (int) (50 * p);
            int endY = bodyEnd + (int) (60 * p);
            g2.drawLine(cx, bodyEnd, endX, endY);
        }
    }

    // ==========================================
    // Logic - Offline
    // ==========================================

    private void showDifficultyDialog() {
        JDialog d = new JDialog(frame, "Select Difficulty", true);
        d.setUndecorated(true);
        d.setSize(400, 350);
        d.setLocationRelativeTo(frame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CLR_PANEL);
        p.setBorder(BorderFactory.createLineBorder(CLR_ACCENT, 2));

        JLabel l = new JLabel("DIFFICULTY");
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(CLR_ACCENT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnEasy = createStyledButton("EASY");
        JButton btnMed = createStyledButton("MEDIUM");
        JButton btnHard = createStyledButton("HARD");
        JButton btnCancel = createStyledButton("CANCEL");

        java.util.Random rnd = new java.util.Random();

        btnEasy.addActionListener(e -> {
            d.dispose();
            startOfflineGame(EASY_WORDS[rnd.nextInt(EASY_WORDS.length)], true);
        });
        btnMed.addActionListener(e -> {
            d.dispose();
            startOfflineGame(MEDIUM_WORDS[rnd.nextInt(MEDIUM_WORDS.length)], true);
        });
        btnHard.addActionListener(e -> {
            d.dispose();
            startOfflineGame(HARD_WORDS[rnd.nextInt(HARD_WORDS.length)], true);
        });
        btnCancel.addActionListener(e -> {
            d.dispose();
            showMainMenu();
        });

        p.add(Box.createVerticalStrut(30));
        p.add(l);
        p.add(Box.createVerticalStrut(30));
        p.add(btnEasy);
        p.add(Box.createVerticalStrut(15));
        p.add(btnMed);
        p.add(Box.createVerticalStrut(15));
        p.add(btnHard);
        p.add(Box.createVerticalStrut(15));
        p.add(btnCancel);

        d.add(p);
        d.setVisible(true);
    }

    private void showSetWordDialog() {
        JDialog d = new JDialog(frame, "Set Word", true);
        d.setSize(400, 300);
        d.setUndecorated(true);
        d.setLocationRelativeTo(frame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CLR_PANEL);
        p.setBorder(BorderFactory.createLineBorder(CLR_ACCENT, 2));

        JLabel l = new JLabel("SECRET WORD");
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(CLR_ACCENT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField tf = new JPasswordField(20);
        tf.setMaximumSize(new Dimension(300, 40));

        JButton btnStart = createStyledButton("START");
        JButton btnCancel = createStyledButton("CANCEL");

        btnStart.addActionListener(e -> {
            String w = new String(tf.getPassword()).toUpperCase();
            if (w.isEmpty() || !w.matches("[A-Z]+")) {
                JOptionPane.showMessageDialog(d, "Invalid Word (A-Z only)", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            d.dispose();
            startOfflineGame(w, false);
        });
        btnCancel.addActionListener(e -> {
            d.dispose();
            showMainMenu();
        });

        p.add(Box.createVerticalStrut(30));
        p.add(l);
        p.add(Box.createVerticalStrut(20));
        p.add(tf);
        p.add(Box.createVerticalStrut(30));
        p.add(btnStart);
        p.add(Box.createVerticalStrut(15));
        p.add(btnCancel);

        d.add(p);
        d.setVisible(true);
    }

    private void startOfflineGame(String w, boolean vsCpu) {
        this.word = w;
        this.isVsComputer = vsCpu;
        initData(w.length());
        this.letters = w.toCharArray();
        showGameScreen(vsCpu ? "Hangman - Vs Computer" : "Hangman - PvP", false);
    }

    private void initData(int len) {
        letters = new char[len];
        found = new boolean[len];
        tries = 6;
        for (int i = 0; i < len; i++)
            found[i] = false;
        animatingLimbIndex = -1;
        drawProgress = 0f;
    }

    private void handleGuess(char c, JButton btn) {
        btn.setEnabled(false);

        boolean hit = false;
        boolean allFound = true;

        for (int i = 0; i < letters.length; i++) {
            if (letters[i] == c) {
                found[i] = true;
                hit = true;
            }
            if (!found[i])
                allFound = false;
        }

        if (!hit) {
            tries--;
            flashBackground(Color.decode("#330000"));
            // Start Animation
            startLimbAnimation(tries);
        } else {
            btn.setBackground(Color.decode("#006400"));
            gamePanel.repaint();
        }

        if (allFound) {
            Timer t = new Timer(500, e -> showEndDialog("YOU WON! Word: " + word, true));
            t.setRepeats(false);
            t.start();
        } else if (tries <= 0) {
            Timer t = new Timer(800, e -> showEndDialog("GAME OVER! Word: " + word, false));
            t.setRepeats(false);
            t.start();
        }
    }

    private void startLimbAnimation(int triesLeft) {
        animatingLimbIndex = triesLeft; // 5 = Head, 4 = Body... 0 = R Leg
        drawProgress = 0f;
        if (animTimer != null && animTimer.isRunning())
            animTimer.stop();

        animTimer = new Timer(15, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawProgress += 0.05f;
                if (drawProgress >= 1.0f) {
                    drawProgress = 1.0f;
                    gamePanel.repaint();
                    ((Timer) e.getSource()).stop();
                }
                gamePanel.repaint();
            }
        });
        animTimer.start();
    }

    private void flashBackground(Color c) {
        gamePanel.setBackground(c);
        Timer t = new Timer(150, e -> {
            gamePanel.setBackground(CLR_BG);
            ((Timer) e.getSource()).stop();
        });
        t.start();
    }

    private void showEndDialog(String msg, boolean win) {
        JDialog d = new JDialog(frame, "Result", true);
        d.setUndecorated(true);
        d.setSize(400, 250);
        d.setLocationRelativeTo(frame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CLR_PANEL);
        p.setBorder(BorderFactory.createLineBorder(win ? Color.GREEN : Color.RED, 2));

        JLabel l = new JLabel("<html><center>" + msg + "</center></html>", SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l.setForeground(CLR_TEXT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);

        JButton btnPlayAgain = createStyledButton("PLAY AGAIN");
        btnPlayAgain.setPreferredSize(new Dimension(140, 45));
        btnPlayAgain.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPlayAgain.addActionListener(e -> {
            d.dispose();
            if (isVsComputer)
                showDifficultyDialog();
            else
                showSetWordDialog();
        });

        JButton btnMenu = createStyledButton("MENU");
        btnMenu.setPreferredSize(new Dimension(140, 45));
        btnMenu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnMenu.addActionListener(e -> {
            d.dispose();
            showMainMenu();
        });

        p.add(Box.createVerticalStrut(40));
        p.add(l);
        p.add(Box.createVerticalStrut(30));

        btnPanel.add(btnPlayAgain);
        btnPanel.add(btnMenu);
        p.add(btnPanel);

        d.add(p);
        d.setVisible(true);
    }
}
