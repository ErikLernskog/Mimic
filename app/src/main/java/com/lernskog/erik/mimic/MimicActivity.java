package com.lernskog.erik.mimic;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;


public class MimicActivity extends Activity implements MediaPlayer.OnCompletionListener {
    private static final String LOG = "Mimic";
    private static final String FILENAME = "my_mimic.3gp";
    private Button recordButton, playButton, deleteButton;
    private CheckBox repeat;
    private ImageView recordingImage;
    private File file;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private AlphaAnimation blink;
    private AtomicBoolean repeatPlay = new AtomicBoolean(false);
    private Drawable[] playButtonIcons;
    private final int PLAY = 0, STOP = 2;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_mimic);

        recordButton = (Button) findViewById(R.id.record);
        playButton = (Button) findViewById(R.id.play);
        deleteButton = (Button) findViewById(R.id.delete);
        recordingImage = (ImageView) findViewById(R.id.recording);
        repeat = (CheckBox) findViewById(R.id.repeat);

        blink = new AlphaAnimation(1, 0);
        blink.setDuration(500);
        blink.setInterpolator(new LinearInterpolator());
        blink.setRepeatCount(Animation.INFINITE);
        blink.setRepeatMode(Animation.REVERSE);

        file = new File(Environment.getExternalStorageDirectory(), FILENAME);
        playButton.setEnabled(hasRecording());
        deleteButton.setEnabled(hasRecording());

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        playButtonIcons = playButton.getCompoundDrawables();
        playButton.setCompoundDrawables(playButtonIcons[PLAY], null, null, null);
        repeatPlay.set(false);
        repeat.setChecked(repeatPlay.get());
    }

    public void record(View v) {
        //...kod ska skrivas här
        if (recordButton.getText().equals("Stop")) {
            stopRecording();
        }
        else {
            startRecording();
        }

    }

    public void play(View v) {
        //...kod ska skrivas här
        startPlayback();
    }

    public void delete(View v) {
        if (file.exists()) {
            //...kod ska skrivas här
            if (!file.delete()) {
                Log.e(LOG, "Failed to delete file");
            }
            Toast.makeText(this, getString(R.string.message_file_deleted), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getString(R.string.message_no_file), Toast.LENGTH_SHORT).show();
        }
    }

    public void repeat(View v) {
        boolean on = ((CheckBox) v).isChecked();
        repeatPlay.set(on);
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(file.getAbsolutePath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();

            recordButton.setText(getString(R.string.button_stop));
            playButton.setEnabled(false);
            recordingImage.setVisibility(View.VISIBLE);
            recordingImage.startAnimation(blink);
        } catch (IOException e) {
            Log.e(LOG, "Fel vid inspelning!", e);
            mediaRecorder = null;
        }
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

        recordingImage.setVisibility(View.INVISIBLE);
        recordingImage.clearAnimation();
        recordButton.setText(getString(R.string.button_record));
        playButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }

    private void startPlayback() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.prepare();
            mediaPlayer.start();

            playButton.setText(getString(R.string.button_stop));
            playButton.setCompoundDrawables(playButtonIcons[STOP], null, null, null);
            recordButton.setEnabled(false);
            deleteButton.setEnabled(false);
        } catch (IOException e) {
            Log.e(LOG, "Fel vid uppspelning!", e);
        }
    }

    private void stopPlayback() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;

        playButton.setText(getString(R.string.button_play));
        playButton.setCompoundDrawables(playButtonIcons[PLAY], null, null, null);
        recordButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (repeatPlay.get()) {
            mediaPlayer.start();
        } else {
            stopPlayback();
        }
    }

    private boolean hasRecording() {
        return file.exists() && file.canRead() && file.length() > 0;
    }
}

