package patterns;

import grid.Board;

import java.util.ArrayList;
import java.util.List;

/**
 * Hazard pattern that targets all cells in a single column.
 *
 * <p>Targets all {@code N} cells in the specified column simultaneously,
 * forcing the player to escape to a different column before the countdown
 * expires. Vertical counterpart to {@link RowHazard}.</p>
 *
 * <p>Inheritance: {@code ColumnHazard} → {@link Hazard}</p>
 *
 * @author Project Team
 * @version 1.0
 * @see Hazard
 */
public class ColumnHazard extends Hazard{

    /**
     * Constructs a {@code ColumnHazard} targeting every cell in the specified
     * column with the given beat countdown.
     *
     * @param board     the game board
     * @param col       column index to target (0-based)
     * @param countdown beats of warning before the column fires; must be &gt;= 0
     */
    public ColumnHazard(Board board, int col, int countdown) {
        super(board, createColumnTiles(col, board.getGridSize()), countdown);
    }

    /**
     * Builds the tile list for the specified column: one {@code {row, col}} entry
     * for every row in the grid.
     *
     * @param col      column index to target
     * @param gridSize number of rows in the grid
     * @return list of {@code {row, col}} arrays covering the full column
     */
    private static List<int[]> createColumnTiles(int col, int gridSize) {
        List<int[]> tiles = new ArrayList<>();
        for (int row = 0; row < gridSize; row++) {
            tiles.add(new int[]{row, col});
        }
        return tiles;
    }
}
