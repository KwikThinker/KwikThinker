package com.grc.models;

import java.util.UUID;

/**
 * Represents a question in the program.
 */
public class Question {

    /**
     * Actual text of the question.
     */
    private String _text;

    /**
     * Unique identifier associated with the question.
     */
    private String _uuid;

    /**
     * Two answers to the question.
     */
    private String[] _answers;

    /**
     * Index of the correct answer.
     */
    private int _correctIndex;

    public String getText() {
        return _text;
    }

    public void setText(String _text) {
        this._text = _text;
    }

    public String getUuid() {
        return _uuid;
    }

    public void setUuid(String _uuid) {
        this._uuid = _uuid;
    }

    public String[] getAnswers() {
        return _answers;
    }

    public void setAnswers(String[] _answers) {
        this._answers = _answers;
    }

    public int getCorrectIndex(){
        return _correctIndex;
    }

    public void setCorrectIndex(int correctIndex){
        _correctIndex = correctIndex;
    }
}
