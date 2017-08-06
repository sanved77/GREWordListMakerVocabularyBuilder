package com.sanved.grewordlistmakervocabularybuilder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by Sanved on 21-05-2016.
 */


public class GridAdapter extends BaseAdapter {
    private Context context;
    private final String[] alphabets = new String[26];


    public GridAdapter(Context context) {
        this.context = context;
        fillAlphabets();
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {

            gridView = new View(context);

            // get layout from mobile.xml
            gridView = inflater.inflate(R.layout.grid_item, null);

            // set value into textview
            TextView textView = (TextView) gridView
                    .findViewById(R.id.tvAlphabet);
            textView.setText(alphabets[position]);


        } else {
            gridView = (View) convertView;
        }

        return gridView;
    }

    @Override
    public int getCount() {
        return alphabets.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void fillAlphabets(){
        int i=0;
        char a = 'A';
        for(i=0,  a = 'A'; a <= 'Z'; a++,i++){
            alphabets[i] = ""+a;
        }
    }

}