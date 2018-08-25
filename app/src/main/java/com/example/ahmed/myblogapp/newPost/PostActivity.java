package com.example.ahmed.myblogapp.newPost;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ahmed.myblogapp.showActivity.MainActivity;
import com.example.ahmed.myblogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

public class PostActivity extends AppCompatActivity {
    Toolbar toolbar_new_post;
    ImageView newPostImage, uploadIcon;
    EditText newPostDesc;
    Button postBtn;
    TextView character_limit;
    ProgressBar mProgressBar;
    Uri postImageUri = null;
    Bitmap compressedImageFile;


    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        toolbar_new_post = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(toolbar_new_post);
        getSupportActionBar().setTitle("Add New Post");
        initViews();
        onClicks();
    }

    private void onClicks() {
        newPostDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                character_limit.setText(newPostDesc.getText().length() + "/50");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(2, 1)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .start(PostActivity.this);
            }
        });
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewPost();
            }
        });
    }

    private void addNewPost() {
        final String dec = newPostDesc.getText().toString();

        if (!TextUtils.isEmpty(dec) && postImageUri != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            final String randomName = UUID.randomUUID().toString();
            Toast.makeText(this, "Uploading image", Toast.LENGTH_SHORT).show();

            StorageReference file_path = storageReference.child("post_images").child(randomName + ".jpg");
            file_path.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.getResult() != null) {
                        final String downloadUri = task.getResult().getDownloadUrl().toString();
                        if (task.isSuccessful()) {
                            File newImageFile = new File(postImageUri.getPath());
                            try {
                                compressedImageFile = new Compressor(PostActivity.this)
                                        .setMaxHeight(100)
                                        .setMaxWidth(100)
                                        .setQuality(2)
                                        .compressToBitmap(newImageFile);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] thumb_data = baos.toByteArray();
                            UploadTask uploadTask = storageReference.child("post_images/thumbs")
                                    .child(randomName + ".jpg").putBytes(thumb_data);

                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    String downloathumbUri = taskSnapshot.getDownloadUrl().toString();
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("image_url", downloadUri);
                                    map.put("image_thumb", downloathumbUri);
                                    map.put("desc", dec);
                                    map.put("user_id", current_user_id);
                                    map.put("timestamp", FieldValue.serverTimestamp());

                                    firebaseFirestore.collection("Posts").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(PostActivity.this, "Post Successfully Added", Toast.LENGTH_SHORT).show();
                                                Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
                                                startActivity(mainIntent);
                                                finish();
                                            } else {
                                                String error = task.getException().getMessage();
                                                Toast.makeText(PostActivity.this, error, Toast.LENGTH_SHORT).show();
                                            }
                                            mProgressBar.setVisibility(View.INVISIBLE);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                                            StorageReference ImageRef = storageRef.child("post_images").child(randomName + ".jpg");
                                            ImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("delImgPostnotAdded", randomName + ".jpg removed");
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("delImgPostnotAdded", randomName + ".jpg not Found");

                                                }
                                            });
                                            StorageReference ThumbImageRef = storageRef.child("post_images/thumbs")
                                                    .child(randomName + ".jpg");
                                            ThumbImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    Log.i("delThumbImgPostnotAdded", randomName + ".jpg removed");

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("delThumbImgPostnotAdded", randomName + ".jpg not Found");

                                                }
                                            });
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                    StorageReference ImageRef = storageRef.child("post_images").child(randomName + ".jpg");
                                    ImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i("delImgPostnotAdded", randomName + ".jpg removed");

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("delImgPostnotAdded", randomName + ".jpg not Found");

                                        }
                                    });
                                }
                            });
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(PostActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(PostActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        } else {
            Toast.makeText(this, "Please select image & Add some Description", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        newPostImage = findViewById(R.id.new_post_image);
        newPostDesc = findViewById(R.id.new_post_desc);
        postBtn = findViewById(R.id.post_btn);
        mProgressBar = findViewById(R.id.post_progress);
        uploadIcon = findViewById(R.id.upload_icon);
        character_limit = findViewById(R.id.character_limit);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                uploadIcon.setVisibility(View.GONE);
                newPostImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }


    }
}
