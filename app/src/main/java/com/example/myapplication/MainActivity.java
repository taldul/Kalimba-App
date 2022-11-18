package com.example.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.SoundPool;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button one, two, three, four, five;
    SwipeListener swipeListener;
    private SoundPool soundPool;
    private int sound_one, sound_two, sound_three, sound_four, sound_five;
    private Button startButton;
    private static final int REQ_PERMISSIONS = 1;
    private String [] permissions = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private Intent mProjectData;
    private boolean isStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        reqPermissions();

        one= (Button) findViewById(R.id.one);
        two= (Button) findViewById(R.id.two);
        three= (Button) findViewById(R.id.three);
        four= (Button) findViewById(R.id.four);
        five= (Button) findViewById(R.id.five);
        startButton = (Button) findViewById(R.id.startButton);

        soundPool = new SoundPool.Builder().setMaxStreams(5).build();

        sound_one = soundPool.load(this, R.raw.one, 1);
        sound_two = soundPool.load(this, R.raw.two, 1);
        sound_three = soundPool.load(this, R.raw.three, 1);
        sound_four = soundPool.load(this, R.raw.four, 1);
        sound_five = soundPool.load(this, R.raw.five, 1);

        swipeListener = new SwipeListener(one);
        swipeListener = new SwipeListener(two);
        swipeListener = new SwipeListener(three);
        swipeListener = new SwipeListener(four);
        swipeListener = new SwipeListener(five);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isStarted) {
                    startButton.setText("Start");
                    isStarted = false;
                } else {
                    startButton.setText("Stop");
                    isStarted = true;
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
                                        if (xDiff <= 0) {
                                            if(view.equals(one)) {
                                                soundPool.play(sound_one, 1 ,1, 0, 0, 1);
                                                Log.i("MY UNIQ TAG", "NOTE 1");
                                            }

                                            if(view.equals(two)) {
                                                soundPool.play(sound_two, 1 ,1, 0, 0, 1);
                                                Log.i("MY UNIQ TAG", "NOTE 2");
                                            }

                                            if(view.equals(three)) {
                                                soundPool.play(sound_three, 1 ,1, 0, 0, 1);
                                                Log.i("MY UNIQ TAG", "NOTE 3");
                                            }

                                            if(view.equals(four)) {
                                                soundPool.play(sound_four, 1 ,1, 0, 0, 1);
                                                Log.i("MY UNIQ TAG", "NOTE 4");
                                            }

                                            if(view.equals(five)) {
                                                soundPool.play(sound_five, 1 ,1, 0, 0, 1);
                                                Log.i("MY UNIQ TAG", "NOTE 5");
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
            gestureDetector = new GestureDetector(listener);
            view.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return gestureDetector.onTouchEvent(motionEvent);
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