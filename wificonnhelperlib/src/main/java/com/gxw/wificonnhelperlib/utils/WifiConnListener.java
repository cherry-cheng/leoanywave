/*
 * Copyright 2017 Bird Guo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gxw.wificonnhelperlib.utils;

/**
 * Created by guoxw on 2017/8/16 0016.
 *
 * @auther guoxw
 * @createTime 2017/8/16 0016 17:04
 * @packageName com.gxw.wificonnhelperlib.utils
 */

public interface WifiConnListener {

    /**
     * On wifi connect start.
     *
     * @param ssid
     *         the ssid
     * @param bssid
     *         the bssid
     */
    void onWifiConnectStart(String ssid, String bssid);


    void onWifiConnecting(String ssid);

    /**
     * On wifi connect success.
     *
     * @param ssid
     *         the ssid
     * @param bssid
     *         the bssid
     */
    void onWifiConnectSuccess(String ssid, String bssid);

    /**
     * On wifi connnect fail.
     *
     * @param ssid
     *         the ssid
     * @param bssid
     *         the bssid
     * @param reason
     *         the reason
     */
    void onWifiConnnectFail(String ssid, String bssid, int reason);

}
