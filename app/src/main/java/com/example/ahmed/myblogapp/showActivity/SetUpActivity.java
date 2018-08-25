package com.example.ahmed.myblogapp.showActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ahmed.myblogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
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

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SetUpActivity extends AppCompatActivity {
    Toolbar toolbar_setUp;
    EditText setupName;
    Button setupButton;
    TextView textViewHint;
    ProgressBar mProgress;
    ImageButton setUpImage;
    Uri mainImageUri = null;

    private String user_id;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    Boolean isChanged = false;
    Bitmap compressedProfileImageFile;

    boolean username_exists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);


        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();

        toolbar_setUp = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar_setUp);

        setUpImage = findViewById(R.id.setup_image);
        setupButton = findViewById(R.id.setup_btn);
        setupName = findViewById(R.id.setup_name);
        textViewHint = findViewById(R.id.hint_tv);
        mProgress = findViewById(R.id.progress_bar);

        mProgress.setVisibility(View.VISIBLE);
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        mainImageUri = Uri.parse(image);
                        setupName.setText(name);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);

                        Glide.with(SetUpActivity.this)
                                .setDefaultRequestOptions(placeholderRequest)
                                .load(image)
                                .into(setUpImage);
                    } else {
                        Toast.makeText(SetUpActivity.this, "Enter your Account Details", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SetUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                mProgress.setVisibility(View.INVISIBLE);
            }
        });

        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = setupName.getText().toString();
                if (!TextUtils.isEmpty(name) && mainImageUri != null) {
                    mProgress.setVisibility(View.VISIBLE);

                    if (isChanged) {
                        user_id = firebaseAuth.getCurrentUser().getUid();
                        File newImageFile = new File(mainImageUri.getPath());
                        try {
                            compressedProfileImageFile = new Compressor(SetUpActivity.this)
                                    .setMaxHeight(100)
                                    .setMaxWidth(100)
                                    .setQuality(2)
                                    .compressToBitmap(newImageFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressedProfileImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] profile_image_data = baos.toByteArray();
                        Toast.makeText(SetUpActivity.this, "Uploading image", Toast.LENGTH_SHORT).show();

                        UploadTask uploadTask = storageReference
                                .child("profile_images").child(user_id + ".jpg").putBytes(profile_image_data);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                storeFireStore(taskSnapshot, name);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SetUpActivity.this, "Image Upload error", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        storeFireStore(null, name);
                    }

                } else {
                    Toast.makeText(SetUpActivity.this, "Select profile image , enter name ", Toast.LENGTH_SHORT).show();
                }

            }
        });

        setUpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textViewHint.setVisibility(View.GONE);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(SetUpActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                            + ContextCompat.checkSelfPermission(SetUpActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(SetUpActivity.this, "Grant Storage Read & Write Permission", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetUpActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                    } else {

                        imagePicker();

                    }

                } else {
                    imagePicker();
                }
            }
        });
    }

    private void storeFireStore(final UploadTask.TaskSnapshot taskSnapshot, final String name) {
        final Uri download_uri;
        if (taskSnapshot != null) {
            download_uri = taskSnapshot.getDownloadUrl();
        } else {
            download_uri = mainImageUri;
        }
        final Map<String, String> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("image", download_uri.toString());

        CollectionReference allUsersRef = firebaseFirestore.collection("Users");
        allUsersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        String userName;
                        userName = documentSnapshot.getString("name");
                        if (!documentSnapshot.getId().equals(firebaseAuth.getCurrentUser().getUid())) {
                            if (!TextUtils.isEmpty(userName)) {
                                if (userName.equals(name)) {
                                    Toast.makeText(SetUpActivity.this, "Username Already Exists ", Toast.LENGTH_SHORT).show();
                                    mProgress.setVisibility(View.INVISIBLE);
                                    username_exists = true;
                                    return;

                                } else {
                                    username_exists = false;
                                }
                            }
                        }
                    }
                    if (!username_exists) {
                        firebaseFirestore.collection("Users").document(user_id).set(userMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SetUpActivity.this, "The user settings are updated", Toast.LENGTH_SHORT).show();
                                            Intent mainIntent = new Intent(SetUpActivity.this, MainActivity.class);
                                            startActivity(mainIntent);
                                            finish();
                                        } else {
                                            String error = task.getException().getMessage();
                                            Toast.makeText(SetUpActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                        mProgress.setVisibility(View.GONE);
                                    }
                                });
                    } else {
                        mProgress.setVisibility(View.GONE);
                        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (!name.equals(task.getResult().getString("name")) && taskSnapshot != null) {

                                        final Map<String, Object> userMapWithoutUsername = new HashMap<>();
                                        userMapWithoutUsername.put("name", name);
                                        userMapWithoutUsername.put("image", download_uri.toString());

                                        firebaseFirestore.collection("Users").document(user_id)
                                                .set(userMapWithoutUsername).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    mProgress.setVisibility(View.INVISIBLE);

                                                    Toast.makeText(SetUpActivity.this, "The user settings are updated ", Toast.LENGTH_SHORT).show();
                                                    Intent mainIntent = new Intent(SetUpActivity.this, MainActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();

                                                } else {

                                                    mProgress.setVisibility(View.INVISIBLE);
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(SetUpActivity.this, error, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(SetUpActivity.this, "Change Fields and save settings", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    mProgress.setVisibility(View.INVISIBLE);

                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetUpActivity.this, error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(SetUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mainImageUri = result.getUri();
                setUpImage.setImageURI(mainImageUri);
                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void imagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(SetUpActivity.this);
    }
}