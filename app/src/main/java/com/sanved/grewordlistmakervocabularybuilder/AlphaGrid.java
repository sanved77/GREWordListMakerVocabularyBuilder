package com.sanved.grewordlistmakervocabularybuilder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Sanved on 04-08-2017.
 */

public class AlphaGrid extends AppCompatActivity {

    GridView grid;
    GridAdapter gAdapt;
    Toolbar toolbar;
    private static Tracker mTracker;

    private String[] alphabets = new String[26];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alphagrid);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setTitle("Choose an Alphabet");
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_36dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fillAlphabets();

        grid = (GridView) findViewById(R.id.gridView);

        gAdapt = new GridAdapter(this);

        grid.setAdapter(gAdapt);

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Usage")
                        .setAction("Word Added " + alphabets[i])
                        .build());

                Intent intent = new Intent(AlphaGrid.this, StartScreen.class);
                intent.putExtra("alphabet", alphabets[i]);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("AlphaGrid");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void fillAlphabets(){
        int i=0;
        char a = 'A';
        for(i=0,  a = 'A'; a <= 'Z'; a++,i++){
            alphabets[i] = ""+a;
        }
    }

}
