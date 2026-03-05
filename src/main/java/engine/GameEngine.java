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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * CLASS: GameEngine
 * Main game controller - manages game loop, timing, patterns, collisions
 *
 * POLYMORPHISM DEMO:
 * - List<Hazard> stores RowHazard, ColumnHazard, SpotHazard, CrossHazard
 * - Calls update() on all → Java resolves to correct subclass at runtime
 *
 * <p>Extends {@link JPanel} and implements {@link Runnable} to drive a
 * fixed-timestep loop on a background thread targeting {@value #FPS} FPS.
 * Coordinates the {@link Board}, {@link Player}, active {@link Hazard} patterns,
 * {@link GameState}, and {@link BeatSoundEngine}.</p>
 *
 * <p>Navigation callbacks are wired by the owner ({@link GameLauncher}) via
 * {@link #setOnGameOver(GameOverCallback)} and {@link #setOnMainMenu(Runnable)}.</p>
 *
 * @author Project Team
 * @version 1.0
 * @see GameState
 * @see BeatSoundEngine
 */
public class GameEngine extends JPanel implements Runnable {

    /** Preferred panel width in pixels. */
    private static final int WIDTH = 800;

    /** Preferred panel height in pixels. */
    private static final int HEIGHT = 700;

    /** Target frame rate for the game loop. */
    private static final int FPS = 60;

    /** The game grid that hazard patterns operate on. */
    private Board board;

    /** The player-controlled avatar. */
    private Player player;

    /** Tracks score, combo, lives, and level. */
    private GameState gameState;

    /** All currently active hazard patterns. Polymorphically updated each frame. */
    private List<Hazard> hazards;

    /**
     * Tracks which hazards have already dealt damage during their current active
     * phase so a single hazard activation can only hurt the player once, even
     * though collision is checked every frame at 60 FPS.
     */
    private Set<Hazard> hazardsHitThisCycle;

    /** Random number generator used for hazard pattern selection and placement. */
    private Random random;

    /** {@code true} while the game loop thread is running. */
    private boolean running;

    /** Timestamp (ms) of the last beat event, used to compute beat timing. */
    private long lastBeatTime;

    /** Frame counter used for beat interpolation (currently unused). */
    private int beatTimer;

    /** Milliseconds between beats, derived from the current BPM. */
    private int beatInterval;

    /** Procedural background music and sound-effect engine. */
    private BeatSoundEngine beatSound;

    /** {@code true} once the player has started the first game session. */
    private boolean gameStarted;

    /** {@code true} while the game-over overlay is being displayed. */
    private boolean showingGameOver;

    /** When {@code true} the game loop skips {@link #update()} but still repaints. */
    private volatile boolean paused;

    // Game over screen buttons
    /** Bounding rectangle of the "Play Again" button on the game-over screen. */
    private Rectangle playAgainButton;

    /** Bounding rectangle of the "Main Menu" button on the game-over screen. */
    private Rectangle mainMenuButton;

    /** {@code true} when the mouse is hovering over the "Play Again" button. */
    private boolean playAgainHovered;

    /** {@code true} when the mouse is hovering over the "Main Menu" button. */
    private boolean mainMenuHovered;

    /** Callback invoked (on EDT) when the player loses all lives. */
    private GameOverCallback onGameOverCallback;

    /** Callback invoked when the player navigates back to the main menu. */
    private Runnable onMainMenuCallback;

    /**
     * Functional interface for game-over event notifications.
     * Receives the final score and highest level reached.
     */
    public interface GameOverCallback {
        /**
         * Called after a short delay once the game-over state is entered.
         *
         * @param score final accumulated score
         * @param level highest difficulty level reached
         */
        void onGameOver(int score, int level);
    }

    /**
     * Constructs and fully initialises a new {@code GameEngine}.
     *
     * <p>Creates the {@link Board}, {@link Player}, {@link GameState}, and
     * {@link BeatSoundEngine}; positions the game-over overlay buttons; and
     * registers key and mouse listeners. Does <em>not</em> start the game loop
     * — call {@link #startGame()} for that.</p>
     */
    public GameEngine() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(5, 5, 16));
        setFocusable(true);

        board = new Board(WIDTH, HEIGHT);
        player = new Player(2, 2, board);
        gameState = new GameState();
        hazards = new ArrayList<>();
        hazardsHitThisCycle = new HashSet<>();
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

    /**
     * Handles all keyboard input for the game.
     *
     * <p>Before a game starts: Space or Enter starts the game.
     * During play: arrow keys and WASD move the player; Escape toggles pause;
     * M toggles audio mute. All inputs except Escape are ignored while paused.</p>
     *
     * @param keyCode the {@link KeyEvent} key code of the pressed key
     */
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

        if (keyCode == KeyEvent.VK_ESCAPE) {
            togglePause();
            return;
        }

        if (paused) return;

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

    /**
     * Resets all game state and starts a new game session.
     *
     * <p>Clears existing hazards, repositions the player at (2, 2), resets
     * {@link GameState}, starts the {@link BeatSoundEngine}, and spawns the
     * game-loop thread. Safe to call multiple times (e.g. "Play Again").</p>
     */
    public void startGame() {
        gameStarted = true;
        running = true;
        showingGameOver = false;
        paused = false;
        gameState.reset();
        hazards.clear();
        hazardsHitThisCycle.clear();
        player = new Player(2, 2, board);

        lastBeatTime = System.currentTimeMillis();
        beatTimer = beatInterval;

        beatSound.start(gameState.getBPM());
        beatSound.setLevel(gameState.getLevel());

        new Thread(this).start();
    }

    /**
     * Toggles the pause state. When pausing, the music is muted and the beat
     * timer is suspended; when resuming, the beat timer reference is reset to
     * the current time to prevent a beat from firing immediately.
     *
     * <p>Has no effect if no game is in progress or the game-over screen is showing.</p>
     */
    private void togglePause() {
        if (!gameStarted || showingGameOver || gameState.isGameOver()) return;
        paused = !paused;
        beatSound.setMuted(paused);
        if (!paused) lastBeatTime = System.currentTimeMillis();
        repaint();
    }

    /**
     * Fixed-timestep game loop body, executed on the game-loop thread.
     *
     * <p>Targets {@value #FPS} frames per second using nanosecond timing.
     * Calls {@link #update()} (unless paused) and {@link #repaint()} each frame.
     * Sleeps 1 ms between checks to avoid busy-waiting.</p>
     */
    @Override
    public void run() {
        long frameTime = 1_000_000_000L / FPS;
        long lastTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            long delta = now - lastTime;

            if (delta >= frameTime) {
                if (!paused) update();
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

    /**
     * Advances the game state by one frame.
     *
     * <p>Checks whether a beat interval has elapsed and fires {@link #onBeat()} if so.
     * Updates the player animation, resets all board cells, then polymorphically
     * updates all active hazards and removes finished ones.</p>
     */
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

        // Update warning hazards first, then active hazards.
        // This guarantees that setActive() always has the final say on any cell
        // that is targeted by both a warning hazard and an active hazard in the
        // same frame — active state must visually win.
        for (Hazard h : hazards) {
            if (!h.isActive()) h.update();
        }
        for (Hazard h : hazards) {
            if (h.isActive()) h.update();
        }

        // Real-time collision: check every frame so damage triggers the instant
        // the player steps onto an active hazard tile, not only on the beat.
        for (Hazard h : hazards) {
            if (h.isActive() && !hazardsHitThisCycle.contains(h)) {
                if (h.checkHit(player.getGridRow(), player.getGridCol())) {
                    hazardsHitThisCycle.add(h); // mark so this hazard can't hit again
                    applyPlayerHit();
                }
            }
        }

        // Remove finished hazards and clear them from the hit-tracking set
        hazards.removeIf(h -> {
            if (h.isFinished()) { hazardsHitThisCycle.remove(h); return true; }
            return false;
        });
    }

    /**
     * Processes a single beat event.
     *
     * <p>Increments the survived-beat counter in {@link GameState}, checks for
     * player collision with each active hazard, awards score for successful dodges,
     * triggers the game-over sequence if lives reach zero, and spawns new hazard
     * patterns. Also updates the beat interval via {@link #updateBeatInterval()}.</p>
     */
    private void onBeat() {
        gameState.surviveBeat();

        // Update all hazards — lifecycle only (collision is handled real-time in update())
        for (int i = hazards.size() - 1; i >= 0; i--) {
            Hazard h = hazards.get(i);

            if (h.isActive()) {
                // Award score for surviving an active hazard that didn't hit the player
                if (!hazardsHitThisCycle.contains(h)) {
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

    /**
     * Applies one hit to the player: decrements a life, plays the hurt sound,
     * and triggers the game-over sequence if lives reach zero.
     * Called from the real-time collision check in {@link #update()}.
     */
    private void applyPlayerHit() {
        gameState.loseLife();
        beatSound.playHurtSound();

        if (gameState.isGameOver()) {
            running = false;
            showingGameOver = true;
            beatSound.playGameOverSound();
            beatSound.stop();
            if (onGameOverCallback != null) {
                final int finalScore = gameState.getScore();
                final int finalLevel = gameState.getLevel();
                new Thread(() -> {
                    try { Thread.sleep(400); } catch (InterruptedException ignored) {}
                    SwingUtilities.invokeLater(() ->
                            onGameOverCallback.onGameOver(finalScore, finalLevel));
                }).start();
            }
        }
    }

    /**
     * Selects a random hazard pattern based on weighted probabilities and adds
     * it to the active hazard list.
     *
     * <p>Countdown length is reduced to 1 beat at high BPM (&gt;140) to increase
     * difficulty; otherwise 2 beats are given as warning. CheckerHazard always
     * receives one additional beat of warning.</p>
     */
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

    /**
     * Recalculates the beat interval in milliseconds from the current BPM and
     * notifies the {@link BeatSoundEngine} of the new tempo and level.
     */
    private void updateBeatInterval() {
        int bpm   = gameState.getBPM();
        int level = gameState.getLevel();
        beatInterval = 60000 / bpm;
        if (beatSound != null && running) {
            beatSound.setBpm(bpm);
            beatSound.setLevel(level);
        }
    }

    /**
     * Applies a visual theme to the game board by delegating to {@link Board#setTheme}.
     *
     * @param style the {@link SettingsPanel.BoardStyle} to apply; must not be {@code null}
     */
    public void applyTheme(SettingsPanel.BoardStyle style) {
        board.setTheme(style);
    }

    /**
     * Applies the given volume level to this engine's {@link BeatSoundEngine} and
     * registers it with {@link SoundManager} so in-game sounds reflect the value
     * the player set in the settings screen.
     *
     * @param volume volume in [0.0, 1.0]; clamped by {@link BeatSoundEngine}
     */
    public void applyVolume(float volume) {
        beatSound.setVolume(volume);
        SoundManager.register(beatSound);
    }

    /**
     * Registers a callback to be invoked on the EDT when the player loses all lives.
     *
     * @param callback receives the final score and level; pass {@code null} to clear
     */
    public void setOnGameOver(GameOverCallback callback) {
        this.onGameOverCallback = callback;
    }

    /**
     * Registers a callback to be invoked on the EDT when the player navigates
     * back to the main menu via the game-over screen button.
     *
     * @param callback the Runnable to execute; pass {@code null} to clear
     */
    public void setOnMainMenu(Runnable callback) {
        this.onMainMenuCallback = callback;
    }

    /**
     * Returns the internal BeatSoundEngine so the launcher can register it
     * with SoundManager when switching into the game screen.
     */
    public BeatSoundEngine getSoundEngine() {
        return beatSound;
    }

    /**
     * Paints the panel. Dispatches to one of the overlay drawing methods depending
     * on the current game state: start screen, active gameplay (with optional
     * pause overlay), or game-over overlay.
     *
     * @param g the Swing {@link Graphics} context provided by the repaint cycle
     */
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

        if (paused) drawPauseOverlay(g2d);

        if (!running && gameState.isGameOver()) {
            drawGameOver(g2d);
        }
    }

    /**
     * Draws the pre-game start screen showing the title, tagline, and controls hint.
     *
     * @param g the {@link Graphics2D} context
     */
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

    /**
     * Draws the heads-up display: score, combo multiplier, life indicator dots,
     * current level, BPM, and mute status.
     *
     * @param g the {@link Graphics2D} context
     */
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

    /**
     * Draws the game-over overlay with the final score, level reached, and the
     * "Play Again" and "Main Menu" action buttons.
     *
     * @param g the {@link Graphics2D} context
     */
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

    /**
     * Draws a single rounded-rectangle button on the game-over overlay.
     *
     * @param g       the {@link Graphics2D} context
     * @param btn     bounding rectangle of the button
     * @param label   text to display centred in the button
     * @param hovered {@code true} if the mouse is currently over this button
     */
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

    /**
     * Draws a semi-transparent pause overlay with a "PAUSED" title and resume hint.
     *
     * @param g the {@link Graphics2D} context
     */
    private void drawPauseOverlay(Graphics2D g) {
        g.setColor(new Color(5, 5, 16, 210));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setFont(new Font("Monospaced", Font.BOLD, 64));
        g.setColor(new Color(0, 255, 204));
        String title = "PAUSED";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, HEIGHT / 2 - 20);

        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g.setColor(new Color(129, 140, 248));
        String resume = "PRESS ESC TO RESUME";
        fm = g.getFontMetrics();
        g.drawString(resume, (WIDTH - fm.stringWidth(resume)) / 2, HEIGHT / 2 + 35);

        g.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g.setColor(new Color(255, 255, 255, 70));
        String hint = "WASD / ARROWS TO MOVE  ·  M TO MUTE";
        fm = g.getFontMetrics();
        g.drawString(hint, (WIDTH - fm.stringWidth(hint)) / 2, HEIGHT / 2 + 72);
    }

    /**
     * Returns the preferred panel width.
     *
     * @return {@value #WIDTH} pixels
     */
    public int getWidth() { return WIDTH; }

    /**
     * Returns the preferred panel height.
     *
     * @return {@value #HEIGHT} pixels
     */
    public int getHeight() { return HEIGHT; }
}