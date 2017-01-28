package org.elitanaroda.domkvzpvnk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.numberprogressbar.NumberProgressBar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadingFragment extends Fragment {
    private static final String TAG = "DownloadingFragment";
    private NumberProgressBar numberProgressBar;

    public DownloadingFragment() {}

    public static DownloadingFragment newInstance() {
        return new DownloadingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter fileDownloadedFilter =
                new IntentFilter(DownloadSongIntentService.BROADCAST_PROGRESS_UPDATE);
        LocalBroadcastManager.getInstance(getActivity()).
                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle args = intent.getExtras();
                        numberProgressBar.setProgress(args.getInt("progress"));
                    }
                }, fileDownloadedFilter);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.downloading_fragment, container, false);
        numberProgressBar = (NumberProgressBar) v.findViewById(R.id.number_progress_bar);
        return v;
    }
}

