package com.okhttp3utils.demo;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends Activity implements View.OnClickListener{

    private Button bt_getSycRequest, bt_getAsycRequest, bt_postAsycRequest;
    private String url = "http://192.168.1.114:8080/PostTest_war_exploded/json/testRequestBody";

    private String getSyc_result, getAsyc_result, postAsyc_result;
    Handler mHandler;
    private TextView tv;
    OkHttpClient mOkHttpClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);

        mHandler = new Handler(Looper.getMainLooper());
        initView();
    }

    private void initView(){
        // 同步get请求
        bt_getSycRequest = (Button) findViewById(R.id.bt_getSycRequest);
        bt_getSycRequest.setOnClickListener(this);
        // 异步get请求
        bt_getAsycRequest = (Button) findViewById(R.id.bt_getAsycRequest);
        bt_getAsycRequest.setOnClickListener(this);
        // 异步post请求
        bt_postAsycRequest = (Button) findViewById(R.id.bt_postAsycRequest);
        bt_postAsycRequest.setOnClickListener(this);

        tv = (TextView) findViewById(R.id.tv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_getSycRequest: // 同步get请求
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getSyc_result =  Okhttp3Utils.getSycRequest(url);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "同步get-result：" + getSyc_result,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).start();

                break;
            case R.id.bt_getAsycRequest: // 异步get请求
                Okhttp3Utils.getAsycRequest(url, new ResultCallback() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(MainActivity.this, "异步get-result:" + "服务器无响应！",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Object response) {
                        if (response != null){
                            getAsyc_result = response.toString();
                            Toast.makeText(MainActivity.this, "异步get-result:" + getAsyc_result,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "异步get-result:" + "response为空",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

//                final Request request = new Request.Builder().url(url).build();
//                OkHttpClient okHttpClient = new OkHttpClient();
//                //
//                okHttpClient.newCall(request).enqueue(new Callback() {
//                    @Override
//                    public void onFailure(Call call, IOException e) {
//                        Log.e("zfq", "onFailure: "+e.getMessage());
//                    }
//
//                    @Override
//                    public void onResponse(Call call, Response response) throws IOException {
//                        final String body=response.body().string();
//                        String headers=response.headers().toString();
//                        mHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(MainActivity.this, "异步get-result:" + body,
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                        ;
//                        Log.i("zfq", "onResponse: ");
//                    }
//                });
                break;
            case R.id.bt_postAsycRequest: // 异步post请求
                String str_params = "";
                Okhttp3Utils.postAsycRequest_String(url, str_params, new ResultCallback() {
                    @Override
                    public void onResponse(Object response) {
                        if (response != null){
                            postAsyc_result = response.toString();
                            tv.setText("异步post-string-result:" + postAsyc_result);
                        } else {
                            Toast.makeText(MainActivity.this, "异步post-string-result:" + "response为空",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(MainActivity.this, "异步post-result:服务未响应！",
                                Toast.LENGTH_SHORT).show();
                    }
                });


//                OkHttpClientManager.postAsyn(url, new OkHttpClientManager.ResultCallback() {
//                    @Override
//                    public void onError(Request request, Exception e) {
//                        Toast.makeText(MainActivity.this, "异步post-result:服务未响应！",
//                                Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onResponse(Object response) {
//                        postAsyc_result = response.toString();
//                        tv.setText("异步post-string-result:" + postAsyc_result);
//                    }
//                });
                break;
        }
    }
}
