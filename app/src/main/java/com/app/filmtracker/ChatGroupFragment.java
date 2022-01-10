package com.app.filmtracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.app.filmtracker.recycler.ChatGroupRecyclerViewAdapter;
import com.app.filmtracker.recycler.ChatRecyclerViewAdapter;
import com.app.filmtracker.vo.Friend;
import com.app.filmtracker.vo.Group;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private FloatingActionButton chatAddGroupButton;


    ChatGroupRecyclerViewAdapter adapter;
    List<Group> groups;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_group, container, false);

        groups = new ArrayList<>();

        //View Components
        chatProgressBar = view.findViewById(R.id.chatGroupProgressBar);
        chatTextViewLoading = view.findViewById(R.id.chatGroupTextViewLoading);
        recyclerView = view.findViewById(R.id.chatGroupRecyclerView);
        chatAddGroupButton = view.findViewById(R.id.chatGroupCreateGroupButton);

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
        configRecyclerView();
        loadGroups();


        //Action Floating Button - Add Friends
        chatAddGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchDialog();
            }
        });



        return view;
    }

    private void configRecyclerView(){
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));


        adapter = new ChatGroupRecyclerViewAdapter(getContext(), groups);
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChatMessageGroupActivity.class);
                Group actualGroup = groups.get(recyclerView.getChildAdapterPosition(view));
                SingletonMap.getInstance().put(SingletonMap.CHAT_ACTUAL_GROUP, actualGroup);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadGroups(){
        chatTextViewLoading.setVisibility(View.VISIBLE);
        chatProgressBar.setVisibility(View.VISIBLE);

        db.collection("Group")
                .whereArrayContains("users_email", thisUser.getEmail())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        for(DocumentChange dc : value.getDocumentChanges()){
                            if(dc.getType().equals(DocumentChange.Type.ADDED)) {
                                QueryDocumentSnapshot doc = dc.getDocument();
                                Group g = new Group();
                                g.setId(doc.getId());
                                g.setName((String) doc.getData().get("name"));
                                g.setEmails((List<String>) doc.getData().get("users_email"));
                                groups.add(g);
                            }
                        }
                        chatTextViewLoading.setVisibility(View.INVISIBLE);
                        chatProgressBar.setVisibility(View.INVISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                });

    }

    private void launchDialog(){
        List<Friend> friendList = (List<Friend>) SingletonMap.getInstance().get("FRIEND_LIST");
        if(friendList!=null && !friendList.isEmpty()){

            List<String> friendNames = new ArrayList<>();

            for(Friend f : friendList){
                friendNames.add(f.getFullNameOrEmail());
            }

            CharSequence[] friendCharSequence = friendNames.toArray(new CharSequence[friendNames.size()]);
            boolean[] checked = new boolean[friendList.size()];
            Arrays.fill(checked, Boolean.FALSE);
            Set<Integer> posFriends = new HashSet<>();

            //Material
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getContext());
            //dialog.setView(customView);
            dialog.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_corners_curved));
            dialog.setTitle(getString(R.string.group_create));
            dialog.setMultiChoiceItems(friendCharSequence, checked, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                    if(b)
                        posFriends.add(i);
                    else
                        posFriends.remove(i);
                }
            });

            dialog.setPositiveButton(this.getString(R.string.dialog_create_group), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    List<String> emailList = new ArrayList<>();
                    for(Integer pos : posFriends){
                        emailList.add(friendList.get(pos).getEmail());
                    }
                    emailList.add(thisUser.getEmail());
                    launchSecondDialog(emailList);

                }
            });
            dialog.show();
        }

    }

    private void launchSecondDialog(List<String> emailList){
        View customView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_chat_group_name, null, false);
        TextInputLayout textInputLayout = customView.findViewById(R.id.dialogChatGroupTextInput);

        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getContext());
        dialog.setView(customView);
        dialog.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_corners_curved));
        dialog.setTitle(getString(R.string.group_create_add_name));
        dialog.setPositiveButton(this.getString(R.string.dialog_create_group), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = textInputLayout.getEditText().getText().toString().trim();
                if(!groupName.isEmpty()){
                    Map<String, Object> data = new HashMap<>();
                    data.put("users_email", emailList);
                    data.put("name", groupName);
                    db.collection("Group")
                            .add(data);
                } else {
                    Toast.makeText(getContext(), getString(R.string.dialog_name_cant_be_null),
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
        dialog.show();
    }



}