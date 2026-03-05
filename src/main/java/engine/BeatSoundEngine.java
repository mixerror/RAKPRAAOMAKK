package engine;

import javax.sound.sampled.*;

/**
 * CLASS: BeatSoundEngine  (Synth-Pop Edition)
 *
 * Continuous streaming soundtrack inspired by bright, euphoric synth-pop.
 * 110 BPM, key of A major. Four layered voices:
 *
 *   KICK      — 4-on-the-floor punch (every beat)
 *   BASS      — warm sub-bass pulse, follows chord root
 *   ARP       — bright plucked synth arpeggio (16th notes)
 *   PAD       — lush, slow-attack chord pad (A major / F#m / D / E)
 *
 * All synthesized — no audio files required.
 * Press M to mute / unmute.
 *
 * <p>All audio processing runs on a dedicated background daemon thread so the
 * game loop is never blocked. One-shot sound effects ({@link #playHurtSound()},
 * {@link #playGameOverSound()}, {@link #playClickSound()}) each spawn their own
 * short-lived daemon thread.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   BeatSoundEngine engine = new BeatSoundEngine();
 *   SoundManager.register(engine);
 *   engine.start(1);
 *   engine.setMuted(true);   // silence without stopping the thread
 *   engine.shutdown();       // release resources on exit
 * </pre>
 *
 * @author Project Team
 * @version 1.0
 */
public class BeatSoundEngine {

    // ── Config ────────────────────────────────────────────────────────────────

    /** PCM sample rate in Hz used for all synthesis and playback. */
    private static final int    SAMPLE_RATE  = 44100;

    /** Number of stereo frames rendered and written to the line per iteration. */
    private static final int    CHUNK        = 512;   // samples per render chunk

    /** Fixed tempo for the background music track, in beats per minute. */
    private static final double BPM          = 110.0;

    /** Duration of one beat in seconds, derived from {@link #BPM}. */
    private static final double BEAT_SEC     = 60.0 / BPM;          // ~0.545s

    /** Duration of one 16th-note subdivision in seconds. */
    private static final double SIXTEENTH    = BEAT_SEC / 4.0;      // 16th note

    /** Peak amplitude of the kick drum voice in the final mix (0.0–1.0). */
    private static final float  VOL_KICK     = 0.55f;

    /** Peak amplitude of the bass voice in the final mix (0.0–1.0). */
    private static final float  VOL_BASS     = 0.35f;

    /** Peak amplitude of the arpeggio voice in the final mix (0.0–1.0). */
    private static final float  VOL_ARP      = 0.22f;

    /** Peak amplitude of the pad voice in the final mix (0.0–1.0). */
    private static final float  VOL_PAD      = 0.18f;

    // ── Chord progression: A  F#m  D  E  (each chord = 1 bar = 4 beats) ─────
    // Root frequencies (Hz) for bass + pad
    /**
     * Root note frequencies (Hz) for the four-chord progression A – F♯m – D – E.
     * Index 0 = A3 (220 Hz), 1 = F♯3, 2 = D3, 3 = E3.
     */
    private static final double[] CHORD_ROOT = {
            220.00,  // A3
            185.00,  // F#3  (≈184.997)
            146.83,  // D3
            164.81,  // E3
    };

    // Major / minor chord intervals (ratio multipliers for pad voices)
    // A major:  1, 5/4, 3/2   |  F#m: 1, 6/5, 3/2   |  D maj: 1, 5/4, 3/2  |  E maj
    /**
     * Frequency-ratio multipliers for the three pad voices of each chord.
     * Rows correspond to {@link #CHORD_ROOT} indices; columns are root, third, fifth.
     */
    private static final double[][] CHORD_RATIOS = {
            { 1.0, 1.2599, 1.4983 },   // A  major
            { 1.0, 1.1892, 1.4983 },   // F# minor
            { 1.0, 1.2599, 1.4983 },   // D  major
            { 1.0, 1.2599, 1.4983 },   // E  major
    };

    // Arpeggio pattern (semitone offsets from chord root, 16 steps per bar)
    // Bright ascending + descending flourish
    /**
     * Semitone offsets from the current chord root for each of the 16 arpeggio
     * steps per bar. The pattern produces a bright ascending then descending flourish.
     */
    private static final int[] ARP_SEMITONES = {
            0,  4,  7, 12,   // up
            16, 12,  7,  4,   // down
            0,  4,  9, 12,   // up with 9th colour
            14, 12,  7,  0    // down resolve
    };

    // ── State ─────────────────────────────────────────────────────────────────

    /** Background thread running the {@link #stream()} render loop. */
    private Thread          playbackThread;

    /** Set to {@code false} to request graceful shutdown of the stream loop. */
    private volatile boolean running = false;

