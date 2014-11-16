package com.grc.data.gioserver;

import android.util.Log;

import com.google.gson.Gson;
import com.grc.data.DataProvider;
import com.grc.models.Choice;
import com.grc.models.Question;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Giovanni on 11/16/2014.
 */
public class GioServerProvider extends DataProvider {

    private static final String _gioServer = "http://alfred.student.rit.edu:7059";

    @Override
    public boolean submitAnswer(Question question, int choiceIndex) {
        try {
            Choice choice = new Choice();
            choice.setUUID(question.getUuid());
            choice.setChoiceIndex(choiceIndex);

            Gson gson = new Gson();
            String jsonChoice = gson.toJson(choice, Choice.class);

            HttpURLConnection con = (HttpURLConnection)( new URL(_gioServer)).openConnection();
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();

            con.getOutputStream().write( ("ChoiceJSON=" + jsonChoice).getBytes());

            InputStream is = con.getInputStream();
            byte[] res = new byte[100];
            is.read(res);
            String response = new String(res);
            Log.i(this.getClass().getName(), response);
            Log.i("poop", "poopy poop");
            con.disconnect();

            return true;

        }catch(Exception e){
            return false;
        }
    }

    @Override
    public Question[] retrieveQuestions(int numberToRetrieve) {

        // TODO GET JSON

        String questionsJson = "";
        Gson gson = new Gson();
        Question[] questions = gson.fromJson(questionsJson, Question[].class);

        return questions;
    }
}
