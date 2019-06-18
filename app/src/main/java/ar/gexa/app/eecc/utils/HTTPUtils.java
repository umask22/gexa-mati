package ar.gexa.app.eecc.utils;

import android.util.Log;

public class HTTPUtils {

    public static boolean ping(String ip) {
        final Runtime runtime = Runtime.getRuntime();
        try {
            final Process  process = runtime.exec("/system/bin/ping -c 1 -w 1 " + ip);
            return process.waitFor() == 0;
        }
        catch (Exception e) {
            Log.e(HTTPUtils.class.getSimpleName(), e.getMessage(), e);
        }
        return false;
    }
}