package com.clementlan.pebblefy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.SpotifyPlayer;

// Imports for Spotify Web API
import java.util.UUID;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends Activity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback
{
    private static final UUID PEBBLE_UUID = UUID.fromString("3ef8524d-6493-49af-b014-90243f5fba61");
    private static final String CLIENT_ID = "8d8624c00f8f48da862bf7447286e708";
    private static final String REDIRECT_URI = "pebblefy-login://callback";
    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int LOGIN_REQUEST_CODE = 1004;

    // Add back in if we need the player
    //private Player mPlayer;
    private SpotifyApi mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mApi = new SpotifyApi();

        /*
        // Hook up the checkbox listener
        CheckBox shufflebox = (CheckBox) findViewById(R.id.shuffle_checkbox);
        shufflebox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mPlayer.setShuffle(null, b);
            }
        });
        // Hook up the checkbox listener
        CheckBox repeatbox = (CheckBox) findViewById(R.id.repeatCheckbox);
        repeatbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mPlayer.setRepeat(null, b);
            }
        });

        // Hook up next/prev buttons
        Button prev = (Button) findViewById(R.id.prevButton);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayer.skipToPrevious((Player.OperationCallback) MainActivity.this);
            }
        });
        Button next = (Button) findViewById(R.id.nextButton);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayer.skipToNext((Player.OperationCallback) MainActivity.this);
            }
        });
        */

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, LOGIN_REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // Check if result comes from the correct activity
        if (requestCode == LOGIN_REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                // Send the token to the pebble app
                PebbleDictionary pebble_dict = new PebbleDictionary();
                pebble_dict.addString(PebbleAppKeys.AccessToken, response.getAccessToken());
                PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_UUID, pebble_dict);

                // Set up the web API with the oAuth token
                this.mApi.setAccessToken(response.getAccessToken());
                /* We don't really need the player anymore... wooo....
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                        // default to shuffle state
                        mPlayer.setShuffle(null, true);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
                */
            }
        }
    }

    @Override
    protected void onDestroy() {
        // Add back in if we need the player after all
        //Spotify.destroyPlayer(this);
        super.onDestroy();
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
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
        SpotifyService spotify_web = mApi.getService();
        spotify_web.getMyPlaylists(new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> playlists, Response response) {
                Log.d("*** Found playlists:", String.valueOf(playlists.total));

                LinearLayout layout = (LinearLayout) findViewById(R.id.mlayout);
                for (PlaylistSimple plist : playlists.items) {
                    Log.d("*** playlist found: ", plist.name);
                    PlaylistTextView p_tv = new PlaylistTextView(getApplicationContext());
                    p_tv.setPlaylist(plist);
                    //p_tv.setClickable(true);
                    p_tv.setText(plist.name);
                    /*
                    p_tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mPlayer.playUri(null, ((PlaylistTextView) (v)).getPlaylistURI(), 0, 0);
                        }
                    });
                    */
                    layout.addView(p_tv);
                    // Before we move on, we need to set the gravity and other layout params for this list.
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) p_tv.getLayoutParams();
                    lp.gravity = Gravity.FILL;
                    lp.weight = 1;
                    p_tv.setLayoutParams(lp);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("getMyPlaylists failed: ", error.toString());
            }
        });
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error e) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }
}