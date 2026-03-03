package patterns;

import grid.Board;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCRETE CLASS: CheckerHazard
 * Extends Hazard - targets checkerboard pattern (alternating tiles)
 * INHERITANCE: CheckerHazard → Hazard
 */
public class CheckerHazard extends Hazard {
    
    public CheckerHazard(Board board, boolean startWithEven, int countdown) {
        super(board, createCheckerTiles(board.getGridSize(), startWithEven), countdown);
    }
    
    private static List<int[]> createCheckerTiles(int gridSize, boolean startWithEven) {
        List<int[]> tiles = new ArrayList<>();
        
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                boolean isEven = (row + col) % 2 == 0;
                
                if (startWithEven && isEven) {
                    tiles.add(new int[]{row, col});
                } else if (!startWithEven && !isEven) {
                    tiles.add(new int[]{row, col});
                }
            }
        }
        
        return tiles;
    }
}
