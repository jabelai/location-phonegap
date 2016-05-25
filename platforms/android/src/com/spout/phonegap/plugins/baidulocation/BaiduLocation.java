package com.spout.phonegap.plugins.baidulocation;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.baidu.location.BDLocation;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

public class BaiduLocation extends CordovaPlugin {

    private static final String STOP_ACTION = "stop";
    private static final String GET_ACTION = "getCurrentPosition";
    private static final String GET_ONCE_ACTION = "getCurrentPositionOnce";
    public boolean result = false;
    public CallbackContext callbackContext;


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            callBackGetPosOnce((BDLocation) intent.getParcelableExtra("location"));
        }
    };

    private void callBackGetPosOnce(BDLocation location) {
        if (callbackContext != null) {
            if (location != null) {
                try {
                    JSONObject jsonObj = new JSONObject();
                    JSONObject coords = new JSONObject();
                    coords.put("latitude", location.getLatitude());
                    coords.put("longitude", location.getLongitude());
                    coords.put("radius", location.getRadius());
                    jsonObj.put("coords", coords);
                    int locationType = location.getLocType();
                    jsonObj.put("locationType", locationType);
                    jsonObj.put("code", locationType);
                    switch (location.getLocType()) {
                        case BDLocation.TypeGpsLocation:
                            coords.put("speed", location.getSpeed());
                            coords.put("altitude", location.getAltitude());
                            jsonObj.put("SatelliteNumber",
                                    location.getSatelliteNumber());
                            break;
                        case BDLocation.TypeNetWorkLocation:
                            jsonObj.put("addr", location.getAddrStr());
                            break;
                    }
                    Log.d("jabe", "call back once pos : " + jsonObj.toString());
                    callbackContext.success(jsonObj.toString());
                } catch (Throwable e) {
                    e.printStackTrace();
                    callbackContext.error("remote service error.");
                }
            } else {
                callbackContext.error(" service not connected error.");
            }
        }
    }


    @Override
    public boolean execute(String action, JSONArray args,
                           final CallbackContext callbackContext) {
        setCallbackContext(callbackContext);
        if (GET_ACTION.equals(action)) {
            try {
                String params = null;
                if (args != null && args.length() > 0) {
                    params = (String) args.get(0);
                }
                Intent it = new Intent(cordova.getActivity(), LocationCacheService.class);
                if (params != null) it.putExtra("params", params);
                cordova.getActivity().startService(it);
                Log.d("jabe", "params : " + args.toString());
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return true;
        } else if (STOP_ACTION.equals(action)) {
            try {
                Intent it = new Intent(cordova.getActivity(), LocationCacheService.class);
                cordova.getActivity().stopService(it);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            callbackContext.success(200);
            return true;
        } else if (GET_ONCE_ACTION.equals(action)) {
            try {
                if (!hasRegBroadcast) {
                    cordova.getActivity().registerReceiver(receiver, new IntentFilter(LocationCacheService.BROADCAST_ACTION));
                    hasRegBroadcast = true;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            Intent it = new Intent(cordova.getActivity(), LocationCacheService.class);
            it.setAction(LocationCacheService.BROADCAST_ACTION);
            try {
                cordova.getActivity().startService(it);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            callbackContext
                    .error(PluginResult.Status.INVALID_ACTION.toString());
        }
        return result;
    }

    boolean hasRegBroadcast = false;

    @Override
    public void onDestroy() {
        if (cordova.getActivity() != null) {
            cordova.getActivity().unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    public void setCallbackContext(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }
}