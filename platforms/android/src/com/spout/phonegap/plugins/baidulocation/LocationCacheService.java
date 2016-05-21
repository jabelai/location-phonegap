package com.spout.phonegap.plugins.baidulocation;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.cordova.hellocordova.R;

/**
 * location background service
 * Created by jabe on 5/14/16.
 */
public class LocationCacheService extends Service {

    private LocationClient locationClient;

    @SuppressWarnings("unused")
    public void registerLocationListener(BDLocationListener listener) {
        if (locationClient != null) {
            locationClient.registerLocationListener(listener);
        }
    }

    @SuppressWarnings("unused")
    public void requestLocation() {
        if (locationClient != null) {
            Log.d("jabe", "location service request location.");
            locationClient.requestLocation();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private Handler mHandler;

    @Override
    public void onCreate() {
        Log.d("jabe", "location service onCreate.");
        super.onCreate();
        mHandler = new Handler();
        locationClient = new LocationClient(this);
        locationClient.setAK("BfkPvjDGHC0ATZhIr6wxnHh9");//设置百度的ak
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setScanSpan(1000);
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
        option.setProdName("BaiduLoc");
        option.disableCache(true);// 禁止启用缓存定位
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(myLocationListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification =  new NotificationCompat.Builder(this)
                .setContentTitle("巡检系统")
                .setContentTitle("正在定位")
                .build();
        startForeground(R.id.notify, notification);
        if (locationClient != null) {
            locationClient.start();
            locationClient.requestLocation();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("jabe", "location service onDestory.");
        stopForeground(true);
        super.onDestroy();
        if (locationClient != null) {
            locationClient.stop();
        }
    }

    public class MyBinder extends Binder {

        public LocationCacheService getService(){
            return LocationCacheService.this;
        }
    }

    private MyBinder myBinder = new MyBinder();

    private BDLocationListener myLocationListener = new MyLocationListener();


    private static final String url = "http://tangbos.3322.org:8090/gps/gpsuserajax/gpsuser_add2.action";

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            if (location == null)
                return;
            try {
                JSONObject jsonObj = new JSONObject();
                int locationType = location.getLocType();
                String netType = NetWorkHelper.getNetType(getApplicationContext());
                jsonObj.put("locationType", getErrorMessage(locationType));
                jsonObj.put("code", locationType);
                jsonObj.put("satelliteNumber", location.getSatelliteNumber());
                jsonObj.put("nettype", netType);
                jsonObj.put("speed", location.getSpeed());
                Log.d("jabe", "获取位置: " + jsonObj.toString());
                if (location.getSpeed() > 0
                        && location.getSatelliteNumber() >= 3) {
                    if (netType.equals(NetWorkHelper.TYPE_2G)
                            || netType.equals(NetWorkHelper.TYPE_3G)
                            || netType.equals(NetWorkHelper.TYPE_4G)
                            || netType.equals(NetWorkHelper.WIFI)) {
                        new UploadLocationTask().execute(url,
                                "mapX",location.getLatitude()+"",
                                "mapY",location.getLongitude()+"",
                                "key","value");
                    }
                } else {
                    Log.d("jabe","不符合条件,不上传.");
                }
                // 这里简单的循环.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (locationClient != null) {
                            locationClient.requestLocation();
                        }
                    }
                }, 3000);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    private static final Map<Integer, String> ERROR_MESSAGE_MAP = new HashMap<Integer, String>();

    private static final String DEFAULT_ERROR_MESSAGE = "服务端定位失败";

    static {
        ERROR_MESSAGE_MAP.put(61, "GPS定位结果");
        ERROR_MESSAGE_MAP.put(62, "扫描整合定位依据失败。此时定位结果无效");
        ERROR_MESSAGE_MAP.put(63, "网络异常，没有成功向服务器发起请求。此时定位结果无效");
        ERROR_MESSAGE_MAP.put(65, "定位缓存的结果");
        ERROR_MESSAGE_MAP.put(66, "离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果");
        ERROR_MESSAGE_MAP.put(67, "离线定位失败。通过requestOfflineLocaiton调用时对应的返回结果");
        ERROR_MESSAGE_MAP.put(68, "网络连接失败时，查找本地离线定位时对应的返回结果。");
        ERROR_MESSAGE_MAP.put(161, "表示网络定位结果");
    }

    public String getErrorMessage(int locationType) {
        String result = ERROR_MESSAGE_MAP.get(locationType);
        if (result == null) {
            result = DEFAULT_ERROR_MESSAGE;
        }
        return result;
    }


    private class UploadLocationTask extends AsyncTask<String, Long, String> {

        protected String doInBackground(String... url) {
            HttpRequest request = HttpRequest.get(url[0],true);
            if (request.code() == 200) {
                return request.body();
            } else {
                return request.code()+"";
            }
        }

        protected void onProgressUpdate(Long... progress) {
        }

        protected void onPostExecute(String result) {
            Log.d("jabe", "上传结果:" + result);
        }
    }
}