package com.app.filmtracker;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.filmtracker.poo.OnLoadCustomListener;
import com.app.filmtracker.poo.SingletonMap;
import com.app.filmtracker.recycler.CustomRecyclerViewAdapter;
import com.app.filmtracker.vo.Genre;
import com.app.filmtracker.vo.Movie;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class LikesFragment extends Fragment {

    public LikesFragment() {
        // Required empty public constructor
    }

    public static LikesFragment newInstance(String param1, String param2) {
        LikesFragment fragment = new LikesFragment();
        return fragment;
    }


    //View Components
    RecyclerView filmsRecyclerView;
    CustomRecyclerViewAdapter recyclerViewAdapter;
    //Data
    List<Genre> genresList;

    //Android Volley
    RequestQueue requestQueue;
    Gson gson;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_films, container, false);

        //Android Volley
        requestQueue = (RequestQueue) SingletonMap.getInstance().get(SingletonMap.REQUEST_QUEUE);
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getActivity());
            SingletonMap.getInstance().put(SingletonMap.REQUEST_QUEUE, requestQueue);
        }

        //Recycler View
        filmsRecyclerView = view.findViewById(R.id.favoriteFilmsRecyclerView);
        genresList = new ArrayList<>();
        gson = new Gson();

        if (SingletonMap.getInstance().get(SingletonMap.GENRES) == null) {
            fetchGenreAndThenStartRecyclerView();
        } else {
            genresList = (List<Genre>) SingletonMap.getInstance().get(SingletonMap.GENRES);
            createAndStartRecyclerView();
        }
        return view;
    }

    private void fetchGenreAndThenStartRecyclerView(){
        String url = "https://api.themoviedb.org/3/genre/movie/list?api_key=a9e15ccf0b964bbf599fef3ba94ef87b&language=" + getString(R.string.language);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray genresJsonList = response.getJSONArray("genres");
                            Type listType = new TypeToken<ArrayList<Genre>>(){}.getType();
                            genresList = gson.fromJson(genresJsonList.toString(), listType);
                            SingletonMap.getInstance().put(SingletonMap.GENRES, genresList);

                            System.out.println(response.toString());
                            createAndStartRecyclerView();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        System.out.println("------Error en el Volley");
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    private void createAndStartRecyclerView(){


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get user's favorite films
        List<Integer> filmsId = new LinkedList<>();
        db.collection("Favorite")
                .whereEqualTo("user_id", user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                filmsId.add(Integer.valueOf((String) document.get("film_id")));
                            }

                            // ------------------------------------

                            //Fetch data
                            filmsRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2)); //new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

                            recyclerViewAdapter = new CustomRecyclerViewAdapter(getActivity(), genresList);

                            recyclerViewAdapter.setOnLoadCustomListener(new OnLoadCustomListener() {
                                @Override
                                public void load() {
                                    if(recyclerViewAdapter.getLastPage() == 1){
                                        recyclerViewAdapter.setLastPage(0);

                                        for (Integer filmId : filmsId) {
                                            String url = "https://api.themoviedb.org/3/movie/" + filmId + "?api_key=a9e15ccf0b964bbf599fef3ba94ef87b&language=" + getString(R.string.language);
                                            System.out.println("            FETCHING... -> " + url);
                                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                                                        @Override
                                                        public void onResponse(JSONObject response) {
                                                            try {
                                                                Movie movie = gson.fromJson(response.toString(), new TypeToken<Movie>(){}.getType());

                                                                // Add genres id to each movie
                                                                JSONArray genreIdsRaw = ((JSONArray) response.get("genres"));
                                                                int[] genreIds = new int[genreIdsRaw.length()];
                                                                for (int i = 0; i < genreIdsRaw.length(); i++) {
                                                                    JSONObject g = (JSONObject) genreIdsRaw.get(i);
                                                                    genreIds[i] = (int) g.get("id");
                                                                }
                                                                movie.setGenre_ids(genreIds);

                                                                recyclerViewAdapter.addNewDataAndNotify(Arrays.asList(movie));
                                                                System.out.println(response);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }


                                                        }
                                                    }, new Response.ErrorListener() {
                                                        @Override
                                                        public void onErrorResponse(VolleyError error) {
                                                            System.out.println("------Error en el Volley");
                                                        }
                                                    });
                                            requestQueue.add(jsonObjectRequest);

                                        }
                                    }
                                    // Adapter movieslist

                                    System.out.println("ITEM COUNT: " + recyclerViewAdapter.getItemCount());
                                }
                            });

                            recyclerViewAdapter.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Movie currentMovie = recyclerViewAdapter.getMovieByPosition(filmsRecyclerView.getChildAdapterPosition(view));
                                    SingletonMap.getInstance().put(SingletonMap.CURRENT_FILM_DETAILS, currentMovie);
                                    Intent intent = new Intent(getContext(), FilmsDetailsActivity.class);
                                    startActivity(intent);
                                }
                            });

                            filmsRecyclerView.setAdapter(recyclerViewAdapter);

                            // ------------------------------------
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    // When return to this fragment, updates the edited film data (rating, favorite)
    @Override
    public void onResume() {
        super.onResume();
//        if (SingletonMap.getInstance().get(SingletonMap.CURRENT_FILMS_HOLDER) != null && recyclerViewAdapter != null) {
//            CustomRecyclerViewAdapter.ViewHolder holder = (CustomRecyclerViewAdapter.ViewHolder) SingletonMap.getInstance().get(SingletonMap.CURRENT_FILMS_HOLDER);
//            int position = (int) SingletonMap.getInstance().get(SingletonMap.CURRENT_FILMS_POSITION);
//            // Updates this film on the previous view
//            recyclerViewAdapter.onBindViewHolder(holder, position);
//        }
    }
}