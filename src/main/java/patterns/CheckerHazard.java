package patterns;

import grid.Board;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCRETE CLASS: CheckerHazard
 * Extends Hazard - targets checkerboard pattern (alternating tiles)
 * INHERITANCE: CheckerHazard → Hazard
 *
 * <p>Targets every other cell in a checkerboard arrangement. When
 * {@code startWithEven = true}, targets all tiles where {@code (row + col) % 2 == 0};
 * when {@code false}, targets the complementary set. The two halves together cover
 * every cell on the board exactly once.</p>
 *
 * <p>This is considered the hardest single-spawn pattern because roughly half the
 * board becomes unavailable, so {@link engine.GameEngine} always adds one extra beat
 * of warning countdown when spawning a {@code CheckerHazard}.</p>
 *
 * @author Project Team
 * @version 1.0
 * @see Hazard
 */
public class CheckerHazard extends Hazard {
    
    /**
     * Constructs a {@code CheckerHazard} for the specified checkerboard half.
     *
     * @param board          the game board
     * @param startWithEven  {@code true} to target "even" tiles where {@code (row+col)%2==0};
     *                       {@code false} to target "odd" tiles
     * @param countdown      beats of warning before the pattern fires; must be &gt;= 0
     */
    public CheckerHazard(Board board, boolean startWithEven, int countdown) {
        super(board, createCheckerTiles(board.getGridSize(), startWithEven), countdown);
    }
    
    /**
     * Builds the tile list for one half of a checkerboard pattern.
     *
     * @param gridSize      number of rows/columns in the square grid
     * @param startWithEven {@code true} to collect tiles where {@code (row+col)%2==0};
     *                      {@code false} for the complementary set
     * @return list of {@code ⌈N²/2⌉} or {@code ⌊N²/2⌋} {@code {row, col}} arrays
     */
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
