package com.app.filmtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    //Android Volley
    private RequestQueue requestQueue;

    //Firebase
    private FirebaseUser thisUser;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    //View Components
    private ProgressBar chatProgressBar;
    private TextView chatTextViewLoading;
    private RecyclerView recyclerView;
    private FloatingActionButton chatAddFriendButton;
    private MaterialToolbar topMenuToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //View Components
        chatProgressBar = findViewById(R.id.chatProgressBar);
        chatTextViewLoading = findViewById(R.id.chatTextViewLoading);
        recyclerView = findViewById(R.id.chatRecyclerView);
        chatAddFriendButton = findViewById(R.id.chatAddFriendButton);
        topMenuToolbar = findViewById(R.id.chatTopMenuToolbar);

        chatProgressBar.setVisibility(View.VISIBLE);

        //Android Volley
        requestQueue = (RequestQueue) SingletonMap.getInstance().get(SingletonMap.REQUEST_QUEUE);
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(this);
            SingletonMap.getInstance().put(SingletonMap.REQUEST_QUEUE, requestQueue);
        }

        //Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        thisUser = (FirebaseUser) SingletonMap.getInstance().get(SingletonMap.FIREBASE_USER_INSTANCE);

        //Recycler View - Get friend list
        db.collection("Friend")
                .whereArrayContains("email", thisUser.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            if(documents == null || documents.isEmpty()){
                                //We need to show to the user that he doenst have friends
                                chatProgressBar.setVisibility(View.INVISIBLE);
                                chatTextViewLoading.setText(R.string.chat_without_friends);
                            } else {
                                //At least the user have 1 friend
                                List<String> emailFriendsList = new ArrayList<>();
                                for(DocumentSnapshot doc : documents){
                                    List<String> emails = (List<String>) doc.getData().get("email");
                                    if(emails.get(0).equalsIgnoreCase(thisUser.getEmail())) //Remove the self user
                                        emailFriendsList.add(emails.get(1));
                                    else
                                        emailFriendsList.add(emails.get(0));
                                }
                                emailFriendsList.addAll(emailFriendsList);
                                getFriendsDetails(emailFriendsList);
                            }

                        } else {
                            Toast.makeText(ChatActivity.this, "Ha ocurrido un error al cargar tu email(debug).",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        //App Bar Layout - Top menu
        topMenuToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Action Floating Button - Add Friends
        chatAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View viewDialog = LayoutInflater.from(ChatActivity.this).inflate(R.layout.dialog_chat_add_friend, null, false);
                launchDialog(viewDialog);
            }
        });
    }

    //------------------Dialog add friends-------------------
    private void launchDialog(View customView){
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(ChatActivity.this);
        TextInputLayout textInputLayout = customView.findViewById(R.id.dialogChatTextInput);
        dialog.setView(customView);
        dialog.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_corners_curved));
        dialog.setTitle(R.string.chat_add_dialog_title);
        dialog.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.out.println("---------------PULSADO EN OK");
                String emailUserName = textInputLayout.getEditText().getText().toString();

                Map<String, Object> data = new HashMap<>();
                List<String> friendList = new ArrayList<>();
                friendList.add(emailUserName);
                friendList.add(thisUser.getEmail());
                data.put("email", friendList);
                db.collection("Friend")
                        .add(data)
                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                Toast.makeText(ChatActivity.this, "Se añadió correctamente .",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        dialog.show();
    }


    //------------------Load and display in recycler view------------------
    //Note: First, we charge the email, fullname and the username. When it's loaded, we will charge
    //the recycler view. While the Recycler view are displaying the elements finally we will charge
    // the profile images.

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
                                f.setHas_image((Boolean) document.getData().get("has_image"));
                                f.setProfileImage(null);
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
                recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        chatTextViewLoading.setVisibility(View.INVISIBLE);
        chatProgressBar.setVisibility(View.INVISIBLE);
        ChatRecyclerViewAdapter adapter = new ChatRecyclerViewAdapter(this, friends);

        recyclerView.setAdapter(adapter);

    }


}