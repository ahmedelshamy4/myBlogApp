package com.example.ahmed.myblogapp.comment;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ahmed.myblogapp.R;
import com.example.ahmed.myblogapp.model.Comments;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {
    Toolbar toolbar_comment;
    EditText comment_field;
    ImageView comment_post_btn;
    ImageView comment_current_user;
    List<Comments> commentsList;
    RecyclerView comment_list_RV;
    CommentsRecyclerAdapter commentsRecyclerAdapter;
    private String blog_post_id;
    private String current_user_id;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        toolbar_comment = findViewById(R.id.comment_toolbar);
        setSupportActionBar(toolbar_comment);
        getSupportActionBar().setTitle("Comments");
        initViews();

        firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (documentSnapshots != null) {
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String commentId = doc.getDocument().getId();
                            Comments comments = doc.getDocument().toObject(Comments.class);
                            commentsList.add(comments);
                            commentsRecyclerAdapter.notifyDataSetChanged();


                        }

                    }
                }
            }
        });

        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).get()
                .addOnCompleteListener(CommentsActivity.this, new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            System.out.println(task.getResult().getString("image"));
                            Glide.with(CommentsActivity.this)
                                    .load(task.getResult().getString("image"))
                                    .into(comment_current_user);

                        } else {

                            Toast.makeText(CommentsActivity.this, "ERROR" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment_text = comment_field.getText().toString();
                if (!TextUtils.isEmpty(comment_text)) {
                    Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("message", comment_text);
                    commentMap.put("user_id", current_user_id);
                    commentMap.put("timestamp", FieldValue.serverTimestamp());
                    firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").add(commentMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                comment_field.setText("");
                                Toast.makeText(CommentsActivity.this, "Comment Successfully posted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CommentsActivity.this, "Error Posting Comment :", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CommentsActivity.this, "Error Posting Comment :", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(CommentsActivity.this, "Please Enter a comment", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void initViews() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();
        blog_post_id = getIntent().getStringExtra("blog_post_id");

        comment_field = findViewById(R.id.comment_field);
        comment_post_btn = findViewById(R.id.comment_post_btn);
        comment_list_RV = findViewById(R.id.comment_list);
        comment_current_user = findViewById(R.id.comment_current_user);
        commentsList = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList, CommentsActivity.this);
        comment_list_RV.setHasFixedSize(true);
        comment_list_RV.setLayoutManager(new LinearLayoutManager(this));
        comment_list_RV.setAdapter(commentsRecyclerAdapter);
    }
}
