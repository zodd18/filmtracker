package com.app.filmtracker.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.filmtracker.R;
import com.app.filmtracker.vo.FilmUserVote;

import java.util.List;

public class DialogVoteResultsRecyclerView extends RecyclerView.Adapter<DialogVoteResultsRecyclerView.ViewHolder> {

    private LayoutInflater mInflater;
    private Context ctx;
    private List<FilmUserVote> data;


    public DialogVoteResultsRecyclerView(Context context, List<FilmUserVote> data) {
        this.ctx = context;
        this.mInflater = LayoutInflater.from(context);
        this.data = data;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder  {
        private ImageView chatDialogVoteResultImage;
        private TextView chatDialogVoteResultUserName;
        private TextView chatDialogVoteResultPoint;

        ViewHolder(View itemView) {
            super(itemView);
            chatDialogVoteResultImage = itemView.findViewById(R.id.chatDialogVoteResultImage);
            chatDialogVoteResultUserName = itemView.findViewById(R.id.chatDialogVoteResultUserName);
            chatDialogVoteResultPoint = itemView.findViewById(R.id.chatDialogVoteResultPoint);
        }
    }


    @Override
    @NonNull
    public DialogVoteResultsRecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.chat_group_vote_result_recycler_item, parent, false);
        return new DialogVoteResultsRecyclerView.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DialogVoteResultsRecyclerView.ViewHolder holder, int position) {
        int cal = data.get(position).getPoint();
        holder.chatDialogVoteResultPoint.setText(""+cal);
        holder.chatDialogVoteResultUserName.setText(data.get(position).getUserName());
        switch (cal){
            case 0:
                holder.chatDialogVoteResultImage.setImageResource(R.drawable.img_emoti_caca_multicolor);
                break;
            case 1:
                holder.chatDialogVoteResultImage.setImageResource(R.drawable.img_emoti_vomiting);
                break;
            case 2:
                holder.chatDialogVoteResultImage.setImageResource(R.drawable.img_emoti_crying);
                break;
            case 3:
                holder.chatDialogVoteResultImage.setImageResource(R.drawable.img_emoti_sad);
                break;
            case 4:
                holder.chatDialogVoteResultImage.setImageResource(R.drawable.img_emoti_angry);
                break;
            case 5:
                holder.chatDialogVoteResultImage.setImageResource(R.drawable.img_medal_bronze);
                break;
            case 6:
                holder.chatDialogVoteResultImage.setImageResource(R.drawable.img_medal_silver);
                break;
            case 7:
                holder.chatDialogVoteResultImage.setImageResource(R.drawable.img_medal_gold);
                break;
            case 8:
                holder.chatDialogVoteResultImage.setImageResource(R.drawable.img_trophy_bronze);
                break;
            case 9:
                holder.chatDialogVoteResultImage.setImageResource(R.drawable.img_trophy_silver);
                break;
            case 10:
                holder.chatDialogVoteResultImage.setImageResource(R.drawable.img_trophy_gold);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
