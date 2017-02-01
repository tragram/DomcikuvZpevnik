package org.elitanaroda.domcikuvzpevnik;

import android.app.ActivityManager;
import android.content.Context;

import java.text.Normalizer;

/**
 * Created by Dominik on 1/30/2017.
 */

public class Helper {
    public static LanguageEnum toLanguageEnum(String language) {
        try {
            return LanguageEnum.valueOf(language);
        } catch (Exception e) {
            return LanguageEnum.OTHER;
        }
    }

    public static String makeTextNiceAgain(String uglyText) {
        return Normalizer.normalize(uglyText.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public enum LanguageEnum {CZECH, ENGLISH, SLOVAK, SPANISH, OTHER}
}