    /** When {@code true} all PCM samples are zeroed before writing to the line. */
    private volatile boolean muted   = false;

    /** Master volume level, range 0.0 (silent) to 1.0 (full). */
    private volatile float   volume  = 0.8f;

    // no-ops: music is fixed tempo, not BPM/level locked
    /**
     * No-op stub kept for API compatibility with other sound engine implementations.
     * The background track runs at a fixed BPM and does not respond to this call.
     *
     * @param bpm ignored
     */
    public void setBpm(int bpm)     {}

    /**
     * No-op stub kept for API compatibility with other sound engine implementations.
     * The background track runs at a fixed tempo and does not respond to this call.
     *
     * @param level ignored
     */
    public void setLevel(int level) {}

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Starts the background music playback thread. Any previously running thread
     * is stopped first via {@link #stop()} before the new one is created.
     *
     * @param bpm ignored by this implementation; the track plays at a fixed 110 BPM
     */
    public void start(int bpm) {
        stop();
        running = true;
        playbackThread = new Thread(this::stream, "SynthPopMusic");
        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    /**
     * Signals the playback thread to exit at the next loop boundary and
     * clears the thread reference. Returns immediately without waiting for
     * the thread to finish.
     */
    public void stop() {
        running = false;
        if (playbackThread != null) { playbackThread.interrupt(); playbackThread = null; }
    }

    /**
     * Mutes or unmutes the audio output. When muted, the render loop continues
     * running (maintaining beat timing) but writes silence to the output line.
     *
     * @param m {@code true} to silence output; {@code false} to restore audio
     */
    public void setMuted(boolean m) { muted = m; }

    /**
     * Returns whether the audio output is currently muted.
     *
     * @return {@code true} if muted
     */
    public boolean isMuted()        { return muted; }

    /**
     * Sets the master volume for all audio output.
     *
     * @param v volume in the range [0.0, 1.0]; clamped if out of range
     */
    public void setVolume(float v)  { volume = Math.max(0f, Math.min(1f, v)); }

    /**
     * Returns the current master volume level.
     *
     * @return volume in the range [0.0, 1.0]
     */
    public float getVolume()        { return volume; }

    /**
     * Stops playback and releases all audio resources. Equivalent to
     * calling {@link #stop()}. Safe to call multiple times.
     */
    public void shutdown()          { stop(); }

    // Sound Effects

    /**
     * HURT - sharp downward pitch-sweep stab (600 to 80 Hz) with distortion.
     * Fires on its own daemon thread so it never blocks the music stream.
     */
    public void playHurtSound() {
        if (muted) return;
        Thread t = new Thread(() -> {
            int    n   = (int)(SAMPLE_RATE * 0.22);
            byte[] buf = new byte[n * 2];
            double ph  = 0;
            for (int i = 0; i < n; i++) {
                double progress = (double) i / n;
                double freq = 600.0 * Math.pow(80.0 / 600.0, progress);
                ph += 2 * Math.PI * freq / SAMPLE_RATE;
                double env   = Math.exp(-progress * 12.0);
                double wave  = Math.signum(Math.sin(ph)) * 0.6 + Math.sin(ph) * 0.4;
                double noise = (Math.random() * 2 - 1) * Math.max(0, 1 - progress * 6);
                double s     = (wave * 0.75 + noise * 0.25) * env * 0.65 * volume;
                int pcm = Math.max(-32768, Math.min(32767, (int)(s * 32767)));
                buf[i * 2]     = (byte)(pcm & 0xFF);
                buf[i * 2 + 1] = (byte)((pcm >> 8) & 0xFF);
            }
            playMonoPcm(buf);
        }, "HurtSound");
        t.setDaemon(true);
        t.start();
    }

    /**
     * GAME OVER - deep descending boom (220 to 30 Hz) with harmonic overtones.
     */
    public void playGameOverSound() {
        if (muted) return;
        Thread t = new Thread(() -> {
            int    n   = (int)(SAMPLE_RATE * 0.85);
            byte[] buf = new byte[n * 2];
            double ph  = 0;
            for (int i = 0; i < n; i++) {
                double progress = (double) i / n;
                double freq = 220.0 * Math.pow(0.25, progress * 1.5);
                ph += 2 * Math.PI * freq / SAMPLE_RATE;
                double env  = Math.exp(-progress * 4.5);
                double wave = Math.sin(ph) * 0.7
                        + Math.sin(ph * 2) * 0.2
                        + Math.sin(ph * 3) * 0.1;
                double s    = wave * env * 0.55 * volume;
                int pcm = Math.max(-32768, Math.min(32767, (int)(s * 32767)));
                buf[i * 2]     = (byte)(pcm & 0xFF);
                buf[i * 2 + 1] = (byte)((pcm >> 8) & 0xFF);
            }
            playMonoPcm(buf);
        }, "GameOverSound");
        t.setDaemon(true);
        t.start();
    }

    /**
     * CLICK - short bright tick for UI button presses.
     */
    public void playClickSound() {
        if (muted) return;
        Thread t = new Thread(() -> {
            int    n   = (int)(SAMPLE_RATE * 0.055);
            byte[] buf = new byte[n * 2];
            double ph  = 0;
            for (int i = 0; i < n; i++) {
                double progress = (double) i / n;
                double env  = Math.exp(-progress * 65.0);
                ph += 2 * Math.PI * 1100.0 / SAMPLE_RATE;
                double tone  = Math.sin(ph) * 0.55 + Math.sin(ph * 2.4) * 0.25;
                double noise = (Math.random() * 2 - 1) * Math.max(0, 1 - progress * 18);
                double s     = (tone * 0.7 + noise * 0.3) * env * 0.50 * volume;
                int pcm = Math.max(-32768, Math.min(32767, (int)(s * 32767)));
                buf[i * 2]     = (byte)(pcm & 0xFF);
                buf[i * 2 + 1] = (byte)((pcm >> 8) & 0xFF);
            }
            playMonoPcm(buf);
        }, "ClickSound");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Opens a mono 16-bit PCM {@link SourceDataLine}, writes the supplied buffer
     * in one call, drains it, and closes the line. Used for one-shot sound effects.
     *
     * @param buf little-endian signed 16-bit mono PCM samples (2 bytes per frame)
     */
    private void playMonoPcm(byte[] buf) {
        try {
            AudioFormat fmt = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    SAMPLE_RATE, 16, 1, 2, SAMPLE_RATE, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
            if (!AudioSystem.isLineSupported(info)) return;
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(fmt, buf.length * 2);
            line.start();
            line.write(buf, 0, buf.length);
            line.drain();
            line.close();
        } catch (LineUnavailableException ignored) {}
    }


    // ── Main streaming loop ───────────────────────────────────────────────────

    /**
     * Main PCM streaming loop executed on {@link #playbackThread}.
     *
     * <p>Opens a stereo 16-bit 44.1 kHz {@link SourceDataLine} and renders
     * {@link #CHUNK} stereo frames per iteration by synthesising and mixing the
     * four voices (kick, bass, arpeggio, pad). Runs until {@link #running} is
     * set to {@code false} or the thread is interrupted.</p>
     *
     * <p>The output is passed through a soft tanh clipper to prevent digital
     * clipping from voice accumulation, then written directly to the audio line.</p>
     */
    private void stream() {
        AudioFormat fmt = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                SAMPLE_RATE, 16, 2, 4, SAMPLE_RATE, false); // STEREO
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
        if (!AudioSystem.isLineSupported(info)) return;

        try {
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(fmt, CHUNK * 8);
            line.start();

            // Per-channel oscillator phases
            double phKick     = 0;   // kick sine sweep phase
            double phBass     = 0;
            double phBassub   = 0;
            double[] phPad    = new double[3];  // 3-voice pad
            double phArp      = 0;

            // Envelope state
            double kickEnv    = 0;
            double bassEnv    = 0;
            double arpEnv     = 0;

            // Timing counters (in samples)
            long   sample     = 0;
            double kickFreq   = 150.0;

            // Previous arp/bass note info
            int    prevArpStep    = -1;
            int    prevBassStep   = -1;
            double arpFreqTarget  = 440.0;
            double bassFreqTarget = CHORD_ROOT[0];

            byte[] buf = new byte[CHUNK * 4]; // stereo = 4 bytes/frame

            while (running && !Thread.currentThread().isInterrupted()) {

                for (int i = 0; i < CHUNK; i++) {
                    double t       = (double) sample / SAMPLE_RATE;

                    // ── Timing positions ─────────────────────────────────────
                    double beatPos   = (t / BEAT_SEC);            // absolute beat
                    double barPos    = beatPos / 4.0;             // absolute bar
                    int    chordIdx  = (int)(barPos) % CHORD_ROOT.length;
                    double beatPhase = (beatPos % 1.0);           // 0..1 within beat
                    double sixPos    = (t / SIXTEENTH);           // absolute 16th
                    int    sixStep   = (int)(sixPos) % 16;        // step within bar

                    // ── Fade in ──────────────────────────────────────────────
                    double fadeIn = Math.min(t / 2.5, 1.0);

                    // ── KICK (every beat: beatPhase resets to 0) ─────────────
                    boolean newBeat = (beatPhase < (double)CHUNK / SAMPLE_RATE / BEAT_SEC * 2);
                    // Detect beat boundary by integer beat change
                    int curBeatInt = (int) beatPos;
                    // Use sample-accurate beat trigger
                    boolean beatTrigger = (sample % (long)(SAMPLE_RATE * BEAT_SEC) < CHUNK);

                    if (beatTrigger) {
                        kickEnv  = 1.0;
                        kickFreq = 150.0;
                        phKick   = 0;
                    }

                    // Kick synthesis: swept sine
                    kickFreq  *= Math.exp(-20.0 / SAMPLE_RATE);   // freq decay
                    kickEnv   *= Math.exp(-18.0 / SAMPLE_RATE);   // amp decay
                    phKick    += 2 * Math.PI * kickFreq / SAMPLE_RATE;
                    double kick = Math.sin(phKick) * kickEnv * VOL_KICK;

                    // ── BASS (triggers on beat 0 and beat 2 of each bar) ─────
                    int barBeat = (int)(beatPos) % 4;
                    boolean bassTrigger = beatTrigger && (barBeat == 0 || barBeat == 2);
                    if (bassTrigger) {
                        bassEnv       = 1.0;
                        bassFreqTarget = CHORD_ROOT[chordIdx] * (barBeat == 2 ? 1.5 : 1.0);
                        phBass        = 0;
                        phBassub      = 0;
                    }
                    bassEnv *= Math.exp(-6.5 / SAMPLE_RATE);
                    phBass  += 2 * Math.PI * bassFreqTarget / SAMPLE_RATE;
                    phBassub+= 2 * Math.PI * (bassFreqTarget / 2.0) / SAMPLE_RATE;
                    // Sawtooth + sub sine
                    double saw  = ((phBass / Math.PI) % 2.0) - 1.0;
                    double sub  = Math.sin(phBassub) * 0.5;
                    // Low-pass the saw (simple one-pole)
                    double bass = (saw * 0.6 + sub * 0.4) * bassEnv * VOL_BASS;

                    // ── ARP (16th notes) ──────────────────────────────────────
                    if (sixStep != prevArpStep) {
                        prevArpStep  = sixStep;
                        int semi     = ARP_SEMITONES[sixStep];
                        arpFreqTarget = CHORD_ROOT[chordIdx] * Math.pow(2.0, semi / 12.0);
                        // Octave up for brightness
                        if (sixStep >= 8) arpFreqTarget *= 2.0;
                        arpEnv = 0.9;
                        phArp  = 0;
                    }
                    arpEnv *= Math.exp(-22.0 / SAMPLE_RATE);
                    phArp  += 2 * Math.PI * arpFreqTarget / SAMPLE_RATE;
                    // Triangle wave for that classic synth pluck brightness
                    double tri = (2.0 / Math.PI) * Math.asin(Math.sin(phArp));
                    double arp = tri * arpEnv * VOL_ARP;

                    // ── PAD (3-voice lush chord, slow attack) ─────────────────
                    double padAtt    = Math.min(t / 1.5, 1.0);   // global slow attack
                    double[] ratios  = CHORD_RATIOS[chordIdx];
                    double rootFreq  = CHORD_ROOT[chordIdx];
                    double pad       = 0;
                    for (int v = 0; v < 3; v++) {
                        phPad[v] += 2 * Math.PI * rootFreq * ratios[v] / SAMPLE_RATE;
                        // Slightly detuned per voice for width
                        double detune = 1.0 + (v - 1) * 0.003;
                        pad += Math.sin(phPad[v] * detune) * (1.0 / 3.0);
                    }
                    pad *= VOL_PAD * padAtt;

                    // ── Mix all layers ────────────────────────────────────────
                    double mixL = (kick + bass + arp + pad) * fadeIn;
                    // Slight stereo spread: arp panned slightly right, pad left
                    double mixR = (kick + bass + arp * 1.2 + pad * 0.8) * fadeIn;

                    // Soft clip
                    mixL = Math.tanh(mixL * 1.4) * 0.75;
                    mixR = Math.tanh(mixR * 1.4) * 0.75;

                    int pcmL = muted ? 0 : Math.max(-32768, Math.min(32767, (int)(mixL * 32767 * volume)));
                    int pcmR = muted ? 0 : Math.max(-32768, Math.min(32767, (int)(mixR * 32767 * volume)));

                    buf[i * 4]     = (byte)(pcmL & 0xFF);
                    buf[i * 4 + 1] = (byte)((pcmL >> 8) & 0xFF);
                    buf[i * 4 + 2] = (byte)(pcmR & 0xFF);
                    buf[i * 4 + 3] = (byte)((pcmR >> 8) & 0xFF);

                    sample++;
                }

                line.write(buf, 0, buf.length);
            }

            line.drain();
            line.close();

        } catch (LineUnavailableException | IllegalArgumentException ignored) {
        } catch (Exception ignored) {}
    }
}