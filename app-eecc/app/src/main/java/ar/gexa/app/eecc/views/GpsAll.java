//package ar.gexa.app.eecc.views;
//
//import android.Manifest;
//import android.accessibilityservice.AccessibilityService;
//import android.app.Activity;
//import android.app.AlarmManager;
//import android.app.IntentService;
//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.os.SystemClock;
//import android.support.annotation.Nullable;
//import android.support.annotation.RequiresApi;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.content.ContextCompat;
//import android.widget.Toast;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.maps.model.LatLng;
//
//
//import org.greenrobot.eventbus.EventBus;
//
//import ar.gexa.app.eecc.rest.Callback;
//import ar.gexa.app.eecc.rest.GexaClient;
//
//
//public class GpsAll extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
//
//    private LocationRequest mLocationRequest;
//    private GoogleApiClient mGoogleApiClient;
//    boolean boolean_permission;
//    private static final int REQUEST_PERMISSIONS = 100;
//
//    public static final String MY_SERVICE = "ar.gexa.app.eecc";
//
//
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        if (Build.VERSION.SDK_INT >= 26) {
//            String CHANNEL_ID = "my_channel_01";
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
//                    "Channel human readable title",
//                    NotificationManager.IMPORTANCE_DEFAULT);
//
//            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
//
//            Notification notification = new NotificationCompat.Builder(this, "")
//                    .setContentTitle("")
//                    .setContentText("").build();
//
//            startForeground(1, notification);
//        }
//        buildGoogleApiClient();
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private void startMyOwnForeground() {}
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//        if (!mGoogleApiClient.isConnected())
//            mGoogleApiClient.connect();
//        return START_STICKY;
//    }
//
//    @Override
//    public boolean stopService(Intent name) {
//        stopSelf();
//        return true;
//    }
//
//    @Override
//    public void onConnected(Bundle bundle) {
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
//                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
//                PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if (l != null) { }
//
//        startLocationUpdate();
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) { }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        LatLng mLocation = (new LatLng(location.getLatitude(), location.getLongitude()));
//        EventBus.getDefault().post(mLocation);
//        Toast.makeText(getApplicationContext(), String.valueOf(mLocation), Toast.LENGTH_SHORT).show();
//        GexaClient.get().sendUserLocation(String.valueOf(mLocation), new Callback<String>() {
//            @Override
//            public void onResponse(String s) {
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//            }
//        });
//
//    }
//
//    @Override
//    public void onDestroy() {
//        stopSelf();
//        super.onDestroy();
////        Intent broadcastIntent = new Intent();
////        broadcastIntent.setAction("restartservice");
////        broadcastIntent.setClass(this, Broadcast.class);
////        this.sendBroadcast(broadcastIntent);
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) { }
//
//    private void initLocationRequest() {
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(5000);
//        mLocationRequest.setFastestInterval(2000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//    }
//
//
//    private void startLocationUpdate() {
//        initLocationRequest();
//        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
//            if ((ActivityCompat.shouldShowRequestPermissionRationale((Activity) getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION))) {
//            } else {
//                ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION
//                        },
//                        REQUEST_PERMISSIONS);
//            }
//        } else {
//            boolean_permission = true;
//        }
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//    }
//
//    protected synchronized void buildGoogleApiClient() {
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addOnConnectionFailedListener(this)
//                .addConnectionCallbacks(this)
//                .addApi(LocationServices.API)
//                .build();
//    }
//}