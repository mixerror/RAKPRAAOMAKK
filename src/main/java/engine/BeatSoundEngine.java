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
 */
public class BeatSoundEngine {

    // ── Config ────────────────────────────────────────────────────────────────

    private static final int    SAMPLE_RATE  = 44100;
    private static final int    CHUNK        = 512;   // samples per render chunk
    private static final double BPM          = 110.0;
    private static final double BEAT_SEC     = 60.0 / BPM;          // ~0.545s
    private static final double SIXTEENTH    = BEAT_SEC / 4.0;      // 16th note

    private static final float  VOL_KICK     = 0.55f;
    private static final float  VOL_BASS     = 0.35f;
    private static final float  VOL_ARP      = 0.22f;
    private static final float  VOL_PAD      = 0.18f;

    // ── Chord progression: A  F#m  D  E  (each chord = 1 bar = 4 beats) ─────
    // Root frequencies (Hz) for bass + pad
    private static final double[] CHORD_ROOT = {
            220.00,  // A3
            185.00,  // F#3  (≈184.997)
            146.83,  // D3
            164.81,  // E3
    };

    // Major / minor chord intervals (ratio multipliers for pad voices)
    // A major:  1, 5/4, 3/2   |  F#m: 1, 6/5, 3/2   |  D maj: 1, 5/4, 3/2  |  E maj
    private static final double[][] CHORD_RATIOS = {
            { 1.0, 1.2599, 1.4983 },   // A  major
            { 1.0, 1.1892, 1.4983 },   // F# minor
            { 1.0, 1.2599, 1.4983 },   // D  major
            { 1.0, 1.2599, 1.4983 },   // E  major
    };

    // Arpeggio pattern (semitone offsets from chord root, 16 steps per bar)
    // Bright ascending + descending flourish
    private static final int[] ARP_SEMITONES = {
            0,  4,  7, 12,   // up
            16, 12,  7,  4,   // down
            0,  4,  9, 12,   // up with 9th colour
            14, 12,  7,  0    // down resolve
    };

    // ── State ─────────────────────────────────────────────────────────────────

    private Thread          playbackThread;
    private volatile boolean running = false;
    private volatile boolean muted   = false;

    // no-ops: music is fixed tempo, not BPM/level locked
    public void setBpm(int bpm)     {}
    public void setLevel(int level) {}

    // ── Public API ────────────────────────────────────────────────────────────

    public void start(int bpm) {
        stop();
        running = true;
        playbackThread = new Thread(this::stream, "SynthPopMusic");
        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    public void stop() {
        running = false;
        if (playbackThread != null) { playbackThread.interrupt(); playbackThread = null; }
    }

    public void setMuted(boolean m) { muted = m; }
    public boolean isMuted()        { return muted; }
    public void shutdown()          { stop(); }

    // ── Main streaming loop ───────────────────────────────────────────────────

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

                    int pcmL = muted ? 0 : Math.max(-32768, Math.min(32767, (int)(mixL * 32767)));
                    int pcmR = muted ? 0 : Math.max(-32768, Math.min(32767, (int)(mixR * 32767)));

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