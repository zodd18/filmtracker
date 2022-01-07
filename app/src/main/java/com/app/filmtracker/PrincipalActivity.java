package com.app.filmtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class PrincipalActivity extends AppCompatActivity {

    //View Components
    NavigationBarView navigationBarView;
    MaterialToolbar topMenu;

    //Fragments
    ProfileFragment profileFragment;
    FilmsFragment filmsFragment;
    LikesFragment likesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        //Fragments
        profileFragment = new ProfileFragment();
        filmsFragment = new FilmsFragment();
        likesFragment = new LikesFragment();

        //View Components
        navigationBarView = findViewById(R.id.MainBottomNavigation);
        topMenu = findViewById(R.id.principalTopMenu);

        //Bottom Menu

        // Default fragment: profileFragment
        loadFragment(profileFragment);
        navigationBarView.setSelectedItemId(R.id.bottomMenuProfile);

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
                    case R.id.bottomMenuLikes:
                        loadFragment(likesFragment);
                        return true;
                }
                return false;
            }
        });


        //Top menu
        topMenu.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.topMenuChat:
                        System.out.println("--------entra en chat");
                        Intent intent = new Intent(PrincipalActivity.this, ChatActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.topMenuSearch:
                        System.out.println("--------entra en la busqueda");
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

    public void loadFragmentWithMotion(Fragment fragment, View view){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addSharedElement(view, "shared_container");
        fragmentTransaction.replace(R.id.mainFrameContainer, fragment);
        fragmentTransaction.commit();
    }
}