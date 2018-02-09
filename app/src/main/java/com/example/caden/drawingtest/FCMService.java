package com.example.caden.drawingtest;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMService extends FirebaseMessagingService {

    final int RC_Notify_ID = 1733;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        /*
         There are two types of messages data messages and notification messages. Data messages are handled
         here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
         traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
         is in the foreground. When the app is in the background an automatically generated notification is displayed.
         When the user taps on the notification they are returned to the app. Messages containing both notification
         and data payloads are treated as notification messages. The Firebase console always sends notification
         messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        */

        /* Not getting messages here? See why this may be: https://goo.gl/39bRN */
        /* Messages are from:  remoteMessage.getFrom()) */

        /* Check if message contains a data payload. */

        /* Check if message contains a notification payload. */
        if (remoteMessage.getNotification() != null) {
            sendNotification(remoteMessage.getNotification().getBody());
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, DrawingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, RC_Notify_ID , intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(RC_Notify_ID, notificationBuilder.build());
        }
    }
}