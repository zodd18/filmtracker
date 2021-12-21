package com.app.filmtracker.reclycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.app.filmtracker.R;
import com.app.filmtracker.poo.OnLoadCustomListener;
import com.app.filmtracker.vo.Genre;
import com.app.filmtracker.vo.Movie;
import com.google.gson.reflect.TypeToken;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CustomReclyclerViewAdapter extends RecyclerView.Adapter<CustomReclyclerViewAdapter.ViewHolder>{
    //TODO: DOCUMENTACION https://developer.android.com/guide/topics/ui/layout/recyclerview
    //TODO: Ejemplo en stackOverflow: https://stackoverflow.com/questions/40587168/simple-android-grid-example-using-recyclerview-with-gridlayoutmanager-like-the


    private List<Movie> data;
    private List<Genre> genres;
    private LayoutInflater mInflater;
    //private ItemClickListener mClickListener;

    //Dynamic load using API Rest from TBDb
    private int lastPage;
    private boolean isFetching;
    private static final int VISIBLE_THRESHOLD = 15;
    private OnLoadCustomListener onLoadCustomListener;

    public CustomReclyclerViewAdapter(Context context, List<Genre> genres) {
        this.mInflater = LayoutInflater.from(context);
        this.lastPage = 1;
        this.isFetching = false;
        this.data = new ArrayList<>();
        this.genres = genres;
    }

    // Inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.film_card_view, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if((position+VISIBLE_THRESHOLD)>=data.size() && !isFetching){
            isFetching = true;
            onLoadCustomListener.load();
        } else {
            holder.title.setText(this.data.get(position).getTitle());
            String genresResult = "";
            for(int i : this.data.get(position).getGenre_ids()){
                Genre gi = new Genre(i);
                for(Genre g : genres){
                    if(g.equals(gi)){
                        genresResult+=g.getName()+"\n";
                    }
                }
            }

            holder.subtitle.setText(genresResult);
        }
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    /*public void fetchMovies(){
        String url = "https://api.themoviedb.org/3/discover/movie?api_key=a9e15ccf0b964bbf599fef3ba94ef87b&language=es-ES&sort_by=popularity.desc&include_adult=false&include_video=false&page="+ lastPage +"&with_watch_monetization_types=flatrate";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            lastPage = response.getInt("page") + 1;
                            JSONArray movieJsonList = response.getJSONArray("results");
                            Type listType = new TypeToken<ArrayList<Movie>>(){}.getType();
                            List<Movie> movList = gson.fromJson(movieJsonList.toString(), listType);
                            movieList.addAll(movList);

                            System.out.println(response.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        isFetching = false;
                        CustomReclyclerViewAdapter.this.notifyDataSetChanged();
                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        System.out.println("------Error en el Volley");
                        isFetching = false;
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }*/

    // RecyclerView with data
    public static class ViewHolder extends RecyclerView.ViewHolder  { //implements View.OnClickListener
        private ImageView image;
        private TextView title;
        private TextView subtitle;
        private TextView description;
        private Button btnTrailer;
        private ImageButton btnLike;
        private ImageButton btnShare;

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



    public void setOnLoadCustomListener(OnLoadCustomListener onLoadCustomListener) {
        this.onLoadCustomListener = onLoadCustomListener;
        this.onLoadCustomListener.load();
    }


    public void addNewDataAndNotify(Collection<? extends Movie> collection){
        this.data.addAll(collection);
        this.setFetched();
        this.notifyDataSetChanged();
    }


    public void setFetched() {
        this.isFetching = false;
    }

    public void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }

    public int getLastPage() {
        return lastPage;
    }



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