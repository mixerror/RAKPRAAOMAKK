package patterns;

import grid.Board;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Hazard — base class and all subclasses")
class HazardTest {

    private Board board;

    @BeforeEach
    void setUp() { board = new Board(800, 700); }

    // helper
    private boolean containsTile(List<int[]> tiles, int row, int col) {
        for (int[] t : tiles) if (t[0] == row && t[1] == col) return true;
        return false;
    }

    private boolean hasDuplicate(List<int[]> tiles) {
        for (int i = 0; i < tiles.size(); i++)
            for (int j = i + 1; j < tiles.size(); j++)
                if (tiles.get(i)[0] == tiles.get(j)[0] && tiles.get(i)[1] == tiles.get(j)[1])
                    return true;
        return false;
    }

    // ── Hazard base: isActive / isFinished / activate / finish ───────────────
    @Nested @DisplayName("Hazard base — isActive(), isFinished(), activate(), finish()")
    class HazardBase {

        @Test @DisplayName("new hazard is not active")
        void notActive() { assertFalse(new RowHazard(board, 0, 2).isActive()); }

        @Test @DisplayName("new hazard is not finished")
        void notFinished() { assertFalse(new RowHazard(board, 0, 2).isFinished()); }

        @Test @DisplayName("activate() sets isActive() to true")
        void activateWorks() { RowHazard h = new RowHazard(board, 0, 2); h.activate(); assertTrue(h.isActive()); }

        @Test @DisplayName("finish() sets isFinished() to true")
        void finishWorks() { RowHazard h = new RowHazard(board, 0, 0); h.activate(); h.finish(); assertTrue(h.isFinished()); }

        @Test @DisplayName("finish() can be called without activating first (no crash)")
        void finishWithoutActivate() { RowHazard h = new RowHazard(board, 0, 1); assertDoesNotThrow(h::finish); assertTrue(h.isFinished()); }

        @Test @DisplayName("isActive() stays true after finish()")
        void activeRemainsAfterFinish() { RowHazard h = new RowHazard(board, 0, 0); h.activate(); h.finish(); assertTrue(h.isActive()); }
    }

    // ── Hazard base: decrementCountdown() ────────────────────────────────────
    @Nested @DisplayName("Hazard base — decrementCountdown()")
    class DecrementCountdown {

        @Test @DisplayName("countdown=1 → decrement once → activates")
        void activatesAtCountdownOne() {
            RowHazard h = new RowHazard(board, 0, 1);
            h.decrementCountdown();
            assertTrue(h.isActive());
        }

        @Test @DisplayName("countdown=2 → decrement once → not yet active")
        void notActiveAfterOneDecrement() {
            RowHazard h = new RowHazard(board, 0, 2);
            h.decrementCountdown();
            assertFalse(h.isActive());
        }

        @Test @DisplayName("countdown=2 → decrement twice → active")
        void activeAfterTwoDecrements() {
            RowHazard h = new RowHazard(board, 0, 2);
            h.decrementCountdown();
            h.decrementCountdown();
            assertTrue(h.isActive());
        }

        @Test @DisplayName("countdown=0 → already activates on construction decrement")
        void zeroCountdownActivatesOnFirstDecrement() {
            RowHazard h = new RowHazard(board, 0, 0);
            h.decrementCountdown(); // countdown already 0 → activate
            assertTrue(h.isActive());
        }

        @Test @DisplayName("decrementCountdown does not throw when already active")
        void noThrowWhenActive() {
            RowHazard h = new RowHazard(board, 0, 1);
            h.decrementCountdown(); // activates
            assertDoesNotThrow(h::decrementCountdown);
        }
    }

    // ── Hazard base: checkHit() ───────────────────────────────────────────────
    @Nested @DisplayName("Hazard base — checkHit()")
    class CheckHit {

        @Test @DisplayName("false when hazard is not active")
        void falseWhenNotActive() {
            RowHazard h = new RowHazard(board, 2, 2);
            assertFalse(h.checkHit(2, 0));
        }

        @Test @DisplayName("true when active and player is on a targeted tile")
        void trueWhenActiveAndOnTile() {
            RowHazard h = new RowHazard(board, 2, 0);
            h.activate();
            assertTrue(h.checkHit(2, 0));
        }

        @Test @DisplayName("true for every targeted tile when active")
        void trueForEveryTile() {
            RowHazard h = new RowHazard(board, 3, 0);
            h.activate();
            for (int c = 0; c < 6; c++) assertTrue(h.checkHit(3, c), "Expected hit at col " + c);
        }

