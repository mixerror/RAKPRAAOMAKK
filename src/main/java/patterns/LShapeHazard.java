package patterns;

import grid.Board;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CONCRETE CLASS: LShapeHazard
 * Extends Hazard - targets an L-shaped pattern from a corner
 * INHERITANCE: LShapeHazard → Hazard
 *
 * <p>Combines a vertical arm and a horizontal arm meeting at one of the four
 * corners of the grid. Each arm has length {@code gridSize/2 + 1}, and the
 * shared corner tile is not duplicated. The corner is chosen randomly at
 * construction time, keeping the pattern unpredictable across sessions.</p>
 *
 * @author Project Team
 * @version 1.0
 * @see Hazard
 */
public class LShapeHazard extends Hazard {
    
    /**
     * Constructs an {@code LShapeHazard} anchored at a randomly chosen corner.
     *
     * @param board     the game board
     * @param countdown beats of warning before the L-shape fires; must be &gt;= 0
     */
    public LShapeHazard(Board board, int countdown) {
        super(board, createLShapeTiles(board.getGridSize()), countdown);
    }
    
    /**
     * Builds the tile list for an L-shape anchored at a random corner.
     *
     * <p>Picks one of the four corners at random (0 = top-left, 1 = top-right,
     * 2 = bottom-left, 3 = bottom-right), then constructs a vertical arm
     * (moving away from the corner edge) and a horizontal arm (extending from
     * the end of the vertical arm). The corner tile is included in the vertical
     * arm, so the horizontal arm starts at index 1 to avoid duplication.</p>
     *
     * @param gridSize number of rows/columns in the square grid
     * @return list of {@code {row, col}} arrays forming the L-shape
     */
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
