package com.dmi.meetingrecorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

/**
 * Created by Psingh on 2/23/2018.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private Toolbar mToolbar;
    private Button mLoginBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mLoginBtn=(Button)findViewById(R.id.login_btn);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        mLoginBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(LoginActivity.this,HomeActivity.class));
    }
}
