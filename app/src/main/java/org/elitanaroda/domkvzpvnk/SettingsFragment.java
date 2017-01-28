package org.elitanaroda.domkvzpvnk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.lang.reflect.Array;
import java.util.List;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final String TAG = "SettingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

        Preference button = findPreference("plsDownloadAll");
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                List<Song> songList = ((MainActivity) getActivity()).getmSongList();
                Song[] songArray = songList.toArray(new Song[songList.size()]);

                Intent serviceIntent = new Intent(getActivity(), DownloadSongIntentService.class);
                serviceIntent.putExtra(PDFActivity.SONG_ARRAY_KEY, songArray).putExtra("openPDF", false);
                getActivity().startService(serviceIntent);
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
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (!sharedPref.getBoolean("keepFiles", true)) {
                try {
                    for (File file : getActivity().getFilesDir().listFiles()) {
                        if (!file.isDirectory())
                            file.delete();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }
}