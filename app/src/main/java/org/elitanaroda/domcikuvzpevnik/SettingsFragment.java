package org.elitanaroda.domcikuvzpevnik;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.List;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final String TAG = "SettingsFragment";
    Preference downloadAll;
    CheckBoxPreference keepFiles;
    private SharedPreferences sharedPref;
    private Context mContext;

    public static void downloadAllSongs(Context context, List<Song> songList) {
        Song[] songArray = songList.toArray(new Song[songList.size()]);
        Intent serviceIntent = new Intent(context, DownloadSongIntentService.class);
        serviceIntent.putExtra(PDFActivity.SONG_ARRAY_KEY, songArray).putExtra("openPDF", false);
        context.startService(serviceIntent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        downloadAll = findPreference("plsDownloadAll");
        keepFiles = (CheckBoxPreference) findPreference("keepFiles");
        downloadAll.setEnabled(keepFiles.isChecked());

        downloadAll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                downloadAllSongs(mContext, ((MainActivity) getActivity()).getmSongList());
                return true;
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(Color.WHITE);
        return view;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG, "PreferenceChanged!");
        if (key.equals("keepFiles")) {
            if (!keepFiles.isChecked()) {
                if (Helper.isMyServiceRunning(mContext, DownloadSongIntentService.class)) {
                    Intent localIntent = new Intent(DownloadSongIntentService.BROADCAST_STOP_BATCH_DOWNLOAD);
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(localIntent);
                }
                downloadAll.setEnabled(false);
                try {
                    for (File file : mContext.getFilesDir().listFiles()) {
                        if (!file.isDirectory())
                            file.delete();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            } else
                downloadAll.setEnabled(true);
        }
    }
}