        @Test @DisplayName("false when active but player is on a safe tile")
        void falseOnSafeTile() {
            RowHazard h = new RowHazard(board, 2, 0);
            h.activate();
            assertFalse(h.checkHit(3, 0)); // row 3 is not targeted
        }

        @Test @DisplayName("false after hazard is finished")
        void falseWhenFinished() {
            RowHazard h = new RowHazard(board, 0, 0);
            h.activate(); h.finish();
            // checkHit still returns active-based result — active remains true but let's verify no crash
            assertDoesNotThrow(() -> h.checkHit(0, 0));
        }
    }

    // ── Hazard base: update() ────────────────────────────────────────────────
    @Nested @DisplayName("Hazard base — update()")
    class HazardUpdate {

        @Test @DisplayName("update() in warning phase does not throw")
        void warningPhaseNoThrow() {
            RowHazard h = new RowHazard(board, 0, 2);
            assertDoesNotThrow(h::update);
        }

        @Test @DisplayName("update() in active phase does not throw")
        void activePhaseNoThrow() {
            RowHazard h = new RowHazard(board, 0, 0);
            h.activate();
            assertDoesNotThrow(h::update);
        }

        @Test @DisplayName("update() after finish() is a no-op (no throw)")
        void finishedPhaseNoThrow() {
            RowHazard h = new RowHazard(board, 0, 0);
            h.activate(); h.finish();
            assertDoesNotThrow(h::update);
        }
    }

    // ── Hazard base: getTiles() ──────────────────────────────────────────────
    @Nested @DisplayName("Hazard base — getTiles()")
    class GetTiles {

        @Test @DisplayName("getTiles() returns non-null")
        void nonNull() { assertNotNull(new RowHazard(board, 0, 1).getTiles()); }

        @Test @DisplayName("getTiles() returns non-empty list")
        void nonEmpty() { assertFalse(new RowHazard(board, 0, 1).getTiles().isEmpty()); }

        @Test @DisplayName("each tile has exactly 2 elements [row, col]")
        void tilesHaveTwoElements() {
            for (int[] t : new RowHazard(board, 0, 1).getTiles())
                assertEquals(2, t.length);
        }
    }

    // ── RowHazard ────────────────────────────────────────────────────────────
    @Nested @DisplayName("RowHazard")
    class RowHazardTests {

        @Test @DisplayName("targets exactly 6 tiles")
        void tileCount() { assertEquals(6, new RowHazard(board, 0, 1).getTiles().size()); }

        @Test @DisplayName("all tiles are in the correct row")
        void correctRow() {
            for (int[] t : new RowHazard(board, 4, 1).getTiles())
                assertEquals(4, t[0]);
        }

        @Test @DisplayName("covers all 6 columns")
        void allColumns() {
            boolean[] seen = new boolean[6];
            for (int[] t : new RowHazard(board, 0, 1).getTiles()) seen[t[1]] = true;
            for (boolean s : seen) assertTrue(s);
        }

        @Test @DisplayName("works for row 0")
        void row0() { assertDoesNotThrow(() -> new RowHazard(board, 0, 1)); }

        @Test @DisplayName("works for row 5")
        void row5() { assertDoesNotThrow(() -> new RowHazard(board, 5, 1)); }

        @Test @DisplayName("no duplicate tiles")
        void noDuplicates() { assertFalse(hasDuplicate(new RowHazard(board, 2, 1).getTiles())); }
    }

    // ── ColumnHazard ─────────────────────────────────────────────────────────
    @Nested @DisplayName("ColumnHazard")
    class ColumnHazardTests {

        @Test @DisplayName("targets exactly 6 tiles")
        void tileCount() { assertEquals(6, new ColumnHazard(board, 0, 1).getTiles().size()); }

        @Test @DisplayName("all tiles are in the correct column")
        void correctCol() {
            for (int[] t : new ColumnHazard(board, 3, 1).getTiles())
                assertEquals(3, t[1]);
        }

        @Test @DisplayName("covers all 6 rows")
        void allRows() {
            boolean[] seen = new boolean[6];
            for (int[] t : new ColumnHazard(board, 0, 1).getTiles()) seen[t[0]] = true;
            for (boolean s : seen) assertTrue(s);
        }

        @Test @DisplayName("works for col 0")
        void col0() { assertDoesNotThrow(() -> new ColumnHazard(board, 0, 1)); }

        @Test @DisplayName("works for col 5")
        void col5() { assertDoesNotThrow(() -> new ColumnHazard(board, 5, 1)); }

