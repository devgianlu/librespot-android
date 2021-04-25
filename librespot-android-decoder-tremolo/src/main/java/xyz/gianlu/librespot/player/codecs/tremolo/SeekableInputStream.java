package xyz.gianlu.librespot.player.codecs.tremolo;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by M. Lehmann on 08.12.2016.
 */
public abstract class SeekableInputStream extends InputStream {

    abstract public void seek(long positionBytes) throws IOException;

    abstract public long tell() throws IOException;

    abstract public long length() throws IOException;

    abstract public int read(byte[] bytes) throws IOException;

    abstract public void close() throws IOException;
}
