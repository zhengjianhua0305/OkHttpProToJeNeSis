package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.Interceptor.LoggingInterceptor;
import com.google.gson.Gson;
import com.google.gson.annotations.Until;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;
import okhttp3.internal.http2.Header;

public class MainActivity extends AppCompatActivity {


    private TextView tv_show;
    private ImageView iv_show;
    private OkHttpClient client;
    private static final String TAG = "TAG";

    //动态获取权限
    public static final int EXTERNAL_STORAGE_REQ_CODE = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_show = (TextView) findViewById(R.id.tv_show);
        iv_show = (ImageView) findViewById(R.id.iv_show);

        //
       /* client = new OkHttpClient.Builder()
                //实例化拦截器
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();*/

        client = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .build();

       /* client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new LoggingInterceptor())
                .build();*/

        //动态获取读写权限
        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    EXTERNAL_STORAGE_REQ_CODE);
        }
    }

    //同步Get按钮点击事件
    public void SyncGet(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("http://www.baidu.com")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    final String result = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_show.setText(result);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //异步按钮点击事件
    public void AsyncGet(View view) {
        final Request request = new Request.Builder().url("http://www.baidu.com").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                showThreadInfo(e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String result = response.body().string();
                    showThreadInfo(result);
                }
            }
        });
    }

    //配置单个按钮点击事件
    public void SetOnRequestOne(View view) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("http:/www.baidu.com")
                        .build();
                OkHttpClient copy = client.newBuilder()
                        .readTimeout(50, TimeUnit.SECONDS)
                        .build();
                try {
                    Response response = copy.newCall(request).execute();
                    final String result = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_show.setText(result);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //通过Handler更新UI
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            tv_show.setText(msg.obj.toString());
        }
    };

    public void TestHandler(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("http://www.baidu.com")
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        handler.obtainMessage(0, response.body().string()).sendToTarget();
                    }
                });
            }
        }).start();
    }

    //通过runOnUiThread方法更新UI
    public void TestRunOnUiThread(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("http://www.baidu.com")
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String result = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_show.setText(result);
                            }
                        });
                    }
                });
            }
        }).start();
    }

    //通过View.post方法更新UI
    public void TestViewPost(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("http://www.baidu.com")
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String result = response.body().string();
                        tv_show.post(new Runnable() {
                            @Override
                            public void run() {
                                tv_show.setText(result);
                            }
                        });
                    }
                });
            }
        }).start();
    }

    //https://3w.huanqiu.com/a/3458fa/9CaKrnQhYAn?agt=8
    public void TestGetDataByHashMap(View view) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("agt", "8");
        String url = formataParams("https://3w.huanqiu.com/a/3458fa/9CaKrnQhYAn?", params);
        final Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                showThreadInfo(result);
            }
        });
    }

    //定义url格式
    private String formataParams(String url, HashMap<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        for (Map.Entry<String, Object> p : params.entrySet()) {
            sb.append(p.getKey());
            sb.append("=");
            try {
                sb.append(URLEncoder.encode(p.getValue().toString(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
            sb.append("&");
        }
        return sb.toString();
    }

    //调用第三方库Gosn解析数据
    public void TestParseObjectForm(View view) {
        Request request = new Request.Builder()
                .url("https://api.github.com/users/zhengjianhua0305")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Gson gson = new Gson();
                User user = gson.fromJson(result, User.class);
                Log.i("---User", user.toString());
                showThreadInfo(user.toString());
            }
        });
    }


    //取消同步请求
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public void TestAutoCancelRequest(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("https://api.github.com/users/zhengjianhua0305")
                        .build();
                //创建纳米时间
                final long startNanos = System.nanoTime();
                final Call call = client.newCall(request);

                executor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, String.format("%.2f Canceling call.%n", (System.nanoTime() - startNanos) / 1e9f));
                        call.cancel();
                        Log.d(TAG, String.format("%.2f Canceled call.%n", (System.nanoTime() - startNanos) / 1e9f));
                    }
                }, 0, TimeUnit.SECONDS);
                Log.d(TAG, String.format("%.2f Executing call.%n", (System.nanoTime() - startNanos) / 1e9f));
                //执行这个请求
                try {
                    Response response = call.execute();
                    Log.d(TAG, String.format("%.2f Call was excepted to fail,but completed: %s%n", (System.nanoTime() - startNanos) / 1e9f, response));
                } catch (IOException e) {
                    //如果一个线程正在写请求获读响应，它应抛出IOException异常
                    Log.d(TAG, String.format("%.2f Call failed as excepted: %s%n", (System.nanoTime() - startNanos) / 1e9f, e));
                }

            }
        }).start();
    }

    //取消异步请求

    public void TestAutoCancleAsyncRequest(View view) {
        final Request request = new Request.Builder()
                .url("https://api.github.com/users/zhengjianhua0305")
                .build();
        //创建纳米时间
        final long startNanos = System.nanoTime();
        final Call call = client.newCall(request);
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, String.format("%.2f Canceling call.%n", (System.nanoTime() - startNanos) / 1e9f));
                call.cancel();
                Log.d(TAG, String.format("%.2f Canceled call.%n", (System.nanoTime() - startNanos) / 1e9f));
            }
        }, 0, TimeUnit.SECONDS);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "onResponse: " + response.body().string());
            }
        });
    }

    //自动下载文件到sdcard/
    public void TestDownloadFile(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("http://www.baidu.com")
                        .build();
                InputStream inputStream = null;
                FileOutputStream fileOutputStream = null;
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        inputStream = response.body().byteStream();

                        fileOutputStream = new FileOutputStream("/sdcard/baidu.html");

                        byte[] bytes = new byte[4096];
                        int len = -1;
                        while ((len = inputStream.read(bytes)) != -1) {
                            fileOutputStream.write(bytes, 0, len);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    //关闭输出流
                    Util.closeQuietly(fileOutputStream);
                    //关闭输入流
                    Util.closeQuietly(inputStream);
                }
            }
        }).start();
    }

    //https://dss1.bdstatic.com/5aAHeD3nKgcUp2HgoI7O1ygwehsv/media/ch1/jpg/%E4%B8%93%E5%AE%B6%E7%BB%84%E9%80%9A%E6%A0%8F%E5%8D%A1%E7%AA%84%E5%B1%8F-0.jpg
    //通过BitMap远程解码图片
    public void TestBitMapDecodeStream(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("https://dss1.bdstatic.com/5aAHeD3nKgcUp2HgoI7O1ygwehsv/media/ch1/jpg/%E4%B8%93%E5%AE%B6%E7%BB%84%E9%80%9A%E6%A0%8F%E5%8D%A1%E7%AA%84%E5%B1%8F-0.jpg")
                        .build();
                InputStream inputStream = null;
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        Log.d(TAG, "run: " + response);
                        inputStream = response.body().byteStream();

                        //直接使用inputStream解码图片
                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                iv_show.setImageBitmap(bitmap);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Util.closeQuietly(inputStream);
                }
            }
        }).start();
    }

    //使用日志拦截器
    public void TestUseLogInterceptor(View view) {
        final Request request = new Request.Builder()
                .url("http://www.baidu.com")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showThreadInfo(e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String result = response.body().string();
                    showThreadInfo(result);
                } else {
                    showThreadInfo("请求失败");
                }
            }
        });
    }

    //请求头
    public void TestHeaders(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Headers headers = new Headers.Builder()
                        .add("customName", "customValue")
                        .build();
                Request request = new Request.Builder()
                        .url("http://www.baidu.com")
                        .header("username", "pich")
                        .addHeader("password", "123456")
                        .headers(headers)
                        .removeHeader("custom")
                        .build();

                try {
                    Response response = client.newCall(request).execute();

                    //获取server请求头
                    String server = response.header("Server");
                    //获取Date，如果没有Date字段，就是用默认值
                    String Date = response.header("Data", "这就是默认值");
                    //获取相同的header的多个值(也就是key相同，有多个)
                    List<String> servers = response.headers("Server");

                    //获取所有header，包装到Headers类
                    Headers h = response.headers();

                    Log.d(TAG, "server" + server);
                    Log.d(TAG, "Date: " + Date);
                    Log.d(TAG, "header" + h.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void TestUseCache(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //缓存目录,/data/data/packagename/http/
                /* File file = new File(getCacheDir(), "http");*/
                File file = new File(Environment.getExternalStorageDirectory(), "http");
                //创建缓存,指定缓存为100M.
                Cache cache = new Cache(file, 1024 * 1024 * 100);

                OkHttpClient client = new OkHttpClient.Builder()
                        .cache(cache)
                        .addInterceptor(new LoggingInterceptor())
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.github.com/users/zhengjianhua0305")
                        .build();
                try {
                    //第一次请求
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        Log.d(TAG, "response: " + bodyString(response));

                        Log.d(TAG, "networkResponse: " + bodyString(response.networkResponse()));

                        Log.d(TAG, "cacheResponse: " + bodyString(response.cacheResponse()));
                    } else {
                        Log.d(TAG, "error1: " + response.code());
                    }
                    //第二次请求
                    response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        Log.d(TAG, "response: " + bodyString(response));

                        //缓存的response
                        //使用了缓存，返回null
                        Log.d(TAG, "networkResponse: " + bodyString(response.networkResponse()));

                        //如果没有缓存，或者不适用缓存，缓存过期返回null
                        Log.d(TAG, "cacheResponse: " + bodyString(response.cacheResponse()));
                    } else {
                        Log.d(TAG, "error2: " + response.code());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    //定义拦截器方法
    private static final Interceptor FORCE_CACHE_NETWORK_DATA_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                    .header("Cache-Control", "max-age-=60")//设置缓存时间为60s
                    .build();
        }
    };

    //使用拦截器支持缓存任意网页
    public void TestCacheAnyData(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //缓存目录
                File file = new File(getCacheDir(), "http");

                //创建缓存，指定缓存为100M
                Cache cache = new Cache(file, 1024 * 1024 * 100);

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .cache(cache)
                        .addInterceptor(new LoggingInterceptor())
                        .addNetworkInterceptor(FORCE_CACHE_NETWORK_DATA_INTERCEPTOR)
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.github.com/users/zhengjianhua0305")
                        .build();

                try {
                    //第一次请求
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        Log.d(TAG, "response: " + bodyString(response));

                        Log.d(TAG, "networkResponse: " + bodyString(response.networkResponse()));

                        Log.d(TAG, "cacheResponse: " + bodyString(response.cacheResponse()));
                    } else {
                        Log.d(TAG, "error1: " + response.code());
                    }
                    //第二次请求
                    response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        Log.d(TAG, "response: " + bodyString(response));

                        //缓存的response
                        //使用了缓存，返回null
                        Log.d(TAG, "networkResponse: " + bodyString(response.networkResponse()));

                        //如果没有缓存，或者不适用缓存，缓存过期返回null
                        Log.d(TAG, "cacheResponse: " + bodyString(response.cacheResponse()));
                    } else {
                        Log.d(TAG, "error2: " + response.code());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }


    //定义拦截器方法
    private static final Interceptor FINAL_CACHE_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                    .header("Cache-Control", "max-age-=60")//设置缓存时间为60s
                    .build();
        }
    };

    //缓存终极版
    public void TestFinalCache(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //客户端
                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .addInterceptor(FINAL_CACHE_INTERCEPTOR)
                        .addInterceptor(new LoggingInterceptor())
                        .addNetworkInterceptor(FINAL_CACHE_INTERCEPTOR)
                        .build();
                //请求
                Request request = new Request.Builder()
                        .url("https://api.github.com/users/zhengjianhua0305")
                        .build();

                try {
                    //发送请求
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {

                        //缓存的response
                        Log.d(TAG, "response: " + bodyString(response));

                        //使用了缓存，返回null
                        Log.d(TAG, "networkResponse: " + bodyString(response.networkResponse()));

                        //没有缓存，或者不适用缓存，缓存过期返回null
                        Log.d(TAG, "cacheResponse: " + bodyString(response.cacheResponse()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    //gzip压缩请求体
    private void TestCompressRequest(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    //定义response.body().string();方法节省了body().string();
    private String bodyString(Response response) {
        if (response != null && response.body() != null) {
            try {
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    //Ctrl+shit+T将共同的部分直接抽成一种方法
    private void showThreadInfo(final String sss) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_show.setText(sss);
            }
        });
    }
}
