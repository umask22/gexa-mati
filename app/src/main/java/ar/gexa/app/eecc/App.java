package ar.gexa.app.eecc;

import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import ar.gexa.app.eecc.db.DatabaseManager;
import ar.gexa.app.eecc.rest.GexaClient;
import ar.gexa.app.eecc.services.NotificationService;
import ar.gexa.app.eecc.services.PhoneCallReceiverService;
import ar.gexa.app.eecc.utils.DateUtils;
import ar.gexa.app.eecc.utils.FileUtils;

public class App extends Application {

    private Intent appServiceIntent;

    @Override
    public void onCreate() {

        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable throwable) {
                NotificationService.getInstance().txtLog(App.class.getSimpleName()+"\n"+throwable.getMessage()+"\n\n");
                Log.e(App.class.getSimpleName(), throwable.getMessage(), throwable);
            }
        });


        GexaClient.getInstance().init();

        DatabaseManager.getInstance().init(getApplicationContext());

        FileUtils.createDirectory("gexa/calls");

        initNotificationChannel();

        appServiceIntent = new Intent(getApplicationContext(), PhoneCallReceiverService.class);
        if (!isMyServiceRunning(PhoneCallReceiverService.class)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(appServiceIntent);
            }else
                startService(appServiceIntent);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        stopService(appServiceIntent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        final ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if(manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            final NotificationChannel channel = new NotificationChannel("gexa", "geXa", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notificaciones geXa");

            final NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if(notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }
}
