package net.xvidia.vowmee;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class Splash extends Activity {
	/** Called when the activity is first created. */
	private Thread mSplashThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Context ctx = getApplicationContext();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		setContentView(R.layout.splash);
		mSplashThread = new Thread() {
			@Override
			public void run() {
				try {
					synchronized (this) {
						wait(2000);
					}
				} catch (InterruptedException ex) {
				}

				startActivity(new Intent(Splash.this, VerifyPhonenumberActivity.class));
				finish();
			}
		};
		mSplashThread.start();
	}

	@Override
	public boolean onTouchEvent(MotionEvent evt) {
		if (evt.getAction() == MotionEvent.ACTION_DOWN) {
			synchronized (mSplashThread) {
				mSplashThread.notifyAll();
			}
		}
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	}

	public void showToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		this.finish();
	}

	@Override
	protected void onResume() {
		super.onResume();


	}
	

}
