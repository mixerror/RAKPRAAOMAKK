package grid;

import engine.SettingsPanel.BoardStyle;
import interfaces.Renderable;
import java.awt.*;

/**
 * CLASS: Board
 * Manages the 6x6 grid of cells.
 * Supports different visual themes.
 *
 * <p>Owns a two-dimensional array of {@link GridCell} objects and provides helpers
 * for resetting cell state, applying visual themes, validating grid coordinates,
 * and delegating rendering. Grid coordinates use {@code (row, col)} notation where
 * {@code (0,0)} is the top-left cell. Pixel positions for each cell are computed
 * once at construction from the supplied screen-centre offsets.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   Board board = new Board(800, 700);
 *   board.setTheme(BoardStyle.MATRIX);
 *   GridCell cell = board.getCell(3, 4);
 *   board.resetAllCells();
 * </pre>
 *
 * @author Project Team
 * @version 1.0
 * @see GridCell
 */
public class Board implements Renderable {

    /** Number of rows and columns in the square grid. */
    private static final int GRID_SIZE = 6;

    /** Pixel width (and height) of each square cell, excluding gaps. */
    private static final int CELL_SIZE = 80;

    /** Pixel gap between adjacent cells. */
    private static final int GAP = 4;

    /** Pixel padding between the board background edge and the first cell. */
    private static final int PADDING = 12;

    /** Two-dimensional array of all grid cells; indexed by [row][col]. */
    private GridCell[][] cells;

    /** Pixel x/y coordinates of the board background's top-left corner. */
    private int boardX, boardY;

    /** Total pixel width and height of the board background (cells + gaps + padding). */
    private int boardWidth, boardHeight;

    /**
     * Constructs a new {@code Board} centred within a screen of the given dimensions.
     *
     * <p>Computes the board's pixel origin so that it is centred on the screen, then
     * creates and positions all {@link GridCell} instances within the grid.</p>
     *
     * @param screenWidth  total width of the containing panel in pixels
     * @param screenHeight total height of the containing panel in pixels
     */
    public Board(int screenWidth, int screenHeight) {
        int totalSize = GRID_SIZE * CELL_SIZE + (GRID_SIZE - 1) * GAP;
        boardWidth = totalSize + PADDING * 2;
        boardHeight = totalSize + PADDING * 2;
        boardX = (screenWidth - boardWidth) / 2;
        boardY = (screenHeight - boardHeight) / 2;

        cells = new GridCell[GRID_SIZE][GRID_SIZE];

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int cellX = boardX + PADDING + col * (CELL_SIZE + GAP);
                int cellY = boardY + PADDING + row * (CELL_SIZE + GAP);
                cells[row][col] = new GridCell(row, col, cellX, cellY, CELL_SIZE);
            }
        }
    }

    /**
     * Resets every cell to the default safe state by calling {@link GridCell#reset()}
     * on each one. Should be called at the start of each game session and at the
     * beginning of each frame in the game loop before hazards are updated.
     */
    public void resetAllCells() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                cells[row][col].reset();
            }
        }
    }

    /**
     * Applies a visual theme to every cell in the grid by calling
     * {@link GridCell#setTheme(BoardStyle)} on each one. Takes effect on the next repaint.
     *
     * @param style the {@link BoardStyle} to apply; must not be {@code null}
     */
    public void setTheme(BoardStyle style) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                cells[row][col].setTheme(style);
            }
        }
    }

    /**
     * Returns the {@link GridCell} at the specified grid coordinate, or {@code null}
     * if the coordinate is out of bounds.
     *
     * @param row row index in the range [0, {@value #GRID_SIZE})
     * @param col column index in the range [0, {@value #GRID_SIZE})
     * @return the cell at {@code (row, col)}, or {@code null} if out of bounds
     */
    public GridCell getCell(int row, int col) {
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) {
            return null;
        }
        return cells[row][col];
    }

    /**
     * Returns whether the given grid coordinates fall within the board boundaries.
     *
     * @param row row index to test
     * @param col column index to test
     * @return {@code true} if both indices are in the range [0, {@value #GRID_SIZE})
     */
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE;
    }

    /**
     * Renders the board background and all cells onto the supplied graphics context.
     *
     * <p>Draws a semi-transparent rounded rectangle as the board background and
     * border, then delegates to each {@link GridCell#render(Graphics2D)} to draw
     * individual cells.</p>
     *
     * @param g the {@link Graphics2D} context
     */
    @Override
    public void render(Graphics2D g) {
        // Board background
        g.setColor(new Color(10, 10, 26, 200));
        g.fillRoundRect(boardX, boardY, boardWidth, boardHeight, 12, 12);

        // Board border
        g.setColor(new Color(129, 140, 248, 50));
        g.setStroke(new BasicStroke(1));
        g.drawRoundRect(boardX, boardY, boardWidth, boardHeight, 12, 12);

        // Render all cells
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                cells[row][col].render(g);
            }
        }
    }

    /**
     * Returns the number of rows (and columns) in the square grid.
     *
     * @return {@value #GRID_SIZE}
     */
    public int getGridSize() { return GRID_SIZE; }
}
