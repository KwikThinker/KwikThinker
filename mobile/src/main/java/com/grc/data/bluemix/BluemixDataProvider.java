package com.grc.data.bluemix;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.grc.data.DataProvider;
import com.grc.models.Question;
import com.ibm.mobile.services.core.IBMBluemix;
import com.ibm.mobile.services.data.IBMData;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

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
        // register the Item Specialization
        BluemixQuestionDataItem.registerSpecialization(BluemixQuestionDataItem.class);

        return true;
    }

    @Override
    public boolean submitAnswer(Question question, int answerIndex) {
        return false;
    }

    @Override
    public Question[] retrieveQuestions(int numberToRetrieve) {
        return new Question[0];
    }
}
