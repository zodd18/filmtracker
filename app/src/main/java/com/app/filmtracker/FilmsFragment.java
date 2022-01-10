package com.app.filmtracker;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FilmsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilmsFragment extends Fragment {
    public FilmsFragment() {
        // Required empty public constructor
    }

    public static FilmsFragment newInstance(String param1, String param2) {
        FilmsFragment fragment = new FilmsFragment();
        return fragment;
    }


    //View Components
    RecyclerView filmsRecyclerView;
    CustomRecyclerViewAdapter adapter;
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
        filmsRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2)); //new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        adapter = new CustomRecyclerViewAdapter(getActivity(), genresList);

        //Fetch data
        adapter.setOnLoadCustomListener(new OnLoadCustomListener() {
            @Override
            public void load() {

                String url = "https://api.themoviedb.org/3/discover/movie?api_key=a9e15ccf0b964bbf599fef3ba94ef87b&language="+ getString(R.string.language) +"&sort_by=popularity.desc&include_adult=false&include_video=false&page="+ adapter.getLastPage() +"&with_watch_monetization_types=flatrate";
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    adapter.setLastPage(response.getInt("page") + 1);
                                    JSONArray movieJsonList = response.getJSONArray("results");
                                    Type listType = new TypeToken<ArrayList<Movie>>(){}.getType();
                                    List<Movie> movList = gson.fromJson(movieJsonList.toString(), listType);
                                    adapter.addNewDataAndNotify(movList);

                                    System.out.println(response);
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
        });

        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Movie currentMovie = adapter.getMovieByPosition(filmsRecyclerView.getChildAdapterPosition(view));
                SingletonMap.getInstance().put(SingletonMap.CURRENT_FILM_DETAILS, currentMovie);
                Intent intent = new Intent(getContext(), FilmsDetailsActivity.class);
                startActivity(intent);
            }
        });

        filmsRecyclerView.setAdapter(adapter);
    }

    // When return to this fragment, updates the edited film data (rating, favorite)
    @Override
    public void onResume() {
        super.onResume();

//        if (SingletonMap.getInstance().get(SingletonMap.CURRENT_FILMS_HOLDER) != null) {
//            CustomRecyclerViewAdapter.ViewHolder holder = (CustomRecyclerViewAdapter.ViewHolder) SingletonMap.getInstance().get(SingletonMap.CURRENT_FILMS_HOLDER);
//            int position = (int) SingletonMap.getInstance().get(SingletonMap.CURRENT_FILMS_POSITION);
//            // Updates this film on the previous view
//            adapter.onBindViewHolder(holder, position);
//        }
    }
}