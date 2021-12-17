package com.app.filmtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Bundle;

import com.app.filmtracker.reclycler.CustomReclyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class FilmsActivity extends AppCompatActivity {

    RecyclerView filmsRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_films);


        //View Components
        filmsRecyclerView = findViewById(R.id.filmsRecyclerView);

        filmsRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        List<Object> lista = new ArrayList<>();
        lista.add("hola");
        lista.add("xd");
        lista.add("adios");
        lista.add("argos");
        lista.add("amn");
        lista.add("ancara");
        lista.add("messi");
        CustomReclyclerViewAdapter adapter = new CustomReclyclerViewAdapter(this, lista);
        filmsRecyclerView.setAdapter(adapter);

    }
}