package org.elitanaroda.domcikuvzpevnik;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.numberprogressbar.NumberProgressBar;

public class DownloadDialogFragment extends DialogFragment {
    private static final String TAG = "DownloadDialogFragment";
    private NumberProgressBar numberProgressBar;

    public DownloadDialogFragment() {}

    public static DownloadDialogFragment newInstance() {
        return new DownloadDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, getTheme());
        setCancelable(false);
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
        View v = inflater.inflate(R.layout.download_dialogfragment, container, false);
        numberProgressBar = (NumberProgressBar) v.findViewById(R.id.number_progress_bar);
        return v;
    }
}

