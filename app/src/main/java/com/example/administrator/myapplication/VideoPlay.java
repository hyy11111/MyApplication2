package com.example.administrator.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;

/**
 * Created by Administrator on 2018/6/11.
 */

public class VideoPlay extends AppCompatActivity {
    VideoView videoPlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video);
        Intent intent = getIntent();
        videoPlay = (VideoView)findViewById(R.id.playVideo);
        videoPlay.setVideoPath(intent.getStringExtra("videoPath"));
        videoPlay.requestFocus();
        videoPlay.start();
    }
}
