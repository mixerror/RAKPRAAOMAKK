package patterns;

import grid.Board;

import java.util.ArrayList;
import java.util.List;

public class ColumnHazard extends Hazard{

    public ColumnHazard(Board board, int col, int countdown) {
        super(board, createColumnTiles(col, board.getGridSize()), countdown);
    }

    private static List<int[]> createColumnTiles(int col, int gridSize) {
        List<int[]> tiles = new ArrayList<>();
        for (int row = 0; row < gridSize; row++) {
            tiles.add(new int[]{row, col});
        }
        return tiles;
    }
}
