package com.example.groceriesmanager.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.groceriesmanager.Activities.MainActivity;
import com.example.groceriesmanager.Models.FoodItem;
import com.example.groceriesmanager.Models.Recipe;
import com.example.groceriesmanager.R;

import java.util.List;

public class RecipeAdapter extends
        RecyclerView.Adapter<RecipeAdapter.ViewHolder>{
    private List<Recipe> recipeList;
    MainActivity context;
    public static final String TAG = "RecipeAdapter";

    // constructor to set context
    public RecipeAdapter(Context context, List<Recipe> recipeList) {
        this.context = (MainActivity) context;
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View recipeItemView = inflater.inflate(R.layout.item_recipe, parent, false);

        // Return a new holder instance
        RecipeAdapter.ViewHolder viewHolder = new ViewHolder(recipeItemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        holder.bind(recipe);

        holder.itemView.setClickable(true);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView tvRecipeTitle;
        public TextView tvRecipeIngredientLines;
        public TextView tvOpenRecipeLink;
        public TextView tvRecipeFilters;
        public ImageButton ibOpenRecipeLink;
        public ImageView ivRecipeImage;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            itemView.setOnClickListener(this);
            tvRecipeTitle = (TextView) itemView.findViewById(R.id.tvRecipeTitle);
            tvRecipeIngredientLines = (TextView) itemView.findViewById(R.id.tvRecipeIngredientLines);
            tvOpenRecipeLink = (TextView) itemView.findViewById(R.id.tvOpenRecipeLink);
            tvRecipeFilters = (TextView) itemView.findViewById(R.id.tvRecipeFilters);
            ibOpenRecipeLink = (ImageButton) itemView.findViewById(R.id.ibOpenRecipeLink);
            ivRecipeImage = (ImageView) itemView.findViewById(R.id.ivRecipeImage);
        }

        public void bind(Recipe recipe) {
            tvRecipeTitle.setText(recipe.getTitle());
            Glide.with(context)
                    .load(recipe.getImage_url())
                    .placeholder(R.drawable.vegetables)
                    .error(R.drawable.vegetables)
                    .into(ivRecipeImage);

            // todo: convert recipe lines from recipe from string array to a string that can be displayed in

            // todo: what happens when user clicks open recipe link
            tvOpenRecipeLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            // todo: what happens when user clicks open recipe link
            ibOpenRecipeLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        @Override
        public void onClick(View v) {
            // todo: implement what happens when user clicks on an item
        }
    }
}