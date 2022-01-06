package com.app.filmtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.app.filmtracker.poo.SingletonMap;
import com.app.filmtracker.vo.Genre;
import com.app.filmtracker.vo.Movie;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilmsDetailsActivity extends AppCompatActivity {

    // View components
    private ImageView filmImage;
    private RatingBar ratingBar;
    private TextView title, description, subtitleGenres;
    private ImageButton btnLike;

    // Get movie data from SingletonMap
    private final Movie movie =
            (Movie) SingletonMap.getInstance().get(SingletonMap.CURRENT_FILM_DETAILS);

    private final List<Genre> genres =
            (List<Genre>) SingletonMap.getInstance().get(SingletonMap.GENRES);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_films_details);

        setViewComponents();
        setFilmInfo();
    }

    private void setFilmInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // --------------- Title, Description and Genres ---------------

        title.setText(movie.getTitle());
        description.setText(movie.getOverview());
        StringBuilder genresResult = new StringBuilder();
        for(int i : movie.getGenre_ids()){
            Genre gi = new Genre(i);
            for(Genre g : genres){
                if(g.equals(gi)){
                    genresResult.append(g.getName()).append(", ");
                }
            }
        }
        subtitleGenres.setText(genresResult.toString().length() > 0 ? genresResult.toString().substring(0, genresResult.toString().length() - 2) : genresResult.toString());

        // --------------- END of Title, Description and Genres ---------------

        // --------------- Film Rating ---------------

        db.collection("Rating")
                .whereEqualTo("film_id", String.valueOf(movie.getId()))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        float sum = 0;
                        float ratings = 0;

                        if (task.isSuccessful()) {

                            // Print query
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("FILM RATING", document.getId() + " => " + document.getData());
                                Double puntuation = document.getData().get("puntuation") == null ? null : Double.parseDouble(document.getData().get("puntuation").toString());
                                if (puntuation != null) {
                                    sum += puntuation;
                                    ratings++;
                                }
                            }
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }

                        if (ratings == 0)
                            ratingBar.setRating(0);
                        else {
                            System.out.println("SUM: " + sum + ", RATINGS: " + ratings);
                            ratingBar.setRating(sum/ratings/2);
                        }
                    }
                });

        // --------------- END of Film Rating ---------------

        // --------------- Image ---------------

        filmImage.setImageBitmap(movie.getImage());

        // --------------- END of Image ---------------

        // --------------- Like btn ---------------

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Color likeBtnColor = Color.valueOf(Color.rgb(233, 30, 99));
        Color likeBtnColorOff = Color.valueOf(Color.rgb(117, 117, 117));
        db.collection("Favorite")
                .whereEqualTo("user_id", user.getEmail())
                .whereEqualTo("film_id", String.valueOf(this.movie.getId()))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            if (task.getResult().size() > 0)
                                btnLike.setColorFilter(likeBtnColor.toArgb());
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Favorite")
                        .whereEqualTo("user_id", user.getEmail())
                        .whereEqualTo("film_id", String.valueOf(movie.getId()))
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().size() > 0) {
                                        // Remove Favorite
                                        QueryDocumentSnapshot doc = task.getResult().iterator().next();
                                        db.collection("Favorite")
                                                .document(doc.getId())
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        btnLike.setColorFilter(likeBtnColorOff.toArgb());
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("ERROR", "Error deleting document Favorite", e);
                                                    }
                                                });
                                    } else {
                                        // Favorite
                                        Map<String, Object> favorite = new HashMap<>();
                                        favorite.put("user_id", user.getEmail());
                                        favorite.put("film_id", String.valueOf(movie.getId()));

                                        // Add favorite to database
                                        db.collection("Favorite").document()
                                                .set(favorite)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        btnLike.setColorFilter(likeBtnColor.toArgb());
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("ERROR", "Error writing document", e);
                                                    }
                                                });
                                    }

                                } else {
                                    Log.w("TAG", "Error getting documents.", task.getException());
                                }
                            }
                        });
            }
        });

        // --------------- END of Like btn ---------------


    }

    private void setViewComponents() {
        ratingBar = findViewById(R.id.detailsRatingBar);
        filmImage = findViewById(R.id.detailsFilmImage);
        description = findViewById(R.id.detailsDescription);
        title = findViewById(R.id.detailsTitle);
        btnLike = findViewById(R.id.detailsButtonLike);
        subtitleGenres = findViewById(R.id.detailsGenres);
    }
}