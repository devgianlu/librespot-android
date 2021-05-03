package xyz.gianlu.librespot.android;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import xyz.gianlu.librespot.audio.decoders.Decoders;
import xyz.gianlu.librespot.audio.format.SuperAudioFormat;
import xyz.gianlu.librespot.player.decoders.AndroidNativeDecoder;
import xyz.gianlu.librespot.player.decoders.TremoloVorbisDecoder;

public final class LibrespotApp extends Application {
    private static final String TAG = LibrespotApp.class.getSimpleName();

    static {
        if (isArm() && !BuildConfig.FORCE_NATIVE_DECODER) {
            Decoders.replaceDecoder(SuperAudioFormat.VORBIS, TremoloVorbisDecoder.class);
            Log.i(TAG, "Using ARM optimized Vorbis decoder");
        } else {
            Decoders.replaceDecoder(SuperAudioFormat.VORBIS, AndroidNativeDecoder.class);
            Log.i(TAG, "Using Android native Vorbis decoder");
        }
    }

    private static boolean isArm() {
        for (String abi : Build.SUPPORTED_ABIS)
            if (abi.contains("arm"))
                return true;

        return false;
    }
}
