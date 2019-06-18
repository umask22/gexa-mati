package ar.gexa.app.eecc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import ar.gexa.app.eecc.views.SendLocation;

public class Broadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Broadcast Listened", "Service tried to stop");
        Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, SendLocation.class));
            Toast.makeText(context,"startBroadcast",Toast.LENGTH_SHORT).show();
        } else {
            context.startService(new Intent(context, SendLocation.class));
            Toast.makeText(context,"startBroadcastElse",Toast.LENGTH_SHORT).show();
        }
    }
}
