package ar.gexa.app.eecc.views;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.models.User;
import ar.gexa.app.eecc.repository.UserRepository;

public class MainView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        final TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (Build.VERSION.SDK_INT >= 23) {
                            checkPermissions();
                        }else {
                            goToNexActivity();
                        }
                    }
                });
            }
        };
        final Timer timer = new Timer();
        timer.schedule(timerTask, 3000);

    }
    public void goToNexActivity(){
        final User user = UserRepository.getInstance().find();
        if (user == null) {
            final Intent intent = new Intent(MainView.this, BootstrapView.class);
            intent.putExtra("isSynchronizationInProgress", false);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else {
            final Intent intent = new Intent(MainView.this, MenuView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == 1)
            checkPermissions();
    }


    private String[] getRequiredPermissions() {
        String[] permissions = null;
        try {
            permissions = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_PERMISSIONS).requestedPermissions;
        } catch (Exception e) {
            Log.e("", e.getMessage(), e);
        }
        return permissions == null ? new String[0] : permissions.clone();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        String[] ungrantedPermissions = requiredPermissionsStillNeeded();
        if (ungrantedPermissions.length == 0 || ungrantedPermissions.length == 2) {
            goToNexActivity();
        } else {
            requestPermissions(ungrantedPermissions, 1);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private String[] requiredPermissionsStillNeeded() {
        final Set<String> permissions = new HashSet<>(Arrays.asList(getRequiredPermissions()));

        for (Iterator<String> i = permissions.iterator(); i.hasNext();) {
            String permission = i.next();
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d("", "Permission: " + permission + " already granted.");
                i.remove();
            } else {
                Log.d("","Permission: " + permission + " not yet granted.");
            }
        }
        return permissions.toArray(new String[permissions.size()]);
    }
}