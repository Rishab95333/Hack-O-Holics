package com.dmi.meetingrecorder.summarize;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Karan on 25/2/16.
 */
public class SummarizedTextModel implements Serializable {
    public ArrayList<String> sentences;

    public ArrayList<String> getSentences() {
        return sentences;
    }

    public void setSentences(ArrayList<String> sentences) {
        this.sentences = sentences;
    }
}
