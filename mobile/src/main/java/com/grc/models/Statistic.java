package com.grc.models;

import java.util.UUID;

/**
 * Represents a question's cataloged answers.
 */
public class Statistic {

    /**
     * Unique identifier of the question this is related to.
     */
    private String _uuid;

    /**
     * Number of times each answer was chosen by a user.
     */
    private Integer[] _numberOfAnswers;

    public String getUuid(){
        return _uuid;
    }

    public void setUuid(String uuid){
        _uuid = uuid;
    }

    public Integer[] getNumberOfAnswers(){
        return _numberOfAnswers;
    }

    public void setNumberOfAnswers(Integer[] numberOfAnswers){
        _numberOfAnswers = numberOfAnswers;
    }

    public void incrementIndex(int index){
        _numberOfAnswers[index]++;
    }
}
