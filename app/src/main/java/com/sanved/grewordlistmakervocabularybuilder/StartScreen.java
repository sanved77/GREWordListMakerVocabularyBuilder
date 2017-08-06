package com.sanved.grewordlistmakervocabularybuilder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
    boolean sentFromAlpha = false;
    String alphaSentByGrid;
    Drawable addIcon;
    SharedPreferences prefs;
    SharedPreferences.Editor ed;
    TextToSpeech tts;
    Bundle params;
    HashMap<String, String> map;
    CoordinatorLayout cl1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);

        //fillData();

        // Checks if AplhaGrid has sent any alphabet to look for

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                alphaSentByGrid = null;
            } else {
                alphaSentByGrid = extras.getString("alphabet");
            }
        } else {
            alphaSentByGrid = (String) savedInstanceState.getSerializable("alphabet");
        }

        if (alphaSentByGrid != null) sentFromAlpha = true;

        initVals();


        // This one explains the use of the buttons

        new DBSniffer().execute(this);

        if (prefs.getBoolean("firstUse", true)) {

            TapTargetView.showFor(this,                 // `this` is an Activity
                    TapTarget.forView(findViewById(R.id.fabAdd), "Add Words", "Use this to add new words")
                            // All options below are optional
                            .outerCircleColor(R.color.blue)      // Specify a color for the outer circle
                            .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                            .targetCircleColor(R.color.white)   // Specify a color for the target circle
                            .titleTextSize(25)                  // Specify the size (in sp) of the title text
                            .titleTextColor(R.color.white)      // Specify the color of the title text
                            .descriptionTextSize(15)            // Specify the size (in sp) of the description text
                            .descriptionTextColor(R.color.white)  // Specify the color of the description text
                            .textColor(R.color.white)            // Specify a color for both the title and description text
                            .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                            .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                            .drawShadow(true)
                            .icon(addIcon)// Whether to draw a drop shadow or not
                            .cancelable(true)                   // Whether tapping outside the outer circle dismisses the view
                            .tintTarget(true)                   // Whether to tint the target view's color
                            .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                            .targetRadius(60),                  // Specify the target radius (in dp)
                    new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);      // This call is optional
                            //addWordStart();
                        }
                    });

            ed.putBoolean("firstUse", false);
            ed.commit();
        }
    }

    public void initVals() {

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        if(sentFromAlpha){
            getSupportActionBar().setTitle(alphaSentByGrid + "...");
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_36dp));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }else{
            getSupportActionBar().setTitle("Word List");
        }

        rv = (RecyclerView) findViewById(R.id.rv);
        llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        list = new ArrayList<>();

        rv.addOnItemTouchListener(new RecyclerItemClickListener(this, rv, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Word")
                        .setAction(list.get(position).getWord())
                        .build());

                Intent i = new Intent(StartScreen.this, WordScreen.class);
                i.putExtra("word",list.get(position).getWord());
                i.putExtra("sentence",list.get(position).getSentence());
                startActivityForResult(i, 70);
            }

            @Override
            public void onItemLongClick(View view, final int position) {

                //  Long Click Menu for each Word element.

                final CharSequence[] items = {"Open", "Edit" ,"Hear", "Meaning" ,"Delete", "Share"};

                android.app.AlertDialog.Builder diag = new android.app.AlertDialog.Builder(StartScreen.this);

                diag
                        .setTitle("Options")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch(i){
                                    case 0: // Open
                                        mTracker.send(new HitBuilders.EventBuilder()
                                                .setCategory("Word")
                                                .setAction(list.get(position).getWord())
                                                .build());

                                        Intent i2 = new Intent(StartScreen.this, WordScreen.class);
                                        i2.putExtra("word",list.get(position).getWord());
                                        i2.putExtra("sentence",list.get(position).getSentence());
                                        startActivityForResult(i2, 70);
                                        break;

                                    case 1:  // Edit
                                        addWordStart(list.get(position).getWord(),list.get(position).getSentence());
                                        break;

                                    case 2: // Pronounce
                                        mTracker.send(new HitBuilders.EventBuilder()
                                                .setCategory("Usage")
                                                .setAction("Pronounce " + list.get(position).getWord())
                                                .build());
                                        Toast.makeText(StartScreen.this, "Loading Pronunciation...", Toast.LENGTH_LONG);
                                        speakIt(list.get(position).getWord());
                                        break;

                                    case 3:  // Dictionary
                                        Intent intent = new Intent(Intent.ACTION_SEARCH);
                                        intent.setPackage("livio.pack.lang.en_US");//you can use also livio.pack.lang.en_US, livio.pack.lang.es_ES, livio.pack.lang.de_DE, livio.pack.lang.pt_BR or livio.pack.lang.fr_FR
                                        intent.putExtra(SearchManager.QUERY, list.get(position).getWord());
                                        if (isIntentAvailable(StartScreen.this, intent)) // check if intent is available ?
                                            startActivity(intent);
                                        else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(StartScreen.this);
                                            builder.setMessage("There is an amazing offline dictionary which is needed to display the meaning of the words. Download it from Play Store ?")
                                                    .setCancelable(false)
                                                    .setTitle("Download Offline Dictionary")
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {

                                                            try {
                                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=livio.pack.lang.en_US")));
                                                            } catch (android.content.ActivityNotFoundException anfe) {
                                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=livio.pack.lang.en_US")));
                                                            }
                                                        }
                                                    })
                                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                            AlertDialog alert = builder.create();
                                            alert.show();
                                        }
                                        break;

                                    case 4: // Delete
                                        AlertDialog.Builder builder2 = new AlertDialog.Builder(StartScreen.this);
                                        builder2.setMessage("Are you sure you want to Delete the Word ?")
                                                .setCancelable(false)
                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        db.deleteWord(list.get(position).getWord());
                                                        refreshList();
                                                    }
                                                })
                                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                        AlertDialog alert2 = builder2.create();
                                        alert2.show();
                                        break;

                                    case 5:  // Share
                                        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                                        whatsappIntent.setType("text/plain");
                                        whatsappIntent.setPackage("com.whatsapp");
                                        whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Word - " + list.get(position).getWord() + "\nPhrase - "
                                        + list.get(position).getSentence());
                                        try {
                                            startActivity(whatsappIntent);
                                        } catch (android.content.ActivityNotFoundException ex) {
                                            Toast.makeText(StartScreen.this, "WhatsApp Doesn't seem to be installed", Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                }
                            }
                        });
                AlertDialog alert = diag.create();
                alert.show();
            }
        }));

        cl1 = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");

        map = new HashMap<String, String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Your phone lacks speech functionality :-(");
                    }

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });

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

        addIcon = ContextCompat.getDrawable(this, R.drawable.ic_note_add_white_36dp);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ed = prefs.edit();

    }


    // Checks if the intent package is there on the phone.
    public boolean isIntentAvailable(Context context, Intent intent) {
        List<ResolveInfo> lri = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return (lri != null) && (lri.size() > 0);
    }


    // This method uses the Text-To-Speech engine on the phone.
    public void speakIt(String thingToSay){

        final Snackbar snackbar = Snackbar
                .make(cl1, Html.fromHtml("Loading Speech"), Snackbar.LENGTH_LONG);
        View snbView  = snackbar.getView();
        snbView.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });

        TextView action = (TextView) snbView.findViewById(android.support.design.R.id.snackbar_action);
        TextView normal = (TextView) snbView.findViewById(android.support.design.R.id.snackbar_text);
        action.setTextColor(ContextCompat.getColor(this, R.color.blue));
        normal.setTextColor(ContextCompat.getColor(this, R.color.black));
        snackbar.show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(thingToSay, TextToSpeech.QUEUE_FLUSH, params, "UniqueID");
        }else{
            tts.speak(thingToSay, TextToSpeech.QUEUE_FLUSH, map);
        }

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                snackbar.dismiss();
            }

            @Override
            public void onDone(String s) {

            }

            @Override
            public void onError(String s) {

                Toast.makeText(StartScreen.this, "Error !", Toast.LENGTH_SHORT).show();
            }
        });

    }


    // A stub made to test the data
    public void fillData() {

        // Stub

        WordData wd = new WordData(1, "misanthrope", "Being an misanthrope doesn't really mean that the person is an introvert");
        WordData wd2 = new WordData(1, "ubiquitous", "The greed for money seems to be ubiquitous");
        WordData wd3 = new WordData(1, "polyglot ", "Polyglots often tend to have a huge memory");

        db.addWord(wd);
        db.addWord(wd2);
        db.addWord(wd3);

    }


    // An async task to handle the retrieval of the list of word on a different thread.
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

            if (sentFromAlpha) {
                list = db.alphaWords(alphaSentByGrid);
            } else {
                list = db.allWords();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            this.progressDialog.dismiss();

            if (list.isEmpty()) {
                tvPlaceHolder.setVisibility(View.VISIBLE);
                tvPlaceHolder.setText("No words yet");
            }

            Context con = StartScreen.this.getApplication();
            adapt = new RVAdapt(list, con);
            rv.setAdapter(adapt);


        }
    }


    // To add a word
    public void addWordStart() {
        Intent i = new Intent(StartScreen.this, AddWord.class);
        startActivityForResult(i, 69);
    }


    // To edit a word
    public void addWordStart(String word, String sentence) {
        Intent i = new Intent(StartScreen.this, AddWord.class);
        i.putExtra("word",word);
        i.putExtra("sentence", sentence);
        startActivityForResult(i, 69);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // The list refreshes after the word adding/editing screen is exited.
        if (requestCode == 69 || requestCode == 70) {
            refreshList();
        }

    }


    // Simple clears the list and reloads the async task to get the new data
    public void refreshList() {
        list.clear();
        adapt.notifyDataSetChanged();
        new DBSniffer().execute(StartScreen.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text))
                .setTextColor(ContextCompat.getColor(this, R.color.white));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                adapt.filter(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapt.filter(s);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                Intent i = new Intent(StartScreen.this, AboutCopyright.class);
                startActivity(i);
                break;

            case R.id.malphagrid:
                Intent i7 = new Intent(StartScreen.this, AlphaGrid.class);
                startActivity(i7);
                break;

            case R.id.mrefresh:
                refreshList();
                break;

            case R.id.madd:
                addWordStart();
                break;

            case R.id.mshuffle:
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Usage")
                        .setAction("Shuffled")
                        .build());
                Collections.shuffle(list);
                adapt.notifyDataSetChanged();
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
    }
}
