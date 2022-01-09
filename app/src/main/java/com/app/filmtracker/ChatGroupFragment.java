package com.app.filmtracker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatGroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatGroupFragment extends Fragment {


    public ChatGroupFragment() {
        // Required empty public constructor
    }


    public static ChatGroupFragment newInstance(String param1, String param2) {
        ChatGroupFragment fragment = new ChatGroupFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_group, container, false);
    }
}