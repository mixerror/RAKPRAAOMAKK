package patterns;

import grid.Board;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCRETE CLASS: CornersHazard
 * Extends Hazard - targets all four corner tiles
 * INHERITANCE: CornersHazard → Hazard
 *
 * <p>Always targets exactly four cells regardless of board size:
 * top-left {@code (0,0)}, top-right {@code (0,N-1)},
 * bottom-left {@code (N-1,0)}, and bottom-right {@code (N-1,N-1)}.
 * Less threatening in isolation, but effective in combinations with other patterns.</p>
 *
 * @author Project Team
 * @version 1.0
 * @see Hazard
 */
public class CornersHazard extends Hazard {
    
    /**
     * Constructs a {@code CornersHazard} targeting all four grid corners.
     *
     * @param board     the game board
     * @param countdown beats of warning before the corners fire; must be &gt;= 0
     */
    public CornersHazard(Board board, int countdown) {
        super(board, createCornerTiles(board.getGridSize()), countdown);
    }
    
    /**
     * Builds the tile list for the four corners of an {@code N×N} grid.
     *
     * @param gridSize number of rows/columns in the square grid
     * @return list of exactly four {@code {row, col}} arrays, one per corner
     */
    private static List<int[]> createCornerTiles(int gridSize) {
        List<int[]> tiles = new ArrayList<>();
        int max = gridSize - 1;
        
        tiles.add(new int[]{0, 0});         // Top-left
        tiles.add(new int[]{0, max});       // Top-right
        tiles.add(new int[]{max, 0});       // Bottom-left
        tiles.add(new int[]{max, max});     // Bottom-right
        
        return tiles;
    }
}
