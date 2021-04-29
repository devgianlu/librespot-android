package xyz.gianlu.librespot.android;

import android.app.Application;

import xyz.gianlu.librespot.audio.decoders.Decoders;
import xyz.gianlu.librespot.audio.format.SuperAudioFormat;
import xyz.gianlu.librespot.player.decoders.TremoloVorbisCodec;

public final class LibrespotApp extends Application {
    static {
        Decoders.replaceDecoder(SuperAudioFormat.VORBIS, TremoloVorbisCodec.class);
    }
}
