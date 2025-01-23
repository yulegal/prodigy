package com.vapid_software.prodigy.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.vapid_software.prodigy.R;

public class NotificationBuilder {

    private NotificationCompat.Builder builder;

    public final static int NID = 1000;
    public final static int NEW_MESSAGE_NID = 1001;

    private final static String CHANNEL_ID = "channel_id";
    private final static String CHANNEL_NAME = "gobbol_channel";
    private int nid = NID;

    private Context context;

    public NotificationBuilder(Context context) {
        builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.notifications);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setChannelId(CHANNEL_ID);
        this.context = context;
        setPendingIntent(new Intent());
    }

    public NotificationCompat.Builder getBuilder() {
        return builder;
    }

    public static NotificationBuilder create(Context context) {
        return new NotificationBuilder(context);
    }
    public void setNotificationId(int id) {
        nid = id;
    }

    public void setPendingIntent(Intent intent) {
        builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public void build() {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }
        nm.notify(nid, builder.build());
    }

}
