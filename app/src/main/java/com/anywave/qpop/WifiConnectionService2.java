package com.anywave.qpop;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.anywave.qpop.event.OnOff;
import com.anywave.qpop.event.WifiStateEvent;
import com.anywave.qpop.utils.Util;
import com.gxw.wificonnhelperlib.utils.WifiAdmin;
import com.gxw.wificonnhelperlib.utils.WifiConnListener;
import com.gxw.wificonnhelperlib.utils.WifiConnector;
import com.gxw.wificonnhelperlib.utils.bean.WifiBeanConn;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class WifiConnectionService2 extends Service implements WifiConnListener {

    public static final int WAIT_FOR_SCAN_RESULT = 10 * 1000; //10 seconds
    public static final int WIFI_SCAN_TIMEOUT = 20 * 1000;

    private WifiAdmin mWifiAdmin;

    private WifiConnector mWifiConnector;
    private WifiStateReceiver mWifiStateReceiver;


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    ScanWifi();
                    break;
                case 2:
                    if (!mWifiConnector.CanReConnect()) {
                        ScanWifi();
                    }
                    break;

                case 3:

                    mWifiAdmin.openWifi();
                    ScanWifi();

                    break;

                case 4:

                    if (mWifiAdmin.getCurrentWifiState() > 1) {
                        //说明wifi重新链接了 扫描wifi
                        ScanWifi();

                    } else {
                        //轮询查询wifi是否连接
                        sendEmptyMessageDelayed(4, 2000);
                    }

                    break;

            }

        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        mWifiAdmin = new WifiAdmin(this);

        registerWifiStateReceiver();

        mWifiAdmin.openWifi();
        // }

        //wifiBeens = new ArrayList<>();
        // wifiBeensTemp = new ArrayList<>();

       /* buildWifiBean();

        wifiBeens.addAll(wifiBeensTemp);*/

        mWifiConnector = new WifiConnector(this);

    }


    private void registerWifiStateReceiver() {
        if (mWifiStateReceiver == null) {
            System.out.println("leo 注册监听");
            mWifiStateReceiver = new WifiStateReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            registerReceiver(mWifiStateReceiver, intentFilter);
        }
    }

    private void unregisterWifiStateReceiver() {
        if (mWifiStateReceiver != null) {
            unregisterReceiver(mWifiStateReceiver);
            mWifiStateReceiver = null;
        }
    }


    private class WifiStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
            //;
            //  System.out.println("leo WifiStateReceiver onReceive : " + intent.getDataString());

            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {

                int authState = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                if (authState == WifiManager.ERROR_AUTHENTICATING) {//身份验证错误到这儿
                    //提示密码错误
                    System.out.println("leo1 WifiStateReceiver : 密码错误");
                    // reason = 1;
                }
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {


                    switch (mWifiAdmin.checkState()) {
                        case WifiManager.WIFI_STATE_DISABLING:
                        case WifiManager.WIFI_STATE_DISABLED:

                            System.out.println("leo1 手动关闭了wifi");
                            EventBus.getDefault().post(WifiStateEvent.getInstance(false));
                            mHandler.removeCallbacksAndMessages(null);
                            mHandler.sendEmptyMessageDelayed(4, 1000);


                            break;

                        case WifiManager.WIFI_STATE_UNKNOWN:

                            System.out.println("leo WIFI_STATE_UNKNOWN");

                            break;


                        case WifiManager.WIFI_STATE_ENABLED:
                        case WifiManager.WIFI_STATE_ENABLING:

                            System.out.println("leo WIFI_STATE_ENABLED : " + info.getExtraInfo());
                            if (!Util.isWifi(info.getExtraInfo())) {
                                mWifiAdmin.dissconnectAll();
                                mHandler.sendEmptyMessage(1);
                            }


                            break;


                        default:
                            System.out.println("leo WifiStateReceiver DISCONNECTED : " + info.getExtraInfo());
                           // mHandler.sendEmptyMessage(1);
                            break;
                    }

             /*       if (i == WifiManager.WIFI_STATE_DISABLING || i == WifiManager.WIFI_STATE_DISABLED || i == WifiManager.WIFI_STATE_UNKNOWN) {

                    } else {
                        //mWifiAdmin.dissconnectAll();

                    }*/
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    //获取当前wifi名称

                    WifiInfo info2 = mWifiAdmin.getConnectWifi(WifiConnectionService2.this);

                    if (info2 != null && info2.getSupplicantState() == SupplicantState.COMPLETED) {//这儿可能报空指针
                        if (Util.isWifi(info2.getSSID())) {
                            EventBus.getDefault().post(WifiStateEvent.getInstance(true));
                            System.out.println("leo1 WifiStateReceiver : 连接成功 " + info2.getSSID());
                        }

                    }
                }

            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void WifiConnected(OnOff event) {
        System.out.println("leo Event : " + event.isDisConnecetWifi());
       /* App.isWifi = event.isWifiConnect();
        if (App.isWifi) {
            App.startActivity(HomeActivity.class);
           // finish();
        }*/
        if (event.isDisConnecetWifi()) {
            System.out.println("leo 退出后台 ");
            String connectWifiSSID = mWifiAdmin.getConnectWifiSSID(this);
            if (Util.isWifi(connectWifiSSID)) {
                mHandler.removeCallbacksAndMessages(null);
                mWifiAdmin.forget(connectWifiSSID);
            }
        } else {
            System.out.println("leo 恢复前台 ");
            if(mHandler != null)
            mHandler.sendEmptyMessage(3);
        }

    }

    @Override
    public void onWifiConnectStart(String ssid, String bssid) {
        System.out.println("leo onWifiConnectStart : " + ssid);
    }

    @Override
    public void onWifiConnecting(String ssid) {
        System.out.println("leo1 onWifiConnecting : " + ssid);
        /*mHandler.removeMessages(2);
        mHandler.sendEmptyMessageDelayed(2, 500);*/
    }

    @Override
    public void onWifiConnectSuccess(String ssid, String bssid) {
        System.out.println("leo1 onWifiConnectSuccess : " + ssid);
        if (Util.isWifi(ssid)) {
            EventBus.getDefault().post(WifiStateEvent.getInstance(true));
        }
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(1, 2000);
    }

    @Override
    public void onWifiConnnectFail(String ssid, String bssid, int reason) {
        System.out.println("leo1 onWifiConnnectFail : " + ssid + " reason : " + reason);
        if (reason == 1) {
            System.out.println("leo1 密码错误");
        } else {
            mWifiAdmin.removeExitConfig(ssid);
            ScanWifi();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

      /*  long startTime = System.currentTimeMillis();
        while (mWifiAdmin.getCurrentWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            try {

                if ((System.currentTimeMillis() - startTime) > WIFI_SCAN_TIMEOUT) {
                    return false;
                }

                // 为了避免程序一直while循环，让它睡个100毫秒检测……
                Thread.sleep(100);
                //  System.out.println("leo2 openwifi");

            } catch (InterruptedException ie) {
                //  System.out.println("leo3 "+ ie.toString());
                // Log.e(TAG, ie.toString());
            }
        }*/
        ScanWifi();

        return START_STICKY;
    }

    private void ScanWifi() {

        if (mWifiConnector.CanReConnect()) {
            //System.out.println("leo1 正在链接状态");
            return;
        }
        // if(mWifiAdmin.getCurrentWifiState() == 0 || )
        String connectWifiSSID = mWifiAdmin.getConnectWifiSSID(this);//当前连接的wifi ssid

        System.out.println("leo ScanWifi : " + connectWifiSSID );

        if (Util.isWifi(connectWifiSSID)) {//如果当前

            EventBus.getDefault().post(WifiStateEvent.getInstance(true));

            System.out.println("leo 已经连接了 : " + connectWifiSSID);
           // mHandler.sendEmptyMessageDelayed(1, 2000);

        } else {
            // mWifiAdmin.
            //如果没有连接  那么就扫描附近的wifi信号
            mWifiAdmin.startScan();
            ArrayList<ScanResult> wifiListAll = mWifiAdmin.getWifiListAll();
         //   System.out.println("leo wifiListAll : " + wifiListAll.toString());
            if (wifiListAll.size() == 0) {
                System.out.println("leo 没有扫描到信号或者权限 ");
                mHandler.sendEmptyMessageDelayed(1, 3000);

            } else {
                boolean hasResult = false;
                for (ScanResult scanResult : wifiListAll) {
                    //if (Util.isWifi(scanResult.SSID)) {
                    if (!TextUtils.isEmpty(scanResult.SSID) && Util.isWifi(scanResult.SSID)) {
                        WifiConfiguration exsits = mWifiAdmin.isExsits(scanResult.SSID);
                        hasResult = true;
                        if (exsits != null) {//有历史记录
                            System.out.println("leo 历史记录链接 : " + scanResult.SSID);
                            mWifiConnector.addWifiConfig(exsits).connectWithConfig(this);
                            mWifiAdmin.removeConfig(exsits);
                            mHandler.sendEmptyMessage(1);

                        } else {//没有历史记录

                            if (!mWifiAdmin.hasPwd(scanResult)) {
                                //不需要密码连接
                                System.out.println("leo1 不需要密码连接 : " + scanResult);
                                mWifiConnector.addWifiBeanConn(new WifiBeanConn("", scanResult)).connectWithSSID(this);

                            } else {
                                //需要密码连接
                                System.out.println("leo1 需要密码连接 : " + scanResult);
                                mWifiConnector.addWifiBeanConn(new WifiBeanConn("wwkj2017", scanResult)).connectWithSSID(this);
                            }
                        }
                        break;
                    }
                }

                if (!hasResult) {
                    System.out.println("leo 处理没有扫描到指定wifi");
                    mHandler.sendEmptyMessageDelayed(1, 1000);
                    EventBus.getDefault().post(WifiStateEvent.getInstance(false));
                }
            }
        }
    }


    public void disConnectWifi() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        String connectWifiSSID = mWifiAdmin.getConnectWifiSSID(this);
        if (Util.isWifi(connectWifiSSID)) {
            mWifiAdmin.forget(connectWifiSSID);
        }
    }

    public void reConnectWifi() {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(3);
        }
    }




 /*   private void buildWifiBean() {
        wifiBeensTemp.clear();
        mWifiAdmin.startScan();


        String connectWifiSSID = mWifiAdmin.getConnectWifiSSID(this);
        ArrayList<ScanResult> wifiListAll = mWifiAdmin.getWifiListAll();
        for (ScanResult scanResult : wifiListAll) {
            if (connectWifiSSID != null && connectWifiSSID.equalsIgnoreCase(scanResult.SSID)) {
                wifiBeensTemp.add(new WifiBean(true, scanResult));
            } else {
                wifiBeensTemp.add(new WifiBean(false, scanResult));
            }
        }
    }*/


    @Override
    public void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        EventBus.getDefault().unregister(this);
        unregisterWifiStateReceiver();
        super.onDestroy();
    }
}
