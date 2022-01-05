package com.app.filmtracker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.filmtracker.poo.SingletonMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.InputStream;

public class ProfileFragment extends Fragment {

    //Firebase
    FirebaseAuth mAuth;
    FirebaseUser user;

    //View Components
    Button button;
    Button btnProfileFilms;
    TextView tvprofileName, favoriteFilms, watchedFilms, comments;
    ImageView imageProfile;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //FirebaseAuth
        mAuth = (FirebaseAuth) SingletonMap.getInstance().get(SingletonMap.FIREBASE_AUTH_INSTANCE);
        user = (FirebaseUser) SingletonMap.getInstance().get(SingletonMap.FIREBASE_USER_INSTANCE);

        //View Components
        tvprofileName = view.findViewById(R.id.profileName);
        imageProfile = view.findViewById(R.id.profileImageViewUser);
        btnProfileFilms = view.findViewById(R.id.profileFilms);
        favoriteFilms = view.findViewById(R.id.textFavoriteFilms);
        watchedFilms = view.findViewById(R.id.textWatchedFilms);
        comments = view.findViewById(R.id.textComments);

        //Complete User data in profile
        tvprofileName.setText(user.getDisplayName());

        // Download image
        try {
            new DownloadImageTask().execute(user.getPhotoUrl().toString());
        } catch (Exception e) {
            // Null image
        }

        //Events - Log Out
        button = view.findViewById(R.id.profileButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });

        // Info
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Watched/Rated films
        db.collection("Rating")
                .whereEqualTo("user_id", user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Print query
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Rating", document.getId() + " => " + document.getData());
                            }
                            watchedFilms.setText(String.valueOf(task.getResult().size()));
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });

        db.collection("Favorite")
                .whereEqualTo("user_id", user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Print query
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Favorite", document.getId() + " => " + document.getData());
                            }

                            favoriteFilms.setText(String.valueOf(task.getResult().size()));
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });

        db.collection("Comment")
                .whereEqualTo("user_id", user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Print query
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Comment", document.getId() + " => " + document.getData());
                            }

                            comments.setText(String.valueOf(task.getResult().size()));
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });

        return view;
    }

    protected class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

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
        getActivity().finish();
    }
}