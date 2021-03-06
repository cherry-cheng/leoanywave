package com.anywave.qpop.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anywave.qpop.App;
import com.anywave.qpop.R;
import com.anywave.qpop.event.WifiStateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WifiActivity extends Activity {

    @BindView(R.id.my)
    ImageButton my;
    @BindView(R.id.live)
    ImageButton live;
    @BindView(R.id.news)
    ImageButton news;
    @BindView(R.id.wifi)
    ImageButton wifi;
    @BindView(R.id.rl)
    RelativeLayout rl;
    @BindView(R.id.tv_connect)
    TextView tvConnect;

    private static final String TAG = "WifiActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        App.currentWifiActivity = this;
        // if (Util.isWifi(getConnectWifiSsid())){
        if (App.isWifi) {

            App.startActivity(this,HomeActivity.class);
            finish();
        }
        //  }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");
        App.isCurrentApp = true;

        /*if (App.isWifi) {
            App.startActivity(HomeActivity.class);
            finish();
        }*/

    }


    @Override
    protected void onPause() {
        super.onPause();

        App.isCurrentApp = false;

        Log.e(TAG, "onPause: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e(TAG, "onDestroy: ");

        App.isWifi = false;
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void WifiConnected(WifiStateEvent event) {
        App.isWifi = event.isWifiConnect();
        if (App.isWifi) {
            App.startActivity(this,HomeActivity.class);
            finish();
        }

    }


    @Override
    public void onBackPressed() {
        //        super.onBackPressed();
    }

    @OnClick(R.id.tv_connect)
    public void onViewClicked() {
        Intent intent = new Intent();
        intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
        startActivity(intent);
        //        finish();
    }

    public static String getConnectWifiSsid() {
        WifiManager wifiManager = (WifiManager) App.context.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.e("wifiInfo", wifiInfo.toString());
        Log.e("SSID", wifiInfo.getSSID());

        return wifiInfo.toString();
    }
}
