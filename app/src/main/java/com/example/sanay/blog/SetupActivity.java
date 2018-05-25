package com.example.sanay.blog;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView accCircleImageView;
    private Uri mainImageURI = null;
    private Toolbar accToolbar;
    private boolean isChanged = false;
    private EditText accName;
    private Button accSave;
    private String username;
    private String uId;
    private FirebaseAuth mAuth;
    private StorageReference sRef;
    private FirebaseFirestore fStoreRef;


    private ProgressBar accProgress;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageURI = result.getUri();
                accCircleImageView.setImageURI(mainImageURI);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                String err = error.getMessage();
                Toast.makeText(this, "Error: " + err, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        accToolbar = findViewById(R.id.acc_toolbar);
        setSupportActionBar(accToolbar);
        getSupportActionBar().setTitle("Account settings");

        accCircleImageView = findViewById(R.id.acc_profile);
        accName = findViewById(R.id.acc_name);
        accSave = findViewById(R.id.acc_save);
        mAuth = FirebaseAuth.getInstance();
        uId = mAuth.getCurrentUser().getUid();
        sRef = FirebaseStorage.getInstance().getReference();
        accProgress = findViewById(R.id.acc_progressbar);
        fStoreRef = FirebaseFirestore.getInstance();

        accProgress.setVisibility(View.VISIBLE);
        accSave.setEnabled(false);
        fStoreRef.collection("users").document(uId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        mainImageURI = Uri.parse(image);
                        RequestOptions placeholdersRequest = new RequestOptions();
                        placeholdersRequest.placeholder(R.drawable.default_profile);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholdersRequest).load(image).into(accCircleImageView);
                        accName.setText(name);
                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "FireStore retrieve error: " + error, Toast.LENGTH_SHORT).show();
                }

                accProgress.setVisibility(View.INVISIBLE);
                accSave.setEnabled(true);
            }
        });

        accCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if ((ContextCompat.checkSelfPermission(SetupActivity.this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                            && (ContextCompat.checkSelfPermission(SetupActivity.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{READ_EXTERNAL_STORAGE}, 1);
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 1);
                    } else {
                        BringImagePicker();
                    }

                } else {
                    BringImagePicker();
                }
            }
        });

        accSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                username = accName.getText().toString();
                if (!TextUtils.isEmpty(username) && mainImageURI != null) {
                    accProgress.setVisibility(View.VISIBLE);
                    if (isChanged) {
                        StorageReference imagePath = sRef.child("profile_images").child(uId + ".jpg");
                        imagePath.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    storeFirestore(task, username);
                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                }
                                accProgress.setVisibility(View.INVISIBLE);
                            }
                        });

                    } else {
                        storeFirestore(null, username);
                    }
                }


            }
        });


    }

    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, String username) {
        Uri downloadUri;
        if (task != null) {
            downloadUri = task.getResult().getDownloadUrl();
        } else {
            downloadUri = mainImageURI;
        }
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", username);
        userMap.put("image", downloadUri.toString());
        fStoreRef.collection("users").document(uId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SetupActivity.this, "Account Settings Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }

            }
        });
    }

    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);
    }


}
