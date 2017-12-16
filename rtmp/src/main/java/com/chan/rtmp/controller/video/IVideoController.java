package com.chan.rtmp.controller.video;

import com.chan.rtmp.configuration.VideoConfiguration;
import com.chan.rtmp.video.OnVideoEncodeListener;

public interface IVideoController {
    void start();
    void stop();
    void pause();
    void resume();
    boolean setVideoBps(int bps);
    void setVideoEncoderListener(OnVideoEncodeListener listener);
    void setVideoConfiguration(VideoConfiguration configuration);
}
