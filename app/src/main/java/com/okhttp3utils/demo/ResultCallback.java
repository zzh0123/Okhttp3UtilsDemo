package com.okhttp3utils.demo;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * ResultCallback可定义为泛型类
 */
public abstract class ResultCallback<T>{
    public abstract void onResponse(T response);
    public abstract void onFailure(Exception e);
}
