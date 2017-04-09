package org.elitanaroda.domcikuvzpevnik;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/*
 * Created by Dominik on 2/1/2017.
 */

/**
 * Filters the list according to the settings and query (if provided).
 */

public class FilterSongList {
    private final static String TAG = "FilterSongList";
    private List<Song> mSongList;
    private EnumSet<LanguageManager.LanguageEnum> languageEna;
    private String query;
    private onFilterDone onFilterDoneListener;

    /**
     * Instantiates a new Filter song list.
     */
    public FilterSongList() {}

    /**
     * Filters out the songs not matching the query
     *
     * @param songs List to filter
     * @param query The string to look for in the artist and the title fields
     * @return Filtered list of songs
     */
    private static List<Song> filterByQuery(List<Song> songs, String query) {
        if (query != null) {
            final String niceQuery = Utils.makeTextNiceAgain(query);
            Log.i(TAG, niceQuery);
            List<Song> filteredSongList = new ArrayList<>();
            for (Song song : songs) {
                final String artist = Utils.makeTextNiceAgain(song.getmArtist());
                final String title = Utils.makeTextNiceAgain(song.getmTitle());
                if (artist.contains(niceQuery) || title.contains(niceQuery)) {
                    filteredSongList.add(song);
                }
            }
            return filteredSongList;
        } else
            return songs;
    }

    /**
     * When no query is provided, uses the previous query
     *
     * @param mSongList   A list of Songs to go through
     * @param languageEna Languages to keep
     */
    public void filter(List<Song> mSongList, EnumSet<LanguageManager.LanguageEnum> languageEna) {
        filter(mSongList, languageEna, query);
    }

    /**
     * Filter.
     *
     * @param mSongList   A list of Songs to go through
     * @param languageEna Languages to keep
     * @param query       The string to look for in the artist and the title fields
     */
    public void filter(List<Song> mSongList, EnumSet<LanguageManager.LanguageEnum> languageEna, String query) {
        this.mSongList = mSongList;
        this.languageEna = languageEna;
        this.query = query;
        new FilterAndReloadDatList().execute();
    }

    /**
     * Filters out Songs not matching the langauge settings
     * @param completeList A list of Songs to filter
     * @param languageSetFilter Languages to be kept
     * @return A filtered list of Songs according to the language settings
     */
    private List<Song> filterByLanguage(List<Song> completeList, EnumSet<LanguageManager.LanguageEnum> languageSetFilter) {
        List<Song> filteredSongList = new ArrayList<>();
        for (Song song : completeList) {
            if (languageSetFilter.contains(song.getmLanguage())) {
                filteredSongList.add(song);
            }
        }
        return filteredSongList;
    }

    /**
     * Allows for an activity to register for a message when the background proccess ends
     *
     * @param listener Called when the background task is finished
     */
    public void setOnFilterDoneListener(onFilterDone listener) {
        this.onFilterDoneListener = listener;
    }

    /**
     * The interface On filter done.
     */
    interface onFilterDone {
        /**
         * On filter.
         *
         * @param songs the songs
         */
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
            if (onFilterDoneListener != null)
                onFilterDoneListener.onFilter(songList);
        }
    }
}
