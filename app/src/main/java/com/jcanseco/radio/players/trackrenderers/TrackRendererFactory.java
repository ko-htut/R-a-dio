package com.jcanseco.radio.players.trackrenderers;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.jcanseco.radio.BuildConfig;
import com.jcanseco.radio.R;
import com.jcanseco.radio.constants.Constants;

public class TrackRendererFactory {

    private static final int BUFFER_SEGMENT_SIZE_IN_BYTES = 1024;
    private static final int NUM_OF_SEGMENTS_TO_BUFFER = 64;
    private static final int REQUESTED_BUFFER_SIZE = NUM_OF_SEGMENTS_TO_BUFFER * BUFFER_SEGMENT_SIZE_IN_BYTES;

    public static TrackRenderer createAudioTrackRenderer(Context context) {
        SampleSource sampleSource = createSampleSource(context);
        return new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT);
    }

    private static SampleSource createSampleSource(Context context) {
        Uri streamUri = Uri.parse(Constants.Endpoints.STREAM_URL);
        DataSource dataSource = new DefaultUriDataSource(context, null, getUserAgent(context));
        Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE_IN_BYTES);
        return new ExtractorSampleSource(streamUri, dataSource, allocator, REQUESTED_BUFFER_SIZE);
    }

    private static String getUserAgent(Context context) {
        String appName = getAppName(context);
        String appBuildVersion = getAppBuildVersion();
        return String.format("%s/%s", appName, appBuildVersion);
    }

    private static String getAppName(Context context) {
        return context.getString(R.string.app_name);
    }

    private static String getAppBuildVersion() {
        return BuildConfig.VERSION_NAME;
    }
}
