package com.jcanseco.radio.models;

import com.jcanseco.radio.models.utilities.ModelTestingUtilities;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class RadioContentMetaTest {

    private RadioContentMeta radioContentMeta;

    @Before
    public void setup() throws Exception {
        radioContentMeta = (RadioContentMeta) ModelTestingUtilities.parseFakeJson(this, "RadioContentMeta", RadioContentMeta.class);
    }

    @Test
    public void testGetStreamUrl() {
        assertThat(radioContentMeta.getStreamUrl()).isEqualTo("https://stream.r-a-d.io/main.mp3");
    }
}