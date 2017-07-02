package com.gc.mp4v2demo;


public interface MP4Player {
    int STATE_PLAY     = 1;
    int STATE_PAUSE    = 2;
    int STATE_STOP     = 3;

    void play(int running);
    void pause(int running);
    void stop(int running);
    void seek(int progress);
}
