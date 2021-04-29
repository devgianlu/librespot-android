package xyz.gianlu.librespot.android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.spotify.connectstate.Connect;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;

import xyz.gianlu.librespot.android.databinding.ActivityMainBinding;
import xyz.gianlu.librespot.android.sink.AndroidSinkOutput;
import xyz.gianlu.librespot.core.Session;
import xyz.gianlu.librespot.mercury.MercuryClient;
import xyz.gianlu.librespot.player.Player;
import xyz.gianlu.librespot.player.PlayerConfiguration;

public final class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LibrespotHolder.clear();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        File credentialsFile = new File(getDataDir(), "credentials.json");
        if (!credentialsFile.exists() || !credentialsFile.canRead()) {
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            return;
        }

        binding.logout.setOnClickListener(v -> {
            credentialsFile.delete();
            LibrespotHolder.clear();
            startActivity(new Intent(MainActivity.this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });

        new PlayerThread(credentialsFile, "spotify:track:1ozGeip1vdU0wY1dIw1scz", new PlayerCallback() {
            @Override
            public void startedPlayback() {
                Toast.makeText(MainActivity.this, R.string.playbackStarted, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void notLoggedIn() {
                startActivity(new Intent(MainActivity.this, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }

            @Override
            public void failedStartingPlayback(@NotNull Exception ex) {
                Toast.makeText(MainActivity.this, R.string.failedStartingPlayback, Toast.LENGTH_SHORT).show();
            }
        }).start();
    }

    private interface PlayerCallback {
        void startedPlayback();

        void notLoggedIn();

        void failedStartingPlayback(@NotNull Exception ex);
    }

    private static class PlayerThread extends Thread {
        private final File credentialsFile;
        private final String playUri;
        private final PlayerCallback callback;
        private final Handler handler;

        PlayerThread(@NotNull File credentialsFile, @NotNull String playUri, @NotNull PlayerCallback callback) {
            this.credentialsFile = credentialsFile;
            this.playUri = playUri;
            this.callback = callback;
            this.handler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void run() {
            Session session;
            if (LibrespotHolder.hasSession()) {
                session = LibrespotHolder.getSession();
                if (session == null) throw new IllegalStateException();
            } else if (credentialsFile.exists() && credentialsFile.canRead()) {
                try {
                    Session.Configuration conf = new Session.Configuration.Builder()
                            .setStoreCredentials(true)
                            .setStoredCredentialsFile(credentialsFile)
                            .setCacheEnabled(false)
                            .build();

                    Session.Builder builder = new Session.Builder(conf)
                            .setPreferredLocale(Locale.getDefault().getLanguage())
                            .setDeviceType(Connect.DeviceType.SMARTPHONE)
                            .setDeviceId(null).setDeviceName("librespot-android");

                    session = builder.stored(credentialsFile).create();
                    Log.i(TAG, "Logged in as: " + session.apWelcome().getCanonicalUsername());

                    LibrespotHolder.set(session);
                } catch (IOException |
                        GeneralSecurityException |
                        Session.SpotifyAuthenticationException |
                        MercuryClient.MercuryException ex) {
                    Log.e(TAG, "Session creation failed!", ex);
                    handler.post(() -> callback.failedStartingPlayback(ex));
                    return;
                }
            } else {
                handler.post(callback::notLoggedIn);
                return;
            }

            Player player;
            if (LibrespotHolder.hasPlayer()) {
                player = LibrespotHolder.getPlayer();
                if (player == null) throw new IllegalStateException();
            } else {
                PlayerConfiguration configuration = new PlayerConfiguration.Builder()
                        .setOutput(PlayerConfiguration.AudioOutput.CUSTOM)
                        .setOutputClass(AndroidSinkOutput.class.getName())
                        .build();

                player = new Player(configuration, session);
                LibrespotHolder.set(player);
            }

            try {
                player.waitReady();
            } catch (InterruptedException ex) {
                LibrespotHolder.clear();
                return;
            }

            player.load(playUri, true, false);
            handler.post(callback::startedPlayback);
        }
    }
}
