package com.gc.mp4v2demo;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.LinkedList;

public class MP4AudioPlayer extends Thread implements MP4Player {

    private MP4v2Codec mCodec;

    private MP4AudioInfo mInfo;

    private AudioTrack mTrack;

    private AudioPlayThread mPlayer;

    private int mBufferSize = 0;

    private final LinkedList<MP4AudioBuffer> mAudioBuffers = new LinkedList<MP4AudioBuffer>();

    private boolean isStop = true;

    private int mRunning = MP4Player.STATE_STOP;

    private int mProgress = 1;

    private final String TAG = "MP4AudioTrack";

    MP4AudioPlayer(MP4v2Codec codec, MP4AudioInfo info) {
        mCodec  = codec;
        mInfo   = info;
        Log.i(TAG, " sampleRateInHz=" + mInfo.getTimescale() + " Bitrate=" + mInfo.getBitrate() +
                " Channels=" + mInfo.getChannels() + " Duration=" + mInfo.getDuration());
        mBufferSize = AudioTrack.getMinBufferSize(mInfo.getTimescale(), AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        Log.i(TAG, "minBufferSize: " + mBufferSize);
        mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mInfo.getTimescale(), AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, mBufferSize * 2, AudioTrack.MODE_STREAM);
    }

    @Override
    public synchronized void start() {
        super.start();
        mPlayer = new AudioPlayThread();
        mPlayer.start();
        isStop = false;
    }

    public void play(int running) {
        if (mRunning == running)
            return ;

        if (isStop) {
            isStop = false;
            synchronized (this) {
                notify();
            }
            synchronized (mPlayer) {
                mPlayer.notify();
            }
        }
        mTrack.play();
        mRunning = running;
    }

    public void pause(int running) {
        if (mRunning == running)
            return ;

        mTrack.pause();
        mRunning = running;
    }

    public void stop(int running) {
        if (mRunning == running)
            return ;

        isStop = true;
        mTrack.stop();
        mAudioBuffers.clear();
        mRunning = running;
        mProgress = 1;
    }

    public void seek(int progress) {
        if (0 == progress) {
            mProgress = 1;
        } else {
            mProgress = progress * mInfo.getDuration() / 100;
        }
        mTrack.flush();
    }

    public void finish() {
        interrupt();
        mAudioBuffers.clear();
        mTrack.release();
    }


    @Override
    public void run() {
        int sampleCnt = 1; //mInfo.getDuration();
        int sampleSize = 0;
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        mTrack.play();

        while (!Thread.interrupted()) {
            try {
                    while (isStop) {
                        synchronized (this) {
                            wait();
                        }
                    }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            MP4AudioBuffer audioBuffer = new MP4AudioBuffer(4);
            sampleSize = mCodec.readAudioSampleData(audioBuffer.mAudioBuffer, mInfo.getTrackid(), mProgress, sampleCnt);
            if (sampleSize < (sampleCnt * 2)) {
                isStop = true;
            }
            audioBuffer.mSampleSize = sampleSize;
            synchronized(mAudioBuffers) {
                mAudioBuffers.push(audioBuffer);
            }
            mProgress += sampleCnt;
        }
        Log.i(TAG, "Audio Playing Stop");
    }

    private class AudioPlayThread extends Thread {

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                while (isStop) {
                    synchronized (this) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
               synchronized(mAudioBuffers) {
                    while ((true != mAudioBuffers.isEmpty())) {
                        MP4AudioBuffer audioBuffer = mAudioBuffers.pop();
                        mTrack.write(audioBuffer.mAudioBuffer, 0, audioBuffer.mSampleSize);
                    }
                }
            }
        }
    }
}
