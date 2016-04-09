package com.jcanseco.radio.radioplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jcanseco.radio.R;
import com.jcanseco.radio.injectors.Injector;
import com.jcanseco.radio.services.RadioPlayerService;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RadioPlayerActivity extends AppCompatActivity implements RadioPlayerPresenter.View {

    RadioPlayerPresenter radioPlayerPresenter;

    RadioPlayerService radioPlayerService;
    ServiceConnection radioPlayerServiceConnection;
    BroadcastReceiver failedToPlayStreamBroadcastReceiver;


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

        radioPlayerServiceConnection = initRadioPlayerServiceConnection();
        failedToPlayStreamBroadcastReceiver = initFailedToPlayStreamBroadcastReceiver();
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

    public void startRadioPlayerService() {
        startService(getServiceIntent());
    }

    public void bindToRadioPlayerService() {
        bindService(getServiceIntent(), radioPlayerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindFromRadioPlayerService() {
        unbindService(radioPlayerServiceConnection);
        radioPlayerPresenter.onRadioPlayerServiceDisconnected();
        radioPlayerService = null;
    }

    private ServiceConnection initRadioPlayerServiceConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                RadioPlayerService.RadioPlayerBinder radioPlayerBinder = (RadioPlayerService.RadioPlayerBinder) binder;
                radioPlayerService = radioPlayerBinder.getService();
                radioPlayerPresenter.onRadioPlayerServiceConnected(radioPlayerService.isPlayingStream());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                radioPlayerPresenter.onRadioPlayerServiceDisconnected();
                radioPlayerService = null;
            }
        };
    }

    public void registerBroadcastReceiverToListenLocallyFor(String broadcastIntentAction) {
        LocalBroadcastManager.getInstance(this).registerReceiver(failedToPlayStreamBroadcastReceiver, new IntentFilter(broadcastIntentAction));
    }

    public void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(failedToPlayStreamBroadcastReceiver);
    }

    private BroadcastReceiver initFailedToPlayStreamBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                radioPlayerPresenter.onFailedToPlayStreamBroadcastReceived();
            }
        };
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
