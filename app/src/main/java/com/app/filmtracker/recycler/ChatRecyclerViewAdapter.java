package com.app.filmtracker.recycler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.filmtracker.R;
import com.app.filmtracker.vo.Friend;
import com.app.filmtracker.vo.Genre;

import java.util.ArrayList;
import java.util.List;

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder> implements View.OnClickListener{

    private View.OnClickListener onClickListener;
    private LayoutInflater mInflater;
    private Context ctx;
    private List<Friend> data;

    public ChatRecyclerViewAdapter(Context context, List<Friend> friendList) {
        this.ctx = context;
        this.mInflater = LayoutInflater.from(context);
        this.data = friendList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder  {
        private ImageView chatItemprofileImage;
        private TextView chatItemUserName;

        ViewHolder(View itemView) {
            super(itemView);
            chatItemprofileImage = itemView.findViewById(R.id.chatItemprofileImage);
            chatItemUserName = itemView.findViewById(R.id.chatItemUserName);
        }
    }

    @Override
    @NonNull
    public ChatRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.chat_item, parent, false);
        view.setOnClickListener(this);
        return new ChatRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name;
        if(data.get(position).getFullName()!=null && !data.get(position).getFullName().isEmpty())
            name = data.get(position).getFullName();
        else
            name = data.get(position).getUsername();
        holder.chatItemUserName.setText(name);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }



    //OnClick events
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public void onClick(View view) {
        if(this.onClickListener!=null)
            onClickListener.onClick(view);
    }
}
