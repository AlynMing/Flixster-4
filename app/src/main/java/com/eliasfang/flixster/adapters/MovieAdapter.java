package com.eliasfang.flixster.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.eliasfang.flixster.DetailActivity;
import com.eliasfang.flixster.MainActivity;
import com.eliasfang.flixster.R;
import com.eliasfang.flixster.models.Movie;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String YOUTUBE_API_KEY = "AIzaSyBVdEmwEQc_D1R0AOJU2bZH_ZGV7cQ7cvI";
    public static final String VIDEOS_URL = "https://api.themoviedb.org/3/movie/%d/videos?api_key=a07e22bc18f5cb106bfe4cc1f83ad8ed";

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

        RelativeLayout container;
        TextView tvTitle;
        TextView tvOverview;
        ImageView ivPoster;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvOverview = itemView.findViewById(R.id.tvOverview);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            container = itemView.findViewById(R.id.container);
        }

        public void bind(final Movie movie) {
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
            Glide.with(context).load(imageUrl).override(342,513).placeholder(R.drawable.placeholder).transform(new RoundedCornersTransformation(30, 10)).into(ivPoster);

            // 1. Register click listener on the whole row
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 2. Navigate to a new activity on tap
                    Intent i = new Intent(context, DetailActivity.class);
                    i.putExtra("movie", Parcels.wrap(movie));
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, tvTitle, "title");
                    context.startActivity(i, options.toBundle());
                }
            });
        }

    }

    public class PopularViewHolder extends RecyclerView.ViewHolder {

        ImageView ivBackdrop;
        RelativeLayout container;
        YouTubePlayerView youTubePlayerView;

        public PopularViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBackdrop = itemView.findViewById(R.id.ivBackdrop);
            container = itemView.findViewById(R.id.container);
            youTubePlayerView = itemView.findViewById(R.id.player);
        }

        public void bind(final Movie movie) {
            Glide.with(context).load(movie.getBackdropPath()).placeholder(R.drawable.placeholder).transform(new RoundedCornersTransformation(30, 10)).into(ivBackdrop);

            // 1. Register click listener on the whole row
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 2. Play video
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.get(String.format(VIDEOS_URL, movie.getMovieId()), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            try {
                                JSONArray results = json.jsonObject.getJSONArray("results");
                                if (results.length() == 0) {
                                    return;
                                }
                                String youtubeKey = results.getJSONObject(0).getString("key");
                                Log.d("DetailActivity", youtubeKey);
                                initializeYoutube(youtubeKey);
                            } catch (JSONException e) {
                                Log.d("DetailActivity", "Failed to parse JSON", e);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

                        }
                    });
                }
            });
        }

        private void initializeYoutube(final String youtubeKey) {
            youTubePlayerView.initialize(YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                    Log.d("DetailActivity", "onInitializeSuccess");
                    youTubePlayer.loadVideo(youtubeKey);
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                    Log.d("DetailActivity", "onInitializeFailure");
                }
            });
        }

    }
}
