package com.app.filmtracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatPrivateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatPrivateFragment extends Fragment {
    


    public ChatPrivateFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ChatPrivateFragment newInstance() {
        ChatPrivateFragment fragment = new ChatPrivateFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


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


    ChatRecyclerViewAdapter adapter;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_private, container, false);

        //View Components
        chatProgressBar = view.findViewById(R.id.chatProgressBar);
        chatTextViewLoading = view.findViewById(R.id.chatTextViewLoading);
        recyclerView = view.findViewById(R.id.chatRecyclerView);
        chatAddFriendButton = view.findViewById(R.id.chatAddFriendButton);

        chatProgressBar.setVisibility(View.VISIBLE);

        //Android Volley
        requestQueue = (RequestQueue) SingletonMap.getInstance().get(SingletonMap.REQUEST_QUEUE);
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getContext());
            SingletonMap.getInstance().put(SingletonMap.REQUEST_QUEUE, requestQueue);
        }

        //Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        thisUser = (FirebaseUser) SingletonMap.getInstance().get(SingletonMap.FIREBASE_USER_INSTANCE);

        //Recycler View - Get friend list
        loadFriendsAndConfRecycler();



        //Action Floating Button - Add Friends
        chatAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View viewDialog = LayoutInflater.from(getContext()).inflate(R.layout.dialog_chat_add_friend, null, false);
                launchDialog(viewDialog);
            }
        });

        return view;
    }


    private void loadFriendsAndConfRecycler(){
        List<Friend> friendList = (List<Friend>) SingletonMap.getInstance().get("FRIEND_LIST");
        if(friendList == null) {

            db.collection("Friend")
                    .whereArrayContains("email", thisUser.getEmail())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                                if (documents == null || documents.isEmpty()) {
                                    //We need to show to the user that he doenst have friends
                                    List<Friend> f = new ArrayList<>();
                                    SingletonMap.getInstance().put("FRIEND_LIST", f);
                                    chatProgressBar.setVisibility(View.INVISIBLE);
                                    chatTextViewLoading.setText(R.string.chat_without_friends);
                                } else {
                                    //At least the user have 1 friend
                                    List<String> emailFriendsList = new ArrayList<>();
                                    for (DocumentSnapshot doc : documents) {
                                        List<String> emails = (List<String>) doc.getData().get("email");
                                        if (emails.get(0).equalsIgnoreCase(thisUser.getEmail())) //Remove the self user
                                            emailFriendsList.add(emails.get(1));
                                        else
                                            emailFriendsList.add(emails.get(0));
                                    }
                                    emailFriendsList.addAll(emailFriendsList);
                                    getFriendsDetails(emailFriendsList, true);
                                }

                            } else {
                                Toast.makeText(getContext(), "Ha ocurrido un error al cargar tu email(debug).",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            configureRecyclerView(friendList);
        }

    }

    //------------------Dialog add friends-------------------
    private void launchDialog(View customView){
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getContext());
        TextInputLayout textInputLayout = customView.findViewById(R.id.dialogChatTextInput);
        dialog.setView(customView);
        dialog.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_corners_curved));
        dialog.setTitle(R.string.chat_add_dialog_title);
        dialog.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.out.println("---------------PULSADO EN OK");
                String emailUserName = textInputLayout.getEditText().getText().toString();

                db.collection("User")
                        .whereEqualTo("email", emailUserName)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                                    if(documents == null || documents.isEmpty()) {
                                        Toast.makeText(getContext(), "No se ha encontrado el usuario(debug).",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Friend f = new Friend();
                                        f.setEmail((String) documents.get(0).getData().get("email"));
                                        f.setFullName((String) documents.get(0).getData().get("full_name"));
                                        f.setUsername((String) documents.get(0).getData().get("username"));
                                        f.setHas_image((Boolean) documents.get(0).getData().get("has_image"));
                                        f.setProfileImage(null);
                                        List<Friend> friendList = (List<Friend>) SingletonMap.getInstance().get("FRIEND_LIST");
                                        friendList.add(f);
                                        adapter.notifyDataSetChanged();

                                        //Quizas put? en el singleton
                                        Map<String, Object> data = new HashMap<>();
                                        List<String> dataList = new ArrayList<>();
                                        dataList.add(emailUserName);
                                        dataList.add(thisUser.getEmail());
                                        data.put("email", dataList);
                                        db.collection("Friend")
                                                .add(data)
                                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        Toast.makeText(getContext(), "Se añadió correctamente .",
                                                                Toast.LENGTH_SHORT).show();

                                                    }
                                                });
                                    }


                                } else {
                                    Toast.makeText(getContext(), "No se ha encontrado el usuario(debug).",
                                            Toast.LENGTH_SHORT).show();
                                }
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

    private void getFriendsDetails(List<String> emailList, boolean configureRecyclerView)  {

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
                            SingletonMap.getInstance().put("FRIEND_LIST", friendList);
                            if(configureRecyclerView)
                                configureRecyclerView(friendList);
                        } else {
                            Toast.makeText(getContext(), "Ha ocurrido un error al los datos de tus amigos(debug).",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void configureRecyclerView(List<Friend> friends){
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        chatTextViewLoading.setVisibility(View.INVISIBLE);
        chatProgressBar.setVisibility(View.INVISIBLE);

        adapter = new ChatRecyclerViewAdapter(getContext(), friends);
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChatMessageActivity.class);
                Friend actualChat = friends.get(recyclerView.getChildAdapterPosition(view));
                SingletonMap.getInstance().put("ACTUAL_CHAT", actualChat);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

    }
}