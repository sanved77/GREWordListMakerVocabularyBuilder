package com.sanved.grewordlistmakervocabularybuilder;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Sanved on 04-08-2017.
 */

/*
*   This Activity is here to add a word onto the database
*   It is also use to edit an existing word
*/

public class AddWord extends AppCompatActivity {

    EditText word, sentence;
    Button add;
    SQLiteHelper db;
    Tracker mTracker;
    ImageButton back;

    String strWord, strSentence;
    boolean isEdit = false;
    int keyid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addword);

        initVals();

        /*
        *   If someone wants to edit a word then this Activity gets the word and sentence from the previous activity
        *   Otherwise they are null
        *   The date sent is taken.
        */

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();

            if(extras == null){
                isEdit = false;
            }else {
                strWord = extras.getString("word");
                strSentence = extras.getString("sentence");
            }
        }else{
            strWord = (String) savedInstanceState.getSerializable("word");
            strSentence = (String) savedInstanceState.getSerializable("sentence");
        }

        if(strWord == null) isEdit = false;
        else isEdit = true;

        //  If the word is to be edited, then Edittexts are filled with the data
        //  and the Button text changes from Add to Update

        if(isEdit){
            keyid = db.getId(strWord);
            word.setText(strWord);
            sentence.setText(strSentence);
            add.setText("Update");
        }


    }

    public void initVals(){

        // Just declaration and initialisation of the variables.

        word = (EditText) findViewById(R.id.etWord);
        sentence = (EditText) findViewById(R.id.etSentence);

        word.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(word, InputMethodManager.SHOW_IMPLICIT);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        // This is the SQLiteHelper object which we will need later.
        db = new SQLiteHelper(this);

        add = (Button) findViewById(R.id.bAdd);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Validations

                // If the word is empty
                if(word.getText().toString() == null){
                    Toast.makeText(AddWord.this, "Please enter the word", Toast.LENGTH_SHORT).show();
                }
                // If the sentence is empty
                else if(sentence.getText().toString() == null){
                    Toast.makeText(AddWord.this, "Enter a phrase", Toast.LENGTH_SHORT).show();
                }

                else{
                    // Entering the word

                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Usage")
                            .setAction("Word Added " + word.getText().toString())
                            .build());

                    /*
                    *   Creating an object of the class WordData, which we made
                    *   This object is sent to the DataBase helper so that it can be added.
                    */

                    WordData wd = new WordData(1, word.getText().toString(), sentence.getText().toString());

                    // If an edit was happening, it will invoke the editword method or else it will add the new word.
                    if(isEdit) {
                        db.editWord(keyid,wd);
                        Toast.makeText(AddWord.this, "Word Edited", Toast.LENGTH_SHORT).show();
                    }else{
                        db.addWord(wd);
                        Toast.makeText(AddWord.this, "Word Added", Toast.LENGTH_SHORT).show();
                    }

                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Usage")
                            .setAction("S " + sentence.getText().toString())
                            .build());
                    finish();

                }

            }
        });

        back = (ImageButton) findViewById(R.id.ibBack);
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
        mTracker.setScreenName("AddWord");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

}

