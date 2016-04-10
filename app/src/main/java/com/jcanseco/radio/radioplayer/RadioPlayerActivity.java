package com.jcanseco.radio.radioplayer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jcanseco.radio.R;
import com.jcanseco.radio.constants.Constants;
import com.jcanseco.radio.injectors.Injector;
import com.jcanseco.radio.radioplayer.broadcastreceivers.FailedToPlayStreamBroadcastReceiver;
import com.jcanseco.radio.radioplayer.serviceconnections.RadioPlayerServiceConnection;
import com.jcanseco.radio.services.RadioPlayerService;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RadioPlayerActivity extends AppCompatActivity implements RadioPlayerPresenter.View,
        RadioPlayerServiceConnection.ServiceConnectionListener, FailedToPlayStreamBroadcastReceiver.BroadcastReceivedListener {

    RadioPlayerPresenter radioPlayerPresenter;

    RadioPlayerService radioPlayerService;
    RadioPlayerServiceConnection radioPlayerServiceConnection;
    FailedToPlayStreamBroadcastReceiver failedToPlayStreamBroadcastReceiver;

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

        radioPlayerServiceConnection = Injector.provideRadioPlayerServiceConnection(this);
        failedToPlayStreamBroadcastReceiver = Injector.provideFailedToPlayStreamBroadcastReceiver(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        radioPlayerPresenter.onStart();
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

    @Override
    protected void onStop() {
        super.onStop();

        radioPlayerPresenter.onStop();
    }

    @Override
    public void startRadioPlayerService() {
        startService(getServiceIntent());
    }

    @Override
    public void bindToRadioPlayerService() {
        bindService(getServiceIntent(), radioPlayerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void unbindFromRadioPlayerService() {
        unbindService(radioPlayerServiceConnection);
        radioPlayerPresenter.onRadioPlayerServiceDisconnected();
        radioPlayerService = null;
    }

    @Override
    public void onRadioPlayerServiceConnected(RadioPlayerService radioPlayerService) {
        radioPlayerPresenter.onRadioPlayerServiceConnected(radioPlayerService.isPlayingStream());
        this.radioPlayerService = radioPlayerService;
    }

    @Override
    public void onRadioPlayerServiceDisconnected() {
        radioPlayerPresenter.onRadioPlayerServiceDisconnected();
        radioPlayerService = null;
    }

    @Override
    public void registerFailedToPlayStreamBroadcastReceiver() {
        String broadcastIntentAction = Constants.Actions.FAILED_TO_PLAY_RADIO_STREAM;
        LocalBroadcastManager.getInstance(this).registerReceiver(failedToPlayStreamBroadcastReceiver, new IntentFilter(broadcastIntentAction));
    }

    @Override
    public void unregisterFailedToPlayStreamBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(failedToPlayStreamBroadcastReceiver);
    }

    @Override
    public void onFailedToPlayStreamBroadcastReceived() {
        radioPlayerPresenter.onFailedToPlayStreamBroadcastReceived();
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
        radioPlayerService.startPlayingRadioStream(streamUrl);
    }

    @Override
    public void stopPlayingRadioStream() {
        radioPlayerService.stopPlayingRadioStream();
    }

    @Override
    public void showCouldNotLoadRadioContentErrorMessage() {
        Toast.makeText(this, R.string.failed_to_load_content, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showCouldNotPlayRadioStreamErrorMessage() {
        Toast.makeText(this, R.string.failed_to_load_stream, Toast.LENGTH_SHORT).show();
    }

    private Intent getServiceIntent() {
        return new Intent(this, RadioPlayerService.class);
    }
}
