package xyz.gianlu.librespot.android.sink;

import android.media.AudioFormat;
import android.media.AudioTrack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.IOException;

import xyz.gianlu.librespot.player.mixing.output.OutputAudioFormat;
import xyz.gianlu.librespot.player.mixing.output.SinkException;
import xyz.gianlu.librespot.player.mixing.output.SinkOutput;

/**
 * @author devgianlu
 */
public final class AndroidSinkOutput implements SinkOutput {
    private AudioTrack track;
    private float lastVolume = -1;

    public AndroidSinkOutput() {
    }

    @Override
    public boolean start(@NotNull OutputAudioFormat format) throws SinkException {
        AudioTrack.Builder builder = new AudioTrack.Builder();
        builder.setAudioFormat(new AudioFormat.Builder()
                .setSampleRate((int) format.getSampleRate())
                .build());

        track = builder.build();
        if (lastVolume != -1) track.setVolume(lastVolume);
        return true;
    }

    @Override
    public void write(byte[] buffer, int offset, int len) throws IOException {
        track.write(buffer, offset, len, AudioTrack.WRITE_BLOCKING);
    }

    @Override
    public void flush() {
        if (track != null) track.flush();
    }

    @Override
    public boolean setVolume(@Range(from = 0L, to = 1L) float volume) {
        lastVolume = volume;
        if (track != null) track.setVolume(volume);
        return true;
    }

    @Override
    public void release() {
        if (track != null) track.release();
    }

    @Override
    public void stop() {
        if (track != null) track.stop();
    }

    @Override
    public void close() throws IOException {
        track = null;
    }
}
