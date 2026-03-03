package patterns;

import grid.Board;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCRETE CLASS: DiagonalHazard
 * Extends Hazard - targets diagonal line from corner to corner
 * INHERITANCE: DiagonalHazard → Hazard
 */
public class DiagonalHazard extends Hazard {
    
    public DiagonalHazard(Board board, boolean topLeftToBottomRight, int countdown) {
        super(board, createDiagonalTiles(board.getGridSize(), topLeftToBottomRight), countdown);
    }
    
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
