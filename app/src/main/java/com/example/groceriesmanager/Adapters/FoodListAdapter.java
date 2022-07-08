package com.example.groceriesmanager.Adapters;

import android.content.Context;
import android.content.Intent;
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

import com.example.groceriesmanager.Activities.EditFoodItemActivity;
import com.example.groceriesmanager.Activities.MainActivity;
import com.example.groceriesmanager.Gestures.OnSwipeTouchListener;
import com.example.groceriesmanager.Models.FoodItem;
import com.example.groceriesmanager.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FoodListAdapter extends
        RecyclerView.Adapter<FoodListAdapter.ViewHolder>{
    protected List<FoodItem> foodItemList;
    MainActivity context;
    String type;
    public static final String TAG = "FoodListAdapter";
    public static final String PANTRY = "pantry";
    public static final String GROCERY = "grocery";
    public List<FoodItem> selected = new ArrayList<>();

    // constructor to set context
    public FoodListAdapter(Context context, List<FoodItem> foodItemList, String type) {
        this.context = (MainActivity) context;
        this.foodItemList = foodItemList;
        this.type = type;
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
            tvFoodItemName = (TextView) itemView.findViewById(R.id.tvFoodItemName);
            tvFoodItemQty = (TextView) itemView.findViewById(R.id.tvFoodItemQty);
            tvFoodItemMeasure = (TextView) itemView.findViewById(R.id.tvFoodItemMeasure);
            ivFoodItemPic = (ImageView) itemView.findViewById(R.id.ivRecipeImage);
            ibFoodItemSwitchList = (ImageButton) itemView.findViewById(R.id.ibFoodItemSwitchList);
            ibFoodItemDelete = (ImageButton) itemView.findViewById(R.id.ibFoodItemDelete);
            cvFoodItem = (CardView) itemView.findViewById(R.id.cvFoodItem);
            rlFoodItem = (RelativeLayout) itemView.findViewById(R.id.rlFoodItem);
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
            // todo: glide the food category picture if there is one
//            Glide.with(context)
//                    .load(foodItem.getPic().getUrl()).transform(new CircleCrop())
//                    .into(ivFoodItemPic);


            cvFoodItem.setOnTouchListener(new OnSwipeTouchListener(context) {
                @Override
                public void onClick() {
                    super.onClick();
                    // your on click here
                    Intent intent = new Intent(context, EditFoodItemActivity.class);
                    intent.putExtra("process", "edit");
                    intent.putExtra("foodItem", foodItem);
                    context.startActivity(intent);
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
                // todo: reimplement switch list and delete here

                @Override
                public void onSwipeLeft() {
                    super.onSwipeLeft();
                    // your swipe left here.
                    if (Objects.equals(foodItem.getType(), "pantry")){
                        switchFoodItemList(foodItem, itemView);
                    }

                }
                @Override
                public void onSwipeRight() {
                    super.onSwipeRight();
                    // your swipe right here.
                    if (Objects.equals(foodItem.getType(), "grocery")){
                        switchFoodItemList(foodItem, itemView);
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

    public void switchFoodItemList(FoodItem foodItem, View itemView){
                    foodItem.switchList();
        String new_list_type;
                    if (Objects.equals(type, "grocery")){
                        new_list_type = "pantry";
                    }
                    else{
                        new_list_type = "grocery";
                    }
                    Snackbar.make(itemView, foodItem.getName() + " will be moved to " + new_list_type + " list!", Snackbar.LENGTH_SHORT).addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            // the code here runs while snackbar is being shown
                            foodItemList.remove(foodItem);
                            notifyDataSetChanged();
                        }
                    }).setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            foodItem.switchList();
                        }
                    }).show();
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
