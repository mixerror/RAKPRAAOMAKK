package engine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CLASS: ScoreboardPanel
 * Displays top 10 high scores stored locally in gridbeat_scores.txt
 * File format per line:  name,score,level
 */
public class ScoreboardPanel extends JPanel {

    private static final int WIDTH  = 800;
    private static final int HEIGHT = 700;
    private static final String SCORES_FILE = "gridbeat_scores.txt";

    private List<ScoreEntry> scores;
    private Runnable onBack;
    private Rectangle backButton;
    private boolean backHovered;

    public ScoreboardPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(5, 5, 16));

        backButton = new Rectangle(50, 620, 150, 50);
        scores = new ArrayList<>();
        loadScores();

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (backButton.contains(e.getPoint()) && onBack != null) onBack.run();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                boolean h = backButton.contains(e.getPoint());
                if (h != backHovered) { backHovered = h; repaint(); }
            }
        });
    }

    // ── Load ─────────────────────────────────────────────────────────────────

    public void loadScores() {
        scores.clear();
        try {
            File file = new File(SCORES_FILE);
            if (!file.exists()) return;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3) {
                    scores.add(new ScoreEntry(
                            parts[0].trim(),
                            Integer.parseInt(parts[1].trim()),
                            Integer.parseInt(parts[2].trim())));
                } else if (parts.length == 2) {
                    // Legacy format (no name)
                    scores.add(new ScoreEntry("???",
                            Integer.parseInt(parts[0].trim()),
                            Integer.parseInt(parts[1].trim())));
                }
            }
            reader.close();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        Collections.sort(scores);
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    /**
     * Add a named score entry and persist the top-10 to disk.
     */
    public void saveScore(String name, int score, int level) {
        String safeName = name.replaceAll(",", "").trim();
        if (safeName.isEmpty()) safeName = "PLAYER";
        if (safeName.length() > 16) safeName = safeName.substring(0, 16).toUpperCase();

        scores.add(new ScoreEntry(safeName.toUpperCase(), score, level));
        Collections.sort(scores);
        if (scores.size() > 10) scores = new ArrayList<>(scores.subList(0, 10));

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(SCORES_FILE));
            for (ScoreEntry entry : scores) {
                writer.write(entry.name + "," + entry.score + "," + entry.level);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        repaint();
    }

    // ── Render ───────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Title
        g2d.setFont(new Font("Monospaced", Font.BOLD, 48));
        g2d.setColor(new Color(0, 255, 204));
        String title = "HIGH SCORES";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, 90);

        // Separator
        g2d.setColor(new Color(129, 140, 248, 80));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(100, 110, 700, 110);

        // Headers
        g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2d.setColor(new Color(129, 140, 248));
        g2d.drawString("RANK",  110, 150);
        g2d.drawString("NAME",  200, 150);
        g2d.drawString("SCORE", 460, 150);
        g2d.drawString("LEVEL", 620, 150);
        g2d.setColor(new Color(129, 140, 248, 60));
        g2d.drawLine(100, 160, 700, 160);

        // Rows
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 18));
        int y = 200;
        for (int i = 0; i < Math.min(scores.size(), 10); i++) {
            ScoreEntry entry = scores.get(i);
            Color rc;
            if      (i == 0) rc = new Color(251, 191,  36);
            else if (i == 1) rc = new Color(192, 192, 192);
            else if (i == 2) rc = new Color(205, 127,  50);
            else             rc = new Color(226, 232, 240);

            if (i < 3) {
                g2d.setColor(new Color(rc.getRed(), rc.getGreen(), rc.getBlue(), 20));
                g2d.fillRoundRect(100, y - 18, 600, 28, 6, 6);
            }
            g2d.setColor(rc);
            g2d.drawString("#" + (i + 1),               110, y);
            g2d.drawString(entry.name,                   200, y);
            g2d.drawString(String.valueOf(entry.score),  460, y);
            g2d.drawString("LVL " + entry.level,         620, y);
            y += 38;
        }

        // Empty state
        if (scores.isEmpty()) {
            g2d.setFont(new Font("Monospaced", Font.ITALIC, 17));
            g2d.setColor(new Color(129, 140, 248, 150));
            String msg = "No scores yet — play to set a record!";
            fm = g2d.getFontMetrics();
            g2d.drawString(msg, (WIDTH - fm.stringWidth(msg)) / 2, 320);
        }

        // Back button
        if (backHovered) {
            g2d.setColor(new Color(0, 255, 204, 30));
            g2d.fillRoundRect(backButton.x, backButton.y, backButton.width, backButton.height, 8, 8);
        }
        g2d.setColor(backHovered ? new Color(0, 255, 204) : new Color(129, 140, 248));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(backButton.x, backButton.y, backButton.width, backButton.height, 8, 8);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
        String backText = "BACK";
        fm = g2d.getFontMetrics();
        g2d.drawString(backText,
                backButton.x + (backButton.width  - fm.stringWidth(backText)) / 2,
                backButton.y + (backButton.height + fm.getAscent()) / 2 - 5);
    }

    public void setOnBack(Runnable callback) { this.onBack = callback; }

    // ── Inner class ──────────────────────────────────────────────────────────

    private static class ScoreEntry implements Comparable<ScoreEntry> {
        String name;
        int    score, level;

        ScoreEntry(String name, int score, int level) {
            this.name = name; this.score = score; this.level = level;
        }

        @Override
        public int compareTo(ScoreEntry o) { return Integer.compare(o.score, score); }
    }
}