package xyz.gianlu.librespot.player.decoders;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import xyz.gianlu.librespot.player.mixing.output.OutputAudioFormat;

public final class AndroidNativeDecoder extends Decoder {
    private static final String TAG = AndroidNativeDecoder.class.getSimpleName();
    private final byte[] buffer = new byte[2 * BUFFER_SIZE];
    private final MediaCodec codec;

    public AndroidNativeDecoder(@NotNull SeekableInputStream audioIn, float normalizationFactor, int duration) throws IOException {
        super(audioIn, normalizationFactor, duration);

        codec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_VORBIS);
        codec.configure(MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_VORBIS, 44100, 2), null, null, 0);
        codec.start();

        setAudioFormat(new OutputAudioFormat(44100, 16, 2, true, false));
    }

    @Override
    protected int readInternal(@NotNull OutputStream out) throws IOException, CodecException {
        if (closed) return -1;

        int inputBufferId = codec.dequeueInputBuffer(-1);
        if (inputBufferId >= 0) {
            int count = audioIn.read(buffer);
            if (count == -1)
                return -1;

            ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferId);
            inputBuffer.put(buffer, 0, count);
            codec.queueInputBuffer(inputBufferId, 0, count, -1, 0);
        }

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int outputBufferId = codec.dequeueOutputBuffer(info, -1);
        if (outputBufferId >= 0) {
            ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferId);
            out.write(outputBuffer.array(), info.offset, info.size);
            codec.releaseOutputBuffer(outputBufferId, false);
            return info.size;
        } else {
            Log.e(TAG, "Failed decoding: " + outputBufferId);
            return -1;
        }
    }

    @Override
    public void close() throws IOException {
        codec.release();
        super.close();
    }

    @Override
    public int time() throws CannotGetTimeException {

        return 0;
    }
}