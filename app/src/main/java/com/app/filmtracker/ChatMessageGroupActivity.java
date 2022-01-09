package com.app.filmtracker;

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
import com.app.filmtracker.vo.Group;
import com.app.filmtracker.vo.Message;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatMessageGroupActivity extends AppCompatActivity {

    //Firebase
    private FirebaseUser thisUser;
    private FirebaseFirestore db;

    //View Components
    private MaterialToolbar topMenuToolbar;
    private EditText chatMessageEditText;
    private RecyclerView recyclerView;
    private ImageButton chatMessageSendButton;

    //Data
    private Group thisGroup;
    private List<Message> messages;


    //Custom Recycler View Adapter
    private ChatMessageRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message_group);

        //Init data
        thisGroup = (Group) SingletonMap.getInstance().get(SingletonMap.CHAT_ACTUAL_GROUP);
        messages = new ArrayList<>();

        //View Components
        topMenuToolbar = findViewById(R.id.chatMessageGroupTopMenuToolbar);
        chatMessageEditText = findViewById(R.id.chatMessageGroupEditText);
        recyclerView = findViewById(R.id.chatMessageGroupRecyclerView);
        chatMessageSendButton = findViewById(R.id.chatMessageGroupSendButton);

        //Firebase
        thisUser = (FirebaseUser) SingletonMap.getInstance().get(SingletonMap.FIREBASE_USER_INSTANCE);
        db = FirebaseFirestore.getInstance();

        //Recycler View
        configRecyclerView();

        //Fetch Data and set Snapshot Listener
        addSnapshotListenerMessagesInGroup(thisGroup);

        //Send botttom
        chatMessageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = chatMessageEditText.getText().toString().trim();
                if(!text.isEmpty()){
                    sendData(text);
                }
                chatMessageEditText.setText("");
            }
        });

        //Material Toolbar - Top menu
        topMenuToolbar.setTitle(thisGroup.getName());
        topMenuToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void configRecyclerView(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new ChatMessageRecyclerViewAdapter(this, messages, thisUser.getEmail(), true);
        recyclerView.setAdapter(adapter);
    }

    private void addSnapshotListenerMessagesInGroup(Group group){
        db.collection("GroupMessage")
                .whereEqualTo("group_id", group.getId())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        boolean existsNewMessages = false;
                        for(DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType().equals(DocumentChange.Type.ADDED)) {
                                QueryDocumentSnapshot doc = dc.getDocument();
                                Message m = new Message();
                                m.setText((String) doc.getData().get("text"));
                                m.setFrom((String) doc.getData().get("from"));
                                m.setFromName((String) doc.getData().get("from_name"));
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


    private void sendData(String text){
        Map<String, Object> data = new HashMap<>();
        data.put("from", thisUser.getEmail());
        String name;
        if(thisUser.getDisplayName() != null && !thisUser.getDisplayName().isEmpty())
            name = thisUser.getDisplayName();
        else
            name = thisUser.getEmail();
        data.put("from_name", name);
        data.put("group_id", thisGroup.getId());
        data.put("text", text);
        data.put("time", Timestamp.now());

        db.collection("GroupMessage")
                .add(data);
    }

}