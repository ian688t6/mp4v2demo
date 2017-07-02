package com.gc.mp4v2demo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MP4PlayActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private final String TAG = "MP4PlayActivity";

    private final String ARG_FILENAME = "filename";

    private MP4v2Codec mMP4Codec;

    private MP4Manager mManager;

    private MP4AudioInfo mAudioInfo;

    private MP4VideoInfo mVideoInfo;

    private boolean mBound = false;

    private SeekBar mSeekBar;

    private ImageView mImageView;

    private TextView mTimeText;

    private MP4FileManager mFileManager;

    private SurfaceView mSurfaceView;

    private String mFilename;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.mp4actvity);

        mSurfaceView    = (SurfaceView) findViewById(R.id.mp4_surface);
        mSeekBar        = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mTimeText = (TextView) findViewById(R.id.time_text);

        mImageView      = (ImageView) findViewById(R.id.imgview_play);
        Intent intent = getIntent();
        mMP4Codec = new MP4v2Codec();
        mFilename = intent.getStringExtra(ARG_FILENAME);

        mFileManager = new MP4FileManager();
        mFileManager.setCur(mFilename);
        mMP4Codec.openSource(mFilename);
        mVideoInfo = mMP4Codec.getVideoInfo();
        mAudioInfo = mMP4Codec.getAudioInfo();
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        bindMP4Manager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        if (mBound) {
            mMP4Codec.closeSource();
            unbindMP4Manager();
            mBound = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (MP4Player.STATE_PLAY == mManager.getPlaystate()) {
            mManager.setPlaystate(MP4Player.STATE_PAUSE);
        }
    }


    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            MP4Manager.BinderService binder = (MP4Manager.BinderService)service;
            mManager = binder.getService();
            mManager.setCallback(mCallback);
            mManager.configure(mMP4Codec, mVideoInfo, mAudioInfo);
            mBound = true;

            ViewGroup.LayoutParams layoutParams = mSurfaceView.getLayoutParams();
            layoutParams.width = 1280;
            layoutParams.height = 720;
            mSurfaceView.getHolder().addCallback(mManager);
            mSurfaceView.setLayoutParams(layoutParams);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    private void bindMP4Manager() {
        Intent intent = new Intent(this, MP4Manager.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindMP4Manager() {
        unbindService(mConnection);
    }

    private static final int MSG_PROGRESS = 1;

    private static final int MSG_STATE_PLAY = 2;

    private static final int MSG_STATE_PAUSE = 3;

    private static final int MSG_STATE_STOP = 4;

    private static final int MSG_STATE_PREV = 5;

    private static final int MSG_STATE_NEXT = 6;

    private static final int MSG_TIMESTAMP = 7;

    private MP4Manager.ICallBack mCallback = new MP4Manager.ICallBack() {
        int progressStep = 0;
        String timeText = "00:00:00";

        @Override
        public void progress(int step) {
            if (progressStep != step) {
//                Log.i(TAG, "progress: " + String.valueOf(step));
                mHandler.sendMessage(mHandler.obtainMessage(MSG_PROGRESS, step, 0));
                progressStep = step;
            }
        }

        @Override
        public void state(int state) {
            switch (state) {
                case MP4Player.STATE_PLAY:
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_STATE_PLAY, state, 0));
                    break;

                case MP4Player.STATE_STOP:
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_STATE_STOP, state, 0));
                    break;

                default:
                    break;
            }
        }

        @Override
        public void timestamp(String time) {
            if (!timeText.contentEquals(time)) {
                mHandler.sendMessage(mHandler.obtainMessage(MSG_TIMESTAMP, time));
                timeText = time;
            }
        }

    };

    private void switching() {
        mManager.destroy();
        mMP4Codec.closeSource();
        mMP4Codec.openSource(mFilename);
        mVideoInfo = mMP4Codec.getVideoInfo();
        mAudioInfo = mMP4Codec.getAudioInfo();
        mManager.configure(mMP4Codec, mVideoInfo, mAudioInfo);
        mSurfaceView.setVisibility(View.GONE);
        mSurfaceView.setVisibility(View.VISIBLE);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PROGRESS:
                    mSeekBar.setProgress(msg.arg1);
                    break;

                case MSG_STATE_PLAY:
                    mImageView.setImageResource(R.drawable.pause);
                    mManager.setPlaystate(MP4Player.STATE_PLAY);
                    break;

                case MSG_STATE_PAUSE:
                case MSG_STATE_STOP:
                    mImageView.setImageResource(R.drawable.play);
                    mTimeText.setText("00:00:00");
                    mManager.setPlaystate(MP4Player.STATE_PAUSE);
                    break;

                case MSG_STATE_PREV:
                case MSG_STATE_NEXT:
                    switching();
                    break;

                case MSG_TIMESTAMP:
                    String time = (String)msg.obj;
                    mTimeText.setText(time);
                    break;

                default:

                    break;
            }
        }
    };

    public void onPlay(View view) {

        int state = mManager.getPlaystate();
        Log.i(TAG, "onPlay" + " state: " + String.valueOf(state));
        switch (state) {
            case MP4Player.STATE_PAUSE:
            case MP4Player.STATE_STOP:
                mHandler.sendMessage(mHandler.obtainMessage(MSG_STATE_PLAY, state, 0));
                break;

            case MP4Player.STATE_PLAY:
                mHandler.sendMessage(mHandler.obtainMessage(MSG_STATE_PAUSE, state, 0));
                break;
        }

    }

    public void onPrev(View view) {
        mFilename = mFileManager.getPrev();
        Log.i(TAG, "onPrev" + "file: " + mFilename);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_STATE_PREV, 0, 0));
    }

    public void onNext(View view) {
        mFilename = mFileManager.getNext();
        Log.i(TAG, "onNext" + "file: " + mFilename);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_STATE_NEXT, 0, 0));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mManager.seek(mSeekBar.getProgress());
    }
}
