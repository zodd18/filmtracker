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

    private View.OnClickListener onClickListener;
    private LayoutInflater mInflater;
    private Context ctx;
    private List<Message> data;
    private String thisUserEmail;
    private SimpleDateFormat sdt;

    //entrara una lsita de Message
    public ChatMessageRecyclerViewAdapter(Context context, List<Message> messages, String thisUserEmail) {
        this.ctx = context;
        this.mInflater = LayoutInflater.from(context);
        this.data = messages;
        this.thisUserEmail = thisUserEmail;
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

    //ViewHolder de los mensajes de los amigos
    public static class FriendMessageViewHolder extends RecyclerView.ViewHolder  {
        private TextView chatItemFriendMessage;
        private TextView chatItemFriendTime;

        FriendMessageViewHolder(View itemView) {
            super(itemView);
            chatItemFriendMessage = itemView.findViewById(R.id.chatItemFriendMessage);
            chatItemFriendTime = itemView.findViewById(R.id.chatItemFriendTime);
        }
    }



    //terminado
    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SELF){
            View view = mInflater.inflate(R.layout.chat_self_item, parent, false);
            return new SelfMessageViewHolder(view);
        } else {
            View view = mInflater.inflate(R.layout.chat_friend_item, parent, false);
            return new FriendMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SELF){
            SelfMessageViewHolder selfHolder = (SelfMessageViewHolder) holder;
            selfHolder.chatItemSelfMessage.setText(data.get(position).getText());

            selfHolder.chatItemSelfTime.setText(sdt.format(data.get(position).getDate()));
        } else {
            FriendMessageViewHolder friendHolder = (FriendMessageViewHolder) holder;
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
        } else  {
            return VIEW_TYPE_FRIEND;
        }
    }
}
