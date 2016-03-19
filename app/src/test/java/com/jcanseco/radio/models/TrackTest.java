package com.jcanseco.radio.models;

import com.jcanseco.radio.models.utilities.ModelTestingUtilities;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class TrackTest {

    private Track track;

    @Before
    public void setup() throws Exception {
        track = (Track) ModelTestingUtilities.parseFakeJson(this, "Track", Track.class);
    }

    @Test
    public void testGetTitle() {
        assertThat(track.getTitle()).isEqualTo("Yoshida Hitomi - Kono Sora no Mukou");
    }
}