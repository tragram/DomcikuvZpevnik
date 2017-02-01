package org.elitanaroda.domcikuvzpevnik;

import android.util.Log;

import java.util.Comparator;
import java.util.Objects;

/**
 * Created by Dominik on 2/1/2017.
 */

public class SongComparator<Song> implements Comparator<Song> {
    public final int ID;

    public SongComparator(int id) {
        this.ID = id;
    }

    public int compare(Song o1, Song o2) {
        Log.e("SongComparator", "compare not overriden!");
        return 1;
    }
}
