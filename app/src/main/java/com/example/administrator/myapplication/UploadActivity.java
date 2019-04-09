package com.example.administrator.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import com.alibaba.fastjson.JSON;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2018/6/6.
 */

public class UploadActivity extends AppCompatActivity {
    Button rec;
    Button upload1;
    Button play;
    Button home;
    Button mine;
    File videoFile;
    MediaRecorder mRecorder;
    private boolean isRecording = false;
    SurfaceView sView;
    VideoView videoView;
    int count;
    String path;
    Camera camera;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String result =(String) msg.obj;
            System.out.println(result);
//            if("0".equals(result)){
//                result = "上传成功！";
//            }else {
//                result = "上传失败！";
//            }
            Toast.makeText(UploadActivity.this,result,Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload);
        ActivityCompat.requestPermissions(UploadActivity.this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
        rec = (Button) findViewById(R.id.rec);
        upload1 = (Button) findViewById(R.id.upload1);
        play = (Button) findViewById(R.id.play);
        play.setEnabled(false);
        sView = (SurfaceView) findViewById(R.id.sView);
        sView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        sView.getHolder().setFixedSize(640, 480);
        sView.getHolder().setKeepScreenOn(true);
        home = (Button) findViewById(R.id.home);
        mine = (Button) findViewById(R.id.mine);
        videoView = (VideoView) findViewById(R.id.video);
        rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count % 2 == 0) {
                    sView.setVisibility(View.VISIBLE);
                    videoView.setVisibility(View.GONE);
                    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(UploadActivity.this, "SD卡不存在，无法录制", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        videoFile = new File(Environment.getExternalStorageDirectory().getCanonicalFile() + "/" + timeStamp + ".mp4");
                        path = videoFile.getAbsolutePath();
                        mRecorder = new MediaRecorder();
                        mRecorder.reset();
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                       // mRecorder.setOrientationHint(90);
                        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
                        mRecorder.setVideoSize(640, 480);
                        mRecorder.setVideoFrameRate(30);
                        mRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);
                        mRecorder.setOutputFile(videoFile.getAbsolutePath());
                        mRecorder.setPreviewDisplay(sView.getHolder().getSurface());
                        mRecorder.prepare();
                        mRecorder.start();
                        rec.setText("结束");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    rec.setText("录制");
                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;
                }
                count = count + 1;
                if (count % 2 != 0 || count == 0) {
                    play.setEnabled(false);
                    upload1.setEnabled(false);
                } else {
                    play.setEnabled(true);
                    upload1.setEnabled(true);
                }
            }
        });
        upload1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] buffer = null;
                try {
                    FileInputStream fis = new FileInputStream(new File(path));
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] b = new byte[1024];
                    int n;
                    while ((n = fis.read(b)) != -1) {
                        bos.write(b, 0, n);
                    }
                    fis.close();
                    bos.close();
                    buffer = bos.toByteArray();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                VideoDao videoDao = new VideoDao();
                videoDao.setVideoName(new File(path).getName());
                videoDao.setVideoFile(buffer);
                System.out.println(buffer + "123456");
                upload(videoDao);
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(count);
                sView.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                System.out.println("yes");
                videoView.setVideoPath(path);
                videoView.start();
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UploadActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
        mine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UploadActivity.this, MineActivity.class);
                startActivity(intent);
            }
        });
    }


    private void upload(VideoDao videoDao){
        String video = JSON.toJSONString(videoDao);
        OkHttpClient okHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("videoName",videoDao.getVideoName());
        builder.add("videoFile", Base64.encodeToString(videoDao.getVideoFile(),Base64.NO_WRAP));
        final Request request = new Request.Builder()
                .url("http://192.168.43.245:8080/video/add")
                .post(builder.build())
                .build();
        Call call = okHttpClient.newCall(request);
        System.out.println(videoFile.getAbsolutePath());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Toast.makeText(UploadActivity.this,"上传失败",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(Response response) throws IOException {
                String responseStr = response.body().string();
                Message msg = mHandler.obtainMessage();
                msg.obj = responseStr;
                mHandler.sendMessage(msg);
            }
        });
    }




}
