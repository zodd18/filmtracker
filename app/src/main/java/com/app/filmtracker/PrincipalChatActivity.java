package com.app.filmtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class PrincipalChatActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private MaterialToolbar topMenuToolbar;

    //Fragments
    private ChatPrivateFragment chatPrivateFragment;
    private ChatGroupFragment chatGroupFragment;

    private CustomViewPagerAdapter fragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_chat);

        //View Components
        viewPager = findViewById(R.id.principalChatViewPager);
        tabLayout = findViewById(R.id.principalChatTabLayout);
        topMenuToolbar = findViewById(R.id.principalChatTopMenuToolbar);

        //Fragments
        chatPrivateFragment = new ChatPrivateFragment();
        chatGroupFragment = new ChatGroupFragment();

        //Configure ViewPager2Adapter
        configViewPager2Adapter();

        //Material Toolbar - Top menu
        topMenuToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void configViewPager2Adapter(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentAdapter = new CustomViewPagerAdapter(fragmentManager, getLifecycle());
        fragmentAdapter.putFragment(chatPrivateFragment);
        fragmentAdapter.putFragment(chatGroupFragment);
        viewPager.setAdapter(fragmentAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

    private class CustomViewPagerAdapter extends FragmentStateAdapter {

        private List<Fragment> fragments;


        public CustomViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
            this.fragments = new ArrayList<>();
        }

        public void putFragment(Fragment fragment){
            this.fragments.add(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }
    }
}