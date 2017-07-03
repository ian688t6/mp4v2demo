package com.gc.mp4v2demo;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MP4FileManager {
    private final String TAG = "MP4FileManager";

    private int mFileId = 0;

    private static final String mPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() +
                    File.separator + "FromMac";

    List<String> mFileList = new ArrayList<>();
    List<String> mFiles = new ArrayList<>();


    MP4FileManager() {
        scanMP4Files();
    }

    private void scanMP4Files() {
        Log.i(TAG, "path:" + mPath);
        File files = new File(mPath);
        File[] mp4files = files.listFiles();
        for (File mp4file : mp4files) {
            Log.i(TAG, mp4file.getName());
            mFileList.add(mPath + File.separator + mp4file.getName());
            mFiles.add(mp4file.getName());
        }
    }

    public List<String> getFileList() {
        return mFileList;
    }

    public List<String> getFiles() {
        return mFiles;
    }

    public void setCur(String filename) {

        for (int i = 0; i < mFileList.size(); i ++) {
            String file = mFileList.get(i);
            if (file.contentEquals(filename)) {
                mFileId = i;
                return ;
            }
        }
        mFileId = 0;
        return ;
    }

    public String getNext() {

        if (mFileList.isEmpty()) {
            return null;
        }

        ++ mFileId;
        if (mFileId == mFileList.size()) {
            mFileId = 0;
        }

        return mFileList.get(mFileId);
    }

    public String getPrev() {

        if (mFileList.isEmpty()) {
            return null;
        }

        -- mFileId;
        if (mFileId < 0) {
            mFileId = (mFileList.size() - 1);
        }
        Log.i(TAG, "1 getPrev " + String.valueOf(mFileId) + " File " + mFileList.get(mFileId));
        return mFileList.get(mFileId);
    }

    public void setListener(Context context) {
        Log.i(TAG, "setListener");
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
//        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
//        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
//        intentFilter.addDataScheme("file");
//        MP4UsbManager usbReceiver = new MP4UsbManager();
//        context.registerReceiver(usbReceiver, intentFilter);
    }
}
