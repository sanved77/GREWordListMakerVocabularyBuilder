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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sanved on 04-08-2017.
 */

public class WordScreen extends AppCompatActivity {

    TextView word, sentence;
    ImageButton back, speak, share, delete, edit, dict;
    TextToSpeech tts;
    ProgressDialog progDiag;
    Tracker mTracker;
    Bundle params;
    HashMap<String, String> map;
    CoordinatorLayout cl1;
    SQLiteHelper db;
    String strWord, strSentence;
    int keyid;
    SharedPreferences prefs;
    SharedPreferences.Editor ed;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word_screen);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        db = new SQLiteHelper(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ed = prefs.edit();

        cl1 = (CoordinatorLayout) findViewById(R.id.cl1);

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

        word = (TextView) findViewById(R.id.tvWord);
        sentence = (TextView) findViewById(R.id.tvSentence);

        back = (ImageButton) findViewById(R.id.ibBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        speak = (ImageButton) findViewById(R.id.ibSpeak);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Usage")
                        .setAction("Speak - " + strWord)
                        .build());
                speakIt(strWord);
            }
        });

        share = (ImageButton) findViewById(R.id.ibShare);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.setPackage("com.whatsapp");
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Word - " + strWord + "\nPhrase - "
                        + strSentence);
                try {
                    startActivity(whatsappIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(WordScreen.this, "WhatsApp Doesn't seem to be installed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        delete = (ImageButton) findViewById(R.id.ibDelete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WordScreen.this);
                builder.setMessage("Are you sure you want to Delete the Word ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                db.deleteWord(strWord);
                                finish();
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
        });

        edit = (ImageButton) findViewById(R.id.ibEdit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyid = db.getId(strWord);
                Intent i = new Intent(WordScreen.this, AddWord.class);
                i.putExtra("word",strWord);
                i.putExtra("sentence", strSentence);
                startActivityForResult(i, 69);
            }
        });

        dict = (ImageButton) findViewById(R.id.ibDict);
        dict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEARCH);
                intent.setPackage("livio.pack.lang.en_US");//you can use also livio.pack.lang.en_US, livio.pack.lang.es_ES, livio.pack.lang.de_DE, livio.pack.lang.pt_BR or livio.pack.lang.fr_FR
                intent.putExtra(SearchManager.QUERY, strWord);
                if (isIntentAvailable(WordScreen.this, intent)) // check if intent is available ?
                    startActivity(intent);
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(WordScreen.this);
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
            }
        });

        if(savedInstanceState == null){

            Bundle extras = getIntent().getExtras();

            if(extras == null){
                Toast.makeText(this, "Something is wrong", Toast.LENGTH_SHORT).show();
                strSentence = "There was an error, go back and select the word again";
                strWord = "Error";
                speak.setVisibility(View.GONE);

            }else{
                strWord = extras.getString("word");
                strSentence = extras.getString("sentence");
            }

        }else{

            strWord = (String) savedInstanceState.getSerializable("word");
            strSentence = (String) savedInstanceState.getSerializable("sentence");

        }

        word.setText(strWord);
        sentence.setText(strSentence);

        Drawable addIcon = ContextCompat.getDrawable(this, R.drawable.ic_volume_up_white_48dp);

        if(prefs.getBoolean("firstUseWord", true)){

            TapTargetView.showFor(this,                 // `this` is an Activity
                    TapTarget.forView(findViewById(R.id.llbuttons), "Hear and Learn", "You can hear the words and see their meaning")
                            // All options below are optional
                            .outerCircleColor(R.color.blue)      // Specify a color for the outer circle
                            .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                            .titleTextSize(25)                  // Specify the size (in sp) of the title text
                            .titleTextColor(R.color.white)      // Specify the color of the title text
                            .descriptionTextSize(15)            // Specify the size (in sp) of the description text
                            .descriptionTextColor(R.color.white)  // Specify the color of the description text
                            .textColor(R.color.white)            // Specify a color for both the title and description text
                            .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                            .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                            .drawShadow(true)
                            .cancelable(true)                   // Whether tapping outside the outer circle dismisses the view
                            .tintTarget(true)                   // Whether to tint the target view's color
                            .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                            .targetRadius(70),                  // Specify the target radius (in dp)
                    new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);      // This call is optional
                            //addWordStart();
                        }
                    });

            ed.putBoolean("firstUseWord",false);
            ed.commit();
        }

    }

    public boolean isIntentAvailable(Context context, Intent intent) {
        List<ResolveInfo> lri = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return (lri != null) && (lri.size() > 0);
    }

    public void speakIt(String thingToSay){

        final Snackbar snackbar = Snackbar
                .make(cl1, Html.fromHtml("Loading Speech"), Snackbar.LENGTH_SHORT);
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

                Toast.makeText(WordScreen.this, "Error !", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 69){
            WordData wd = db.searchWord(keyid);
            strWord = wd.getWord();
            strSentence = wd.getSentence();
            word.setText(strWord);
            sentence.setText(strSentence);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("WordScreen");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.stop();
        tts.shutdown();
    }
}
