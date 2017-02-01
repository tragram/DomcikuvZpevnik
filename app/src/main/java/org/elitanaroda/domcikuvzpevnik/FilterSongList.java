package org.elitanaroda.domcikuvzpevnik;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by Dominik on 2/1/2017.
 */

public class FilterSongList {
    private final static String TAG = "FilterSongList";
    private List<Song> mSongList;
    private EnumSet<Helper.LanguageEnum> languageEna;
    private String query;
    private onFilterDone onFilterDoneListener;

    public FilterSongList() {}

    //Vytvoření seznamu odpovídajícího hledání
    private static List<Song> filterByQuery(List<Song> songs, String query) {
        if (query != null) {
            final String niceQuery = Helper.makeTextNiceAgain(query);
            Log.i(TAG, niceQuery);
            List<Song> filteredSongList = new ArrayList<>();
            for (Song song : songs) {
                final String artist = Helper.makeTextNiceAgain(song.getmArtist());
                final String title = Helper.makeTextNiceAgain(song.getmTitle());
                if (artist.contains(niceQuery) || title.contains(niceQuery)) {
                    filteredSongList.add(song);
                }
            }
            return filteredSongList;
        } else
            return songs;
    }

    public void filter(List<Song> mSongList, EnumSet<Helper.LanguageEnum> languageEna) {
        filter(mSongList, languageEna, null);
    }

    public void filter(List<Song> mSongList, EnumSet<Helper.LanguageEnum> languageEna, String query) {
        this.mSongList = mSongList;
        this.languageEna = languageEna;
        this.query = query;
        new FilterAndReloadDatList().execute();
    }

    private List<Song> filterByLanguage(List<Song> completeList, EnumSet<Helper.LanguageEnum> languageSetFilter) {
        List<Song> filteredSongList = new ArrayList<>();
        for (Song song : completeList) {
            if (languageSetFilter.contains(song.getmLanguage())) {
                filteredSongList.add(song);
            }
        }
        return filteredSongList;
    }

    public void setOnFilterDoneListener(onFilterDone listener) {
        this.onFilterDoneListener = listener;
    }

    interface onFilterDone {
        void onFilter(List<Song> songs);
    }

    private class FilterAndReloadDatList extends AsyncTask<Void, Void, List<Song>> {
        @Override
        protected List<Song> doInBackground(Void... params) {
            return filterByQuery(filterByLanguage(mSongList, languageEna), query);
        }

        @Override
        protected void onPostExecute(List<Song> songList) {
            super.onPostExecute(songList);
            onFilterDoneListener.onFilter(songList);
        }
    }
}
