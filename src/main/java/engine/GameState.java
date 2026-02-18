package engine;

/**
 * CLASS: GameState
 * Manages all game state: score, lives, combo, level progression
 */
public class GameState {
    
    private int score;
    private int combo;
    private int lives;
    private int level;
    private int beatsSurvived;
    private int beatsToNextLevel;
    
    private static final int MAX_COMBO = 8;
    private static final int STARTING_LIVES = 3;
    private static final int BASE_BPM = 120;
    
    public GameState() {
        reset();
    }
    
    public void reset() {
        score = 0;
        combo = 1;
        lives = STARTING_LIVES;
        level = 1;
        beatsSurvived = 0;
        beatsToNextLevel = 20;
    }
    
    public void addScore(int points) {
        score += points * combo;
    }
    
    public void incrementCombo() {
        combo = Math.min(combo + 1, MAX_COMBO);
    }
    
    public void resetCombo() {
        combo = 1;
    }
    
    public void loseLife() {
        lives--;
        resetCombo();
    }
    
    public void surviveBeat() {
        beatsSurvived++;
        if (beatsSurvived >= beatsToNextLevel) {
            levelUp();
        }
    }
    
    private void levelUp() {
        level = Math.min(level + 1, 10);
        beatsSurvived = 0;
        beatsToNextLevel = 20;
    }
    
    public int getBPM() {
        return BASE_BPM + (level - 1) * 10;
    }
    
    public boolean isGameOver() {
        return lives <= 0;
    }
    
    // Getters
    public int getScore() { return score; }
    public int getCombo() { return combo; }
    public int getLives() { return lives; }
    public int getLevel() { return level; }
}
