package org.elitanaroda.domcikuvzpevnik;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.Normalizer;

/**
 * Created by Dominik on 2/3/2017.
 * Class used for managing comparators in use to sort the list and saving/loading the settings
 */

public class ComparatorManager {
    //Sorts the list alphabetically by title
    public static final SongComparator<Song> ALPHABETICAL_BY_TITLE = new SongComparator<Song>(0) {
        @Override
        public int compare(Song o1, Song o2) {
            return Normalizer.normalize(o1.getmTitle(), Normalizer.Form.NFD).
                    compareTo(Normalizer.normalize(o2.getmTitle(), Normalizer.Form.NFD));
        }
    };
    //Sorts the list alphabetically by artist
    public static final SongComparator<Song> ALPHABETICAL_BY_ARTIST = new SongComparator<Song>(1) {
        @Override
        public int compare(Song o1, Song o2) {
            return Normalizer.normalize(o1.getmArtist(), Normalizer.Form.NFD).
                    compareTo(Normalizer.normalize(o2.getmArtist(), Normalizer.Form.NFD));
        }
    };

    //Sorts the list by date, newest first
    //On same date, sorts it alphabetically by title
    public static final SongComparator<Song> BY_DATE = new SongComparator<Song>(2) {
        @Override
        public int compare(Song o1, Song o2) {
            int difference = o2.getmDateAdded() - o1.getmDateAdded();
            if (difference != 0)
                return difference;
            else
                return ALPHABETICAL_BY_TITLE.compare(o1, o2);
        }
    };
    private static final String TAG = "ComparatorManager";
    private Context mContext;

    public ComparatorManager(Context context) {
        this.mContext = context;
    }

    public SongComparator<Song> getComparatorPreferences() {return loadComparatorPreferences();}

    public void saveComparator(SongComparator<Song> songComparator) {saveComparatorPreferences(songComparator);}

    private void saveComparatorPreferences(SongComparator<Song> songComparator) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(mContext.getString(R.string.sort_settings), songComparator.ID);
        editor.apply();
    }

    private SongComparator<Song> loadComparatorPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        switch (sharedPref.getInt(mContext.getString(R.string.sort_settings), 0)) {
            case 0:
                return ALPHABETICAL_BY_TITLE;
            case 1:
                return ALPHABETICAL_BY_ARTIST;
            case 2:
                return BY_DATE;
            default:
                Log.e(TAG, "Unexpected value in sort_settings");
                return ALPHABETICAL_BY_TITLE;
        }
    }
}
