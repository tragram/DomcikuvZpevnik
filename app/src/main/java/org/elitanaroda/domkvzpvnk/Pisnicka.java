package org.elitanaroda.domkvzpvnk;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Dominik on 10.12.2016.
 */

public class Pisnicka {
    private String mNazev;
    private String mInterpret;

    public Pisnicka(String nazev, String interpret) {
        this.mNazev = nazev;
        this.mInterpret = interpret;
    }

    public String getmNazev() {
        return mNazev;
    }

    public String getmInterpret() {
        return mInterpret;
    }
}
