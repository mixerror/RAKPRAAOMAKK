package engine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * CLASS: GameLauncher
 * Main entry point - manages menu screens and game flow.
 * Shows a styled name-entry dialog on game over before saving the score.
 *
 * <p>Extends {@link JFrame} and uses a {@link CardLayout} to host four named screens:
 * {@code MENU}, {@code GAME}, {@code SCOREBOARD}, and {@code SETTINGS}. Navigation between
 * screens is wired by registering {@link Runnable} callbacks on each child panel.</p>
 *
 * @author Project Team
 * @version 1.0
 * @see GameEngine
 * @see MainMenu
 */
public class GameLauncher extends JFrame {

    /** Layout manager that switches between the four application screens. */
    private CardLayout cardLayout;

    /** Root container that holds all screen panels under {@link #cardLayout}. */
    private JPanel cardPanel;

    /** The animated main menu screen. */
    private MainMenu       mainMenu;

    /** The gameplay panel and game-loop controller. */
    private GameEngine     gameEngine;

    /** The high-score display panel. */
    private ScoreboardPanel scoreboardPanel;

    /** The board-theme selection settings panel. */
    private SettingsPanel  settingsPanel;

    /** dont forget to fill this !!!!!!!!!!! */
    private TutorialPanel tutorialPanel;

    /**
     * Constructs the application window: creates all screen panels, wires
     * navigation callbacks between them, adds them to the card layout, packs
     * the frame, centres it on screen, and shows the main menu.
     */
    public GameLauncher() {
        setTitle("GRID BEAT");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);

        mainMenu        = new MainMenu();
        gameEngine      = new GameEngine();
        scoreboardPanel = new ScoreboardPanel();
        settingsPanel   = new SettingsPanel();
        tutorialPanel = new TutorialPanel();

        setupMainMenu();
        setupGameEngine();
        setupScoreboard();
        setupSettings();
        setupTutorial();

        cardPanel.add(mainMenu,        "MENU");
        cardPanel.add(gameEngine,      "GAME");
        cardPanel.add(scoreboardPanel, "SCOREBOARD");
        cardPanel.add(settingsPanel,   "SETTINGS");
        cardPanel.add(tutorialPanel, "TUTORIAL");

