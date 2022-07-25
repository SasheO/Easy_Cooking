package com.example.groceriesmanager.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.groceriesmanager.Activities.MainActivity;
import com.example.groceriesmanager.Activities.VideoPlayerActivity;
import com.example.groceriesmanager.Models.FoodItem;
import com.example.groceriesmanager.Models.Recipe;
import com.example.groceriesmanager.Models.Video;
import com.example.groceriesmanager.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.List;
import java.util.Objects;

public class VideoSearchAdapter extends
        RecyclerView.Adapter<VideoSearchAdapter.ViewHolder>{
    private List<Video> videoList;
    MainActivity context;
    private List<Video> savedVideosList;
    public static final String TAG = "VideoAdapter";

    public VideoSearchAdapter(Context context, List<Video> videoList, List<Video> savedVideosList) {
        this.context = (MainActivity) context;
        this.videoList = videoList;
        this.savedVideosList = savedVideosList;
    }

    @NonNull
    @Override
    public VideoSearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View videoItemView = inflater.inflate(R.layout.item_video_search, parent, false);

        // Return a new holder instance
        VideoSearchAdapter.ViewHolder viewHolder = new VideoSearchAdapter.ViewHolder(videoItemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Video video = videoList.get(position);

        holder.bind(video);

        holder.itemView.setClickable(true);
    }


    @Override
    public int getItemCount() {
        return videoList.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        private TextView tvVideoeTitle;
        private ImageView ivVideoThumbnail;
        private RelativeLayout llVideo;
        private ImageButton ibSaved;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            itemView.setOnClickListener(this);
            tvVideoeTitle = itemView.findViewById(R.id.tvVideoTitle);
            ivVideoThumbnail = itemView.findViewById(R.id.ivVideoThumbnail);
            llVideo = itemView.findViewById(R.id.llVideo);
            ibSaved = itemView.findViewById(R.id.ibSaved2);
        }

        public void bind(Video video) {
            ibSaved.bringToFront(); // this is necessary so that the star shows above the video not behind it
            tvVideoeTitle.setText(video.getTitle());
            Glide.with(context)
                    .load(video.getThumbnail_url())
                    .into(ivVideoThumbnail);

        if (savedVideosList != null){ // saved videos are null when this adapter is used in user profile fragment, so all the videos given are already saved
            if (videoIsSaved(video)){
                ibSaved.setImageResource(android.R.drawable.star_big_on);
            }
            else {
                ibSaved.setImageResource(android.R.drawable.star_big_off);
            }
        }
        else {
            ibSaved.setImageResource(android.R.drawable.star_big_on);
        }


            // when user tries to save/unsave video
            ibSaved.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveOrUnsaveRecipe(video);
                }
            });
            // when user clicks on video this happens
            llVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, VideoPlayerActivity.class);
                    intent.putExtra("video", Parcels.wrap(video));
                    context.startActivity(intent);
                }
            });
        }

        private void saveOrUnsaveRecipe(Video video) {
            if (savedVideosList != null){
                if (videoIsSaved(video)){
                    video.deleteInBackground();
                    for (Video savedVideo: savedVideosList){
                        if (Objects.equals(video.getTitle(), savedVideo.getTitle())){
                            savedVideosList.remove(savedVideo);
                            break;
                        }
                    }
                    ibSaved.setImageResource(android.R.drawable.star_big_off);
                    Toast.makeText(context, "Unsaved!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    // save all the ingredients to the server
                    video.setUser(ParseUser.getCurrentUser());
                    video.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e!= null){
                                Log.e(TAG, "error saving video to server:" + e.toString());
                                Toast.makeText(context, "error saving video", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Log.i(TAG, "video saved successffully");
                                savedVideosList.add(video);
                                Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show();
                                ibSaved.setImageResource(android.R.drawable.star_big_on);
                            }
                        }
                    });
                }
            }
                else { // saved videos are null when this adapter is used in user profile fragment, so all the videos given are already saved
                video.deleteInBackground();
                videoList.remove(video);
                notifyDataSetChanged();
            }

        }

        @Override
        public void onClick(View v) {

        }

    }

    private boolean videoIsSaved(Video video){
        for (Video savedVideo: savedVideosList){
            // check title names since object ids will be different for the same recipe in each search
            if (Objects.equals(video.getVideoID(), savedVideo.getVideoID())){
                return true;
            }
        }
        return false;
    }

    public void clear() {
        videoList.clear();
        notifyDataSetChanged();
    }
}
