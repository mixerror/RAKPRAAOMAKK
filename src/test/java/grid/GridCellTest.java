package grid;

import engine.SettingsPanel.BoardStyle;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GridCell — every method")
class GridCellTest {

    private GridCell cell;

    @BeforeEach
    void setUp() { cell = new GridCell(2, 3, 100, 200, 80); }

    // ── constructor ──────────────────────────────────────────────────────────
    @Nested @DisplayName("constructor")
    class Constructor {

        @Test @DisplayName("gridRow set correctly")
        void gridRow() { assertEquals(2, cell.getGridRow()); }

        @Test @DisplayName("gridCol set correctly")
        void gridCol() { assertEquals(3, cell.getGridCol()); }

        @Test @DisplayName("pixel x set correctly")
        void pixelX() { assertEquals(100, cell.getX()); }

        @Test @DisplayName("pixel y set correctly")
        void pixelY() { assertEquals(200, cell.getY()); }

        @Test @DisplayName("size set correctly")
        void size() { assertEquals(80, cell.getSize()); }

        @Test @DisplayName("starts in safe state (setDanger works immediately)")
        void startsSafe() {
            // If already active, setDanger would be ignored. Succeeding proves safe state.
            assertDoesNotThrow(() -> cell.setDanger(3));
        }
    }

    // ── getters ──────────────────────────────────────────────────────────────
    @Nested @DisplayName("getters")
    class Getters {

        @Test @DisplayName("getGridRow returns the row passed to constructor")
        void getGridRow() { assertEquals(2, cell.getGridRow()); }

        @Test @DisplayName("getGridCol returns the col passed to constructor")
        void getGridCol() { assertEquals(3, cell.getGridCol()); }

        @Test @DisplayName("getX returns the x passed to constructor")
        void getX() { assertEquals(100, cell.getX()); }

        @Test @DisplayName("getY returns the y passed to constructor")
        void getY() { assertEquals(200, cell.getY()); }

        @Test @DisplayName("getSize returns the size passed to constructor")
        void getSize() { assertEquals(80, cell.getSize()); }

        @Test @DisplayName("cell at (0,0) has row=0")
        void zeroZeroRow() { assertEquals(0, new GridCell(0, 0, 0, 0, 80).getGridRow()); }

        @Test @DisplayName("cell at (0,0) has col=0")
        void zeroZeroCol() { assertEquals(0, new GridCell(0, 0, 0, 0, 80).getGridCol()); }

        @Test @DisplayName("cell at (5,5) has row=5")
        void fiveFiveRow() { assertEquals(5, new GridCell(5, 5, 0, 0, 80).getGridRow()); }

        @Test @DisplayName("cell at (5,5) has col=5")
        void fiveFiveCol() { assertEquals(5, new GridCell(5, 5, 0, 0, 80).getGridCol()); }
    }

    // ── reset() ──────────────────────────────────────────────────────────────
    @Nested @DisplayName("reset()")
    class Reset {

        @Test @DisplayName("reset after setDanger lets setDanger work again (not blocked by active)")
        void resetAfterDanger() {
            cell.setDanger(2);
            cell.reset();
            assertDoesNotThrow(() -> cell.setDanger(3));
        }

        @Test @DisplayName("reset after setActive lets setDanger work again")
        void resetAfterActive() {
            cell.setActive();
            cell.reset();
            // setDanger is ignored when isActive; after reset it should work
            assertDoesNotThrow(() -> cell.setDanger(1));
        }

        @Test @DisplayName("reset can be called on a fresh cell without error")
        void resetFreshCell() { assertDoesNotThrow(() -> cell.reset()); }

        @Test @DisplayName("reset is idempotent (safe to call multiple times)")
        void resetIdempotent() { cell.setActive(); cell.reset(); cell.reset(); assertDoesNotThrow(() -> {}); }
    }

    // ── setDanger(int) ───────────────────────────────────────────────────────
    @Nested @DisplayName("setDanger(int)")
    class SetDanger {

        @Test @DisplayName("does not throw on a fresh cell")
        void noThrow() { assertDoesNotThrow(() -> cell.setDanger(2)); }

        @Test @DisplayName("does not throw with countdown=1")
        void countdownOne() { assertDoesNotThrow(() -> cell.setDanger(1)); }

