package com.anywave.qpop.event;

/**
 * Created by leo on 2017/12/21.
 */

public class WifiStateEvent {

    private WifiStateEvent() {}

    public boolean isWifiConnect() {
        return isWifiConnect;
    }

    private void setWifiConnect(boolean wifiConnect) {
        isWifiConnect = wifiConnect;
    }

    private boolean isWifiConnect;

    private static WifiStateEvent Instance;

    public synchronized static WifiStateEvent getInstance(boolean isWifiConnect){
        if (Instance == null) {
            Instance = new WifiStateEvent();
        }
        Instance.setWifiConnect(isWifiConnect);
        return Instance;
    }


}
