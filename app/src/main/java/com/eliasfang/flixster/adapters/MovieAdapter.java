package com.eliasfang.flixster.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eliasfang.flixster.R;
import com.eliasfang.flixster.models.Movie;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<Movie> movies;

    final int NORMAL = 0;
    final int POPULAR = 1;

    public MovieAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("MovieAdapter", "onCreateViewHolder");
        View movieView;
        // Check movie type
        if (viewType == NORMAL) {
            movieView = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
            return new ViewHolder(movieView);
        }
        movieView = LayoutInflater.from(context).inflate(R.layout.item_popular_movie, parent, false);
        return new PopularViewHolder(movieView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.d("MovieAdapter", "onBindViewHolder " + position);
        // Get the movie at the passed in position
        Movie movie = movies.get(position);
        // Bind the movie data into the VH or PVH
        if (getItemViewType(position) == NORMAL) {
            ((ViewHolder)holder).bind(movie);
        } else if (getItemViewType(position) == POPULAR) {
            ((PopularViewHolder)holder).bind(movie);
        }
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return movies.size();
    }

    // Returns the item view type
    public int getItemViewType(int position) {
        Movie movie = movies.get(position);
        if (movie.getVoteAverage() >= 7.5) {
            return POPULAR;
        }
        return NORMAL;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvOverview;
        ImageView ivPoster;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvOverview = itemView.findViewById(R.id.tvOverview);
            ivPoster = itemView.findViewById(R.id.ivPoster);
        }

        public void bind(Movie movie) {
            tvTitle.setText(movie.getTitle());
            tvOverview.setText(movie.getOverview());
            String imageUrl;
            // if phone is in landscape
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                // then imageUrl = backdrop image
                imageUrl = movie.getBackdropPath();
            } else {
                // else imageUrl = poster image
                imageUrl = movie.getPosterPath();
            }

            Glide.with(context).load(imageUrl).override(342,513).placeholder(R.drawable.placeholder).into(ivPoster);
        }

    }

    public class PopularViewHolder extends RecyclerView.ViewHolder {

        ImageView ivBackdrop;

        public PopularViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBackdrop = itemView.findViewById(R.id.ivBackdrop);
        }

        public void bind(Movie movie) {
            Glide.with(context).load(movie.getBackdropPath()).placeholder(R.drawable.placeholder).into(ivBackdrop);
        }

    }
}
