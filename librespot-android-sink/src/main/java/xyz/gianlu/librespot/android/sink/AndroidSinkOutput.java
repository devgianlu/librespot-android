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

    @Override
    public boolean start(@NotNull OutputAudioFormat format) throws SinkException {
        int pcmEncoding = format.getSampleSizeInBits() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_FLOAT;
        int channelConfig = format.getChannels() == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
        int sampleRate = (int) format.getSampleRate();
        int minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                channelConfig,
                pcmEncoding
        );

        AudioFormat audioFormat = new AudioFormat.Builder()
                .setEncoding(pcmEncoding)
                .setSampleRate(sampleRate)
                .build();

        try {
            track = new AudioTrack.Builder()
                    .setBufferSizeInBytes(minBufferSize)
                    .setAudioFormat(audioFormat)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build();
        } catch (UnsupportedOperationException e) {
            throw new SinkException("AudioTrack creation failed in Sink: ", e.getCause());
        }

        if (lastVolume != -1) track.setVolume(lastVolume);

        track.play();
        return true;
    }

    @Override
    public void write(byte[] buffer, int offset, int len) throws IOException {
        int outcome = track.write(buffer, offset, len, AudioTrack.WRITE_BLOCKING);
        switch (outcome) {
            case AudioTrack.ERROR:
                throw new IOException("Generic Operation Failure while writing Track");
            case AudioTrack.ERROR_BAD_VALUE:
                throw new IOException("Invalid value used while writing Track");
            case AudioTrack.ERROR_DEAD_OBJECT:
                throw new IOException("Track Object has died in the meantime");
            case AudioTrack.ERROR_INVALID_OPERATION:
                throw new IOException("Failure due to improper use of Track Object methods");
        }
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
    public void close() {
        track = null;
    }
}
