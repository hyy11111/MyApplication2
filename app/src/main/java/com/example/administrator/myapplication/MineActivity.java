package com.example.administrator.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2018/6/6.
 */

public class MineActivity extends AppCompatActivity {
    Button home;
    Button upload;
    TextView name;
    TextView likeCount;
    TextView concern;
    TextView concerned;
    Button product;
    Button like;
    ListView videoListView;
    String type = "product";
    List<VideoDao> videoDaoList;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String result;
            switch (msg.what){
                case 0:
                    result =(String) msg.obj;
                    likeCount.setText(result);break;
                case 1:
                    result =(String) msg.obj;
                    concerned.setText(result);break;
                case 2:
                    result =(String) msg.obj;
                    concern.setText(result);break;
                case 3:
                    result =(String) msg.obj;
                    name.setText(result);break;
                case 4:
                    videoDaoList = (List<VideoDao>)msg.obj;
                    System.out.println(videoDaoList.size()+"000");
                    if(videoDaoList != null){
                        BaseAdapter adapter = new BaseAdapter() {
                            @Override
                            public int getCount() {
                                return videoDaoList.size();
                            }

                            @Override
                            public Object getItem(int position) {
                                return null;
                            }

                            @Override
                            public long getItemId(int position) {
                                return videoDaoList.get(position).getVideoId();
                            }

                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                System.out.println("yes");
                                LinearLayout linearLayout = new LinearLayout(MineActivity.this);
                                linearLayout.setOrientation(1);
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                lp.gravity = Gravity.CENTER;
                                TextView textView = new TextView(MineActivity.this);
                                textView.setLayoutParams(lp);
                                ImageView imageView = new ImageView(MineActivity.this);
                                try {
                                    File file = new File(Environment.getExternalStorageDirectory().getCanonicalFile()+"/"+ videoDaoList.get(position).getVideoName());
                                    file.createNewFile();
                                    FileOutputStream fos = new FileOutputStream(file);
                                    System.out.println(videoDaoList.get(position).getVideoFile());
                                    fos.write(videoDaoList.get(position).getVideoFile());
                                    fos.close();
                                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                                    mediaMetadataRetriever.setDataSource(file.getPath());
                                    Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime();
                                    imageView.setImageBitmap(bitmap);
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200,300);
                                    params.gravity = Gravity.CENTER;
                                    imageView.setLayoutParams(params);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if("product".equals(type)){
                                    textView.setText(videoDaoList.get(position).getVideoName());
                                    System.out.println(textView.getText());
                                }if("like".equals(type)){
                                    textView.setText("上传者："+videoDaoList.get(position).getUsername());
                                }
                                textView.setTextSize(18);
                                linearLayout.addView(imageView);
                                linearLayout.addView(textView);
                                return linearLayout;
                            }
                        };
                        videoListView.setAdapter(adapter);
                    }
                    videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(MineActivity.this,VideoPlay.class);
                            String path = null;
                            try {
                                path = Environment.getExternalStorageDirectory().getCanonicalFile()+"/"+ videoDaoList.get(position).getVideoName();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            intent.putExtra("videoPath",path);
                            startActivity(intent);
                        }
                    });
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mine);
        name = (TextView)findViewById(R.id.name);
        likeCount = (TextView)findViewById(R.id.liked);
        concern = (TextView)findViewById(R.id.concernCount);
        concerned = (TextView)findViewById(R.id.concernedCount);
        getMessage("getLikedCount");
        getMessage("getConcern");
        getMessage("getConcerned");
        getMessage("getName");
        getProductOrLike("getProduct");
        home = (Button)findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(MineActivity.this,HomeActivity.class);
                startActivity(intent);
            }
        });
        upload = (Button)findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MineActivity.this,UploadActivity.class);
                startActivity(intent);
            }
        });
        product = (Button)findViewById(R.id.production);
        product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "product";
                like.setTextColor(Color.BLACK);
                product.setTextColor(Color.BLUE);
                getProductOrLike("getProduct");
            }
        });
        like = (Button)findViewById(R.id.like);
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "like";
                like.setTextColor(Color.BLUE);
                product.setTextColor(Color.BLACK);
                getProductOrLike("getLiked");
            }
        });
        videoListView = (ListView)findViewById(R.id.videoListView);
    }

    private void getMessage(final String message){
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("http://192.168.43.245:8080/user/"+message)
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
                if("getLikedCount".equals(message)){
                    msg.what = 0;
                }if("getConcerned".equals(message)){
                    msg.what = 1;
                }if("getConcern".equals(message)){
                    msg.what = 2;
                }if("getName".equals(message)){
                    msg.what = 3;
                }
                msg.obj = responseStr;
                mHandler.sendMessage(msg);
            }
        });
    }

    private void getProductOrLike(String message){
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url("http://192.168.43.245:8080/user/"+message)
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
                System.out.println("video.size:"+videoDaoList.size());
                Message msg = mHandler.obtainMessage();
                msg.what = 4;
                msg.obj = videoDaoList;
                mHandler.sendMessage(msg);
            }
        });
    }
}
