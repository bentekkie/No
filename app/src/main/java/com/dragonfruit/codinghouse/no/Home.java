package com.dragonfruit.codinghouse.no;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.media.*;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class Home extends Activity implements SensorEventListener {

    static RelativeLayout main;
    RelativeLayout.LayoutParams nblayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    RelativeLayout.LayoutParams rblayout;
    ImageButton noButton;
    ImageButton recButton;
    SoundPool soundPool0;
    SoundPool soundPool1;
    SoundPool soundPool2;
    Boolean customSound = false;
    Boolean playSound = false;
    int sound0;
    int sound1;
    int sound2;
    float standardGravity;
    float thresholdGraqvity;
    Boolean recorderOn = false;
    private SensorManager mySensorManager;
    private Sensor myGravitySensor;
    Boolean oriantaton = true;
    MediaRecorder recorder = new MediaRecorder();
    FileOutputStream fos ;
    String mFileName;
    protected void onCreate(Bundle savedInstanceState) {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/nosound.m4a";
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(mFileName);
        standardGravity = SensorManager.STANDARD_GRAVITY;
        thresholdGraqvity = standardGravity/2;
        mySensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        myGravitySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        soundPool0 = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundPool1 = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundPool2 = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        sound0 = soundPool0.load(this, R.raw.noaud, 1);
        sound1 = soundPool1.load(this, R.raw.onaud, 1);
        super.onCreate(savedInstanceState);
        noButton = new ImageButton(this);
        recButton = new ImageButton(this);
        main = new RelativeLayout(this);
        nblayout.addRule(RelativeLayout.CENTER_HORIZONTAL);
        nblayout.addRule(RelativeLayout.CENTER_VERTICAL);
        recButton.setBackgroundColor(Color.WHITE);

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        rblayout = new RelativeLayout.LayoutParams(width/4,width/4);
        rblayout.addRule(RelativeLayout.ALIGN_PARENT_END);
        rblayout.addRule(RelativeLayout.ALIGN_BASELINE);
        recButton.setImageBitmap(drawableToBitmap(getResources().getDrawable(R.drawable.rec_default), rblayout.width));
        recButton.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View view, MotionEvent event) {

                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        try {
                            recorder.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        recorder.start();
                        recorderOn = true;
                        noButton.setBackgroundColor(Color.parseColor("#ffff1842"));
                        recButton.setBackgroundColor(Color.parseColor("#ffff1842"));
                        break;
                    case MotionEvent.ACTION_UP:
                        if(recorderOn) recorder.stop();
                        recorder.release();
                        recorder = new MediaRecorder();
                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                        try {
                            FileInputStream fs = new FileInputStream(mFileName);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        recorder.setOutputFile(mFileName);
                        sound2 = soundPool2.load(mFileName, 1);
                        customSound = true;
                        noButton.setBackgroundColor(Color.WHITE);
                        recButton.setBackgroundColor(Color.WHITE);

                }


                return false;
            }
        });
        int widtha = (width*9)/10;
        noButton.setImageBitmap(drawableToBitmap(getResources().getDrawable(R.drawable.no_default), widtha));
        noButton.setBackgroundColor(Color.WHITE);
        noButton.setSoundEffectsEnabled(false);
        noButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (playSound) {
                    if(customSound) soundPool2.play(sound2,1.0f,1.0f,0,0,1.0f);
                    else if (!oriantaton) soundPool1.play(sound1, 1.0f, 1.0f, 0, 0, 1.0f);
                    else soundPool0.play(sound0, 1.0f, 1.0f, 0, 0, 1.0f);
                    playSound = false;
                }
            }
        });
        noButton.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View view, MotionEvent event) {

                float eventX = event.getX();
                float eventY = event.getY();
                float[] eventXY = new float[]{eventX, eventY};

                Matrix invertMatrix = new Matrix();
                ((ImageView) view).getImageMatrix().invert(invertMatrix);

                invertMatrix.mapPoints(eventXY);
                int x = (int) eventXY[0];
                int y = (int) eventXY[1];


                Drawable imgDrawable = ((ImageView) view).getDrawable();
                Bitmap bitmap = ((BitmapDrawable) imgDrawable).getBitmap();


                //Limit x, y range within bitmap
                if (x < 0) x = 0;
                else if (x > bitmap.getWidth() - 1) x = bitmap.getWidth() - 1;

                if (y < 0) y = 0;
                else if (y > bitmap.getHeight() - 1) y = bitmap.getHeight() - 1;

                int touchedRGB = bitmap.getPixel(x, y);

                //soundPool0.play(sound0, 1.0f, 1.0f, 0, 0, 1.0f);
                if (touchedRGB == Color.parseColor("#ff3399ff")) playSound = true;



                return false;
            }
        });
        super.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        main.addView(noButton, nblayout);
        main.addView(recButton, rblayout);
        //super.setTheme(android.R.style.Theme_Black_NoTitleBar);
        super.setContentView(main);
    }

    public Bitmap drawableToBitmap (Drawable drawable, int width) {



        Bitmap bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
    @Override
    protected void onResume() {
        super.onResume();
        mySensorManager.registerListener(
                this,
                myGravitySensor,
                SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mySensorManager.unregisterListener(this);
    }

    public void onSensorChanged(SensorEvent event) {
        Sensor source = event.sensor;
        float z = event.values[1];
        if(source.getType() == Sensor.TYPE_GRAVITY) oriantaton = z >= 0;

    }

    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    }

