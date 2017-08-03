package com.sanved.grewordlistmakervocabularybuilder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

/**
 * Created by Sanved on 03-08-2017.
 */

public class RVAdapt extends RecyclerView.Adapter<RVAdapt.DataHolder> {

    ArrayList<WordData> list;
    static ArrayList<WordData> list2;

    static Tracker mTracker;
    Context context;

    RVAdapt(ArrayList<WordData> list, Context context){
        this.list = list;
        list2 = list;
        this.context = context;
        AnalyticsApplication application = (AnalyticsApplication) context;
        mTracker = application.getDefaultTracker();
    }

    public static class DataHolder extends RecyclerView.ViewHolder {

        TextView word, sentence;
        LinearLayout cv;

        DataHolder(final View v) {
            super(v);
            word = (TextView) v.findViewById(R.id.tvWord);
            sentence = (TextView) v.findViewById(R.id.tvSentence);
            cv = (LinearLayout) v.findViewById(R.id.cvList);
            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Word")
                            .setAction(list2.get(getAdapterPosition()).getWord())
                            .build());


                }

            });
        }

    }

    @Override
    public DataHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        DataHolder dh = new DataHolder(v);
        return dh;
    }

    @Override
    public void onBindViewHolder(DataHolder holder, int position) {
        holder.word.setText(list.get(position).getWord());
        holder.sentence.setText(list.get(position).getSentence());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
