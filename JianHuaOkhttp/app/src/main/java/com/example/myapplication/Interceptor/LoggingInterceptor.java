package com.example.myapplication.Interceptor;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class LoggingInterceptor implements Interceptor {

    private static final String TAG = "LoggingInterceptor";
    @Override
    public Response intercept(Chain chain) throws IOException {

        //获取到原来的Request，注意这是还没真正获取的服务端
        Request request = chain.request();

        //获取当前时间，并打印日志说要发起请求了
        //同时使用了前面我们也讲到了的headers打印请求头
        long t1 = System.nanoTime();

        //这里打印可以使用自己的日志框架
        Log.d(TAG, String.format("Sending request %s on %s%n%s",request.url(),chain.connection(),request.headers()));

        Response response = chain.proceed(request);
        long t2 = System.nanoTime();

        //还是根据是否是调试模式打印不同信息
        //如果是调试模式，可以打印更多的信息

        Log.d(TAG, String.format("Received response for %s in %.1fms status %d %n%s",
                response.request().url(),(t2 - t1) / 1e6d,response.code(),response.headers()));

        return response;
    }
}
