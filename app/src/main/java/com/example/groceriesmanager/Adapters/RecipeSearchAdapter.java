package com.example.groceriesmanager.Adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.groceriesmanager.Activities.MainActivity;
import com.example.groceriesmanager.Gestures.OnSwipeTouchListener;
import com.example.groceriesmanager.Models.FoodItem;
import com.example.groceriesmanager.Models.Recipe;
import com.example.groceriesmanager.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RecipeSearchAdapter extends
        RecyclerView.Adapter<RecipeSearchAdapter.ViewHolder>{
    private List<Recipe> recipeList;
    private List<Recipe> savedRecipesList;
    MainActivity context;
    public static final String TAG = "RecipeSearchAdapter";
    private List<FoodItem> pantryList;
    private final ParseUser currentUser = ParseUser.getCurrentUser();


    // constructor to set context
    public RecipeSearchAdapter(Context context, List<Recipe> recipeList, List<Recipe> savedRecipesList) {
        this.context = (MainActivity) context;
        this.recipeList = recipeList;
        this.savedRecipesList = savedRecipesList;
        queryPantryList();
    }

    private void queryPantryList() {
        // specify what type of data we want to query - FoodItem.class
        ParseQuery<FoodItem> query = ParseQuery.getQuery(FoodItem.class);
        // include data which matches given requirements
        query.whereEqualTo("type", "pantry");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        // necessary to include non-primitive types
        query.include("user");
        query.findInBackground(new FindCallback<FoodItem>() {
            @Override
            public void done(List<FoodItem> objects, ParseException e) {
                if (e!=null){
                    Log.e(TAG, "error retrieving grocery list: " + e.toString());
                }
                else{
                    pantryList = new ArrayList<>();
                    pantryList.addAll(objects);
                    notifyDataSetChanged();
                }
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View recipeItemView = inflater.inflate(R.layout.item_recipe_search, parent, false);
        RecipeSearchAdapter.ViewHolder viewHolder = new ViewHolder(recipeItemView);
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
        // Your holder should contain a member variable for any view that will be set as you render a row
        private TextView tvRecipeTitle;
        private TextView tvRecipeIngredientLines;
        private TextView tvOpenRecipeLink;
        private TextView tvAddAll;
        public TextView tvFractionGottenIngredients;
//        public TextView tvRecipeFilters;
        private ImageButton ibOpenRecipeLink;
        private ImageView ivRecipeImage;
        private RelativeLayout rlRecipeSearch;
        private ImageButton ibSaved;


        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvAddAll = itemView.findViewById(R.id.tvAddAll);
            tvFractionGottenIngredients = itemView.findViewById(R.id.tvFractionGottenIngredients);
            tvRecipeTitle = itemView.findViewById(R.id.tvRecipeTitle);
            tvRecipeIngredientLines = itemView.findViewById(R.id.tvRecipeIngredientsLabel);
            tvOpenRecipeLink = itemView.findViewById(R.id.tvOpenRecipeLink);
//            tvRecipeFilters = itemView.findViewById(R.id.tvRecipeFilters);
            ibOpenRecipeLink = itemView.findViewById(R.id.ibOpenRecipeLink);
            ivRecipeImage = itemView.findViewById(R.id.ivFoodItemPic);
            rlRecipeSearch = itemView.findViewById(R.id.rlSavedRecipe);
            ibSaved = itemView.findViewById(R.id.ibSaved);
        }

        public void bind(Recipe recipe) {
            tvRecipeTitle.setText(recipe.getTitle());
            tvAddAll.setTextColor(context.getResources().getColor(R.color.dark_blue));
            Glide.with(context)
                    .load(recipe.getImage_url()).error(R.drawable.placeholder).error(R.drawable.placeholder)
                    .into(ivRecipeImage);

            // convert recipe lines from recipe from string array to a string that can be displayed in text box
            String recipeIngredientStr = "";
            int totalIngredients = recipe.getIngredients().size();
            int gottenIngredients = 0;

            // this list initially has all ingredients
            List<FoodItem> addAllList = new ArrayList<>();
            addAllList.addAll(recipe.getIngredients());

            for (FoodItem ingredient: recipe.getIngredients()){
                String quantity = ingredient.getQuantity();
                String measure = ingredient.getMeasure();
                String name = ingredient.getName();
                if (quantity!=null){
                    recipeIngredientStr = recipeIngredientStr + quantity + " " + measure + " " + name + "\r\n";
                }
                else{
                    recipeIngredientStr = recipeIngredientStr + name + "\r\n";
                }

                if (pantryList!=null){
                    // check if ingredient with same name in pantry
                    for (FoodItem pantryItem: pantryList){
                        // todo: when you get lemmatizer working, use lemmatizer instead of substring match
                        if (Objects.equals(name.toLowerCase(), pantryItem.getName().toLowerCase()) || name.toLowerCase().contains(pantryItem.getName().toLowerCase())){
                            gottenIngredients ++;
                            addAllList.remove(ingredient); // if already in pantry, remove ingredient from list
                        }
                    }
                }

            }

            tvRecipeIngredientLines.setText(recipeIngredientStr.trim());
            tvFractionGottenIngredients.setText("INGREDIENTS: (" + String.valueOf(gottenIngredients) + " / " + String.valueOf(totalIngredients) + " in pantry)");

            if (recipeIsSaved(recipe)){
                ibSaved.setImageResource(android.R.drawable.star_big_on);
            }
            else {
                ibSaved.setImageResource(android.R.drawable.star_big_off);
            }

            // convert filters from string array to a string that can be displayed in text box
//            List<String> recipe_filters_array = recipe.getFilters();
//            if (recipe_filters_array.size()!=0){
//                String recipe_filters = "Filters: " + recipe_filters_array.get(0);
//                for (int i=0; i<recipe_filters_array.size(); i++){
//                    if (i==0){
//                        continue;
//                    }
//                    recipe_filters = recipe_filters + ", " + recipe_filters_array.get(i);
//                }
//                tvRecipeFilters.setText(recipe_filters);
//            }

            tvOpenRecipeLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // lines below open hyperlink
                    Uri uri = Uri.parse(recipe.getHyperlink_url()); // missing 'http://' will cause crashed
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    context.startActivity(intent);
                }
            });

            ibOpenRecipeLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // lines below open hyperlink
                    Uri uri = Uri.parse(recipe.getHyperlink_url()); // missing 'http://' will cause crashed
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    context.startActivity(intent);
                }
            });

            ibSaved.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveOrUnsaveRecipe(recipe);
                }
            });

            tvAddAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                    * creates a dialog box where you can select items not in pantry list (i.e. items in AddAllList)
                    * allows you to add them to grocery list by saving them to server
                    * */
                    tvAddAll.setTextColor(context.getColor(R.color.dark_grey));
                    Dialog dialog;
                    List<Integer> indexOfIngredientsSelectedArray = new ArrayList();
                    String[] ingredientNamesStringArr = new String[addAllList.size()];
                    for (int i=0; i<addAllList.size(); i++){
                        ingredientNamesStringArr[i] = addAllList.get(i).getName();
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Add to Grocery List: ");
                    builder.setMultiChoiceItems(ingredientNamesStringArr, null, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int selectedItemIndex, boolean isSelected) {
                            if (isSelected) {
                                indexOfIngredientsSelectedArray.add(selectedItemIndex);
                            } else if (indexOfIngredientsSelectedArray.contains(selectedItemIndex)) {
                                indexOfIngredientsSelectedArray.remove(Integer.valueOf(selectedItemIndex));
                            }
                        }
                    }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Log.i(TAG, indexOfIngredientsSelectedArray.toString());
                            for (int index: indexOfIngredientsSelectedArray){
                                addAllList.get(index).setType("grocery");
                                addAllList.get(index).setUser(currentUser);
                                addAllList.get(index).saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e!=null){
                                            Log.e(TAG, "error adding ingredient to grocery list: " + e.toString());
                                        }
                                    }
                                });
                            }
