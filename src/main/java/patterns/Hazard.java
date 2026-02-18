package patterns;

import grid.Board;
import interfaces.Updatable;
import java.util.List;

/**
 * ABSTRACT CLASS: Hazard
 * Base class for all hazard patterns (row, column, spots, cross, etc.)
 * Each hazard has a countdown before becoming active.
 */
public abstract class Hazard implements Updatable {
    
    protected Board board;
    protected List<int[]> tiles;  // List of [row, col] positions
    protected int countdown;
    protected boolean active;
    protected boolean finished;
    
    public Hazard(Board board, List<int[]> tiles, int countdown) {
        this.board = board;
        this.tiles = tiles;
        this.countdown = countdown;
        this.active = false;
        this.finished = false;
    }
    
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
    
    public void decrementCountdown() {
        if (countdown > 0) {
            countdown--;
        }
        if (countdown == 0 && !active) {
            activate();
        }
    }
    
    public void activate() {
        active = true;
    }
    
    public void finish() {
        finished = true;
    }
    
    public boolean isActive() { return active; }
    public boolean isFinished() { return finished; }
    
    public boolean checkHit(int playerRow, int playerCol) {
        if (!active) return false;
        
        for (int[] pos : tiles) {
            if (pos[0] == playerRow && pos[1] == playerCol) {
                return true;
            }
        }
        return false;
    }
    
    public List<int[]> getTiles() { return tiles; }
}