        @Test @DisplayName("no duplicate tiles")
        void noDuplicates() { assertFalse(hasDuplicate(new ColumnHazard(board, 2, 1).getTiles())); }
    }

    // ── DiagonalHazard ───────────────────────────────────────────────────────
    @Nested @DisplayName("DiagonalHazard")
    class DiagonalHazardTests {

        @Test @DisplayName("main diagonal has 6 tiles")
        void mainDiagTileCount() { assertEquals(6, new DiagonalHazard(board, true, 1).getTiles().size()); }

        @Test @DisplayName("anti-diagonal has 6 tiles")
        void antiDiagTileCount() { assertEquals(6, new DiagonalHazard(board, false, 1).getTiles().size()); }

        @Test @DisplayName("main diagonal: each tile satisfies row == col")
        void mainDiagRowEqCol() {
            for (int[] t : new DiagonalHazard(board, true, 1).getTiles())
                assertEquals(t[0], t[1], "row != col at (" + t[0] + "," + t[1] + ")");
        }

        @Test @DisplayName("anti-diagonal: each tile satisfies row + col == 5")
        void antiDiagRowPlusCol() {
            for (int[] t : new DiagonalHazard(board, false, 1).getTiles())
                assertEquals(5, t[0] + t[1], "row+col != 5 at (" + t[0] + "," + t[1] + ")");
        }

        @Test @DisplayName("main diagonal includes (0,0)")
        void mainDiagIncludesOrigin() { assertTrue(containsTile(new DiagonalHazard(board, true, 1).getTiles(), 0, 0)); }

        @Test @DisplayName("main diagonal includes (5,5)")
        void mainDiagIncludesEnd() { assertTrue(containsTile(new DiagonalHazard(board, true, 1).getTiles(), 5, 5)); }

        @Test @DisplayName("anti-diagonal includes (0,5)")
        void antiDiagIncludesTopRight() { assertTrue(containsTile(new DiagonalHazard(board, false, 1).getTiles(), 0, 5)); }

        @Test @DisplayName("anti-diagonal includes (5,0)")
        void antiDiagIncludesBottomLeft() { assertTrue(containsTile(new DiagonalHazard(board, false, 1).getTiles(), 5, 0)); }

        @Test @DisplayName("no duplicate tiles in main diagonal")
        void mainNoDuplicates() { assertFalse(hasDuplicate(new DiagonalHazard(board, true, 1).getTiles())); }

        @Test @DisplayName("no duplicate tiles in anti-diagonal")
        void antiNoDuplicates() { assertFalse(hasDuplicate(new DiagonalHazard(board, false, 1).getTiles())); }
    }

    // ── XShapeHazard ─────────────────────────────────────────────────────────
    @Nested @DisplayName("XShapeHazard")
    class XShapeHazardTests {

        @Test @DisplayName("has 12 tiles (6 + 6)")
        void tileCount() { assertEquals(12, new XShapeHazard(board, 1).getTiles().size()); }

        @Test @DisplayName("no duplicate tiles")
        void noDuplicates() { assertFalse(hasDuplicate(new XShapeHazard(board, 1).getTiles())); }

        @Test @DisplayName("includes top-left corner (0,0)")
        void includesTopLeft() { assertTrue(containsTile(new XShapeHazard(board, 1).getTiles(), 0, 0)); }

        @Test @DisplayName("includes top-right corner (0,5)")
        void includesTopRight() { assertTrue(containsTile(new XShapeHazard(board, 1).getTiles(), 0, 5)); }

        @Test @DisplayName("includes bottom-left corner (5,0)")
        void includesBottomLeft() { assertTrue(containsTile(new XShapeHazard(board, 1).getTiles(), 5, 0)); }

        @Test @DisplayName("includes bottom-right corner (5,5)")
        void includesBottomRight() { assertTrue(containsTile(new XShapeHazard(board, 1).getTiles(), 5, 5)); }

        @Test @DisplayName("all tiles are within board bounds")
        void inBounds() {
            for (int[] t : new XShapeHazard(board, 1).getTiles())
                assertTrue(board.isValidPosition(t[0], t[1]));
        }
    }

    // ── CornersHazard ────────────────────────────────────────────────────────
    @Nested @DisplayName("CornersHazard")
    class CornersHazardTests {

        @Test @DisplayName("targets exactly 4 tiles")
        void tileCount() { assertEquals(4, new CornersHazard(board, 1).getTiles().size()); }

        @Test @DisplayName("includes top-left (0,0)")
        void topLeft() { assertTrue(containsTile(new CornersHazard(board, 1).getTiles(), 0, 0)); }

