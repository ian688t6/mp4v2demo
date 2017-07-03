package com.gc.mp4v2demo;


import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class MP4VideoPlayer implements MP4Player {

    private MediaCodec mDecoder;

    private MP4v2Codec mCodec;

    private MP4VideoInfo mInfo;

    private Surface mSurface;

    private MediaCodec.BufferInfo mBufferInfo;

    private ByteBuffer[] mInputBuffers;

    private int mRunning = MP4Player.STATE_STOP;

    private int mProgress = 1;

    private Timer mTimer;

    private final String TAG = "MP4Video";

    MP4VideoPlayer(MP4v2Codec codec, MP4VideoInfo info, Surface surface) {
        String mime = "Video/AVC";
        mCodec = codec;
        mInfo = info;
        mSurface = surface;
        try {
            mDecoder = MediaCodec.createDecoderByType(mime);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play(int running) {
        Log.i(TAG, "play Running " + String.valueOf(mRunning) + " " + String.valueOf(running));
        if (mRunning == running)
            return ;

        if (mRunning == MP4Player.STATE_STOP) {
            configVideoDecoder(mDecoder, mSurface, mInfo);
            mDecoder.start();
            mInputBuffers = mDecoder.getInputBuffers();
            mBufferInfo = new MediaCodec.BufferInfo();
        }

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                if (mProgress <= mInfo.getSamples()) {
                    message.what = MSG_DECODE;
                    message.arg1 = mProgress;
                } else {
                    message.what = MSG_DECODE_END;
                    message.arg1 = mProgress;
                    Log.i(TAG, "MSG_DECODE_END: " + String.valueOf(mProgress));
                }
                mHandler.sendMessage(message);
                mProgress ++;
            }
        }, 0, mInfo.getFramerate());
        mRunning = running;
    }

    public void pause(int running) {
        Log.i(TAG, "pause Running " + String.valueOf(mRunning));
        if (mRunning == running)
            return ;
        mTimer.cancel();
        mRunning = running;
    }

    public void stop(int running) {
        Log.i(TAG, "pause Running " + String.valueOf(mRunning));
        if (mRunning == running)
            return ;

        mDecoder.stop();
        mTimer.cancel();
        mRunning = running;
        mProgress = 1;
    }

    public void seek(int progress) {

        if (0 == progress) {
            mProgress = 1;
        } else {
            mProgress = progress * mInfo.getSamples() / 100;
            mDecoder.flush();
        }
    }

    public void finish() {
        mDecoder.release();
    }

    private void decodeEnd(int running) {
        int inIndex;
        int outIndex;

        if (running == mRunning) {
            return ;
        }

        inIndex = mDecoder.dequeueInputBuffer(10000);
        if (inIndex > 0) {
            mDecoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            outIndex = mDecoder.dequeueOutputBuffer(mBufferInfo, 10000);
            mDecoder.releaseOutputBuffer(outIndex, true);
        }
        mProgress = 1;
    }

    private int decode(int sampleId) {
        int outIndex;
        int inIndex;
        int sampleSize = 0;
        long sampleTime = 0;
        try {
            inIndex = mDecoder.dequeueInputBuffer(10000);
            if (inIndex > 0) {
                ByteBuffer inBuffer = mInputBuffers[inIndex];
                inBuffer.clear();
                sampleSize = mCodec.readVideoSampleData(inBuffer, mInfo.getTrackid(), sampleId);
                sampleTime = mCodec.getMP4SampleTime(mInfo.getTrackid(), sampleId);
                mListener.timestamp(sampleTime);
//                Log.i(TAG, "sampleTime : " + String.valueOf(sampleTime));
                mDecoder.queueInputBuffer(inIndex, 0, sampleSize, sampleTime, 0);
                outIndex = mDecoder.dequeueOutputBuffer(mBufferInfo, 10000);
                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                        break;

                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
                        break;

                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.d(TAG, "INFO_TRY_AGAIN_LATER");
                        break;

                    default:
                        try {
                            mDecoder.releaseOutputBuffer(outIndex, true);
                            mListener.progress(sampleId);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                            break;
                        }
                        break;
                }
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return sampleId;
    }

    private int configVideoDecoder(MediaCodec codec, Surface surface, MP4VideoInfo info) {
        String mime = "Video/AVC";

        MediaFormat mediaFormat = new MediaFormat();
        mediaFormat.setString(MediaFormat.KEY_MIME, mime);
        if (null == info) {
            Log.e(TAG, "can not find video info");
            return -1;
        }

        mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(info.getSpsHeader()));
        mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(info.getPpsHeader()));
        Log.i(TAG, "width=" + String.valueOf(info.getWidth()) + " height=" + String.valueOf(info.getHeight()));
        mediaFormat.setInteger(MediaFormat.KEY_WIDTH, info.getWidth());
        mediaFormat.setInteger(MediaFormat.KEY_HEIGHT, info.getHeight());
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, info.getFramerate());
        mDecoder.configure(mediaFormat, surface, null, 0);
        return 0;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DECODE:
                    decode(msg.arg1);
                    break;
                case MSG_DECODE_END:
                    decodeEnd(MP4Player.STATE_STOP);
                    mListener.progressEnd();
                    Log.i(TAG, "MSG_DECODE_END");
                    break;
            }
        }
    };

    private final int MSG_DECODE = 1;

    private final int MSG_DECODE_END = 2;

    public interface Listener {
        int progress(int sampleid);
        int progressEnd();
        void timestamp(long sampletime);
    }

    private Listener mListener;
    public void setListener(Listener listener) {
        mListener = listener;
    }
}
