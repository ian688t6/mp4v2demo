package com.gc.mp4v2demo;

import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;

public class MP4v2Codec {

    private static final String TAG = "MP4v2Codec";

    public MP4v2Codec() {
    }


    public MP4VideoInfo getVideoInfo() {

        MP4VideoInfo mp4Info = new MP4VideoInfo();
        if (0 > getMP4VideoInfo(mp4Info)) {
            Log.e(TAG, "can not get video info");
            return null;
        }

        return mp4Info;
    }

    public MP4AudioInfo getAudioInfo() {
        MP4AudioInfo audioInfo = new MP4AudioInfo();
        if (0 > getMP4AudioInfo(audioInfo)) {
            Log.e(TAG, "can not get audio info");
            return null;
        }
        return audioInfo;
    }

    public int readVideoSampleData(ByteBuffer bb, int trackid, int off) {
        return readVideoSample(bb, trackid, off);
    }

    public int readAudioSampleData(byte[] b, int trackid, int sampleid, int cnt) {
        return readAudioSample(b, trackid, sampleid, cnt);
    }

    public int readMP4Sample(ByteBuffer bb, int trackid, int off, int cnt) {
        return readSample(bb, trackid, off, cnt);
    }

    public int readMP4Sample(ByteBuffer bb, int trackid, int off) {
        return readSample(bb, trackid, off, 1);
    }

    public void set(MP4VideoInfo info, Surface surface) {

    }

    public int openSource(String filename) {
        return openMP4File(filename);
    }

    public void closeSource() {
        closeMP4File();
    }

    public long getMP4SampleTime(int trackid, int sampleid) { return getSampleTime(trackid, sampleid); }

    static {
        /*
        加载动态库，动态库加载的时候 JNI_OnLoad函数会被调用

        在JNI——OnLoad函数中，Java虚拟机通过函数表的形式将JNI函数和java类中native函数对应起来
         */
        System.loadLibrary("native-lib");
    }

    /*
        Jni 函数的声明
        当调用到此函数时，java虚拟机会通过JNI_OnLoad里注册的函数表找到对应的函数去执行
    */
    private native int openMP4File(String filename);

    private native int closeMP4File();

    private native int getMP4VideoInfo(MP4VideoInfo info);

    private native int readVideoSample(ByteBuffer byteBuffer, int trackid, int sampleid);

    private native int readAudioSample(byte[] b, int trackid, int sampleid, int cnt);

    private native int readSample(ByteBuffer byteBuffer, int trackid, int sampleid, int cnt);

    private native long getSampleTime(int trackid, int sampleid);

    private native int getMP4AudioInfo(MP4AudioInfo info);
}
