package com.example.lyn.hyundai2;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.StringTokenizer;


public class GcmIntentService extends IntentService {
    public static final String TAG = "letter";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    private static PowerManager.WakeLock sCpuWakeLock;
    private static KeyguardManager.KeyguardLock mKeyguardLock;
    private static boolean isScreenLock;
    public GcmIntentService() {
//        Used to name the worker thread, important only for debugging.
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        acquireCpuWakeLock(this);
        Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {

            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.

                String msg = intent.getStringExtra("msg");
                String address = intent.getStringExtra("address");
                String tel = intent.getStringExtra("tel");

                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.
                try {
                    String data = URLDecoder.decode(msg, "euc-kr");
                    address = URLDecoder.decode(address, "euc-kr");
                    tel = URLDecoder.decode(tel, "euc-kr");
                    Log.d("saveus", "msg : " + msg);
                    Log.d("saveus", "address : " + address);
                    Log.d("saveus", "tel : " + tel);
                    //sendNotification(" [ " + sender + " ] " + data, data, sender, receiver);


                    /*************** App이 실행중일 때 와 실행중이지 않을 때 알림 받는 경우를 구분하기 위해 사용 *************************/
                    ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> runList = am.getRunningTasks(10);
                    ComponentName name = runList.get(0).topActivity;
                    String className = name.getClassName();
                    boolean isAppRunning = false;

                    if(className.contains("package com.example.lyn.hyundai2")) {
                        isAppRunning = true;
                    }

                    if(isAppRunning == true) {
                        Intent cardintent = new Intent(this, Main2Activity.class);
                        cardintent.putExtra("msg", data);
                        cardintent.putExtra("address", address);
                        cardintent.putExtra("tel", tel);
                        cardintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        this.startActivity(cardintent);
                        // 앱이 실행중일 경우 로직 구현

                    } else {
                        sendNotification(data, address, tel);
                    }
                    releaseCpuLock();
                    /****************************************************************************************************/
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                //Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String data, String address, String tel) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, Main2Activity.class);
        intent.putExtra("msg", data);
        intent.putExtra("address", address);
        intent.putExtra("tel", tel);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(500);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.hyundai)
                        .setContentTitle("SaveUs")
                        .setTicker("New Alarm!")
                        .setAutoCancel(true)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(data))
                        .setContentText(data);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    static void acquireCpuWakeLock(Context context) {
        Log.e("PushWakeLock", "Acquiring cpu wake lock");
        Log.e("PushWakeLock", "wake sCpuWakeLock = " + sCpuWakeLock);

        if (sCpuWakeLock != null) {
            return;
        }
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        sCpuWakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "hello");

        sCpuWakeLock.acquire();
    }

    static void releaseCpuLock() {
        Log.e("PushWakeLock", "Releasing cpu wake lock");
        Log.e("PushWakeLock", "relase sCpuWakeLock = " + sCpuWakeLock);

        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }
}
