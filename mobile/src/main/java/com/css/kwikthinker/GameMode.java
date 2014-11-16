package com.css.kwikthinker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.css.kwikthinker.util.SystemUiHider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class GameMode extends Activity implements View.OnClickListener{
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    private static final int NUM_SECONDS_PER_MILLISECOND = 1000;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 6000;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = 0;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    ProgressBar countdownProgressBar;
    TextView countdownTV;
    Button startButton, playAgainButton;
    // im british... and pissed!
    private static final int[] COLOURS = {
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.MAGENTA,
        Color.CYAN,
        Color.YELLOW,
        Color.WHITE };
    private static int lastColor = Color.BLUE;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    // user input gets locked once they've answered once
    private static boolean inputLockedOnResponse = true;

    private boolean correctAnswer;

    private ArrayList<SampleQuestion> QUESTIONS =
            (new SampleQuestionProvider()).getSampleQuestions();

    private Long JON_SKEETS_REPUTATION;

    private int NUM_CORRECT = 0;
    private int NUM_YES = 0;
    private int NUM_NO  = 0;
    private float PERCENT_CORRECT = 0;
    private float NUM_ANSWERED = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getRandomNumber(0, 20);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_game_mode);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });


        // Gesture detection
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };

        contentView.setOnClickListener(GameMode.this);
        contentView.setOnTouchListener(gestureListener);


        countdownProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        countdownProgressBar.setVisibility(View.INVISIBLE);
        countdownProgressBar.setMax(5000); // TODO GLOBAL CONST
        countdownProgressBar.setProgress(5000);

        countdownTV = (TextView)findViewById(R.id.countdownTV);
        /*countdownTV.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT,
                0.75f));); // TODO take up 75% of parent width - can't do in XML */
        startButton = (Button)findViewById(R.id.start_em_countdown);
        playAgainButton = (Button)findViewById(R.id.playAgainButton);
        playAgainButton.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    public void onStartClick(View view) {

        countdownTV.setTextSize(250);

        NUM_CORRECT = 0;
        NUM_YES = 0;
        NUM_NO  = 0;
        PERCENT_CORRECT = 0;
        NUM_ANSWERED = 0;

        countdownProgressBar.setProgress(5000);
        startButton.setVisibility(View.INVISIBLE);
        playAgainButton.setVisibility(View.INVISIBLE);
        CountDownTimer cdt = new CountDownTimer(5000, 10) {

            @Override
            public void onFinish() {
                countdownProgressBar.setVisibility(View.VISIBLE);
                spawnQuestion();
            }


            public void onTick(long ms) {
                long s = ms / NUM_SECONDS_PER_MILLISECOND + 1;
                int a = (int)( ( s / 5.0 ) * 255 ); // TODO global consts
                if ( ms < 50 ) {
                    countdownTV.setText("");
                }
                countdownTV.setText(String.valueOf(s));
                countdownTV.setTextColor(countdownTV.getTextColors().withAlpha(a));
            }


        };
        // do countdown
        cdt.start();
    }

    private void spawnQuestion() {
        // TODO RANDOM SEEDS
        //      background color
        //      question

        if ( ! inputLockedOnResponse ) // time ran out!
        {
            NUM_ANSWERED++; // this is misleading, NUM_ANSWERED actually = number of questions seen
            if ( correctAnswer )
                NUM_YES++;
            else
                NUM_NO++;

            updateStatsTextViews();
        }

        countdownProgressBar.setProgress(5000); // reset progress bar
        inputLockedOnResponse = false; // reset input lock for answering


        countdownTV.setTextColor(countdownTV.getTextColors().withAlpha(255));
        int colour = (int) Math.floor(Math.random() * 7); // im british... and pissed!
        while ( colour == lastColor )
            colour = (int) Math.floor(Math.random() * 7);
        lastColor = colour; // so we don't get repeated randoms
        FrameLayout layout = (FrameLayout) findViewById( R.id.EM_frame_layout);
        layout.setBackgroundColor(COLOURS[colour]);
        countdownTV.setTextSize(40);
        countdownTV.setTextColor(Color.BLACK);

        // TODO k-v pairs: (Question : boolean answer)
        // TODO k-v pairs: (QuestionString, TextSize)
        // TODO the above solution: great for hackathon, terrible for the long run

        if ( QUESTIONS.size() == 0 ) {
            countdownTV.setText("All available questions have been exhausted. Thanks for playing :)");
            playAgainButton.setVisibility(View.VISIBLE);
            QUESTIONS =
                    (new SampleQuestionProvider()).getSampleQuestions();
            inputLockedOnResponse = true;
            return;
        }

        int questionNumber = (new Random()).nextInt((QUESTIONS.size()));
        countdownTV.setText(QUESTIONS.get(questionNumber).QUESTION_STRING);
        correctAnswer = QUESTIONS.get(questionNumber).ANSWER;
        QUESTIONS.remove(questionNumber);

        CountDownTimer cdt = new CountDownTimer(5000, 10) {
            public void onTick(long ms) {
                // this... just... what? (looks the nicest, for who knows why)
                countdownProgressBar.setProgress(countdownProgressBar.getProgress()-16);
                // this refers to my confusion over why a decrement of 16 is the trick
                // this could be an asynchronous problem
                // UPDATE: This is mildly unreliable and pretty noticeably buggy, especially
                // when using the fade in / fade out animations for the images
            }

            @Override
            public void onFinish() {
                spawnQuestion();
            }
        }.start();

    }

    /**
     * Gesture detector detects left/right swipes.
     */
    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            try {

                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;

                final ImageView imgView = (ImageView) findViewById(R.id.answerFeedbackImageView);

                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if ( correctAnswer && ! inputLockedOnResponse ) {

                        NUM_ANSWERED++;
                        NUM_CORRECT++;
                        NUM_YES++;
                        PERCENT_CORRECT = Float.valueOf(NUM_CORRECT) / Float.valueOf(NUM_ANSWERED) * 100;

                        imgView.setImageResource(R.drawable.korrect_answer144);
                        imgView.setScaleX(5);
                        imgView.setScaleY(5);

                        imgView.setVisibility(View.VISIBLE);
                        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
                        anim.setDuration(750);
                        anim.setRepeatCount(1);
                        anim.setRepeatMode(Animation.REVERSE);
                        imgView.startAnimation(anim);
                        imgView.setVisibility(View.INVISIBLE);

                        updateStatsTextViews();

                    } else if ( ! inputLockedOnResponse ) {

                        NUM_ANSWERED++;
                        NUM_NO++;
                        PERCENT_CORRECT = Float.valueOf(NUM_CORRECT) / Float.valueOf(NUM_ANSWERED) * 100;

                        imgView.setImageResource(R.drawable.krong_answer144);
                        imgView.setScaleX(5);
                        imgView.setScaleY(5);

                        imgView.setVisibility(View.VISIBLE);
                        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
                        anim.setDuration(750);
                        anim.setRepeatCount(1);
                        anim.setRepeatMode(Animation.REVERSE);
                        imgView.startAnimation(anim);
                        imgView.setVisibility(View.INVISIBLE);

                        updateStatsTextViews();

                    }
                    inputLockedOnResponse = true;
                    // LEFT SWIPE
                    // ANSWER "NO"
                    // LOCK INPUT UNTIL NEW QUESTION IS SPAWNEDiiu


                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if ( ! correctAnswer && ! inputLockedOnResponse ) {

                        NUM_ANSWERED++;
                        NUM_CORRECT++;
                        NUM_NO++;
                        PERCENT_CORRECT = Float.valueOf(NUM_CORRECT) / Float.valueOf(NUM_ANSWERED) * 100;

                        imgView.setImageResource(R.drawable.korrect_answer144);
                        imgView.setScaleX(5);
                        imgView.setScaleY(5);

                        imgView.setVisibility(View.VISIBLE);
                        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
                        anim.setDuration(750);
                        anim.setRepeatCount(1);
                        anim.setRepeatMode(Animation.REVERSE);
                        imgView.startAnimation(anim);
                        imgView.setVisibility(View.INVISIBLE);

                        updateStatsTextViews();

                    } else if ( ! inputLockedOnResponse ) {

                        NUM_ANSWERED++;
                        NUM_NO++;
                        PERCENT_CORRECT = Float.valueOf(NUM_CORRECT) / Float.valueOf(NUM_ANSWERED) * 100;

                        imgView.setImageResource(R.drawable.krong_answer144);
                        imgView.setScaleX(5);
                        imgView.setScaleY(5);

                        imgView.setVisibility(View.VISIBLE);
                        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
                        anim.setDuration(750);
                        anim.setRepeatCount(1);
                        anim.setRepeatMode(Animation.REVERSE);
                        imgView.startAnimation(anim);
                        imgView.setVisibility(View.INVISIBLE);

                        updateStatsTextViews();

                    }
                    inputLockedOnResponse = true;
                    // RIGHT SWIPE
                    // ANSWER "YES"
                    // LOCK INPUT UNTIL NEW QUESTION IS SPAWNED


                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    public void onToggleStatsToggle(View view) {
        TextView av = (TextView) findViewById( R.id.averageValue    );
        TextView ny = (TextView) findViewById( R.id.numYesValue     );
        TextView nn = (TextView) findViewById( R.id.numNoValue      );
        TextView nc = (TextView) findViewById( R.id.correctNumValue );

        TextView avl = (TextView) findViewById( R.id.averageLabel    );
        TextView nyl = (TextView) findViewById( R.id.numYesLabel     );
        TextView nnl = (TextView) findViewById( R.id.numNoLabel      );
        TextView ncl = (TextView) findViewById( R.id.numCorrectLabel );

        ToggleButton tb = (ToggleButton) findViewById( R.id.toggleButton );
        if ( tb.isChecked() ) {
            av.setVisibility(View.VISIBLE);
            ny.setVisibility(View.VISIBLE);
            nn.setVisibility(View.VISIBLE);
            nc.setVisibility(View.VISIBLE);
            avl.setVisibility(View.VISIBLE);
            nyl.setVisibility(View.VISIBLE);
            nnl.setVisibility(View.VISIBLE);
            ncl.setVisibility(View.VISIBLE);
        } else {
            av.setVisibility(View.INVISIBLE);
            ny.setVisibility(View.INVISIBLE);
            nn.setVisibility(View.INVISIBLE);
            nc.setVisibility(View.INVISIBLE);
            avl.setVisibility(View.INVISIBLE);
            nyl.setVisibility(View.INVISIBLE);
            nnl.setVisibility(View.INVISIBLE);
            ncl.setVisibility(View.INVISIBLE);
        }
    }

    private void updateStatsTextViews () {
        TextView av = (TextView) findViewById( R.id.averageValue    );
        TextView ny = (TextView) findViewById( R.id.numYesValue     );
        TextView nn = (TextView) findViewById( R.id.numNoValue      );
        TextView nc = (TextView) findViewById( R.id.correctNumValue );
        av.setText(Float.valueOf(Float.valueOf(NUM_CORRECT) / Float.valueOf(NUM_ANSWERED) * 100).toString());
        ny.setText(Integer.valueOf(NUM_YES).toString());
        nn.setText(Integer.valueOf(NUM_NO).toString());
        nc.setText(Integer.valueOf(NUM_CORRECT).toString());
    }

    public void onClick(View v) {
        // nothing yet
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }










    private static String readUrl(String urlString) {

        try {
            URL url = new URL(urlString);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));

            String inputLine;
            int i = 0;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.contains("<span class=\"count\">")) {
                    Pattern regex = Pattern.compile("<span class=\"count\">(.*?)</span>", Pattern.DOTALL);
                    Matcher matcher = regex.matcher(inputLine);
                    Pattern regex2 = Pattern.compile("<([^<>]+)>([^<>]+)</\\1>");
                    if (matcher.find()) {
                        String result = matcher.group(1);
                        return result;
                    }
                }
                i++;
            }
            in.close();

            return null; // TODO Change me

            // look out- its the MURLEX!
        } catch ( MalformedURLException murlex ) {
            murlex.printStackTrace();
        } catch ( IOException ioex ) {
            ioex.printStackTrace();
        }

        return null;
    }

    // access stack overflow api
    private class readURLAsyncTask extends AsyncTask<URL, Integer, Long> {

        public Long doInBackground(URL ... urls){

            try {
                Long result;
                URL url = new URL("http://stackoverflow.com/users/22656/jon-skeet");
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));

                String inputLine;
                int i = 0;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.contains("<span class=\"count\">")) {
                        Pattern regex = Pattern.compile("<span class=\"count\">(.*?)</span>", Pattern.DOTALL);
                        Matcher matcher = regex.matcher(inputLine);
                        Pattern regex2 = Pattern.compile("<([^<>]+)>([^<>]+)</\\1>");
                        if (matcher.find()) {
                            result = Long.parseLong(matcher.group(1).replaceAll(",", ""));
                            in.close();
                            JON_SKEETS_REPUTATION = result;
                            return result;
                        }
                    }
                    i++;
                }
                in.close();

                return null;

                // look out- its the MURLEX!
            } catch ( MalformedURLException murlex ) {
                murlex.printStackTrace();
            } catch ( IOException ioex ) {
                ioex.printStackTrace();
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    // lower bound and upper bound random #
    public final int getRandomNumber(int lb, int ub) {

        // arbitrary
        readURLAsyncTask yolo = new readURLAsyncTask();
        yolo.execute(null, null);

        long startTime = System.currentTimeMillis();
        while ( JON_SKEETS_REPUTATION == null )
        {
            // wait up to 10 seconds on fetching Jon Skeet's reputation
            if ( System.currentTimeMillis() - startTime > 10000 ) break;
        }

        /* debug */
        // System.err.println(" ........................... " + JON_SKEETS_REPUTATION);

        if ( JON_SKEETS_REPUTATION != null ) {
            // do something with Jon Skeet's reputation here
        } else {
            // we tried :(
        }

        return 2;
    }

    static class Item {
        String title;
        String link;
        String description;
    }

    static class Page {
        String title;
        String link;
        String description;
        String language;
        List<Item> items;
    }

















    private class SampleQuestion {
        private final String QUESTION_STRING;
        private final boolean ANSWER;

        SampleQuestion ( final String s , final boolean a ) {
            this.QUESTION_STRING = s;
            this.ANSWER = a;
        }
    }

    /* define a bunch of sample questions and the ability to fetch them. */
    /* "YOu should really use an enum for this, you ding-dong" -Chris */
    private class SampleQuestionProvider {
        private final ArrayList<SampleQuestion> SAMPLE_QUESTIONS;

        public SampleQuestionProvider() {
            SAMPLE_QUESTIONS = new ArrayList<SampleQuestion>(30);
            populate_sample_questions();
        }

        public ArrayList<SampleQuestion> getSampleQuestions(){
            return this.SAMPLE_QUESTIONS;
        }

        /* I apologize in advance if any of these are wrong, I wrote this kinda fast -Chris*/
        private final void populate_sample_questions()
        {
            SampleQuestion q1 = new SampleQuestion(
                    "Abe Washington was the first President of the United States", false
            );
            SampleQuestion q2 = new SampleQuestion(
                    "The square root of 2 is a real number", true
            );
            SampleQuestion q3 = new SampleQuestion(
                    "Pi is a real number", true
            );
            SampleQuestion q4 = new SampleQuestion(
                    "Pi is a rational number", false
            );
            SampleQuestion q5 = new SampleQuestion(
                    "( ( 3 + 7 ) / 12 ) + ( 7 / 6 ) = 2", true
            );
            SampleQuestion q6 = new SampleQuestion(
                    "The developers of the app, \"Kwik Thinker\", are very attractive", true
            );
            SampleQuestion q7 = new SampleQuestion(
                    "RPI is located in Greece, New York", false
            );
            SampleQuestion q8 = new SampleQuestion(
                    "Google's Nexus 6 was released in late 2013", false
            );
            SampleQuestion q9 = new SampleQuestion(
                    "( ( T && !F ) || ( F && T ) )", true
            );
            SampleQuestion q10 = new SampleQuestion(
                    "1436, Christopher Columbus crossed the River Styx", false
            );
            SampleQuestion q11 = new SampleQuestion(
                    "Harvey Milk was the first openly gay person elected into California public office", true
            );
            SampleQuestion q12 = new SampleQuestion(
                    "Android Studio replaced Eclipse and its ADK library as the official IDE for Android development, as determined by Google", true
            );
            SampleQuestion q13 = new SampleQuestion(
                    "Jupiter is Roman equivalent of Greek mythology's king of the gods, Zues", true
            );
            SampleQuestion q14 = new SampleQuestion(
                    "The average lifespan for women is larger than of men", true
            );
            SampleQuestion q15 = new SampleQuestion(
                    "Git is a centralized Version Control System used widely across the Software Engineering industry", false
            );
            SampleQuestion q16 = new SampleQuestion(
                    "One of Albert Einstein's most prominent accomplishments in his life was finding the answer to the \"P=NP\" problem", false
            );
            SampleQuestion q17 = new SampleQuestion(
                    "Michael Buble was the artist behind the hit single, \"Careless Whisper\"", false
            );
            SampleQuestion q18 = new SampleQuestion(
                    "Trenton is the capital city of the state of New Jersey", true
            );
            SampleQuestion q19 = new SampleQuestion(
                    "Seal is the mastermind behind the touching piece entitled \"Kiss from a Rose\"", true
            );
            SampleQuestion q20 = new SampleQuestion(
                    "The song \"Kiss from a Rose\" was actually part of a soundtrack for a Spiderman movie", true
            );

            SAMPLE_QUESTIONS.add(q1);
            SAMPLE_QUESTIONS.add(q2);
            SAMPLE_QUESTIONS.add(q3);
            SAMPLE_QUESTIONS.add(q4);
            SAMPLE_QUESTIONS.add(q5);
            SAMPLE_QUESTIONS.add(q6);
            SAMPLE_QUESTIONS.add(q7);
            SAMPLE_QUESTIONS.add(q8);
            SAMPLE_QUESTIONS.add(q9);
            SAMPLE_QUESTIONS.add(q10);
            SAMPLE_QUESTIONS.add(q11);
            SAMPLE_QUESTIONS.add(q12);
            SAMPLE_QUESTIONS.add(q13);
            SAMPLE_QUESTIONS.add(q14);
            SAMPLE_QUESTIONS.add(q15);
            SAMPLE_QUESTIONS.add(q16);
            SAMPLE_QUESTIONS.add(q17);
            SAMPLE_QUESTIONS.add(q18);
            SAMPLE_QUESTIONS.add(q19);
            SAMPLE_QUESTIONS.add(q20);

        }
    }
}
