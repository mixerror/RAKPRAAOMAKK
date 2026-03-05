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
 *
 * <p>Renders a custom-painted gradient background with four navigation buttons.
 * Mouse hover states are tracked per button and reflected visually on each repaint.
 * Navigation actions are dispatched via {@link Runnable} callbacks registered by
 * the owning {@link GameLauncher}.</p>
 *
 * @author Project Team
 * @version 1.0
 */
public class MainMenu extends JPanel {

    /** Preferred panel width in pixels. */
    private static final int WIDTH = 800;

    /** Preferred panel height in pixels. */
    private static final int HEIGHT = 700;

    /** All four navigation buttons, rendered and hit-tested in order. */
    private List<MenuButton> buttons;

    /** Callback fired when the player clicks "Start Game". */
    private Runnable onStartGame;

    /** Callback fired when the player clicks "HOW TO PLAY". */
    private Runnable onShowTutorial;

    /** Callback fired when the player clicks "Scoreboard". */
    private Runnable onShowScoreboard;

    /** Callback fired when the player clicks "Settings". */
    private Runnable onShowSettings;

    /** Callback fired when the player clicks "Quit". */
    private Runnable onQuit;

    /**
     * Constructs a new {@code MainMenu}: sets the preferred size and background,
     * creates the four navigation buttons at evenly spaced vertical positions,
     * and registers mouse click and motion listeners.
     */
    public MainMenu() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(5, 5, 16));
        setLayout(null);

        buttons = new ArrayList<>();

        // Create buttons
        int buttonWidth = 300;
        int buttonHeight = 60;
        int centerX = (WIDTH - buttonWidth) / 2;
        int startY = 280;
        int spacing = 80;

        MenuButton startBtn = new MenuButton("START GAME", centerX, startY, buttonWidth, buttonHeight);
        MenuButton tutorialBtn = new MenuButton("HOW TO PLAY", centerX, startY + spacing, buttonWidth, buttonHeight);
        MenuButton scoreBtn = new MenuButton("SCOREBOARD", centerX, startY + spacing * 2, buttonWidth, buttonHeight);
        MenuButton settingsBtn = new MenuButton("SETTINGS", centerX, startY + spacing * 3, buttonWidth, buttonHeight);
        MenuButton quitBtn = new MenuButton("QUIT", centerX, startY + spacing * 4, buttonWidth, buttonHeight);

        buttons.add(startBtn);
        buttons.add(tutorialBtn);
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

    /**
     * Plays a click sound and fires the appropriate navigation callback for the
     * supplied button.
     *
     * @param btn the button that was clicked; its text is used to dispatch the action
     */
    private void handleButtonClick(MenuButton btn) {
        switch (btn.getText()) {
            case "START GAME":
                if (onStartGame != null) onStartGame.run();
                break;
            case "HOW TO PLAY":
                if (onShowTutorial != null) onShowTutorial.run();
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

    /**
     * Paints the menu: dark background, centred game title and subtitle, then
     * all navigation buttons via their own {@link MenuButton#render(Graphics2D)} methods.
     *
     * @param g the Swing {@link Graphics} context
     */
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

    /**
     * Registers the callback invoked when the player clicks "Start Game".
     *
     * @param callback the {@link Runnable} to execute; must not be {@code null}
     */
    public void setOnStartGame(Runnable callback) { this.onStartGame = callback; }

    /**
     * Registers the callback invoked when the player clicks "HOW TO PLAY".
     *
     * @param callback the {@link Runnable} to execute; must not be {@code null}
     */
    public void setOnShowTutorial(Runnable callback) { this.onShowTutorial = callback; }
    /**
     * Registers the callback invoked when the player clicks "Scoreboard".
     *
     * @param callback the {@link Runnable} to execute; must not be {@code null}
     */
    public void setOnShowScoreboard(Runnable callback) { this.onShowScoreboard = callback; }

    /**
     * Registers the callback invoked when the player clicks "Settings".
     *
     * @param callback the {@link Runnable} to execute; must not be {@code null}
     */
    public void setOnShowSettings(Runnable callback) { this.onShowSettings = callback; }

    /**
     * Registers the callback invoked when the player clicks "Quit".
     *
     * @param callback the {@link Runnable} to execute; must not be {@code null}
     */
    public void setOnQuit(Runnable callback) { this.onQuit = callback; }

    // Inner class for menu buttons
    /**
     * Lightweight value object representing a single main-menu navigation button.
     *
     * <p>Stores position, size, label text, and hover state; renders itself
     * via {@link #render(Graphics2D)}; and provides hit-testing via
     * {@link #contains(int, int)}.</p>
     */
    private static class MenuButton {

        /** Display label shown centred inside the button. */
        private String text;

        /** Pixel x/y position and dimensions of this button in pixels. */
        private int x, y, width, height;

        /** {@code true} when the mouse cursor is over this button. */
        private boolean hovered;

        /**
         * Constructs a new {@code MenuButton} at the given position and size.
         *
         * @param text   display label
         * @param x      pixel x of top-left corner
         * @param y      pixel y of top-left corner
         * @param width  button width in pixels
         * @param height button height in pixels
         */
        public MenuButton(String text, int x, int y, int width, int height) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.hovered = false;
        }

        /**
         * Draws this button: optional hover background fill, rounded border,
         * and centred label text.
         *
         * @param g the {@link Graphics2D} context
         */
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

        /**
         * Returns whether the given pixel coordinates fall within this button's bounds.
         *
         * @param px pixel x to test
         * @param py pixel y to test
         * @return {@code true} if (px, py) is inside the button rectangle
         */
        public boolean contains(int px, int py) {
            return px >= x && px <= x + width && py >= y && py <= y + height;
        }

        /**
         * Returns the button's display label text.
         *
         * @return label string; never {@code null}
         */
        public String getText() { return text; }

        /**
         * Returns whether the mouse is currently hovering over this button.
         *
         * @return {@code true} if hovered
         */
        public boolean isHovered() { return hovered; }

        /**
         * Sets the hover state for this button, used to update rendering.
         *
         * @param hovered {@code true} if the mouse is over this button
         */
        public void setHovered(boolean hovered) { this.hovered = hovered; }
    }
}
