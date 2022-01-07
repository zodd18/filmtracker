package com.app.filmtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.app.filmtracker.poo.SingletonMap;
import com.app.filmtracker.recycler.ChatRecyclerViewAdapter;
import com.app.filmtracker.vo.Friend;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    //Android Volley
    private RequestQueue requestQueue;

    //Firebase
    private FirebaseUser thisUser;
    private FirebaseFirestore db;

    //View Components
    private ProgressBar chatProgressBar;
    private TextView chatTextViewLoading;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //View Components
        chatProgressBar = findViewById(R.id.chatProgressBar);
        chatTextViewLoading = findViewById(R.id.chatTextViewLoading);
        recyclerView = findViewById(R.id.chatRecyclerView);

        chatProgressBar.setVisibility(View.VISIBLE);

        //Android Volley
        requestQueue = (RequestQueue) SingletonMap.getInstance().get(SingletonMap.REQUEST_QUEUE);
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(this);
            SingletonMap.getInstance().put(SingletonMap.REQUEST_QUEUE, requestQueue);
        }

        //Firebase
        db = FirebaseFirestore.getInstance();
        thisUser = (FirebaseUser) SingletonMap.getInstance().get(SingletonMap.FIREBASE_USER_INSTANCE);

        db.collection("Friend")
                .whereEqualTo("user_email", thisUser.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> emailFriendsList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, String> mapList = (Map<String, String>) document.getData().get("friend_list");
                                emailFriendsList.addAll(mapList.values());
                                getFriendsDetails(emailFriendsList);
                            }
                        } else {
                            Toast.makeText(ChatActivity.this, "Ha ocurrido un error al cargar tu email(debug).",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private List<Friend> getFriendsDetails(List<String> emailList)  {
        List<Friend> friendList = new ArrayList<>();

        db.collection("User")
                .whereIn("email", emailList)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Friend> friendList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Friend f = new Friend();
                                f.setEmail((String) document.getData().get("email"));
                                f.setFullName((String) document.getData().get("full_name"));
                                f.setUsername((String) document.getData().get("username"));
                                friendList.add(f);
                            }
                            configureRecyclerView(friendList);
                        } else {
                            Toast.makeText(ChatActivity.this, "Ha ocurrido un error al los datos de tus amigos(debug).",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });



        return friendList;
    }

    private void configureRecyclerView(List<Friend> friends){
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        chatTextViewLoading.setVisibility(View.INVISIBLE);
        chatProgressBar.setVisibility(View.INVISIBLE);
        ChatRecyclerViewAdapter adapter = new ChatRecyclerViewAdapter(this, friends);

        recyclerView.setAdapter(adapter);

    }


}