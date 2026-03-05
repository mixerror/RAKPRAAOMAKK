package engine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * CLASS: SettingsPanel
 * Allows player to change board visual style/theme
 *
 * <p>Renders a clickable button for each {@link BoardStyle} enum constant,
 * including a two-swatch colour preview. The currently selected style is
 * highlighted and returned via {@link #getSelectedStyle()} for the owning
 * {@link GameLauncher} to apply before the next game session.</p>
 *
 * @author Project Team
 * @version 1.0
 * @see BoardStyle
 */
public class SettingsPanel extends JPanel {

    /** Preferred panel width in pixels. */
    private static final int WIDTH = 800;

    /** Preferred panel height in pixels. */
    private static final int HEIGHT = 700;

    /**
     * Enumeration of available visual themes for the game board.
     *
     * <p>Each constant carries a human-readable {@link #name}, and a pair of
     * {@link Color} values ({@link #dark} and {@link #light}) that are applied
     * to the alternating checkerboard cells of the {@link grid.GridCell} grid.</p>
     */
    public enum BoardStyle {

        /** Dark navy colour scheme — the default board appearance. */
        DARK_BLUE("Dark Blue", new Color(17, 17, 40), new Color(26, 26, 58)),

        /** Deep purple colour scheme. */
        PURPLE("Purple", new Color(30, 20, 50), new Color(45, 30, 70)),

        /** Green phosphor CRT "Matrix" aesthetic. */
        GREEN("Matrix", new Color(10, 30, 10), new Color(15, 45, 15)),

        /** Deep red "Crimson" colour scheme. */
        RED("Crimson", new Color(40, 10, 10), new Color(60, 15, 15)),

        /** Neon Cyberpunk purple/magenta colour scheme. */
        CYBERPUNK("Cyberpunk", new Color(20, 5, 30), new Color(35, 10, 45));

        /** Human-readable label shown in the settings UI. */
        final String name;

        /** Darker shade applied to checkerboard cells where {@code (row+col) % 2 == 0}. */
        public final Color dark;

        /** Lighter shade applied to checkerboard cells where {@code (row+col) % 2 == 1}. */
        public final Color light;

        /**
         * Constructs a {@code BoardStyle} constant with the given display name and colours.
         *
         * @param name  human-readable label
         * @param dark  dark cell colour
         * @param light light cell colour
         */
        BoardStyle(String name, Color dark, Color light) {
            this.name = name;
            this.dark = dark;
            this.light = light;
        }
    }

    /** The currently selected visual theme; defaults to {@link BoardStyle#DARK_BLUE}. */
    private BoardStyle selectedStyle;

    /** Callback invoked when the player clicks the Back button. */
    private Runnable onBack;

    /** Bounding rectangle of the Back navigation button. */
    private Rectangle backButton;

    /** Volume slider dragging state. */
    private Rectangle volumeTrack;
    private boolean   volumeDragging = false;

    /**
     * Constructs a new {@code SettingsPanel}: sets preferred size, background, and
     * default theme, positions the Back button, and registers mouse click listeners
     * for both navigation and theme selection.
     */
    public SettingsPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(5, 5, 16));

        selectedStyle = BoardStyle.DARK_BLUE;
        backButton = new Rectangle(50, 600, 150, 50);
        volumeTrack = new Rectangle(250, 540, 300, 12);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (backButton.contains(e.getPoint())) {
                    SoundManager.click();
                    if (onBack != null) onBack.run();
                    return;
                }

                // Check style button clicks
                int startY = 200;
                int spacing = 80;
                for (int i = 0; i < BoardStyle.values().length; i++) {
                    Rectangle styleButton = new Rectangle(250, startY + i * spacing, 300, 60);
                    if (styleButton.contains(e.getPoint())) {
                        SoundManager.click();
                        selectedStyle = BoardStyle.values()[i];
                        repaint();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // Enlarge hit area for slider
                Rectangle sliderHit = new Rectangle(volumeTrack.x - 8, volumeTrack.y - 12, volumeTrack.width + 16, volumeTrack.height + 24);
                if (sliderHit.contains(e.getPoint())) {
                    volumeDragging = true;
                    updateVolume(e.getX());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                volumeDragging = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (volumeDragging) updateVolume(e.getX());
            }
        });
    }

    /**
     * Paints the settings screen: title, "Board Style" subtitle, a button row for
     * each {@link BoardStyle} (with colour swatches and a selection highlight),
     * and the Back navigation button.
     *
     * @param g the Swing {@link Graphics} context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Title
        g2d.setFont(new Font("Monospaced", Font.BOLD, 48));
        g2d.setColor(new Color(0, 255, 204));
        String title = "SETTINGS";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, 100);

        // Subtitle
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g2d.setColor(new Color(129, 140, 248));
        String subtitle = "Board Style";
        g2d.drawString(subtitle, 250, 170);

        // Style buttons
        int startY = 200;
        int spacing = 80;
        for (int i = 0; i < BoardStyle.values().length; i++) {
            BoardStyle style = BoardStyle.values()[i];
            Rectangle styleButton = new Rectangle(250, startY + i * spacing, 300, 60);

            boolean isSelected = style == selectedStyle;

            // Button background
            if (isSelected) {
                g2d.setColor(new Color(0, 255, 204, 30));
                g2d.fillRoundRect(styleButton.x, styleButton.y, styleButton.width, styleButton.height, 8, 8);
            }

            // Color preview boxes
            g2d.setColor(style.dark);
            g2d.fillRect(styleButton.x + 10, styleButton.y + 15, 30, 30);
            g2d.setColor(style.light);
            g2d.fillRect(styleButton.x + 45, styleButton.y + 15, 30, 30);

            // Button border
            g2d.setColor(isSelected ? new Color(0, 255, 204) : new Color(129, 140, 248));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(styleButton.x, styleButton.y, styleButton.width, styleButton.height, 8, 8);

            // Button text
            g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
            g2d.setColor(isSelected ? new Color(0, 255, 204) : new Color(226, 232, 240));
            g2d.drawString(style.name, styleButton.x + 90, styleButton.y + 38);
        }

        // ── Volume Slider ──────────────────────────────────────────────────────
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g2d.setColor(new Color(129, 140, 248));
        g2d.drawString("Volume", 250, 525);

        float vol = SoundManager.getVolume();
        int trackY  = volumeTrack.y;
        int trackX  = volumeTrack.x;
        int trackW  = volumeTrack.width;
        int trackH  = volumeTrack.height;

        // Track background
        g2d.setColor(new Color(40, 40, 80));
        g2d.fillRoundRect(trackX, trackY, trackW, trackH, 6, 6);

        // Filled portion
        int fillW = (int)(trackW * vol);
        g2d.setColor(new Color(0, 255, 204));
        if (fillW > 0) g2d.fillRoundRect(trackX, trackY, fillW, trackH, 6, 6);

        // Track border
        g2d.setColor(new Color(129, 140, 248));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(trackX, trackY, trackW, trackH, 6, 6);

        // Thumb
        int thumbX = trackX + fillW - 8;
        thumbX = Math.max(trackX, Math.min(trackX + trackW - 16, thumbX));
        g2d.setColor(new Color(0, 255, 204));
        g2d.fillRoundRect(thumbX, trackY - 6, 16, trackH + 12, 6, 6);
        g2d.setColor(new Color(5, 5, 16));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(thumbX, trackY - 6, 16, trackH + 12, 6, 6);

        // Volume percentage label
        g2d.setFont(new Font("Monospaced", Font.BOLD, 15));
        g2d.setColor(new Color(0, 255, 204));
        String volPct = (int)(vol * 100) + "%";
        g2d.drawString(volPct, trackX + trackW + 14, trackY + trackH);

        // Back button
        g2d.setColor(new Color(129, 140, 248));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(backButton.x, backButton.y, backButton.width, backButton.height, 8, 8);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
        String backText = "BACK";
        fm = g2d.getFontMetrics();
        g2d.drawString(backText,
                backButton.x + (backButton.width - fm.stringWidth(backText)) / 2,
                backButton.y + (backButton.height + fm.getAscent()) / 2 - 5);
    }

    /**
     * Updates the master volume based on the mouse x position relative to the track.
     */
    private void updateVolume(int mouseX) {
        float ratio = (float)(mouseX - volumeTrack.x) / volumeTrack.width;
        ratio = Math.max(0f, Math.min(1f, ratio));
        SoundManager.setVolume(ratio);
        repaint();
    }

    /**
     * Registers the callback invoked when the player clicks the Back button.
     *
     * @param callback the {@link Runnable} to execute; must not be {@code null}
     */
    public void setOnBack(Runnable callback) { this.onBack = callback; }

    /**
     * Returns the currently selected board visual theme.
     *
     * @return the selected {@link BoardStyle}; never {@code null}
     */
    public BoardStyle getSelectedStyle() { return selectedStyle; }

    /**
     * Returns the current volume setting as set by the slider.
     *
     * @return volume in [0.0, 1.0]
     */
    public float getSelectedVolume() { return SoundManager.getVolume(); }
}