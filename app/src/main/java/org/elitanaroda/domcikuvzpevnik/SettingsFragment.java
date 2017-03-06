package org.elitanaroda.domcikuvzpevnik;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
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
    private OnDeleteSongs onDeleteSongsListener;

    public static void downloadAllSongs(Context context, List<Song> songList) {
        Song[] songArray = songList.toArray(new Song[songList.size()]);
        Intent serviceIntent = new Intent(context, MoreSongsDownloadIS.class);
        serviceIntent.putExtra(PDFActivity.SONG_ARRAY_KEY, songArray).putExtra("openPDF", false);
        context.startService(serviceIntent);
    }

    public void setOnDeleteSongsListener(OnDeleteSongs listener) {this.onDeleteSongsListener = listener;}

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
                ConnectivityManager cm = ((ConnectivityManager) mContext.getSystemService(Activity.CONNECTIVITY_SERVICE));
                if (cm.isActiveNetworkMetered()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Are you sure?");
                    builder.setMessage("You are not connected to WiFi. \nThis will action consume 290MB! \nAre you sure? ");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            downloadAllSongs(mContext, ((MainActivity) getActivity()).getmSongList());
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    // Set other dialog properties

                    // Create the AlertDialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    downloadAllSongs(mContext, ((MainActivity) getActivity()).getmSongList());
                }
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null)
            view.setBackgroundColor(Color.WHITE);
        return view;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("keepFiles")) {
            //if its unchecked, we need to stop downloading, delete the files and also grey out the other button
            if (!keepFiles.isChecked()) {
                if (Helper.isMyServiceRunning(mContext, MoreSongsDownloadIS.class)) {
                    Intent localIntent = new Intent(MoreSongsDownloadIS.BROADCAST_STOP_BATCH_DOWNLOAD);
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(localIntent);
                }
                downloadAll.setEnabled(false);
                try {
                    for (File file : mContext.getFilesDir().listFiles()) {
                        if (!file.isDirectory())
                            file.delete();
                    }
                    if (onDeleteSongsListener != null) {
                        onDeleteSongsListener.reloadSongs();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            } else
                downloadAll.setEnabled(true);
        }
    }

    interface OnDeleteSongs {
        void reloadSongs();
    }
}