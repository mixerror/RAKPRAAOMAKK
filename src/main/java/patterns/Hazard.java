package patterns;

import grid.Board;
import interfaces.Updatable;
import java.util.List;

/**
 * ABSTRACT CLASS: Hazard
 * Base class for all hazard patterns (row, column, spots, cross, etc.)
 * Each hazard has a countdown before becoming active.
 *
 * <p>A hazard targets a fixed set of {@link grid.GridCell} tile coordinates and
 * progresses through a three-phase lifecycle driven by beat events:</p>
 * <ol>
 *   <li><strong>Warning</strong> — {@code countdown > 0}; targeted cells display a
 *       countdown number so the player has time to react.</li>
 *   <li><strong>Active</strong> — countdown reaches 0 and {@link #activate()} is called;
 *       cells fire and the engine checks for player collision.</li>
 *   <li><strong>Finished</strong> — one beat after activation, {@link #finish()} is called;
 *       cells are reset and the engine removes this hazard from the active list.</li>
 * </ol>
 *
 * <p>Concrete subclasses only need to supply the tile-coordinate list via the
 * protected constructor — the entire lifecycle is managed here.</p>
 *
 * @author Project Team
 * @version 1.0
 * @see RowHazard
 * @see ColumnHazard
 * @see DiagonalHazard
 * @see XShapeHazard
 * @see LShapeHazard
 * @see BorderHazard
 * @see CornersHazard
 * @see CheckerHazard
 */
public abstract class Hazard implements Updatable {
    
    /** The game board; used to access {@link grid.GridCell} instances during lifecycle transitions. */
    protected Board board;

    /** List of {@code {row, col}} coordinate pairs that this hazard targets. */
    protected List<int[]> tiles;  // List of [row, col] positions

    /** Beats remaining before this hazard activates. Decremented by {@link #decrementCountdown()}. */
    protected int countdown;

    /** {@code true} once {@link #activate()} has been called; {@code false} initially. */
    protected boolean active;

    /** {@code true} once {@link #finish()} has been called; signals removal by the engine. */
    protected boolean finished;
    
    /**
     * Constructs a new {@code Hazard} targeting the given tile list with the
     * specified countdown. Immediately marks all targeted cells as danger via
     * {@link grid.GridCell#setDanger(int)}.
     *
     * @param board     the game board
     * @param tiles     list of {@code {row, col}} arrays identifying targeted cells; must not be {@code null}
     * @param countdown beats of warning before activation; must be &gt;= 0
     */
    public Hazard(Board board, List<int[]> tiles, int countdown) {
        this.board = board;
        this.tiles = tiles;
        this.countdown = countdown;
        this.active = false;
        this.finished = false;
    }
    
    /**
     * Advances the hazard by one game tick.
     *
     * <p>If not yet finished and not active, marks all targeted cells as danger
     * (warning phase). If active, marks all targeted cells as active (fired phase).
     * Note: transition logic (decrement/activate/finish) is driven externally by
     * {@link engine.GameEngine#onBeat()} rather than inside this method.</p>
     */
    @Override
    public void update() {
        if (finished) return;
        
        if (!active) {
            // Warning phase
            for (int[] pos : tiles) {
                board.getCell(pos[0], pos[1]).setDanger(countdown);
            }
        } else {
            // Active phase
            for (int[] pos : tiles) {
                board.getCell(pos[0], pos[1]).setActive();
            }
        }
    }
    
    /**
     * Decrements the beat countdown by 1 and calls {@link #activate()} when it
     * reaches zero. Has no effect if the countdown is already zero.
     */
    public void decrementCountdown() {
        if (countdown > 0) {
            countdown--;
        }
        if (countdown == 0 && !active) {
            activate();
        }
    }
    
    /**
     * Transitions this hazard from the warning phase to the active (fired) phase
     * by setting {@link #active} to {@code true}.
     */
    public void activate() {
        active = true;
    }
    
    /**
     * Completes the hazard lifecycle by setting {@link #finished} to {@code true}.
     * The engine will remove this hazard from its active list on the next frame.
     */
    public void finish() {
        finished = true;
    }
    
    /**
     * Returns whether this hazard is currently in the active (fired) phase.
     *
     * @return {@code true} after {@link #activate()} and before (or after) {@link #finish()}
     */
    public boolean isActive() { return active; }

    /**
     * Returns whether this hazard has completed its full lifecycle.
     *
     * @return {@code true} after {@link #finish()} has been called
     */
    public boolean isFinished() { return finished; }
    
    /**
     * Returns whether the given grid position is targeted by this hazard while it
     * is in the active phase. Used by the engine for player collision detection.
     *
     * @param playerRow the player's current row index
     * @param playerCol the player's current column index
     * @return {@code true} if this hazard is active and {@code (playerRow, playerCol)}
     *         is in the targeted tile list
     */
    public boolean checkHit(int playerRow, int playerCol) {
        if (!active) return false;
        
        for (int[] pos : tiles) {
            if (pos[0] == playerRow && pos[1] == playerCol) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the list of {@code {row, col}} int arrays identifying the tiles
     * targeted by this hazard.
     *
     * @return unmodifiable view of the tile list; never {@code null}
     */
    public List<int[]> getTiles() { return tiles; }
}
