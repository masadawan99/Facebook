import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SnakeGame extends Game {
    public SnakeGame() {
        super("SnakeGame", "Single Player");
    }

    @Override
    public void Game_launch() {
        new GameFrame();
    }
}
class GameFrame extends JFrame {
    GamePanel panel;

    GameFrame() {
        panel = new GamePanel();
        this.setContentPane(panel);
        panel.setPreferredSize(new Dimension(panel.DESIRED_WIDTH, panel.DESIRED_HEIGHT));
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        this.pack();
        int fudge = 10;
        this.setSize(this.getWidth() - fudge, this.getHeight() - fudge);

        Dimension size = panel.getSize();
        panel.computePlayableArea(size.width, size.height);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
            }
        });

        this.setLocationRelativeTo(null);
        this.setVisible(true);
        panel.requestFocusInWindow();
    }
}


class GamePanel extends JPanel implements ActionListener {

    final int DESIRED_WIDTH = 800;
    final int DESIRED_HEIGHT = 700;
    final int UNIT_SIZE = 25;
    final int INFO_PANEL_HEIGHT = 50;
    final int borderThickness = 20;

    int delay = 200;
    int speedUps = 0;
    final int MAX_SPEEDUPS = 5;

    Timer timer;
    Random random;

    int cols;
    int rows;

    int playableLeft, playableTop, playableRight, playableBottom;

    final int GAME_UNITS = (DESIRED_WIDTH * DESIRED_HEIGHT) / UNIT_SIZE;
    int x[] = new int[GAME_UNITS];
    int y[] = new int[GAME_UNITS];

    int bodyParts = 3;
    int applesEaten = 0;
    int highScore = 0;

    int appleX;
    int appleY;

    char direction = 'R';
    boolean paused = false;

    enum GameState { INCOMING, RUNNING, GAMEOVER, PAUSED }
    GameState state = GameState.INCOMING;

    GamePanel() {
        random = new Random();
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
    }

    public void computePlayableArea(int actualWidth, int actualHeight) {
        playableLeft = borderThickness;
        playableTop = INFO_PANEL_HEIGHT + borderThickness;

        int maxPlayableWidth = actualWidth - 2 * borderThickness;
        int maxPlayableHeight = actualHeight - INFO_PANEL_HEIGHT - 2 * borderThickness;

        cols = maxPlayableWidth / UNIT_SIZE;
        rows = maxPlayableHeight / UNIT_SIZE;

        playableRight = playableLeft + (cols - 1) * UNIT_SIZE;
        playableBottom = playableTop + (rows - 1) * UNIT_SIZE;

        highScore = Database.Load_HighScore();
    }

