package com.chan.rtmp.stream.sender.rtmp.io;

import com.chan.rtmp.stream.sender.rtmp.packets.Chunk;

public interface OnReadListener {
    void onChunkRead(Chunk chunk);
    void onDisconnect();
    void onStreamEnd();
}
