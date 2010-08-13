package com.redhorse.reddial;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, ServiceRed.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(i);
	}
}