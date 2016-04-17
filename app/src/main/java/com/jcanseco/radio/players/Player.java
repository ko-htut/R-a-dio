package com.jcanseco.radio.players;

public interface Player {

    boolean isPlaying();

    void play();

    void pause();

    void release();

    void setPlayerListener(Player.Listener playerListener);


    interface Listener {

        void onPlayerStreamError();
    }
}
