package com.gc.mp4v2demo;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String ARG_FILENAME = "filename";

    private static final String TAG = "MainActivity";

    private List<String> mMp4List;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ListView listView = (ListView) findViewById(R.id.mp4list);

        MP4FileManager fileManager = new MP4FileManager();
        mMp4List = fileManager.getFileList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_expandable_list_item_1);
        adapter.addAll(fileManager.getFiles());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(mListener);
    }


    private OnItemClickListener mListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent intent = new Intent();
            intent.setClass(MainActivity.this, MP4PlayActivity.class);
            intent.putExtra(ARG_FILENAME, mMp4List.get(position));
            startActivity(intent);

        }
    };


}
