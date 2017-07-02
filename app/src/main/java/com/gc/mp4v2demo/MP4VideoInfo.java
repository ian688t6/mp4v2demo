package com.gc.mp4v2demo;

/**
 * Created by jyin on 21/06/2017.
 */

public class MP4VideoInfo {
    private int mTrackid;
    private int mWidth;
    private int mHeight;
    private int mSamples;
    private int mSampleMaxsize;
    private int mFramerate;
    private byte[] mSpsHeader;
    private byte[] mPpsHeader;

    public int getTrackid() { return mTrackid; }

    public int getFramerate() {
        return mFramerate;
    }

    public int getSamples() {
        return mSamples;
    }

    public int getSampleMaxsize() { return mSampleMaxsize; }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public byte[] getSpsHeader() {
        return mSpsHeader;
    }

    public byte[] getPpsHeader() {
        return mPpsHeader;
    }
}
