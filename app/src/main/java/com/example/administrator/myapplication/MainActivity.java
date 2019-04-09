package com.example.administrator.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;


import static android.R.id.message;

public class MainActivity extends AppCompatActivity{
    EditText name;
    EditText password;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String result =(String) msg.obj;
            Toast.makeText(MainActivity.this,result,Toast.LENGTH_SHORT).show();
            String success1 = "注册成功！";
            String success2 = "登录成功！";
            if(result.equals(success1)|| result.equals(success2) ){
                Intent intent = new Intent(MainActivity.this,HomeActivity.class);
                startActivity(intent);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        name =(EditText)findViewById(R.id.name);
        password = (EditText) findViewById(R.id.password);
        Button regist = (Button)findViewById(R.id.regist);
        final Button login = (Button)findViewById(R.id.login);
        regist.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                loginOrRegist(name.getText().toString(),password.getText().toString(),"regist");
            }
        });

        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                loginOrRegist(name.getText().toString(),password.getText().toString(),"login");
            }
        });

    }


    private void loginOrRegist(String name,String password,String type){
        OkHttpClient okHttpClient = new OkHttpClient();
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("name",name);
        builder.add("password",password);
        final Request request = new Request.Builder()
                .url("http://192.168.43.245:8080/user/"+type)
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
                msg.obj = responseStr;
                mHandler.sendMessage(msg);
            }
        });
    }
}

