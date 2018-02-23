package com.dmi.meetingrecorder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

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
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    String meetingName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mRecordSampleBtn = findViewById(R.id.record_sample_btn);
        mStartMeeting = findViewById(R.id.record_icon);

        ActivityCompat.requestPermissions(HomeActivity.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        mRecordSampleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isRecording) {
                    startService(new Intent(HomeActivity.this, AudioService.class));
                    mRecordSampleBtn.setText("Stop");
                } else {
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

                askMeetingNameAndStartRecording();
            }
        });
    }

    private void askMeetingNameAndStartRecording() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("Meeting Name");
        alertDialog.setMessage("Enter your meeting name");

        final EditText input = new EditText(HomeActivity.this);
        input.setText("Meeting01");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_launcher);

        alertDialog.setPositiveButton("Start",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        meetingName = input.getText().toString();
                        if (meetingName.compareTo("") != 0) {
                            Intent intent=new Intent(HomeActivity.this, MainActivity.class);
                            intent.putExtra("MeetingName", meetingName);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Empty meeting names not allowed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }
}
