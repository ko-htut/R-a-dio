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
import com.jcanseco.radio.constants.Constants;
import com.jcanseco.radio.injectors.Injector;
import com.jcanseco.radio.models.RadioContent;
import com.jcanseco.radio.services.RadioContentService;
import com.jcanseco.radio.services.RadioPlayerService;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RadioPlayerActivity extends AppCompatActivity implements RadioPlayerPresenter.View {

    RadioPlayerPresenter radioPlayerPresenter;

    RadioPlayerService radioPlayerService;
    ServiceConnection radioPlayerServiceConnection;
    BroadcastReceiver failedToPlayStreamBroadcastReceiver;

    RadioContentService radioContentService;
    ServiceConnection radioContentServiceConnection;
    BroadcastReceiver radioContentLoadStatusBroadcastReceiver;

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

        radioContentServiceConnection = initRadioContentServiceConnection();
        radioContentLoadStatusBroadcastReceiver = initRadioContentLoadStatusBroadcastReceiver();

        radioPlayerServiceConnection = initRadioPlayerServiceConnection();
        failedToPlayStreamBroadcastReceiver = initFailedToPlayStreamBroadcastReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();

        radioPlayerPresenter.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        radioPlayerPresenter.onStop();
    }

    public void startServices() {
        startRadioPlayerService();
    }

    public void bindToServices() {
        bindToRadioContentService();
        bindToRadioPlayerService();
    }

    public void unbindFromServices() {
        unbindFromRadioContentService();
        unbindFromRadioPlayerService();
    }

    private ServiceConnection initRadioContentServiceConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                RadioContentService.RadioContentBinder radioContentBinder = (RadioContentService.RadioContentBinder) binder;
                radioContentService = radioContentBinder.getService();
                radioPlayerPresenter.onRadioContentServiceConnected();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                radioPlayerPresenter.onRadioContentServiceDisconnected();
                radioContentService = null;
            }
        };
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

    public void registerBroadcastReceivers() {
        registerRadioContentLoadStatusBroadcastReceiver();
        registerFailedToPlayStreamBroadcastReceiver();
    }

    public void unregisterBroadcastReceivers() {
        unregisterRadioContentLoadStatusBroadcastReceiver();
        unregisterFailedToPlayStreamBroadcastReceiver();
    }

    private void registerRadioContentLoadStatusBroadcastReceiver() {
        String broadcastIntentActionForLoadSuccess = Constants.Actions.RADIO_CONTENT_LOAD_SUCCESS;
        LocalBroadcastManager.getInstance(this).registerReceiver(radioContentLoadStatusBroadcastReceiver, new IntentFilter(broadcastIntentActionForLoadSuccess));

        String broadcastIntentActionForLoadFailed = Constants.Actions.RADIO_CONTENT_LOAD_FAILED;
        LocalBroadcastManager.getInstance(this).registerReceiver(radioContentLoadStatusBroadcastReceiver, new IntentFilter(broadcastIntentActionForLoadFailed));
    }

    private void unregisterRadioContentLoadStatusBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(radioContentLoadStatusBroadcastReceiver);
    }

    private void registerFailedToPlayStreamBroadcastReceiver() {
        String broadcastIntentAction = Constants.Actions.FAILED_TO_PLAY_RADIO_STREAM;
        LocalBroadcastManager.getInstance(this).registerReceiver(failedToPlayStreamBroadcastReceiver, new IntentFilter(broadcastIntentAction));
    }

    private void unregisterFailedToPlayStreamBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(failedToPlayStreamBroadcastReceiver);
    }

    private BroadcastReceiver initRadioContentLoadStatusBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                RadioContent radioContent = (RadioContent) intent.getSerializableExtra(Constants.Extras.RADIO_CONTENT);

                if (Constants.Actions.RADIO_CONTENT_LOAD_SUCCESS.equals(action)) {
                    radioPlayerPresenter.onRadioContentLoadSuccess(radioContent);
                } else if (Constants.Actions.RADIO_CONTENT_LOAD_FAILED.equals(action)) {
                    radioPlayerPresenter.onRadioContentLoadFailed();
                }
            }
        };
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

    private void bindToRadioContentService() {
        bindService(getRadioContentServiceIntent(), radioContentServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindFromRadioContentService() {
        unbindService(radioContentServiceConnection);
        radioPlayerPresenter.onRadioContentServiceDisconnected();
        radioContentService = null;
    }

    private void startRadioPlayerService() {
        startService(getRadioPlayerServiceIntent());
    }

    private void bindToRadioPlayerService() {
        bindService(getRadioPlayerServiceIntent(), radioPlayerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindFromRadioPlayerService() {
        unbindService(radioPlayerServiceConnection);
        radioPlayerPresenter.onRadioPlayerServiceDisconnected();
        radioPlayerService = null;
    }

    private Intent getRadioContentServiceIntent() {
        return new Intent(this, RadioContentService.class);
    }

    private Intent getRadioPlayerServiceIntent() {
        return new Intent(this, RadioPlayerService.class);
    }
}
