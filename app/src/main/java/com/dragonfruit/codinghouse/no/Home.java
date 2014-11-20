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
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.media.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


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
    float standardGravity;
    float thresholdGraqvity;
    Boolean recorderOn = false;
    private SensorManager mySensorManager;
    private Sensor myGravitySensor;
    Boolean oriantaton = true;
    MediaRecorder recorder = new MediaRecorder();
    String mFileName;
    RecordAudio recordTask;
    PlayAudio playTask;
    File recordingFile;
    boolean isRecording = false,isPlaying = false;

    int frequency = 11025,channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    protected void onCreate(Bundle savedInstanceState) {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/nosound.m4a";
        recordingFile = new File(mFileName);
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
        sound0 = soundPool0.load(this, R.raw.intro, 1);
        super.onCreate(savedInstanceState);
        noButton = new ImageButton(this);
        recButton = new ImageButton(this);
        main = new RelativeLayout(this);
        nblayout.addRule(RelativeLayout.CENTER_HORIZONTAL);
        nblayout.addRule(RelativeLayout.CENTER_VERTICAL);
        recButton.setBackgroundColor(Color.TRANSPARENT);

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int widtha = (width*9)/10;
        rblayout = new RelativeLayout.LayoutParams(width/4,width/4);
        rblayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rblayout.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        rblayout.setMargins(2*(width/3),size.y/2+(width/2)-(width/3),0,0);
        recButton.setImageBitmap(drawableToBitmap(getResources().getDrawable(R.drawable.rec_default), rblayout.width));
        recButton.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View view, MotionEvent event) {

                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        record();
                        recorderOn = true;
                        noButton.setBackgroundColor(Color.parseColor("#ffff1842"));
                        //recButton.setBackgroundColor(Color.parseColor("#ffff1842"));
                        break;
                    case MotionEvent.ACTION_UP:
                        stopRecording();

                        customSound = true;
                        noButton.setBackgroundColor(Color.WHITE);
                       // recButton.setBackgroundColor(Color.parseColor("#ff3399ff"));
                        break;

                }


                return false;
            }
        });

        noButton.setImageBitmap(drawableToBitmap(getResources().getDrawable(R.drawable.no_default), widtha));
        noButton.setBackgroundColor(Color.WHITE);
        noButton.setSoundEffectsEnabled(false);
        noButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (playSound) {
                    if(customSound){if(!isPlaying)play();}
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
    public void record() {
        isPlaying = false;
        recordTask = new RecordAudio();
        recordTask.execute();
    }
    public void play() {
        playTask = new PlayAudio();
        playTask.execute();
    }
    public void stopPlaying() {
        isPlaying = false;
    }
    public void stopRecording() {
        isRecording = false;
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



private class PlayAudio extends AsyncTask<Void, Integer, Void> {
    @Override
    protected Void doInBackground(Void... params) {
        isPlaying = true;

        int bufferSize = AudioTrack.getMinBufferSize(frequency,channelConfiguration, audioEncoding);
        short[] audiodata = new short[bufferSize / 4];

        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(recordingFile)));
            AudioTrack audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC, frequency,
                    channelConfiguration, audioEncoding, bufferSize,
                    AudioTrack.MODE_STREAM);
            audioTrack.setStereoVolume(5.0f,5.0f);
            audioTrack.play();
            while (isPlaying && dis.available() > 0) {
                int i = 0;
                while (dis.available() > 0 && i < audiodata.length) {
                    audiodata[i] = dis.readShort();
                    i++;
                }
                audioTrack.write(audiodata, 0, audiodata.length);
            }
            dis.close();
        } catch (Throwable t) {
            Log.e("AudioTrack", "Playback Failed");
        }
        isPlaying = false;
        return null;

    }
}

private class RecordAudio extends AsyncTask<Void, Integer, Void> {
    @Override
    protected Void doInBackground(Void... params) {
        isRecording = true;
        try {
            DataOutputStream dos = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(
                            recordingFile)));
            int bufferSize = AudioRecord.getMinBufferSize(frequency,
                    channelConfiguration, audioEncoding);
            AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, frequency,
                    channelConfiguration, audioEncoding, bufferSize);

            short[] buffer = new short[bufferSize];
            audioRecord.startRecording();
            int r = 0;
            while (isRecording) {
                int bufferReadResult = audioRecord.read(buffer, 0,
                        bufferSize);
                for (int i = 0; i < bufferReadResult; i++) {
                    dos.writeShort(buffer[i]);
                }
                publishProgress(new Integer(r));
                r++;
            }
            audioRecord.stop();
            dos.close();
        } catch (Throwable t) {
            Log.e("AudioRecord", "Recording Failed");
        }
        return null;
    }
    protected void onProgressUpdate(Integer... progress) {
    }
    protected void onPostExecute(Void result) {
    }
}
}
