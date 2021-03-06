/*
 * Created by chenru on 2021/01/12.
 * Copyright 2015－2021 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sensorsdata.uniapp;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.sensorsdata.analytics.android.sdk.SAConfigOptions;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPIEmptyImplementation;
import com.sensorsdata.uniapp.property.PluginVersionInterceptor;
import com.sensorsdata.uniapp.property.UniPropertyManager;

import io.dcloud.feature.uniapp.UniAppHookProxy;

public class UniSensorsAnalyticsProxy implements UniAppHookProxy {
    private static final String LOG_TAG = "SA.UniProxy";

    @Override
    public void onCreate(Application application) {
        try {
            if (SensorsDataAPI.sharedInstance() instanceof SensorsDataAPIEmptyImplementation) {
                initSensorsDataAPI(application);
            }
            UniPropertyManager.addInterceptor(new PluginVersionInterceptor());
        } catch (Exception e) {
            Log.i(LOG_TAG, "SensorsDataAnalytics init failed!");
        }
    }

    @Override
    public void onSubProcessCreate(Application application) {
        //子进程初始化回调
    }

    /**
     * 初始化 Sensors Analytics SDK
     */
    private void initSensorsDataAPI(Application application) {
        PackageManager pm = application.getPackageManager();
        SAConfigOptions configOptions = new SAConfigOptions("");
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA);
            // 设置数据上报地址
            String serverUrl = appInfo.metaData.getString("com.sensorsdata.analytics.uni.ServerUrl", "");
            if (!TextUtils.isEmpty(serverUrl)) {
                configOptions.setServerUrl(serverUrl);
            }
            // 设置全埋点采集内容
            try {
                int autoTrackType = appInfo.metaData.getInt("com.sensorsdata.analytics.uni.AutoTrackType", 0);
                configOptions.setAutoTrackEventType(autoTrackType);
            } catch (Exception ignored) {

            }
            // 日志打印
            try {
                boolean enableLog = appInfo.metaData.getBoolean("com.sensorsdata.analytics.uni.EnableLogging", false);
                if (enableLog) {
                    configOptions.enableLog(enableLog);
                }
            } catch (Exception ignored) {

            }
            // 设置本地缓存上限
            try {
                int maxCacheSize = appInfo.metaData.getInt("com.sensorsdata.analytics.uni.MaxCacheSize", 32);
                if (maxCacheSize > 0) {
                    configOptions.setMaxCacheSize(maxCacheSize * 1024 * 1024);
                }
            } catch (Exception ignored) {

            }
            // 设置是否开启加密
            try {
                boolean encrypt = appInfo.metaData.getBoolean("com.sensorsdata.analytics.uni.Encrypt", false);
                if (encrypt) {
                    configOptions.enableEncrypt(true);
                }
            } catch (Exception ignored) {

            }
            // 设置数据采集
            try {
                boolean dataCollect = appInfo.metaData.getBoolean("com.sensorsdata.analytics.uni.DataCollect", true);
                if (!dataCollect) {
                    configOptions.disableDataCollect();
                }
            } catch (Exception ignored) {

            }
            // 小程序进程是否允许发送数据
            try {
                boolean isMPProcessFlush = appInfo.metaData.getBoolean("com.sensorsdata.analytics.uni.MPProcessFlush", false);
                if (isMPProcessFlush) {
                    configOptions.enableSubProcessFlushData();
                }
            } catch (Exception ignored) {

            }
            // 设置是否开启日志
            try {
                boolean javaScriptBridge = appInfo.metaData.getBoolean("com.sensorsdata.analytics.uni.JavaScriptBridge", false);
                if (javaScriptBridge) {
                    // 设置是否开启日志
                    boolean isSupportJellyBean = false;
                    try {
                        isSupportJellyBean = appInfo.metaData.getBoolean("com.sensorsdata.analytics.uni.JavaScriptBridgeSupportJellyBean", false);
                    } catch (Exception ignored) {

                    }
                    configOptions.enableJavaScriptBridge(isSupportJellyBean);
                }
            } catch (Exception ignored) {

            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(LOG_TAG, e.getMessage());
        }

        SensorsDataAPI.startWithConfigOptions(application, configOptions);
    }
}
