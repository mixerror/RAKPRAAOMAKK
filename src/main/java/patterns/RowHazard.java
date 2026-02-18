package patterns;

import grid.Board;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCRETE CLASS: RowHazard
 * Extends Hazard - targets an entire horizontal row
 * INHERITANCE: RowHazard → Hazard
 */
public class RowHazard extends Hazard {
    
    public RowHazard(Board board, int row, int countdown) {
        super(board, createRowTiles(row, board.getGridSize()), countdown);
    }
    
    private static List<int[]> createRowTiles(int row, int gridSize) {
        List<int[]> tiles = new ArrayList<>();
        for (int col = 0; col < gridSize; col++) {
            tiles.add(new int[]{row, col});
        }
        return tiles;
    }
}
