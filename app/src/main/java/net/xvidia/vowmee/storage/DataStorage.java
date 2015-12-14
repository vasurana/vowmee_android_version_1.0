package net.xvidia.vowmee.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import net.xvidia.vowmee.MyApplication;


public class DataStorage {
	static String MY_PREFERENCES = "SETTINGPREFERENCE";
	static SharedPreferences sharedpreferences;
	 public static final String STR_REGISTERED = "REGISTERED";
	private static  DataStorage instance = null;
	public static  DataStorage getInstance() {
		if (instance == null) {
			instance = new DataStorage();
		}
		return instance;
	}
	private static SharedPreferences getSharedPreference() {
		sharedpreferences = MyApplication.getAppContext().getSharedPreferences(MY_PREFERENCES,
				Context.MODE_PRIVATE);
		return sharedpreferences;
	}

	public static  boolean getIntervalSound() {
		try {
			return getSharedPreference().getBoolean(STR_REGISTERED, false);
		} catch (Exception e) {
		}
		return false;
	}
	public static void setIntervalSound(boolean flag) {
			try {
				Editor editor = getSharedPreference().edit();
					editor.putBoolean(STR_REGISTERED, flag);
				editor.apply();
			} catch (Exception e) {
				// retVal = STR_UNKNOWN;
			}
	}

	public void savePreferences(Context ctx,String key, boolean prefVal) {
		try {
			SharedPreferences.Editor editor = getSharedPreference().edit();
			editor.putBoolean(key, prefVal);
			editor.apply();
		} catch (Exception e) {
		}
	}
	public  boolean readPreferences(Context ctx,String key, boolean prefVal) {
		boolean retVal=false;
		try {
			retVal = getSharedPreference().getBoolean(key, prefVal);
		} catch (Exception e) {
		}
		return retVal;
	}

}