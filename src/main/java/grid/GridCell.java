package grid;

import engine.SettingsPanel.BoardStyle;
import interfaces.Renderable;
import java.awt.*;

/**
 * CLASS: GridCell
 * Represents one tile in the 6x6 grid.
 * Displays countdown numbers, danger states, and hold indicators.
 * Supports different visual themes.
 */
public class GridCell implements Renderable {

    private int gridRow, gridCol;
    private int x, y, size;

    private boolean isDanger;
    private boolean isActive;
    private int countdown;

    private Color baseDark;
    private Color baseLight;

    private static final Color BORDER = new Color(26, 26, 58);
    private static final Color DANGER_BORDER = new Color(124, 58, 237);
    private static final Color ACTIVE_BORDER = new Color(239, 68, 68);
    private static final Color COUNTDOWN_COLOR = new Color(245, 158, 11);

    public GridCell(int gridRow, int gridCol, int x, int y, int size) {
        this.gridRow = gridRow;
        this.gridCol = gridCol;
        this.x = x;
        this.y = y;
        this.size = size;

        // Default colors
        baseDark = new Color(17, 17, 40);
        baseLight = new Color(26, 26, 58);

        reset();
    }

    public void setTheme(BoardStyle style) {
        this.baseDark = style.dark;
        this.baseLight = style.light;
    }

    public void reset() {
        isDanger = false;
        isActive = false;
        countdown = 0;
    }

    public void setDanger(int countdown) {
        this.isDanger = true;
        this.countdown = countdown;
        this.isActive = false;
    }

    public void setActive() {
        this.isActive = true;
        this.isDanger = false;
    }

    @Override
    public void render(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Base color (checkerboard pattern) - now uses theme colors
        boolean isDark = (gridRow + gridCol) % 2 == 0;
        Color baseColor = isDark ? baseDark : baseLight;

        g.setColor(baseColor);
        g.fillRoundRect(x, y, size, size, 8, 8);

        // Danger warning state
        if (isDanger) {
            g.setColor(new Color(124, 58, 237, 65));
            g.fillRoundRect(x, y, size, size, 8, 8);

            g.setColor(DANGER_BORDER);
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(x + 2, y + 2, size - 4, size - 4, 8, 8);

            // Draw countdown number
            if (countdown > 0) {
                g.setFont(new Font("Monospaced", Font.BOLD, 32));
                g.setColor(COUNTDOWN_COLOR);
                String text = String.valueOf(countdown);
                FontMetrics fm = g.getFontMetrics();
                int textX = x + (size - fm.stringWidth(text)) / 2;
                int textY = y + (size + fm.getAscent()) / 2 - 5;
                g.drawString(text, textX, textY);
            }
        }

        // Active danger state
        if (isActive) {
            g.setColor(new Color(239, 68, 68, 90));
            g.fillRoundRect(x, y, size, size, 8, 8);

            g.setColor(ACTIVE_BORDER);
            g.setStroke(new BasicStroke(3));
            g.drawRoundRect(x + 2, y + 2, size - 4, size - 4, 8, 8);

            // X mark
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(3));
            g.drawLine(x + 15, y + 15, x + size - 15, y + size - 15);
            g.drawLine(x + size - 15, y + 15, x + 15, y + size - 15);
        }

        // Base border
        if (!isDanger && !isActive) {
            g.setColor(BORDER);
            g.setStroke(new BasicStroke(1));
            g.drawRoundRect(x, y, size, size, 8, 8);
        }
    }

    public int getGridRow() { return gridRow; }
    public int getGridCol() { return gridCol; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getSize() { return size; }
}