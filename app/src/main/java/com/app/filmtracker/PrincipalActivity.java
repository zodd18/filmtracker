package com.app.filmtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class PrincipalActivity extends AppCompatActivity {

    //View Components
    NavigationBarView navigationBarView;

    //Fragments
    ProfileFragment profileFragment;
    FilmsFragment filmsFragment;
    ChatFragment chatFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        //Fragments
        profileFragment = new ProfileFragment();
        filmsFragment = new FilmsFragment();
        chatFragment = new ChatFragment();

        //Bottom Menu
        navigationBarView = findViewById(R.id.MainBottomNavigation);

        loadFragment(filmsFragment);
        navigationBarView.setSelectedItemId(R.id.bottomMenuFilms);
        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.bottomMenuProfile:
                        loadFragment(profileFragment);
                        return true;
                    case R.id.bottomMenuFilms:
                        loadFragment(filmsFragment);
                        return true;
                    case R.id.bottomMenuChat:
                        loadFragment(chatFragment);
                        return true;

                }
                return false;
            }
        });


    }

    private void loadFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrameContainer, fragment);
        fragmentTransaction.commit();
    }

   /* private void fetchGenreAndThenStartRecyclerView(){
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
    }*/



}