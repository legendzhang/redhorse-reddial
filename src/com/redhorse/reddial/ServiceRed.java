package com.redhorse.reddial;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class ServiceRed extends Service {
	private NotificationManager mNM;
	private Method mStartForeground;
	private Method mStopForeground;
	private Object[] mStartForegroundArgs = new Object[2];
	private Object[] mStopForegroundArgs = new Object[1];

	@Override
	public void onCreate() {

		notification("");

//		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//		CharSequence text = "小红马快速启动：随时启动你的最爱!";
//
//		Notification notification = new Notification(R.drawable.icon_noborder,
//				text, System.currentTimeMillis());
//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//				new Intent(this, reddial.class), 0);
//		notification.setLatestEventInfo(this,
//				this.getString(R.string.app_name), text, contentIntent);
//		notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
//		startForegroundCompat(0, notification);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private void notification(String msginfo) {
		try {
			mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			CharSequence appName = this.getString(R.string.app_name);
			Notification notification = new Notification(
					R.drawable.icon, appName,
					System.currentTimeMillis());
			notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
			CharSequence appDescription = msginfo;
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					new Intent(this, reddial.class), 0);
			notification.setLatestEventInfo(this, appName, appDescription,
					contentIntent);
			mNM.notify(0, notification);
		} catch (Exception e) {
			mNM = null;
		}
	}

	void startForegroundCompat(int id, Notification notification) {
		// If we have the new startForeground API, then use it.
		if (mStartForeground != null) {
			mStartForegroundArgs[0] = Integer.valueOf(id);
			mStartForegroundArgs[1] = notification;
			try {
				mStartForeground.invoke(this, mStartForegroundArgs);
			} catch (InvocationTargetException e) {
				// Should not happen.
				Log.w("reddial", "Unable to invoke startForeground", e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				Log.w("reddial", "Unable to invoke startForeground", e);
			}
			return;
		}

		// Fall back on the old API.
		setForeground(true);
		mNM.notify(id, notification);
	}

}