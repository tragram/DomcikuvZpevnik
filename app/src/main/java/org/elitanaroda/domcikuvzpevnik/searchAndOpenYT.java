package org.elitanaroda.domcikuvzpevnik;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.google.android.youtube.player.YouTubeIntents;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.List;

/*
 * Created by Dominik on 2/2/2017.
 */

/**
 * Class used for showing the first video on the query provided
 */

public class SearchAndOpenYT {
    private static final String YOUTUBE_DATA_API_KEY = "AIzaSyCcGkkc3xOCvUfo-4VTejHq5QrZh0qV90U";
    private static final String TAG = "SearchAndOpenYT";
    private YouTube youtube;
    private Context mContext;

    /**
     * Instantiates a new Search and open yt.
     *
     * @param context the context
     */
    public SearchAndOpenYT(Context context) {this.mContext = context;}

    /**
     * Open youtube video.
     *
     * @param songToLookUp the song to look up
     */
    public void openYoutubeVideo(Song songToLookUp) {
        new openYoutube().execute(songToLookUp.getmArtist() + " - " + songToLookUp.getmTitle());
    }
    private class openYoutube extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                // This object is used to make YouTube Data API requests
                youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                    public void initialize(HttpRequest request) throws IOException {
                    }
                }).setApplicationName(mContext.getPackageName()).build();

                // Define the API request for retrieving search results.
                YouTube.Search.List search = youtube.search().list("id");
                search.setKey(YOUTUBE_DATA_API_KEY)
                        .setQ(params[0])
                        .setType("video")
                        .setFields("items(id/videoId)")
                        .setMaxResults((long) 1);

                // Call the API
                SearchListResponse searchResponse = search.execute();
                List<SearchResult> searchResultList = searchResponse.getItems();
                return searchResultList.get(0).getId().getVideoId();

            } catch (GoogleJsonResponseException e) {
                Log.e(TAG, "There was a service error: " + e.getDetails().getCode() + " : "
                        + e.getDetails().getMessage());
            } catch (IOException e) {
                Log.e(TAG, "There was an IO error: " + e.getCause() + " : " + e.getMessage());
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String videoID) {
            super.onPostExecute(videoID);
            if (videoID != null && YouTubeIntents.isYouTubeInstalled(mContext))
                mContext.startActivity(YouTubeIntents.createPlayVideoIntentWithOptions(mContext, videoID, true, true)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            else {
                Snackbar snackbar = Snackbar.make(((Activity) mContext).findViewById(android.R.id.content),
                        "Couldn't load the YouTube App", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }
}
