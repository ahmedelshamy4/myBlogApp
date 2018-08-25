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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText current_password;
    private EditText new_password;
    private EditText new_password_confirm;
    private Button change_paassword_button;
    private ProgressBar mProgressBar;
    Toolbar change_password_toolbar;
    private FirebaseAuth firebaseAuth;
    private String user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        firebaseAuth = FirebaseAuth.getInstance();

        change_password_toolbar = findViewById(R.id.change_pass_toolbar);
        setSupportActionBar(change_password_toolbar);
        getSupportActionBar().setTitle("Change Password");

        user_email = firebaseAuth.getCurrentUser().getEmail();

        initViews();
        onClicks();
    }


    private void initViews() {
        current_password = findViewById(R.id.change_pass_current);
        new_password = findViewById(R.id.change_pass_new);
        new_password_confirm = findViewById(R.id.change_pass_new_confirm);
        change_paassword_button = findViewById(R.id.change_pass_btn);
        mProgressBar = findViewById(R.id.change_pass_progress);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void onClicks() {
        change_paassword_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String current_pass = current_password.getText().toString();
                String new_pass = new_password.getText().toString();
                final String confirm_new_pass = new_password_confirm.getText().toString();
                if (!TextUtils.isEmpty(current_pass) && !TextUtils.isEmpty(new_pass) && !TextUtils.isEmpty(confirm_new_pass)) {
                    if (confirm_new_pass.equals(new_pass)) {
                        if (!new_pass.equals(current_pass)) {
                            mProgressBar.setVisibility(View.VISIBLE);

                            AuthCredential authCredential = EmailAuthProvider.getCredential(user_email, current_pass);
                            firebaseAuth.getCurrentUser().reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        firebaseAuth.getCurrentUser().updatePassword(confirm_new_pass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(ChangePasswordActivity.this, "Password changed Successfully!", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(ChangePasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(ChangePasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                }

                            });
                        } else {
                            Toast.makeText(ChangePasswordActivity.this, "Current & New Password cannot be the same", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(ChangePasswordActivity.this, "All Fields are Mandatory", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
