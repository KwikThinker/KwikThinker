package com.grc.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.css.kwikthinker.R;
import com.grc.data.bluemix.BluemixDataProvider;
import com.grc.data.bluemix.BluemixQuestionDataItem;
import com.grc.data.bluemix.BluemixStatisticDataItem;
import com.ibm.mobile.services.data.IBMDataObject;

import java.util.UUID;

import bolts.Continuation;
import bolts.Task;

public class CreateQuestionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_question);

        // set up action listeners
        final Button buttonUUID = (Button)findViewById(R.id.buttonUUID);
        final Button buttonSubmit = (Button)findViewById(R.id.buttonSubmit);
        final EditText editTextUUID = (EditText)findViewById(R.id.editTextUUID);
        final EditText editTextQuestionText = (EditText)findViewById(R.id.editTextQuestionText);
        final EditText editTextAnswer1 = (EditText)findViewById(R.id.editTextAnswer1);
        final EditText editTextAnswer2 = (EditText)findViewById(R.id.editTextAnswer2);
        final EditText editTextCorrectIndex = (EditText)findViewById(R.id.editTextCorrectIndex);
        final EditText editTextNumCorrect = (EditText)findViewById(R.id.editTextNumCorrect);
        final EditText editTextNumIncorrect = (EditText)findViewById(R.id.editTextNumIncorrect);

        final Context context = this.getApplicationContext();

        buttonUUID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextUUID.setText(UUID.randomUUID().toString());
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluemixQuestionDataItem q = new BluemixQuestionDataItem();
                UUID id = UUID.fromString(editTextUUID.getText().toString());
                q.setUuid(id.toString());
                q.setText(editTextQuestionText.getText().toString());
                String[] answers = new String[2];
                answers[0] = editTextAnswer1.getText().toString();
                answers[1] = editTextAnswer2.getText().toString();
                q.setAnswers(answers);
                q.setCorrectIndex(Integer.parseInt(editTextCorrectIndex.getText().toString()));

                try {
                    q.save().continueWith(new Continuation<IBMDataObject, Void>() {

                        @Override
                        public Void then(Task<IBMDataObject> task) throws Exception {
                            // Log if the save was cancelled.
                            if (task.isCancelled()) {
                                Log.e(BluemixDataProvider.class.getName(), "Exception : Task " + task.toString() + " was cancelled.");
                            }
                            // Log error message, if the save task fails.
                            else if (task.isFaulted()) {
                                Log.e(BluemixDataProvider.class.getName(), "Exception : " + task.getError().getMessage());
                            }

                            // If the result succeeds, load the list.
                            else {
                                // do do nothing
                            }
                            return null;
                        }

                    });
                }catch(IllegalStateException e){
                    Log.e("IllegalStateException", "Exception caught!");
                }

                BluemixStatisticDataItem s = new BluemixStatisticDataItem();
                s.setId(id.toString());
                int numCorrect = Integer.parseInt(editTextNumCorrect.getText().toString());
                int numIncorrect = Integer.parseInt(editTextNumIncorrect.getText().toString());
                s.setNumberOfAnswers(new Integer[] {numCorrect, numIncorrect});

                try {
                    s.save().continueWith(new Continuation<IBMDataObject, Void>() {

                        @Override
                        public Void then(Task<IBMDataObject> task) throws Exception {
                            if (task.isCancelled()) {
                                Log.e(BluemixDataProvider.class.getName(), "Exception : " + task.toString() + " was cancelled.");
                                throw new Exception("Write task was cancelled.");
                            } else if (task.isFaulted()) {
                                Log.e(BluemixDataProvider.class.getName(), "Exception : " + task.getError().getMessage());
                                throw new Exception("Write task was faulted.");
                            } else {
                                // all good in da hood
                            }
                            return null;
                        }

                    }, Task.UI_THREAD_EXECUTOR);
                }catch(IllegalStateException e){
                    Log.e("IllegalStateException", "Exception caught!");
                }

                Toast.makeText(context, "Entry submitted.", Toast.LENGTH_LONG);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_question, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }
}
