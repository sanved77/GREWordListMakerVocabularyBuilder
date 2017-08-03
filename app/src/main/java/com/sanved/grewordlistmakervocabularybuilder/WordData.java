package com.sanved.grewordlistmakervocabularybuilder;

/**
 * Created by Sanved on 03-08-2017.
 */

public class WordData {

    int difficulty;
    String word, sentence;

    public WordData(int difficulty, String word, String sentence){

        this.difficulty = difficulty;
        this.word= word;
        this.sentence = sentence;

    }

    public int getDifficulty() {
        return difficulty;
    }

    public String getSentence() {
        return sentence;
    }

    public String getWord() {
        return word;
    }
}
