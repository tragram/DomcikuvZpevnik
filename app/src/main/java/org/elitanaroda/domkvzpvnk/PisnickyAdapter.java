package org.elitanaroda.domkvzpvnk;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik on 06.12.2016.
 */


public class PisnickyAdapter extends
        RecyclerView.Adapter<PisnickyAdapter.ViewHolder> {

    private ArrayList<Pisnicka> mPisnicky;
    private Context mContext;


    private OnItemClickListener listener;

    public PisnickyAdapter(Context context, ArrayList<Pisnicka> pisnicky) {
        this.mPisnicky = pisnicky;
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

        View pisnickaView = inflater.inflate(R.layout.item_pisnicka, parent, false);

        ViewHolder viewHolder = new ViewHolder(pisnickaView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pisnicka pisnicka = mPisnicky.get(position);

        TextView nazev = holder.nazevTextView;
        nazev.setText(pisnicka.getmNazev());
        TextView interpret = holder.interpretTextView;
        interpret.setText(pisnicka.getmInterpret());
    }

    @Override
    public int getItemCount() {
        return mPisnicky.size();
    }

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nazevTextView;
        public TextView interpretTextView;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.nazevTextView = (TextView) itemView.findViewById(R.id.nazev);
            this.interpretTextView = (TextView) itemView.findViewById(R.id.interpret);

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
