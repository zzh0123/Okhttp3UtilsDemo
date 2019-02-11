package com.okhttp3utils.demo;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Okhttp3Utils {

    /**
     * "application/x-www-form-urlencoded"，是默认的MIME内容编码类型，一般可以用于所有的情况，但是在传输比较大的二进制或者文本数据时效率低。
     这时候应该使用"multipart/form-data"。如上传文件或者二进制数据和非ASCII数据。
     */
    public static final MediaType MEDIA_TYPE_NORAML_FORM = MediaType.parse("application/x-www-form-urlencoded;charset=utf-8");

    //既可以提交普通键值对，也可以提交(多个)文件键值对。
    public static final MediaType MEDIA_TYPE_MULTIPART_FORM = MediaType.parse("multipart/form-data;charset=utf-8");

    //只能提交二进制，而且只能提交一个二进制，如果提交文件的话，只能提交一个文件,后台接收参数只能有一个，而且只能是流（或者字节数组）
    public static final MediaType MEDIA_TYPE_STREAM = MediaType.parse("application/octet-stream");

    public static final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain;charset=utf-8");

    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");

    private static String TAG = "Okhttp3Utils";
    private static Okhttp3Utils mOkhttp3Utils;
    private static OkHttpClient mOkHttpClient;
    public static Handler mDelivery;

    private Okhttp3Utils(){
        if (mOkHttpClient == null){
            mOkHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.SECONDS)//设置连接超时时间
                    .readTimeout(3, TimeUnit.SECONDS)//设置读取超时时间
                    .build();
        }
        mDelivery = new Handler(Looper.getMainLooper());
    }
    public static Okhttp3Utils getInstance(){
        if (mOkhttp3Utils == null){
            synchronized(Okhttp3Utils.class){
                if (mOkhttp3Utils == null){
                    mOkhttp3Utils = new Okhttp3Utils();
                }
            }
        }
        return mOkhttp3Utils;
    }

    /**
     * 封装方法
     * 1. 同步get
     * 2. 异步get
     * 3. 异步post
     * 4.
     * 5.
     */

    /**
     * 1.同步GET请求, 注意：同步调用会阻塞主线程,这边在子线程进行
     * @param url 请求的网络url
     * @return result 请求返回的结果
     */
    public static String getSycRequest(String url){
        String result = "";
        //1.创建OkHttpClient对象
        OkHttpClient okHttpClient = getInstance().mOkHttpClient;
        //2.创建Request对象，设置一个url地址（例如百度地址）,设置请求方式。
        Request request = new Request.Builder()
                .url(url)
                .method("GET",null)
                .build();
        //3.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //4.同步调用会阻塞主线程,这边在子线程进行
        try {
            //同步调用,返回Response,会抛出IO异常
            Response response = call.execute();
            if (response != null && response.code() == 200){
                result = response.body().string();
            }
            if (response == null){
                result = "同步get-result：服务器无响应！";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 2.异步GET请求
     * @param url
     * @param callback
     */
    public static void getAsycRequest(String url, final ResultCallback callback){
        //1.创建OkHttpClient对象
        OkHttpClient okHttpClient = getInstance().mOkHttpClient;
        //2.创建Request对象，设置一个url地址（例如百度地址）,设置请求方式。
        Request request = new Request.Builder().url(url)
                .method("GET",null)
                .build();
        //3.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //4.请求加入调度，重写回调方法
        call.enqueue(new Callback() {
            //请求失败执行的方法
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailedStringCallback(e, callback);
            }
            //请求成功执行的方法
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();
                sendSuccessResultCallback(string, callback);
            }
        });
    }

    /**
     * 3.异步post请求,提交json字符串
     * @param url
     * @param data
     * @param callback
     */
    public static void postAsycRequest_Json(String url, JSONObject data, final ResultCallback callback){
        //1.创建OkHttpClient对象
        OkHttpClient okHttpClient = getInstance().mOkHttpClient;
        //2.创建请求体
        RequestBody requestBody = build_postJsonString_RequestBody(data);
        //3.创建Request对象，设置URL地址，将RequestBody作为post方法的参数传入
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        //4.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //5.请求加入调度,重写回调方法
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailedStringCallback(e, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();
                sendSuccessResultCallback(string, callback);
            }
        });
    }

    /**
     * 4.异步post请求,提交字符串
     * @param url
     * @param str_params
     * @param callback
     */
    public static void postAsycRequest_String(String url, String str_params, final ResultCallback callback){
        //1.创建OkHttpClient对象
        OkHttpClient okHttpClient = getInstance().mOkHttpClient;
        //2.创建请求体
        RequestBody requestBody = build_postString_RequestBody(str_params);
        //3.创建Request对象，设置URL地址，将RequestBody作为post方法的参数传入
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        //4.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //5.请求加入调度,重写回调方法
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailedStringCallback(e, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();
                sendSuccessResultCallback(string, callback);
            }
        });
    }

    /**
     * 5.异步post请求,提交表单
     * @param url
     * @param data 传入的参数，与前端约定为json格式
     * @param callback
     */
    public static void postAsyRequest_Form(String url, JSONObject data, final ResultCallback callback){
        //1.创建OkHttpClient对象
        OkHttpClient okHttpClient = getInstance().mOkHttpClient;
        // post请求传参
        HashMap<String,String> paramsMap = null;
        try {
            paramsMap = new HashMap<>();
            Iterator<String> objs =  data.keys();
            String key;
            while (objs.hasNext()){
                key = objs.next();
                System.err.println("key: "+ key);
                String value = data.getString(key);
                System.err.println("value: "+value);
                paramsMap.put(key, value);
            }
            Log.i("----paramsMap.size--", "----paramsMap.size-----" + paramsMap.size());
        }catch (JSONException e){
            e.printStackTrace();
        }

        //2.通过new FormBody()调用build方法,创建一个RequestBody,可以用add添加键值对
        RequestBody requestBody = null;
        if (paramsMap != null && paramsMap.size() > 0){
            requestBody = build_postForm_RequestBody(paramsMap);
        }
        //3.创建Request对象，设置URL地址，将RequestBody作为post方法的参数传入
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        //4.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //5.请求加入调度,重写回调方法
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailedStringCallback(e, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();
                sendSuccessResultCallback(string, callback);
            }
        });
    }

    /**
     * 6.上传文件（单/多文件）
     * @param url
     * @param file_list
     * @param filekeys_list 表单name值
     * @param id
     * @param callback
     */
    public static void upLoadFile(String url, List<File> file_list,
                                  List<String> filekeys_list, String id, final ResultCallback callback){
        //1.创建OkHttpClient对象
        OkHttpClient  okHttpClient = getInstance().mOkHttpClient;
        //2.通过RequestBody.create 创建requestBody对象
        RequestBody requestBody = build_postMultiFile_RequestBody(file_list, filekeys_list);
        //3.创建Request对象，设置URL地址，将RequestBody作为post方法的参数传入
        Request request = new Request.Builder().url(url).post(requestBody).build();
        //4.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //5.请求加入调度,重写回调方法
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailedStringCallback(e, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();
                sendSuccessResultCallback(string, callback);
            }
        });
    }

    /**
     * 7.异步下载
     * @param url
     * @param fileName
     * @param callback
     */
    public static void downLoadFile(String url, final String fileName, final ResultCallback callback){
        //1.创建OkHttpClient对象
        OkHttpClient okHttpClient = getInstance().mOkHttpClient;
        //2.创建Request对象，设置一个url地址,设置请求方式。
        Request request = new Request.Builder().url(url)
                .get()
                .build();
        //3.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //4.请求加入调度,重写回调方法
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailedStringCallback(e, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //拿到字节流
                InputStream is = response.body().byteStream();
                int len = 0;
                //设置下载图片存储路径和名称，保存到本地存储卡中
                String path =  Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/ACNetworkTemp/";
//                FileUtil.delete(path);  // 删除上次的temp文件
                //将视频保存到本地存储卡中
                File file = new File(path);//文件路径, 注意文件路径要一级一级的建立
                if (!file.exists()) {
                    //folder /SoundRecorder doesn't exist, create the folder
                    file.mkdir();
                }
                Log.i("--down111111-----", "-down111111-----" );

                File file_temp = new File(file, fileName);
                String tempFilePath = path + fileName;
                FileOutputStream fos = new FileOutputStream(file_temp);
                byte[] buf = new byte[2048 * 1024]; // 2M
                while((len = is.read(buf))!= -1){
                    fos.write(buf,0,len);
                    Log.e(TAG , "onResponse: " + len );
                }
                fos.flush();
                fos.close();
                is.close();
                final String string = response.body().string();
                sendSuccessResultCallback(string, callback);
            }
        });
    }

    /******************************************* 辅助方法**********************************************/
    // 发送失败回调到主线程
    private static void sendFailedStringCallback(final Exception e, final ResultCallback callback)
    {
        mDelivery.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (callback != null)
                    callback.onFailure(e);
            }
        });
    }

    // 发送请求成功结果回调到主线程
    private static void sendSuccessResultCallback(final Object object, final ResultCallback callback)
    {
        mDelivery.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (callback != null)
                {
                    callback.onResponse(object);
                }
            }
        });
    }

    /**
     * 构建请求体
     */
    // get请求体
