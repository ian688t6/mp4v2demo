package com.gc.mp4v2demo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;


public class MP4Manager extends Service implements SurfaceHolder.Callback, MP4Player {

    private MP4v2Codec mCodec;

    private MP4VideoInfo mVideoInfo;

    private MP4VideoPlayer mVideoPlayer;

    private MP4AudioInfo mAudioInfo;

    private int     mRunning = MP4Player.STATE_STOP;

    private MP4AudioPlayer mAudioPlayer = null;

    private final String TAG = "MP4Manager";

    private final BinderService mBinderService = new BinderService();

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged");

        holder.setFixedSize(1280, 720);
        if (mVideoPlayer == null) {
            mVideoPlayer = new MP4VideoPlayer(mCodec, mVideoInfo, holder.getSurface());
            mVideoPlayer.setListener(mVideoListener);
        }

        if (mAudioPlayer == null) {
            mAudioPlayer = new MP4AudioPlayer(mCodec, mAudioInfo);
            mAudioPlayer.start();
        }
        mCallback.state(MP4Player.STATE_PLAY);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
    }

    public class BinderService extends Binder {
        MP4Manager getService() {
            return MP4Manager.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinderService;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public void configure(MP4v2Codec codec, MP4VideoInfo videoInfo, MP4AudioInfo audioInfo) {
        Log.i(TAG, "configure");

        mCodec = codec;
        mVideoInfo = videoInfo;
        mAudioInfo = audioInfo;
    }

    public void play(int running) {
        Log.i(TAG, "play");
        if ((MP4Player.STATE_PAUSE == mRunning) || (MP4Player.STATE_STOP == mRunning)) {
            Log.i(TAG, "play running: " + String.valueOf(mRunning));
            mRunning = MP4Player.STATE_PLAY;
            mCallback.state(mRunning);
            mAudioPlayer.play(mRunning);
            mVideoPlayer.play(mRunning);
        }
    }

    public void pause(int running) {

        if (MP4Player.STATE_STOP == mRunning) {
            return ;
        }

        if ((MP4Player.STATE_PLAY == mRunning)) {
            Log.i(TAG, "pause running: " + String.valueOf(mRunning));
            mRunning = MP4Player.STATE_PAUSE;
            mAudioPlayer.pause(mRunning);
            mVideoPlayer.pause(mRunning);
            mCallback.state(mRunning);
        }
    }

    public void stop(int running) {
        if ((MP4Player.STATE_PLAY == mRunning) || (MP4Player.STATE_PAUSE == mRunning)) {
            Log.i(TAG, "stop running: " + String.valueOf(mRunning));
            mRunning = running;
            mAudioPlayer.stop(mRunning);
            mVideoPlayer.stop(mRunning);
            mVideoPlayer.seek(0);
            mAudioPlayer.seek(0);
            mCallback.progress(0);
            mCallback.state(mRunning);
        }
    }

    public void seek(int progress) {
        pause(MP4Player.STATE_PAUSE);
        mVideoPlayer.seek(progress);
        mAudioPlayer.seek(progress);
        play(MP4Player.STATE_PLAY);
    }

    public void destroy() {
        stop(MP4Player.STATE_STOP);
        mVideoPlayer.finish();
        mAudioPlayer.finish();
        mVideoPlayer = null;
        mAudioPlayer = null;
    }

    public int getPlaystate() {
        return mRunning;
    }

    public void setPlaystate(int state) {
        switch (state) {
            case MP4Player.STATE_PLAY:
                play(MP4Player.STATE_PLAY);
                break;

            case MP4Player.STATE_PAUSE:
                pause(MP4Player.STATE_PAUSE);
                break;

            case MP4Player.STATE_STOP:
                stop(MP4Player.STATE_STOP);
                break;
        }
    }

    private MP4VideoPlayer.Listener mVideoListener = new MP4VideoPlayer.Listener() {

        @Override
        public int progress(int sampleid) {
            int totalSample = mVideoInfo.getSamples();
            double step = (((double)sampleid / (double)totalSample) * 100);
            mCallback.progress((int)step);
            return 0;
        }

        @Override
        public int progressEnd() {
            stop(MP4Player.STATE_STOP);
            mVideoPlayer.seek(0);
            mAudioPlayer.seek(0);
            mCallback.progress(0);
            return 0;
        }

        @Override
        public void timestamp(long sampletime) {
            int ss = 1000 * 1000;
            int mi = ss * 60;
            int hh = mi * 60;
            int dd = hh * 24;

            long day = sampletime / dd;
            long hour = (sampletime - day * dd) / hh;
            long minute = (sampletime - day * dd - hour * hh) / mi;
            long second = (sampletime - day * dd - hour * hh - minute * mi) / ss;

            String strHour = hour < 10 ? "0" + hour : "" + hour;//小时
            String strMinute = minute < 10 ? "0" + minute : "" + minute;//分钟
            String strSecond = second < 10 ? "0" + second : "" + second;//秒

            String strTimestamp = strHour + ":" + strMinute + ":" + strSecond;
            mCallback.timestamp(strTimestamp);
        }
    };

    public interface ICallBack {
        void progress(int step);
        void state(int state);
        void timestamp(String time);
    }

    private ICallBack mCallback;
    public void setCallback(ICallBack listener) {
        mCallback = listener;
    }
}
