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
    private UUID _uuid;

    /**
     * Two answers to the question.
     */
    private String[] _answers;

    /**
     * Distribution of answers. _percentages[0] represents the number of people that chose
     * _answers[0], and so on.
     */
    private double[] _percentages;

    public String getText() {
        return _text;
    }

    public void setText(String _text) {
        this._text = _text;
    }

    public UUID getUuid() {
        return _uuid;
    }

    public void setUuid(UUID _uuid) {
        this._uuid = _uuid;
    }

    public String[] getAnswers() {
        return _answers;
    }

    public void setAnswers(String[] _answers) {
        this._answers = _answers;
    }
}
