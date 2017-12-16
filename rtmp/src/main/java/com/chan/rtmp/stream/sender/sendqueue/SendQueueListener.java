package com.chan.rtmp.stream.sender.sendqueue;

public interface SendQueueListener {
    void good();
    void bad();
}
