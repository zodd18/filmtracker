package com.app.filmtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.app.filmtracker.poo.SingletonMap;
import com.app.filmtracker.recycler.ChatMessageRecyclerViewAdapter;
import com.app.filmtracker.vo.Friend;
import com.app.filmtracker.vo.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatMessageActivity extends AppCompatActivity {

    //Firebase
    private FirebaseUser thisUser;
    private FirebaseFirestore db;

    //View components
    private MaterialToolbar chatMessageTopMenuToolbar;
    private EditText chatMessageEditText;
    private RecyclerView recyclerView;
    private ImageButton chatMessageSendButton;

    //Data
    private Friend thisFriend;
    private List<Message> messages;

    //Custom Recycler View Adapter
    private ChatMessageRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        messages = new ArrayList<>();

        //View Components
        chatMessageTopMenuToolbar = findViewById(R.id.chatMessageTopMenuToolbar);
        recyclerView = findViewById(R.id.chatMessageRecyclerView);
        chatMessageEditText = findViewById(R.id.chatMessageEditText);
        chatMessageSendButton = findViewById(R.id.chatMessageSendButton);

        //SingletonMap
        this.thisFriend = (Friend) SingletonMap.getInstance().get("ACTUAL_CHAT");

        //Firebase
        thisUser = (FirebaseUser) SingletonMap.getInstance().get(SingletonMap.FIREBASE_USER_INSTANCE);
        db = FirebaseFirestore.getInstance();

        //App Bar Layout - Top menu
        if(thisFriend.getFullName() == null || thisFriend.getFullName().isEmpty())
            chatMessageTopMenuToolbar.setTitle(thisFriend.getUsername());
        else
            chatMessageTopMenuToolbar.setTitle(thisFriend.getFullName());

        chatMessageTopMenuToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        chatMessageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = chatMessageEditText.getText().toString();
                if(text!=null && !text.trim().isEmpty()){
                    sendMessageToEmail(text.trim(), thisUser.getEmail(), thisFriend.getEmail());
                    chatMessageEditText.setText("");
                }

            }
        });


        //RecyclerView - Configure the recycler view and then Listen Add events in the db
        configureRecyclerView(messages);

    }

    private void configureRecyclerView(List<Message> messageList){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new ChatMessageRecyclerViewAdapter(this, messageList, thisUser.getEmail(), false);
        recyclerView.setAdapter(adapter);
        addSnapshotListenerBetweenUsers(thisUser.getEmail(), thisFriend.getEmail());
    }


    //Provide the realtime chat
    private void addSnapshotListenerBetweenUsers(String email1, String email2){
        db.collection("Message")
                .whereEqualTo("from", email1)
                .whereEqualTo("to", email2)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        boolean existsNewMessages = false;

                        for(DocumentChange dc : value.getDocumentChanges()){
                            if(dc.getType().equals(DocumentChange.Type.ADDED)){
                                QueryDocumentSnapshot doc = dc.getDocument();
                                Message m = new Message();
                                m.setText((String) doc.getData().get("text"));
                                m.setFrom(email1);
                                m.setTo(email2);
                                m.setDate(doc.getTimestamp("time").toDate());
                                messages.add(m);
                                existsNewMessages = true;
                            }
                        }
                        if(existsNewMessages){
                            Collections.sort(messages, Comparator.comparing(Message::getDate));
                            adapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(messages.size() - 1);
                        }

                    }
                });

        db.collection("Message")
                .whereEqualTo("from", email2)
                .whereEqualTo("to", email1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        boolean existsNewMessages = false;

                        for(DocumentChange dc : value.getDocumentChanges()){
                            if(dc.getType().equals(DocumentChange.Type.ADDED)){
                                QueryDocumentSnapshot doc = dc.getDocument();
                                Message m = new Message();
                                m.setText((String) doc.getData().get("text"));
                                m.setFrom(email2);
                                m.setTo(email1);
                                m.setDate(doc.getTimestamp("time").toDate());
                                messages.add(m);
                                existsNewMessages = true;
                            }
                        }
                        if(existsNewMessages){
                            Collections.sort(messages, Comparator.comparing(Message::getDate));
                            adapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(messages.size() - 1);
                        }

                    }
                });

    }

    private void sendMessageToEmail(String message, String emailFrom, String emailTo){
        Map<String, Object> data = new HashMap<>();
        data.put("from", emailFrom);
        data.put("to", emailTo);
        data.put("time", Timestamp.now());
        data.put("text", message);
        db.collection("Message")
                .add(data);
    }
}