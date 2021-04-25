package xyz.gianlu.librespot.player.codecs.tremolo;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by M. Lehmann on 15.11.2016.
 */
@SuppressWarnings("unused")
public class OggDecodingInputStream extends InputStream {
    private static final int BUFFER_SIZE = 4096;
    private static final int SEEK_SET = 0;
    private static final int SEEK_CUR = 1;
    private static final int SEEK_END = 2;
    private final static String TAG = OggDecodingInputStream.class.getName();

    static {
        System.loadLibrary("tremolo");
    }

    /**
     * address of native OggFileHandle structure
     **/
    private final long handle;
    private final SeekableInputStream oggInputStream;
    private final ByteBuffer jniBuffer;

    public OggDecodingInputStream(@NotNull SeekableInputStream oggInputStream) throws IOException {
        this.oggInputStream = oggInputStream;
        this.jniBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        this.handle = initDecoder(jniBuffer);
        if (handle == 0)
            throw new IOException("Couldn't start decoder!");
    }

    private native long initDecoder(ByteBuffer jniBuffer);

    private native int read(long handle, int len);

    private native int seekMs(long handle, int milliseconds);

    private native int seekSamples(long handle, int samples);

    private native long tellMs(long handle);

    private native long tellSamples(long handle);

    private native long totalSamples(long handle);

    private native void close(long handle);

    private int writeOgg(int size) {
        byte[] bytes = new byte[Math.min(size, BUFFER_SIZE)];
        try {
            int read = oggInputStream.read(bytes);
            if (read > -1) {
                jniBuffer.put(bytes);
                jniBuffer.flip();
                return read;
            }

            return 0;
        } catch (Exception ex) {
            Log.e(TAG, "Internal writeOgg failed.", ex);
            return -1;
        }
    }

    private int seekOgg(long offset, int whence) {
        try {
            if (whence == SEEK_SET)
                oggInputStream.seek(offset);
            else if (whence == SEEK_CUR)
                oggInputStream.seek(oggInputStream.tell() + offset);
            else if (whence == SEEK_END)
                oggInputStream.seek(oggInputStream.length() + offset);

            return 0;
        } catch (Exception ex) {
            Log.e(TAG, "Internal seekOgg failed.", ex);
            return -1;
        }
    }

    private int tellOgg() {
        try {
            return (int) oggInputStream.tell();
        } catch (Exception ex) {
            Log.e(TAG, "Internal tellOgg failed.", ex);
            return -1;
        }
    }

    @Override
    public synchronized int read() {
        jniBuffer.clear();

        int size = read(handle, 1);
        jniBuffer.limit(size);

        byte b = jniBuffer.get();
        jniBuffer.clear();
        return b;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) {
        len = Math.min(len, BUFFER_SIZE);
        jniBuffer.clear();
        int size = read(handle, len);
        if (size > 0) {
            jniBuffer.limit(size);
            jniBuffer.get(b, off, size);
            jniBuffer.clear();
            return size;
        }

        return -1;
    }

    @Override
    public synchronized int read(byte[] b) {
        return this.read(b, 0, b.length);
    }

    public synchronized int seekMs(int milliseconds) {
        return seekMs(handle, milliseconds);
    }

    public synchronized long tellMs() {
        return tellMs(handle);
    }

    @Override
    public synchronized void close() throws IOException {
        close(handle);
        oggInputStream.close();
        super.close();
    }
}
