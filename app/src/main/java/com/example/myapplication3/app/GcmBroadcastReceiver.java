package com.example.myapplication3.app;

/**
 * Created by Gitanshu on 25-Mar-14.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import static android.media.RingtoneManager.*;

import android.provider.Settings;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    public static Integer numMessages = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
/*    	Log.d("Debug","something received");
        Log.d("Debug",intent.getAction());*/
        String defaultPath = Settings.System.DEFAULT_NOTIFICATION_URI.getPath();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
            String notification_text = intent.getExtras().getString("text");
            Log.d("notif",notification_text);
    			/*
    			NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				Notification notification = null;
				notification = new Notification(R.drawable.ic_launcher, "New Notification on Backpack", System.currentTimeMillis());
				// Cancel the notification after its selected
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				notification.defaults |= Notification.DEFAULT_SOUND;
				notification.defaults |= Notification.DEFAULT_VIBRATE;
				*/

            Intent intent_activity = new Intent(context, MainActivity.class);
            //intent_activity.setAction("OpenNotif");
            PendingIntent pending_intent = PendingIntent.getActivity(context, 0, intent_activity, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int notifyID = 1;
            NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(context)
                    .setContentTitle("New Notification")
                    .setContentText(notification_text)
                    .setContentIntent(pending_intent)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setAutoCancel(true);
//            numMessages++;
            //Log.d("NotifMessage",numMessages.toString());
//            if (numMessages > 1) {
//                mNotifyBuilder.setContentText("You have " + numMessages + " new notifications on Backpack");
//                intent_activity = new Intent(context, MainActivity.class);
//                intent_activity.setAction("OpenNotif");
//                intent_activity.putExtra("type", "ALL");
//                intent_activity.putExtra("id", id);
//                intent_activity.putExtra("courseID", courseID);
//                pending_intent = PendingIntent.getActivity(context, 0, intent_activity, PendingIntent.FLAG_UPDATE_CURRENT);
//                mNotifyBuilder.setContentIntent(pending_intent);
//            }
            mNotificationManager.notify(
                    notifyID,
                    mNotifyBuilder.build());

        }
    }
}