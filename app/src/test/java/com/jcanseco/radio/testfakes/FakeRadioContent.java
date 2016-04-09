package com.jcanseco.radio.testfakes;

import com.jcanseco.radio.models.Dj;
import com.jcanseco.radio.models.NowPlayingTrack;
import com.jcanseco.radio.models.RadioContent;
import com.jcanseco.radio.models.Track;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FakeRadioContent extends RadioContent {
    @Override
    public NowPlayingTrack getCurrentTrack() {
        NowPlayingTrack nowPlayingTrack = mock(NowPlayingTrack.class);

        when(nowPlayingTrack.getTitle()).thenReturn("current track title");

        when(nowPlayingTrack.getLengthAsTimeString()).thenReturn("3:42");
        when(nowPlayingTrack.getLengthInSeconds()).thenReturn(222);

        when(nowPlayingTrack.getElapsedTimeAsTimeString()).thenReturn("1:20");
        when(nowPlayingTrack.getElapsedTimeInSeconds()).thenReturn(80);

        when(nowPlayingTrack.getRemainingTimeAsTimeString()).thenReturn("2:22");
        when(nowPlayingTrack.getRemainingTimeInSeconds()).thenReturn(142);

        return nowPlayingTrack;
    }

    @Override
    public Dj getCurrentDj() {
        Dj dj = mock(Dj.class);

        when(dj.getName()).thenReturn("current dj name");
        when(dj.getId()).thenReturn(20);
        when(dj.getAvatarUrl()).thenReturn("http://r-a-d.io/api/dj-image/20");

        return dj;
    }

    @Override
    public int getNumOfListeners() {
        return 253;
    }

    @Override
    public List<Track> getQueuedTracks() {
        final Track firstTrack = mock(Track.class);
        final Track secondTrack = mock(Track.class);
        final Track thirdTrack = mock(Track.class);
        final Track fourthTrack = mock(Track.class);
        final Track fifthTrack = mock(Track.class);

        when(firstTrack.getTitle()).thenReturn("first queued track title");
        when(secondTrack.getTitle()).thenReturn("second queued track title");
        when(thirdTrack.getTitle()).thenReturn("third queued track title");
        when(fourthTrack.getTitle()).thenReturn("fourth queued track title");
        when(fifthTrack.getTitle()).thenReturn("fifth queued track title");

        List<Track> queuedTracks = new ArrayList<Track>() {{
            add(firstTrack);
            add(secondTrack);
            add(thirdTrack);
            add(fourthTrack);
            add(fifthTrack);
        }};

        return queuedTracks;
    }

    @Override
    public List<Track> getLastPlayedTracks() {
        final Track firstTrack = mock(Track.class);
        final Track secondTrack = mock(Track.class);
        final Track thirdTrack = mock(Track.class);
        final Track fourthTrack = mock(Track.class);
        final Track fifthTrack = mock(Track.class);

        when(firstTrack.getTitle()).thenReturn("first last played track title");
        when(secondTrack.getTitle()).thenReturn("second last played track title");
        when(thirdTrack.getTitle()).thenReturn("third last played track title");
        when(fourthTrack.getTitle()).thenReturn("fourth last played track title");
        when(fifthTrack.getTitle()).thenReturn("fifth last played track title");

        List<Track> lastPlayedTracks = new ArrayList<Track>() {{
            add(firstTrack);
            add(secondTrack);
            add(thirdTrack);
            add(fourthTrack);
            add(fifthTrack);
        }};

        return lastPlayedTracks;
    }

    @Override
    public String getDiscussionThreadUrl() {
        return "https://discussionthreadurl.com";
    }

    @Override
    public String getStreamUrl() {
        return "https://streamurl.com";
    }
}
