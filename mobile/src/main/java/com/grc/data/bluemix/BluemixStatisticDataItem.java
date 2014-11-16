package com.grc.data.bluemix;

import com.grc.models.Statistic;
import com.ibm.mobile.services.data.IBMDataObject;
import com.ibm.mobile.services.data.IBMDataObjectSpecialization;

import java.util.UUID;

/**
 * Data object that represents a {@link com.grc.models.Statistic}.
 */
@IBMDataObjectSpecialization("BluemixStatisticDataItem")
public class BluemixStatisticDataItem extends IBMDataObject {

    public BluemixStatisticDataItem(){
        super();
    }

    private static final String ID = "uuid";
    private static final String NUMBER_OF_ANSWERS = "numberOfAnswers";

    public String getId(){
        return (String)getObject(ID);
    }

    public void setId(String id){
        setObject(ID, id);
    }

    public Integer[] getNumberOfAnswers(){
        return (Integer[])getObject(NUMBER_OF_ANSWERS);
    }

    public void setNumberOfAnswers(Integer[] numberOfAnswers){
        setObject(NUMBER_OF_ANSWERS, numberOfAnswers);
    }

    /**
     * Converts the BluemixStatisticDataItem into a Statistic object.
     * @return
     */
    public Statistic toStatistic(){
        Statistic s = new Statistic();
        s.setNumberOfAnswers(getNumberOfAnswers());
        s.setUuid(getId());

        return s;
    }

    /**
     * Creates a BluemixStatisticDataItem from a Statistic object.
     * @param s
     * @return
     */
    public static BluemixStatisticDataItem fromStatistic(Statistic s){
        BluemixStatisticDataItem item = new BluemixStatisticDataItem();
        item.setNumberOfAnswers(s.getNumberOfAnswers());
        item.setId(s.getUuid());

        return item;
    }

    public void incrementIndex(int index){
        getNumberOfAnswers()[index]++;
    }

}
