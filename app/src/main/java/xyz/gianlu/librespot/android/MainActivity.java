package xyz.gianlu.librespot.android;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.security.GeneralSecurityException;

import xyz.gianlu.librespot.core.Session;
import xyz.gianlu.librespot.mercury.MercuryClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            try {
                Session session = new Session.Builder()
                        .userPass("test123", "test123")
                        .create();

                System.out.println("Logged in as: " + session.apWelcome().getCanonicalUsername());
            } catch (IOException | GeneralSecurityException | Session.SpotifyAuthenticationException | MercuryClient.MercuryException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}
