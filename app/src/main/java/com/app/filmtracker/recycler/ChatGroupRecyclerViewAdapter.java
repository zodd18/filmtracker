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
import com.app.filmtracker.vo.Group;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ChatGroupRecyclerViewAdapter extends RecyclerView.Adapter<ChatGroupRecyclerViewAdapter.ViewHolder> implements View.OnClickListener {
    private View.OnClickListener onClickListener;
    private LayoutInflater mInflater;
    private Context ctx;
    private List<Group> data;

    public ChatGroupRecyclerViewAdapter(Context context, List<Group> groupList) {
        this.ctx = context;
        this.mInflater = LayoutInflater.from(context);
        this.data = groupList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder  {
        private ImageView chatItemprofileImage;
        private TextView chatItemGroupName;

        ViewHolder(View itemView) {
            super(itemView);
            chatItemprofileImage = itemView.findViewById(R.id.chatItemprofileImage);
            chatItemGroupName = itemView.findViewById(R.id.chatItemUserName);
        }
    }

    @Override
    @NonNull
    public ChatGroupRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.chat_item, parent, false);
        view.setOnClickListener(this);
        return new ChatGroupRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatGroupRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.chatItemGroupName.setText(data.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    //Profile image task
    /*private class ChargeImageProfileFireStorage extends AsyncTask<Object, Void, Void> {

        private String userEmail;
        private ChatRecyclerViewAdapter.ViewHolder holder;
        private Friend thisFriend;

        @Override
        protected Void doInBackground(Object... objects) {
            this.userEmail = (String) objects[0];
            this.holder = (ChatRecyclerViewAdapter.ViewHolder) objects [1];
            this.thisFriend = (Friend) objects [2];

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference().child(this.userEmail + "/" +"image_profile");
            final long FIVE_MEGABYTE = 1024 * 1024 * 5;
            storageReference.getBytes(FIVE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(@NonNull byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if(bitmap!=null){
                        thisFriend.setProfileImage(bitmap);
                        holder.chatItemprofileImage.setImageBitmap(bitmap);
                    }
                }
            });

            return null;
        }

    }*/

    //-------------------------OnClick events
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public void onClick(View view) {
        if(this.onClickListener!=null)
            onClickListener.onClick(view);
    }
}
