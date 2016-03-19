package com.jcanseco.radio.models;

import com.jcanseco.radio.models.utilities.ModelTestingUtilities;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class DjTest {

    private Dj dj;

    @Before
    public void setup() throws Exception {
        dj = (Dj) ModelTestingUtilities.parseFakeJson(this, "Dj", Dj.class);
    }

    @Test
    public void testGetName() {
        assertThat(dj.getName()).isEqualTo("Hanyuu-sama");
    }

    @Test
    public void testGetId() {
        assertThat(dj.getId()).isEqualTo(18);
    }

    @Test
    public void testGetAvatarUrl() {
        assertThat(dj.getAvatarUrl()).isEqualTo("http://r-a-d.io/api/dj-image/18");
    }
}