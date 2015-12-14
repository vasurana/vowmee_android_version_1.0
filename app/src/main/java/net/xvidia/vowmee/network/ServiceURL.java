package net.xvidia.vowmee.network;


import net.xvidia.vowmee.MyApplication;
import net.xvidia.vowmee.R;

public class ServiceURL {
	public static final String API_KEY_AUTH_FACEBOOK = MyApplication.getAppContext().getResources().getString(R.string.api_auth_facebook);
	public static final String API_KEY_AUTH_TWITTER = MyApplication.getAppContext().getResources().getString(R.string.api_auth_twitter);
	public static final String API_KEY_SOCIAL_REGISTER= MyApplication.getAppContext().getResources().getString(R.string.api_social_regiter);
	public static final String API_KEY_LAYOUT_TIME_OFFLINE = "/signage/layoutDisplay/saveList";
	public static final String API_KEY_ONOFF_BOX= "/signage/status/box";
	public static final String API_KEY_ONOFF_APP = "/signage/status/app";
	public static final String API_KEY_ONOFF_SCREEN = "/signage/status/screen";
	public static final String API_KEY_ONOFF_BOX_OFFLINE= "/signage/status/boxList";
	public static final String API_KEY_ONOFF_APP_OFFLINE = "/signage/status/appList";
	public static final String API_KEY_ONOFF_SCREEN_OFFLINE = "/signage/status/screenList";
	public static final String API_KEY_BOX_LOCATION = "/signage/box/saveBoxLocation";
	public static final String API_KEY_BOX_INVENTORY_DATA = "/signage/remote/box";
	public static final String API_KEY_BOX_GET_INVENTORY_DATA = "/signage/remote/box/license/";
	public static final String API_KEY_BOX_DOWNLOADING_STATUS = "/signage/remote/box/downloadStatus";
	public static final String API_KEY_BOX_UPDATE_APK = "/signage/apkStatus/";
}
