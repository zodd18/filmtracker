package com.app.filmtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.filmtracker.poo.SingletonMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    //Firebase
    FirebaseAuth mAuth;
    FirebaseUser user;

    //View Components
    Button button;
    Button btnProfileFilms;
    TextView tvprofileName;
    ImageView imageProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        //FirebaseAuth
        mAuth = (FirebaseAuth) SingletonMap.getInstance().get(SingletonMap.FIREBASE_AUTH_INSTANCE);
        user = (FirebaseUser) SingletonMap.getInstance().get(SingletonMap.FIREBASE_USER_INSTANCE);

        //View Components
        tvprofileName = findViewById(R.id.profileName);
        imageProfile = findViewById(R.id.profileImageViewUser);
        btnProfileFilms = findViewById(R.id.profileFilms);

        //Complete User data in profile layout
        tvprofileName.setText(user.getDisplayName());

        new DownloadImageTask().execute(user.getPhotoUrl().toString());

        //Events - Log Out
        button = findViewById(R.id.profileButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });

        //Events - Go to Films
        btnProfileFilms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, FilmsActivity.class);
                startActivity(intent);
            }
        });

    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap iconProfile = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                iconProfile = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return iconProfile;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageProfile.setImageBitmap(bitmap);
        }
    }


    public void logOut(){
        mAuth.signOut();
        finish();
    }
}