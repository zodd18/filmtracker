package com.app.filmtracker.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.filmtracker.R;
import com.app.filmtracker.vo.Message;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.List;

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
//    private List<FilmVote> filmVotes;
    private String thisUserEmail;
    private SimpleDateFormat sdt;
    private boolean showFriendNames;

    //entrara una lsita de Message
    public ChatMessageRecyclerViewAdapter(Context context, List<Message> messages, String thisUserEmail, boolean showFriendNames) {
        this.ctx = context;
        this.mInflater = LayoutInflater.from(context);
        this.data = messages;
//        this.filmVotes = new ArrayList<>();
        this.thisUserEmail = thisUserEmail;
        this.showFriendNames = showFriendNames;
        sdt = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    }

//    public ChatMessageRecyclerViewAdapter(Context context, List<Message> messages, String thisUserEmail, boolean showFriendNames, List<FilmVote> filmVotes) {
//        this.ctx = context;
//        this.mInflater = LayoutInflater.from(context);
//        this.data = messages;
//        this.filmVotes = filmVotes;
//        this.thisUserEmail = thisUserEmail;
//        this.showFriendNames = showFriendNames;
//        sdt = new SimpleDateFormat("HH:mm dd/MM/yyyy");
//    }

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
        private TextView filmName;
        private Button buttonStartVote;

        VoteMessageViewHolder(View itemView) {
            super(itemView);
            filmName = itemView.findViewById(R.id.chatVotationFilmName);
            buttonStartVote = itemView.findViewById(R.id.buttonStartVote);
            buttonStartVote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View viewDialog = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_chat_vote, null, false);
                    launchModal(view, viewDialog);
                }
            });
        }

        private void launchModal(View view,View viewDialog){
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(view.getContext());
            RecyclerView recyclerView = viewDialog.findViewById(R.id.dialogChatVoteRecycler);
            recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));

            DialogVoteRecyclerView adapter = new DialogVoteRecyclerView(view.getContext(), null);
            adapter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            recyclerView.setAdapter(adapter);

            dialog.setView(viewDialog);
            dialog.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.shape_corners_curved));
            dialog.setTitle("Votar una pelicula");
            dialog.show();
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

            voteHolder.filmName.setText(data.get(position).getText());
            //voteHolder.buttonStartVote

        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }//+filmVotes.size()

    @Override
    public int getItemViewType(int position) {
        //Mirar si es mio el mensaje o de mis amigos

//        if(filmVotes.get(position).getTime().after(data.get(position).getDate()))
//            return VIEW_TYPE_VOTE;

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
