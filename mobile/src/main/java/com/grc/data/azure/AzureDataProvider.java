package com.grc.data.azure;

import android.content.Context;

import com.grc.data.DataProvider;
import com.grc.models.Question;

/**
 * Created by Giovanni on 11/15/2014.
 */
public class AzureDataProvider extends DataProvider {

    @Override
    public boolean submitAnswer(Question question, int answerIndex) {
        return false;
    }

    @Override
    public Question[] retrieveQuestions(int numberToRetrieve) {
        return new Question[0];
    }
}
