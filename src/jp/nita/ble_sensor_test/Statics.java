package jp.nita.ble_sensor_test;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Statics {

	public static final String PREF_KEY = "ble-sensor-test";
	public static final String SETTING_CENTRAL_AUTO_CONNECT = "centralAutoConnect";

	public static final int SETTING_CENTRAL_AUTO_CONNECT_FALSE = 0;
	public static final int SETTING_CENTRAL_AUTO_CONNECT_TRUE = 1;

	public static String getCentralAutoConnect(int which) {
		switch (which) {
		case Statics.SETTING_CENTRAL_AUTO_CONNECT_FALSE:
			return "false";
		case Statics.SETTING_CENTRAL_AUTO_CONNECT_TRUE:
			return "true";
		default:
			return "???";
		}
	}

	public static String getIncludeDeviceName(Context context, int which) {
		switch (which) {
		case 0:
			return context.getString(R.string.no);
		default:
			return context.getString(R.string.yes);
		}
	}

	public static int preferenceValue(Context context, String key, int def) {
		SharedPreferences pref = context.getSharedPreferences(PREF_KEY, Activity.MODE_PRIVATE);
		return pref.getInt(key, def);
	}

	public static void setPreferenceValue(Context context, String key, int val) {
		SharedPreferences pref = context.getSharedPreferences(PREF_KEY, Activity.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putInt(key, val);
		editor.commit();
	}

}
