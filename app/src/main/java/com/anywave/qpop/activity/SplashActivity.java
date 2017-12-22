package com.anywave.qpop.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.anywave.qpop.App;
import com.anywave.qpop.R;
import com.anywave.qpop.WifiConnectionService2;
import com.anywave.qpop.event.WifiStateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SplashActivity extends Activity {


    // 要申请的权限
   private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_WIFI_STATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        EventBus.getDefault().register(this);

        Intent intent = new Intent(SplashActivity.this, WifiConnectionService2.class);
        App.context.startService(intent);
        //  System.out.println("leo startWifiConnectionService");
        // final WifiAdmin wifiAdmin = new WifiAdmin(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 321);
            }
        }


        Observable.timer(4, TimeUnit.SECONDS).
                subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (App.isWifi) {
                            App.startActivity(SplashActivity.this,HomeActivity.class);
                        } else {
                            App.startActivity(SplashActivity.this,WifiActivity.class);
                        }

                        SplashActivity.this.finish();

                    }
                });



    /*    new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (Util.isWifi(wifiAdmin.getConnectWifiSSID(SplashActivity.this))) {
                        App.startActivity(HomeActivity.class);
                        return;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();*/

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void WifiConnected(WifiStateEvent event) {

        App.isWifi = event.isWifiConnect();

    }


    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
