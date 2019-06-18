package ar.gexa.app.eecc.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import ar.gexa.app.eecc.utils.AndroidUtils;

public class FCMTokenRefreshService extends IntentService {

    public FCMTokenRefreshService() {
        super("FCMTokenRefreshService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
            AndroidUtils.saveValueToPreferences("token", "", this);
            FirebaseInstanceId.getInstance().getToken();
        } catch (Exception e) {
            NotificationService.getInstance().txtLog(FCMTokenRefreshService.class.getSimpleName()+e.getMessage());
            Log.e("FCMTokenRefreshService", e.getMessage(), e);
        }
    }
}
