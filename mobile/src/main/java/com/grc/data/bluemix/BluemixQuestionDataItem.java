package com.grc.data.bluemix;

import com.ibm.mobile.services.data.IBMDataObject;
import com.ibm.mobile.services.data.IBMDataObjectSpecialization;

import java.util.UUID;

/**
 * Data object that represents a {@link com.grc.models.Question}.
 */
@IBMDataObjectSpecialization("BluemixQuestionDataItem")
public class BluemixQuestionDataItem extends IBMDataObject {

    private static final String TEXT = "text";
    private static final String ID = "uuid";
    private static final String ANSWERS = "answers";
    private static final String PERCENTAGES = "percentages";

    public String getText(){
        return (String)getObject(TEXT);
    }

    public void setText(String text){
        if(text == null || text.equals("")){
            // we don't want null UUIDs.
            throw new IllegalArgumentException("text cannot be null or empty.");
        }
        setObject(TEXT, (text != null) ? text : "");
    }

    public UUID getUuid(){
        return (UUID)getObject(ID);
    }

    public void setUuid(UUID id){
        if(id == null){
            // we don't want null UUIDs.
            throw new IllegalArgumentException("UUID cannot be null.");
        }
        setObject(ID, id);
    }

    public String[] getAnswers(){
        return (String[])getObject(ANSWERS);
    }

    public void setAnswers(String[] answers){
        if(answers == null || answers.length != 2){
            // we don't want null UUIDs.
            throw new IllegalArgumentException("answers must not be null and must have length 2.");
        }
        setObject(ANSWERS, answers);
    }

    public String[] getPercentages(){
        return (String[])getObject(PERCENTAGES);
    }

    public void setPercentages(String[] percentages){
        if(percentages == null || percentages.length != 2){
            // we don't want null UUIDs.
            throw new IllegalArgumentException("percentages must not be null and must have length 2.");
        }
        setObject(PERCENTAGES, percentages);
    }

}
