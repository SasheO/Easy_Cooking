package com.example.groceriesmanager.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.groceriesmanager.Activities.EditFoodItemActivity;
import com.example.groceriesmanager.Activities.MainActivity;
import com.example.groceriesmanager.Gestures.OnSwipeTouchListener;
import com.example.groceriesmanager.Models.FoodItem;
import com.example.groceriesmanager.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.SaveCallback;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FoodListAdapter extends
        RecyclerView.Adapter<FoodListAdapter.ViewHolder>{
    private List<FoodItem> foodItemList;
    MainActivity context;
    String type; // will be either grocery or pantry to differentiate which one that particular instance of the class is being used for
    private static final String TAG = "FoodListAdapter";
    // todo: extract this to values since this hashtable is also found in FoodCategorySpinnerAdapter
    private static Hashtable textToDrawableName = new Hashtable();
    // public because this is accessed in other class
    public List<FoodItem> selected = new ArrayList<>();
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    PrettyTime p = new PrettyTime();

    // constructor to set context
    public FoodListAdapter(Context context, List<FoodItem> foodItemList, String type) {
        this.context = (MainActivity) context;
        this.foodItemList = foodItemList;
        this.type = type;
        textToDrawableName.put("other", "food_item_holder");
        textToDrawableName.put("--no selection--", "food_item_holder");
        textToDrawableName.put("fresh fruits", "fresh_fruit");
        textToDrawableName.put("fresh vegetables", "fresh_fruit");
        textToDrawableName.put("canned food", "canned_food");
        textToDrawableName.put("grains/legumes", "grains_legumes");
        textToDrawableName.put("protein", "protein");
        textToDrawableName.put("beverages/dairy", "dairy");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View foodItemView = inflater.inflate(R.layout.item_food_list, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(foodItemView);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data model based on position
        FoodItem foodItem = foodItemList.get(position);

        holder.bind(foodItem, position);

        holder.itemView.setClickable(true);
    }

    @Override
    public int getItemCount() {
        return foodItemList.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView tvFoodItemName;
        public CardView cvFoodItem;
        public TextView tvFoodItemQty;
        public TextView tvFoodItemMeasure;
        public TextView tvExpiryDate;
        public ImageView ivFoodItemPic;
        public ImageButton ibFoodItemSwitchList;
        public ImageButton ibFoodItemDelete;
        public RelativeLayout rlFoodItem;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            itemView.setOnTouchListener(this);
            tvFoodItemName = itemView.findViewById(R.id.tvFoodItemName);
            tvFoodItemQty = itemView.findViewById(R.id.tvFoodItemQty);
            tvFoodItemMeasure = itemView.findViewById(R.id.tvFoodItemMeasure);
            ivFoodItemPic = itemView.findViewById(R.id.ivFoodItemPic);
            ibFoodItemSwitchList = itemView.findViewById(R.id.ibFoodItemSwitchList);
            ibFoodItemDelete = itemView.findViewById(R.id.ibFoodItemDelete);
            cvFoodItem = itemView.findViewById(R.id.cvFoodItem);
            rlFoodItem = itemView.findViewById(R.id.rlFoodItem);
            tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);
        }

        public void bind(FoodItem foodItem, int position) {
            tvFoodItemName.setText(foodItem.getName());
            String qty = foodItem.getQuantity();

            if (qty == null){
                tvFoodItemQty.setVisibility(View.GONE);
                tvFoodItemMeasure.setVisibility(View.GONE);
            }
            else{
                tvFoodItemQty.setVisibility(View.VISIBLE);
                tvFoodItemQty.setText(foodItem.getQuantity());
                tvFoodItemMeasure.setText(foodItem.getMeasure());
            }

            // for getting the food category picture
            Resources res = context.getResources();
            if (foodItem.getFoodCategory() == null){
                ivFoodItemPic.setVisibility(View.GONE);
            }
            else{
                ivFoodItemPic.setVisibility(View.VISIBLE);
                String drawableName = (String) textToDrawableName.get(foodItem.getFoodCategory()); // the food category names are mapped to the drawable title in textToDrawableName hashmap
                int resId = res.getIdentifier(drawableName, "drawable", context.getPackageName());
                Drawable drawable = res.getDrawable(resId);
                Glide.with(context).load(drawable).transform(new CircleCrop()).into(ivFoodItemPic);
            }

            if (Objects.equals(type, "pantry")){ // show expiry dates for pantry items
                if (foodItem.getExpiryDate()!=null){
                    // todo: populate
                    String expiry_date = p.format(foodItem.getExpiryDate());
                    if (expiry_date.contains("ago")){
                        expiry_date = "expired " + expiry_date;
                    }
                    else {
                        expiry_date = "expires " + expiry_date;
                    }
                    tvExpiryDate.setText(expiry_date);
                }
            }

            cvFoodItem.setOnTouchListener(new OnSwipeTouchListener(context) {
                @Override
                public void onClick() {
                    super.onClick();
                    // your on click here
                    Intent intent = new Intent(context, EditFoodItemActivity.class);
                    intent.putExtra("process", "edit");
                    intent.putExtra("foodItem", foodItem);
                    intent.putExtra("type", type);
                    if (Objects.equals(type, "grocery")){
                        // this function enables the user to see the changes they made to the food item without refreshing the grocery fragment page
                        context.groceryListFragment.editActivityResultLauncher.launch(intent);
                    }
                    else {
                        // this function enables the user to see the changes they made to the food item without refreshing the pantry fragment page
                        context.pantryListFragment.editActivityResultLauncher.launch(intent);
                    }
                }
                public void onLongClick(){
                    if(Objects.equals(foodItem.getType(), "pantry")){
                        if (!selected.contains(foodItem)){
                            selected.add(foodItem);
                            rlFoodItem.setBackgroundColor(context.getResources().getColor(R.color.highlighted_blue));
                        }
                        else{
                            selected.remove(foodItem);
                            rlFoodItem.setBackgroundColor(context.getResources().getColor(R.color.pale_blue));
                        }
                    }
                }

                @Override
                public void onSwipeLeft() {
                    super.onSwipeLeft();
                    // your swipe left here.
                    if (Objects.equals(foodItem.getType(), "pantry")){
                        switchFoodItemList(foodItem, itemView, type);
                    }

                }
                @Override
                public void onSwipeRight() {
                    super.onSwipeRight();
                    // your swipe right here.
                    if (Objects.equals(foodItem.getType(), "grocery")){
                        switchFoodItemList(foodItem, itemView, type);
                    }

                }
            });

            ibFoodItemDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteFoodItem(foodItem, itemView, position);
                }
            });
            }


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }

    }


    public void clear() {
        foodItemList.clear();
        notifyDataSetChanged();
    }

    public void switchFoodItemList(FoodItem foodItem, View itemView, String type){
                    foodItem.switchList();
        foodItemList.remove(foodItem);
        notifyDataSetChanged();
        foodItem.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null){
                    Log.e(TAG, "error switching food item: " + e.toString());
                }
                else{
                    Log.i(TAG, "food item switched lists successfully");
                }
            }
        });
        String new_list_type;
                    if (Objects.equals(type, "grocery")){
                        new_list_type = "pantry";
                    }
                    else{
                        new_list_type = "grocery";
                    }
                    Snackbar.make(itemView, foodItem.getName() + " moved to " + new_list_type + " list!", Snackbar.LENGTH_SHORT).setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            foodItem.switchList();
                            foodItemList.add(foodItem);
                            notifyDataSetChanged();
                            foodItem.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e!=null){
                                        Log.e(TAG, "error switching food item: " + e.toString());
                                    }
                                    else{
                                        Log.i(TAG, "food item switched lists successfully");
                                    }
                                }
                            });
                        }
                    }).show();

                }

    public void deleteFoodItem(FoodItem foodItem, View itemView, Integer position){
                    foodItemList.remove(foodItem);
                    notifyDataSetChanged();
                    Snackbar.make(itemView, foodItem.getName() + " deleted!", Snackbar.LENGTH_SHORT).setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            foodItemList.add(position, foodItem);
                            notifyDataSetChanged();
                        }
                    }).addCallback(new Snackbar.Callback(){
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                // the code in here runs if Snackbar closed on its own i.e. the user does not click UNDO button to restore just deleted item
                                foodItem.deleteFood();
                            }
                        }
                    }).show();

    }

}
