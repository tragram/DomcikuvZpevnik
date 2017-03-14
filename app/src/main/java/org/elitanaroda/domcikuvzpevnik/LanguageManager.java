package org.elitanaroda.domcikuvzpevnik;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Dominik on 2/3/2017.
 * Class used for managing languages in use to sort the list and saving/loading the settings
 */

public class LanguageManager {
    private static final String TAG = "LanguageManager";
    private Context mContext;

    public LanguageManager(Context context) {this.mContext = context;}

    /**
     * Converts a string (e.g. from the DB) to an enum
     *
     * @param language The string to convert
     * @return The corresponding enum. Note: On unknown string returns "OTHER"
     */
    private static LanguageEnum toLanguageEnum(String language) {
        try {
            return LanguageEnum.valueOf(language);
        } catch (Exception e) {
            return LanguageEnum.OTHER;
        }
    }

    public EnumSet<LanguageEnum> getLanguagePreferences() {return loadLanguagePreferences();}

    public void saveLanguages(EnumSet<LanguageEnum> languagesToSave) {saveLanguagePreferences(languagesToSave);}

    private EnumSet<LanguageEnum> loadLanguagePreferences() {
        SharedPreferences languagePreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Set<String> languagePreferencesStringSet = languagePreferences.getStringSet(mContext.getString(R.string.language_settings), null);
        EnumSet<LanguageEnum> languageSettings = EnumSet.noneOf(LanguageEnum.class);
        if (languagePreferencesStringSet == null)
            languageSettings = EnumSet.allOf(LanguageEnum.class);
        else {
            for (String language : languagePreferencesStringSet) {
                languageSettings.add(toLanguageEnum(language));
            }
        }
        return languageSettings;
    }

    private void saveLanguagePreferences(EnumSet<LanguageEnum> mCurrentLanguageSettings) {
        Set<String> stringSet = new HashSet<>();
        for (LanguageEnum language : mCurrentLanguageSettings) {
            stringSet.add(language.toString());
        }
        Log.i(TAG, stringSet.toString());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(mContext.getString(R.string.language_settings), stringSet);
        editor.apply();
    }

    public enum LanguageEnum {CZECH, ENGLISH, SLOVAK, SPANISH, OTHER}
}
