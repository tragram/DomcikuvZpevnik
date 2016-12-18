package org.elitanaroda.domkvzpvnk;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dominik on 06.12.2016.
 */


public class PisnickyAdapter extends
        RecyclerView.Adapter<PisnickyAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nazevTextView;
        public TextView interpretTextView;


        public ViewHolder(View itemView) {
            super(itemView);
            nazevTextView = (TextView) itemView.findViewById(R.id.nazev);
            interpretTextView = (TextView) itemView.findViewById(R.id.interpret);
        }
    }

    private ArrayList<Pisnicka> mPisnicky;
    private Context mContext;

    public PisnickyAdapter(Context context,ArrayList<Pisnicka> pisnicky)
    {
        this.mPisnicky=pisnicky;
        this.mContext=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context=parent.getContext();
        LayoutInflater inflater=LayoutInflater.from(context);

        View pisnickaView=inflater.inflate(R.layout.item_pisnicka,parent,false);

        ViewHolder viewHolder=new ViewHolder(pisnickaView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pisnicka pisnicka=mPisnicky.get(position);

        TextView nazev=holder.nazevTextView;
        nazev.setText(pisnicka.getmNazev());
        TextView interpret=holder.interpretTextView;
        interpret.setText(pisnicka.getmInterpret());
    }

    @Override
    public int getItemCount() {
        return mPisnicky.size();
    }
}
