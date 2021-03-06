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

/*
*   Database helper class with all the methods we need.
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


    // Used to add new words
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


    // Get aa ArrayList of all words
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


    // Written here is a method use to delete every word/row.
    // Use it for testing purposes. Not for release.
    public void deleteAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NAME);
    }


    // Searching a word by giving a similar term
    public ArrayList searchWord(String term){

        ArrayList<WordData> wordDataList = new ArrayList<WordData>();
        String query = "SELECT  * FROM " + TABLE_NAME + " WHERE word LIKE '%"+ term +"%'";
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


    // Gives an ArrayList with all the words starting from the alphabet which is sent as a parameter.
    public ArrayList<WordData> alphaWords(String s) {

        ArrayList<WordData> wordDataList = new ArrayList<WordData>();
        String query = "SELECT  * FROM " + TABLE_NAME + " WHERE word LIKE '" +s+ "%'";
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


    // Delete a given word
    public void deleteWord(String term){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NAME + " where word like '"+ term +"'");
    }


    // Get the primary ID of any word
    public int getId(String term) {

        int id = 99999;
        String query = "SELECT  * FROM " + TABLE_NAME + " WHERE word LIKE '" + term + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(0);
            } while (cursor.moveToNext());
        }

        return id;
    }


    // Use to update current word entries
    public void editWord(int id2, WordData wd){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues vals = new ContentValues();
        vals.put(KEY_WORD,wd.getWord());
        vals.put(KEY_SENTENCE, wd.getSentence());

        db.update(TABLE_NAME, vals, "id="+id2, null);
    }


    // Method to search a word by giving it's ID
    public WordData searchWord(int id){

        String query = "SELECT  * FROM " + TABLE_NAME + " WHERE id="+ id +"";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        WordData word = null;

        if (cursor.moveToFirst()) {
            do {
                word = new WordData(cursor.getInt(3),cursor.getString(1), cursor.getString(2));

            } while (cursor.moveToNext());
        }

        return word;
    }

}
