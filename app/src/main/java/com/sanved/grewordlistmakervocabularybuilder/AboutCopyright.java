package com.sanved.grewordlistmakervocabularybuilder;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Sanved on 03-08-2017.
 */

/*
*  Just a normal About Page with nothing fancy.
*
*  A button to the GitHub repo is the only active element on the page.
*
*/
public class AboutCopyright extends AppCompatActivity {

    Tracker mTracker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        Button github = (Button) findViewById(R.id.bGit);

        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Github")
                        .setAction("Github Opened " + Build.DEVICE)
                        .build());
                String url = "https://github.com/sanved77/GREWordListMakerVocabularyBuilder";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        ImageButton back = (ImageButton) findViewById(R.id.ibBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("StartScreen");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
