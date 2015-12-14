package net.xvidia.vowmee.network;


import net.xvidia.vowmee.MyApplication;
import net.xvidia.vowmee.R;

public class ServiceURLManager implements IAPIConstants {

	final String base_URL = MyApplication.getAppContext().getResources().getString(R.string.base_url);
	public String getURL(int SERVICE_API){
		String service = "";
		switch (SERVICE_API) {
			case API_KEY_AUTH_FACEBOOK:
				service = base_URL + ServiceURL.API_KEY_AUTH_FACEBOOK;
				break;
			case API_KEY_AUTH_TWITTER:
				service = base_URL + ServiceURL.API_KEY_AUTH_TWITTER;
				break;
			case API_KEY_SOCIAL_REGISTER:
				service = base_URL + ServiceURL.API_KEY_SOCIAL_REGISTER;
				break;
			case API_KEY_LAYOUT_TIME_OFFLINE:
				service = base_URL + ServiceURL.API_KEY_LAYOUT_TIME_OFFLINE;
				break;
			case API_KEY_ONOFF_SCREEN:
				service = base_URL + ServiceURL.API_KEY_ONOFF_SCREEN;
				break;
			case API_KEY_ONOFF_APP:
				service = base_URL + ServiceURL.API_KEY_ONOFF_APP;
				break;
			case API_KEY_ONOFF_BOX:
				service = base_URL + ServiceURL.API_KEY_ONOFF_BOX;
				break;
			case API_KEY_ONOFF_SCREEN_OFFLINE:
				service = base_URL + ServiceURL.API_KEY_ONOFF_SCREEN_OFFLINE;
				break;
			case API_KEY_ONOFF_APP_OFFLINE:
				service = base_URL + ServiceURL.API_KEY_ONOFF_APP_OFFLINE;
				break;
			case API_KEY_ONOFF_BOX_OFFLINE:
				service = base_URL + ServiceURL.API_KEY_ONOFF_BOX_OFFLINE;
				break;
			case API_KEY_BOX_LOCATION:
				service = base_URL + ServiceURL.API_KEY_BOX_LOCATION;
				break;
			case API_KEY_BOX_INVENTORY_DATA:
				service = base_URL + ServiceURL.API_KEY_BOX_INVENTORY_DATA;
				break;
			case API_KEY_BOX_GET_INVENTORY_DATA:
				service = base_URL + ServiceURL.API_KEY_BOX_GET_INVENTORY_DATA;
				break;
			case API_KEY_BOX_DOWNLOADING_STATUS:
				service = base_URL + ServiceURL.API_KEY_BOX_DOWNLOADING_STATUS;
				break;
			case API_KEY_BOX_UPDATE_APK:
				service = base_URL + ServiceURL.API_KEY_BOX_UPDATE_APK;
				break;
		}
		return service;	
	}




	public String getUrl(int urlKey){
		return getURL(urlKey);
	}



}
