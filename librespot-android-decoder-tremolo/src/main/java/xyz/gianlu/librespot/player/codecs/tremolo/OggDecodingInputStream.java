package xyz.gianlu.librespot.player.codecs.tremolo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by M. Lehmann on 15.11.2016.
 */
@SuppressWarnings("unused")
public class OggDecodingInputStream extends InputStream {

    private static final int BUFFER_SIZE = 4096;
    private static final int SEEK_SET = 0;
    private static final int SEEK_CUR = 1;
    private static final int SEEK_END = 2;

    static {
        System.loadLibrary("tremolo");
    }

    /**
     * address of native OggFileHandle structure
     **/
    private final long handle;

    private final SeekableInputStream oggInputStream;
    private final ByteBuffer jniBuffer;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public boolean isClosed() {
        return isClosed.get();
    }

    public OggDecodingInputStream(SeekableInputStream oggInputStream) throws IOException {
        this.oggInputStream = oggInputStream;

        jniBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

        handle = initDecoder(jniBuffer);
        if (handle == 0) {
            throw new IOException("Couldn't start decoder!");
        }
    }

    private native long initDecoder(ByteBuffer jniBuffer);

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
        } catch (Exception e) {
            // TODO logging
        }
        return -1;
    }

    private int seekOgg(long offset, int whence) {
        try {
            if (whence == SEEK_SET) {
                oggInputStream.seek(offset);
            } else if (whence == SEEK_CUR) {
                oggInputStream.seek(oggInputStream.tell() + offset);
            } else if (whence == SEEK_END) {
                oggInputStream.seek(oggInputStream.length() + offset);
            }
            return 0;
        } catch (Exception e) {
            // TODO logging
        }
        return -1;
    }

    private int tellOgg() {
        try {
            return (int) oggInputStream.tell();
        } catch (Exception e) {
            // TODO logging
        }
        return -1;
    }

    private native int read(long handle, int len);

    private native int seekMs(long handle, int milliseconds);

    private native int seekSamples(long handle, int samples);

    private native long tellMs(long handle);

    private native long tellSamples(long handle);

    private native long totalSamples(long handle);

    @Override
    public int read() throws IOException {
        synchronized (isClosed) {
            if (isClosed.get()) {
                throw new IOException("OggDecodingInputStream already closed!");
            }
            jniBuffer.clear();
            int size = read(handle, 1);

            jniBuffer.limit(size);
        }
        final byte b = jniBuffer.get();
        jniBuffer.clear();

        return b;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        synchronized (isClosed) {
            if (isClosed.get()) {
                throw new IOException("read: OggDecodingInputStream already closed!");
            }

            len = Math.min(len, BUFFER_SIZE);
            jniBuffer.clear();
            int size = read(handle, len);
            if (size > 0) {
                jniBuffer.limit(size);
                jniBuffer.get(b, off, size);
                jniBuffer.clear();
                return size;
            }
        }
        return -1;
    }

    @Override
    public synchronized int read(byte[] b) throws IOException {
        synchronized (isClosed) {
            if (isClosed.get()) {
                throw new IOException("OggDecodingInputStream already closed!");
            }
            return this.read(b, 0, b.length);
        }
    }

    public synchronized int seekMs(int milliseconds) throws IOException {
        synchronized (isClosed) {
            if (isClosed.get()) {
                throw new IOException("seekMs: OggDecodingInputStream already closed!");
            }
            return seekMs(handle, milliseconds);
        }
    }

    public synchronized int seekSamples(int samples) throws IOException {
        synchronized (isClosed) {
            if (isClosed.get()) {
                throw new IOException("seekSamples: OggDecodingInputStream already closed!");
            }
            return seekSamples(handle, samples);
        }
    }

    public synchronized long tellMs() throws IOException {
        synchronized (isClosed) {
            if (isClosed.get()) {
                throw new IOException("tellMs: OggDecodingInputStream already closed!");
            }
            return tellMs(handle);
        }
    }

    public synchronized long tellSamples() throws IOException {
        synchronized (isClosed) {
            if (isClosed.get()) {
                throw new IOException("tellSamples: OggDecodingInputStream already closed!");
            }
            return tellSamples(handle);
        }
    }

    public synchronized long totalSamples() throws IOException {
        synchronized (isClosed) {
            if (isClosed.get()) {
                throw new IOException("totalSamples: OggDecodingInputStream already closed!");
            }
            return totalSamples(handle);
        }
    }

    @Override
    public void close() throws IOException {
        if (!isClosed.getAndSet(true)) {
            synchronized (isClosed) {
                close(handle);
                try {
                    oggInputStream.close();
                } catch (IOException ignored) {
                }
            }
            super.close();
        } else {
            // TODO logging
        }
    }

    public synchronized long seek(long bytes) throws IOException {
        final int samples = (int) (bytes / ((16 * 2) / 8));

        final int success = seekSamples(samples);
        if (success == 0) {
            return getSamplesToBytes(tellSamples(), 16, 2);
        }

        throw new IOException("seek: Failed to seekSamples!");
    }

    private long getSamplesToBytes(long samples, int bits, int channels) {
        return (samples * bits * channels) / 8;
    }

    private int getBytesToMilliSeconds(long bytes, int bits, int channels) {
        long bytesPerSecond = (bits * 44100 * channels) / 8;
        long seconds = bytes / bytesPerSecond;

        return (int) (seconds * 1000);
    }

    private native void close(long handle);
}
