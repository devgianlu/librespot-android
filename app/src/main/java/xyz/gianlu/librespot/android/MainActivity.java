package xyz.gianlu.librespot.android;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.spotify.connectstate.Connect;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;

import xyz.gianlu.librespot.android.sink.AndroidSinkOutput;
import xyz.gianlu.librespot.audio.format.SuperAudioFormat;
import xyz.gianlu.librespot.core.Session;
import xyz.gianlu.librespot.mercury.MercuryClient;
import xyz.gianlu.librespot.player.Player;
import xyz.gianlu.librespot.player.PlayerConfiguration;
import xyz.gianlu.librespot.player.codecs.Codecs;
import xyz.gianlu.librespot.player.codecs.TremoloVorbisCodec;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";

    static {
        Codecs.replaceCodecs(SuperAudioFormat.VORBIS, TremoloVorbisCodec.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            Session session;
            try {
                Session.Configuration conf = new Session.Configuration.Builder()
                        .setStoreCredentials(true)
                        .setStoredCredentialsFile(new File(getDataDir(), "credentials.json"))
                        .setCacheEnabled(false)
                        .build();

                session = new Session.Builder(conf)
                        .setPreferredLocale(Locale.getDefault().getLanguage())
                        .setDeviceType(Connect.DeviceType.SMARTPHONE)
                        .setDeviceName("librespot-android")
                        .userPass("user", "password")
                        .setDeviceId(null)
                        .create();

                Log.i(TAG, "Logged in as: " + session.apWelcome().getCanonicalUsername());
            } catch (IOException |
                    GeneralSecurityException |
                    Session.SpotifyAuthenticationException |
                    MercuryClient.MercuryException ex) {
                Log.e(TAG, "Session creation failed: ", ex);
                return;
            }

            PlayerConfiguration configuration = new PlayerConfiguration.Builder()
                    .setOutput(PlayerConfiguration.AudioOutput.CUSTOM)
                    .setOutputClass(AndroidSinkOutput.class.getName())
                    .build();

            Player player = new Player(configuration, session);
            try {
                player.waitReady();
            } catch (InterruptedException ex) {
                return;
            }

            player.load("spotify:track:1ozGeip1vdU0wY1dIw1scz", true, false);
        }).start();
    }
}
