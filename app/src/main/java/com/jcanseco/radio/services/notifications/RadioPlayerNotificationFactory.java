package com.jcanseco.radio.services.notifications;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;

import com.jcanseco.radio.R;
import com.jcanseco.radio.loaders.RadioContentLoader;
import com.jcanseco.radio.models.NowPlayingTrack;
import com.jcanseco.radio.models.RadioContent;
import com.jcanseco.radio.radioplayer.RadioPlayerActivity;

public class RadioPlayerNotificationFactory implements RadioContentLoader.RadioContentListener {

    private Client client;
    private RadioContentLoader radioContentLoader;
    private Application applicationContext;

    public RadioPlayerNotificationFactory(Client client, RadioContentLoader radioContentLoader, Application applicationContext) {
        this.client = client;
        this.radioContentLoader = radioContentLoader;
        this.applicationContext = applicationContext;

        radioContentLoader.setRadioContentListener(this);
    }

    public void startScheduledCreationOfPlayerNotifications() {
        radioContentLoader.beginActiveLoadingOfContent();
    }

    public void stopScheduledCreationOfPlayerNotifications() {
        radioContentLoader.stopActiveLoadingOfContent();
    }

    @Override
    public void onRadioContentLoadSuccess(RadioContent radioContent) {
        Notification notification = buildPlayerNotification(radioContent);
        client.onPlayerNotificationCreationSuccess(notification);
    }

    private Notification buildPlayerNotification(RadioContent radioContent) {
        NowPlayingTrack currentTrack = radioContent.getCurrentTrack();

        String contentTitle = applicationContext.getString(R.string.radio_player_notification_content_title);
        return new Notification.Builder(applicationContext)
                .setContentTitle(contentTitle)
                .setContentText(currentTrack.getTitle())
                .setSmallIcon(R.drawable.play)
                .setContentIntent(buildOnClickNotificationContentIntent())
                .build();
    }

    private PendingIntent buildOnClickNotificationContentIntent() {
        Intent intent = new Intent(applicationContext, RadioPlayerActivity.class);
        return PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onRadioContentLoadFailed() {
        client.onPlayerNotificationCreationFailed();
    }


    public interface Client {

        void onPlayerNotificationCreationSuccess(Notification notification);

        void onPlayerNotificationCreationFailed();
    }
}
