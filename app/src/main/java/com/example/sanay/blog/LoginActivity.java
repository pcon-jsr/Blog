package com.example.sanay.blog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText ETloginEmail,ETloginPass;
    Button BloginLogin, BloginRegister;
    ProgressBar loginProgressBar;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ETloginEmail = findViewById(R.id.login_email);
        ETloginPass = findViewById(R.id.login_password);
        BloginLogin = findViewById(R.id.login_login);
        BloginRegister = findViewById(R.id.login_register);
        loginProgressBar = findViewById(R.id.login_progressbar);

        //firebase objects
        mAuth = FirebaseAuth.getInstance();


        BloginLogin.setEnabled(true);

        BloginLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loginEmail = ETloginEmail.getText().toString();
                String loginPassword = ETloginPass.getText().toString();
                if(TextUtils.isEmpty(loginEmail) || TextUtils.isEmpty(loginPassword))
                    BloginLogin.setEnabled(false);
                if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword))
                {
                    loginProgressBar.setVisibility(View.VISIBLE);
                    BloginLogin.setEnabled(true);
                    mAuth.signInWithEmailAndPassword(loginEmail,loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                gotoMain();
                            }

                            else
                            {
                                String error = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error: "+ error, Toast.LENGTH_SHORT).show();
                            }
                            loginProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });


                }
            }
        });

        BloginRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoRegister();
            }
        });

    }

    private void gotoRegister() {
        Intent gotoRegintent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(gotoRegintent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser!=null)
        {
            gotoMain();
        }
    }

    private void gotoMain() {
        Intent gotomainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(gotomainIntent);
        finish();
    }
}
