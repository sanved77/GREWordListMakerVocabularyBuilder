package com.sanved.grewordlistmakervocabularybuilder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Sanved on 04-08-2017.
 */

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "wordsdb";
    private static final String TABLE_NAME = "words";
    private static final String KEY_ID = "id";
    private static final String KEY_WORD = "word";
    private static final String KEY_SENTENCE = "sentence";
    private static final String KEY_LEVEL = "level";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE_NAME + " ( id INTEGER PRIMARY KEY AUTOINCREMENT, word VARCHAR, sentence VARCHAR, level INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addWord(WordData wd) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_WORD, wd.getWord());
        values.put(KEY_SENTENCE, wd.getSentence());
        values.put(KEY_LEVEL, wd.getDifficulty());
        // insert
        db.insert(TABLE_NAME,null, values);
        db.close();
    }

    public ArrayList<WordData> allWords() {

        ArrayList<WordData> wordDataList = new ArrayList<WordData>();
        String query = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        WordData word = null;

        if (cursor.moveToFirst()) {
            do {
                word = new WordData(cursor.getInt(3),cursor.getString(1), cursor.getString(2));

                wordDataList.add(word);
            } while (cursor.moveToNext());
        }

        return wordDataList;
    }

    public void deleteAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NAME);
    }

}
