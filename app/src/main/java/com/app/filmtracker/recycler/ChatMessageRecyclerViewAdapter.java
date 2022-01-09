package com.app.filmtracker.recycler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.filmtracker.R;
import com.app.filmtracker.vo.Friend;
import com.app.filmtracker.vo.Message;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.List;

public class ChatMessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SELF = 0;
    private static final int VIEW_TYPE_FRIEND = 1;
    private static final int VIEW_TYPE_FRIEND_NAMED = 2;

    private View.OnClickListener onClickListener;
    private LayoutInflater mInflater;
    private Context ctx;
    private List<Message> data;
    private String thisUserEmail;
    private SimpleDateFormat sdt;
    private boolean showFriendNames;

    //entrara una lsita de Message
    public ChatMessageRecyclerViewAdapter(Context context, List<Message> messages, String thisUserEmail, boolean showFriendNames) {
        this.ctx = context;
        this.mInflater = LayoutInflater.from(context);
        this.data = messages;
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




    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SELF){
            View view = mInflater.inflate(R.layout.chat_self_item, parent, false);
            return new SelfMessageViewHolder(view);
        } else if(viewType == VIEW_TYPE_FRIEND){
            View view = mInflater.inflate(R.layout.chat_friend_item, parent, false);
            return new FriendMessageViewHolder(view);
        } else { //VIEW_TYPE_FRIEND_NAMED
            View view = mInflater.inflate(R.layout.chat_friend_item_named, parent, false);
            return new FriendMessageNamedViewHolder(view);
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
        } else {    //VIEW_TYPE_FRIEND_NAMED
            FriendMessageNamedViewHolder friendHolder = (FriendMessageNamedViewHolder) holder;
            friendHolder.chatItemFriendName.setText(data.get(position).getFromName());
            friendHolder.chatItemFriendMessage.setText(data.get(position).getText());
            friendHolder.chatItemFriendTime.setText(sdt.format(data.get(position).getDate()));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        //Mirar si es mio el mensaje o de mis amigos
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