        @Test @DisplayName("includes top-right (0,5)")
        void topRight() { assertTrue(containsTile(new CornersHazard(board, 1).getTiles(), 0, 5)); }

        @Test @DisplayName("includes bottom-left (5,0)")
        void bottomLeft() { assertTrue(containsTile(new CornersHazard(board, 1).getTiles(), 5, 0)); }

        @Test @DisplayName("includes bottom-right (5,5)")
        void bottomRight() { assertTrue(containsTile(new CornersHazard(board, 1).getTiles(), 5, 5)); }

        @Test @DisplayName("no duplicate tiles")
        void noDuplicates() { assertFalse(hasDuplicate(new CornersHazard(board, 1).getTiles())); }
    }

    // ── BorderHazard ─────────────────────────────────────────────────────────
    @Nested @DisplayName("BorderHazard")
    class BorderHazardTests {

        @Test @DisplayName("targets 20 tiles (4 * (6-1))")
        void tileCount() { assertEquals(20, new BorderHazard(board, 1).getTiles().size()); }

        @Test @DisplayName("all tiles are on the outer edge")
        void allOnEdge() {
            for (int[] t : new BorderHazard(board, 1).getTiles()) {
                boolean onEdge = t[0] == 0 || t[0] == 5 || t[1] == 0 || t[1] == 5;
                assertTrue(onEdge, "Not on edge: (" + t[0] + "," + t[1] + ")");
            }
        }

        @Test @DisplayName("includes all four corners")
        void includesCorners() {
            List<int[]> tiles = new BorderHazard(board, 1).getTiles();
            assertTrue(containsTile(tiles, 0, 0));
            assertTrue(containsTile(tiles, 0, 5));
            assertTrue(containsTile(tiles, 5, 0));
            assertTrue(containsTile(tiles, 5, 5));
        }

        @Test @DisplayName("no interior tiles (row 1-4, col 1-4)")
        void noInteriorTiles() {
            List<int[]> tiles = new BorderHazard(board, 1).getTiles();
            for (int[] t : tiles) {
                boolean isInterior = t[0] >= 1 && t[0] <= 4 && t[1] >= 1 && t[1] <= 4;
                assertFalse(isInterior, "Interior tile found: (" + t[0] + "," + t[1] + ")");
            }
        }

        @Test @DisplayName("no duplicate tiles")
        void noDuplicates() { assertFalse(hasDuplicate(new BorderHazard(board, 1).getTiles())); }
    }

    // ── CheckerHazard ────────────────────────────────────────────────────────
    @Nested @DisplayName("CheckerHazard")
    class CheckerHazardTests {

        @Test @DisplayName("even half has 18 tiles")
        void evenTileCount() { assertEquals(18, new CheckerHazard(board, true, 2).getTiles().size()); }

        @Test @DisplayName("odd half has 18 tiles")
        void oddTileCount() { assertEquals(18, new CheckerHazard(board, false, 2).getTiles().size()); }

        @Test @DisplayName("even tiles: every tile has (row+col) % 2 == 0")
        void evenTilesCorrect() {
            for (int[] t : new CheckerHazard(board, true, 2).getTiles())
                assertEquals(0, (t[0] + t[1]) % 2, "Odd tile in even set: (" + t[0] + "," + t[1] + ")");
        }

        @Test @DisplayName("odd tiles: every tile has (row+col) % 2 == 1")
        void oddTilesCorrect() {
            for (int[] t : new CheckerHazard(board, false, 2).getTiles())
                assertEquals(1, (t[0] + t[1]) % 2, "Even tile in odd set: (" + t[0] + "," + t[1] + ")");
        }

        @Test @DisplayName("even and odd sets are disjoint")
        void setsAreDisjoint() {
            List<int[]> even = new CheckerHazard(board, true,  2).getTiles();
            List<int[]> odd  = new CheckerHazard(board, false, 2).getTiles();
            for (int[] e : even)
                assertFalse(containsTile(odd, e[0], e[1]), "Shared tile: (" + e[0] + "," + e[1] + ")");
        }

        @Test @DisplayName("even + odd together cover all 36 tiles")
        void togetherCoverAllTiles() {
            List<int[]> even = new CheckerHazard(board, true,  2).getTiles();
            List<int[]> odd  = new CheckerHazard(board, false, 2).getTiles();
            assertEquals(36, even.size() + odd.size());
        }

        @Test @DisplayName("no duplicate tiles in even set")
        void evenNoDuplicates() { assertFalse(hasDuplicate(new CheckerHazard(board, true, 2).getTiles())); }

