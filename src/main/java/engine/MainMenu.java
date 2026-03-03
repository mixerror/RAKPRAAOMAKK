package engine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * CLASS: MainMenu
 * Main menu screen with start game, scoreboard, settings, and quit buttons
 */
public class MainMenu extends JPanel {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 700;

    private List<MenuButton> buttons;
    private Runnable onStartGame;
    private Runnable onShowScoreboard;
    private Runnable onShowSettings;
    private Runnable onQuit;

    public MainMenu() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(5, 5, 16));
        setLayout(null);

        buttons = new ArrayList<>();

        // Create buttons
        int buttonWidth = 300;
        int buttonHeight = 60;
        int centerX = (WIDTH - buttonWidth) / 2;
        int startY = 320;
        int spacing = 80;

        MenuButton startBtn = new MenuButton("START GAME", centerX, startY, buttonWidth, buttonHeight);
        MenuButton scoreBtn = new MenuButton("SCOREBOARD", centerX, startY + spacing, buttonWidth, buttonHeight);
        MenuButton settingsBtn = new MenuButton("SETTINGS", centerX, startY + spacing * 2, buttonWidth, buttonHeight);
        MenuButton quitBtn = new MenuButton("QUIT", centerX, startY + spacing * 3, buttonWidth, buttonHeight);

        buttons.add(startBtn);
        buttons.add(scoreBtn);
        buttons.add(settingsBtn);
        buttons.add(quitBtn);

        // Add mouse listeners
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (MenuButton btn : buttons) {
                    if (btn.contains(e.getX(), e.getY())) {
                        handleButtonClick(btn);
                    }
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean needsRepaint = false;
                for (MenuButton btn : buttons) {
                    boolean wasHovered = btn.isHovered();
                    btn.setHovered(btn.contains(e.getX(), e.getY()));
                    if (wasHovered != btn.isHovered()) {
                        needsRepaint = true;
                    }
                }
                if (needsRepaint) {
                    repaint();
                }
            }
        });
    }

    private void handleButtonClick(MenuButton btn) {
        SoundManager.click();
        switch (btn.getText()) {
            case "START GAME":
                if (onStartGame != null) onStartGame.run();
                break;
            case "SCOREBOARD":
                if (onShowScoreboard != null) onShowScoreboard.run();
                break;
            case "SETTINGS":
                if (onShowSettings != null) onShowSettings.run();
                break;
            case "QUIT":
                if (onQuit != null) onQuit.run();
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw title
        g2d.setFont(new Font("Monospaced", Font.BOLD, 64));
        g2d.setColor(new Color(0, 255, 204));
        String title = "GRID BEAT";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, 180);

        // Draw subtitle
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g2d.setColor(new Color(129, 140, 248));
        String subtitle = "SURVIVE THE RHYTHM · MOVE OR DIE";
        fm = g2d.getFontMetrics();
        g2d.drawString(subtitle, (WIDTH - fm.stringWidth(subtitle)) / 2, 220);

        // Draw buttons
        for (MenuButton btn : buttons) {
            btn.render(g2d);
        }
    }

    public void setOnStartGame(Runnable callback) { this.onStartGame = callback; }
    public void setOnShowScoreboard(Runnable callback) { this.onShowScoreboard = callback; }
    public void setOnShowSettings(Runnable callback) { this.onShowSettings = callback; }
    public void setOnQuit(Runnable callback) { this.onQuit = callback; }

    // Inner class for menu buttons
    private static class MenuButton {
        private String text;
        private int x, y, width, height;
        private boolean hovered;

        public MenuButton(String text, int x, int y, int width, int height) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.hovered = false;
        }

        public void render(Graphics2D g) {
            // Button background
            if (hovered) {
                g.setColor(new Color(0, 255, 204, 30));
                g.fillRoundRect(x, y, width, height, 8, 8);
            }

            // Button border
            g.setColor(hovered ? new Color(0, 255, 204) : new Color(129, 140, 248));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(x, y, width, height, 8, 8);

            // Button text
            g.setFont(new Font("Monospaced", Font.BOLD, 22));
            g.setColor(hovered ? new Color(0, 255, 204) : new Color(226, 232, 240));
            FontMetrics fm = g.getFontMetrics();
            int textX = x + (width - fm.stringWidth(text)) / 2;
            int textY = y + (height + fm.getAscent()) / 2 - 5;
            g.drawString(text, textX, textY);
        }

        public boolean contains(int px, int py) {
            return px >= x && px <= x + width && py >= y && py <= y + height;
        }

        public String getText() { return text; }
        public boolean isHovered() { return hovered; }
        public void setHovered(boolean hovered) { this.hovered = hovered; }
    }
}