package org.elitanaroda.domkvzpvnk;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Dominik on 06.12.2016.
 */


public class SongsAdapter extends
        RecyclerView.Adapter<SongsAdapter.ViewHolder> {

    private final String TAG = "SongsAdapter";
    private final Comparator<Song> mComparator;
    //TODO: Improve these methods
    private final SortedList.Callback<Song> mCallback = new SortedList.Callback<Song>() {
        @Override
        public boolean areContentsTheSame(Song oldItem, Song newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public int compare(Song o1, Song o2) {
            return mComparator.compare(o1, o2);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyDataSetChanged();
        }

        @Override
        public boolean areItemsTheSame(Song item1, Song item2) {
            return item1.getmId() == item2.getmId();
        }

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }
    };
    private Context mContext;
    private OnItemClickListener listener;
    private SortedList<Song> mSongs;

    public SongsAdapter(Context context, Comparator<Song> comparator) {
        //this.mSongs = songs;
        this.mContext = context;
        this.mComparator = comparator;
        mSongs = new SortedList<>(Song.class, mCallback);
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View songView = inflater.inflate(R.layout.item_song, parent, false);
        ViewHolder viewHolder = new ViewHolder(songView);
        return viewHolder;
    }

    //Inicializace jednotlivých řádků
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song song = mSongs.get(position);

        TextView title = holder.titleTextView;
        title.setText(song.getmTitle());
        TextView artist = holder.artistTextView;
        artist.setText(song.getmArtist());
        //ImageButton YTButton = holder.YTButton;

        //TODO: Při stažení nebo smazání upravit stav
        if (new File(mContext.getFilesDir().getAbsolutePath() + File.separatorChar + song.getFileName()).isFile())
            holder.checkmarkIV.setVisibility(View.VISIBLE);
        else
            holder.checkmarkIV.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    //Zaplnění seznamu, přidá jen neexistující
    public void add(List<Song> songs) {
        mSongs.addAll(songs);
    }

    public void remove(List<Song> songs) {
        mSongs.beginBatchedUpdates();
        for (Song song : songs) {
            mSongs.remove(song);
        }
        mSongs.endBatchedUpdates();
    }

    //Odstraní již neexistující a přidá nové
    public void replaceAll(List<Song> songs) {
        mSongs.beginBatchedUpdates();
        for (int i = mSongs.size() - 1; i >= 0; i--) {
            final Song song = mSongs.get(i);
            if (!songs.contains(song)) {
                mSongs.remove(song);
            }
        }
        mSongs.addAll(songs);
        mSongs.endBatchedUpdates();
    }

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, Song song);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView artistTextView;
        public ImageButton YTButton;
        public ImageView checkmarkIV;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.titleTextView = (TextView) itemView.findViewById(R.id.title);
            this.artistTextView = (TextView) itemView.findViewById(R.id.artist);
            this.YTButton = (ImageButton) itemView.findViewById(R.id.YTButton);
            this.checkmarkIV = (ImageView) itemView.findViewById(R.id.checkmarkImage);

            YTButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(YTButton, mSongs.get(position));
                        }
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, mSongs.get(position));
                        }
                    }
                }
            });
        }
    }
}
