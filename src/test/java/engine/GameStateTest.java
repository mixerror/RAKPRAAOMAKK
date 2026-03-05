package engine;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameState — every method")
class GameStateTest {

    private GameState state;

    @BeforeEach
    void setUp() { state = new GameState(); }

    // ── constructor / reset() ────────────────────────────────────────────────
    @Nested @DisplayName("constructor and reset()")
    class ConstructorAndReset {

        @Test @DisplayName("score starts at 0")
        void initialScore() { assertEquals(0, state.getScore()); }

        @Test @DisplayName("combo starts at 1")
        void initialCombo() { assertEquals(1, state.getCombo()); }

        @Test @DisplayName("lives start at 3")
        void initialLives() { assertEquals(3, state.getLives()); }

        @Test @DisplayName("level starts at 1")
        void initialLevel() { assertEquals(1, state.getLevel()); }

        @Test @DisplayName("isGameOver() is false initially")
        void notGameOverInitially() { assertFalse(state.isGameOver()); }

        @Test @DisplayName("reset() zeroes score after adding points")
        void resetClearsScore() { state.addScore(999); state.reset(); assertEquals(0, state.getScore()); }

        @Test @DisplayName("reset() restores combo to 1")
        void resetComboField() { state.incrementCombo(); state.reset(); assertEquals(1, state.getCombo()); }

        @Test @DisplayName("reset() restores lives to 3")
        void resetLives() { state.loseLife(); state.reset(); assertEquals(3, state.getLives()); }

        @Test @DisplayName("reset() restores level to 1")
        void resetLevel() { for (int i = 0; i < 20; i++) state.surviveBeat(); state.reset(); assertEquals(1, state.getLevel()); }

        @Test @DisplayName("reset() restores BPM to 120")
        void resetBpm() { for (int i = 0; i < 20; i++) state.surviveBeat(); state.reset(); assertEquals(120, state.getBPM()); }

        @Test @DisplayName("reset() clears game-over state")
        void resetGameOver() { state.loseLife(); state.loseLife(); state.loseLife(); state.reset(); assertFalse(state.isGameOver()); }

        @Test @DisplayName("reset() can be called multiple times safely")
        void resetIdempotent() { state.addScore(100); state.reset(); state.reset(); assertEquals(0, state.getScore()); }
    }

    // ── addScore(int) ────────────────────────────────────────────────────────
    @Nested @DisplayName("addScore(int)")
    class AddScore {

        @Test @DisplayName("combo=1 awards exact points")
        void comboOne() { state.addScore(100); assertEquals(100, state.getScore()); }

        @Test @DisplayName("combo=2 doubles the award")
        void comboTwo() { state.incrementCombo(); state.addScore(100); assertEquals(200, state.getScore()); }

        @Test @DisplayName("combo=8 (max) multiplies by 8")
        void comboMax() { for (int i = 0; i < 7; i++) state.incrementCombo(); state.addScore(100); assertEquals(800, state.getScore()); }

        @Test @DisplayName("addScore(0) leaves score unchanged")
        void addZero() { state.addScore(100); state.addScore(0); assertEquals(100, state.getScore()); }

        @Test @DisplayName("accumulates across multiple calls")
        void accumulates() { state.addScore(50); state.addScore(50); assertEquals(100, state.getScore()); }

        @Test @DisplayName("uses combo value at the moment of the call")
        void usesCurrentCombo() {
            state.addScore(100);        // combo=1 → +100
            state.incrementCombo();     // combo=2
            state.addScore(100);        // combo=2 → +200
            assertEquals(300, state.getScore());
        }
    }

    // ── incrementCombo() ─────────────────────────────────────────────────────
    @Nested @DisplayName("incrementCombo()")
    class IncrementCombo {

        @Test @DisplayName("increments from 1 to 2")
        void oneToTwo() { state.incrementCombo(); assertEquals(2, state.getCombo()); }

        @Test @DisplayName("each call adds exactly 1 up to cap")
        void incrementsSequentially() {
            for (int expected = 2; expected <= 8; expected++) {
                state.incrementCombo();
                assertEquals(expected, state.getCombo());
            }
        }

        @Test @DisplayName("capped at 8 — does not go to 9")
        void cappedAtEight() { for (int i = 0; i < 20; i++) state.incrementCombo(); assertEquals(8, state.getCombo()); }

        @Test @DisplayName("calling at cap does not throw")
        void atCapNoCrash() { for (int i = 0; i < 8; i++) state.incrementCombo(); assertDoesNotThrow(() -> state.incrementCombo()); }
    }

    // ── resetCombo() ─────────────────────────────────────────────────────────
    @Nested @DisplayName("resetCombo()")
    class ResetCombo {

        @Test @DisplayName("resets elevated combo back to 1")
        void resetsToOne() { state.incrementCombo(); state.incrementCombo(); state.resetCombo(); assertEquals(1, state.getCombo()); }

        @Test @DisplayName("no-op when combo is already 1")
        void noOpAtOne() { state.resetCombo(); assertEquals(1, state.getCombo()); }

