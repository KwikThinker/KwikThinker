package com.grc.data;

import android.content.Context;

import com.grc.models.Question;

import java.util.UUID;

/**
 * A DataProvider provides the application with (what else?) a remote data source.
 */
public abstract class DataProvider {

    /**
     * Submits an answer to the cloud.
     * @param question
     * @param answerIndex
     * @return success
     */
    public abstract boolean submitAnswer(Question question, int answerIndex);

    /**
     * Gets a list of random questions.
     * @param numberToRetrieve
     * @return random list of questions
     */
    public abstract Question[] retrieveQuestions(int numberToRetrieve);

}
