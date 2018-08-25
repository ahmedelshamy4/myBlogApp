package com.example.ahmed.myblogapp.model;


import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class BlogPostID {

    @Exclude
    public
    String blogPostId;

    public<T extends  BlogPostID> T withId(@NonNull final String id) {
        this.blogPostId = id;
        return (T) this;
    }
}
