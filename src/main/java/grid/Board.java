package grid;

import engine.SettingsPanel.BoardStyle;
import interfaces.Renderable;
import java.awt.*;

/**
 * CLASS: Board
 * Manages the 6x6 grid of cells.
 * Supports different visual themes.
 */
public class Board implements Renderable {

    private static final int GRID_SIZE = 6;
    private static final int CELL_SIZE = 80;
    private static final int GAP = 4;
    private static final int PADDING = 12;

    private GridCell[][] cells;
    private int boardX, boardY;
    private int boardWidth, boardHeight;

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

    public void resetAllCells() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                cells[row][col].reset();
            }
        }
    }

    public void setTheme(BoardStyle style) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                cells[row][col].setTheme(style);
            }
        }
    }

    public GridCell getCell(int row, int col) {
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) {
            return null;
        }
        return cells[row][col];
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE;
    }

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

    public int getGridSize() { return GRID_SIZE; }
}