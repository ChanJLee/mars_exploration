package com.chan.rtmp.controller.audio;

import com.chan.rtmp.audio.OnAudioEncodeListener;
import com.chan.rtmp.configuration.AudioConfiguration;

public interface IAudioController {
    void start();
    void stop();
    void pause();
    void resume();
    void mute(boolean mute);
    int getSessionId();
    void setAudioConfiguration(AudioConfiguration audioConfiguration);
    void setAudioEncodeListener(OnAudioEncodeListener listener);
}
