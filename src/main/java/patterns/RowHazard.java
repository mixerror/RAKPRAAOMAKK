package patterns;

import grid.Board;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCRETE CLASS: RowHazard
 * Extends Hazard - targets an entire horizontal row
 * INHERITANCE: RowHazard → Hazard
 *
 * <p>Targets all {@code N} cells in the specified row simultaneously, forcing
 * the player to escape to a different row before the countdown expires.</p>
 *
 * @author Project Team
 * @version 1.0
 * @see Hazard
 */
public class RowHazard extends Hazard {
    
    /**
     * Constructs a {@code RowHazard} targeting every cell in the specified row
     * with the given beat countdown.
     *
     * @param board     the game board
     * @param row       row index to target (0-based)
     * @param countdown beats of warning before the row fires; must be &gt;= 0
     */
    public RowHazard(Board board, int row, int countdown) {
        super(board, createRowTiles(row, board.getGridSize()), countdown);
    }
    
    /**
     * Builds the tile list for the specified row: one {@code {row, col}} entry
     * for every column in the grid.
     *
     * @param row      row index to target
     * @param gridSize number of columns in the grid
     * @return list of {@code {row, col}} arrays covering the full row
     */
    private static List<int[]> createRowTiles(int row, int gridSize) {
        List<int[]> tiles = new ArrayList<>();
        for (int col = 0; col < gridSize; col++) {
            tiles.add(new int[]{row, col});
        }
        return tiles;
    }
}
