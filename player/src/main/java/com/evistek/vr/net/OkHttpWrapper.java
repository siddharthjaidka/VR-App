package com.evistek.vr.net;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by evis on 2016/9/8.
 */
public class OkHttpWrapper {
    private static final String TAG = "OkHttpWrapper";

    public static final MediaType MEDIA_TYPE_JSON
            = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpWrapper mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mHandler;
    private Gson mGson;

    private OkHttpWrapper() {
        mOkHttpClient = new OkHttpClient();
        mHandler = new Handler(Looper.getMainLooper());

        //The Dateformat of Gson is the same with the server.
        mGson = new GsonBuilder().setDateFormat("MMM dd, yyyy hh:mm:ss aa").create();
    }

    public static OkHttpWrapper getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpWrapper.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpWrapper();
                }
            }
        }

        return mInstance;
    }

    public void postAsync(String url, String json, final RequestCallBack requestCallBack) {
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, json);
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handleFailureResult(0, e.getMessage(), requestCallBack);
                Log.e(TAG, "onFailure: " + e.getMessage() + " method: " + call.request().method());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String bodyString = response.body().string();
                    if (requestCallBack.type == String.class) {
                        handleSuccessResult(bodyString, requestCallBack);
                    } else {
                        Object object = mGson.fromJson(bodyString, requestCallBack.type);
                        handleSuccessResult(object, requestCallBack);
                    }
                } else {
                    handleFailureResult(response.code(), response.message(), requestCallBack);
                    Log.e(TAG, "code: " + response.code() + " msg: " + response.message());
                }
            }
        });
    }

    public abstract static class RequestCallBack<T> {
        Type type;

        public RequestCallBack() {
            type = getSuperclassTypeParameter(getClass());
        }

        static Type getSuperclassTypeParameter(Class<?> subclass) {
            Type superclass = subclass.getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }

            Type[] types = ((ParameterizedType) superclass).getActualTypeArguments();
            return types[0];
        }

        public abstract void onResponse(T response);
        public abstract void onFailure(int code, String msg);
    }

    // run on the UI thread
    private void handleSuccessResult(final Object o, final RequestCallBack callBack) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onResponse(o);
                }
            }
        });
    }

    private void handleFailureResult(final int code, final String msg, final RequestCallBack callBack) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onFailure(code, msg);
                }
            }
        });
    }
}
