package me.joshvocal.moodify.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import me.joshvocal.moodify.R;
import me.joshvocal.moodify.activities.SignInActivity;

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener {

    private SpotifyPlayer mPlayer;

    private TracksPager mTracksPager;
    private List<Track> tracks;

    private TextView mArtistNameTextView;
    private TextView mTrackNameTextView;
    private TextView mTrackCurrentTimeTextView;
    private TextView mTrackEndTimeTextView;
    private ImageView mAlbumImageView;
    private SeekBar mSeekBar;
    private ImageView mPlayButton;
    private ImageView mPrevButton;
    private ImageView mNextButton;
    private MediaSessionCompat mMediaSession;

    private int mSongPosition = 0;
    private boolean mIsPlaying = false;

    private Handler mSeekBarHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        bindViews();
        FetchTracks fetchTracks = new FetchTracks();
        fetchTracks.execute(getIntent().getExtras().getString("emotion"));
    }

    private void bindViews() {
        mTrackNameTextView = (TextView) findViewById(R.id.title_tv);
        mTrackNameTextView.setSelected(true);
        mArtistNameTextView = (TextView) findViewById(R.id.artist_tv);

        mAlbumImageView = (ImageView) findViewById(R.id.album_full);

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mTrackCurrentTimeTextView = (TextView) findViewById(R.id.track_current_time_tv);

        mPlayButton = (ImageView) findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(this);

        mPrevButton = (ImageView) findViewById(R.id.previous_button);
        mPrevButton.setOnClickListener(this);

        mNextButton = (ImageView) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(this);

        mTrackEndTimeTextView = (TextView) findViewById(R.id.track_end_time_tv);
    }


    private void setUi() {
        mTrackNameTextView.setText(tracks.get(mSongPosition).name);
        mArtistNameTextView.setText(tracks.get(mSongPosition).artists.get(0).name);

        Picasso.with(this)
                .load(tracks.get(mSongPosition).album.images.get(0).url)
                .into(mAlbumImageView);

        mSeekBar.setMax(getSeekBarMax(tracks.get(mSongPosition).duration_ms));

        mTrackCurrentTimeTextView.setText("0:00");
        mTrackEndTimeTextView.setText(formatSeekBarTime(tracks.get(mSongPosition).duration_ms));

        prepareMusic();
    }


    private void prepareMusic() {
        final String trackUrl = tracks.get(mSongPosition).uri;

        Config playerConfig = new Config(this, SignInActivity.getAccessToken(), getString(R.string.spotify_client_id));
        mPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                mPlayer = spotifyPlayer;
                mPlayer.playUri(null, trackUrl, 0, 0);
                updateSeekBarTime.run();
                mIsPlaying = true;

            }

            @Override
            public void onError(Throwable throwable) {
                // Empty
            }
        });
    }


    private int getSeekBarMax(long durationInMilliseconds) {
        return (int) TimeUnit.MILLISECONDS.toSeconds(durationInMilliseconds);
    }


    private String formatSeekBarTime(long durationInMilliseconds) {
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(durationInMilliseconds),
                TimeUnit.MILLISECONDS.toSeconds(durationInMilliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationInMilliseconds)));
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.previous_button:
                // Decrease song position in playlist.
                mSongPosition--;
                // If this track is the first in the playlist, stay at current track.
                if (mSongPosition < 0) {
                    mSongPosition = 0;
                }

                // Refresh with new track information.
                setUi();
                prepareMusic();
                break;

            case R.id.play_button:
                if (!mIsPlaying) {
                    mPlayer.resume(null);
                    mPlayButton.setImageResource(R.drawable.ic_pause_black_24dp);
                    mIsPlaying = true;
                } else {
                    mPlayer.pause(null);
                    mPlayButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    mIsPlaying = false;
                }
                break;

            case R.id.next_button:
                // Increase song position in the playlist.
                mSongPosition++;
                // If this track is the last in the playlist, stay at the current track.
                if (mSongPosition > tracks.size() - 1) {
                    mSongPosition = 0;
                }

                // Refresh with new track information.
                setUi();
                prepareMusic();
                break;
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            // The reason why I couldn't get the seek bar to change positions when it
            // was clicked was because my seek bar progress was in seconds and Spotify's
            // seek bar is in milliseconds. That's why the song kept restarting from the
            // very start...
            mPlayer.seekToPosition(null, (int) TimeUnit.SECONDS.toMillis(progress));
        }
    }


    private Runnable updateSeekBarTime = new Runnable() {
        @Override
        public void run() {
            // Set mSeekBar progress using time played
            mSeekBar.setProgress((int) TimeUnit.MILLISECONDS.toSeconds(mPlayer.getPlaybackState().positionMs));
            // Set time remaining in minutes and seconds
            mTrackCurrentTimeTextView.setText(formatSeekBarTime(mPlayer.getPlaybackState().positionMs));
            // Ping for updated position every second
            mSeekBarHandler.postDelayed(updateSeekBarTime, 100);
        }
    };


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Empty
    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Empty
    }


    public class FetchTracks extends AsyncTask<String, Void, List<Track>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Track> doInBackground(String... emotion) {

            // For catching network extra exceptions
            try {
                // Do Spotify transaction
                SpotifyApi spotifyApi = new SpotifyApi();
                spotifyApi.setAccessToken(SignInActivity.getAccessToken());
                SpotifyService spotifyService = spotifyApi.getService();

                // Set Filters
                Map<String, Object> filters = new HashMap<>();
                filters.put("country", "US");

                // Search 10 tracks that match emotion.
                mTracksPager = spotifyService.searchTracks(emotion[0], filters);
                tracks = mTracksPager.tracks.items;

                return tracks;

            } catch (Exception e) {
                return tracks;
            }
        }


        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }


        @Override
        protected void onPostExecute(List<Track> tracks) {
            super.onPostExecute(tracks);
            setUi();
        }
    }
}