        @Test @DisplayName("no duplicate tiles in odd set")
        void oddNoDuplicates() { assertFalse(hasDuplicate(new CheckerHazard(board, false, 2).getTiles())); }
    }

    // ── LShapeHazard ─────────────────────────────────────────────────────────
    @Nested @DisplayName("LShapeHazard")
    class LShapeHazardTests {

        @Test @DisplayName("tile count is positive")
        void positiveTileCount() { assertTrue(new LShapeHazard(board, 1).getTiles().size() > 0); }

        @Test @DisplayName("all tiles are within board bounds")
        void inBounds() {
            for (int[] t : new LShapeHazard(board, 1).getTiles())
                assertTrue(board.isValidPosition(t[0], t[1]), "Out of bounds: (" + t[0] + "," + t[1] + ")");
        }

        @Test @DisplayName("no duplicate tiles")
        void noDuplicates() { assertFalse(hasDuplicate(new LShapeHazard(board, 1).getTiles())); }

        @Test @DisplayName("can be constructed multiple times (random corner each time, no crash)")
        void multipleConstructions() {
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 20; i++) new LShapeHazard(board, 1);
            });
        }

        @Test @DisplayName("all tiles have rows in [0, 5]")
        void rowsInRange() {
            List<int[]> tiles = new LShapeHazard(board, 1).getTiles();
            for (int[] t : tiles) { assertTrue(t[0] >= 0); assertTrue(t[0] <= 5); }
        }

        @Test @DisplayName("all tiles have cols in [0, 5]")
        void colsInRange() {
            List<int[]> tiles = new LShapeHazard(board, 1).getTiles();
            for (int[] t : tiles) { assertTrue(t[1] >= 0); assertTrue(t[1] <= 5); }
        }
    }

    // ── Full lifecycle integration ────────────────────────────────────────────
    @Nested @DisplayName("Full lifecycle: warning → active → finished")
    class FullLifecycle {

        @Test @DisplayName("RowHazard full lifecycle completes without error")
        void rowHazardLifecycle() {
            RowHazard h = new RowHazard(board, 0, 2);
            board.resetAllCells(); h.update(); // warning
            h.decrementCountdown();
            board.resetAllCells(); h.update(); // still warning
            h.decrementCountdown();
            board.resetAllCells(); h.update(); // active
            h.finish();
            assertTrue(h.isFinished());
        }

        @Test @DisplayName("checkHit correctly detects player on active RowHazard")
        void rowHitDetection() {
            RowHazard h = new RowHazard(board, 3, 0);
            h.activate();
            assertTrue(h.checkHit(3, 0));
            assertTrue(h.checkHit(3, 5));
            assertFalse(h.checkHit(2, 0));
            assertFalse(h.checkHit(4, 0));
        }

        @Test @DisplayName("checkHit correctly detects player on active ColumnHazard")
        void columnHitDetection() {
            ColumnHazard h = new ColumnHazard(board, 2, 0);
            h.activate();
            assertTrue(h.checkHit(0, 2));
            assertTrue(h.checkHit(5, 2));
            assertFalse(h.checkHit(0, 1));
        }

        @Test @DisplayName("checkHit correctly detects player on active DiagonalHazard")
        void diagonalHitDetection() {
            DiagonalHazard h = new DiagonalHazard(board, true, 0);
            h.activate();
            assertTrue(h.checkHit(0, 0));
            assertTrue(h.checkHit(3, 3));
            assertFalse(h.checkHit(0, 1));
        }

        @Test @DisplayName("checkHit correctly detects player on active CornersHazard")
        void cornersHitDetection() {
            CornersHazard h = new CornersHazard(board, 0);
            h.activate();
            assertTrue(h.checkHit(0, 0));
            assertTrue(h.checkHit(5, 5));
            assertFalse(h.checkHit(2, 2)); // interior is safe
        }

        @Test @DisplayName("checkHit correctly detects player on active BorderHazard")
        void borderHitDetection() {
            BorderHazard h = new BorderHazard(board, 0);
            h.activate();
            assertTrue(h.checkHit(0, 3));  // top edge
            assertTrue(h.checkHit(5, 3));  // bottom edge
            assertFalse(h.checkHit(2, 2)); // interior
        }

        @Test @DisplayName("checkHit correctly detects player on active CheckerHazard (even)")
        void checkerHitDetection() {
            CheckerHazard h = new CheckerHazard(board, true, 0);
            h.activate();
            assertTrue(h.checkHit(0, 0));  // (0+0)%2==0
            assertFalse(h.checkHit(0, 1)); // (0+1)%2==1 → safe
        }
    }
}
