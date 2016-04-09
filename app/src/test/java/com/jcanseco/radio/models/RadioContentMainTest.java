package com.jcanseco.radio.models;

import com.jcanseco.radio.testutilities.ModelTestingUtilities;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RadioContentMainTest {

    private RadioContentMain radioContentMain;

    @Before
    public void setup() throws Exception {
        radioContentMain = (RadioContentMain) ModelTestingUtilities.parseFakeJson(this, "RadioContentMain", RadioContentMain.class);
    }

    @Test
    public void testGetCurrentTrack() {
        NowPlayingTrack expectedCurrentTrack = new NowPlayingTrack("Excel Girls - Ai Sincere Heart", 1458341320, 1458341547);

        NowPlayingTrack actualCurrentTrack = radioContentMain.getCurrentTrack();

        assertEqualTracks(expectedCurrentTrack, actualCurrentTrack);
    }

    @Test
    public void whenCurrentTrackStartTimeAndEndTimeOnJsonAreNull_thenCurrentTrackStartTimeAndEndTimeShouldBeZero() throws  Exception{
        RadioContentMain radioContentMain = (RadioContentMain) ModelTestingUtilities.parseFakeJson(this, "RadioContentMainWithNulls", RadioContentMain.class);
        NowPlayingTrack expectedCurrentTrack = new NowPlayingTrack("Excel Girls - Ai Sincere Heart", 0, 0);

        NowPlayingTrack actualCurrentTrack = radioContentMain.getCurrentTrack();

        assertEqualTracks(expectedCurrentTrack, actualCurrentTrack);
    }

    @Test
    public void testGetCurrentDj() {
        Dj expectedCurrentDj = mock(Dj.class);
        when(expectedCurrentDj.getName()).thenReturn("Hanyuu-sama");
        when(expectedCurrentDj.getId()).thenReturn(18);

        Dj actualCurrentDj = radioContentMain.getCurrentDj();

        assertEquals(expectedCurrentDj.getName(), actualCurrentDj.getName());
        assertEquals(expectedCurrentDj.getId(), actualCurrentDj.getId());
    }

    @Test
    public void testGetNumOfCurrentListeners() {
        assertThat(radioContentMain.getNumOfListeners()).isEqualTo(283);
    }

    @Test
    public void testGetQueuedTracks() {
        List<Track> expectedTracks = new ArrayList<Track>() {{
            add(new Track("Yoshida Hitomi - Kono Sora no Mukou"));
            add(new Track("NANA - Explorer World"));
            add(new Track("Ayumi Hamasaki - Dearest"));
            add(new Track("AKINO with bless4 - Extra Magic Hour"));
            add(new Track("osamuraisan - world end dance hall"));
        }};

        List<Track> actualTracks = radioContentMain.getQueuedTracks();

        assertEqualTracks(expectedTracks.get(0), actualTracks.get(0));
        assertEqualTracks(expectedTracks.get(1), actualTracks.get(1));
        assertEqualTracks(expectedTracks.get(2), actualTracks.get(2));
        assertEqualTracks(expectedTracks.get(3), actualTracks.get(3));
        assertEqualTracks(expectedTracks.get(4), actualTracks.get(4));
    }

    @Test
    public void testGetLastPlayedTracks() {
        List<Track> expectedTracks = new ArrayList<Track>() {{
            add(new Track("The Seatbelts - Pushing the Sky"));
            add(new Track("Naomi Amagata - Suddenly"));
            add(new Track("Kanon Wakeshima - Still Doll"));
            add(new Track("Ken Ashcorp - Touch Fluffy Tail"));
            add(new Track("Touyama Nao - Blue Schedule"));
        }};

        List<Track> actualTracks = radioContentMain.getLastPlayedTracks();

        assertEqualTracks(expectedTracks.get(0), actualTracks.get(0));
        assertEqualTracks(expectedTracks.get(1), actualTracks.get(1));
        assertEqualTracks(expectedTracks.get(2), actualTracks.get(2));
        assertEqualTracks(expectedTracks.get(3), actualTracks.get(3));
        assertEqualTracks(expectedTracks.get(4), actualTracks.get(4));
    }

    @Test
    public void testGetDiscussionThreadUrl() {
        assertThat(radioContentMain.getDiscussionThreadUrl()).isEqualTo("http://discussionthread.com");
    }

    @Test
    public void whenDiscussionThreadIsNoneOnJson_thenDiscussionThreadUrlShouldBeNull() throws Exception {
        RadioContentMain radioContentMain = (RadioContentMain) ModelTestingUtilities.parseFakeJson(this, "RadioContentMainWithNulls", RadioContentMain.class);

        assertThat(radioContentMain.getDiscussionThreadUrl()).isNull();
    }

    private void assertEqualTracks(Track expectedTrack, Track actualTrack) {
        assertEquals(expectedTrack.getTitle(), actualTrack.getTitle());
    }

    private void assertEqualTracks(NowPlayingTrack expectedTrack, NowPlayingTrack actualTrack) {
        assertEquals(expectedTrack.getTitle(), actualTrack.getTitle());
        assertEquals(expectedTrack.getLengthInSeconds(), actualTrack.getLengthInSeconds());
        assertEquals(expectedTrack.getElapsedTimeInSeconds(), actualTrack.getElapsedTimeInSeconds());
    }
}