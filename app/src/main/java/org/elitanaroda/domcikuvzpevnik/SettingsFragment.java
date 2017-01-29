package org.elitanaroda.domcikuvzpevnik;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                List<Song> songList = ((MainActivity) getActivity()).getmSongList();
                Song[] songArray = songList.toArray(new Song[songList.size()]);

                Intent serviceIntent = new Intent(mContext, DownloadSongIntentService.class);
                serviceIntent.putExtra(PDFActivity.SONG_ARRAY_KEY, songArray).putExtra("openPDF", false);
                mContext.startService(serviceIntent);
                return true;
            }
        });
        mContext = getActivity();
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
                //TODO:stopnout service
                if (MainActivity.isMyServiceRunning(mContext, DownloadSongIntentService.class)) {
                    keepFiles.setChecked(true);
                    Snackbar snackbar = Snackbar.make(getActivity()
                            .findViewById(android.R.id.content), "Počkej, až se vše stáhne!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    downloadAll.setEnabled(false);
                    try {
                        for (File file : getActivity().getFilesDir().listFiles()) {
                            if (!file.isDirectory())
                                file.delete();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            } else
                downloadAll.setEnabled(true);
        }
    }
}