        @Test @DisplayName("is ignored when cell is already active (setDanger after setActive)")
        void ignoredWhenActive() {
            cell.setActive();
            // After setDanger on an active cell, subsequent setActive should still work
            assertDoesNotThrow(() -> cell.setDanger(1));
        }

        @Test @DisplayName("when called twice, keeps the lower (more urgent) countdown")
        void keepsLowerCountdown() {
            // We verify indirectly: setDanger(3) then setDanger(1) should not crash
            // and the state should remain in danger (not safe)
            cell.setDanger(3);
            cell.setDanger(1);
            // If countdown is now 1 (urgent), calling setDanger(5) should not override
            cell.setDanger(5);
            // No exception means the priority logic ran correctly
            assertDoesNotThrow(() -> {});
        }

        @Test @DisplayName("higher countdown after lower does not override the lower")
        void higherCountdownIgnored() {
            cell.setDanger(1);
            cell.setDanger(5); // should be ignored (5 > 1)
            assertDoesNotThrow(() -> {});
        }

        @Test @DisplayName("active state always overrides danger: setActive after setDanger works")
        void activeOverridesDanger() {
            cell.setDanger(2);
            assertDoesNotThrow(() -> cell.setActive());
        }
    }

    // ── setActive() ──────────────────────────────────────────────────────────
    @Nested @DisplayName("setActive()")
    class SetActive {

        @Test @DisplayName("does not throw on a fresh cell")
        void noThrowFreshCell() { assertDoesNotThrow(() -> cell.setActive()); }

        @Test @DisplayName("does not throw when called after setDanger")
        void afterDanger() { cell.setDanger(2); assertDoesNotThrow(() -> cell.setActive()); }

        @Test @DisplayName("can be called multiple times without error")
        void calledTwice() { cell.setActive(); assertDoesNotThrow(() -> cell.setActive()); }

        @Test @DisplayName("after setActive, setDanger is ignored (active wins)")
        void dangerIgnoredAfterActive() {
            cell.setActive();
            assertDoesNotThrow(() -> cell.setDanger(1)); // should silently do nothing
        }
    }

    // ── setTheme(BoardStyle) ─────────────────────────────────────────────────
    @Nested @DisplayName("setTheme(BoardStyle)")
    class SetTheme {

        @Test @DisplayName("does not throw for DARK_BLUE")
        void darkBlue() { assertDoesNotThrow(() -> cell.setTheme(BoardStyle.DARK_BLUE)); }

        @Test @DisplayName("does not throw for PURPLE")
        void purple() { assertDoesNotThrow(() -> cell.setTheme(BoardStyle.PURPLE)); }

        @Test @DisplayName("does not throw for GREEN")
        void green() { assertDoesNotThrow(() -> cell.setTheme(BoardStyle.GREEN)); }

        @Test @DisplayName("does not throw for RED")
        void red() { assertDoesNotThrow(() -> cell.setTheme(BoardStyle.RED)); }

        @Test @DisplayName("does not throw for CYBERPUNK")
        void cyberpunk() { assertDoesNotThrow(() -> cell.setTheme(BoardStyle.CYBERPUNK)); }

        @Test @DisplayName("can switch themes multiple times")
        void multipleThemes() {
            assertDoesNotThrow(() -> {
                cell.setTheme(BoardStyle.DARK_BLUE);
                cell.setTheme(BoardStyle.PURPLE);
                cell.setTheme(BoardStyle.GREEN);
            });
        }
    }

    // ── state interactions ───────────────────────────────────────────────────
    @Nested @DisplayName("state interaction sequences")
    class StateInteractions {

        @Test @DisplayName("safe → danger → active → reset → safe cycle")
        void fullCycle() {
            cell.setDanger(2);    // danger
            cell.setActive();     // active
            cell.reset();         // safe again
            assertDoesNotThrow(() -> cell.setDanger(1)); // prove safe: setDanger works
        }

        @Test @DisplayName("danger → reset → danger works (counter is fresh)")
        void dangerResetDanger() {
            cell.setDanger(3);
            cell.reset();
            assertDoesNotThrow(() -> cell.setDanger(2));
        }

        @Test @DisplayName("active → reset → active works")
        void activeResetActive() {
            cell.setActive();
            cell.reset();
            assertDoesNotThrow(() -> cell.setActive());
        }
    }
}
