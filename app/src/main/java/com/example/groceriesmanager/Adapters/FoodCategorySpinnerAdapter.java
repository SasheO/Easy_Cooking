package com.example.groceriesmanager.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.groceriesmanager.R;

import java.util.Hashtable;
import java.util.List;

public class FoodCategorySpinnerAdapter extends ArrayAdapter<String> {

    Context context;
    List<String> foodCategoryList;
    private static final String TAG = "FoodCategorySpinnerAdapter";
    private static Hashtable textToDrawableName = new Hashtable();

    // Constructor accepts Context (from MainActivity) and a list of state abbreviations
    public FoodCategorySpinnerAdapter(Context context, List<String> foodCategoryList) {
        super(context, R.layout.my_selected_item, foodCategoryList);
        this.context = context;
        this.foodCategoryList = foodCategoryList;
        textToDrawableName.put("other", "food_item_holder");
        textToDrawableName.put("--no selection--", "food_item_holder");
        textToDrawableName.put("fresh fruits/veggies", "fresh_fruit");
        textToDrawableName.put("canned food", "canned_food");
        // todo: edit these to the actual picture
        textToDrawableName.put("grains/legumes", "food_item_holder");
        textToDrawableName.put("protein", "food_item_holder");
        textToDrawableName.put("beverages/dairy", "food_item_holder");
        textToDrawableName.put("spices/sauces", "food_item_holder");
    }

    // Override these methods and instead return our custom view (with image and text)
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    // Function to return our custom View (View with an image and text)
    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.spinner_item_food_category, parent, false);

        TextView tvFoodCategory = row.findViewById(R.id.tvFoodCategory);
        ImageView ivFoodCategoryImage = row.findViewById(R.id.ivFoodCategoryImage);

        // Get flag image from drawables folder
        Resources res = context.getResources();
        String drawableName = (String) textToDrawableName.get(foodCategoryList.get(position)); // the food category names are mapped to the drawable title in textToDrawableName hashmap
        int resId = res.getIdentifier(drawableName, "drawable", context.getPackageName());
        Drawable drawable = res.getDrawable(resId);

        tvFoodCategory.setText(foodCategoryList.get(position));
        if (foodCategoryList.get(position)=="--no selection--"){
            ivFoodCategoryImage.setVisibility(View.GONE);
        }
        else{
            Glide.with(context).load(drawable).transform(new CircleCrop()).into(ivFoodCategoryImage);
        }
        return row;
    }
}