    public void startGame() {
        state = GameState.RUNNING;
        paused = false;

        delay = 200;
        speedUps = 0;
        applesEaten = 0;
        bodyParts = 3;
        direction = 'R';

        int centerCol = cols / 2;
        int centerRow = rows / 2;
        x[0] = playableLeft + centerCol * UNIT_SIZE;
        y[0] = playableTop + centerRow * UNIT_SIZE;

        for (int i = 1; i < bodyParts; i++) {
            x[i] = x[0] - i * UNIT_SIZE;
            y[i] = y[0];
        }

        for (int i = bodyParts; i < GAME_UNITS; i++) {
            x[i] = 0;
            y[i] = 0;
        }

        newApple();

        if (timer != null) timer.stop();
        timer = new Timer(delay, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        requestFocusInWindow();

        switch (state) {
            case INCOMING -> drawIncomingScreen(g);
            case RUNNING -> drawGame(g);
            case GAMEOVER -> drawGameOver(g);
            case PAUSED -> drawGame(g);
        }
    }

    private void drawIncomingScreen(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 60));
        g.drawString("WELCOME TO SNAKE", DESIRED_WIDTH / 2 - 300, DESIRED_HEIGHT / 2 - 50);
        g.setFont(new Font("Ink Free", Font.PLAIN, 30));
        g.drawString("Press ENTER to Start", DESIRED_WIDTH / 2 - 150, DESIRED_HEIGHT / 2 + 50);
    }

    private void drawGame(Graphics g) {
        g.setColor(Color.darkGray);
        g.fillRect(0, 0, DESIRED_WIDTH, INFO_PANEL_HEIGHT);
        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 25));
        g.drawString("Score: " + applesEaten, 10, 30);
        g.drawString("High Score: " + highScore, DESIRED_WIDTH - 200, 30);

        g.setColor(Color.white);
        g.fillRect(0, INFO_PANEL_HEIGHT, DESIRED_WIDTH, borderThickness); // top
        g.fillRect(0, INFO_PANEL_HEIGHT + borderThickness, borderThickness, getHeight() - INFO_PANEL_HEIGHT - borderThickness); // left
        g.fillRect(getWidth() - borderThickness, INFO_PANEL_HEIGHT + borderThickness, borderThickness, getHeight() - INFO_PANEL_HEIGHT - borderThickness); // right
        g.fillRect(0, getHeight() - borderThickness, getWidth(), borderThickness); // bottom

        g.setColor(Color.red);
        g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

        for (int i = 0; i < bodyParts; i++) {
            g.setColor(i == 0 ? Color.green : new Color(45, 180, 0));
            g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
        }

        if (paused) {
            g.setColor(Color.yellow);
            g.setFont(new Font("Ink Free", Font.BOLD, 60));
            g.drawString("PAUSED", DESIRED_WIDTH / 2 - 120, DESIRED_HEIGHT / 2);
        }
    }

    private void drawGameOver(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics m1 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (DESIRED_WIDTH - m1.stringWidth("Game Over")) / 2, DESIRED_HEIGHT / 2 - 50);

        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 30));
        FontMetrics m2 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (DESIRED_WIDTH - m2.stringWidth("Score: " + applesEaten)) / 2, DESIRED_HEIGHT / 2 + 10);

        g.setFont(new Font("Ink Free", Font.PLAIN, 26));
        g.drawString("Press ENTER to Restart", (DESIRED_WIDTH - 260) / 2, DESIRED_HEIGHT / 2 + 60);
        g.drawString("Press ESC to Exit", (DESIRED_WIDTH - 220) / 2, DESIRED_HEIGHT / 2 + 100);
    }

    public void move() {
        if (paused) return;
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
        switch (direction) {
            case 'U' -> y[0] -= UNIT_SIZE;
            case 'D' -> y[0] += UNIT_SIZE;
            case 'L' -> x[0] -= UNIT_SIZE;
            case 'R' -> x[0] += UNIT_SIZE;
        }
    }

    public void checkApple() {
        if (x[0] == appleX && y[0] == appleY) {
            applesEaten++;
            bodyParts++;
            if (applesEaten > highScore) {
                highScore = applesEaten;
                Database.Write_HighsSore(highScore);
            }
            if (applesEaten % 5 == 0 && speedUps < MAX_SPEEDUPS) {
                speedUps++;
                delay = Math.max(50, delay - 20);
                timer.setDelay(delay);
            }
            newApple();
        }
    }

    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) state = GameState.GAMEOVER;
        }

        if (x[0] < playableLeft || x[0] > playableRight || y[0] < playableTop || y[0] > playableBottom) {
            state = GameState.GAMEOVER;
        }

        if (state == GameState.GAMEOVER && timer != null) timer.stop();
    }

    public void newApple() {
        int attempts = 0;
        do {
            appleX = playableLeft + random.nextInt(cols) * UNIT_SIZE;
            appleY = playableTop + random.nextInt(rows) * UNIT_SIZE;
            attempts++;
            if (attempts > 1000) break;
        } while (isAppleOnSnake());
    }

    private boolean isAppleOnSnake() {
        for (int i = 0; i < bodyParts; i++) {
            if (x[i] == appleX && y[i] == appleY) return true;
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (state == GameState.RUNNING && !paused) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> { if (direction != 'R') direction = 'L'; }
                case KeyEvent.VK_RIGHT -> { if (direction != 'L') direction = 'R'; }
                case KeyEvent.VK_UP -> { if (direction != 'D') direction = 'U'; }
                case KeyEvent.VK_DOWN -> { if (direction != 'U') direction = 'D'; }
                case KeyEvent.VK_ENTER -> { if (state == GameState.INCOMING || state == GameState.GAMEOVER) startGame(); }
                case KeyEvent.VK_ESCAPE -> {
                    SwingUtilities.getWindowAncestor((Component)e.getSource()).dispose();
                }
                case KeyEvent.VK_P -> { if (state == GameState.RUNNING) paused = !paused; }
            }
        }
    }


}
