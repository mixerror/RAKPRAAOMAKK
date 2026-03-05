package grid;

import engine.SettingsPanel.BoardStyle;
import interfaces.Renderable;
import java.awt.*;

/**
 * CLASS: GridCell
 * Represents one tile in the 6x6 grid.
 * Displays countdown numbers, danger states, and hold indicators.
 * Supports different visual themes.
 *
 * <p>Each cell progresses through three mutually exclusive visual states:</p>
 * <ol>
 *   <li><strong>Safe</strong> — default; no hazard is targeting this cell
 *       (alternating checkerboard fill based on theme colours).</li>
 *   <li><strong>Danger</strong> — a hazard countdown is active; the cell is tinted
 *       purple and displays the beat countdown number so the player can react.</li>
 *   <li><strong>Active</strong> — the hazard has fired; the cell flashes red with an
 *       X mark. Any player occupying this cell loses a life.</li>
 * </ol>
 *
 * @author Project Team
 * @version 1.0
 * @see Board
 */
public class GridCell implements Renderable {

    /** Row and column indices of this cell within the parent {@link Board} grid (0-based). */
    private int gridRow, gridCol;

    /** Pixel x/y position and size (width = height) of this cell. */
    private int x, y, size;

    /** {@code true} while a hazard is in its warning phase targeting this cell. */
    private boolean isDanger;

    /** {@code true} while a hazard is in its active (fired) phase targeting this cell. */
    private boolean isActive;

    /** Beats remaining until the targeting hazard fires; displayed as a countdown. */
    private int countdown;

    /** Dark checkerboard fill colour; sourced from the current {@link BoardStyle}. */
    private Color baseDark;

    /** Light checkerboard fill colour; sourced from the current {@link BoardStyle}. */
    private Color baseLight;

    /** Border colour used in the default safe state. */
    private static final Color BORDER = new Color(26, 26, 58);

    /** Border colour used during the danger warning state. */
    private static final Color DANGER_BORDER = new Color(124, 58, 237);

    /** Border colour used during the active (fired) state. */
    private static final Color ACTIVE_BORDER = new Color(239, 68, 68);

    /** Text colour used to render the countdown digit. */
    private static final Color COUNTDOWN_COLOR = new Color(245, 158, 11);

    /**
     * Constructs a new {@code GridCell} at the specified grid and pixel positions
     * with the default DARK_BLUE theme colours. Calls {@link #reset()} to initialise state.
     *
     * @param gridRow grid row index (0-based)
     * @param gridCol grid column index (0-based)
     * @param x       pixel x of the top-left corner
     * @param y       pixel y of the top-left corner
     * @param size    pixel side-length of the square cell
     */
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

    /**
     * Updates this cell's base fill colours to match the given {@link BoardStyle}.
     * Takes effect on the next {@link #render(Graphics2D)} call.
     *
     * @param style the board style to apply; must not be {@code null}
     */
    public void setTheme(BoardStyle style) {
        this.baseDark = style.dark;
        this.baseLight = style.light;
    }

    /**
     * Resets this cell to the safe state by clearing {@code isDanger},
     * {@code isActive}, and the {@code countdown}.
     */
    public void reset() {
        isDanger = false;
        isActive = false;
        countdown = 0;
    }

    /**
     * Transitions this cell to the danger warning state and sets the countdown
     * number that will be displayed until the hazard fires.
     *
     * <p>If the cell is already in the active (fired) state from another hazard,
     * this call is ignored — active always takes visual priority over warning so
     * overlapping hazards cannot hide a red-X tile behind a countdown number.</p>
     *
     * <p>When multiple warning hazards overlap on the same cell, the lowest
     * (most urgent) countdown is kept so the player always sees the most
     * pressing threat.</p>
     *
     * @param countdown beats remaining until activation; must be &gt; 0
     */
    public void setDanger(int countdown) {
        if (isActive) return; // active wins — never overwrite a red-X with a countdown
        if (isDanger) {
            // Already a warning: keep the most urgent (lowest) countdown
            if (countdown < this.countdown) this.countdown = countdown;
            return;
        }
        this.isDanger = true;
        this.countdown = countdown;
    }

    /**
     * Transitions this cell to the active (fired) state, displaying a red flash
     * with an X mark. The player must have vacated this cell or loses a life.
     *
     * <p>Active always wins over any warning state — calling this on a cell that
     * is currently showing a countdown correctly promotes it to the fired state.</p>
     */
    public void setActive() {
        this.isActive = true;
        this.isDanger = false;
        this.countdown = 0;
    }

    /**
     * Renders this cell onto the supplied graphics context.
     *
     * <p>The base checkerboard fill is drawn first; then the appropriate danger or
     * active overlay (tint, border, countdown digit or X mark) is drawn on top.
     * If the cell is in the safe state, only the base fill and a thin border are drawn.</p>
     *
     * @param g the {@link Graphics2D} context; anti-aliasing is enabled inside this method
     */
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

    /**
     * Returns this cell's row index within the parent {@link Board}.
     *
     * @return row index (0-based)
     */
    public int getGridRow() { return gridRow; }

    /**
     * Returns this cell's column index within the parent {@link Board}.
     *
     * @return column index (0-based)
     */
    public int getGridCol() { return gridCol; }

    /**
     * Returns the pixel x-coordinate of this cell's top-left corner.
     *
     * @return pixel x position
     */
    public int getX() { return x; }

    /**
     * Returns the pixel y-coordinate of this cell's top-left corner.
     *
     * @return pixel y position
     */
    public int getY() { return y; }

    /**
     * Returns the pixel side-length of this square cell.
     *
     * @return cell size in pixels
     */
    public int getSize() { return size; }
}