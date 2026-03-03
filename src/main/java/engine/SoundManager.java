package engine;

/**
 * CLASS: SoundManager
 * Simple singleton accessor so any UI class can trigger sound effects
 * without needing a reference passed through every constructor.
 */
public class SoundManager {
    private static BeatSoundEngine instance;

    public static void register(BeatSoundEngine engine) {
        instance = engine;
    }

    public static void click() {
        if (instance != null) instance.playClickSound();
    }

    public static void hurt() {
        if (instance != null) instance.playHurtSound();
    }

    public static void gameOver() {
        if (instance != null) instance.playGameOverSound();
    }
}
