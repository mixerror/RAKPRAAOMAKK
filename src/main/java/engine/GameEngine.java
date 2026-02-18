package engine;

import entities.Player;
import grid.Board;
import patterns.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
    
    private boolean gameStarted;
    
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
        
        updateBeatInterval();
        
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
        }
    }
    
    public void startGame() {
        gameStarted = true;
        running = true;
        gameState.reset();
        hazards.clear();
        player = new Player(2, 2, board);
        
        lastBeatTime = System.currentTimeMillis();
        beatTimer = beatInterval;
        
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
        
        // Update all hazards
        for (int i = hazards.size() - 1; i >= 0; i--) {
            Hazard h = hazards.get(i);
            
            if (h.isActive()) {
                // Check collision
                if (h.checkHit(player.getGridRow(), player.getGridCol())) {
                    gameState.loseLife();
                    
                    if (gameState.isGameOver()) {
                        running = false;
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
        
        // Spawn new pattern every 2 beats
        if (hazards.size() < 3 && random.nextInt(2) == 0) {
            spawnRandomPattern();
        }
        
        // Update BPM based on level
        updateBeatInterval();
    }
    
    private void spawnRandomPattern() {
        double chance = random.nextDouble();
        int gridSize = board.getGridSize();
        int countdown = gameState.getBPM() > 150 ? 2 : 3;
        
        if (chance < 0.3) {
            // Row hazard
            int row = random.nextInt(gridSize);
            hazards.add(new RowHazard(board, row, countdown));
        } else if (chance < 0.6) {
            // Column hazard
            int col = random.nextInt(gridSize);
            hazards.add(new ColumnHazard(board, col, countdown));
        }
//        else if (chance < 0.85) {
//            // Spot hazards
//            int count = 2 + random.nextInt(3);
//            hazards.add(new SpotHazard(board, count, countdown));
//        } else {
//            // Cross hazard
//            int row = random.nextInt(gridSize);
//            int col = random.nextInt(gridSize);
//            hazards.add(new CrossHazard(board, row, col, countdown));
//        }
    }
    
    private void updateBeatInterval() {
        int bpm = gameState.getBPM();
        beatInterval = 60000 / bpm;
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
    }
    
    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(5, 5, 16, 230));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        g.setFont(new Font("Monospaced", Font.BOLD, 56));
        g.setColor(new Color(239, 68, 68));
        String gameOver = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(gameOver, (WIDTH - fm.stringWidth(gameOver)) / 2, HEIGHT / 2 - 40);
        
        g.setFont(new Font("Monospaced", Font.BOLD, 24));
        g.setColor(new Color(251, 191, 36));
        String score = "FINAL SCORE: " + gameState.getScore();
        fm = g.getFontMetrics();
        g.drawString(score, (WIDTH - fm.stringWidth(score)) / 2, HEIGHT / 2 + 20);
        
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.setColor(new Color(129, 140, 248));
        String restart = "Press SPACE to restart";
        fm = g.getFontMetrics();
        g.drawString(restart, (WIDTH - fm.stringWidth(restart)) / 2, HEIGHT / 2 + 70);
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("GRID BEAT");
        GameEngine game = new GameEngine();
        
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
