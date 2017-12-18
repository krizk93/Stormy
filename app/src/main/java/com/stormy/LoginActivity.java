package com.stormy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    private Button mSignUpBtn;
    private EditText mEmailField, mPasswordField;
    private TextView mSignInTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSignUpBtn = findViewById(R.id.btn_sign_up);
        mEmailField = findViewById(R.id.et_email);
        mPasswordField = findViewById(R.id.et_password);
        mSignInTv = findViewById(R.id.tv_sign_in);

        mSignInTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });
    }

}
