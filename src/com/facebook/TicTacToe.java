package com.facebook;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.Serializable;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class TicTacToe extends Game implements Serializable {

    private static final long serialVersionUID = 1L;

    private String cross = "X";
    private String tick = "O";
    private String turn;
    private String mark;
    private String[] board = new String[9];
    private String filename;
    private String[] players = new String[2];
    private String[] marks = new String[2];
    private boolean isVsComputer = true;
    private boolean isOnline = false;
    private JButton btnExit;

    private transient JFrame frame;
    private transient BoardButton[] boardButtons;
    private transient JLabel statusLabel;
    private transient Timer onlineTimer;

    private final Color CLR_BG = Color.decode("#121212");
    private final Color CLR_PANEL = Color.decode("#1E1E1E");
    private final Color CLR_BTN = Color.decode("#252525");
    private final Color CLR_BTN_HOVER = Color.decode("#303030");
    private final Color CLR_ACCENT_X = Color.decode("#FF4500");
    private final Color CLR_ACCENT_O = Color.decode("#FFA500");
    private final Color CLR_TEXT = Color.decode("#E0E0E0");

    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 40);
    private final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 22);
    private final Font FONT_UI = new Font("Segoe UI", Font.PLAIN, 16);

    public TicTacToe() {
        super("TIC TAC TOE", "DUAL PLAYER");
    }

    @Override
    public void Game_launch() {
        SwingUtilities.invokeLater(this::showMainMenu);
    }

    public State Game_mechanic(String turn) {
        if ((board[0].equals(turn) && board[0].equals(board[1]) && board[1].equals(board[2]))
                || (board[3].equals(turn) && board[3].equals(board[4]) && board[4].equals(board[5]))
                || (board[6].equals(turn) && board[6].equals(board[7]) && board[7].equals(board[8]))) {
            return State.WIN;
        }
        if ((board[0].equals(turn) && board[0].equals(board[3]) && board[3].equals(board[6]))
                || (board[1].equals(turn) && board[1].equals(board[4]) && board[4].equals(board[7]))
                || (board[2].equals(turn) && board[2].equals(board[5]) && board[5].equals(board[8]))) {
            return State.WIN;
        }
        if ((board[0].equals(turn) && board[0].equals(board[4]) && board[4].equals(board[8]))
                || (board[2].equals(turn) && board[2].equals(board[4]) && board[4].equals(board[6]))) {
            return State.WIN;
        }
        for (int i = 0; i < board.length; i++) {
            if (board[i].equals(" "))
                return State.CONTINUE;
        }
        return State.DRAW;
    }

    public boolean Place_marker(int index, String turn) {
        if (board[index].equals(" ")) {
            board[index] = turn;
            return true;
        }
        return false;
    }

    public void board_cleaner() {
        for (int i = 0; i < board.length; i++)
            board[i] = " ";
    }

    private class BoardButton extends JButton {
        private String symbol = " ";
        private float animScale = 0f;

        public BoardButton() {
            setFocusPainted(false);
            setBackground(CLR_PANEL);
            setBorder(null);
            setContentAreaFilled(false);
        }

        public void setSymbol(String s) {
            this.symbol = s;
            this.animScale = 1f;
            repaint();
        }

        public void setAnimationScale(float s) {
            this.animScale = s;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (!symbol.equals(" ")) {
                int w = getWidth();
                int h = getHeight();
                int size = (int) (Math.min(w, h) * 0.6 * animScale);
                int x = (w - size) / 2;
                int y = (h - size) / 2;
                int stroke = 8;

                g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                if (symbol.equals(cross)) {
                    g2.setColor(CLR_ACCENT_X);
                    g2.drawLine(x, y, x + size, y + size);
                    g2.drawLine(x + size, y, x, y + size);
                } else if (symbol.equals(tick)) {
                    g2.setColor(CLR_ACCENT_O);
                    g2.drawOval(x, y, size, size);
                }
            }
            g2.dispose();
        }
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
                g2.setColor(CLR_ACCENT_X);
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

    private JLabel p1StatusLbl;
    private JLabel p2StatusLbl;

    private void cleanupOnlineGame() {
        if (filename == null || players == null || players.length < 2)
            return;
        String current = Main.current.getCredentials().getUsername();
        // Only delete the online presence marker, keeping the game history/scoreboard
        Database.Delete_Online_Game(Database.TicTacToefldr, filename, current);
    }

    private void styleScrollBar(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = CLR_ACCENT_X;
                this.trackColor = CLR_PANEL;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                return btn;
            }
        });
        scrollPane.setBorder(null);
    }

    private void setupFrame(String title) {
        if (frame != null)
            frame.dispose();
        frame = new JFrame(title);
        frame.setUndecorated(true);
        frame.setSize(500, 600);
        // frame.setIconImage(new ImageIcon(Main.favicon).getImage()); // Removed:
        // favicon not existing
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.getContentPane().setBackground(CLR_BG);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (frame.getTitle().contains("Online")) {
                    cleanupOnlineGame();
                }
                if (onlineTimer != null && onlineTimer.isRunning())
                    onlineTimer.stop();
            }
        });
    }

    private void showMainMenu() {
        setupFrame("Tic Tac Toe - Menu");
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CLR_BG);
        content.setBorder(new EmptyBorder(50, 50, 50, 50));

        JLabel title = new JLabel("TIC TAC TOE");
        title.setFont(FONT_TITLE);
        title.setForeground(CLR_ACCENT_X);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(60));

        addButtonToMenu(content, "OFFLINE MODE", e -> showOfflineSelectionDialog());
        addButtonToMenu(content, "ONLINE MODE", e -> startOnlineSetup());

        addButtonToMenu(content, "EXIT", e -> frame.dispose());

        frame.add(content);
        frame.setVisible(true);
    }

    private void addButtonToMenu(JPanel panel, String text, ActionListener action) {
        JButton btn = createStyledButton(text);
        btn.addActionListener(action);
        panel.add(btn);
        panel.add(Box.createVerticalStrut(20));
    }

    private void showCustomDialog(String message, String titleStr) {
        JDialog d = new JDialog(frame, titleStr, true);
        d.setUndecorated(true);
        d.setSize(400, 200);
        d.setLocationRelativeTo(frame);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CLR_PANEL);
        p.setBorder(BorderFactory.createLineBorder(CLR_ACCENT_X, 2));

        JLabel msg = new JLabel("<html><center>" + message + "</center></html>", SwingConstants.CENTER);
        msg.setFont(FONT_UI);
        msg.setForeground(CLR_TEXT);

        JButton btnOk = createStyledButton("OK");
        btnOk.addActionListener(e -> d.dispose());

        p.add(msg, BorderLayout.CENTER);
        JPanel btnP = new JPanel();
        btnP.setOpaque(false);
        btnP.add(btnOk);
        p.add(btnP, BorderLayout.SOUTH);

        d.add(p);
        d.setVisible(true);
    }

    private void showOfflineSelectionDialog() {
        JDialog d = new JDialog(frame, "Select Mode", true);
        d.setUndecorated(true);
        d.setSize(400, 300);

        int x = frame.getX() + (frame.getWidth() - d.getWidth()) / 2;
        int y = frame.getY() + (frame.getHeight() - d.getHeight()) / 2 - 50;
        d.setLocation(x, y);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CLR_BG);

        p.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

        JLabel lbl = new JLabel("SELECT OPPONENT");
        lbl.setFont(FONT_HEADER);
        lbl.setForeground(CLR_ACCENT_X);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(Box.createVerticalStrut(30));
        p.add(lbl);
        p.add(Box.createVerticalStrut(40));

        JButton btnCpu = createStyledButton("VS COMPUTER");
        JButton btnHuman = createStyledButton("VS PLAYER (LOCAL)");
        JButton btnCancel = createStyledButton("CANCEL");

        btnCpu.addActionListener(e -> {
            d.dispose();
            isVsComputer = true;
            setupOfflineGame("You vs Computer");
        });
        btnHuman.addActionListener(e -> {
            d.dispose();
            isVsComputer = false;
            setupOfflineGame("Player 1 vs Player 2");
        });
        btnCancel.addActionListener(e -> d.dispose());

        p.add(btnCpu);
        p.add(Box.createVerticalStrut(15));
        p.add(btnHuman);
        p.add(Box.createVerticalStrut(15));
        p.add(btnCancel);
        p.add(Box.createVerticalGlue());

        d.add(p);
        d.setVisible(true);
    }

    private void showMatchEndDialog(String winnerMsg) {
        if (onlineTimer != null && onlineTimer.isRunning()) {
            onlineTimer.stop();
        }

        JDialog d = new JDialog(frame, "Game Over", true);
        d.setUndecorated(true);
        d.setSize(400, 250);
        d.setLocationRelativeTo(frame);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CLR_PANEL);
        p.setBorder(BorderFactory.createLineBorder(winnerMsg.contains("Won") ? CLR_ACCENT_X : Color.GRAY, 2));

        JLabel lbl = new JLabel(winnerMsg, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lbl.setForeground(CLR_ACCENT_X);

        JPanel btns = new JPanel(new FlowLayout());
        btns.setOpaque(false);

        JButton btnPlay = createStyledButton("PLAY AGAIN");
        btnPlay.setPreferredSize(new Dimension(140, 45));
        btnPlay.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnMenu = createStyledButton("MENU");
        btnMenu.setPreferredSize(new Dimension(100, 45));
        btnMenu.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnPlay.addActionListener(e -> {
            d.dispose();
            if (frame.getTitle().contains("Online")) {
                String current = Main.current.getCredentials().getUsername();
                if (!Database.Check_END(Database.TicTacToefldr, filename, "END" + current)) {
                    String endVal = Database.Load_END(Database.TicTacToefldr, filename, "END");
                    String f = players[0].equals(current) ? players[1] : players[0];
                    Database.Write_END(Database.TicTacToefldr, filename, endVal, "END" + f);
                    Database.Delete_END(Database.TicTacToefldr, filename, "END");
                } else {
                    Database.Delete_END(Database.TicTacToefldr, filename, "END" + current);
                    Database.Delete_END(Database.TicTacToefldr, filename, "END");
                }

                // Reset board for new game
                board_cleaner();
                Database.Write_board(filename, board);

                Online_game_launch(filename);
            } else {
                if (isVsComputer) {
                    setupOfflineGame("You vs Computer");
                } else {
                    setupOfflineGame("Player 1 vs Player 2");
                }
            }
        });

        btnMenu.addActionListener(e -> {
            d.dispose();
            if (frame.getTitle().contains("Online")) {
                cleanupOnlineGame();
            }
            if (onlineTimer != null && onlineTimer.isRunning())
                onlineTimer.stop();
            showMainMenu();
        });

        btns.add(btnPlay);
        btns.add(btnMenu);

        p.add(lbl, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        d.add(p);
        d.setVisible(true);
    }

    private void startOnlineSetup() {
        frame.getContentPane().removeAll();
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(CLR_BG);

        JLabel header = new JLabel("INVITE A FRIEND", SwingConstants.CENTER);
        header.setFont(FONT_HEADER);
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
        styleScrollBar(scroll);
        content.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(CLR_BG);
        footer.setBorder(new EmptyBorder(20, 0, 20, 0));

        JButton btnInvite = createStyledButton("SEND INVITE");
        JButton btnBack = createStyledButton("BACK");
        btnBack.setPreferredSize(new Dimension(100, 50));

        btnInvite.addActionListener(e -> {
            String f = list.getSelectedValue();
            if (f == null)
                return;
            if (Database.Check_Online(f)) {
                selectOnlineMarker(curr, f);
            } else {
                showCustomDialog("Player is OFFLINE", "Error");
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
            nameLbl.setText(Main.Get_Fullname(value));
            boolean isOnline = Database.Check_Online(value);
            statusLbl.setForeground(isOnline ? Color.GREEN : Color.RED);

            if (isSelected) {
                setBackground(CLR_PANEL);
                nameLbl.setForeground(CLR_ACCENT_X);
            } else {
                setBackground(CLR_BG);
                nameLbl.setForeground(CLR_TEXT);
            }
            return this;
        }
    }

    private void setupOfflineGame(String title) {
        players[0] = cross;
        players[1] = tick;
        turn = players[0];
        board_cleaner();
        showGameBoard(title, false);
    }

    private void showGameBoard(String title, boolean isOnline) {
        frame.getContentPane().removeAll();
        frame.setTitle(title);

        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(CLR_BG);
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CLR_BG);

        String labelText;
        if (isOnline) {
            labelText = "TURN: " + turn.toUpperCase();

            // Player 1 Status Panel (Left)
            JPanel p1Panel = new JPanel();
            p1Panel.setBackground(CLR_BG);
            p1Panel.setLayout(new BoxLayout(p1Panel, BoxLayout.Y_AXIS));

            JLabel p1Name = new JLabel(Main.Get_Fullname(players[0]));
            p1Name.setFont(new Font("Segoe UI", Font.BOLD, 12));
            p1Name.setForeground(CLR_TEXT);
            p1StatusLbl = new JLabel("Offline");
            p1StatusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            p1StatusLbl.setForeground(Color.RED);

            p1Panel.add(p1Name);
            p1Panel.add(p1StatusLbl);
            headerPanel.add(p1Panel, BorderLayout.WEST);

            // Player 2 Status Panel (Right)
            JPanel p2Panel = new JPanel();
            p2Panel.setBackground(CLR_BG);
            p2Panel.setLayout(new BoxLayout(p2Panel, BoxLayout.Y_AXIS));

            JLabel p2Name = new JLabel(Main.Get_Fullname(players[1]));
            p2Name.setFont(new Font("Segoe UI", Font.BOLD, 12));
            p2Name.setForeground(CLR_TEXT);
            p2StatusLbl = new JLabel("Offline");
            p2StatusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            p2StatusLbl.setForeground(Color.RED);

            p2Panel.add(p2Name);
            p2Panel.add(p2StatusLbl);
            headerPanel.add(p2Panel, BorderLayout.EAST);

        } else {
            if (isVsComputer) {
                labelText = turn.equals(players[1]) ? "TURN: COMPUTER" : "TURN: YOUR'S";
            } else {
                labelText = "TURN: " + (turn.equals(players[0]) ? "PLAYER 1" : "PLAYER 2");
            }
        }
        statusLabel = new JLabel(labelText, SwingConstants.CENTER);
        statusLabel.setFont(FONT_HEADER);
        statusLabel.setForeground(CLR_ACCENT_X);
        headerPanel.add(statusLabel, BorderLayout.CENTER);

        main.add(headerPanel, BorderLayout.NORTH);

        JPanel gridContainer = new JPanel(new GridBagLayout());
        gridContainer.setBackground(CLR_BG);
        JPanel grid = new JPanel(new GridLayout(3, 3, 15, 15));
        grid.setBackground(CLR_BG);
        grid.setPreferredSize(new Dimension(400, 400));

        boardButtons = new BoardButton[9];
        for (int i = 0; i < 9; i++) {
            final int idx = i;
            BoardButton btn = new BoardButton();
            btn.addActionListener(e -> {
                if (isOnline)
                    handleOnlineClick(idx);
                else
                    handleOfflineClick(idx);
            });
            boardButtons[i] = btn;
            grid.add(btn);
        }
        gridContainer.add(grid);
        main.add(gridContainer, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(CLR_BG);
        JButton btnResign = createStyledButton("RESIGN");
        btnResign.setPreferredSize(new Dimension(120, 45));
        btnResign.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnResign.addActionListener(e -> {
            if (isOnline) {
                String current = Main.current.getCredentials().getUsername();
                String opponent = players[0].equals(current) ? players[1] : players[0];

                // Forfeit logic: Update scoreboard
                Scoreboard sb = Database.Load_Score_board(Database.TicTacToefldr, filename);
                if (sb != null) {
                    if (opponent.equals(players[0]))
                        sb.increment_Score1();
                    else
                        sb.increment_Score2();
                    Database.Write_Score_board(Database.TicTacToefldr, filename, sb);
                }

                Database.Write_END(Database.TicTacToefldr, filename, opponent, "END");
                Database.Write_END(Database.TicTacToefldr, filename, opponent, "END" + opponent);
            } else {
                showMatchEndDialog(turn.equals(players[0]) ? "Player 2 Won!" : "Player 1 Won!");
            }
        });

        footer.add(btnResign);

        if (isOnline) {
            JButton btnScore = createStyledButton("SCOREBOARD");
            btnScore.setPreferredSize(new Dimension(120, 45));
            btnScore.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnScore.addActionListener(e -> showInGameScoreboard());
            footer.add(Box.createHorizontalStrut(20));
            footer.add(Box.createHorizontalStrut(20));
            footer.add(btnScore);

            // Exit Button (Right of Scoreboard)
            btnExit = createStyledButton("EXIT");
            btnExit.setPreferredSize(new Dimension(90, 45));
            btnExit.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnExit.setEnabled(false); // Disabled by default
            btnExit.addActionListener(e -> {
                if (onlineTimer != null)
                    onlineTimer.stop();
                frame.dispose();
                showMainMenu();
            });
            footer.add(Box.createHorizontalStrut(20));
            footer.add(btnExit);

        }
        main.add(footer, BorderLayout.SOUTH);

        frame.add(main);
        frame.revalidate();
        frame.repaint();
    }

    private void handleOfflineClick(int index) {
        if (isVsComputer && !turn.equals(players[0]))
            return;
        if (!board[index].equals(" ")) {
            pulseButton(boardButtons[index], Color.RED);
            return;
        }

        if (Place_marker(index, turn)) {
            animateMarkerParams(index, turn);
            State state = Game_mechanic(turn);

            if (state == State.WIN) {
                highlightWin(turn);
                Timer t = new Timer(1000, e -> {
                    ((Timer) e.getSource()).stop();
                    String winner;
                    if (isVsComputer) {
                        winner = turn.equals(players[1]) ? "Computer" : "You";
                    } else {
                        winner = turn.equals(players[0]) ? "Player 1" : "Player 2";
                    }
                    showMatchEndDialog(winner + " Won!");
                });
                t.start();
            } else if (state == State.DRAW) {
                showMatchEndDialog("Game Draw!");
            } else {
                turn = turn.equals(players[0]) ? players[1] : players[0];
                if (isVsComputer && turn.equals(players[1])) {
                    updateStatusLabel("TURN: COMPUTER");
                    Timer t = new Timer(700, e -> {
                        ((Timer) e.getSource()).stop();
                        computerMove();
                    });
                    t.start();
                } else {
                    updateStatusLabel("TURN: " + (turn.equals(players[0]) ? "PLAYER 1" : "PLAYER 2"));
                }
            }
        }
    }

    private void computerMove() {
        if (frame == null || !frame.isVisible())
            return;
        int move = findBestMove(players[1]);
        if (move == -1)
            move = findBestMove(players[0]);
        if (move == -1 && board[4].equals(" "))
            move = 4;
        if (move == -1) {
            java.util.List<Integer> available = new java.util.ArrayList<>();
            for (int i = 0; i < 9; i++)
                if (board[i].equals(" "))
                    available.add(i);
            if (!available.isEmpty())
                move = available.get(new java.util.Random().nextInt(available.size()));
        }
        if (move != -1 && Place_marker(move, turn)) {
            animateMarkerParams(move, turn);
            State state = Game_mechanic(turn);
            if (state == State.WIN) {
                highlightWin(turn);
                Timer t = new Timer(1000, e -> {
                    ((Timer) e.getSource()).stop();
                    showMatchEndDialog("Computer Won!");
                });
                t.start();
            } else if (state == State.DRAW) {
                showMatchEndDialog("Game Draw!");
            } else {
                turn = players[0];
                updateStatusLabel("TURN: YOUR'S");
            }
        }
    }

    private int findBestMove(String player) {
        for (int i = 0; i < 9; i++) {
            if (board[i].equals(" ")) {
                board[i] = player;
                if (Game_mechanic(player) == State.WIN) {
                    board[i] = " ";
                    return i;
                }
                board[i] = " ";
            }
        }
        return -1;
    }

    private void selectOnlineMarker(String curr, String f) {
        JDialog d = new JDialog(frame, "Marker", true);
        d.setUndecorated(true);
        d.setSize(400, 300);

        int x = frame.getX() + (frame.getWidth() - d.getWidth()) / 2;
        int y = frame.getY() + (frame.getHeight() - d.getHeight()) / 2 - 50;
        d.setLocation(x, y);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CLR_BG);
        p.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

        JLabel l = new JLabel("CHOOSE YOUR MARKER");
        l.setFont(FONT_HEADER);
        l.setForeground(CLR_ACCENT_X);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnX = createStyledButton("CROSS (X)");
        JButton btnO = createStyledButton("TICK (O)");
        JButton btnBack = createStyledButton("BACK");

        ActionListener al = e -> {
            d.dispose();
            boolean isX = e.getSource() == btnX;
            if (isX) {
                marks[0] = cross;
                marks[1] = tick;
                turn = curr;
            } else {
                marks[0] = tick;
                marks[1] = cross;
                turn = f;
            }

            players[0] = curr;
            players[1] = f;
            filename = Database.Alphabetizefilename(curr, f);

            Scoreboard existingScoreboard = Database.Load_Score_board(Database.TicTacToefldr, filename);
            if (existingScoreboard == null) {
                Database.Create_GameFiles(Database.TicTacToefldr, filename);
                Database.Write_Score_board(Database.TicTacToefldr, filename, new Scoreboard());
            }

            // Fix: Clear any stale END files to prevent immediate game over
            Database.Delete_END(Database.TicTacToefldr, filename, "END");
            Database.Delete_END(Database.TicTacToefldr, filename, "END" + curr);
            Database.Delete_END(Database.TicTacToefldr, filename, "END" + f);

            board_cleaner();
            Database.Write_tic_tac(board, filename, turn, players, marks);

            Database.Write_Notification(f, new Notification(Notification.Type.GAME,
                    (Main.current.getFullName()) + " Invited you to play TIC TAC TOE"));
            Database.Write_Game_Invite(f, new Game_Invite("TicTacToe", filename, curr));
            Online_game_launch(filename);
        };

        btnX.addActionListener(al);
        btnO.addActionListener(al);
        btnBack.addActionListener(e -> {
            d.dispose();
            startOnlineSetup(); // Return to friend selection
        });

        p.add(Box.createVerticalStrut(30));
        p.add(l);
        p.add(Box.createVerticalStrut(30));
        p.add(btnX);
        p.add(Box.createVerticalStrut(15));
        p.add(btnO);
        p.add(Box.createVerticalStrut(15));
        p.add(btnBack);
        p.add(Box.createVerticalGlue());

        d.add(p);
        d.setVisible(true);
    }

    public void Online_game_launch(String filepath) {
        filename = filepath;

        String[] p = Database.Load_Players(Database.TicTacToefldr, filename);
        if (p != null)
            players = p;

        String[] m = Database.Load_marks(filename);
        if (m != null)
            marks = m;

        String t = Database.Load_turn(Database.TicTacToefldr, filename);
        if (t != null) {
            turn = t;
        } else if (players[0] != null) {
            turn = players[0];
        }

        String title = "Tic Tac Toe - Online";
        if (frame == null) {
            setupFrame(title);
        }

        // Just Load Board (Joiner Logic) - Do NOT Reset
        String[] b = Database.Load_tic_tac_board(filepath);
        if (b != null)
            board = b;
        else
            board_cleaner(); // Fallback

        Database.Write_Online_Game(Database.TicTacToefldr, filepath, Main.current.getCredentials().getUsername());
        showGameBoard(title, true);
        frame.setVisible(true);

        if (onlineTimer != null)
            onlineTimer.stop();
        // Updated to 1500 as requested
        onlineTimer = new Timer(1500, e -> onlineGameLoop());
        onlineTimer.start();
    }

    private void onlineGameLoop() {
        if (frame == null || !frame.isVisible()) {
            if (onlineTimer != null)
                onlineTimer.stop();
            return;
        }
        String current = Main.current.getCredentials().getUsername();
        boolean END = Database.Check_END(Database.TicTacToefldr, filename, "END");
        boolean ZEEND = Database.Check_END(Database.TicTacToefldr, filename, "END" + current);

        board = Database.Load_tic_tac_board(filename);
        turn = Database.Load_turn(Database.TicTacToefldr, filename);
        updateBoardFromState();

        // CHECK ONLINE STATUS
        if (players != null && players.length >= 2) {
            boolean p1Online = Database.Check_Online_Game(Database.TicTacToefldr, filename, players[0]);
            boolean p2Online = Database.Check_Online_Game(Database.TicTacToefldr, filename, players[1]);

            if (p1StatusLbl != null) {
                p1StatusLbl.setText(p1Online ? "Online" : "Offline");
                p1StatusLbl.setForeground(p1Online ? Color.GREEN : Color.RED);
            }
            if (p2StatusLbl != null) {
                p2StatusLbl.setText(p2Online ? "Online" : "Offline");
                p2StatusLbl.setForeground(p2Online ? Color.GREEN : Color.RED);
            }

            // Exit Button Logic: Enable only if opponent is Offline
            if (btnExit != null) {
                String me = Main.current.getCredentials().getUsername();
                boolean amIP1 = me.equals(players[0]);
                boolean opponentOnline = amIP1 ? p2Online : p1Online;

                if (!opponentOnline) {
                    btnExit.setEnabled(true);
                    btnExit.setToolTipText("Opponent is offline. You can leave safely.");
                } else {
                    btnExit.setEnabled(false);
                    btnExit.setToolTipText("Opponent is online. Use Resign to leave.");
                }
            }
        }

        updateStatusLabel("TURN: " + Main.Get_Fullname(turn).toUpperCase());

        if (END || ZEEND) {
            onlineTimer.stop();
            handleOnlineEnd(END, ZEEND, current);
        }
    }

    private void handleOnlineClick(int index) {
        String current = Main.current.getCredentials().getUsername();
        if (!turn.equals(current)) {
            pulseButton(boardButtons[index], Color.GRAY);
            return;
        }
        mark = turn.equals(players[0]) ? marks[0] : marks[1];
        if (Place_marker(index, mark)) {
            animateMarkerParams(index, mark);
            State state = Game_mechanic(mark);
            if (state == State.WIN) {
                // Update Scoreboard on WIN
                Scoreboard sb = Database.Load_Score_board(Database.TicTacToefldr, filename);
                if (sb != null) {
                    if (turn.equals(players[0]))
                        sb.increment_Score1();
                    else
                        sb.increment_Score2();
                    Database.Write_Score_board(Database.TicTacToefldr, filename, sb);
                }
                Database.Write_END(Database.TicTacToefldr, filename, turn, "END");
            } else if (state == State.DRAW) {
                Scoreboard sb = Database.Load_Score_board(Database.TicTacToefldr, filename);
                if (sb != null) {
                    sb.increment_Total(); // Just increment total games on draw if desired, or handled in increments
                    // Actually Scoreboard.java increment_Total is called by increment_ScoreX, so
                    // for draw we might just want to increment total?
                    // Scoreboard.java: setTotolgame, getTotolgame... logic implies total = s1 + s2
                    // usually, but actually draws count too.
                    // Accessing sb.increment_Total() directly:
                    sb.increment_Total();
                    Database.Write_Score_board(Database.TicTacToefldr, filename, sb);
                }
                Database.Write_END(Database.TicTacToefldr, filename, null, "END");
            }
            turn = turn.equals(players[0]) ? players[1] : players[0];
            Database.Write_board(filename, board);
            Database.Write_turn(Database.TicTacToefldr, filename, turn);
        } else
            pulseButton(boardButtons[index], Color.RED);
    }

    private void showInGameScoreboard() {
        if (onlineTimer != null && onlineTimer.isRunning())
            onlineTimer.stop();

        JDialog d = new JDialog(frame, "Scoreboard", true);
        d.setUndecorated(true);
        d.setSize(400, 300);
        d.setLocationRelativeTo(frame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CLR_BG);
        p.setBorder(BorderFactory.createLineBorder(CLR_ACCENT_X, 2));

        JLabel title = new JLabel("SCOREBOARD");
        title.setFont(FONT_HEADER);
        title.setForeground(CLR_ACCENT_X);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        Scoreboard sb = Database.Load_Score_board(Database.TicTacToefldr, filename);
        int s1 = 0, s2 = 0, total = 0;
        if (sb != null) {
            s1 = sb.getScore1();
            s2 = sb.getScore2();
            total = sb.getTotolgame();
        }

        JLabel p1Sc = new JLabel(Main.Get_Fullname(players[0]) + ": " + s1);
        p1Sc.setFont(FONT_UI);
        p1Sc.setForeground(CLR_TEXT);
        p1Sc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel p2Sc = new JLabel(Main.Get_Fullname(players[1]) + ": " + s2);
        p2Sc.setFont(FONT_UI);
        p2Sc.setForeground(CLR_TEXT);
        p2Sc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tot = new JLabel("Total Games: " + total);
        tot.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        tot.setForeground(Color.GRAY);
        tot.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnBack = createStyledButton("BACK");
        btnBack.addActionListener(e -> {
            d.dispose();
            if (onlineTimer != null)
                onlineTimer.start();
        });

        p.add(Box.createVerticalStrut(30));
        p.add(title);
        p.add(Box.createVerticalStrut(30));
        p.add(p1Sc);
        p.add(Box.createVerticalStrut(10));
        p.add(p2Sc);
        p.add(Box.createVerticalStrut(20));
        p.add(tot);
        p.add(Box.createVerticalStrut(30));
        p.add(btnBack);
        p.add(Box.createVerticalGlue());

        d.add(p);
        d.setVisible(true);
    }

    private void handleOnlineEnd(boolean END, boolean ZEEND, String current) {
        String winner = "Nobody";
        if (END) {
            String w = Database.Load_END(Database.TicTacToefldr, filename, "END");
            if (w != null)
                winner = w;
        }
        if (ZEEND) {
            String w = Database.Load_END(Database.TicTacToefldr, filename, "END" + current);
            if (w != null)
                winner = w;
        }
        if (!winner.equals("Nobody"))
            highlightWin(winner);
        String winName = winner.equals("Nobody") ? "Nobody" : Main.Get_Fullname(winner);

        Timer t = new Timer(500, e -> {
            ((Timer) e.getSource()).stop();
            showMatchEndDialog(winName + " WON!");
        });
        t.start();
    }

    private void updateStatusLabel(String text) {
        if (statusLabel != null)
            statusLabel.setText(text);
    }

    private void updateBoardFromState() {
        for (int i = 0; i < 9; i++) {
            if (!board[i].equals(" ")) {
                boardButtons[i].setSymbol(board[i]);
            } else
                boardButtons[i].setSymbol(" ");
        }
    }

    private void animateMarkerParams(int index, String mark) {
        BoardButton btn = boardButtons[index];
        btn.symbol = mark;
        btn.animScale = 0.1f;
        Timer t = new Timer(15, e -> {
            if (btn.animScale < 1.0f) {
                btn.setAnimationScale(btn.animScale + 0.1f);
            } else {
                btn.setAnimationScale(1.0f);
                ((Timer) e.getSource()).stop();
            }
        });
        t.start();
    }

    private void highlightWin(String winner) {
        for (int i = 0; i < 9; i += 3) {
            if (board[i].equals(winner) && board[i + 1].equals(winner) && board[i + 2].equals(winner)) {
                pulseButton(boardButtons[i], Color.GREEN);
                pulseButton(boardButtons[i + 1], Color.GREEN);
                pulseButton(boardButtons[i + 2], Color.GREEN);
            }
        }
        for (int i = 0; i < 3; i++) {
            if (board[i].equals(winner) && board[i + 3].equals(winner) && board[i + 6].equals(winner)) {
                pulseButton(boardButtons[i], Color.GREEN);
                pulseButton(boardButtons[i + 3], Color.GREEN);
                pulseButton(boardButtons[i + 6], Color.GREEN);
            }
        }
        if (board[0].equals(winner) && board[4].equals(winner) && board[8].equals(winner)) {
            pulseButton(boardButtons[0], Color.GREEN);
            pulseButton(boardButtons[4], Color.GREEN);
            pulseButton(boardButtons[8], Color.GREEN);
        }
        if (board[2].equals(winner) && board[4].equals(winner) && board[6].equals(winner)) {
            pulseButton(boardButtons[2], Color.GREEN);
            pulseButton(boardButtons[4], Color.GREEN);
            pulseButton(boardButtons[6], Color.GREEN);
        }
    }

    private void pulseButton(JButton btn, Color flashColor) {
        Color original = CLR_PANEL;
        Timer t = new Timer(100, new ActionListener() {
            int count = 0;

            public void actionPerformed(ActionEvent e) {
                btn.setBackground(count % 2 == 0 ? flashColor : original);
                if (++count >= 4) {
                    ((Timer) e.getSource()).stop();
                    btn.setBackground(original);
                }
            }
        });
        t.start();
    }
}
