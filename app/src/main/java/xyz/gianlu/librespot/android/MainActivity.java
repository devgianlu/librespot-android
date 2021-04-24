package xyz.gianlu.librespot.android;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.spotify.connectstate.Connect;

import java.io.IOException;
import java.security.GeneralSecurityException;

import xyz.gianlu.librespot.core.Session;
import xyz.gianlu.librespot.mercury.MercuryClient;
import xyz.gianlu.librespot.player.Player;
import xyz.gianlu.librespot.player.PlayerConfiguration;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            Session session;
            try {
                Session.Configuration conf = new Session.Configuration.Builder()
                        .setStoreCredentials(false)
                        .setCacheEnabled(false).build();
                session = new Session.Builder(conf)
                        .setPreferredLocale("en")
                        .setDeviceType(Connect.DeviceType.SMARTPHONE)
                        .setDeviceName("librespot-java")
                        .userPass("user", "password")
                        .setDeviceId(null).create();

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
                    .setOutputClass("xyz.gianlu.librespot.android.sink.AndroidSinkOutput")
                    .setOutputClassParams(new String[0])
                    .build();
            Player player = new Player(configuration, session);
            player.load("spotify:album:5m4VYOPoIpkV0XgOiRKkWC", true, false);

        }).start();
    }
}
