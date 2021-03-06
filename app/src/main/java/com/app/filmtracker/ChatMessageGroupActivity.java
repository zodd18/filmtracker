package com.app.filmtracker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.app.filmtracker.poo.SingletonMap;
import com.app.filmtracker.recycler.ChatMessageRecyclerViewAdapter;
import com.app.filmtracker.vo.Friend;
import com.app.filmtracker.vo.Group;
import com.app.filmtracker.vo.Message;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Set<String> messagesIdVoted;
    private Map<String, Integer> numVotes;
    private Set<String> messagesFinishVoted;

    //Custom Recycler View Adapter
    private ChatMessageRecyclerViewAdapter adapter;

    //Aditional data
    boolean firstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message_group);

        //Init data
        thisGroup = (Group) SingletonMap.getInstance().get(SingletonMap.CHAT_ACTUAL_GROUP);
        messages = new ArrayList<>();
        messagesIdVoted = new HashSet<>();
        numVotes = new HashMap<>();
        messagesFinishVoted = new HashSet<>();
        firstTime = true;

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
        addSnapshotListenerVoteMessagesSelf(thisGroup);
        addSnapshotListenerVoteMessagesAll();

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
        topMenuToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.topMenuCreateVote:
                        System.out.println("--------entra en votacion");
                        View viewDialog = LayoutInflater.from(ChatMessageGroupActivity.this).inflate(R.layout.dialog_chat_add_friend, null, false);
                        launchDialogCreateVote(viewDialog);

                        return true;
                }
                return false;
            }
        });
    }

    private void launchDialogCreateVote(View customView){
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
        TextView textViewInfo = customView.findViewById(R.id.chatItemUserName);
        TextInputLayout textInputLayout = customView.findViewById(R.id.dialogChatTextInput);
        textViewInfo.setText(this.getString(R.string.vote_dialog_create_info));
        textInputLayout.setStartIconDrawable(R.drawable.ic_baseline_poll_24);
        textInputLayout.setHint(this.getString(R.string.vote_dialog_create_write));

        dialog.setView(customView);
        dialog.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_corners_curved));
        dialog.setTitle(this.getString(R.string.vote_dialog_create_title));
        dialog.setPositiveButton(this.getString(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String text = textInputLayout.getEditText().getText().toString().trim();
                if(!text.isEmpty()){
                    Map<String, Object> data = new HashMap<>();
                    String name = thisUser.getEmail();
                    if(thisUser.getDisplayName() != null && !thisUser.getDisplayName().isEmpty())
                        name = thisUser.getDisplayName();

                    data.put("from_name", name);
                    data.put("from", thisUser.getEmail());
                    data.put("group_id", thisGroup.getId());
                    data.put("text", text);
                    data.put("time", Timestamp.now());
                    data.put("is_vote", true);
                    db.collection("GroupMessage")
                            .add(data);
                }
            }
        });
        dialog.show();
    }

    private void configRecyclerView(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new ChatMessageRecyclerViewAdapter(this, messages, thisUser.getEmail(),
                true, messagesIdVoted, messagesFinishVoted, thisGroup.getId());
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
                                m.setVote((Boolean) doc.getData().get("is_vote"));
                                m.setId(doc.getId());
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


    private void addSnapshotListenerVoteMessagesAll(){


        db.collection("FilmUserVote")
                .whereEqualTo("group_id", thisGroup.getId())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        for(DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType().equals(DocumentChange.Type.ADDED)) {
                                String idMessage = (String) dc.getDocument().getData().get("group_message_id");
                                int ant = 0;
                                if(numVotes.get(idMessage)!= null)
                                    ant = numVotes.get(idMessage);
                                numVotes.put(idMessage, ant+1);

                                if(numVotes.get(idMessage).equals(thisGroup.getEmails().size())){
                                    //Votacion terminada - Todos han votado
                                    messagesFinishVoted.add(idMessage);
                                    adapter.notifyDataSetChanged();
                                    if(!firstTime){
                                        Toast.makeText(ChatMessageGroupActivity.this, getString(R.string.vote_complete_toast),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                        firstTime = false;
                    }
                });
    }


    private void addSnapshotListenerVoteMessagesSelf(Group group){

        db.collection("FilmUserVote")
                .whereEqualTo("user_email", thisUser.getEmail())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        boolean existsNewVotes = false;
                        for(DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType().equals(DocumentChange.Type.ADDED)) {
                                QueryDocumentSnapshot doc = dc.getDocument();
                                messagesIdVoted.add((String) doc.getData().get("group_message_id"));
                                existsNewVotes = true;
                            }
                        }
                        if(existsNewVotes){
                            adapter.notifyDataSetChanged();
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
        data.put("is_vote", false);

        db.collection("GroupMessage")
                .add(data);
    }

}