package patterns;

import grid.Board;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCRETE CLASS: XShapeHazard
 * Extends Hazard - targets both diagonals forming an X shape
 * INHERITANCE: XShapeHazard → Hazard
 *
 * <p>Combines the main diagonal (top-left → bottom-right) and the anti-diagonal
 * (top-right → bottom-left) into a single pattern. Duplicate tiles at the
 * intersection (centre cell for odd-sized grids) are automatically deduplicated.
 * The total tile count is {@code 2N - 1} for odd {@code N} or {@code 2N} for
 * even {@code N}.</p>
 *
 * @author Project Team
 * @version 1.0
 * @see Hazard
 * @see DiagonalHazard
 */
public class XShapeHazard extends Hazard {
    
    /**
     * Constructs an {@code XShapeHazard} spanning both diagonals of the board.
     *
     * @param board     the game board
     * @param countdown beats of warning before the X shape fires; must be &gt;= 0
     */
    public XShapeHazard(Board board, int countdown) {
        super(board, createXShapeTiles(board.getGridSize()), countdown);
    }
    
    /**
     * Builds the deduplicated tile list for both diagonals of an {@code N×N} grid.
     *
     * <p>First adds all tiles on the main diagonal, then iterates the anti-diagonal
     * and skips any tile already present in the list to avoid duplicate activation.</p>
     *
     * @param gridSize number of rows/columns in the square grid
     * @return deduplicated list of {@code {row, col}} arrays forming an X shape
     */
    private static List<int[]> createXShapeTiles(int gridSize) {
        List<int[]> tiles = new ArrayList<>();
        
        // Top-left to bottom-right diagonal
        for (int i = 0; i < gridSize; i++) {
            tiles.add(new int[]{i, i});
        }
        
        // Top-right to bottom-left diagonal
        for (int i = 0; i < gridSize; i++) {
            int row = i;
            int col = gridSize - 1 - i;
            
            // Avoid adding center twice if grid size is even
            boolean isDuplicate = false;
            for (int[] existing : tiles) {
                if (existing[0] == row && existing[1] == col) {
                    isDuplicate = true;
                    break;
                }
            }
            
            if (!isDuplicate) {
                tiles.add(new int[]{row, col});
            }
        }
        
        return tiles;
    }
}
