package org.elitanaroda.domkvzpvnk;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Dominik on 06.12.2016.
 */


public class SongsAdapter extends
        RecyclerView.Adapter<SongsAdapter.ViewHolder> {

    private ArrayList<Song> mSongs;
    private Context mContext;


    private OnItemClickListener listener;

    public SongsAdapter(Context context, ArrayList<Song> songs) {
        this.mSongs = songs;
        this.mContext = context;
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View pisnickaView = inflater.inflate(R.layout.item_song, parent, false);

        ViewHolder viewHolder = new ViewHolder(pisnickaView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song song = mSongs.get(position);

        TextView nazev = holder.titleTextView;
        nazev.setText(song.getmTitle());
        TextView interpret = holder.artistTextView;
        interpret.setText(song.getmArtist());
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView artistTextView;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.titleTextView = (TextView) itemView.findViewById(R.id.title);
            this.artistTextView = (TextView) itemView.findViewById(R.id.artist);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView);
                        }
                    }
                }
            });
        }
    }
}
