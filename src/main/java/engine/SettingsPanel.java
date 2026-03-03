package engine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * CLASS: SettingsPanel
 * Allows player to change board visual style/theme
 */
public class SettingsPanel extends JPanel {
    
    private static final int WIDTH = 800;
    private static final int HEIGHT = 700;
    
    public enum BoardStyle {
        DARK_BLUE("Dark Blue", new Color(17, 17, 40), new Color(26, 26, 58)),
        PURPLE("Purple", new Color(30, 20, 50), new Color(45, 30, 70)),
        GREEN("Matrix", new Color(10, 30, 10), new Color(15, 45, 15)),
        RED("Crimson", new Color(40, 10, 10), new Color(60, 15, 15)),
        CYBERPUNK("Cyberpunk", new Color(20, 5, 30), new Color(35, 10, 45));
        
        final String name;
        public final Color dark;
        public final Color light;
        
        BoardStyle(String name, Color dark, Color light) {
            this.name = name;
            this.dark = dark;
            this.light = light;
        }
    }
    
    private BoardStyle selectedStyle;
    private Runnable onBack;
    private Rectangle backButton;
    
    public SettingsPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(5, 5, 16));
        
        selectedStyle = BoardStyle.DARK_BLUE;
        backButton = new Rectangle(50, 600, 150, 50);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (backButton.contains(e.getPoint())) {
                    if (onBack != null) onBack.run();
                    return;
                }
                
                // Check style button clicks
                int startY = 200;
                int spacing = 80;
                for (int i = 0; i < BoardStyle.values().length; i++) {
                    Rectangle styleButton = new Rectangle(250, startY + i * spacing, 300, 60);
                    if (styleButton.contains(e.getPoint())) {
                        selectedStyle = BoardStyle.values()[i];
                        repaint();
                    }
                }
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Title
        g2d.setFont(new Font("Monospaced", Font.BOLD, 48));
        g2d.setColor(new Color(0, 255, 204));
        String title = "SETTINGS";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, 100);
        
        // Subtitle
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g2d.setColor(new Color(129, 140, 248));
        String subtitle = "Board Style";
        g2d.drawString(subtitle, 250, 170);
        
        // Style buttons
        int startY = 200;
        int spacing = 80;
        for (int i = 0; i < BoardStyle.values().length; i++) {
            BoardStyle style = BoardStyle.values()[i];
            Rectangle styleButton = new Rectangle(250, startY + i * spacing, 300, 60);
            
            boolean isSelected = style == selectedStyle;
            
            // Button background
            if (isSelected) {
                g2d.setColor(new Color(0, 255, 204, 30));
                g2d.fillRoundRect(styleButton.x, styleButton.y, styleButton.width, styleButton.height, 8, 8);
            }
            
            // Color preview boxes
            g2d.setColor(style.dark);
            g2d.fillRect(styleButton.x + 10, styleButton.y + 15, 30, 30);
            g2d.setColor(style.light);
            g2d.fillRect(styleButton.x + 45, styleButton.y + 15, 30, 30);
            
            // Button border
            g2d.setColor(isSelected ? new Color(0, 255, 204) : new Color(129, 140, 248));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(styleButton.x, styleButton.y, styleButton.width, styleButton.height, 8, 8);
            
            // Button text
            g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
            g2d.setColor(isSelected ? new Color(0, 255, 204) : new Color(226, 232, 240));
            g2d.drawString(style.name, styleButton.x + 90, styleButton.y + 38);
        }
        
        // Back button
        g2d.setColor(new Color(129, 140, 248));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(backButton.x, backButton.y, backButton.width, backButton.height, 8, 8);
        
        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
        String backText = "BACK";
        fm = g2d.getFontMetrics();
        g2d.drawString(backText, 
            backButton.x + (backButton.width - fm.stringWidth(backText)) / 2,
            backButton.y + (backButton.height + fm.getAscent()) / 2 - 5);
    }
    
    public void setOnBack(Runnable callback) { this.onBack = callback; }
    public BoardStyle getSelectedStyle() { return selectedStyle; }
}
