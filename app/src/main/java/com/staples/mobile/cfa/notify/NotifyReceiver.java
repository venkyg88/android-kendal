package com.staples.mobile.cfa.notify;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.urbanairship.push.BaseIntentReceiver;
import com.urbanairship.push.PushMessage;
import com.urbanairship.push.notifications.NotificationFactory;
import com.urbanairship.util.NotificationIDGenerator;

public class NotifyReceiver extends BaseIntentReceiver {
    private static final String TAG = "NotifyReceiver";

    private static final String APPLICATION = "com.staples.mobile.cfa";

    public static final String ACTION_OPEN_SKU = APPLICATION +".OPEN_SKU";
    public static final String ACTION_OPEN_CATEGORY = APPLICATION +".OPEN_CATEGORY";
    public static final String ACTION_OPEN_SEARCH = APPLICATION +".OPEN_SEARCH";

    public static final String EXTRA_SKU = APPLICATION +".SKU";
    public static final String EXTRA_IDENTIFIER = APPLICATION +".IDENTIFIER";
    public static final String EXTRA_KEYWORD = APPLICATION +".KEYWORD";
    public static final String EXTRA_TITLE = APPLICATION +".TITLE";

    private static final String URBAN_AIRSHIP_CHANNEL = "urbanAirshipChannel";

    private ObjectMapper mapper;

    // JSON model for notifications

    private static class Action {
        private Open open;
    }

    private static class Open {
        private String type;
        private String content;
        private String title;
    }

    // This cruft is necessary only to change the notification icon!

    public static class CustomNotificationFactory extends NotificationFactory {
        private String header;

        public CustomNotificationFactory(Context context) {
            super(context);
            header = context.getResources().getString(R.string.notification_header);
        }

        @Override
        public Notification createNotification(PushMessage message, int notificationId) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
            builder.setDefaults(0);
            builder.setContentTitle(header);
            builder.setContentText(message.getAlert());
            builder.setAutoCancel(true);
            builder.setSmallIcon(R.drawable.ic_notification);
            builder.setColor(getContext().getResources().getColor(R.color.staples_middle_gray));
            builder.extend(createNotificationActionsExtender(message, notificationId));
            return builder.build();
        }

        @Override
        public int getNextId(PushMessage pushMessage) {
            return NotificationIDGenerator.nextID();
        }
    }

    public NotifyReceiver() {
        super();
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    protected void onChannelRegistrationSucceeded(Context context, String channelId) {
        Log.d(TAG, "Urban Airship channel " + channelId);
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.PREFS_FILENAME, Context.MODE_PRIVATE);
        String savedId = prefs.getString(URBAN_AIRSHIP_CHANNEL, null);
        if (savedId==null || !savedId.equals(channelId)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(URBAN_AIRSHIP_CHANNEL, channelId);
            Log.d(TAG, "New Urban Airship channel " + channelId);
        }
    }

    @Override
    protected void onChannelRegistrationFailed(Context context) {
        Log.i(TAG, "Channel registration failed.");
    }

    @Override
    protected void onPushReceived(Context context, PushMessage message, int notificationId) {
        Log.i(TAG, "Received push notification. Alert: " + message.getAlert() + ". Notification ID: " + notificationId);
    }

    @Override
    protected void onBackgroundPushReceived(Context context, PushMessage message) {
        Log.i(TAG, "Received background push message: " + message);
    }

    @Override
    protected boolean onNotificationOpened(Context context, PushMessage message, int notificationId) {
        Log.i(TAG, "User clicked notification. Alert: " + message.getAlert());

        // Parse payload
        String payload = message.getActionsPayload();
        Action note;
        try {
            note = mapper.readValue(payload, Action.class);
        } catch(Exception e) {
            Log.d(TAG, "Exception " + e.toString());
            return(false);
        }

        // Get values
        Open open = note.open;
        if (open==null) return(false);
        String type = open.type;
        if (type==null) return(false);
        String content = open.content;
        if (content==null) return(false);
        String title = open.title;

        if ("sku".equals(type)) {
            Log.d(TAG, "Received open SKU command " + content);
            Intent intent = new Intent(ACTION_OPEN_SKU);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(EXTRA_SKU, content);
            if (title!=null) intent.putExtra(EXTRA_TITLE, title);
            context.startActivity(intent);
            return(false);
        }

        if ("category".equals(type)) {
            Log.d(TAG, "Received open category command " + content);
            Intent intent = new Intent(ACTION_OPEN_CATEGORY);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(EXTRA_IDENTIFIER, content);
            if (title!=null) intent.putExtra(EXTRA_TITLE, title);
            context.startActivity(intent);
            return(false);
        }

        if ("search".equals(type)) {
            Log.d(TAG, "Received open search command " + content);
            Intent intent = new Intent(ACTION_OPEN_SEARCH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(EXTRA_KEYWORD, content);
            if (title!=null) intent.putExtra(EXTRA_TITLE, title);
            context.startActivity(intent);
            return(false);
        }

        return false;
    }

    @Override
    protected boolean onNotificationActionOpened(Context context, PushMessage message, int notificationId, String buttonId, boolean isForeground) {
        Log.i(TAG, "User clicked notification button. Button ID: " + buttonId + " Alert: " + message.getAlert());
        return false;
    }
}
