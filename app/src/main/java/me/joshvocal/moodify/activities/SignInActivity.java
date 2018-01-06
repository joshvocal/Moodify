package me.joshvocal.moodify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import me.joshvocal.moodify.R;

/**
 * SignInActivity prompts the user to sign into Spotify.
 */

public class SignInActivity extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback, View.OnClickListener {

    private Player mPlayer;
    private Button mSignInButton;
    private static String mAccessToken;

    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        setSignInButton();
        hideActionBar();
    }

    private void setSignInButton() {
        mSignInButton = (Button) findViewById(R.id.spotify_sign_in_button);
        mSignInButton.setOnClickListener(this);
    }

    private void hideActionBar() {
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setElevation(0);
    }

    public static String getAccessToken() {
        return mAccessToken;
    }

    private static void setAccessToken(String accessToken) {
        SignInActivity.mAccessToken = accessToken;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.spotify_sign_in_button:

                if (getString(R.string.spotify_client_id).equals("replace_key")) {
                    Toast.makeText(this, "Remember to fill in your keys!", Toast.LENGTH_SHORT).show();
                }

                AuthenticationRequest.Builder builder =
                        new AuthenticationRequest.Builder(getString(R.string.spotify_client_id),
                                AuthenticationResponse.Type.TOKEN,
                                getString(R.string.spotify_redirect_uri));

                builder.setScopes(new String[]{"user-read-private", "streaming"});
                AuthenticationRequest request = builder.build();

                // Check if the user has signed in and access token is valid
                if (mAccessToken != null) {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                } else {
                    AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
                }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {

            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

            if (response.getType() == AuthenticationResponse.Type.TOKEN) {

                setAccessToken(response.getAccessToken());

                Config playerConfig = new Config(this, response.getAccessToken(),
                        getString(R.string.spotify_client_id));
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {

                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(SignInActivity.this);
                        mPlayer.addNotificationCallback(SignInActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {

    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }
}
