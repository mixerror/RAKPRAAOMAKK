package entities;

import grid.Board;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Player — every method")
class PlayerTest {

    private Board board;
    private Player player;

    @BeforeEach
    void setUp() {
        board  = new Board(800, 700);
        player = new Player(2, 2, board);
    }

    // ── constructor ──────────────────────────────────────────────────────────
    @Nested @DisplayName("constructor")
    class Constructor {

        @Test @DisplayName("stores the initial row")
        void initialRow() { assertEquals(2, player.getGridRow()); }

        @Test @DisplayName("stores the initial col")
        void initialCol() { assertEquals(2, player.getGridCol()); }

        @Test @DisplayName("starting at (0,0) is accepted")
        void startAtOrigin() {
            Player p = new Player(0, 0, board);
            assertEquals(0, p.getGridRow());
            assertEquals(0, p.getGridCol());
        }

        @Test @DisplayName("starting at (5,5) is accepted")
        void startAtFarCorner() {
            Player p = new Player(5, 5, board);
            assertEquals(5, p.getGridRow());
            assertEquals(5, p.getGridCol());
        }
    }

    // ── getGridRow() / getGridCol() ──────────────────────────────────────────
    @Nested @DisplayName("getGridRow() and getGridCol()")
    class Getters {

        @Test @DisplayName("getGridRow returns current row")
        void getRow() { assertEquals(2, player.getGridRow()); }

        @Test @DisplayName("getGridCol returns current col")
        void getCol() { assertEquals(2, player.getGridCol()); }

        @Test @DisplayName("getGridRow updates after move")
        void getRowAfterMove() { player.move(1, 0); assertEquals(3, player.getGridRow()); }

        @Test @DisplayName("getGridCol updates after move")
        void getColAfterMove() { player.move(0, 1); assertEquals(3, player.getGridCol()); }
    }

    // ── move(int, int) ───────────────────────────────────────────────────────
    @Nested @DisplayName("move(int, int)")
    class Move {

        // valid moves
        @Test @DisplayName("move(-1,0) moves up (row decreases by 1)")
        void moveUp() { player.move(-1, 0); assertEquals(1, player.getGridRow()); assertEquals(2, player.getGridCol()); }

        @Test @DisplayName("move(1,0) moves down (row increases by 1)")
        void moveDown() { player.move(1, 0); assertEquals(3, player.getGridRow()); assertEquals(2, player.getGridCol()); }

        @Test @DisplayName("move(0,-1) moves left (col decreases by 1)")
        void moveLeft() { player.move(0, -1); assertEquals(2, player.getGridRow()); assertEquals(1, player.getGridCol()); }

        @Test @DisplayName("move(0,1) moves right (col increases by 1)")
        void moveRight() { player.move(0, 1); assertEquals(2, player.getGridRow()); assertEquals(3, player.getGridCol()); }

        @Test @DisplayName("move(0,0) is a no-op")
        void moveZero() { player.move(0, 0); assertEquals(2, player.getGridRow()); assertEquals(2, player.getGridCol()); }

        // out-of-bounds guards
        @Test @DisplayName("move up from row=0 is ignored (stays at row 0)")
        void moveUpAtTopEdge() {
            Player p = new Player(0, 3, board);
            p.move(-1, 0);
            assertEquals(0, p.getGridRow());
        }

        @Test @DisplayName("move down from row=5 is ignored (stays at row 5)")
        void moveDownAtBottomEdge() {
            Player p = new Player(5, 3, board);
            p.move(1, 0);
            assertEquals(5, p.getGridRow());
        }

        @Test @DisplayName("move left from col=0 is ignored (stays at col 0)")
        void moveLeftAtLeftEdge() {
            Player p = new Player(3, 0, board);
            p.move(0, -1);
            assertEquals(0, p.getGridCol());
        }

        @Test @DisplayName("move right from col=5 is ignored (stays at col 5)")
        void moveRightAtRightEdge() {
            Player p = new Player(3, 5, board);
            p.move(0, 1);
            assertEquals(5, p.getGridCol());
        }

        @Test @DisplayName("column is unchanged when vertical move is blocked")
        void colUnchangedOnBlockedVertical() {
            Player p = new Player(0, 2, board);
            p.move(-1, 0);
            assertEquals(2, p.getGridCol());
        }

        @Test @DisplayName("row is unchanged when horizontal move is blocked")
        void rowUnchangedOnBlockedHorizontal() {
            Player p = new Player(2, 0, board);
            p.move(0, -1);
            assertEquals(2, p.getGridRow());
        }

        // multi-step
        @Test @DisplayName("multiple moves accumulate correctly")
        void multipleMovesAccumulate() {
            player.move(-1, 0); // row 1
            player.move(-1, 0); // row 0
            player.move(0,  1); // col 3
            assertEquals(0, player.getGridRow());
            assertEquals(3, player.getGridCol());
        }

        @Test @DisplayName("can traverse entire row from left to right")
        void traverseRow() {
            Player p = new Player(0, 0, board);
            for (int i = 0; i < 5; i++) p.move(0, 1);
            assertEquals(5, p.getGridCol());
        }

        @Test @DisplayName("can traverse entire column from top to bottom")
        void traverseCol() {
            Player p = new Player(0, 0, board);
            for (int i = 0; i < 5; i++) p.move(1, 0);
            assertEquals(5, p.getGridRow());
        }

        @Test @DisplayName("move is blocked but does not throw at any edge")
        void noThrowAtAnyEdge() {
            assertDoesNotThrow(() -> {
                Player p = new Player(0, 0, board);
                p.move(-1, 0);  // blocked top
                p.move(0, -1);  // blocked left
                Player p2 = new Player(5, 5, board);
                p2.move(1, 0);  // blocked bottom
                p2.move(0, 1);  // blocked right
            });
        }
    }

    // ── update() ─────────────────────────────────────────────────────────────
    @Nested @DisplayName("update()")
    class Update {

        @Test @DisplayName("does not throw")
        void noThrow() { assertDoesNotThrow(() -> player.update()); }

        @Test @DisplayName("does not change grid position")
        void doesNotChangePosition() {
            player.update();
            assertEquals(2, player.getGridRow());
            assertEquals(2, player.getGridCol());
        }

        @Test @DisplayName("can be called many times without error")
        void calledManyTimes() { assertDoesNotThrow(() -> { for (int i = 0; i < 100; i++) player.update(); }); }
    }
}
