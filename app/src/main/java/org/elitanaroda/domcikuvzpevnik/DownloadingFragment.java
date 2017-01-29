package org.elitanaroda.domcikuvzpevnik;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.numberprogressbar.NumberProgressBar;

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

