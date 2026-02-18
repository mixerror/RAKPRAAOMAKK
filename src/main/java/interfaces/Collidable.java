package interfaces;

import java.awt.Rectangle;

/**
 * INTERFACE: Collidable
 * Any game object that can collide with others must implement this.
 * Implemented by: Player, Obstacle
 */
public interface Collidable {
    Rectangle getBounds();
    void onCollision();
}
