package xyz.gianlu.librespot.player.decoders;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

import xyz.gianlu.librespot.player.decoders.tremolo.OggDecodingInputStream;
import xyz.gianlu.librespot.player.decoders.tremolo.SeekableInputStream;
import xyz.gianlu.librespot.player.mixing.output.OutputAudioFormat;


public final class TremoloVorbisDecoder extends Decoder {
    private final byte[] buffer = new byte[2 * BUFFER_SIZE];
    private final OggDecodingInputStream in;

    public TremoloVorbisDecoder(@NotNull xyz.gianlu.librespot.player.decoders.SeekableInputStream audioFile, float normalizationFactor, int duration) throws IOException {
        super(audioFile, normalizationFactor, duration);
        seekZero = audioIn.position();
        in = new OggDecodingInputStream(new SeekableInputStream() {
            @Override
            public void seek(long positionBytes) throws IOException {
                audioIn.seek((int) (positionBytes + seekZero));
            }

            @Override
            public long tell() {
                return audioIn.position() - seekZero;
            }

            @Override
            public long length() throws IOException {
                return (audioIn.available() + audioIn.position()) - seekZero;
            }

            @Override
            public int read(byte[] bytes) throws IOException {
                return audioIn.read(bytes);
            }

            @Override
            public void close() {
                audioIn.close();
            }

            @Override
            public int read() throws IOException {
                return audioIn.read();
            }
        });

        setAudioFormat(new OutputAudioFormat(44100, 16, 2, true, false));
    }

    @Override
    protected synchronized int readInternal(@NotNull OutputStream outputStream) throws IOException {
        if (closed) return -1;

        int count = in.read(buffer);
        if (count == -1)
            return -1;

        outputStream.write(buffer, 0, count);
        outputStream.flush();
        return count;
    }

    @Override
    public int time() throws CannotGetTimeException {
        if (closed)
            throw new CannotGetTimeException("Codec is closed");

        return (int) in.tellMs();
    }

    @Override
    public void close() throws IOException {
        if (in != null) in.close();
        super.close();
    }

    @Override
    public void seek(int positionMs) {
        if (!closed) in.seekMs(positionMs);
    }
}
