package com.example.ahmed.myblogapp.showActivity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ahmed.myblogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class passwordForgetActivity extends AppCompatActivity {
    EditText forgot_pass_email;
    Button forgot_pass_btn;
    Toolbar forgot_pass_toolbar;
    ProgressBar mProgressBar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_forget);

        mAuth = FirebaseAuth.getInstance();
        forgot_pass_toolbar = findViewById(R.id.forgot_pass_toolbar);
        forgot_pass_email = findViewById(R.id.forgot_pass_email);
        forgot_pass_btn = findViewById(R.id.forgot_pass_btn);
        mProgressBar = findViewById(R.id.forgot_pass_progress);

        setSupportActionBar(forgot_pass_toolbar);
        getSupportActionBar().setTitle("Forgot Password ?");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        forgot_pass_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = forgot_pass_email.getText().toString();
                if (!TextUtils.isEmpty(email)) {
                mProgressBar.setVisibility(View.VISIBLE);

                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(passwordForgetActivity.this, "Email Sent Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(passwordForgetActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                } else {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(passwordForgetActivity.this, "Please Enter the Registered Email Address", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}
