package com.sanved.grewordlistmakervocabularybuilder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

public class StartScreen extends AppCompatActivity {

    RecyclerView rv;
    Toolbar toolbar;
    LinearLayoutManager llm;
    ArrayList<WordData> list;
    private static Tracker mTracker;
    RVAdapt adapt;
    FloatingActionButton addWord;
    SQLiteHelper db;
    TextView tvPlaceHolder;
    SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);

        initVals();

        fillData();

        new DBSniffer().execute(this);

    }

    public void initVals() {

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setTitle("Word List");

        rv = (RecyclerView) findViewById(R.id.rv);
        llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        list = new ArrayList<>();

        addWord = (FloatingActionButton) findViewById(R.id.fabAdd);
        addWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addWordStart();
            }
        });

        db = new SQLiteHelper(this);

        tvPlaceHolder = (TextView) findViewById(R.id.tvPlaceHolder);

        swipe = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Usage")
                        .setAction("Refresh")
                        .build());

                list.clear();
                adapt.notifyDataSetChanged();
                new DBSniffer().execute(StartScreen.this);
                swipe.setRefreshing(false);

            }
        });
    }

    public void fillData() {


        WordData wd = new WordData(1, "misanthrope", "Being an misanthrope doesn't really mean that the person is an introvert");
        WordData wd2 = new WordData(1, "ubiquitous", "The greed for money seems to be ubiquitous");
        WordData wd3 = new WordData(1, "polyglot ", "Polyglots often tend to have a huge memory");

        db.addWord(wd);
        db.addWord(wd2);
        db.addWord(wd3);


    }

    class DBSniffer extends AsyncTask<Context, Void, Void> {

        private ProgressDialog progressDialog = new ProgressDialog(StartScreen.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            tvPlaceHolder.setVisibility(View.GONE);
            progressDialog.setTitle("Loading all the Words");
            progressDialog.setCancelable(false);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    tvPlaceHolder.setVisibility(View.VISIBLE);
                    tvPlaceHolder.setText("Pull Down to Refresh");
                }
            });

        }

        @Override
        protected Void doInBackground(Context... contexts) {

            list = db.allWords();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            this.progressDialog.dismiss();

            if (list.isEmpty()) {
                tvPlaceHolder.setVisibility(View.VISIBLE);
                tvPlaceHolder.setText("No words yet");
            } else {
                Context con = StartScreen.this.getApplication();
                adapt = new RVAdapt(list, con);
                rv.setAdapter(adapt);
            }

        }
    }

    public void addWordStart(){
        Intent i = new Intent(StartScreen.this, AddWord.class);
        startActivityForResult(i, 69);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 69){
            list.clear();
            adapt.notifyDataSetChanged();
            new DBSniffer().execute(StartScreen.this);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                Intent i = new Intent(StartScreen.this, AboutCopyright.class);
                startActivity(i);
                break;

            case R.id.mrefresh:
                list.clear();
                adapt.notifyDataSetChanged();
                new DBSniffer().execute(StartScreen.this);
                break;

            case R.id.madd:
                addWordStart();
                break;

            case R.id.bug:
                AlertDialog.Builder build2 = new AlertDialog.Builder(StartScreen.this);
                LayoutInflater inflater2 = this.getLayoutInflater();
                final View dialogView2 = inflater2.inflate(R.layout.dialog, null);

                final EditText bug2 = (EditText) dialogView2.findViewById(R.id.etBug);

                build2
                        .setTitle("Report Bugs")
                        .setView(dialogView2)
                        .setMessage("What was the bug/error ?")
                        .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(Intent.ACTION_SEND);
                                i.setType("message/rfc822");
                                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"sanved77@gmail.com"});
                                i.putExtra(Intent.EXTRA_SUBJECT, "GRE Word List: Error Bugs");
                                i.putExtra(Intent.EXTRA_TEXT, "I found a bug in the app. The bug is - " + bug2.getText().toString());
                                try {
                                    startActivity(Intent.createChooser(i, "Send email...."));
                                } catch (android.content.ActivityNotFoundException ex) {
                                    Toast.makeText(StartScreen.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false);

                build2.create().show();
                break;
            case R.id.rate:
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Rate")
                        .setAction("App Rated")
                        .build());
                String url = "https://play.google.com/store/apps/details?id=" + getPackageName();
                Intent i2 = new Intent(Intent.ACTION_VIEW);
                i2.setData(Uri.parse(url));
                startActivity(i2);
                break;

            case R.id.mshare:
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Share")
                        .setAction("App Shared")
                        .build());
                String shareBody = "Hey, check out this app - GRE Word List Maker. It's helps in creating a word list.\nhttps://play.google.com/store/apps/details?id=" + getPackageName();
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share using"));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("StartScreen");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.deleteAll();
    }
}