        @Test @DisplayName("resets from max combo (8) to 1")
        void resetFromMax() { for (int i = 0; i < 7; i++) state.incrementCombo(); state.resetCombo(); assertEquals(1, state.getCombo()); }
    }

    // ── loseLife() ───────────────────────────────────────────────────────────
    @Nested @DisplayName("loseLife()")
    class LoseLife {

        @Test @DisplayName("decrements lives by exactly 1")
        void decrementsLives() { state.loseLife(); assertEquals(2, state.getLives()); }

        @Test @DisplayName("three calls reduce lives to 0")
        void threeCallsZeroLives() { state.loseLife(); state.loseLife(); state.loseLife(); assertEquals(0, state.getLives()); }

        @Test @DisplayName("resets combo to 1")
        void resetsCombo() { state.incrementCombo(); state.incrementCombo(); state.loseLife(); assertEquals(1, state.getCombo()); }

        @Test @DisplayName("resets combo even from max (8)")
        void resetsComboFromMax() { for (int i = 0; i < 7; i++) state.incrementCombo(); state.loseLife(); assertEquals(1, state.getCombo()); }

        @Test @DisplayName("does not affect score")
        void doesNotAffectScore() { state.addScore(500); state.loseLife(); assertEquals(500, state.getScore()); }

        @Test @DisplayName("does not affect level")
        void doesNotAffectLevel() { for (int i = 0; i < 20; i++) state.surviveBeat(); int lv = state.getLevel(); state.loseLife(); assertEquals(lv, state.getLevel()); }
    }

    // ── isGameOver() ─────────────────────────────────────────────────────────
    @Nested @DisplayName("isGameOver()")
    class IsGameOver {

        @Test @DisplayName("false with 3 lives")
        void falseThreeLives() { assertFalse(state.isGameOver()); }

        @Test @DisplayName("false with 2 lives")
        void falseTwoLives() { state.loseLife(); assertFalse(state.isGameOver()); }

        @Test @DisplayName("false with 1 life")
        void falseOneLife() { state.loseLife(); state.loseLife(); assertFalse(state.isGameOver()); }

        @Test @DisplayName("true at exactly 0 lives")
        void trueZeroLives() { state.loseLife(); state.loseLife(); state.loseLife(); assertTrue(state.isGameOver()); }
    }

    // ── surviveBeat() / levelUp() ────────────────────────────────────────────
    @Nested @DisplayName("surviveBeat() and level progression")
    class SurviveBeat {

        @Test @DisplayName("level stays 1 after 19 beats")
        void staysAtLevelOneAfter19() { for (int i = 0; i < 19; i++) state.surviveBeat(); assertEquals(1, state.getLevel()); }

        @Test @DisplayName("level becomes 2 after exactly 20 beats")
        void levelTwoAt20() { for (int i = 0; i < 20; i++) state.surviveBeat(); assertEquals(2, state.getLevel()); }

        @Test @DisplayName("subsequent level-ups need only 15 beats")
        void subsequentLevelsFaster() {
            for (int i = 0; i < 20; i++) state.surviveBeat(); // → level 2
            for (int i = 0; i < 15; i++) state.surviveBeat(); // → level 3
            assertEquals(3, state.getLevel());
        }

        @Test @DisplayName("level never exceeds 10")
        void levelCappedAtTen() { for (int i = 0; i < 500; i++) state.surviveBeat(); assertEquals(10, state.getLevel()); }

        @Test @DisplayName("beat counter resets to 0 after level-up")
        void beatCounterResets() {
            for (int i = 0; i < 20; i++) state.surviveBeat(); // → level 2, counter reset
            for (int i = 0; i < 14; i++) state.surviveBeat(); // one short of level 3
            assertEquals(2, state.getLevel());
        }
    }

    // ── getBPM() ─────────────────────────────────────────────────────────────
    @Nested @DisplayName("getBPM()")
    class GetBpm {

        @Test @DisplayName("120 at level 1")
        void bpmLevel1() { assertEquals(120, state.getBPM()); }

        @Test @DisplayName("135 at level 2  (120 + 1*15)")
        void bpmLevel2() { for (int i = 0; i < 20; i++) state.surviveBeat(); assertEquals(135, state.getBPM()); }

        @Test @DisplayName("150 at level 3  (120 + 2*15)")
        void bpmLevel3() {
            for (int i = 0; i < 20; i++) state.surviveBeat();
            for (int i = 0; i < 15; i++) state.surviveBeat();
            assertEquals(150, state.getBPM());
        }

        @Test @DisplayName("255 at level 10  (120 + 9*15)")
        void bpmLevel10() { for (int i = 0; i < 500; i++) state.surviveBeat(); assertEquals(255, state.getBPM()); }

        @Test @DisplayName("BPM does not grow beyond level-10 cap")
        void bpmDoesNotExceedCap() {
            for (int i = 0; i < 500; i++) state.surviveBeat();
            int cap = state.getBPM();
            for (int i = 0; i < 100; i++) state.surviveBeat();
            assertEquals(cap, state.getBPM());
        }
    }
}
