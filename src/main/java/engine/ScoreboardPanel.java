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
 *
 * <p>Scores are persisted in {@value #SCORES_FILE} as CSV with three comma-separated
 * fields per line. On construction the file is read and sorted descending by score.
 * A legacy two-field format (no name) is also handled gracefully.</p>
 *
 * <p>Only the top 10 entries are kept on disk; excess entries are dropped after each
 * {@link #saveScore(String, int, int)} call.</p>
 *
 * @author Project Team
 * @version 1.0
 */
public class ScoreboardPanel extends JPanel {

    /** Preferred panel width in pixels. */
    private static final int WIDTH  = 800;

    /** Preferred panel height in pixels. */
    private static final int HEIGHT = 700;

    /** Name of the local file used to persist high scores. */
    private static final String SCORES_FILE = "gridbeat_scores.txt";

    /** In-memory list of loaded score entries, sorted descending by score. */
    private List<ScoreEntry> scores;

    /** Callback invoked when the player clicks the Back button. */
    private Runnable onBack;

    /** Bounding rectangle of the Back button. */
    private Rectangle backButton;

    /** {@code true} when the mouse is hovering over the Back button. */
    private boolean backHovered;

    /**
     * Constructs a new {@code ScoreboardPanel}: sets preferred size and background,
     * positions the Back button, loads scores from disk, and registers mouse listeners.
     */
    public ScoreboardPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(5, 5, 16));

        backButton = new Rectangle(50, 620, 150, 50);
        scores = new ArrayList<>();
        loadScores();

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (backButton.contains(e.getPoint()) && onBack != null) {
                    SoundManager.click();
                    onBack.run();
                }
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

    /**
     * Reads all score entries from {@value #SCORES_FILE}, parses each line, and
     * rebuilds the in-memory list sorted descending by score. Missing or malformed
     * files are handled silently (an empty list is returned).
     *
     * <p>Supports both the current three-field format ({@code name,score,level}) and
     * a legacy two-field format ({@code score,level}), substituting {@code "???"} for
     * the missing name.</p>
     */
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
     *
     * <p>The name is sanitised: commas are stripped, leading/trailing whitespace
     * removed, length capped at 16 characters, and the result is uppercased.
     * The list is re-sorted after insertion and trimmed to at most 10 entries
     * before writing to {@value #SCORES_FILE}.</p>
     *
     * @param name  the player's display name; sanitised before storage
     * @param score the final accumulated score
     * @param level the highest difficulty level reached
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

    /**
     * Paints the scoreboard: title, separator, column headers, up to 10 score rows
     * (gold/silver/bronze highlights for the top three), an empty-state message
     * when there are no scores, and the Back button.
     *
     * @param g the Swing {@link Graphics} context
     */
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

    /**
     * Registers the callback invoked when the player clicks the Back button.
     *
     * @param callback the {@link Runnable} to execute; must not be {@code null}
     */
    public void setOnBack(Runnable callback) { this.onBack = callback; }

    // ── Inner class ──────────────────────────────────────────────────────────

    /**
     * Immutable value object representing one high-score entry.
     * Implements {@link Comparable} to sort in descending score order.
     */
    private static class ScoreEntry implements Comparable<ScoreEntry> {

        /** Player display name. */
        String name;

        /** Accumulated score for this session. */
        /** Highest difficulty level reached in this session. */
        int    score, level;

        /**
         * Constructs a new {@code ScoreEntry}.
         *
         * @param name  player display name
         * @param score accumulated score
         * @param level highest level reached
         */
        ScoreEntry(String name, int score, int level) {
            this.name = name; this.score = score; this.level = level;
        }

        /**
         * Compares this entry to another for descending score ordering.
         *
         * @param o the entry to compare against
         * @return negative if this entry's score is higher, positive if lower
         */
        @Override
        public int compareTo(ScoreEntry o) { return Integer.compare(o.score, score); }
    }
}
