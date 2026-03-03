package patterns;

import grid.Board;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCRETE CLASS: XShapeHazard
 * Extends Hazard - targets both diagonals forming an X shape
 * INHERITANCE: XShapeHazard → Hazard
 */
public class XShapeHazard extends Hazard {
    
    public XShapeHazard(Board board, int countdown) {
        super(board, createXShapeTiles(board.getGridSize()), countdown);
    }
    
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
