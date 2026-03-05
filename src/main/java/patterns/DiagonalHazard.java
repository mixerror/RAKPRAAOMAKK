package patterns;

import grid.Board;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCRETE CLASS: DiagonalHazard
 * Extends Hazard - targets diagonal line from corner to corner
 * INHERITANCE: DiagonalHazard → Hazard
 *
 * <p>Targets exactly {@code N} cells running corner-to-corner:</p>
 * <ul>
 *   <li>{@code topLeftToBottomRight = true} — main diagonal: {@code (0,0), (1,1), …, (N-1,N-1)}</li>
 *   <li>{@code topLeftToBottomRight = false} — anti-diagonal: {@code (0,N-1), (1,N-2), …, (N-1,0)}</li>
 * </ul>
 *
 * @author Project Team
 * @version 1.0
 * @see Hazard
 * @see XShapeHazard
 */
public class DiagonalHazard extends Hazard {
    
    /**
     * Constructs a {@code DiagonalHazard} along the specified diagonal direction.
     *
     * @param board                  the game board
     * @param topLeftToBottomRight   {@code true} for the main diagonal (↘);
     *                               {@code false} for the anti-diagonal (↙)
     * @param countdown              beats of warning before the diagonal fires; must be &gt;= 0
     */
    public DiagonalHazard(Board board, boolean topLeftToBottomRight, int countdown) {
        super(board, createDiagonalTiles(board.getGridSize(), topLeftToBottomRight), countdown);
    }
    
    /**
     * Builds the tile list for the specified diagonal direction.
     *
     * @param gridSize             number of rows/columns in the square grid
     * @param topLeftToBottomRight {@code true} for the main diagonal; {@code false} for the anti-diagonal
     * @return list of {@code N} {@code {row, col}} arrays along the chosen diagonal
     */
    private static List<int[]> createDiagonalTiles(int gridSize, boolean topLeftToBottomRight) {
        List<int[]> tiles = new ArrayList<>();
        
        if (topLeftToBottomRight) {
            // Top-left to bottom-right diagonal
            for (int i = 0; i < gridSize; i++) {
                tiles.add(new int[]{i, i});
            }
        } else {
            // Top-right to bottom-left diagonal
            for (int i = 0; i < gridSize; i++) {
                tiles.add(new int[]{i, gridSize - 1 - i});
            }
        }
        
        return tiles;
    }
}
