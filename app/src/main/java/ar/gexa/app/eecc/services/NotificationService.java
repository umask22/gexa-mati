package ar.gexa.app.eecc.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import ar.gexa.app.eecc.BuildConfig;
import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.repository.UserRepository;
import ar.gexa.app.eecc.utils.DateUtils;
import ar.gexa.app.eecc.views.GMailSender;
import ar.gexa.app.eecc.views.SendLocation;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationService {

    private static volatile NotificationService instance;
    private Intent serviceIntent;

    public static NotificationService getInstance() {
        if (instance == null) {
            synchronized (NotificationService.class) {
                if (instance == null)
                    instance = new NotificationService();
            }
        }
        return instance;
    }

    private static final String CHANNEL_ID = "gexa";

    private int index = 0;


    public NotificationCompat.Builder builder(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_notification_gexa);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setWhen(System.currentTimeMillis());
        builder.setPriority(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? NotificationManager.IMPORTANCE_HIGH
                        : NotificationCompat.PRIORITY_MAX);
        return builder;
    }

    public void show(Context context, String title, String description, String group) {

        final NotificationCompat.Builder builder = builder(context);
        builder.setContentTitle(title);
        builder.setContentText(description);
        if (group != null)
            builder.setGroup(group);
        show(context, builder.build());
    }

    public void onDeviceSynchronize(Context context) {
        final NotificationCompat.Builder builder = builder(context);
        builder.setProgress(0, 100, true);
        builder.setOngoing(true);
        builder.setContentTitle("Sincronizando dispositivo");
        show(context, builder.build());
    }

    public void onDeviceUnSynchronize(Context context) {
        final NotificationCompat.Builder builder = builder(context);
        builder.setProgress(0, 100, true);
        builder.setOngoing(true);
        builder.setContentTitle("Desincronizando dispositivo");
        show(context, builder.build());
    }

    public void onClearNotifications(Context context) {
        getNotificationManager(context).cancelAll();
    }

    public void show(Context context, Notification notification) {
        show(context, notification, this.index);
        this.index++;
    }

    public void show(Context context, Notification notification, int index) {
        final NotificationManager notificationManager = getNotificationManager(context);
        assert notificationManager != null;
        notificationManager.notify(0, notification);
    }

    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }

    public void onDeviceLocateStart(Context context) {
        Broadcast broadcast = new Broadcast();
        serviceIntent = new Intent(context, broadcast.getClass());
        if (!isServiceRunning(broadcast.getClass(), context)) {
            context.sendBroadcast(serviceIntent);
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager;
        manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }

    public void onDeviceLocateStop(Context context) {
        SendLocation sendLocation = new SendLocation();
        serviceIntent = new Intent(context, sendLocation.getClass());
        if (isServiceRunning(sendLocation.getClass(), context)) {
//            context.stopService(serviceIntent);
            sendLocation.onDestroy();
//            sendLocation.stopService(new Intent(SendLocation.MY_SERVICE));

//            Intent stopIntent = new Intent(context, Broadcast.class);
//            context.stopService(stopIntent);
        }
    }

    public void txtLog(String text) {

        File logFile = new File(Environment.getExternalStorageDirectory() + "/gexa/logger.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));
            bufferedWriter.write(DateUtils.toString(new Date(), DateUtils.Pattern.DEFAULT) + "\n" + text);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void txtLogDelete() {
        File logFile = new File(Environment.getExternalStorageDirectory() + "/gexa/logger.txt");
        if (logFile.exists())
            logFile.delete();
    }

    public void updateApk(final Context applicationContext) {
        final NotificationCompat.Builder builder = builder(applicationContext);

        builder.setProgress(0, 100, true);
        builder.setOngoing(true);
        builder.setContentTitle("Actualizando geXa...");
        show(applicationContext, builder.build());
        File logFile = new File(Environment.getExternalStorageDirectory() + "/gexa/logger.txt");
        if (logFile.exists())
            logFile.delete();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://gexa.com.ar/app/export/apk");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();

                    File file = new File(Environment.getExternalStorageDirectory().getPath() + "/gexa/apkFile.apk");
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    int bytesRead;
                    byte[] buffer = new byte[4096];
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                    fileOutputStream.close();
                    inputStream.close();

                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onClearNotifications(applicationContext);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Uri apkUri = FileProvider.getUriForFile(applicationContext, BuildConfig.APPLICATION_ID + ".provider",
                                        new File(Environment.getExternalStorageDirectory()+"/gexa/" + "apkFile.apk"));
                                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                                applicationContext.startActivity(intent);
                            } else{
                                onClearNotifications(applicationContext);
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/gexa/" + "apkFile.apk")), "application/vnd.android.package-archive");
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                applicationContext.startActivity(intent);
                            }
                        }
                    }, 10000);

                } catch (Exception e) {
                    txtLog(NotificationService.class.getSimpleName() + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void getAllFilesInDir() {
        String path = Environment.getExternalStorageDirectory() + "/gexa/";

        Log.d("Files", "Path: " + path);
        File f = new File(path);
        for (File file : f.listFiles()) {
            if (file.exists())
                file.delete();
        }
    }

    public void getMail() {
        final GMailSender sender = new GMailSender("gexa.desarrollo@gmail.com", "suicida23");
        final String title = "LogTxt " + UserRepository.getInstance().find().getUsername();
        final String bodyMessage = "No se encontro el archivo en el dispositivo";
        final String receiver = "gexa.desarrollo@gmail.com";
        final String attachments = Environment.getExternalStorageDirectory() + "/gexa/logger.txt";

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(new File(attachments).exists())
                        sender.sendMail(title, "", receiver,receiver, attachments);
                    else
                        sender.sendMail(title,bodyMessage, receiver, receiver, null);
                } catch (Exception e) {
                    Log.e("mylog", "Error: " + e.getMessage()); }
            }
        });
        thread.start();
    }
}
