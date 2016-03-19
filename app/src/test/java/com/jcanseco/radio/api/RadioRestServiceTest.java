package com.jcanseco.radio.api;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class RadioRestServiceTest {

    @Test
    public void testCreationOfRadioRestServiceUsingItsFactory() {
        assertThat(RadioRestService.Factory.create()).isInstanceOf(RadioRestService.class);
    }
}