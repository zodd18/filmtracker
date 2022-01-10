package com.app.filmtracker.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.filmtracker.R;
import com.app.filmtracker.poo.SingletonMap;
import com.app.filmtracker.vo.FilmUserVote;
import com.app.filmtracker.vo.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatMessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SELF = 0;
    private static final int VIEW_TYPE_FRIEND = 1;
    private static final int VIEW_TYPE_FRIEND_NAMED = 2;
    private static final int VIEW_TYPE_VOTE = 3;

    private View.OnClickListener onClickListener;
    private LayoutInflater mInflater;
    private Context ctx;

    //Data
    private List<Message> data;
    private String thisUserEmail;
    private SimpleDateFormat sdt;
    private boolean showFriendNames;
    private Set<String> messagesIdVoted;
    private Set<String> messagesFinishVoted;
    private String groupId;

    //entrara una lsita de Message
    public ChatMessageRecyclerViewAdapter(Context context, List<Message> messages, String thisUserEmail, boolean showFriendNames) {
        this.ctx = context;
        this.mInflater = LayoutInflater.from(context);
        this.data = messages;
        this.thisUserEmail = thisUserEmail;
        this.showFriendNames = showFriendNames;
        sdt = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    }

    public ChatMessageRecyclerViewAdapter(Context context, List<Message> messages,
                                          String thisUserEmail, boolean showFriendNames,
                                          Set<String> messagesIdVoted, Set<String> messagesFinishVoted,
                                          String groupId) {
        this.ctx = context;
        this.mInflater = LayoutInflater.from(context);
        this.data = messages;
        this.messagesIdVoted = messagesIdVoted;
        this.groupId = groupId;
        this.messagesFinishVoted = messagesFinishVoted;
        this.thisUserEmail = thisUserEmail;
        this.showFriendNames = showFriendNames;
        sdt = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    }


    //ViewHolder de los mensajes propios
    public static class SelfMessageViewHolder extends RecyclerView.ViewHolder  {
        private TextView chatItemSelfMessage;
        private TextView chatItemSelfTime;

        SelfMessageViewHolder(View itemView) {
            super(itemView);
            chatItemSelfMessage = itemView.findViewById(R.id.chatItemSelfMessage);
            chatItemSelfTime = itemView.findViewById(R.id.chatItemSelfTime);
        }
    }

    //ViewHolder de los mensajes de los amigos sin nombre
    public static class FriendMessageViewHolder extends RecyclerView.ViewHolder  {
        private TextView chatItemFriendMessage;
        private TextView chatItemFriendTime;

        FriendMessageViewHolder(View itemView) {
            super(itemView);
            chatItemFriendMessage = itemView.findViewById(R.id.chatItemFriendMessage);
            chatItemFriendTime = itemView.findViewById(R.id.chatItemFriendTime);
        }
    }

    //ViewHolder de los mensajes de los amigos con nombre
    public static class FriendMessageNamedViewHolder extends RecyclerView.ViewHolder  {
        private TextView chatItemFriendName;
        private TextView chatItemFriendMessage;
        private TextView chatItemFriendTime;

        FriendMessageNamedViewHolder(View itemView) {
            super(itemView);
            chatItemFriendName = itemView.findViewById(R.id.chatItemFriendMessageNamedName);
            chatItemFriendMessage = itemView.findViewById(R.id.chatItemFriendMessageNamedText);
            chatItemFriendTime = itemView.findViewById(R.id.chatItemFriendNamedTime);
        }
    }

    //ViewHolder de los votos
    public static class VoteMessageViewHolder extends RecyclerView.ViewHolder  {
        private TextView chatVotationTextView;
        private TextView filmName;
        private Button buttonStartVote;
        private FirebaseFirestore db;

        //Data
        private String groupMessageId;
        private String groupId;
        AlertDialog modal;

        VoteMessageViewHolder(View itemView) {
            super(itemView);
            db = FirebaseFirestore.getInstance();
            chatVotationTextView = itemView.findViewById(R.id.chatVotationTextView);
            filmName = itemView.findViewById(R.id.chatVotationFilmName);
            buttonStartVote = itemView.findViewById(R.id.buttonStartVote);
        }

        //---------------------VOTE
        private void setButtonVote(){
            buttonStartVote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View viewDialog = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_chat_vote, null, false);
                    launchVoteModal(view, viewDialog, groupMessageId, groupId);
                }
            });
        }

        private void launchVoteModal(View view, View viewDialog, String messageId, String groupId){
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(view.getContext());
            RecyclerView recyclerView = viewDialog.findViewById(R.id.dialogChatVoteRecycler);
            recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));

            DialogVoteRecyclerView adapter = new DialogVoteRecyclerView(view.getContext());
            adapter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    modal.dismiss();
                    int cal = 10 - recyclerView.getChildAdapterPosition(view);

                    FirebaseUser thisUser = (FirebaseUser) SingletonMap.getInstance().get(SingletonMap.FIREBASE_USER_INSTANCE);
                    Map<String, Object> data = new HashMap<>();
                    String name = thisUser.getEmail();
                    if(thisUser.getDisplayName()!= null && !thisUser.getDisplayName().isEmpty()){
                        name = thisUser.getDisplayName();
                    }
                    data.put("user_name", name);
                    data.put("user_email", thisUser.getEmail());
                    data.put("point", cal);
                    data.put("group_message_id", messageId);
                    data.put("group_id", groupId);

                    db.collection("FilmUserVote")
                            .add(data);
                }
            });
            recyclerView.setAdapter(adapter);

            dialog.setView(viewDialog);
            dialog.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.shape_corners_curved));
            dialog.setTitle("Votar una pelicula");
            modal = dialog.show();
        }

        //---------------------VIEW RESULTS
        private void setButtonResults(){
            this.chatVotationTextView.setText("Votación terminada!");
            this.buttonStartVote.setEnabled(true);
            this.buttonStartVote.setText("Ver resultados");
            buttonStartVote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View viewDialog = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_chat_vote, null, false);
                    launchResultsModal(view, viewDialog, groupMessageId, groupId);
                }
            });
        }


        private void launchResultsModal(View view, View viewDialog, String messageId, String groupId){
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(view.getContext());
            RecyclerView recyclerView = viewDialog.findViewById(R.id.dialogChatVoteRecycler);
            TextView textView = viewDialog.findViewById(R.id.dialogChatVoteTextView);
            textView.setText("Puntuación de tus amigos");
            recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));

            List<FilmUserVote> filmUserVotes = new ArrayList<>();
            DialogVoteResultsRecyclerView adapter = new DialogVoteResultsRecyclerView(view.getContext(), filmUserVotes);
            recyclerView.setAdapter(adapter);

            db.collection("FilmUserVote")
                    .whereEqualTo("group_message_id", groupMessageId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                List<DocumentSnapshot> dslist = task.getResult().getDocuments();
                                for(DocumentSnapshot ds: dslist){
                                    FilmUserVote fuv = new FilmUserVote();

                                    Double point = ds.getDouble("point");
                                    fuv.setPoint(point.intValue());
                                    fuv.setUserName((String) ds.getData().get("user_name"));
                                    fuv.setUserEmail((String) ds.getData().get("user_email"));
                                    filmUserVotes.add(fuv);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });

            dialog.setView(viewDialog);
            dialog.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.shape_corners_curved));
            dialog.setTitle("Resultados del voto");
            modal = dialog.show();
        }
    }



    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SELF){
            View view = mInflater.inflate(R.layout.chat_self_item, parent, false);
            return new SelfMessageViewHolder(view);
        } else if(viewType == VIEW_TYPE_FRIEND){
            View view = mInflater.inflate(R.layout.chat_friend_item, parent, false);
            return new FriendMessageViewHolder(view);
        } else if(viewType == VIEW_TYPE_FRIEND_NAMED){
            View view = mInflater.inflate(R.layout.chat_friend_item_named, parent, false);
            return new FriendMessageNamedViewHolder(view);
        } else { //VIEW_TYPE_VOTE
            View view = mInflater.inflate(R.layout.chat_group_votation_item, parent, false);
            return new VoteMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SELF){
            SelfMessageViewHolder selfHolder = (SelfMessageViewHolder) holder;

            selfHolder.chatItemSelfMessage.setText(data.get(position).getText());
            selfHolder.chatItemSelfTime.setText(sdt.format(data.get(position).getDate()));
        } else if(getItemViewType(position) == VIEW_TYPE_FRIEND){
            FriendMessageViewHolder friendHolder = (FriendMessageViewHolder) holder;

            friendHolder.chatItemFriendMessage.setText(data.get(position).getText());
            friendHolder.chatItemFriendTime.setText(sdt.format(data.get(position).getDate()));
        } else if(getItemViewType(position) == VIEW_TYPE_FRIEND_NAMED){
            FriendMessageNamedViewHolder friendHolder = (FriendMessageNamedViewHolder) holder;

            friendHolder.chatItemFriendName.setText(data.get(position).getFromName());
            friendHolder.chatItemFriendMessage.setText(data.get(position).getText());
            friendHolder.chatItemFriendTime.setText(sdt.format(data.get(position).getDate()));
        } else { //VIEW_TYPE_VOTE
            VoteMessageViewHolder voteHolder = (VoteMessageViewHolder) holder;


            voteHolder.groupId = this.groupId;
            voteHolder.groupMessageId = data.get(position).getId();
            voteHolder.filmName.setText(data.get(position).getText());

            if(messagesFinishVoted.contains(data.get(position).getId())){   //Todos han votado
                voteHolder.setButtonResults();
            } else if(messagesIdVoted.contains(data.get(position).getId())) { //Ya he votado pero faltan compas
                voteHolder.buttonStartVote.setEnabled(false);
                voteHolder.buttonStartVote.setText("Esperando al resto...");
                //voteHolder.buttonStartVote.setVisibility(View.INVISIBLE);
            } else {    //Aun no he votado
                voteHolder.setButtonVote();
            }

        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }//+filmVotes.size()

    @Override
    public int getItemViewType(int position) {
        //Mirar si es mio el mensaje o de mis amigos

        if(data.get(position).isVote())
            return VIEW_TYPE_VOTE;

        if(data.get(position).getFrom().equalsIgnoreCase(this.thisUserEmail)){
            return VIEW_TYPE_SELF;
        } else {
            if(position >= 1){
                if(this.data.get(position).getFrom().equalsIgnoreCase(this.data.get(position-1).getFrom())){
                    return VIEW_TYPE_FRIEND;
                } else {
                    return VIEW_TYPE_FRIEND_NAMED;
                }
            } else {
                return VIEW_TYPE_FRIEND_NAMED;
            }
        }
    }
}
