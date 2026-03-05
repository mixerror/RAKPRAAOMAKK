package patterns;

import grid.Board;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCRETE CLASS: BorderHazard
 * Extends Hazard - targets all tiles on the outer border/edge
 * INHERITANCE: BorderHazard → Hazard
 *
 * <p>Targets all {@code 4 × (N - 1)} border cells (top row, bottom row, left column
 * excluding corners already counted, and right column excluding corners), forcing
 * the player to retreat toward the safe interior of the grid.</p>
 *
 * @author Project Team
 * @version 1.0
 * @see Hazard
 */
public class BorderHazard extends Hazard {
    
    /**
     * Constructs a {@code BorderHazard} targeting all cells on the outermost ring.
     *
     * @param board     the game board
     * @param countdown beats of warning before the border fires; must be &gt;= 0
     */
    public BorderHazard(Board board, int countdown) {
        super(board, createBorderTiles(board.getGridSize()), countdown);
    }
    
    /**
     * Builds the tile list for the full outer border of an {@code N×N} grid.
     *
     * <p>Corners are included exactly once (via the top and bottom rows). The left
     * and right column segments added afterwards exclude the already-counted corner rows.</p>
     *
     * @param gridSize number of rows/columns in the square grid
     * @return list of {@code {row, col}} arrays covering all {@code 4(N-1)} border tiles
     */
    private static List<int[]> createBorderTiles(int gridSize) {
        List<int[]> tiles = new ArrayList<>();
        int max = gridSize - 1;
        
        // Top row
        for (int col = 0; col < gridSize; col++) {
            tiles.add(new int[]{0, col});
        }
        
        // Bottom row
        for (int col = 0; col < gridSize; col++) {
            tiles.add(new int[]{max, col});
        }
        
        // Left column (excluding corners already added)
        for (int row = 1; row < max; row++) {
            tiles.add(new int[]{row, 0});
        }
        
        // Right column (excluding corners already added)
        for (int row = 1; row < max; row++) {
            tiles.add(new int[]{row, max});
        }
        
        return tiles;
    }
}
