package org.elitanaroda.domcikuvzpevnik;

import android.app.ActivityManager;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;

/*
 * Created by Dominik on 1/30/2017.
 */

/**
 * Utils class contains functions to remove diacritics, check whether a particular service is running, gets a JSON from URL and also implements an updated version of the fromHtml spanned
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
     *
     * @param context      Activity context
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


    /**
     * Connects to the URL provided and downloads the JSON
     *
     * @param urlString URL where the JSON is
     * @return A JSON object created based on the data received
     * @throws IOException   Couldn't read the file
     * @throws JSONException Couln't create a JSON object
     */
    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {

        HttpURLConnection urlConnection = null;

        URL url = new URL(urlString);

        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */);
        urlConnection.setConnectTimeout(15000 /* milliseconds */);

        urlConnection.setDoOutput(true);

        urlConnection.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

        char[] buffer = new char[1024];

        String jsonString;

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        jsonString = sb.toString();
        return new JSONObject(jsonString);
    }

    /**
     * Creates a spanned from an html
     *
     * @param html HTML to parse
     * @return Spanned of the HTML
     */
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

}
