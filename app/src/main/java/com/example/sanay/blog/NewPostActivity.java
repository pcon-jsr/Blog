package com.example.sanay.blog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.data.TextFilterable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    //UI elements Variables
    private Toolbar newPostToolbar;
    private ImageView newPostImage;
    private EditText newPostEditText;
    private Button newPostButton;

    //URI,progressBar variables
    private Uri postImageUri = null;
    private ProgressBar postProgress;

    //Firebase Variables
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    //Image Compression Variables
    private Bitmap compressedImageFile;

    //Strings
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        //FireBase Initialisation
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //String Initialisation
        currentUserId = mAuth.getCurrentUser().getUid();

        //Toolbar initialisation
        newPostToolbar = findViewById(R.id.newpost_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //UI elements initialisation
        newPostImage = findViewById(R.id.PostImage);
        newPostEditText = findViewById(R.id.postDescription);
        newPostButton = findViewById(R.id.postButton);
        postProgress = findViewById(R.id.post_progress);

        //Click listener for Image
        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //On Clicking the image the crop tool opens
                //Use of https://github.com/ArthurHub/Android-Image-Cropper Library
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1, 1)
                        .start(NewPostActivity.this);
            }
        });


        //Setup OnClickListener for the post button
        //works only when both the image description and the image are set
        //use of random string UUID generator

        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String desc = newPostEditText.getText().toString();
                if(!TextUtils.isEmpty(desc) && postImageUri !=null)
                {
                    postProgress.setVisibility(View.VISIBLE);
                    final String randomName = UUID.randomUUID().toString();

                    //Photo Upload--
                    //The File variable stores the path of the image uri.
                    File newImageFile = new File((postImageUri.getPath()));

                    try {
                        //use of image compression library https://github.com/zetbaitsu/Compressor
                        //compression for original image
                        compressedImageFile = new Compressor(NewPostActivity.this)
                                .setMaxHeight(720)
                                .setMaxWidth(720)
                                .setQuality(50)
                                .compressToBitmap(newImageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //from Firebase documentation: https://firebase.google.com/docs/storage/android/upload-files
                    //converts the image into a byte stream and then uploads it
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                    byte[] imageData = baos.toByteArray();

                    //Checking the status of the uploading file
                    UploadTask filePath = storageReference.child("post_images")
                            .child(randomName+".jpg")
                            .putBytes(imageData);

                    //on Completion of the download
                    //Use of the firebase documentation : https://firebase.google.com/docs/storage/android/upload-files
                    filePath.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            final String downloadUri = task.getResult().getDownloadUrl().toString();

                            if(task.isSuccessful())
                            {
                                //creating the new thumbnail file
                                File newThumbFile = new File(postImageUri.getPath());
                                //compressing the thumbnail image with high compression ratio so as to reduce the size greatly
                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(100)
                                            .setMaxWidth(100)
                                            .setQuality(1)
                                            .compressToBitmap(newThumbFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                byte[] thumbData = baos.toByteArray();

                                UploadTask uploadTask = storageReference.child("post_images/thumbs")
                                        .child(randomName+".jpg").putBytes(thumbData);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        String downloadThumbUri = taskSnapshot.getDownloadUrl().toString();
                                        Map<String,Object> postMap = new HashMap<>();
                                        postMap.put("imageurl",downloadUri);
                                        postMap.put("description",desc);
                                        postMap.put("userid",currentUserId);
                                        postMap.put("thumbnailurl", downloadThumbUri);
                                        postMap.put("timestamp", FieldValue.serverTimestamp());


                                        firebaseFirestore.collection("posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(NewPostActivity.this, "Posted", Toast.LENGTH_SHORT).show();
                                                    Intent mainIntent = new Intent(NewPostActivity.this,MainActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();
                                                }
                                                else
                                                {
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(NewPostActivity.this, "Error: "+ error, Toast.LENGTH_SHORT).show();
                                                }
                                                postProgress.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        String error = e.getMessage();
                                        Toast.makeText(NewPostActivity.this, "Error: "+ error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else
                            {
                                postProgress.setVisibility(View.INVISIBLE);
                            }
                        }
                    });

                }
            }
        });
    }

    //Overriding function for the ImageCropping tool.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                String err = error.getMessage();
                Toast.makeText(this, "Error: " + err, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
