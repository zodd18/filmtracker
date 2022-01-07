package com.app.filmtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.app.filmtracker.poo.SingletonMap;
import com.app.filmtracker.vo.Genre;
import com.app.filmtracker.vo.Movie;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilmsDetailsActivity extends AppCompatActivity {

    // View components
    private ImageView filmImage;
    private RatingBar ratingBar;
    private TextView title, description, subtitleGenres;
    private ImageButton btnLike;
    private Spinner dropdown;

    String[] spinnerSelections;
    private int initialSpinnerDisplayHasHappened = 0;

    // Get movie data from SingletonMap
    private final Movie movie =
            (Movie) SingletonMap.getInstance().get(SingletonMap.CURRENT_FILM_DETAILS);

    private final List<Genre> genres =
            (List<Genre>) SingletonMap.getInstance().get(SingletonMap.GENRES);


    // Attributes
    private int userRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_films_details);

        setViewComponents();
        setFilmInfo();
    }

    private void setFilmInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

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

        setStars(db, user);

        // ---- Spinner ----
        spinnerSelections = new String[] {
                getString(R.string.spinner_details_unwatched),
                getString(R.string.spinner_details_planned_to_watch),
                getString(R.string.spinner_details_null_rating),
                "(10) " + getString(R.string.spinner_details_10),
                "(9) " + getString(R.string.spinner_details_9),
                "(8) " + getString(R.string.spinner_details_8),
                "(7) " + getString(R.string.spinner_details_7),
                "(6) " + getString(R.string.spinner_details_6),
                "(5) " + getString(R.string.spinner_details_5),
                "(4) " + getString(R.string.spinner_details_4),
                "(3) " + getString(R.string.spinner_details_3),
                "(2) " + getString(R.string.spinner_details_2),
                "(1) " + getString(R.string.spinner_details_1)
        };

        userRating = -(spinnerSelections.length - 11);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerSelections);
        dropdown.setAdapter(adapter);
        dropdown.setSelection(0);

        db.collection("Rating")
                .whereEqualTo("film_id", String.valueOf(movie.getId()))
                .whereEqualTo("user_id", user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {
                                QueryDocumentSnapshot document = task.getResult().iterator().next();
                                userRating = document.getData().get("puntuation") == null ? null : Integer.parseInt(document.getData().get("puntuation").toString());
                                setSpinnerSelection(userRating);
                            } else {
                                initialSpinnerDisplayHasHappened++;
                            }
                        } else {
                            Log.w("ERROR", "Error getting documents.", task.getException());
                            initialSpinnerDisplayHasHappened++;
                        }
                    }
                });

        // SET user's rating
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (initialSpinnerDisplayHasHappened >= 2) {
                    if (position == 0) {
                        // UNWATCHED => DELETE RATING FROM DB
                        db.collection("Rating")
                                .whereEqualTo("film_id", String.valueOf(movie.getId()))
                                .whereEqualTo("user_id", user.getEmail())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (DocumentSnapshot document : task.getResult()) {
                                                db.collection("Rating").document(document.getId()).delete();
                                            }

                                            // Update rating stars
                                            setStars(db, user);
                                        } else {
                                            Log.w("ERROR", "Error getting documents.", task.getException());
                                        }
                                    }
                                });
                    } else {
                        // ADD RATING TO DB
                        userRating = getRatingFromSpinnerSelection(position);
                        Map<String, Object> rating = new HashMap<>();
                        rating.put("user_id", user.getEmail());
                        rating.put("film_id", String.valueOf(movie.getId()));
                        rating.put("date", new Timestamp(new Date()));
                        rating.put("puntuation", getRatingFromSpinnerSelection(position));

                        // Add rating to database, or update existing one
                        db.collection("Rating")
                                .whereEqualTo("film_id", String.valueOf(movie.getId()))
                                .whereEqualTo("user_id", user.getEmail())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().size() == 0) {
                                                // ADD NEW
                                                db.collection("Rating").document()
                                                        .set(rating)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                setStars(db, user);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w("ERROR", "Error writing document", e);
                                                            }
                                                        });
                                            } else {
                                                // UPDATE EXISTING RATING
                                                DocumentSnapshot document = task.getResult().iterator().next();
                                                db.collection("Rating").document(document.getId()).update(
                                                        "date", rating.get("date"), "rating", rating.get("puntuation"));
                                            }
                                        } else {
                                            Log.w("ERROR", "Error getting documents.", task.getException());
                                        }
                                    }
                                });
                    }
                } else {
                    initialSpinnerDisplayHasHappened++;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // ---- END of Spinner ----

        // --------------- END of Film Rating ---------------


        // --------------- Image ---------------

        filmImage.setImageBitmap(movie.getImage());

        // --------------- END of Image ---------------


        // --------------- Like btn ---------------

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

    private void setStars(FirebaseFirestore db, FirebaseUser user) {
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
                                if (puntuation != null && puntuation > 0) {
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
    }

    private void setSpinnerSelection(int userRating) {
        if (userRating >= 1) {
            // Puntuacion real
            dropdown.setSelection(spinnerSelections.length - userRating, true);
        } else {
            // Otro estado <= 0
            // 0 => null_rating
            dropdown.setSelection((spinnerSelections.length - 11) + userRating, true);
        }
    }

    private int getRatingFromSpinnerSelection(int spinnerPosition) {
        int rating = 0;
        if (spinnerPosition > spinnerSelections.length - 11) {
            // Puntuacion real
            rating = (spinnerSelections.length) - spinnerPosition;
        } else {
            // Otro estado <= 0
            rating =  (-(spinnerSelections.length - 11)) + spinnerPosition;
        }

        return rating;
    }


//    String[] spinnerSelections = new String[] {
//            getString(R.string.spinner_details_unwatched),
//            getString(R.string.spinner_details_planned_to_watch),
//            getString(R.string.spinner_details_watching),
//            getString(R.string.spinner_details_null_rating),
//            "(10) " + getString(R.string.spinner_details_10),
//            "(9) " + getString(R.string.spinner_details_9),
//            "(8) " + getString(R.string.spinner_details_8),
//            "(7) " + getString(R.string.spinner_details_7),
//            "(6) " + getString(R.string.spinner_details_6),
//            "(5) " + getString(R.string.spinner_details_5),
//            "(4) " + getString(R.string.spinner_details_4),
//            "(3) " + getString(R.string.spinner_details_3),
//            "(2) " + getString(R.string.spinner_details_2),
//            "(1) " + getString(R.string.spinner_details_1)
//    };


    private void setViewComponents() {
        ratingBar = findViewById(R.id.detailsRatingBar);
        filmImage = findViewById(R.id.detailsFilmImage);
        description = findViewById(R.id.detailsDescription);
        title = findViewById(R.id.detailsTitle);
        btnLike = findViewById(R.id.detailsButtonLike);
        subtitleGenres = findViewById(R.id.detailsGenres);
        dropdown = findViewById(R.id.detailsRatingSpinner);
    }


}