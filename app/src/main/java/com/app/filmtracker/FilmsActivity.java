package com.app.filmtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.filmtracker.poo.OnLoadCustomListener;
import com.app.filmtracker.poo.SingletonMap;
import com.app.filmtracker.reclycler.CustomReclyclerViewAdapter;
import com.app.filmtracker.vo.Genre;
import com.app.filmtracker.vo.Movie;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FilmsActivity extends AppCompatActivity {

    //View Components
    RecyclerView filmsRecyclerView;
    ProgressBar filmsProgressBar;

    //Android Volley
    RequestQueue requestQueue;
    Gson gson;

    //Data
    List<Genre> genresList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_films);

        //Android Volley
        requestQueue = (RequestQueue) SingletonMap.getInstance().get(SingletonMap.REQUEST_QUEUE);
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            SingletonMap.getInstance().put(SingletonMap.REQUEST_QUEUE, requestQueue);
        }

        //View Components
        filmsRecyclerView = findViewById(R.id.filmsRecyclerView);
        filmsProgressBar = findViewById(R.id.filmsProgressBar);
        filmsProgressBar.setIndeterminate(true);
        filmsProgressBar.onVisibilityAggregated(true);

        //Recycler View
        gson = new Gson();
        fetchGenreAndThenStartRecyclerView();


    }

    private void fetchGenreAndThenStartRecyclerView(){
        String url = "https://api.themoviedb.org/3/genre/movie/list?api_key=a9e15ccf0b964bbf599fef3ba94ef87b&language=es";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray genresJsonList = response.getJSONArray("genres");
                            Type listType = new TypeToken<ArrayList<Genre>>(){}.getType();
                            genresList = gson.fromJson(genresJsonList.toString(), listType);

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
        filmsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); //new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        CustomReclyclerViewAdapter adapter = new CustomReclyclerViewAdapter(this, genresList);

        adapter.setOnLoadCustomListener(new OnLoadCustomListener() {
            @Override
            public void load() {
                filmsProgressBar.setIndeterminate(true);
                String url = "https://api.themoviedb.org/3/discover/movie?api_key=a9e15ccf0b964bbf599fef3ba94ef87b&language=es-ES&sort_by=popularity.desc&include_adult=false&include_video=false&page="+ adapter.getLastPage() +"&with_watch_monetization_types=flatrate";
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

                                    System.out.println(response.toString());
                                    filmsProgressBar.setIndeterminate(false);
                                    filmsProgressBar.onVisibilityAggregated(false);
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

        filmsRecyclerView.setAdapter(adapter);
    }



}