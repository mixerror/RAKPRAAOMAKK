package interfaces;

/**
 * INTERFACE: Updatable
 * Any object that needs to update its state each frame.
 *
 * <p>Implemented by {@link patterns.Hazard} and all its concrete subclasses.
 * The {@link engine.GameEngine} calls {@link #update()} on every active
 * {@code Updatable} each frame so hazards can progress through their
 * warning → active → finished lifecycle independently.</p>
 *
 * @author Project Team
 * @version 1.0
 */
public interface Updatable {

    /**
     * Advances this object's internal state by one game tick (one beat period).
     * Called once per frame by the game loop.
     */
    void update();
}
