package engine;

import entities.Player;
import grid.Board;
import patterns.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CLASS: GameEngine
 * Main game controller - manages game loop, timing, patterns, collisions
 *
 * POLYMORPHISM DEMO:
 * - List<Hazard> stores RowHazard, ColumnHazard, SpotHazard, CrossHazard
 * - Calls update() on all → Java resolves to correct subclass at runtime
 */
public class GameEngine extends JPanel implements Runnable {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 700;
    private static final int FPS = 60;

    private Board board;
    private Player player;
    private GameState gameState;
    private List<Hazard> hazards;
    private Random random;

    private boolean running;
    private long lastBeatTime;
    private int beatTimer;
    private int beatInterval;

    private BeatSoundEngine beatSound;

    private boolean gameStarted;
    private boolean showingGameOver;

    // Game over screen buttons
    private Rectangle playAgainButton;
    private Rectangle mainMenuButton;
    private boolean playAgainHovered;
    private boolean mainMenuHovered;

    private GameOverCallback onGameOverCallback;
    private Runnable onMainMenuCallback;

    public interface GameOverCallback {
        void onGameOver(int score, int level);
    }

    public GameEngine() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(5, 5, 16));
        setFocusable(true);

        board = new Board(WIDTH, HEIGHT);
        player = new Player(2, 2, board);
        gameState = new GameState();
        hazards = new ArrayList<>();
        random = new Random();

        running = false;
        gameStarted = false;
        showingGameOver = false;

        beatSound = new BeatSoundEngine();

        int btnW = 220, btnH = 55;
        int btnY = HEIGHT / 2 + 100;
        playAgainButton = new Rectangle((WIDTH / 2) - btnW - 20, btnY, btnW, btnH);
        mainMenuButton  = new Rectangle((WIDTH / 2) + 20,        btnY, btnW, btnH);

        updateBeatInterval();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!showingGameOver) return;
                if (playAgainButton.contains(e.getPoint())) {
                    showingGameOver = false;
                    startGame();
                } else if (mainMenuButton.contains(e.getPoint())) {
                    showingGameOver = false;
                    if (onMainMenuCallback != null) {
                        SwingUtilities.invokeLater(onMainMenuCallback);
                    }
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!showingGameOver) return;
                boolean paH = playAgainButton.contains(e.getPoint());
                boolean mmH = mainMenuButton.contains(e.getPoint());
                if (paH != playAgainHovered || mmH != mainMenuHovered) {
                    playAgainHovered = paH;
                    mainMenuHovered  = mmH;
                    repaint();
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
            }
        });
    }

    private void handleKeyPress(int keyCode) {
        if (!gameStarted) {
            if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_ENTER) {
                startGame();
            }
            return;
        }

        // Allow restart after game over - handled by on-screen buttons now
        if (!running && gameState.isGameOver()) {
            return;
        }

        if (!running) return;

        switch (keyCode) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                player.move(-1, 0);
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                player.move(1, 0);
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                player.move(0, -1);
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                player.move(0, 1);
                break;
            case KeyEvent.VK_M:
                beatSound.setMuted(!beatSound.isMuted());
                repaint();
                break;
        }
    }

    public void startGame() {
        gameStarted = true;
        running = true;
        showingGameOver = false;
        gameState.reset();
        hazards.clear();
        player = new Player(2, 2, board);

        lastBeatTime = System.currentTimeMillis();
        beatTimer = beatInterval;

        beatSound.start(gameState.getBPM());
        beatSound.setLevel(gameState.getLevel());

        new Thread(this).start();
    }

    @Override
    public void run() {
        long frameTime = 1_000_000_000L / FPS;
        long lastTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            long delta = now - lastTime;

            if (delta >= frameTime) {
                update();
                repaint();
                lastTime = now;
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        // Beat timing
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBeatTime >= beatInterval) {
            onBeat();
            lastBeatTime = currentTime;
        }

        // Update player animation
        player.update();

        // Reset all cells first
        board.resetAllCells();

        // POLYMORPHISM: update() called on all Hazard references
        // Java resolves to RowHazard.update(), ColumnHazard.update(), etc.
        for (Hazard h : hazards) {
            h.update();  // ← POLYMORPHISM HERE
        }

        // Remove finished hazards
        hazards.removeIf(Hazard::isFinished);
    }

    private void onBeat() {
        gameState.surviveBeat();

        // Reset player movement for new beat
        //player.resetMove();

        // Update all hazards
        for (int i = hazards.size() - 1; i >= 0; i--) {
            Hazard h = hazards.get(i);

            if (h.isActive()) {
                // Check collision
                if (h.checkHit(player.getGridRow(), player.getGridCol())) {
                    gameState.loseLife();

                    if (gameState.isGameOver()) {
                        running = false;
                        showingGameOver = true;
                        beatSound.stop();
                        // Trigger name dialog + score save via callback
                        if (onGameOverCallback != null) {
                            final int finalScore = gameState.getScore();
                            final int finalLevel = gameState.getLevel();
                            new Thread(() -> {
                                // Small delay so game-over screen renders first
                                try { Thread.sleep(400); } catch (InterruptedException ignored) {}
                                SwingUtilities.invokeLater(() ->
                                        onGameOverCallback.onGameOver(finalScore, finalLevel));
                            }).start();
                        }
                    }
                } else {
                    // Survived!
                    gameState.addScore(100);
                    gameState.incrementCombo();
                }

                h.finish();
            } else {
                h.decrementCountdown();
            }
        }

        // Spawn new patterns MORE FREQUENTLY and MORE AT ONCE
        // Allow up to 5 overlapping hazards (was 3)
        if (hazards.size() < 5) {
            // Spawn EVERY beat at higher levels
            if (gameState.getLevel() >= 3 || random.nextInt(2) == 0) {
                spawnRandomPattern();
            }

            // Chance for DOUBLE spawn at high levels
            if (gameState.getLevel() >= 5 && hazards.size() < 4 && random.nextDouble() < 0.4) {
                spawnRandomPattern();
            }
        }

        // Update BPM based on level
        updateBeatInterval();
    }

    private void spawnRandomPattern() {
        double chance = random.nextDouble();
        int gridSize = board.getGridSize();
        // HARDER: Less warning time
        int countdown = gameState.getBPM() > 140 ? 1 : 2; // Was 2:3, now 1:2

        if (chance < 0.2) {
            // Row hazard
            int row = random.nextInt(gridSize);
            hazards.add(new RowHazard(board, row, countdown));
        } else if (chance < 0.4) {
            // Column hazard
            int col = random.nextInt(gridSize);
            hazards.add(new ColumnHazard(board, col, countdown));
        } else if (chance < 0.55) {
            // Diagonal hazard
            boolean topLeft = random.nextBoolean();
            hazards.add(new DiagonalHazard(board, topLeft, countdown));
        } else if (chance < 0.70) {
            // X shape (both diagonals)
            hazards.add(new XShapeHazard(board, countdown));
        } else if (chance < 0.80) {
            // Four corners
            hazards.add(new CornersHazard(board, countdown));
        } else if (chance < 0.88) {
            // L-shape
            hazards.add(new LShapeHazard(board, countdown));
        } else if (chance < 0.94) {
            // Border (entire edge)
            hazards.add(new BorderHazard(board, countdown));
        } else {
            // Checkerboard (hard!)
            boolean startEven = random.nextBoolean();
            hazards.add(new CheckerHazard(board, startEven, countdown + 1)); // Still gets +1
        }
    }

    private void updateBeatInterval() {
        int bpm   = gameState.getBPM();
        int level = gameState.getLevel();
        beatInterval = 60000 / bpm;
        if (beatSound != null && running) {
            beatSound.setBpm(bpm);
            beatSound.setLevel(level);
        }
    }

    public void applyTheme(SettingsPanel.BoardStyle style) {
        board.setTheme(style);
    }

    public void setOnGameOver(GameOverCallback callback) {
        this.onGameOverCallback = callback;
    }

    public void setOnMainMenu(Runnable callback) {
        this.onMainMenuCallback = callback;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!gameStarted) {
            drawStartScreen(g2d);
            return;
        }

        // Draw game
        board.render(g2d);
        player.render(g2d);

        // Draw HUD
        drawHUD(g2d);

        if (!running && gameState.isGameOver()) {
            drawGameOver(g2d);
        }
    }

    private void drawStartScreen(Graphics2D g) {
        g.setFont(new Font("Monospaced", Font.BOLD, 48));
        g.setColor(new Color(0, 255, 204));
        String title = "GRID BEAT";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, HEIGHT / 2 - 50);

        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g.setColor(new Color(129, 140, 248));
        String sub = "SURVIVE THE RHYTHM · MOVE OR DIE";
        fm = g.getFontMetrics();
        g.drawString(sub, (WIDTH - fm.stringWidth(sub)) / 2, HEIGHT / 2);

        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.setColor(new Color(0, 255, 204));
        String start = "PRESS SPACE TO START";
        fm = g.getFontMetrics();
        g.drawString(start, (WIDTH - fm.stringWidth(start)) / 2, HEIGHT / 2 + 60);

        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g.setColor(new Color(255, 255, 255, 100));
        String controls = "ARROW KEYS / WASD TO MOVE";
        fm = g.getFontMetrics();
        g.drawString(controls, (WIDTH - fm.stringWidth(controls)) / 2, HEIGHT / 2 + 100);
    }

    private void drawHUD(Graphics2D g) {
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        g.setColor(new Color(251, 191, 36));

        // Score
        g.drawString("SCORE: " + gameState.getScore(), 20, 30);

        // Combo
        g.drawString("COMBO: x" + gameState.getCombo(), 20, 55);

        // Lives
        g.drawString("LIVES: ", 20, 80);
        for (int i = 0; i < gameState.getLives(); i++) {
            g.setColor(new Color(0, 255, 204));
            g.fillOval(90 + i * 20, 68, 12, 12);
        }

        // Level
        g.setColor(new Color(251, 191, 36));
        g.drawString("LEVEL: " + gameState.getLevel(), WIDTH - 130, 30);

        // BPM
        g.drawString("BPM: " + gameState.getBPM(), WIDTH - 130, 55);

        // Mute indicator
        if (beatSound.isMuted()) {
            g.setColor(new Color(239, 68, 68));
            g.drawString("♪ MUTED [M]", WIDTH - 150, 80);
        } else {
            g.setColor(new Color(0, 255, 204, 120));
            g.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g.drawString("[M] mute", WIDTH - 110, 80);
        }

        /*
 Move status indicator
        if (player.canMove()) {
            g.setColor(new Color(0, 255, 204));
            g.drawString("MOVE: READY", WIDTH - 150, 80);
        } else {
            g.setColor(new Color(100, 100, 150));
            g.drawString("MOVE: USED", WIDTH - 150, 80);
        }
*/
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(5, 5, 16, 230));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setFont(new Font("Monospaced", Font.BOLD, 56));
        g.setColor(new Color(239, 68, 68));
        String gameOver = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(gameOver, (WIDTH - fm.stringWidth(gameOver)) / 2, HEIGHT / 2 - 60);

        g.setFont(new Font("Monospaced", Font.BOLD, 24));
        g.setColor(new Color(251, 191, 36));
        String score = "FINAL SCORE: " + gameState.getScore();
        fm = g.getFontMetrics();
        g.drawString(score, (WIDTH - fm.stringWidth(score)) / 2, HEIGHT / 2);

        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g.setColor(new Color(129, 140, 248));
        String levelStr = "LEVEL REACHED: " + gameState.getLevel();
        fm = g.getFontMetrics();
        g.drawString(levelStr, (WIDTH - fm.stringWidth(levelStr)) / 2, HEIGHT / 2 + 40);

        // PLAY AGAIN button
        drawGameOverButton(g, playAgainButton, "PLAY AGAIN", playAgainHovered);
        // MAIN MENU button
        drawGameOverButton(g, mainMenuButton, "MAIN MENU", mainMenuHovered);
    }

    private void drawGameOverButton(Graphics2D g, Rectangle btn, String label, boolean hovered) {
        if (hovered) {
            g.setColor(new Color(0, 255, 204, 40));
            g.fillRoundRect(btn.x, btn.y, btn.width, btn.height, 10, 10);
        }
        g.setColor(hovered ? new Color(0, 255, 204) : new Color(129, 140, 248));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(btn.x, btn.y, btn.width, btn.height, 10, 10);

        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.setColor(hovered ? new Color(0, 255, 204) : new Color(226, 232, 240));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label,
                btn.x + (btn.width - fm.stringWidth(label)) / 2,
                btn.y + (btn.height + fm.getAscent()) / 2 - 5);
    }

    public int getWidth() { return WIDTH; }
    public int getHeight() { return HEIGHT; }
}