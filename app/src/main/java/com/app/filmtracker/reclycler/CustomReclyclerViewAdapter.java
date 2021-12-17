package com.app.filmtracker.reclycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.filmtracker.R;

import java.util.List;

public class CustomReclyclerViewAdapter extends RecyclerView.Adapter<CustomReclyclerViewAdapter.ViewHolder>{
    //TODO: DOCUMENTACION https://developer.android.com/guide/topics/ui/layout/recyclerview
    //TODO: Ejemplo en stackOverflow: https://stackoverflow.com/questions/40587168/simple-android-grid-example-using-recyclerview-with-gridlayoutmanager-like-the


    private List<Object> data;
    private LayoutInflater mInflater;
    //private ItemClickListener mClickListener;

    //Dynamic load from

    public CustomReclyclerViewAdapter(Context context, List<Object> data) {
        this.mInflater = LayoutInflater.from(context);
        this.data = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.film_card_view, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //holder.myTextView.setText(this.data.get(position).toString());
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return data.size();
    }


    // stores and recycles views as they are scrolled off screen
    public static class ViewHolder extends RecyclerView.ViewHolder  { //implements View.OnClickListener
        ImageView image;
        TextView title;
        TextView subtitle;
        TextView description;
        Button btnTrailer;
        ImageButton btnLike;
        ImageButton btnShare;

        ViewHolder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.filmCardImage);
            title = itemView.findViewById(R.id.filmCardTitle);
            subtitle = itemView.findViewById(R.id.filmCardSubtitle);
            //description = itemView.findViewById(R.id.filmCardDescription);
            btnTrailer = itemView.findViewById(R.id.filmCardButtonTrailer);
            //btnLike = itemView.findViewById(R.id.filmCardButtonLike);
            //btnShare = itemView.findViewById(R.id.filmCardButtonShare);

        }

        /*@Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }*/
    }

    /*private fetchFilmsTMDB(){

    }*/

    // convenience method for getting data at click position
    /*Object getItem(int id) {
        return data.get(id);
    }*/



    // allows clicks events to be caught
    /*void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }*/

    // parent activity will implement this method to respond to click events
    /*public interface ItemClickListener {
        void onItemClick(View view, int position);
    }*/
}
