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

public class RegisterActivity extends AppCompatActivity {

    EditText EDregEmail,EDregPassword,EDregConfirmPassword;
    Button BregRegister,BregLogin;
    ProgressBar regProgressbar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EDregEmail = findViewById(R.id.register_email);
        EDregPassword = findViewById(R.id.register_password);
        EDregConfirmPassword =findViewById(R.id.register_confirm_password);
        BregRegister = findViewById(R.id.reg_createaccount);
        BregLogin = findViewById(R.id.register_gotologin);
        regProgressbar  = findViewById(R.id.register_progress);
        mAuth = FirebaseAuth.getInstance();


        BregRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = EDregEmail.getText().toString(),
                        pass= EDregPassword.getText().toString(),
                        confPass = EDregConfirmPassword.getText().toString();

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(confPass))
                    BregRegister.setEnabled(false);
                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confPass))
                {
                    regProgressbar.setVisibility(View.VISIBLE);
                    BregRegister.setEnabled(true);
                    if(pass.equals(confPass))
                    {
                        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {
                                    Intent accIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(accIntent);
                                    finish();
                                }
                                else
                                {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error: "+ error, Toast.LENGTH_SHORT).show();
                                }
                                regProgressbar.setVisibility(View.INVISIBLE);
                            }


                        });


                    }
                }

            }
        });


        BregLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser!=null)
            goToMain();
    }

    private void goToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
