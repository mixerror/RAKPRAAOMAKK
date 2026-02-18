package interfaces;

import java.awt.Graphics2D;

/**
 * INTERFACE: Renderable
 * Any object that can be drawn on screen must implement this.
 */
public interface Renderable {
    void render(Graphics2D g);
}
