package com.app.filmtracker.recycler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.filmtracker.FilmsDetailsActivity;
import com.app.filmtracker.R;
import com.app.filmtracker.poo.OnLoadCustomListener;
import com.app.filmtracker.poo.SingletonMap;
import com.app.filmtracker.vo.Genre;
import com.app.filmtracker.vo.Movie;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.ViewHolder> implements View.OnClickListener {
    //TODO: DOCUMENTACION https://developer.android.com/guide/topics/ui/layout/recyclerview
    //TODO: Ejemplo en stackOverflow: https://stackoverflow.com/questions/40587168/simple-android-grid-example-using-recyclerview-with-gridlayoutmanager-like-the

    private ProgressBar progressBar;
    private TextView progressBarText;

    private List<Movie> data;
    private List<Genre> genres;
    private LayoutInflater mInflater;
    private View.OnClickListener onClickListener;
    private Context ctx;
    private FirebaseFirestore db;


    //Dynamic load using API Rest from TBDb
    private int lastPage;
    private boolean isFetching;
    private static final int VISIBLE_THRESHOLD = 30;
    private OnLoadCustomListener onLoadCustomListener;

    public CustomRecyclerViewAdapter(Context context, List<Genre> genres, ProgressBar progressBar, TextView progressBarText) {
        this.ctx = context;
        this.mInflater = LayoutInflater.from(context);
        this.lastPage = 1;
        this.isFetching = false;
        this.data = new ArrayList<>();
        this.genres = genres;
        this.progressBar = progressBar;
        this.progressBarText = progressBarText;
        db = FirebaseFirestore.getInstance();
    }

    // Inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.film_card_view, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        try {

            if ((position+VISIBLE_THRESHOLD)>=data.size() && !isFetching) {
                isFetching = true;
                onLoadCustomListener.load();
           }
                Movie currentMovie = data.get(position);


                // --------------- About ---------------

                View.OnClickListener detailsListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SingletonMap.getInstance().put(SingletonMap.CURRENT_FILM_DETAILS, currentMovie);
                        Intent intent = new Intent(ctx, FilmsDetailsActivity.class);
                        ctx.startActivity(intent);
                    }
                };
                holder.btnAbout.setOnClickListener(detailsListener);

                // --------------- END of About ---------------


                // --------------- Image ---------------

                holder.image.setImageBitmap(currentMovie.getImage());
                // --------------- END of Image ---------------


                // --------------- Title and Genres ---------------

                holder.title.setText(currentMovie.getTitle());
                String genresResult = "";
                for(int i : currentMovie.getGenre_ids()){
                    Genre gi = new Genre(i);
                    for(Genre g : genres){
                        if(g.equals(gi)){
                            genresResult+=g.getName()+"\n";
                        }
                    }
                }
                holder.subtitle.setText(genresResult);



                // --------------- END of Title and Genres ---------------


                // --------------- Film Rating ---------------

                db.collection("Rating")
                        .whereEqualTo("film_id", String.valueOf(currentMovie.getId()))
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
                                    holder.ratingBar.setRating(0);
                                else {
//                                    System.out.println("SUM: " + sum + ", RATINGS: " + ratings);
                                    holder.ratingBar.setRating(sum/ratings/2);
                                }
                            }
                        });



                // --------------- END of Film Rating ---------------


                // --------------- Like btn ---------------


                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Color likeBtnColor = Color.valueOf(Color.rgb(233, 30, 99));
                Color likeBtnColorOff = Color.valueOf(Color.rgb(117, 117, 117));
                // Colorize fav buttons for every user's favorite film
                db.collection("Favorite")
                        .whereEqualTo("user_id", user.getEmail())
                        .whereEqualTo("film_id", String.valueOf(currentMovie.getId()))
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().isEmpty())
                                        holder.btnLike.setColorFilter(likeBtnColorOff.toArgb());
                                    else {
                                        holder.btnLike.setColorFilter(likeBtnColor.toArgb());
                                    }
                                } else {
                                    Log.w("TAG", "Error getting documents.", task.getException());
                                }
                            }
                        });

                holder.btnLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.collection("Favorite")
                                .whereEqualTo("user_id", user.getEmail())
                                .whereEqualTo("film_id", String.valueOf(currentMovie.getId()))
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
                                                                holder.btnLike.setColorFilter(likeBtnColorOff.toArgb());
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
                                                favorite.put("film_id", String.valueOf(currentMovie.getId()));

                                                // Add favorite to database
                                                db.collection("Favorite").document()
                                                        .set(favorite)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                holder.btnLike.setColorFilter(likeBtnColor.toArgb());
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // RecyclerView with data
    public static class ViewHolder extends RecyclerView.ViewHolder  {
        private ImageView image;
        private TextView title;
        private TextView subtitle;
        private RatingBar ratingBar;
        private TextView description;
        private Button btnAbout;
        private ImageButton btnLike;
        private ImageButton btnShare;
        private CardView card;

        ViewHolder(View itemView) {
            super(itemView);
            btnLike = itemView.findViewById(R.id.filmCardButtonLike);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            image = itemView.findViewById(R.id.filmCardImage);
            title = itemView.findViewById(R.id.filmCardTitle);
            subtitle = itemView.findViewById(R.id.filmCardSubtitle);
            btnAbout = itemView.findViewById(R.id.filmCardButtonAbout);
            card = itemView.findViewById(R.id.card);

        }
    }

    public void setOnLoadCustomListener(OnLoadCustomListener onLoadCustomListener) {
        this.onLoadCustomListener = onLoadCustomListener;
        this.onLoadCustomListener.load();
    }


    public void addNewDataAndNotify(Collection<? extends Movie> collection){
        new DownloadImageTask().execute(collection);
    }


    public void setFetched() {
        this.isFetching = false;
    }

    public void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }

    public int getLastPage() {
        return lastPage;
    }



    private class DownloadImageTask extends AsyncTask<Object, Void, Collection<Movie>> {

        @Override
        protected Collection<Movie> doInBackground(Object... objects) {
            Collection<Movie> movieCollection = (Collection<Movie>) objects[0];

            for(Movie m : movieCollection){
                String urldisplay = "https://image.tmdb.org/t/p/w500/"+m.getPoster_path();
                Bitmap movieImage = null;
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    movieImage = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                m.setImage(movieImage);
            }
            return movieCollection;
        }

        @Override
        protected void onPostExecute(Collection<Movie> movies) {
            data.addAll(movies);
            progressBar.setVisibility(View.INVISIBLE);
            progressBarText.setVisibility(View.INVISIBLE);
            setFetched();
            notifyDataSetChanged();
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }



    @Override
    public void onClick(View view) {
        if(this.onClickListener!=null)
            onClickListener.onClick(view);
    }

    public Movie getMovieByPosition(int position){
        return this.data.get(position);
    }
}
