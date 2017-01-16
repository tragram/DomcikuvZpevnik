package org.elitanaroda.domkvzpvnk;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.numberprogressbar.NumberProgressBar;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DownloadingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DownloadingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DownloadingFragment extends DialogFragment {
    private OnFragmentInteractionListener mListener;

    public DownloadingFragment() {
        // Required empty public constructor
    }

    public static DownloadingFragment newInstance() {
        return new DownloadingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, 0);
        setCancelable(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.progressbar_overlay, container, false);
        NumberProgressBar numberProgressBar = (NumberProgressBar) v.findViewById(R.id.number_progress_bar);
        return v;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
