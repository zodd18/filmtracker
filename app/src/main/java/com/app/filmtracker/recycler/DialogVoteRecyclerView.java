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

import java.util.List;

public class DialogVoteRecyclerView extends RecyclerView.Adapter<DialogVoteRecyclerView.ViewHolder> implements View.OnClickListener{

    private View.OnClickListener onClickListener;
    private LayoutInflater mInflater;
    private Context ctx;


    public DialogVoteRecyclerView(Context context) {
        this.ctx = context;
        this.mInflater = LayoutInflater.from(context);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder  {
        private ImageView chatDialogVoteImage;
        private TextView chatDialogVoteNumber;

        ViewHolder(View itemView) {
            super(itemView);
            chatDialogVoteImage = itemView.findViewById(R.id.chatDialogVoteImage);
            chatDialogVoteNumber = itemView.findViewById(R.id.chatDialogVoteNumber);
        }
    }

    @Override
    @NonNull
    public DialogVoteRecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.chat_group_votation_recycler_item, parent, false);
        view.setOnClickListener(this);
        return new DialogVoteRecyclerView.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DialogVoteRecyclerView.ViewHolder holder, int position) {
        switch (position){
            case 0:
                holder.chatDialogVoteNumber.setText("10");
                holder.chatDialogVoteImage.setImageResource(R.drawable.img_trophy_gold);
                break;
            case 1:
                holder.chatDialogVoteNumber.setText("9");
                holder.chatDialogVoteImage.setImageResource(R.drawable.img_trophy_silver);
                break;
            case 2:
                holder.chatDialogVoteNumber.setText("8");
                holder.chatDialogVoteImage.setImageResource(R.drawable.img_trophy_bronze);
                break;
            case 3:
                holder.chatDialogVoteNumber.setText("7");
                holder.chatDialogVoteImage.setImageResource(R.drawable.img_medal_gold);
                break;
            case 4:
                holder.chatDialogVoteNumber.setText("6");
                holder.chatDialogVoteImage.setImageResource(R.drawable.img_medal_silver);
                break;
            case 5:
                holder.chatDialogVoteNumber.setText("5");
                holder.chatDialogVoteImage.setImageResource(R.drawable.img_medal_bronze);
                break;
            case 6:
                holder.chatDialogVoteNumber.setText("4");
                holder.chatDialogVoteImage.setImageResource(R.drawable.img_emoti_angry);
                break;
            case 7:
                holder.chatDialogVoteNumber.setText("3");
                holder.chatDialogVoteImage.setImageResource(R.drawable.img_emoti_sad);
                break;
            case 8:
                holder.chatDialogVoteNumber.setText("2");
                holder.chatDialogVoteImage.setImageResource(R.drawable.img_emoti_crying);
                break;
            case 9:
                holder.chatDialogVoteNumber.setText("1");
                holder.chatDialogVoteImage.setImageResource(R.drawable.img_emoti_vomiting);
                break;
            case 10:
                holder.chatDialogVoteNumber.setText("0");
                holder.chatDialogVoteImage.setImageResource(R.drawable.img_emoti_caca_multicolor);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return 11;
    }



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
