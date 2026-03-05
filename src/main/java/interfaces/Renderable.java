package interfaces;

import java.awt.Graphics2D;

/**
 * INTERFACE: Renderable
 * Any object that can be drawn on screen must implement this.
 *
 * <p>Implemented by {@link grid.Board}, {@link grid.GridCell}, and
 * {@link entities.Player}. The {@link engine.GameEngine} calls
 * {@link #render(Graphics2D)} on each {@code Renderable} once per frame
 * during the Swing repaint cycle.</p>
 *
 * <p>Implementors should avoid permanently modifying the graphics state
 * (transforms, clip, stroke, composite) without restoring it afterwards,
 * as the context is shared across multiple renderers in the same frame.</p>
 *
 * @author Project Team
 * @version 1.0
 */
public interface Renderable {

    /**
     * Paints this object onto the supplied 2D graphics context.
     * Called once per frame during the game's repaint cycle.
     *
     * @param g the {@link Graphics2D} context to paint onto; never {@code null}
     */
    void render(Graphics2D g);
}
