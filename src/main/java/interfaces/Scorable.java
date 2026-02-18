package interfaces;

/**
 * INTERFACE: Scorable
 * Any object that tracks and manages a score.
 * Implemented by: ScoreManager
 */
public interface Scorable {
    int getScore();
    void addScore(int pts);
}
