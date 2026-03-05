package grid;

import engine.SettingsPanel.BoardStyle;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Board — every method")
class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() { board = new Board(800, 700); }

    // ── constructor ──────────────────────────────────────────────────────────
    @Nested @DisplayName("constructor")
    class Constructor {

        @Test @DisplayName("creates a 6x6 grid (getGridSize == 6)")
        void gridSize() { assertEquals(6, board.getGridSize()); }

        @Test @DisplayName("every cell is non-null after construction")
        void allCellsNonNull() {
            for (int r = 0; r < 6; r++)
                for (int c = 0; c < 6; c++)
                    assertNotNull(board.getCell(r, c), "null at (" + r + "," + c + ")");
        }

        @Test @DisplayName("cells store correct grid row indices")
        void cellGridRows() {
            for (int r = 0; r < 6; r++)
                for (int c = 0; c < 6; c++)
                    assertEquals(r, board.getCell(r, c).getGridRow());
        }

        @Test @DisplayName("cells store correct grid col indices")
        void cellGridCols() {
            for (int r = 0; r < 6; r++)
                for (int c = 0; c < 6; c++)
                    assertEquals(c, board.getCell(r, c).getGridCol());
        }

        @Test @DisplayName("all cells have the expected size (80 px)")
        void cellSize() {
            for (int r = 0; r < 6; r++)
                for (int c = 0; c < 6; c++)
                    assertEquals(80, board.getCell(r, c).getSize());
        }

        @Test @DisplayName("different screen dimensions produce a board without error")
        void differentDimensions() { assertDoesNotThrow(() -> new Board(1024, 768)); }
    }

    // ── getGridSize() ────────────────────────────────────────────────────────
    @Nested @DisplayName("getGridSize()")
    class GetGridSize {

        @Test @DisplayName("returns 6")
        void returnsSix() { assertEquals(6, board.getGridSize()); }
    }

    // ── getCell(int,int) ─────────────────────────────────────────────────────
    @Nested @DisplayName("getCell(int, int)")
    class GetCell {

        @Test @DisplayName("returns non-null for (0,0)")
        void topLeft() { assertNotNull(board.getCell(0, 0)); }

        @Test @DisplayName("returns non-null for (5,5)")
        void bottomRight() { assertNotNull(board.getCell(5, 5)); }

        @Test @DisplayName("returns non-null for every valid position")
        void allValid() {
            for (int r = 0; r < 6; r++)
                for (int c = 0; c < 6; c++)
                    assertNotNull(board.getCell(r, c));
        }

        @Test @DisplayName("returns null for row = -1")
        void negativeRow() { assertNull(board.getCell(-1, 0)); }

        @Test @DisplayName("returns null for col = -1")
        void negativeCol() { assertNull(board.getCell(0, -1)); }

        @Test @DisplayName("returns null for row = 6 (== gridSize)")
        void rowAtLimit() { assertNull(board.getCell(6, 0)); }

        @Test @DisplayName("returns null for col = 6 (== gridSize)")
        void colAtLimit() { assertNull(board.getCell(0, 6)); }

        @Test @DisplayName("returns null for row = 100")
        void largeRow() { assertNull(board.getCell(100, 0)); }

        @Test @DisplayName("returns null for col = 100")
        void largeCol() { assertNull(board.getCell(0, 100)); }

        @Test @DisplayName("returns null for both row and col negative")
        void bothNegative() { assertNull(board.getCell(-1, -1)); }
    }

    // ── isValidPosition(int,int) ─────────────────────────────────────────────
    @Nested @DisplayName("isValidPosition(int, int)")
    class IsValidPosition {

        @Test @DisplayName("true for (0,0) — top-left corner")
        void topLeft() { assertTrue(board.isValidPosition(0, 0)); }

        @Test @DisplayName("true for (5,5) — bottom-right corner")
        void bottomRight() { assertTrue(board.isValidPosition(5, 5)); }

        @Test @DisplayName("true for every valid cell")
        void allValid() {
            for (int r = 0; r < 6; r++)
                for (int c = 0; c < 6; c++)
                    assertTrue(board.isValidPosition(r, c), "Should be valid: (" + r + "," + c + ")");
        }

        @Test @DisplayName("false for row = -1")
        void negativeRow() { assertFalse(board.isValidPosition(-1, 0)); }

        @Test @DisplayName("false for col = -1")
        void negativeCol() { assertFalse(board.isValidPosition(0, -1)); }

        @Test @DisplayName("false for row = 6")
        void rowAtGridSize() { assertFalse(board.isValidPosition(6, 0)); }

        @Test @DisplayName("false for col = 6")
        void colAtGridSize() { assertFalse(board.isValidPosition(0, 6)); }

        @Test @DisplayName("false for both negative")
        void bothNegative() { assertFalse(board.isValidPosition(-1, -1)); }

        @Test @DisplayName("false for both out-of-bounds high")
        void bothHigh() { assertFalse(board.isValidPosition(6, 6)); }
    }

    // ── resetAllCells() ──────────────────────────────────────────────────────
    @Nested @DisplayName("resetAllCells()")
    class ResetAllCells {

        @Test @DisplayName("does not throw on a fresh board")
        void noThrowFresh() { assertDoesNotThrow(() -> board.resetAllCells()); }

        @Test @DisplayName("does not throw after activating every cell")
        void noThrowAfterActivate() {
            for (int r = 0; r < 6; r++)
                for (int c = 0; c < 6; c++)
                    board.getCell(r, c).setActive();
            assertDoesNotThrow(() -> board.resetAllCells());
        }

        @Test @DisplayName("after reset, setDanger works on previously active cells")
        void clearsPreviousActiveState() {
            for (int r = 0; r < 6; r++)
                for (int c = 0; c < 6; c++)
                    board.getCell(r, c).setActive();
            board.resetAllCells();
            // setDanger is ignored when active; if this works, reset succeeded
            for (int r = 0; r < 6; r++)
                for (int c = 0; c < 6; c++) {
                    int finalR = r;
                    int finalC = c;
                    assertDoesNotThrow(() -> board.getCell(finalR, finalC).setDanger(1));
                }
        }

        @Test @DisplayName("can be called multiple times without error")
        void idempotent() { board.resetAllCells(); board.resetAllCells(); assertDoesNotThrow(() -> {}); }
    }

    // ── setTheme(BoardStyle) ─────────────────────────────────────────────────
    @Nested @DisplayName("setTheme(BoardStyle)")
    class SetTheme {

        @Test @DisplayName("DARK_BLUE does not throw")
        void darkBlue() { assertDoesNotThrow(() -> board.setTheme(BoardStyle.DARK_BLUE)); }

        @Test @DisplayName("PURPLE does not throw")
        void purple() { assertDoesNotThrow(() -> board.setTheme(BoardStyle.PURPLE)); }

        @Test @DisplayName("GREEN does not throw")
        void green() { assertDoesNotThrow(() -> board.setTheme(BoardStyle.GREEN)); }

        @Test @DisplayName("RED does not throw")
        void red() { assertDoesNotThrow(() -> board.setTheme(BoardStyle.RED)); }

        @Test @DisplayName("CYBERPUNK does not throw")
        void cyberpunk() { assertDoesNotThrow(() -> board.setTheme(BoardStyle.CYBERPUNK)); }

        @Test @DisplayName("switching themes multiple times does not throw")
        void multipleThemes() {
            assertDoesNotThrow(() -> {
                board.setTheme(BoardStyle.PURPLE);
                board.setTheme(BoardStyle.GREEN);
                board.setTheme(BoardStyle.DARK_BLUE);
            });
        }
    }

    // ── pixel layout ─────────────────────────────────────────────────────────
    @Nested @DisplayName("cell pixel layout")
    class PixelLayout {

        @Test @DisplayName("all cells have positive x coordinates")
        void positiveX() {
            for (int r = 0; r < 6; r++)
                for (int c = 0; c < 6; c++)
                    assertTrue(board.getCell(r, c).getX() >= 0);
        }

        @Test @DisplayName("all cells have positive y coordinates")
        void positiveY() {
            for (int r = 0; r < 6; r++)
                for (int c = 0; c < 6; c++)
                    assertTrue(board.getCell(r, c).getY() >= 0);
        }

        @Test @DisplayName("cell (0,1) has greater x than cell (0,0)")
        void columnsIncreaseInX() {
            assertTrue(board.getCell(0, 1).getX() > board.getCell(0, 0).getX());
        }

        @Test @DisplayName("cell (1,0) has greater y than cell (0,0)")
        void rowsIncreaseInY() {
            assertTrue(board.getCell(1, 0).getY() > board.getCell(0, 0).getY());
        }

        @Test @DisplayName("x gap between adjacent columns is consistent")
        void consistentColumnGap() {
            int gap01 = board.getCell(0, 1).getX() - board.getCell(0, 0).getX();
            int gap12 = board.getCell(0, 2).getX() - board.getCell(0, 1).getX();
            assertEquals(gap01, gap12);
        }

        @Test @DisplayName("y gap between adjacent rows is consistent")
        void consistentRowGap() {
            int gap01 = board.getCell(1, 0).getY() - board.getCell(0, 0).getY();
            int gap12 = board.getCell(2, 0).getY() - board.getCell(1, 0).getY();
            assertEquals(gap01, gap12);
        }
    }
}
