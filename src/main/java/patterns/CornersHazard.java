package patterns;

import grid.Board;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCRETE CLASS: CornersHazard
 * Extends Hazard - targets all four corner tiles
 * INHERITANCE: CornersHazard → Hazard
 */
public class CornersHazard extends Hazard {
    
    public CornersHazard(Board board, int countdown) {
        super(board, createCornerTiles(board.getGridSize()), countdown);
    }
    
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
