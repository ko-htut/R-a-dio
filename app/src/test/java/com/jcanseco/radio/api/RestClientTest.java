package com.jcanseco.radio.api;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class RestClientTest {

    @Test
    public void testGetRadioRestService() {
        assertThat(RestClient.getRadioRestService()).isInstanceOf(RadioRestService.class);
    }

    @Test
    public void radioRestServiceShouldBeSingleton() {
        RadioRestService radioRestServiceOne = RestClient.getRadioRestService();
        RadioRestService radioRestServiceTwo = RestClient.getRadioRestService();

        assertThat(radioRestServiceOne).isSameAs(radioRestServiceTwo);
    }
}