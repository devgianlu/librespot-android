package xyz.gianlu.librespot.android;

import android.app.Application;

import xyz.gianlu.librespot.audio.format.SuperAudioFormat;
import xyz.gianlu.librespot.player.codecs.Codecs;
import xyz.gianlu.librespot.player.codecs.TremoloVorbisCodec;

public final class LibrespotApp extends Application {
    static {
        Codecs.replaceCodecs(SuperAudioFormat.VORBIS, TremoloVorbisCodec.class);
    }
}
