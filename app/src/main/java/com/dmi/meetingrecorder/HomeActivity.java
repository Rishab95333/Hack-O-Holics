package com.dmi.meetingrecorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by Psingh on 2/23/2018.
 */

public class HomeActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private Button mRecordSampleBtn;
    ImageView mStartMeeting;
    boolean isRecording = false;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mRecordSampleBtn= findViewById(R.id.record_sample_btn);
        mStartMeeting=findViewById(R.id.record_icon);

        ActivityCompat.requestPermissions(HomeActivity.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        mRecordSampleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isRecording){
                    startService(new Intent(HomeActivity.this, AudioService.class));
                    mRecordSampleBtn.setText("Stop");
                }else{
                    stopService(new Intent(HomeActivity.this,
                            AudioService.class));
                    mRecordSampleBtn.setText(getResources().getString(R.string.record_sample));
                }
                isRecording = !isRecording;
            }
        });

        mStartMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }
}
