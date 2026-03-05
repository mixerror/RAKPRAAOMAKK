package engine;

/**
 * CLASS: SoundManager
 * Simple singleton accessor so any UI class can trigger sound effects
 * without needing a reference passed through every constructor.
 *
 * <p>Register a live {@link BeatSoundEngine} once via {@link #register(BeatSoundEngine)}
 * at application start-up. All static trigger methods silently do nothing if no
 * engine has been registered, so callers are always safe to call them.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   SoundManager.register(new BeatSoundEngine());
 *   SoundManager.click();    // play UI feedback
 *   SoundManager.hurt();     // player hit
 *   SoundManager.gameOver(); // end of session
 * </pre>
 *
 * @author Project Team
 * @version 1.0
 */
public class SoundManager {

    /** The registered engine; {@code null} until {@link #register(BeatSoundEngine)} is called. */
    private static BeatSoundEngine instance;

    /**
     * Registers a {@link BeatSoundEngine} for use by the static trigger methods.
     * Pass {@code null} to clear the current registration.
     *
     * @param engine the engine to register; may be {@code null}
     */
    public static void register(BeatSoundEngine engine) {
        instance = engine;
    }

    /**
     * Plays the UI button-click feedback sound.
     * No-op if no engine has been registered.
     */
    public static void click() {
        if (instance != null) instance.playClickSound();
    }

    /**
     * Plays the player-hit hurt sound effect.
     * No-op if no engine has been registered.
     */
    public static void hurt() {
        if (instance != null) instance.playHurtSound();
    }

    /**
     * Plays the game-over audio cue.
     * No-op if no engine has been registered.
     */
    public static void gameOver() {
        if (instance != null) instance.playGameOverSound();
    }
}
