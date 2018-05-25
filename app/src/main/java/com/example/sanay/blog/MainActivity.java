package com.example.sanay.blog;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar mainToolbar;
    private FloatingActionButton BaddPost;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String uid;
    private BottomNavigationView mainBottomNav;
    private AccountFragment accountFragment = new AccountFragment();
    private HomeFragment homeFragment = new HomeFragment();
    private NotificationFragment notificationFragment = new NotificationFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainToolbar = findViewById(R.id.main_toolbar);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        mainBottomNav = findViewById(R.id.mainBottomNav);


        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Blog");
        BaddPost = findViewById(R.id.main_add_post);
        setFragment(homeFragment);
        if (mAuth != null) {
            BaddPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent newpostIntent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(newpostIntent);
                }
            });

            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.bottomHome:

                            setFragment(homeFragment);
                            return true;
                        case R.id.bottomNotification:
                            setFragment(notificationFragment);
                            return true;
                        case R.id.bottomAccount:
                            setFragment(accountFragment);
                            return true;
                        default:
                            return false;
                    }

                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            goToLogin();
        } else {
            uid = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                        } else {
                            //Toast.makeText(MainActivity.this, "No", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mainmenu_logout:
                logout();
                return true;
            case R.id.mainmenu_accsett:
                gotoAccSetting();
                return true;
            default:
                return false;
        }


    }

    private void gotoAccSetting() {
        Intent acc = new Intent(MainActivity.this, SetupActivity.class);
        startActivity(acc);
    }

    private void logout() {
        mAuth.signOut();
        goToLogin();
    }

    private void setFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrame, fragment);
        fragmentTransaction.commit();

    }
}
