package patterns;

import grid.Board;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CONCRETE CLASS: LShapeHazard
 * Extends Hazard - targets an L-shaped pattern from a corner
 * INHERITANCE: LShapeHazard → Hazard
 */
public class LShapeHazard extends Hazard {
    
    public LShapeHazard(Board board, int countdown) {
        super(board, createLShapeTiles(board.getGridSize()), countdown);
    }
    
    private static List<int[]> createLShapeTiles(int gridSize) {
        List<int[]> tiles = new ArrayList<>();
        Random rand = new Random();
        
        // Pick a random corner (0=TL, 1=TR, 2=BL, 3=BR)
        int corner = rand.nextInt(4);
        int length = gridSize / 2 + 1; // L-shape length
        
        switch (corner) {
            case 0: // Top-left corner
                // Vertical part (down from top-left)
                for (int i = 0; i < length; i++) {
                    tiles.add(new int[]{i, 0});
                }
                // Horizontal part (right from bottom of vertical)
                for (int i = 1; i < length; i++) {
                    tiles.add(new int[]{length - 1, i});
                }
                break;
                
            case 1: // Top-right corner
                // Vertical part (down from top-right)
                for (int i = 0; i < length; i++) {
                    tiles.add(new int[]{i, gridSize - 1});
                }
                // Horizontal part (left from bottom of vertical)
                for (int i = 1; i < length; i++) {
                    tiles.add(new int[]{length - 1, gridSize - 1 - i});
                }
                break;
                
            case 2: // Bottom-left corner
                // Vertical part (up from bottom-left)
                for (int i = 0; i < length; i++) {
                    tiles.add(new int[]{gridSize - 1 - i, 0});
                }
                // Horizontal part (right from top of vertical)
                for (int i = 1; i < length; i++) {
                    tiles.add(new int[]{gridSize - length, i});
                }
                break;
                
            case 3: // Bottom-right corner
                // Vertical part (up from bottom-right)
                for (int i = 0; i < length; i++) {
                    tiles.add(new int[]{gridSize - 1 - i, gridSize - 1});
                }
                // Horizontal part (left from top of vertical)
                for (int i = 1; i < length; i++) {
                    tiles.add(new int[]{gridSize - length, gridSize - 1 - i});
                }
                break;
        }
        
        return tiles;
    }
}
