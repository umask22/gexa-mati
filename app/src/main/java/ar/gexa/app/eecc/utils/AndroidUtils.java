package ar.gexa.app.eecc.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class AndroidUtils {

    public interface LoopListener {
        boolean loop();
        void onFinish();
    }

    public static <V> V findViewById(int viewId, Class<V> clazz, Activity activity)  {
        return clazz.cast(activity.findViewById(viewId));
    }

    public static <V> V findViewById(int viewId, Class<V> clazz, View view)  {
        return clazz.cast(view.findViewById(viewId));
    }

    public static void saveValueToPreferences(String key, String value, Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getValueToPreferences(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

    public static void loop(final LoopListener listener) {

        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if(listener.loop())
                    loop(listener);
                else
                    listener.onFinish();
            }
        };
        final Timer timer = new Timer();
        timer.schedule(timerTask, 1500);
    }
}
