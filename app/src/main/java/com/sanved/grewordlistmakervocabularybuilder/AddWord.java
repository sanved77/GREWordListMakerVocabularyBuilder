package com.sanved.grewordlistmakervocabularybuilder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Sanved on 04-08-2017.
 */

public class AddWord extends AppCompatActivity {

    EditText word, sentence;
    Button add;
    SQLiteHelper db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addword);

        initVals();

    }

    public void initVals(){

        word = (EditText) findViewById(R.id.etWord);
        sentence = (EditText) findViewById(R.id.etSentence);

        db = new SQLiteHelper(this);

        add = (Button) findViewById(R.id.bAdd);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(word.getText().toString() == null){
                    Toast.makeText(AddWord.this, "Please enter the word", Toast.LENGTH_SHORT).show();
                }else if(sentence.getText().toString() == null){
                    Toast.makeText(AddWord.this, "Enter a phrase", Toast.LENGTH_SHORT).show();
                }else if(!(sentence.getText().toString().contains(word.getText().toString()))){
                    Toast.makeText(AddWord.this, "The phrase doesn't contain the word", Toast.LENGTH_SHORT).show();
                }else{
                    // Entering the word

                    WordData wd = new WordData(1, word.getText().toString(), sentence.getText().toString());
                    db.addWord(wd);
                    Toast.makeText(AddWord.this, "Word Added", Toast.LENGTH_SHORT).show();
                    finish();

                }

            }
        });
    }

}

