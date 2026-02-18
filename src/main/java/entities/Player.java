package entities;

import grid.Board;
import grid.GridCell;
import interfaces.Renderable;
import java.awt.*;

/**
 * CLASS: Player
 * The player character that moves on the grid.
 * Renders as a glowing cyan square.
 */
public class Player implements Renderable {
    
    private int gridRow, gridCol;
    private Board board;
    private float pulseTime;
    
    private static final Color PLAYER_COLOR = new Color(0, 255, 204);
    
    public Player(int startRow, int startCol, Board board) {
        this.gridRow = startRow;
        this.gridCol = startCol;
        this.board = board;
        this.pulseTime = 0;
    }
    
    public void move(int deltaRow, int deltaCol) {
        int newRow = gridRow + deltaRow;
        int newCol = gridCol + deltaCol;
        
        if (board.isValidPosition(newRow, newCol)) {
            gridRow = newRow;
            gridCol = newCol;
        }
    }
    
    public void update() {
        pulseTime += 0.1f;
    }
    
    @Override
    public void render(Graphics2D g) {
        GridCell cell = board.getCell(gridRow, gridCol);
        if (cell == null) return;
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int x = cell.getX() + 8;
        int y = cell.getY() + 8;
        int size = cell.getSize() - 16;
        
        // Pulsing glow effect
        float pulse = (float)(Math.sin(pulseTime) * 0.3 + 0.7);
        int glowSize = (int)(size * (1 + pulse * 0.15f));
        int glowX = x - (glowSize - size) / 2;
        int glowY = y - (glowSize - size) / 2;
        
        // Outer glow
        g.setColor(new Color(0, 255, 204, (int)(50 * pulse)));
        g.fillRoundRect(glowX, glowY, glowSize, glowSize, 6, 6);
        
        // Inner player
        g.setColor(PLAYER_COLOR);
        g.fillRoundRect(x, y, size, size, 6, 6);
        
        // Bright center highlight
        g.setColor(new Color(255, 255, 255, 150));
        g.fillRoundRect(x + size/3, y + size/3, size/3, size/3, 3, 3);
    }
    
    public int getGridRow() { return gridRow; }
    public int getGridCol() { return gridCol; }
}
