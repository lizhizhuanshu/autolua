package top.lizhistudio.sample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;

public class MainService extends Service {
    private static final String TAG = "MainService";
    private static final int NOTIFICATION_ID = 10086;
    private static final String CHANNEL_ONE_ID = "AutoLua";
    public MainService() {
    }

    protected void onStartNotification()
    {
        String CHANNEL_ONE_NAME = "常驻服务设置";
        NotificationChannel notificationChannel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(CHANNEL_ONE_ID)
                .setContentText("正在运行.......")
                .setSmallIcon(R.mipmap.auxiliary)
                .setWhen(System.currentTimeMillis());

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            builder.setChannelId(CHANNEL_ONE_ID);

        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        startForeground(NOTIFICATION_ID,notification);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        onStartNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}