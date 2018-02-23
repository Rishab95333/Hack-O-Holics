package com.dmi.meetingrecorder;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioService extends Service
             implements MediaRecorder.OnInfoListener {
    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;

    private MediaRecorder mRecorder = null;
    //setting maximum file size to be recorded
    private long Audio_MAX_FILE_SIZE = 1000000;//1Mb

    private int[] amplitudes = new int[100];
    private int i = 0;

    private File mOutputFile;
    private long mStartTime;
    private MediaPlayer   mPlayer = null;

    @Override
    public void onCreate() {
        super.onCreate();
        // Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
        Log.d("filename",mFileName);
    }

    @Override
    public int onStartCommand(Intent intent, int flags,
            int startId) {
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        startRecording();
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    protected void stopRecording(boolean saveFile) {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

        // to stop the service by itself
        stopSelf();
    }

    private File getOutputFile() {
        SimpleDateFormat dateFormat = new SimpleDateFormat
                ("yyyyMMdd_HHmmssSSS", Locale.US);
        return new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().toString()
                + "/MeetingApp/RECORDING_"
                + dateFormat.format(new Date())
                + ".m4a");
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        //check whether file size has reached to 1MB to stop recording
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
            stopRecording(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Recording","Stopped");
        stopRecording(true);
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }


  }