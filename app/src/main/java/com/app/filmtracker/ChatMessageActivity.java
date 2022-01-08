package com.app.filmtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.app.filmtracker.poo.SingletonMap;
import com.app.filmtracker.recycler.ChatMessageRecyclerViewAdapter;
import com.app.filmtracker.vo.Friend;
import com.app.filmtracker.vo.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ChatMessageActivity extends AppCompatActivity {

    //Firebase
    private FirebaseUser thisUser;
    private FirebaseFirestore db;

    private MaterialToolbar chatMessageTopMenuToolbar;
    private Friend thisFriend;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        //View Components
        chatMessageTopMenuToolbar = findViewById(R.id.chatMessageTopMenuToolbar);
        recyclerView = findViewById(R.id.chatMessageRecyclerView);

        //SingletonMap
        this.thisFriend = (Friend) SingletonMap.getInstance().get("ACTUAL_CHAT");

        //Firebase
        thisUser = (FirebaseUser) SingletonMap.getInstance().get(SingletonMap.FIREBASE_USER_INSTANCE);
        db = FirebaseFirestore.getInstance();

        //App Bar Layout - Top menu
        chatMessageTopMenuToolbar.setTitle(thisFriend.getFullName());
        chatMessageTopMenuToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        db.collection("Message")
                .whereEqualTo("from", thisUser.getEmail())
                .whereEqualTo("to", thisFriend.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            List<DocumentSnapshot> documentsFrom = task.getResult().getDocuments();
                            List<Message> messages = new ArrayList<>();
                            for(DocumentSnapshot doc : documentsFrom){
                                Message m = new Message();
                                m.setText((String) doc.getData().get("text"));
                                m.setFrom(thisUser.getEmail());
                                m.setTo(thisFriend.getEmail());
                                m.setDate(doc.getTimestamp("time").toDate());
                                messages.add(m);
                            }
                            db.collection("Message")
                                    .whereEqualTo("to", thisUser.getEmail())
                                    .whereEqualTo("from", thisFriend.getEmail())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if(task.isSuccessful()){
                                                List<DocumentSnapshot> documentsTo = task.getResult().getDocuments();
                                                for(DocumentSnapshot doc : documentsTo){
                                                    Message m = new Message();
                                                    m.setText((String) doc.getData().get("text"));
                                                    m.setFrom(thisFriend.getEmail());
                                                    m.setTo(thisUser.getEmail());
                                                    m.setDate(doc.getTimestamp("time").toDate());
                                                    messages.add(m);
                                                }
                                                Collections.sort(messages, Comparator.comparing(Message::getDate));
                                                configureRecyclerView(messages);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void configureRecyclerView(List<Message> messageList){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        ChatMessageRecyclerViewAdapter adapter = new ChatMessageRecyclerViewAdapter(this, messageList, thisUser.getEmail());
        recyclerView.setAdapter(adapter);
    }
}