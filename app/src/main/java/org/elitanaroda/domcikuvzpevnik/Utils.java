package org.elitanaroda.domcikuvzpevnik;

import android.app.ActivityManager;
import android.content.Context;

import java.text.Normalizer;

/**
 * Created by Dominik on 1/30/2017.
 */

public class Utils {
    private Utils() {}

    /**
     * Lowercase and removes all diacritical marks
     *
     * @param uglyText A task to perform the method on
     * @return A lowercase text without diacritical marks
     */
    public static String makeTextNiceAgain(String uglyText) {
        return Normalizer.normalize(uglyText.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /**
     * Checks whether a service is running
     * @param context Activity context
     * @param serviceClass Service to look for
     * @return True if the process is running
     */
    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
