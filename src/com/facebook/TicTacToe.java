package com.facebook;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.Serializable;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class TicTacToe extends Game implements Serializable {
    private static final long serialVersionUID = 1L;

    // Game Logic Variables
    private String cross = "X"; // Logic identifier
    private String tick = "O"; // Logic identifier
    private String turn;
    private String mark;
    private String[] board = new String[9];
    private String filename;
    private String[] players = new String[2];
    private String[] marks = new String[2];
    private boolean isVsComputer = true;

    // GUI Components
    private transient JFrame frame;
    private transient BoardButton[] boardButtons; // Custom Button Class
    private transient JLabel statusLabel;
    private transient Timer onlineTimer;

    // Premium Palette
    private final Color CLR_BG = Color.decode("#121212");
    private final Color CLR_PANEL = Color.decode("#1E1E1E");
    private final Color CLR_BTN = Color.decode("#252525");
    private final Color CLR_BTN_HOVER = Color.decode("#303030");
    private final Color CLR_ACCENT_X = Color.decode("#FF4500"); // OrangeRed
    private final Color CLR_ACCENT_O = Color.decode("#FFA500"); // Orange
    private final Color CLR_TEXT = Color.decode("#E0E0E0");

    // Fonts
    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 40);
    private final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 22);
    // FONT_BTN removed as we draw manually
    private final Font FONT_UI = new Font("Segoe UI", Font.PLAIN, 16);

    TicTacToe() {
        super("TIC TAC TOE", "DUAL PLAYER");
    }

    @Override
    public void Game_launch() {
        SwingUtilities.invokeLater(this::showMainMenu);
    }

    // ==========================================
    // Custom Rendering Button (The Fix)
    // ==========================================
    private class BoardButton extends JButton {
        private String symbol = " "; // "X", "O", or " "
        private float animScale = 0f; // 0 to 1 for animation

        public BoardButton() {
            setFocusPainted(false);
            setBackground(CLR_PANEL);
            setBorder(new LineBorder(Color.DARK_GRAY, 1));
            setContentAreaFilled(false);
        }

        public void setSymbol(String s) {
            this.symbol = s;
            this.animScale = 1f; // Default full scale if set directly
            repaint();
        }

        public void setAnimationScale(float s) {
            this.animScale = s;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            // Background
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Symbol Rendering
            if (!symbol.equals(" ")) {
                int w = getWidth();
                int h = getHeight();
                int size = (int) (Math.min(w, h) * 0.6 * animScale); // Scale affects size
                int x = (w - size) / 2;
                int y = (h - size) / 2;
                int stroke = 8; // Thicker lines

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

    // ==========================================
    // Custom UI Components
    // ==========================================
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(CLR_TEXT);
        btn.setBackground(CLR_BTN);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
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

    // ==========================================
    // Views
    // ==========================================
    private void setupFrame(String title) {
        if (frame != null)
            frame.dispose();
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 650);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.getContentPane().setBackground(CLR_BG);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (onlineTimer != null)
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
        addButtonToMenu(content, "SCOREBOARD",
                e -> showCustomDialog("Feature unavailable without database context", "Info"));
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

    // ==========================================
    // Custom Dialogs "Pushing Limits"
    // ==========================================
    private void showCustomDialog(String message, String titleStr) {
        JDialog d = new JDialog(frame, titleStr, true);
        d.setUndecorated(true);
        d.setSize(400, 200);
        d.setLocationRelativeTo(frame);

        JPanel p = new JPanel((LayoutManager) null); // Using null layout for absolute positioning or just use
                                                     // BorderLayout
        p = new JPanel(new BorderLayout());
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
        d.setLocationRelativeTo(frame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CLR_PANEL);
        p.setBorder(BorderFactory.createLineBorder(CLR_ACCENT_X, 2));

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
                }
                Online_game_launch(filename);
            } else {
                showOfflineSelectionDialog();
            }
        });

        btnMenu.addActionListener(e -> {
            d.dispose();
            if (frame.getTitle().contains("Online")) {
                String current = Main.current.getCredentials().getUsername();
                String f = players[0].equals(current) ? players[1] : players[0];
                if (!Database.Check_Online_Game(Database.TicTacToefldr, filename, f)) {
                    Database.Delete_Game_files(Database.TicTacToefldr, filename);
                } else {
                    Database.Delete_Online_Game(Database.TicTacToefldr, filename, current);
                }
                if (onlineTimer != null)
                    onlineTimer.stop();
            }
            showMainMenu();
        });

        btns.add(btnPlay);
        btns.add(btnMenu);

        p.add(lbl, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        d.add(p);
        d.setVisible(true);
    }

    // ==========================================
    // Friend List (Slick & Minimal)
    // ==========================================
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

        JButton btnInvite = createStyledButton("INVITE SELECTED");
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
            nameLbl.setText(value);
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

    // ==========================================
    // Game Logic Integration
    // ==========================================
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

        // Header
        statusLabel = new JLabel("TURN: " + turn.toUpperCase(), SwingConstants.CENTER);
        statusLabel.setFont(FONT_HEADER);
        statusLabel.setForeground(CLR_ACCENT_X);
        main.add(statusLabel, BorderLayout.NORTH);

        // Grid with padding
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

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(CLR_BG);
        JButton btnResign = createStyledButton("RESIGN");
        btnResign.setPreferredSize(new Dimension(150, 45));
        btnResign.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnResign.addActionListener(e -> {
            if (isOnline) {
                Database.Write_END(Database.TicTacToefldr, filename,
                        players[0].equals(Main.current.getCredentials().getUsername()) ? players[1] : players[0],
                        "END");
            } else {
                showMatchEndDialog(turn.equals(players[0]) ? "Player 2 Won!" : "Player 1 Won!");
            }
        });

        JButton btnMenu = createStyledButton("MENU");
        btnMenu.setPreferredSize(new Dimension(150, 45));
        btnMenu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnMenu.addActionListener(e -> {
            if (isOnline && onlineTimer != null)
                onlineTimer.stop();
            showMainMenu();
        });

        footer.add(btnResign);
        footer.add(Box.createHorizontalStrut(20));
        footer.add(btnMenu);
        main.add(footer, BorderLayout.SOUTH);

        frame.add(main);
        frame.revalidate();
        frame.repaint();
    }

    // ... Logic ...
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
                    String winner = isVsComputer && turn.equals(players[1]) ? "Computer"
                            : (turn.equals(players[0]) ? "Player 1" : "Player 2");
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
                updateStatusLabel("TURN: PLAYER 1");
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

    // Online integration helpers
    private void selectOnlineMarker(String curr, String f) {
        JDialog d = new JDialog(frame, "Marker", true);
        d.setUndecorated(true);
        d.setSize(400, 200);
        d.setLocationRelativeTo(frame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CLR_PANEL);
        p.setBorder(BorderFactory.createLineBorder(CLR_ACCENT_X, 2));

        JLabel l = new JLabel("CHOOSE YOUR MARKER");
        l.setFont(FONT_HEADER);
        l.setForeground(CLR_TEXT);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnX = createStyledButton("CROSS (X)");
        JButton btnO = createStyledButton("TICK (O)");

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
            Database.Create_GameFiles(Database.TicTacToefldr, filename);
            Database.Write_tic_tac(board, filename, turn, players, marks);
            Database.Write_Score_board(Database.TicTacToefldr, filename, new Scoreboard());
            Database.Write_Notification(f, new Notification(Notification.Type.GAME,
                    (Main.current.getFullName()) + " Invited you to play TIC TAC TOE"));
            Database.Write_Game_Invite(f, new Game_Invite(new TicTacToe(), filename, curr));
            Online_game_launch(filename);
        };

        btnX.addActionListener(al);
        btnO.addActionListener(al);

        p.add(Box.createVerticalStrut(30));
        p.add(l);
        p.add(Box.createVerticalStrut(30));
        p.add(btnX);
        p.add(Box.createVerticalStrut(15));
        p.add(btnO);

        d.add(p);
        d.setVisible(true);
    }

    public void Online_game_launch(String filepath) {
        filename = filepath;
        board_cleaner();
        Database.Write_Online_Game(Database.TicTacToefldr, filepath, Main.current.getCredentials().getUsername());
        Database.Write_board(filepath, board);
        showGameBoard("Tic Tac Toe - Online", true);
        if (onlineTimer != null)
            onlineTimer.stop();
        onlineTimer = new Timer(1000, e -> onlineGameLoop());
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
            if (state == State.WIN)
                Database.Write_END(Database.TicTacToefldr, filename, turn, "END");
            else if (state == State.DRAW)
                Database.Write_END(Database.TicTacToefldr, filename, null, "END");
            turn = turn.equals(players[0]) ? players[1] : players[0];
            Database.Write_board(filename, board);
            Database.Write_turn(Database.TicTacToefldr, filename, turn);
        } else
            pulseButton(boardButtons[index], Color.RED);
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
        int[][] wins = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 }, { 0, 4, 8 },
                { 2, 4, 6 } };
        for (int[] w : wins) {
            String b0 = board[w[0]];
            if (!b0.equals(" ") && b0.equals(board[w[1]]) && b0.equals(board[w[2]]) && b0.equals(winner)) {
                for (int idx : w)
                    pulseButton(boardButtons[idx], Color.GREEN);
            }
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

    public void board_cleaner() {
        for (int i = 0; i < board.length; i++)
            board[i] = " ";
    }

    public State Game_mechanic(String turn) {
        // ... Original simple check reused ...
        int[][] wins = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 }, { 0, 4, 8 },
                { 2, 4, 6 } };
        for (int[] w : wins) {
            if (board[w[0]].equals(turn) && board[w[1]].equals(turn) && board[w[2]].equals(turn))
                return State.WIN;
        }
        for (String s : board)
            if (s.equals(" "))
                return State.CONTINUE;
        return State.DRAW;
    }

    public boolean Place_marker(int index, String turn) {
        if (board[index].equals(" ")) {
            board[index] = turn;
            return true;
        }
        return false;
    }

    public void offline_game() {
    }

    public void online() {
    }

    public void Print_board() {
    }
}
