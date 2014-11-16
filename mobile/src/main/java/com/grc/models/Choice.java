package com.grc.models;

import java.util.UUID;

/**
 * Created by Giovanni on 11/16/2014.
 */
public class Choice {

    private String _uuid;

    private int _choiceIndex;

    public String getUUID(){
        return _uuid;
    }

    public void setUUID(String uuid){
        _uuid = uuid;
    }

    public int getChoiceIndex(){
        return _choiceIndex;
    }

    public void setChoiceIndex(int choiceIndex){
        _choiceIndex = choiceIndex;
    }
}
