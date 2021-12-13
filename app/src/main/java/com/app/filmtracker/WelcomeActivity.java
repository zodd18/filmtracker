package com.app.filmtracker;

import static android.graphics.Color.RED;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.ArrayList;
import java.util.List;


public class WelcomeActivity extends AppCompatActivity {

    private List<Integer> imageArray;
    private CarouselView carouselView;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        //Carousel
        imageArray = new ArrayList();
        imageArray.add(R.drawable.imagen1);
        imageArray.add(R.drawable.img2);

        carouselView = findViewById(R.id.carouselViewWelcomeActivity);
        carouselView.setPageCount(imageArray.size());
        carouselView.setImageListener(imageListener);
        carouselView.setSlideInterval(10000);   //10 segundos
        carouselView.setFillColor(RED);


        //Events
        startButton = findViewById(R.id.welcomeStartButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            imageView.setImageResource(imageArray.get(position));
        }
    };

}