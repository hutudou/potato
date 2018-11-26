package com.example.administrator.potato.application;

import android.app.Application;
import android.content.Context;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.MemoryCookieStore;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.squareup.leakcanary.LeakCanary;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;


public class MyApplication extends Application {
    private static Context mContext;

    //重写父类方法 获取ApplicationContext
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        LeakCanary.install(this);
        initOkgo();
    }

    //得到Application的context
    public static Context getContext() {
        return mContext;
    }

    //初始化OKgo框架
    private void initOkgo() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        /**
         * 配置自定义log
         */
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("Potato");
        //设置log打印级别
        /* NONE,       //不打印log
           BASIC,      //只打印 请求首行 和 响应首行
           HEADERS,    //打印请求和响应的所有 Header
           BODY        //所有数据全部打印*/
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
        //设置log颜色
        loggingInterceptor.setColorLevel(Level.SEVERE);
        //添加拦截器
        builder.addInterceptor(loggingInterceptor);
        /**
         * 配置响应时间
         */
        //全局的读取超时时间
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        //全局的写入超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        //全局的连接超时时间
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        /**
         * 配置cookie 任选其一
         */
       /* //使用sp保持cookie，如果cookie不过期，则一直有效
        builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));
        //使用数据库保持cookie，如果cookie不过期，则一直有效
        builder.cookieJar(new CookieJarImpl(new DBCookieStore(this)));*/
        //使用内存保持cookie，app退出后，cookie消失
        builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));
        /**
         * 配置https
         */
        //方法一：信任所有证书,不安全有风险
        HttpsUtils.SSLParams sslParams1 = HttpsUtils.getSslSocketFactory();
        builder.sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager);
        /**
         * 配置Okgo
         */
        OkGo.getInstance().init(this)                           //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置将使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3);                              //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0

    }
}