        add(cardPanel);
        pack();
        setLocationRelativeTo(null);
        showMainMenu();
    }

    // ── Setup ────────────────────────────────────────────────────────────────

    /**
     * Wires the main menu button callbacks: start game, show scoreboard,
     * show settings, and quit.
     */
    private void setupMainMenu() {
        mainMenu.setOnStartGame(() -> {
            gameEngine.applyTheme(settingsPanel.getSelectedStyle());
            SoundManager.setVolume(settingsPanel.getSelectedVolume());
            cardLayout.show(cardPanel, "GAME");
            gameEngine.requestFocus();
            gameEngine.startGame();
        });
        mainMenu.setOnShowTutorial(() -> {
            tutorialPanel.reset();
            cardLayout.show(cardPanel, "TUTORIAL");
        });
        mainMenu.setOnShowScoreboard(() -> {
            scoreboardPanel.loadScores();
            cardLayout.show(cardPanel, "SCOREBOARD");
        });
        mainMenu.setOnShowSettings(() -> cardLayout.show(cardPanel, "SETTINGS"));
        mainMenu.setOnQuit(() -> System.exit(0));
    }

    /**
     * Wires the game engine callbacks: shows the name-entry dialog and saves
     * the score on game over; returns to the main menu when the player clicks
     * "Main Menu" on the game-over screen.
     */
    private void setupGameEngine() {
        // Called by GameEngine when the run ends — ask for name, then save
        gameEngine.setOnGameOver((score, level) -> {
            String name = askPlayerName(score, level);
            scoreboardPanel.saveScore(name, score, level);
        });

        // Called when player clicks "Main Menu" on the game-over screen
        gameEngine.setOnMainMenu(this::showMainMenu);
    }

    /**
     * Wires the scoreboard panel's back button to return to the main menu.
     */
    private void setupScoreboard() {
        scoreboardPanel.setOnBack(this::showMainMenu);
    }

    /**
     * Wires the settings panel's back button to return to the main menu.
     */
    private void setupSettings() {
        settingsPanel.setOnBack(this::showMainMenu);
    }

    /**
     * Don't forget to fill this!!!!!!!!!!!
     */
    private void setupTutorial() {
        tutorialPanel.setOnComplete(() -> {
            // After tutorial, go straight to game
            gameEngine.applyTheme(settingsPanel.getSelectedStyle());
            SoundManager.setVolume(settingsPanel.getSelectedVolume());
            cardLayout.show(cardPanel, "GAME");
            gameEngine.requestFocus();
            gameEngine.startGame();
        });
    }
    // ── Name-entry dialog ────────────────────────────────────────────────────

    /**
     * Shows a dark, themed dialog asking the player for their name.
     * Returns the entered name, or "PLAYER" if left blank / cancelled.
     *
     * <p>The dialog is modal and blocks until the player confirms. The name
     * is trimmed, stripped of commas (CSV safety), truncated to 16 characters,
     * and uppercased before being returned.</p>
     *
     * @param score the final score to display in the dialog
     * @param level the highest level reached to display in the dialog
     * @return the player's chosen display name; never {@code null} or empty
     */
    private String askPlayerName(int score, int level) {
        // Build a custom styled dialog
        JDialog dialog = new JDialog(this, "New Score!", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        // Content panel
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(10, 10, 28));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(0, 255, 204, 80));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Title label
        JLabel titleLbl = new JLabel("GAME OVER");
        titleLbl.setFont(new Font("Monospaced", Font.BOLD, 28));
        titleLbl.setForeground(new Color(239, 68, 68));
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Score label
        JLabel scoreLbl = new JLabel("Score: " + score + "  ·  Level: " + level);
        scoreLbl.setFont(new Font("Monospaced", Font.BOLD, 16));
        scoreLbl.setForeground(new Color(251, 191, 36));
        scoreLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Prompt label
        JLabel promptLbl = new JLabel("Enter your name:");
        promptLbl.setFont(new Font("Monospaced", Font.PLAIN, 15));
        promptLbl.setForeground(new Color(129, 140, 248));
        promptLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Text field
        JTextField nameField = new JTextField(14);
        nameField.setFont(new Font("Monospaced", Font.BOLD, 20));
        nameField.setForeground(new Color(0, 255, 204));
        nameField.setBackground(new Color(20, 20, 45));
        nameField.setCaretColor(new Color(0, 255, 204));
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 255, 204, 120), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setMaximumSize(new Dimension(260, 44));
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // OK button
        JButton okBtn = new JButton("SAVE SCORE") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                        ? new Color(0, 255, 204, 50) : new Color(0, 0, 0, 0));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(0, 255, 204));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
                super.paintComponent(g);
            }
        };
        okBtn.setFont(new Font("Monospaced", Font.BOLD, 16));
        okBtn.setForeground(new Color(0, 255, 204));
        okBtn.setContentAreaFilled(false);
        okBtn.setBorderPainted(false);
        okBtn.setFocusPainted(false);
        okBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        okBtn.setMaximumSize(new Dimension(200, 46));
        okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        final String[] result = {"PLAYER"};

        Runnable submit = () -> {
            SoundManager.click();
            String t = nameField.getText().trim();
            result[0] = t.isEmpty() ? "PLAYER" : t;
            dialog.dispose();
        };

        okBtn.addActionListener(e -> submit.run());
        nameField.addActionListener(e -> submit.run()); // Enter key

        // Assemble
        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(8));
        panel.add(scoreLbl);
        panel.add(Box.createVerticalStrut(24));
        panel.add(promptLbl);
        panel.add(Box.createVerticalStrut(10));
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(okBtn);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        nameField.requestFocusInWindow();
        dialog.setVisible(true); // blocks until dispose()

        return result[0];
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    /**
     * Switches the card layout to the main menu screen and requests focus
     * so keyboard events are routed to the menu panel.
     */
    private void showMainMenu() {
        cardLayout.show(cardPanel, "MENU");
        mainMenu.requestFocus();
    }

    // ── Entry point ──────────────────────────────────────────────────────────

    /**
     * Application entry point. Bootstraps the Swing UI safely on the
     * Event Dispatch Thread.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameLauncher launcher = new GameLauncher();
            launcher.setVisible(true);
        });
    }
}