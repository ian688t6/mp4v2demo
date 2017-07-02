package com.gc.mp4v2demo;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String ARG_FILENAME = "filename";

    private static final String mPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() +
                    File.separator + "FromMac";

    private static final String TAG = "MainActivity";

    private List<String> mMp4List;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ListView listView = (ListView) findViewById(R.id.mp4list);

        mMp4List = scanMP4Files();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_expandable_list_item_1);
        adapter.addAll(mMp4List);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(mListener);
    }

    private List<String> scanMP4Files() {
        List<String> mp4fileList = new ArrayList<>();

        Log.i(TAG, "path:" + mPath);
        File files = new File(mPath);
        File[] mp4files = files.listFiles();
        for (File mp4file : mp4files) {
            Log.i(TAG, mp4file.getName());
            mp4fileList.add(mp4file.getName());
        }

        return mp4fileList;
    }

    private OnItemClickListener mListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String filepath = mPath + File.separator + mMp4List.get(position);
//            mMp4Info = mMp4v2Codec.getVideoInfo();
//            Log.i(TAG, "file: " + mMp4List.get(position) + " Samples = " + String.valueOf(mMp4Info.getSamples()) +
//                    " Framerate = " + String.valueOf(mMp4Info.getFramerate()));

            Intent intent = new Intent();
            intent.setClass(MainActivity.this, MP4PlayActivity.class);
            intent.putExtra(ARG_FILENAME, filepath);
            startActivity(intent);
            /*
            Log.i(TAG, "SPS: ");
            printHexString(mMp4Info.getSpsHeader());

            Log.i(TAG, "PPS: ");
            printHexString(mMp4Info.getPpsHeader());
            */

        }
    };


}
