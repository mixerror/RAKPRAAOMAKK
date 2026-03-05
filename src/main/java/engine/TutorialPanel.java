package engine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Tutorial screen that teaches players how to play Grid Beat.
 *
 * <p>Shows game rules, controls, and hazard types with visual examples.</p>
 *
 * @author Your Name
 * @version 1.0
 */
public class TutorialPanel extends JPanel {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 700;

    private int currentPage;
    private static final int TOTAL_PAGES = 3;

    private Rectangle nextButton;
    private Rectangle prevButton;
    private Rectangle skipButton;

    private Runnable onComplete;
    private Runnable onMainMenu;

    public TutorialPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(5, 5, 16));

        currentPage = 0;
        nextButton = new Rectangle(550, 600, 200, 50);
        prevButton = new Rectangle(50, 600, 200, 50);
        skipButton = new Rectangle(WIDTH - 180, 30, 150, 40);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (nextButton.contains(e.getPoint())) {
                    SoundManager.click();
                    if (currentPage < TOTAL_PAGES - 1) {
                        currentPage++;
                        repaint();
                    } else {
                        // Last page - go to game
                        if (onComplete != null) onComplete.run();
                    }
                } else if (prevButton.contains(e.getPoint()) && currentPage > 0) {
                    SoundManager.click();
                    currentPage--;
                    repaint();
                } else if (skipButton.contains(e.getPoint())) {
                    SoundManager.click();
                    // Skip goes back to main menu, NOT into the game
                    if (onMainMenu != null) onMainMenu.run();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw current page
        switch (currentPage) {
            case 0:
                drawPage1(g2d);
                break;
            case 1:
                drawPage2(g2d);
                break;
            case 2:
                drawPage3(g2d);
                break;
        }

        // Draw navigation buttons
        drawButtons(g2d);

        // Page indicator
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2d.setColor(new Color(129, 140, 248));
        String pageNum = (currentPage + 1) + " / " + TOTAL_PAGES;
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(pageNum, (WIDTH - fm.stringWidth(pageNum)) / 2, 670);
    }

    private void drawPage1(Graphics2D g) {
        // Title
        g.setFont(new Font("Monospaced", Font.BOLD, 42));
        g.setColor(new Color(0, 255, 204));
        String title = "HOW TO PLAY";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, 100);

        // Objective
        g.setFont(new Font("Monospaced", Font.BOLD, 24));
        g.setColor(new Color(251, 191, 36));
        g.drawString("OBJECTIVE:", 150, 180);

        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g.setColor(new Color(226, 232, 240));
        g.drawString("Survive dangerous patterns on the grid", 150, 220);
        g.drawString("Move to the beat and avoid hazards", 150, 250);
        g.drawString("Build combos for higher scores!", 150, 280);

        // Controls
        g.setFont(new Font("Monospaced", Font.BOLD, 24));
        g.setColor(new Color(251, 191, 36));
        g.drawString("CONTROLS:", 150, 350);

        // Draw keyboard visual
        int keySize = 50;
        int keyX = 300;
        int keyY = 380;

        // Draw arrow keys
        drawKey(g, "↑", keyX + keySize, keyY);
        drawKey(g, "←", keyX, keyY + keySize);
        drawKey(g, "↓", keyX + keySize, keyY + keySize);
        drawKey(g, "→", keyX + keySize * 2, keyY + keySize);

        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.setColor(new Color(129, 140, 248));
        g.drawString("OR", keyX + keySize * 4, keyY + keySize);

        // Draw WASD keys
        int wasdX = keyX + keySize * 5 + 20;
        drawKey(g, "W", wasdX + keySize, keyY);
        drawKey(g, "A", wasdX, keyY + keySize);
        drawKey(g, "S", wasdX + keySize, keyY + keySize);
        drawKey(g, "D", wasdX + keySize * 2, keyY + keySize);

        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g.setColor(new Color(226, 232, 240));
        g.drawString("Move between grid squares", 230, 520);
    }

    private void drawPage2(Graphics2D g) {
        // Title
        g.setFont(new Font("Monospaced", Font.BOLD, 42));
        g.setColor(new Color(0, 255, 204));
        String title = "HAZARD PHASES";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, 100);

        int startY = 180;
        int spacing = 160;

        // Phase 1 - Warning
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.setColor(new Color(124, 58, 237));
        g.drawString("1. WARNING PHASE", 150, startY);

        // Draw example warning tile
        int tileSize = 60;
        int tileX = 550;
        g.setColor(new Color(124, 58, 237, 65));
        g.fillRoundRect(tileX, startY - 40, tileSize, tileSize, 8, 8);
        g.setColor(new Color(124, 58, 237));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(tileX, startY - 40, tileSize, tileSize, 8, 8);
        g.setFont(new Font("Monospaced", Font.BOLD, 28));
        g.setColor(new Color(245, 158, 11));
        g.drawString("3", tileX + 22, startY - 2);

        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.setColor(new Color(226, 232, 240));
        g.drawString("Orange tiles with countdown", 150, startY + 30);
        g.drawString("GET READY TO MOVE!", 150, startY + 55);

        // Phase 2 - Active
        startY += spacing;
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.setColor(new Color(239, 68, 68));
        g.drawString("2. ACTIVE PHASE", 150, startY);

        // Draw example active tile
        g.setColor(new Color(239, 68, 68, 90));
        g.fillRoundRect(tileX, startY - 40, tileSize, tileSize, 8, 8);
        g.setColor(new Color(239, 68, 68));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(tileX, startY - 40, tileSize, tileSize, 8, 8);
        g.drawLine(tileX + 15, startY - 25, tileX + tileSize - 15, startY + 10);
        g.drawLine(tileX + tileSize - 15, startY - 25, tileX + 15, startY + 10);

        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.setColor(new Color(226, 232, 240));
        g.drawString("Red X marks - DANGER!", 150, startY + 30);
        g.drawString("DON'T BE ON THESE TILES!", 150, startY + 55);

        // Phase 3 - Safe
        startY += spacing;
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.setColor(new Color(52, 211, 153));
        g.drawString("3. SAFE AGAIN", 150, startY);

        // Draw example normal tile
        g.setColor(new Color(17, 17, 40));
        g.fillRoundRect(tileX, startY - 40, tileSize, tileSize, 8, 8);
        g.setColor(new Color(26, 26, 58));
        g.setStroke(new BasicStroke(1));
        g.drawRoundRect(tileX, startY - 40, tileSize, tileSize, 8, 8);

        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.setColor(new Color(226, 232, 240));
        g.drawString("Hazard disappears", 150, startY + 30);
        g.drawString("You survived - combo increased!", 150, startY + 55);
    }

    private void drawPage3(Graphics2D g) {
        // Title
        g.setFont(new Font("Monospaced", Font.BOLD, 42));
        g.setColor(new Color(0, 255, 204));
        String title = "GAME TIPS";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, 100);

        int tipY = 180;
        int spacing = 90;

        // Tip 1
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.setColor(new Color(251, 191, 36));
        g.drawString("★ WATCH THE COUNTDOWN", 150, tipY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.setColor(new Color(226, 232, 240));
        g.drawString("Numbers show how many beats until danger", 150, tipY + 30);
        g.drawString("Plan your escape route early!", 150, tipY + 55);

        // Tip 2
        tipY += spacing;
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.setColor(new Color(251, 191, 36));
        g.drawString("★ BUILD YOUR COMBO", 150, tipY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.setColor(new Color(226, 232, 240));
        g.drawString("Survive hazards to increase combo multiplier", 150, tipY + 30);
        g.drawString("Higher combo = more points! (max x8)", 150, tipY + 55);

        // Tip 3
        tipY += spacing;
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.setColor(new Color(251, 191, 36));
        g.drawString("★ GAME GETS HARDER", 150, tipY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.setColor(new Color(226, 232, 240));
        g.drawString("BPM increases each level - stay focused!", 150, tipY + 30);
        g.drawString("Multiple patterns can overlap", 150, tipY + 55);

        // Tip 4
        tipY += spacing;
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.setColor(new Color(251, 191, 36));
        g.drawString("★ YOU HAVE 3 LIVES", 150, tipY);
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g.setColor(new Color(226, 232, 240));
        g.drawString("Getting hit loses 1 life and resets combo", 150, tipY + 30);
        g.drawString("Game over when all lives are lost", 150, tipY + 55);
    }

    private void drawKey(Graphics2D g, String label, int x, int y) {
        int size = 50;

        // Key background
        g.setColor(new Color(30, 30, 50));
        g.fillRoundRect(x, y, size, size, 8, 8);

        // Key border
        g.setColor(new Color(129, 140, 248));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, size, size, 8, 8);

        // Key label
        g.setFont(new Font("Monospaced", Font.BOLD, 24));
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (size - fm.stringWidth(label)) / 2;
        int textY = y + (size + fm.getAscent()) / 2 - 5;
        g.drawString(label, textX, textY);
    }

    private void drawButtons(Graphics2D g) {
        // Skip button (top right)
        g.setColor(new Color(129, 140, 248, 100));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(skipButton.x, skipButton.y, skipButton.width, skipButton.height, 6, 6);

        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g.setColor(new Color(129, 140, 248));
        String skipText = "MAINMENU";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(skipText,
                skipButton.x + (skipButton.width - fm.stringWidth(skipText)) / 2,
                skipButton.y + (skipButton.height + fm.getAscent()) / 2 - 3);

        // Previous button
        if (currentPage > 0) {
            g.setColor(new Color(129, 140, 248));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(prevButton.x, prevButton.y, prevButton.width, prevButton.height, 8, 8);

            g.setFont(new Font("Monospaced", Font.BOLD, 18));
            String prevText = "← PREVIOUS";
            fm = g.getFontMetrics();
            g.drawString(prevText,
                    prevButton.x + (prevButton.width - fm.stringWidth(prevText)) / 2,
                    prevButton.y + (prevButton.height + fm.getAscent()) / 2 - 5);
        }

        // Next/Start button
        g.setColor(new Color(0, 255, 204));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(nextButton.x, nextButton.y, nextButton.width, nextButton.height, 8, 8);

        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        String nextText = currentPage < TOTAL_PAGES - 1 ? "NEXT →" : "START GAME";
        fm = g.getFontMetrics();
        g.drawString(nextText,
                nextButton.x + (nextButton.width - fm.stringWidth(nextText)) / 2,
                nextButton.y + (nextButton.height + fm.getAscent()) / 2 - 5);
    }

    public void reset() {
        currentPage = 0;
    }

    public void setOnComplete(Runnable callback) {
        this.onComplete = callback;
    }

    public void setOnMainMenu(Runnable callback) {
        this.onMainMenu = callback;
    }
}