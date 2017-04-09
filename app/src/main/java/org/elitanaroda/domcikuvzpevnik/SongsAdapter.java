package org.elitanaroda.domcikuvzpevnik;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Comparator;
import java.util.List;

/**
 * Adapter used with the MainActivity RecyclerView
 */
public class SongsAdapter extends
        RecyclerView.Adapter<SongsAdapter.ViewHolder> {
    private final String TAG = "SongsAdapter";
    private final Comparator<Song> mComparator;
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

    /**
     * Instantiates a new Songs adapter.
     *
     * @param context    the context
     * @param comparator the comparator
     */
    public SongsAdapter(Context context, Comparator<Song> comparator) {
        //this.mSongs = songs;
        this.mContext = context;
        this.mComparator = comparator;
        mSongs = new SortedList<>(Song.class, mCallback);
    }

    /**
     * Sets on item click listener.
     *
     * @param listener the listener
     */
//Define the method that allows the parent activity or fragment to define the listener
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

    //Inicialization of each "line" within the RView
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song song = mSongs.get(position);

        TextView title = holder.titleTextView;
        title.setText(song.getmTitle());
        TextView artist = holder.artistTextView;
        artist.setText(song.getmArtist());
        //ImageButton YTButton = holder.YTButton;

        //TODO: Při stažení nebo smazání upravit stav
        if (song.ismIsOnLocalStorage())
            holder.checkmarkIV.setVisibility(View.VISIBLE);
        else
            holder.checkmarkIV.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    //Zaplnění seznamu, přidá jen neexistující

    /**
     * Add to the list all the songs not included yet
     * Doesn't create duplicates
     *
     * @param songs Songs to be included
     */
    public void add(List<Song> songs) {
        mSongs.addAll(songs);
    }

    /**
     * Removes provided songs from the list
     *
     * @param songs Songs to be removed
     */
    public void remove(List<Song> songs) {
        mSongs.beginBatchedUpdates();
        for (Song song : songs) {
            mSongs.remove(song);
        }
        mSongs.endBatchedUpdates();
    }

    /**
     * Efficiently replace the old list with the new
     *
     * @param songs The new list of Songs to be shown
     */
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

    /**
     * The interface On item click listener.
     */
// Define the listener interface
    public interface OnItemClickListener {
        /**
         * On item click.
         *
         * @param itemView the item view
         * @param song     the song
         */
        void onItemClick(View itemView, Song song);
    }

    /**
     * The type View holder.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * The Title text view.
         */
        public TextView titleTextView;
        /**
         * The Artist text view.
         */
        public TextView artistTextView;
        /**
         * The Yt button.
         */
        public ImageButton YTButton;
        /**
         * The Checkmark iv.
         */
        public ImageView checkmarkIV;

        /**
         * Instantiates a new View holder.
         *
         * @param songItemView the song item view
         */
        public ViewHolder(final View songItemView) {
            super(songItemView);
            this.titleTextView = (TextView) songItemView.findViewById(R.id.title);
            this.artistTextView = (TextView) songItemView.findViewById(R.id.artist);
            this.YTButton = (ImageButton) songItemView.findViewById(R.id.YTButton);
            this.checkmarkIV = (ImageView) songItemView.findViewById(R.id.checkmarkImage);

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

            songItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(songItemView, mSongs.get(position));
                        }
                    }
                }
            });
        }
    }
}
