package com.grc.data.bluemix;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.util.Log;

import com.grc.data.DataProvider;
import com.grc.models.Question;
import com.ibm.mobile.services.core.IBMBluemix;
import com.ibm.mobile.services.data.IBMData;
import com.ibm.mobile.services.data.IBMDataException;
import com.ibm.mobile.services.data.IBMDataObject;
import com.ibm.mobile.services.data.IBMQuery;
import com.ibm.mobile.services.data.IBMQueryCondition;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import bolts.Continuation;
import bolts.Task;

//import bolts.Task;

/**
 * Created by Giovanni on 11/15/2014.
 */
public class BluemixDataProvider extends DataProvider {

    public BluemixDataProvider(Activity activity) throws Exception{
        if(!initialize(activity)){
            throw new Exception("There was an error initializing Bluemix.");
        }
    }

    /**
     * Validates that the "bluemix.properties" file is present and uncorrupted.
     * @param activity
     */
    private boolean initialize(Activity activity){
        // Read from properties file
        Properties props = new java.util.Properties();
        try {
            AssetManager assetManager = activity.getApplicationContext().getAssets();
            props.load(assetManager.open(BluemixFiles.APP_PROPS.toString()));
            Log.i(this.getClass().getName(), "Found configuration file: " + BluemixFiles.APP_PROPS);
        } catch (FileNotFoundException e) {
            Log.e(this.getClass().getName(), "The bluelist.properties file was not found.", e);
            return false;
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "The bluelist.properties file could not be read properly.", e);
            return false;
        }
        // initialize the IBM core backend-as-a-service
        IBMBluemix.initialize(activity, props.getProperty(BluemixFiles.APP_ID),
                props.getProperty(BluemixFiles.APP_SECRET), props.getProperty(BluemixFiles.APP_ROUTE));
        // initialize the IBM Data Service
        IBMData.initializeService();
        // register the Specializations
        BluemixQuestionDataItem.registerSpecialization(BluemixQuestionDataItem.class);
        BluemixStatisticDataItem.registerSpecialization(BluemixStatisticDataItem.class);

        return true;
    }

    @Override
    public boolean submitAnswer(final Question question, final int answerIndex) {
        try {
            // Construct the query
            IBMQuery<BluemixStatisticDataItem> query = IBMQuery.queryForClass(BluemixStatisticDataItem.class);

            query.find().continueWith(new Continuation<List<BluemixStatisticDataItem>, Object>() {

                @Override
                public Void then(Task<List<BluemixStatisticDataItem>> task) throws Exception {
                    final List<BluemixStatisticDataItem> objects = task.getResult();
                    // Log if the find was cancelled.
                    if (task.isCancelled()){
                        Log.e(BluemixDataProvider.class.getName(), "Exception : Task " + task.toString() + " was cancelled.");
                        throw new Exception("Retrieval task was cancelled.");
                    }
                    // Log error message, if the find task fails.
                    else if (task.isFaulted()) {
                        Log.e(BluemixDataProvider.class.getName(), "Exception : " + task.getError().getMessage());
                        throw new Exception("Retrieval task was faulted.");
                    }
                    // If the result succeeds, load the list.
                    else {
                        // Find the statistic with the matching UUID to the question, if it exists.
                        BluemixStatisticDataItem theOneWereLookingFor = null;
                        for(IBMDataObject obj : objects){
                            BluemixStatisticDataItem item = (BluemixStatisticDataItem)obj;
                            if(item.getId().equals(question.getUuid())){
                                theOneWereLookingFor = item;
                                break;
                            }
                        }

                        if(theOneWereLookingFor != null){
                            theOneWereLookingFor.incrementIndex(answerIndex);
                            theOneWereLookingFor.save().continueWith(new Continuation<IBMDataObject, Void>() {

                                @Override
                                public Void then(Task<IBMDataObject> task) throws Exception {
                                    if(task.isCancelled()) {
                                        Log.e(BluemixDataProvider.class.getName(), "Exception : " + task.toString() + " was cancelled.");
                                        throw new Exception("Write task was cancelled.");
                                    }

                                    else if (task.isFaulted()) {
                                        Log.e(BluemixDataProvider.class.getName(), "Exception : " + task.getError().getMessage());
                                        throw new Exception("Write task was faulted.");
                                    }

                                    else {
                                        // all good in da hood
                                    }
                                    return null;
                                }

                            },Task.UI_THREAD_EXECUTOR);
                        }
                    }
                    return null;
                }
            },Task.UI_THREAD_EXECUTOR);
        } catch(Exception e){
            Log.e(this.getClass().getName(), "Exception : " + e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public Question[] retrieveQuestions(final int numberToRetrieve) {
        final List<Question> questions = new ArrayList<Question>();
        try {
            // Construct the query
            IBMQuery<BluemixQuestionDataItem> query = IBMQuery.queryForClass(BluemixQuestionDataItem.class);

            query.find().continueWith(new Continuation<List<BluemixQuestionDataItem>, Object>() {

                @Override
                public Void then(Task<List<BluemixQuestionDataItem>> task) throws Exception {
                    final List<BluemixQuestionDataItem> objects = task.getResult();
                    // Log if the find was cancelled.
                    if (task.isCancelled()){
                        Log.e(BluemixDataProvider.class.getName(), "Exception : Task " + task.toString() + " was cancelled.");
                    }
                    // Log error message, if the find task fails.
                    else if (task.isFaulted()) {
                        Log.e(BluemixDataProvider.class.getName(), "Exception : " + task.getError().getMessage());
                    }


                    // If the result succeeds, load the list.
                    else {
                        // if the length of the list of Questions that is retrieved is
                        // less than or equal to the size of numberToRetrieve we can just
                        // return that list without the shuffle.
                        if(objects.size() <= numberToRetrieve){
                            for(IBMDataObject obj : objects){
                                BluemixQuestionDataItem item = (BluemixQuestionDataItem)obj;
                                questions.add(item.toQuestion());
                            }
                        }
                        else{
                            // We want to get numberToRetrieve Questions without repeating,
                            // so we'll use a shuffle.
                            // http://stackoverflow.com/questions/16000196/java-generating-non-repeating-random-numbers
                            Integer[] nums = new Integer[numberToRetrieve];
                            for(int i = 0; i < numberToRetrieve; i++){
                                nums[i] = i;
                            }
                            Collections.shuffle(Arrays.asList(nums));

                            for(Integer i : nums) {
                                BluemixQuestionDataItem item = (BluemixQuestionDataItem)objects.get(i);
                                questions.add(item.toQuestion());
                            }
                        }
                    }
                    return null;
                }
            },Task.UI_THREAD_EXECUTOR);
        } catch(IBMDataException e){
            Log.e(this.getClass().getName(), "Exception : " + e.getMessage());
            return null;
        }

        return questions.toArray(new Question[questions.size()]);
    }
}
