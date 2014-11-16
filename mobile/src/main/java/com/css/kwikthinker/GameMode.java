package com.css.kwikthinker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.css.kwikthinker.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class GameMode extends Activity {
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
    Button startButton;
    // im british... and pissed!
    private static final int[] COLOURS = {
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.MAGENTA,
        Color.CYAN,
        Color.YELLOW,
        Color.WHITE };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        countdownProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        countdownProgressBar.setVisibility(View.INVISIBLE);
        countdownProgressBar.setMax(5000); // TODO GLOBAL CONST
        countdownProgressBar.setProgress(5000);

        countdownTV = (TextView)findViewById(R.id.countdownTV);
        /*countdownTV.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT,
                0.75f));); // TODO take up 75% of parent width - can't do in XML */
        startButton = (Button)findViewById(R.id.start_em_countdown);
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
        countdownProgressBar.setVisibility(View.VISIBLE);
        countdownProgressBar.setProgress(5000);
        ViewGroup v = (ViewGroup)startButton.getParent();
        v.removeView(startButton);
        CountDownTimer cdt = new CountDownTimer(5000, 10) {
            public void onTick(long ms) {
                long s = ms / NUM_SECONDS_PER_MILLISECOND ;
                int a = (int)( ( s / 5.0 ) * 255 ); // TODO global consts
                if ( ms < 50 ) {
                    countdownTV.setText("");
                    return;
                }
                countdownTV.setText(String.valueOf(s));
                countdownTV.setTextColor(countdownTV.getTextColors().withAlpha(a));
            }

            @Override
            public void onFinish() {
                spawnQuestion();
            }
        };
        // do countdown
        cdt.start();
    }

    private void spawnQuestion() {
        // TODO RANDOM SEEDS
        //      background color
        //      question

        countdownProgressBar.setProgress(5000);

        countdownTV.setTextColor(countdownTV.getTextColors().withAlpha(255));
        int colour = (int) Math.floor(Math.random() * 7); // im british... and pissed!
        FrameLayout layout = (FrameLayout) findViewById( R.id.EM_frame_layout);
        layout.setBackgroundColor(COLOURS[colour]);
        countdownTV.setTextSize(40);
        countdownTV.setTextColor(Color.BLACK);
        // TODO k-v pairs: (Question : boolean answer)
        // TODO k-v pairs: (QuestionString, TextSize)
        // TODO the above solution: great for hackathon, terrible for the long run
        countdownTV.setText("Abe Washington was the first President of the United States");


        CountDownTimer cdt = new CountDownTimer(5000, 10) {
            public void onTick(long ms) {
                countdownProgressBar.setProgress(countdownProgressBar.getProgress()-16);
            }

            @Override
            public void onFinish() {
                spawnQuestion();
            }
        }.start();


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
}
