package interfaces;

/**
 * INTERFACE: Playable
 * Any stage/level that can be started, paused, and completed.
 * Implemented by: Stage
 */
public interface Playable {
    void start();
    void pause();
    boolean isCompleted();
}
