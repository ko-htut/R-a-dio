package com.jcanseco.radio.radioplayer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jcanseco.radio.R;
import com.jcanseco.radio.injectors.Injector;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RadioPlayerActivity extends AppCompatActivity implements RadioPlayerPresenter.View {

    protected RadioPlayerPresenter radioPlayerPresenter;

    protected MediaPlayer mediaPlayer;

    @Bind(R.id.track_title)
    TextView trackTitleView;

    @Bind(R.id.dj_name)
    TextView djNameView;

    @Bind(R.id.num_of_listeners)
    TextView numOfListenersView;

    @Bind(R.id.progress_bar)
    ProgressBar progressBar;

    @Bind(R.id.elapsed_time)
    TextView elapsedTimeView;

    @Bind(R.id.track_length)
    TextView trackLengthView;

    @Bind(R.id.action_button)
    Button actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio_player);
        ButterKnife.bind(this);

        radioPlayerPresenter = Injector.provideRadioPlayerPresenter();
        radioPlayerPresenter.attachView(this);

        mediaPlayer = new MediaPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        radioPlayerPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        radioPlayerPresenter.onPause();
    }

    @OnClick(R.id.action_button)
    public void onActionButtonClick() {
        radioPlayerPresenter.onActionButtonClicked();
    }

    @Override
    public void showPlayButton() {
        actionButton.setBackgroundResource(R.drawable.play);
    }

    @Override
    public void showPauseButton() {
        actionButton.setBackgroundResource(R.drawable.pause);
    }

    @Override
    public void showCurrentTrackTitle(String title) {
        trackTitleView.setText(title);
    }

    @Override
    public void showCurrentDjName(String name) {
        djNameView.setText(name);
    }

    @Override
    public void showNumOfListeners(int numOfListeners) {
        String numOfListenersText = getString(R.string.num_of_listeners, numOfListeners);
        numOfListenersView.setText(numOfListenersText);
    }

    @Override
    public void startPlayingRadioStream(String streamUrl) {
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(buildOnPreparedListener());
            mediaPlayer.setOnErrorListener(buildOnErrorListener());
            mediaPlayer.setDataSource(streamUrl);
            mediaPlayer.prepareAsync();
        } catch (IOException|IllegalStateException e) {
            showCouldNotPlayRadioStreamErrorMessage();
        }
    }

    @Override
    public void stopPlayingRadioStream() {
        mediaPlayer.stop();
        mediaPlayer.reset();
    }

    @NonNull
    private MediaPlayer.OnPreparedListener buildOnPreparedListener() {
        return new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
            }
        };
    }

    @NonNull
    private MediaPlayer.OnErrorListener buildOnErrorListener() {
        return new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                showCouldNotPlayRadioStreamErrorMessage();
                mediaPlayer.reset();
                return false;
            }
        };
    }

    @Override
    public void showCouldNotLoadRadioContentErrorMessage() {
        Toast.makeText(this, R.string.failed_to_load_content, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showCouldNotPlayRadioStreamErrorMessage() {
        Toast.makeText(this, R.string.failed_to_load_stream, Toast.LENGTH_SHORT).show();
    }
}
