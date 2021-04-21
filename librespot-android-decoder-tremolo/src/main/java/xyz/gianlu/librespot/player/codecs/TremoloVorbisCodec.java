package xyz.gianlu.librespot.player.codecs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;

import xyz.gianlu.librespot.audio.GeneralAudioStream;
import xyz.gianlu.librespot.audio.NormalizationData;
import xyz.gianlu.librespot.player.PlayerConfiguration;
import xyz.gianlu.librespot.player.codecs.tremolo.OggDecodingInputStream;
import xyz.gianlu.librespot.player.codecs.tremolo.SeekableInputStream;
import xyz.gianlu.librespot.player.mixing.output.OutputAudioFormat;

public class TremoloVorbisCodec extends Codec {
    private final byte[] buffer = new byte[2 * BUFFER_SIZE];

    private OggDecodingInputStream inputStream;

    public TremoloVorbisCodec(@NotNull GeneralAudioStream audioFile, @Nullable NormalizationData normalizationData, @NotNull PlayerConfiguration conf, int duration) throws IOException {
        super(audioFile, normalizationData, conf, duration);

        seekZero = audioIn.pos();

        inputStream = new OggDecodingInputStream(new SeekableInputStream() {
            @Override
            public void seek(long positionBytes) throws IOException {
                audioFile.stream().seek((int) (positionBytes + seekZero));
            }

            @Override
            public long tell() {
                return audioFile.stream().pos() - seekZero;
            }

            @Override
            public long length() {
                return (audioFile.stream().available() + audioFile.stream().pos()) - seekZero;
            }

            @Override
            public int read(byte[] bytes) throws IOException {
                return audioFile.stream().read(bytes);
            }

            @Override
            public void close() {
                audioFile.stream().close();
            }

            @Override
            public int read() throws IOException {
                return audioFile.stream().read();
            }
        });

        setAudioFormat(new OutputAudioFormat(44100, 16, 2, true, false));
    }

    @Override
    protected synchronized int readInternal(@NotNull OutputStream outputStream) throws IOException {
        if (closed) return -1;

        int count = inputStream.read(buffer);
        if (count == -1) return -1;
        try {
            outputStream.write(buffer, 0, count);
            outputStream.flush();
        } catch (IllegalStateException e) {
            // TODO logging
        }
        return count;
    }

    @Override
    public int time() throws CannotGetTimeException {
        try {
            return (int) inputStream.tellMs();
        } catch (Exception e) {
            throw new CannotGetTimeException();
        }
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        super.close();
    }

    @Override
    public /*synchronized*/ void seek(int positionMs) {
        try {
            inputStream.seekMs(positionMs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