//                            gottenIngredients = gottenIngredients + addAllList.size();
                            // todo: update gotten ingredients bar
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {}
                    });
                    dialog = builder.create();
                    dialog.show();
                }
            });

            rlRecipeSearch.setOnTouchListener(new OnSwipeTouchListener(context) {
                @Override
                public void onClick() {
                    super.onClick();
                    // your on click here

                }
                public void onDoubleClick(){
                    saveOrUnsaveRecipe(recipe);
                }

            });

        }

        @Override
        public void onClick(View v) {}

        private void saveOrUnsaveRecipe(Recipe recipe) {
            if (recipeIsSaved(recipe)){
                // delete all food items associated wiht it
                for (FoodItem ingredient: recipe.getIngredients()){
                    ingredient.deleteFood();
                }
                recipe.deleteInBackground();
                for (Recipe savedRecipe: savedRecipesList){
                    if (Objects.equals(recipe.getTitle(), savedRecipe.getTitle())){
                        savedRecipesList.remove(savedRecipe);
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
                for (FoodItem ingredient: recipe.getIngredients()){
                    ingredient.setUser(currentUser);
                    ingredient.setType("recipe");
                    ingredient.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e!=null){
                                Log.e(TAG, "error saving ingredient: " + e.toString());
                            }
                        }
                    });
                }
                recipe.setUser(ParseUser.getCurrentUser());
                recipe.setType("saved");
                recipe.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e!= null){
                            Log.e(TAG, "error saving recipe to server:" + e.toString());
                            Toast.makeText(context, "error saving recipe", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Log.i(TAG, "recipe saved successffully");
                            savedRecipesList.add(recipe);
                            Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show();
                            ibSaved.setImageResource(android.R.drawable.star_big_on);
                        }
                    }
                });
            }
        }


    }

    private boolean recipeIsSaved(Recipe recipe){
        for (Recipe savedRecipe: savedRecipesList){
            // check links since object ids will be different for the same recipe in each search
            if (Objects.equals(recipe.getHyperlink_url(), savedRecipe.getHyperlink_url())){
                return true;
            }
        }
        return false;
    }


    public void clear() {
        recipeList.clear();
        notifyDataSetChanged();
    }
}
