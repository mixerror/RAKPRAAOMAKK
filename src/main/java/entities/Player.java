package entities;

import grid.Board;
import grid.GridCell;
import interfaces.Renderable;
import java.awt.*;

/**
 * CLASS: Player
 * The player character that moves on the grid.
 * Renders as a glowing cyan square.
 *
 * <p>The player occupies exactly one {@link GridCell} at a time and moves one
 * cell per key press in the four cardinal directions. Out-of-bounds moves are
 * silently ignored. A pulsing glow animation is advanced each frame via
 * {@link #update()}. Collision detection is performed externally by
 * {@link engine.GameEngine}.</p>
 *
 * @author Project Team
 * @version 1.0
 * @see Board
 * @see GridCell
 */
public class Player implements Renderable {
    
    /** Current row and column indices on the {@link Board} grid (0-based). */
    private int gridRow, gridCol;

    /** Reference to the board used for bounds checking and cell lookup. */
    private Board board;

    /**
     * Monotonically increasing phase value (in radians) driving the pulse animation.
     * Incremented by 0.1 each frame in {@link #update()}.
     */
    private float pulseTime;
    
    /** Base fill colour for the player avatar. */
    private static final Color PLAYER_COLOR = new Color(0, 255, 204);
    
    /**
     * Constructs a new {@code Player} at the specified grid position on the given board.
     *
     * @param startRow initial row index
     * @param startCol initial column index
     * @param board    the game board; used for bounds validation and cell lookup
     */
    public Player(int startRow, int startCol, Board board) {
        this.gridRow = startRow;
        this.gridCol = startCol;
        this.board = board;
        this.pulseTime = 0;
    }
    
    /**
     * Moves the player by the given row and column deltas if the destination
     * is within the board boundaries. The move is silently ignored when it
     * would place the player outside the grid.
     *
     * @param deltaRow row offset to apply: {@code -1} = up, {@code +1} = down, {@code 0} = no change
     * @param deltaCol column offset to apply: {@code -1} = left, {@code +1} = right, {@code 0} = no change
     */
    public void move(int deltaRow, int deltaCol) {
        int newRow = gridRow + deltaRow;
        int newCol = gridCol + deltaCol;
        
        if (board.isValidPosition(newRow, newCol)) {
            gridRow = newRow;
            gridCol = newCol;
        }
    }
    
    /**
     * Advances the pulse animation by one frame. Call once per game tick from
     * the game loop before {@link #render(Graphics2D)}.
     */
    public void update() {
        pulseTime += 0.1f;
    }
    
    /**
     * Renders the player avatar centred on the current {@link GridCell}.
     *
     * <p>Draws a cyan rounded square with a pulsing outer glow ring whose radius
     * oscillates with {@link #pulseTime}, plus a small bright white highlight in
     * the centre of the square.</p>
     *
     * @param g the {@link Graphics2D} context; anti-aliasing is enabled inside this method
     */
    @Override
    public void render(Graphics2D g) {
        GridCell cell = board.getCell(gridRow, gridCol);
        if (cell == null) return;
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int x = cell.getX() + 8;
        int y = cell.getY() + 8;
        int size = cell.getSize() - 16;
        
        // Pulsing glow effect
        float pulse = (float)(Math.sin(pulseTime) * 0.3 + 0.7);
        int glowSize = (int)(size * (1 + pulse * 0.15f));
        int glowX = x - (glowSize - size) / 2;
        int glowY = y - (glowSize - size) / 2;
        
        // Outer glow
        g.setColor(new Color(0, 255, 204, (int)(50 * pulse)));
        g.fillRoundRect(glowX, glowY, glowSize, glowSize, 6, 6);
        
        // Inner player
        g.setColor(PLAYER_COLOR);
        g.fillRoundRect(x, y, size, size, 6, 6);
        
        // Bright center highlight
        g.setColor(new Color(255, 255, 255, 150));
        g.fillRoundRect(x + size/3, y + size/3, size/3, size/3, 3, 3);
    }
    
    /**
     * Returns the player's current row index on the board.
     *
     * @return row index (0-based)
     */
    public int getGridRow() { return gridRow; }

    /**
     * Returns the player's current column index on the board.
     *
     * @return column index (0-based)
     */
    public int getGridCol() { return gridCol; }
}
