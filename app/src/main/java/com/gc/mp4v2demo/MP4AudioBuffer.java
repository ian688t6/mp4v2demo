package com.gc.mp4v2demo;

public class MP4AudioBuffer {
    public byte[] mAudioBuffer;
    public int mSampleSize;

    MP4AudioBuffer(int size) {
        mAudioBuffer = new byte[size];
    }
}

//import java.nio.ByteBuffer;
//
//public class MP4AudioBuffer {
//    public ByteBuffer mAudioBuffer;
//    public int mSampleSize;
//
//    MP4AudioBuffer(int size) {
//        mAudioBuffer = ByteBuffer.allocateDirect(size);
//    }
//}
