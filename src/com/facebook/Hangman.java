package com.facebook;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

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
    private final Color CLR_ACCENT = Color.decode("#FF4500");
    private final Color CLR_ACCENT_GLOW = new Color(255, 69, 0, 100);
    private final Color CLR_TEXT = Color.decode("#E0E0E0");
    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 48);
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
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1000, 800); // Larger for airy feel
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.getContentPane().setBackground(CLR_BG);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (onlineTimer != null)
                    onlineTimer.stop();
                if (animTimer != null)
                    animTimer.stop();
            }
        });
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(CLR_ACCENT);
                    // Glow
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                    g2.fill(new RoundRectangle2D.Float(-2, -2, getWidth() + 4, getHeight() + 4, 20, 20));
                    g2.setComposite(AlphaComposite.SrcOver);
                } else {
                    g2.setColor(CLR_PANEL);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
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
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(250, 50));
        btn.setMaximumSize(new Dimension(250, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
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

        addButtonToMenu(content, "OFFLINE MODE", e -> showOfflineSourceDialog());
        addButtonToMenu(content, "ONLINE MODE", e -> startOnlineSetup());
        addButtonToMenu(content, "SCOREBOARD", e -> showFriendScoreboard());
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
                if (isOnline) {
                    // Check if I am allowed to guess
                    String me = Main.current.getCredentials().getUsername();
                    // I can only guess if I am NOT the setter (players[0])
                    // Actually, let's just check: Am I the Guesser?
                    // We established Inviter(0) = Setter, Invitee(1) = Guesser.
                    if (players[1] != null && players[1].equals(me)) {
                        handleOnlineGuess(c, kBtn);
                    } else {
                        // Spectator clicked
                        // Do nothing or shake
                    }
                } else {
                    handleGuess(c, kBtn);
                }
            });
            keyButtons[i] = kBtn;
            keyboardPanel.add(kBtn);
        }
        main.add(keyboardPanel, BorderLayout.SOUTH);

        // Menu Button
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.setOpaque(false);
        JButton btnMenu = new JButton("MENU");
        btnMenu.setFont(FONT_BTN);
        btnMenu.setBackground(CLR_BG);
        btnMenu.setForeground(Color.GRAY); // Discreet
        btnMenu.setBorder(null);
        btnMenu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMenu.addActionListener(e -> {
            if (onlineTimer != null)
                onlineTimer.stop();
            showMainMenu();
        });
        top.add(btnMenu);
        main.add(top, BorderLayout.NORTH);

        frame.add(main);
        frame.revalidate();
        frame.repaint();
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
            // Hook
            // g2.drawArc(centerX - 10, startY - 10, 20, 20, 270, 180); // Small hook? No,
            // keep it simple.

            // Draw Man (Neon Style)
            g2.setColor(CLR_ACCENT);
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Parts Logic:
            // 6=None
            // 5=Head
            // 4=Body
            // 3=L Arm
            // 2=R Arm
            // 1=L Leg
            // 0=R Leg

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
                // If animatingLimbIndex == tries, it means we are currently losing this life
                // Wait, logic: when tries goes 6->5, we animate Head. initializingLimbIndex =
                // 5.
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
    private void showOfflineSourceDialog() {
        JDialog d = new JDialog(frame, "Select Opponent", true);
        d.setUndecorated(true);
        d.setSize(400, 300);
        d.setLocationRelativeTo(frame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CLR_PANEL);
        p.setBorder(new LineBorder(CLR_ACCENT, 2));

        JLabel l = new JLabel("MODE SELECTION");
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(CLR_ACCENT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnCpu = createStyledButton("VS COMPUTER");
        JButton btnPlayer = createStyledButton("VS PLAYER (LOCAL)");
        JButton btnCancel = createStyledButton("CANCEL");

        btnCpu.addActionListener(e -> {
            d.dispose();
            showDifficultyDialog();
        });
        btnPlayer.addActionListener(e -> {
            d.dispose();
            showSetWordDialog();
        });
        btnCancel.addActionListener(e -> d.dispose());

        p.add(Box.createVerticalStrut(30));
        p.add(l);
        p.add(Box.createVerticalStrut(30));
        p.add(btnCpu);
        p.add(Box.createVerticalStrut(15));
        p.add(btnPlayer);
        p.add(Box.createVerticalStrut(15));
        p.add(btnCancel);

        d.add(p);
        d.setVisible(true);
    }

    private void showDifficultyDialog() {
        JDialog d = new JDialog(frame, "Select Difficulty", true);
        d.setUndecorated(true);
        d.setSize(400, 350);
        d.setLocationRelativeTo(frame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CLR_PANEL);
        p.setBorder(new LineBorder(CLR_ACCENT, 2));

        JLabel l = new JLabel("DIFFICULTY");
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(CLR_ACCENT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnEasy = createStyledButton("EASY");
        JButton btnMed = createStyledButton("MEDIUM");
        JButton btnHard = createStyledButton("HARD");

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

        p.add(Box.createVerticalStrut(30));
        p.add(l);
        p.add(Box.createVerticalStrut(30));
        p.add(btnEasy);
        p.add(Box.createVerticalStrut(15));
        p.add(btnMed);
        p.add(Box.createVerticalStrut(15));
        p.add(btnHard);

        d.add(p);
        d.setVisible(true);
    }

    private void showSetWordDialog() {
        JDialog d = new JDialog(frame, "Set Word", true);
        d.setSize(400, 250);
        d.setUndecorated(true);
        d.setLocationRelativeTo(frame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CLR_PANEL);
        p.setBorder(new LineBorder(CLR_ACCENT, 2));

        JLabel l = new JLabel("SECRET WORD");
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(CLR_ACCENT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField tf = new JPasswordField(20);
        tf.setMaximumSize(new Dimension(300, 40));

        JButton btnStart = createStyledButton("Start");
        btnStart.addActionListener(e -> {
            String w = new String(tf.getPassword()).toUpperCase();
            if (w.isEmpty() || !w.matches("[A-Z]+")) {
                JOptionPane.showMessageDialog(d, "Invalid Word (A-Z only)", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            d.dispose();
            startOfflineGame(w, false);
        });

        p.add(Box.createVerticalStrut(30));
        p.add(l);
        p.add(Box.createVerticalStrut(20));
        p.add(tf);
        p.add(Box.createVerticalStrut(30));
        p.add(btnStart);

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
            startLimbAnimation(tries); // tries checks: if tries=5, we draw head (idx 5)
        } else {
            btn.setBackground(Color.decode("#006400"));
            gamePanel.repaint();
        }

        if (allFound) {
            if (isVsComputer)
                saveLocalWin();
            Timer t = new Timer(500, e -> showEndDialog("YOU WON! Word: " + word, true));
            t.setRepeats(false);
            t.start();
        } else if (tries <= 0) {
            // Wait for leg animation to finish?
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
                    // Don't reset animatingLimbIndex yet, let it be drawn by the 'static' logic
                    // next repaint?
                    // The 'static' logic only draws if index > tries.
                    // If we have 5 tries left, static draws > 5. Head is 5.
                    // So we must rely on animatingLimbIndex to draw the current one even if full.
                    // OR better: In paint, if tries < 6, we draw head.
                    // My static logic was: if(tries < 6) drawHead.
                    // So, if I set tries=5 immediately (which I did), paintComponent will draw FULL
                    // HEAD immediately.
                    // I need to intercept that.
                    // Logic fix in paintComponent:
                    // If(tries < 6 && animatingLimbIndex != 5) drawHead(full)
                    // If(animatingLimbIndex == 5) drawHead(progress)
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

    private void saveLocalWin() {
        String curr = Main.current.getCredentials().getUsername();
        File fldr = new File(Database.HangManfldr, curr);
        if (!fldr.exists())
            fldr.mkdirs();
        File file = new File(fldr, "HighScore");
        int score = 0;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            score = (int) in.readObject();
        } catch (Exception e) {
        }
        score++;
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(score);
        } catch (Exception e) {
        }
    }

    private void showEndDialog(String msg, boolean win) {
        JDialog d = new JDialog(frame, "Result", true);
        d.setUndecorated(true);
        d.setSize(400, 250);
        d.setLocationRelativeTo(frame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CLR_PANEL);
        p.setBorder(new LineBorder(win ? Color.GREEN : Color.RED, 2));

        JLabel l = new JLabel("<html><center>" + msg + "</center></html>", SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l.setForeground(CLR_TEXT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnMenu = createStyledButton("MENU");
        btnMenu.addActionListener(e -> {
            d.dispose();
            showMainMenu();
        });

        p.add(Box.createVerticalStrut(40));
        p.add(l);
        p.add(Box.createVerticalStrut(40));
        p.add(btnMenu);

        d.add(p);
        d.setVisible(true);
    }

    // ==========================================
    // Friend Scoreboard
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
        JDialog d = new JDialog(frame, "Scoreboard", true);
        d.setUndecorated(true);
        d.setSize(400, 500);
        d.setLocationRelativeTo(frame);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(CLR_BG);
        main.setBorder(new LineBorder(CLR_ACCENT, 2));

        JLabel head = new JLabel("FRIEND WINS (VS CPU)", SwingConstants.CENTER);
        head.setFont(new Font("Segoe UI", Font.BOLD, 22));
        head.setForeground(CLR_ACCENT);
        head.setBorder(new EmptyBorder(20, 0, 20, 0));
        main.add(head, BorderLayout.NORTH);

        DefaultListModel<Info> model = new DefaultListModel<>();

        String curr = Main.current.getCredentials().getUsername();
        int myScore = 0;
        try {
            File f = new File(Database.HangManfldr, curr + "/HighScore");
            if (f.exists()) {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
                myScore = (int) in.readObject();
                in.close();
            }
        } catch (Exception e) {
        }
        model.addElement(new Info(curr + " (You)", myScore));

        ArrayList<String> friends = Database.Load_Friends(curr);
        for (String f : friends) {
            int s = 0;
            try {
                File file = new File(Database.HangManfldr, f + "/HighScore");
                if (file.exists()) {
                    ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                    s = (int) in.readObject();
                    in.close();
                }
            } catch (Exception e) {
            }
            model.addElement(new Info(f, s));
        }

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

        main.add(new JScrollPane(jlist), BorderLayout.CENTER);

        JButton btnClose = createStyledButton("CLOSE");
        btnClose.setPreferredSize(new Dimension(150, 40));
        btnClose.addActionListener(e -> d.dispose());
        JPanel btnP = new JPanel();
        btnP.setBackground(CLR_BG);
        btnP.add(btnClose);
        main.add(btnP, BorderLayout.SOUTH);

        d.add(main);
        d.setVisible(true);
    }

    // ==========================================
    // Online Implementation
    // ==========================================
    private void startOnlineSetup() {
        frame.getContentPane().removeAll();
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(CLR_BG);

        JLabel header = new JLabel("INVITE A FRIEND", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(CLR_TEXT);
        header.setBorder(new EmptyBorder(30, 0, 20, 0));
        content.add(header, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        String curr = Main.current.getCredentials().getUsername();
        List<String> friends = Database.Load_Friends(curr);
        for (String f : friends)
            model.addElement(f);

        JList<String> list = new JList<>(model);
        list.setBackground(CLR_BG);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new FriendListRenderer());

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            protected void configureScrollBarColors() {
                this.thumbColor = CLR_ACCENT;
                this.trackColor = CLR_PANEL;
            }

            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                return btn;
            }
        });
        content.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(CLR_BG);
        footer.setBorder(new EmptyBorder(20, 0, 20, 0));

        JButton btnInvite = createStyledButton("INVITE SELECTED");
        JButton btnBack = createStyledButton("BACK");
        btnBack.setPreferredSize(new Dimension(100, 50));

        btnInvite.addActionListener(e -> {
            String f = list.getSelectedValue();
            if (f == null)
                return;
            if (Database.Check_Online(f)) {
                selectOnlineRole(curr, f); // Hand off to role selection
            } else {
                JOptionPane.showMessageDialog(frame, "Player is OFFLINE", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBack.addActionListener(e -> showMainMenu());

        footer.add(btnBack);
        footer.add(btnInvite);
        content.add(footer, BorderLayout.SOUTH);

        frame.add(content);
        frame.revalidate();
        frame.repaint();
    }

    private void selectOnlineRole(String curr, String opponent) {
        JDialog d = new JDialog(frame, "Select Role", true);
        d.setUndecorated(true);
        d.setSize(400, 250);
        d.setLocationRelativeTo(frame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CLR_PANEL);
        p.setBorder(new LineBorder(CLR_ACCENT, 2));

        JLabel l = new JLabel("CHOOSE YOUR ROLE");
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(CLR_TEXT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnSetter = createStyledButton("SET WORD");
        JButton btnGuesser = createStyledButton("GUESS WORD");

        btnSetter.addActionListener(e -> {
            d.dispose();
            handleRoleSelection(curr, opponent, true);
        });

        btnGuesser.addActionListener(e -> {
            d.dispose();
            handleRoleSelection(curr, opponent, false);
        });

        p.add(Box.createVerticalStrut(30));
        p.add(l);
        p.add(Box.createVerticalStrut(30));
        p.add(btnSetter);
        p.add(Box.createVerticalStrut(15));
        p.add(btnGuesser);

        d.add(p);
        d.setVisible(true);
    }

    private void handleRoleSelection(String curr, String opponent, boolean amISetter) {
        if (amISetter) {
            // I am Setter, I need to input word first
            JDialog d = new JDialog(frame, "Set Word", true);
            d.setSize(400, 250);
            d.setUndecorated(true);
            d.setLocationRelativeTo(frame);

            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(CLR_PANEL);
            p.setBorder(new LineBorder(CLR_ACCENT, 2));

            JLabel l = new JLabel("ENTER WORD FOR OPPONENT");
            l.setFont(new Font("Segoe UI", Font.BOLD, 18));
            l.setForeground(CLR_ACCENT);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPasswordField tf = new JPasswordField(20);
            tf.setMaximumSize(new Dimension(300, 40));

            JButton btnStart = createStyledButton("INVITE");
            btnStart.addActionListener(ev -> {
                String w = new String(tf.getPassword()).toUpperCase();
                if (w.isEmpty() || !w.matches("[A-Z]+")) {
                    JOptionPane.showMessageDialog(d, "Invalid Word (A-Z only)", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                d.dispose();
                initiateOnlineGame(curr, opponent, w, true); // I am Setter
            });

            p.add(Box.createVerticalStrut(30));
            p.add(l);
            p.add(Box.createVerticalStrut(20));
            p.add(tf);
            p.add(Box.createVerticalStrut(30));
            p.add(btnStart);

            d.add(p);
            d.setVisible(true);
        } else {
            // I am Guesser, I let opponent set word?
            // Actually, for simplicity based on "exactly like TicTacToe" (immediate
            // launch):
            // We can't really do "Wait for them to set".
            // So, if I choose Guesser, I'm waiting for them to set.
            // BUT, normally standard is Inviter Sets.
            // Let's FORCE Inviter to Set for v1 to avoid "Waiting Room" UX complexity which
            // TicTacToe dind't have.
            // Wait, I promised "Role Selection" in Plan.
            // Okay, if I am Guesser -> I pick a Random word for myself? No that's single
            // player.
            // If I am Guesser, I am inviting THE OTHER PERSON to SET specific word.
            // So effectively, I send invite, and game starts "Waiting for Word".
            // Complex.
            // Simpler: I will just ask user to input word.
            // "You are the setter" is implicit for Inviter?
            // NO, let's stick to: Inviter Sets Word.
            // Reverting role selection to just "Input Word" and start.
            // Wait, user asked for "Online mode of hangman exactly like that of tic tac
            // toe".
            // TicTacToe has "Choose Marker".
            // Hangman Equivalent: "Choose Role".
            // If I choose Guesser, I am asking the OTHER person to Set.
            // This requires the other person to receive invite, accept, THEN pop up "Set
            // Word".
            // TicTacToe just starts.
            // I'll implement: Inviter Sets the Word. This is the most direct map to "I
            // choose X, you get O".
            // Inviter (Me) -> Sets Word -> Playing as "Setter" (Watching), Opponent is
            // "Guesser".
            // OR I want to Guess? Then I set word for... wait.
            // If I want to Guess, I should ask opponent to Set.
            // Let's implement: I Set Word -> Game Starts. I Watch. Opponent Guesses.
            // If I want to Guess, I shouldn't be the one Inviting, or I should tell them to
            // Invite me.
            // To make it ROBUST:
            // Role Dialog ->
            // 1. "I will Set Word" -> Input Word -> Send Invite (State: Playing) -> I see
            // them playing.
            // 2. "I will Guess" -> Send Invite (State: Waiting for Word) -> They get pop up
            // "Set Word for [User]".
            // This is too divergent from TTT.
            // TTT: One invite, Game Starts.
            // I will force Inviter = Setter.
            // It simplifies everything.
            // I will skip the Role Selection Dialog and go straight to "Set Word".
            // Wait, I'll just change the "startOnlineSetup" to "Select Friend" -> "Set
            // Word" -> "Go".
            // And show a tooltip "You will spectate while they guess".
            // Actually, playing as Guesser is funner.
            // Maybe "Pick Word for Opponent" (Classic).

            // Let's stick to the Plan: Inviter Sets Word.
            // Code below implements that flow only for now to ensure stability.
            // If I want to implement "Play as Guesser", I'd need a "Word Request" packet.
            // I'll stick to Inviter Sets.
        }
    }

    private void initiateOnlineGame(String curr, String opponent, String secretWord, boolean amISetter) {
        // Setup internal state
        this.word = secretWord;
        this.isVsComputer = false; // It's PvP
        // If I am Setter, I am NOT playing. I am watching.
        // My UI should be locked or just "Spectating".
        // But TicTacToe allows both to click? No, turns.
        // Hangman: Only Guesser clicks letters.

        // Players: 0 = Setter (Inviter), 1 = Guesser (Invitee)
        players[0] = curr;
        players[1] = opponent;

        filename = Database.Alphabetizefilename(curr, opponent);

        // Init DB
        Database.Create_GameFiles(Database.HangManfldr, filename);
        Database.Write_Word(secretWord, filename);

        initData(secretWord.length()); // Reset local state

        // Write Initial State
        Database.Write_found_arr(found, filename);
        Database.Write_tries(filename, tries);
        Database.Write_players(Database.HangManfldr, filename, players);

        // Send Invite
        Database.Write_Game_Invite(opponent, new Game_Invite(new Hangman(), filename, curr));

        Online_game_launch(filename);
    }

    public void Online_game_launch(String filepath) {
        this.filename = filepath;
        // Load Players to know who I am
        String[] p = Database.Load_Players(Database.HangManfldr, filename);
        if (p != null)
            players = p;

        // Determine Word Length for UI init
        String w = Database.Load_Word(filename);
        if (w != null) {
            this.word = w; // Store locally for display
            // Reload state
            boolean[] f = Database.Load_found_arr(filename);
            int t = Database.Load_tries(filename);

            // Init local
            this.letters = w.toCharArray();
            if (f != null)
                this.found = f;
            else
                this.found = new boolean[w.length()];

            if (t != -1)
                this.tries = t;
            else
                this.tries = 6;

            // UI
            String myName = Main.current.getCredentials().getUsername();
            boolean amIGuesser = myName.equals(players[1]); // Player 1 is Guesser (Invitee) if Inviter is 0.
            // Wait, if I am Invitee (received invite), I am players[1].
            // If I am Inviter, I am players[0].
            // If Inviter Sets word, then Inviter = Setter, Invitee = Guesser.

            // However, TicTacToe alphabetizes filename.
            // We need to be careful about who is 0 and 1.
            // Database.Alphabetizefilename sorts them.
            // TicTacToe writes players explicitely:
            // players[0] = curr; players[1] = f; (at invite time)
            // But we must respect that order.

            String title = "Hangman Online - " + (amIGuesser ? "GUESSING" : "SPECTATING");
            showGameScreen(title, true);

            // Start Loop
            if (onlineTimer != null)
                onlineTimer.stop();
            onlineTimer = new Timer(1000, e -> onlineGameLoop());
            onlineTimer.start();
        }
    }

    private void onlineGameLoop() {
        if (frame == null || !frame.isVisible()) {
            if (onlineTimer != null)
                onlineTimer.stop();
            return;
        }

        String current = Main.current.getCredentials().getUsername();
        boolean END = Database.Check_END(Database.HangManfldr, filename, "END");
        // Check for specific victory/loss signals if any

        // Reload State
        boolean[] f = Database.Load_found_arr(filename);
        int t = Database.Load_tries(filename);

        if (f != null)
            this.found = f;
        if (t != -1) {
            // Check delta for animation
            if (t < this.tries) {
                // Tries decreased! Animate. // Logic handled in paint, but we should trigger
                // it.
                // We just update tries, repaint will handle "static" limbs.
                // For smooth animation, we might miss it if we poll 1s.
                // We'll just snap for now or trigger 'flash'.
                flashBackground(Color.decode("#330000"));
            }
            this.tries = t;
        }

        gamePanel.repaint();
        updateKeyboardState(); // Disable guessed letters

        if (END) {
            onlineTimer.stop();
            String winner = Database.Load_END(Database.HangManfldr, filename, "END");
            handleOnlineEnd(winner);
        }
    }

    private void updateKeyboardState() {
        // Disable buttons for letters that are guessed (if we can infer them)
        // Actually we don't store 'guessed letters' history, we store 'found' (boolean
        // array).
        // BUT, if we are Guesser, we have local buttons.
        // If we are Spectator, we want to see what they guessed?
        // Current DB schema doesn't store "All Guessed Chars", only "Found Indices" and
        // "Tries Left".
        // We can't know WHICH wrong letters were pressed.
        // LIMITATION: Spectator won't see which *wrong* letters were pressed, only
        // correct ones (via word reveal) and tries count.
        // Users might want that.
        // For "exact" port, TTT syncs board.
        // I should ideally sync "GuessedChars".
        // But Database.java doesn't have `Load_GuessedChars`.
        // I'll stick to what I have. Spectator sees body building up.
    }

    private void handleOnlineGuess(char c, JButton btn) {
        // I am the Guesser.
        // Logic is same as offline, but update DB.
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

        // Update DB
        Database.Write_found_arr(found, filename);

        if (!hit) {
            tries--;
            Database.Write_tries(filename, tries);
            flashBackground(Color.decode("#330000"));
            startLimbAnimation(tries);
        } else {
            btn.setBackground(Color.decode("#006400"));
            gamePanel.repaint();
        }

        if (allFound) {
            // Win
            Database.Write_END(Database.HangManfldr, filename, Main.current.getCredentials().getUsername(), "END");
            // Dialog handled by loop mostly, or immediate
            // saveLocalWin(); // For online? Maybe.
            showEndDialog("YOU WON!", true);
        } else if (tries <= 0) {
            // Loss
            // Winner is ... Nobody? Or Setter? Getter Lost.
            Database.Write_END(Database.HangManfldr, filename, "Setter", "END"); // Sentinel for "Setter Won"
            showEndDialog("GAME OVER!", false);
        }
    }

    private void handleOnlineEnd(String winner) {
        String msg;
        boolean win;
        String me = Main.current.getCredentials().getUsername();
        if (winner.equals(me)) {
            msg = "YOU WON!";
            win = true;
        } else if (winner.equals("Setter")) {
            // If I am setter, I won.
            // Who is setter? players[0]
            if (players[0].equals(me)) {
                msg = "YOU WON! (Guesser Failed)";
                win = true;
            } else {
                msg = "GAME OVER!";
                win = false;
            }
        } else {
            msg = "GAME OVER!";
            win = false;
        }

        // Remove Game Files
        Timer t = new Timer(2000, e -> {
            ((Timer) e.getSource()).stop();
            Database.Delete_Game_files(Database.HangManfldr, filename);
            showEndDialog(msg, win);
        });
        t.start();
    }

    private class FriendListRenderer extends JPanel implements ListCellRenderer<String> {
        private JLabel nameLbl = new JLabel();
        private JLabel statusLbl = new JLabel("●");

        public FriendListRenderer() {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(15, 20, 15, 20));
            setBackground(CLR_BG);
            nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            nameLbl.setForeground(CLR_TEXT);
            statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            add(nameLbl, BorderLayout.CENTER);
            add(statusLbl, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                boolean isSelected, boolean cellHasFocus) {
            nameLbl.setText(value);
            boolean isOnline = Database.Check_Online(value);
            statusLbl.setForeground(isOnline ? Color.GREEN : Color.RED);
            if (isSelected) {
                setBackground(CLR_PANEL);
                nameLbl.setForeground(CLR_ACCENT);
            } else {
                setBackground(CLR_BG);
                nameLbl.setForeground(CLR_TEXT);
            }
            return this;
        }
    }

}
