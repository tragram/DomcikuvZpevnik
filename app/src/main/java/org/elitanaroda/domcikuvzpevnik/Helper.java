package org.elitanaroda.domcikuvzpevnik;

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

    public enum LanguageEnum {CZECH, ENGLISH, SLOVAK, SPANISH, OTHER}
}
