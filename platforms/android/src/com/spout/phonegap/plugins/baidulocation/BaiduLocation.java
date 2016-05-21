package com.spout.phonegap.plugins.baidulocation;


import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;

public class BaiduLocation extends CordovaPlugin {

	private static final String STOP_ACTION = "stop";
	private static final String GET_ACTION = "getCurrentPosition";
	public boolean result = false;
	public CallbackContext callbackContext;

	@Override
	public boolean execute(String action, JSONArray args,
						   final CallbackContext callbackContext) {
		setCallbackContext(callbackContext);
		if (GET_ACTION.equals(action)) {
			try {
				Intent it = new Intent(cordova.getActivity(), LocationCacheService.class);
				cordova.getActivity().startService(it);
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
		} else {
			callbackContext
					.error(PluginResult.Status.INVALID_ACTION.toString());
		}
		return result;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void setCallbackContext(CallbackContext callbackContext) {
		this.callbackContext = callbackContext;
	}
}