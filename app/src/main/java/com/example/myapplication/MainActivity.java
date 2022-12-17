package com.example.myapplication;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.SoundPool;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {

    Button one, two, three, four, five;
    SwipeListener swipeListener;
    private SoundPool soundPool;
    private int sound_one, sound_two, sound_three, sound_four, sound_five, sound_startTimer, sound_stopTimer;
    private Button startTimerButton;
    private Button hideTimerButton;
    private EditText timerText;
    private int counter;
    private boolean isTimerButtonClicked = false;
    private boolean isHideTimerButtonClicked = false;
    private CountDownTimer countDownTimer = null;
    private static final int REQ_PERMISSIONS = 1;
    private String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private MediaProjectionManager mpm;
    AudioRecord record;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("DEBUG", "onCreate started");

        setContentView(R.layout.activity_main);

        //Define app to be from right to left, even if the phone is on LTR language
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        one = findViewById(R.id.one);
        two = findViewById(R.id.two);
        three = findViewById(R.id.three);
        four = findViewById(R.id.four);
        five = findViewById(R.id.five);
        startTimerButton = findViewById(R.id.startTimerBtn);
        timerText = findViewById(R.id.timerText);
        hideTimerButton = findViewById(R.id.hideTimerBtn);
        reqPermissions();
        mpm = ((MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE));


        soundPool = new SoundPool.Builder().setMaxStreams(5).build();

        sound_one = soundPool.load(this, R.raw.one, 1);
        sound_two = soundPool.load(this, R.raw.two, 1);
        sound_three = soundPool.load(this, R.raw.three, 1);
        sound_four = soundPool.load(this, R.raw.four, 1);
        sound_five = soundPool.load(this, R.raw.five, 1);
        sound_startTimer = soundPool.load(this, R.raw.starttimersound, 1);
        sound_stopTimer = soundPool.load(this, R.raw.stoptimersound, 1);

        swipeListener = new SwipeListener(one);
        swipeListener = new SwipeListener(two);
        swipeListener = new SwipeListener(three);
        swipeListener = new SwipeListener(four);
        swipeListener = new SwipeListener(five);

        startTimerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "Timer start button got clicked");
                if (!isTimerButtonClicked) {
                    counter = Integer.parseInt(timerText.getText().toString());
                    startTimerButton.setText(R.string.stop);
                    isTimerButtonClicked = true;
                    soundPool.play(sound_startTimer, 1 ,1, 0, 0, 1);
                    try {
                        Thread.sleep(3000);  // wait 3 seconds to start timer so the startTimerSound will end
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},
                                1000);
                    } else {
                        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                        if (mediaProjectionManager != null) {
                            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 2000);
                        }
                    }
                    ActivityResultLauncher<Intent> startActivityForResult = registerForActivityResult(
                            new ActivityResultContracts.StartActivityForResult(),
                            result -> {
                                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                                    Intent data = result.getData();
                                    MediaProjection mp = null;
                                    if (data != null) {
                                        mp = mpm.getMediaProjection(result.getResultCode(), data);
                                    }
                                    AudioPlaybackCaptureConfiguration config =
                                            new AudioPlaybackCaptureConfiguration.Builder(mp)
                                                    .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                                                    .build();
                                    record = new AudioRecord.Builder()
                                            .setAudioPlaybackCaptureConfig(config)
                                            .build();
                                }
                            }
                    );
                    record.startRecording();
                    countDownTimer =   new CountDownTimer(counter* 1000L, 1000){
                        public void onTick(long millisUntilFinished){
                            timerText.setText(String.valueOf(counter));
                            counter--;
                        }
                        public  void onFinish(){
                            Log.d("DEBUG", "countdown finished");
                            timerText.setText(R.string.testTime);
                            startTimerButton.setText(R.string.start);
                            isTimerButtonClicked = false;
                            soundPool.play(sound_stopTimer, 1 ,1, 0, 0, 1);
                            record.stop();
                        }
                    };
                    countDownTimer.start();
                    Log.d("DEBUG", "countdown started");


                } else {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                        Log.d("DEBUG", "countdown canceled");
                        startTimerButton.setText(R.string.start);
                        isTimerButtonClicked = false;
                    }

                }

            }
        });

        hideTimerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "hide timer button got clicked");
                if (!isHideTimerButtonClicked) {
                    timerText.setVisibility(View.INVISIBLE);
                    isHideTimerButtonClicked = true;
                    hideTimerButton.setText(R.string.show);
                } else {
                    timerText.setVisibility(View.VISIBLE);
                    isHideTimerButtonClicked = false;
                    hideTimerButton.setText(R.string.hide);
                }

            }
        });
}

    class SwipeListener implements View.OnTouchListener {
        GestureDetector gestureDetector;

        SwipeListener(View view) {
            int threshold = 100;
            int velocity_threshold = 100;

            GestureDetector.SimpleOnGestureListener listener =
                    new GestureDetector.SimpleOnGestureListener(){
                        @Override
                        public boolean onDown(MotionEvent e) {
                            return true;
                        }

                        @Override
                        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                            float xDiff = e2.getX() - e1.getX();
                            float yDiff = e2.getY() - e1.getY();

                            try {
                                if (Math.abs(xDiff) > Math.abs(yDiff)) {
                                    if ( (Math.abs(xDiff) > threshold) &&
                                            Math.abs(velocityX) > velocity_threshold ) {
                                        if (xDiff >= 0) {
                                            if(view.equals(one) ) {
                                                soundPool.play(sound_one, 1 ,1, 0, 0, 1);
                                                Log.i("DEBUG", "NOTE 1");
                                                changeTineColor(one);
                                            }

                                            if(view.equals(two)) {
                                                soundPool.play(sound_two, 1 ,1, 0, 0, 1);
                                                Log.i("DEBUG", "NOTE 2");
                                                changeTineColor(two);
                                            }

                                            if(view.equals(three)) {
                                                soundPool.play(sound_three, 1 ,1, 0, 0, 1);
                                                Log.i("DEBUG", "NOTE 3");
                                                changeTineColor(three);
                                            }

                                            if(view.equals(four)) {
                                                soundPool.play(sound_four, 1 ,1, 0, 0, 1);
                                                Log.i("DEBUG", "NOTE 4");
                                                changeTineColor(four);
                                            }

                                            if(view.equals(five)) {
                                                soundPool.play(sound_five, 1 ,1, 0, 0, 1);
                                                Log.i("DEBUG", "NOTE 5");
                                                changeTineColor(five);
                                            }

                                            return true;
                                        }

                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return false;

                        }
                    };
            gestureDetector = new GestureDetector(getApplicationContext(), listener);
            view.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return gestureDetector.onTouchEvent(motionEvent);
        }

        // When the tine is pressed, change the color for 1/5 a second then change back
        public void changeTineColor(Button tinePressed) {
            tinePressed.setPressed(true);
            Log.d("DEBUG", tinePressed +"changed color");
            new CountDownTimer(200, 1000) {

                @Override
                public void onTick(long arg0) {

                }

                @Override
                public void onFinish() {
                    tinePressed.setPressed(false);
                    Log.d("DEBUG", tinePressed +"changed color back to default");
                }
            }.start();
        }

    }

    private void reqPermissions() {
        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, permissions, REQ_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_PERMISSIONS) {
            if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                finish();
            }
        }
    }
}