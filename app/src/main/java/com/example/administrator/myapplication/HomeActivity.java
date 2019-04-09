package com.example.administrator.myapplication;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/6/6.
 */

public class HomeActivity extends AppCompatActivity {
    Button recommend;
    Button concerned;
    Button upload;
    Button mine;
    VideoView videoView;
    ImageView love;
    TextView addConcern;
    long videoId;
    long userId;
    List<VideoDao> videoDaoList;
    int m = 0;
    private Handler mHandler = new Handler(){
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0 ){
                videoDaoList = (List<VideoDao>)msg.obj;
                System.out.println(videoDaoList.size());
                if(videoDaoList.size()!=0){
                    play(0);
                }else {
                    Toast.makeText(HomeActivity.this,"无相关视频",Toast.LENGTH_SHORT).show();
                }

            }if(msg.what == 1){
                String result = (String)msg.obj;
                if("点赞成功".equals(result)){
                    love.getDrawable().setTint(Color.RED);
                }
            }if(msg.what == 2){
                Toast.makeText(HomeActivity.this,(String)msg.obj,Toast.LENGTH_SHORT).show();
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        videoView= (VideoView) findViewById(R.id.mVideoView);
        videoView.setOnTouchListener(new View.OnTouchListener() {
            float y1,y2;
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    y1 = event.getY();
                }if(event.getAction() == MotionEvent.ACTION_UP){
                    y2 = event.getY();
                    if(videoDaoList.size()>1){
                        if(y1-y2>50){
                            if(m<videoDaoList.size()-1){
                                m = m + 1;
                                play(m);
                            }
                            else{
                                Toast.makeText(HomeActivity.this,"没有更多视频",Toast.LENGTH_SHORT).show();
                            }
                        }if(y2-y1>50){
                            if(m>0){
                                m = m - 1;
                                play(m);
                            }
                            else{
                                Toast.makeText(HomeActivity.this,"这是最新的视频",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                return true;
            }
        });
        videoView.setLongClickable(true);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onPrepared(MediaPlayer mp)
            {
                mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            }
        });
        recommend = (Button)findViewById(R.id.recommend);
        recommend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                recommendOrConcerned("recommend");
            }
        });
        concerned = (Button)findViewById(R.id.concerned);
        concerned.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                recommendOrConcerned("concerned");
            }
        });
        upload = (Button)findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(HomeActivity.this,UploadActivity.class);
                startActivity(intent);
                finish();
            }
        });
        mine = (Button)findViewById(R.id.mine);
        mine.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(HomeActivity.this,MineActivity.class);
                startActivity(intent);
                finish();
            }
        });
        recommendOrConcerned("recommend");
        love = (ImageView)findViewById(R.id.love);
        love.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v){
                if(videoId != 0){
                    liked(videoId);
                }
            }
        });
        addConcern = (TextView)findViewById(R.id.addConcern);
        addConcern.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(userId != 0){
                    concern(userId);
                }
            }
        });
    }
    private void recommendOrConcerned(String type){
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("http://192.168.43.245:8080/video/"+type)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }
            @Override
            public void onResponse(Response response) throws IOException {
                String responseStr = response.body().string();
                List<VideoDao> videoDaoList = com.alibaba.fastjson.JSONArray.parseArray(responseStr,VideoDao.class);
                Message msg = mHandler.obtainMessage();
                msg.what = 0;
                msg.obj = videoDaoList;
                mHandler.sendMessage(msg);
            }
        });
    }
    private void liked(long videoId){
        OkHttpClient okHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("videoId", String.valueOf(videoId));
        final Request request = new Request.Builder()
                .url("http://192.168.43.245:8080/user/liked")
                .post(builder.build())
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }
            @Override
            public void onResponse(Response response) throws IOException {
                String responseStr = response.body().string();
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = responseStr;
                mHandler.sendMessage(msg);
            }
        });
    }
    private void concern(long userId){
        OkHttpClient okHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("userId",String.valueOf(userId));
        final Request request = new Request.Builder()
                .url("http://192.168.43.245:8080/user/concerned")
                .post(builder.build())
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }
            @Override
            public void onResponse(Response response) throws IOException {
                String responseStr = response.body().string();
                Message msg = mHandler.obtainMessage();
                msg.what = 2;
                msg.obj = responseStr;
                mHandler.sendMessage(msg);
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void play(int n){
        videoId = videoDaoList.get(n).getVideoId();
        System.out.println(videoId);

        userId = videoDaoList.get(n).getUserId();
        System.out.println(userId);
        if(videoDaoList.get(n).getLiked() == 0){
            love.getDrawable().setTint(Color.RED);
        }
        try {
            File file = new File(Environment.getExternalStorageDirectory().getCanonicalFile()+"/"+ videoDaoList.get(n).getVideoName());
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            System.out.println(videoDaoList.get(n).getVideoFile());
            fos.write(videoDaoList.get(n).getVideoFile());
            fos.close();
            videoView.setVideoPath(file.getPath());
            videoView.requestFocus();
            videoView.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