//    private static RequestBody build_get_RequestBody(){
//        RequestBody requestBody=RequestBody.create(MEDIA_TYPE_TEXT,str);
//    }

    // postJson字符串请求体
    private static RequestBody build_postJsonString_RequestBody(JSONObject body_params){
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, body_params.toString());
        return requestBody;
    }
    // post字符串请求体
    private static RequestBody build_postString_RequestBody(String body_params){
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_TEXT, body_params);
        return requestBody;
    }
    // post表单请求体,键值对
    private static RequestBody build_postForm_RequestBody(HashMap<String,String> paramsMap){
        RequestBody requestBody = null;
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            //追加表单信息
            builder.add(key, paramsMap.get(key));
        }
        requestBody = builder.build();
        return requestBody;
    }

    /**
     * post上传多文件请求体
     * @param file_list
     * @param filekeys_list 文件表单name值
     * @return
     */
    private static RequestBody build_postMultiFile_RequestBody(List<File> file_list, List<String> filekeys_list){
        MultipartBody.Builder builder = new MultipartBody.Builder();
        //设置类型是表单
        builder.setType(MEDIA_TYPE_MULTIPART_FORM);
        //添加数据
        if (file_list != null && file_list.size() > 0) {
            for (int i=0; i<file_list.size(); i++){
                //TODO 根据文件名设置contentType
                builder.addFormDataPart(filekeys_list.get(i) ,file_list.get(i).getName(),
                        RequestBody.create(MediaType.parse(guessMimeType(file_list.get(i).getName())), file_list.get(i)));
            }
        }
        RequestBody requestBody = builder.build();
        return requestBody;
    }

    private static String guessMimeType(String path)
    {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null)
        {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    public static void sendToast(final Context context, final String msg){
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
