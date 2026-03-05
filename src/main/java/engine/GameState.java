package engine;

/**
 * Manages all mutable game state for a single play session, including score,
 * combo multiplier, remaining lives, difficulty level, and beat progression.
 *
 * <p>All score mutations should pass through {@link #addScore(int)} so the combo
 * multiplier is applied consistently. The level cap is 10; after level 10 the
 * BPM and beat thresholds no longer increase.</p>
 *
 * <p>Typical usage:</p>
 * <pre>
 *   GameState state = new GameState();
 *   state.incrementCombo();
 *   state.addScore(100);     // awards 100 * combo points
 *   state.surviveBeat();     // may trigger level-up
 *   if (state.isGameOver()) { showGameOverScreen(); }
 * </pre>
 *
 * @author Project Team
 * @version 1.0
 */
public class GameState {

    private int score;
    private int combo;
    private int lives;
    private int level;
    private int beatsSurvived;
    private int beatsToNextLevel;

    /** Maximum combo multiplier the player can reach. */
    private static final int MAX_COMBO = 8;

    /** Number of lives the player starts each session with. */
    private static final int STARTING_LIVES = 3;

    /** Base beats-per-minute at level 1 before any level scaling. */
    private static final int BASE_BPM = 120;

    /**
     * Constructs a new {@code GameState} and immediately calls {@link #reset()}
     * to initialise all fields to their starting values.
     */
    public GameState() {
        reset();
    }

    /**
     * Resets all fields to their initial values, preparing this instance for
     * a fresh game session.
     *
     * <p>Sets: score = 0, combo = 1, lives = {@value #STARTING_LIVES},
     * level = 1, beatsSurvived = 0, beatsToNextLevel = 20.</p>
     */
    public void reset() {
        score = 0;
        combo = 1;
        lives = STARTING_LIVES;
        level = 1;
        beatsSurvived = 0;
        beatsToNextLevel = 20;
    }

    /**
     * Adds points to the score, scaled by the current combo multiplier.
     *
     * <p>Effective award = {@code points * combo}.</p>
     *
     * @param points base point value to award; should be &gt;= 0
     */
    public void addScore(int points) {
        score += points * combo;
    }

    /**
     * Increases the combo multiplier by 1, up to the maximum of {@value #MAX_COMBO}.
     * Call this each time the player successfully survives an active hazard.
     */
    public void incrementCombo() {
        combo = Math.min(combo + 1, MAX_COMBO);
    }

    /**
     * Resets the combo multiplier back to 1.
     * Called automatically by {@link #loseLife()}.
     */
    public void resetCombo() {
        combo = 1;
    }

    /**
     * Decrements remaining lives by 1 and resets the combo multiplier.
     * Has no effect if the player is already at 0 lives (game over state).
     */
    public void loseLife() {
        lives--;
        resetCombo();
    }

    /**
     * Records one survived beat and triggers a level-up when the per-level
     * beat threshold is reached.
     *
     * @see #levelUp()
     */
    public void surviveBeat() {
        beatsSurvived++;
        if (beatsSurvived >= beatsToNextLevel) {
            levelUp();
        }
    }

    /**
     * Advances the game to the next difficulty level, resets the beat counter,
     * and reduces the threshold for future level-ups to 15 beats.
     * The level is capped at 10.
     */
    private void levelUp() {
        level = Math.min(level + 1, 10);
        beatsSurvived = 0;
        beatsToNextLevel = 15; // Was 20, now 15 - faster level ups!
    }

    /**
     * Returns the current playback tempo in beats-per-minute.
     *
     * <p>Formula: {@code BASE_BPM + (level - 1) * 15}</p>
     *
     * @return BPM value; always positive
     */
    public int getBPM() {
        return BASE_BPM + (level - 1) * 15; // Was +10, now +15 per level
    }

    /**
     * Returns whether all lives have been exhausted, signalling the end of
     * the current game session.
     *
     * @return {@code true} when lives &lt;= 0
     */
    public boolean isGameOver() {
        return lives <= 0;
    }

    /**
     * Returns the player's accumulated score for this session.
     *
     * @return current score; always &gt;= 0
     */
    public int getScore() { return score; }

    /**
     * Returns the current combo multiplier.
     *
     * @return combo value in the range [1, {@value #MAX_COMBO}]
     */
    public int getCombo() { return combo; }

    /**
     * Returns the number of lives remaining.
     *
     * @return remaining lives; 0 indicates game over
     */
    public int getLives() { return lives; }

    /**
     * Returns the current difficulty level.
     *
     * @return level in the range [1, 10]
     */
    public int getLevel() { return level; }
}
