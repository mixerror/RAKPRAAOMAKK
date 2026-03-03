package patterns;

import grid.Board;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCRETE CLASS: BorderHazard
 * Extends Hazard - targets all tiles on the outer border/edge
 * INHERITANCE: BorderHazard → Hazard
 */
public class BorderHazard extends Hazard {
    
    public BorderHazard(Board board, int countdown) {
        super(board, createBorderTiles(board.getGridSize()), countdown);
    }
    
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
