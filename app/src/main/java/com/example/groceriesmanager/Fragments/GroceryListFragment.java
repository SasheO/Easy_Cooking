package com.example.groceriesmanager.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.groceriesmanager.Activities.EditFoodItemActivity;
import com.example.groceriesmanager.Adapters.FoodListAdapter;
import com.example.groceriesmanager.Models.FoodItem;
import com.example.groceriesmanager.Models.Recipe;
import com.example.groceriesmanager.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroceryListFragment extends Fragment {
    RecyclerView rvGroceryList;
    ImageButton ibAddGroceryItem;
    List<FoodItem> groceryList;
    private static final String TAG = "GroceryListFragment";
    public FoodListAdapter adapter;
    private static final String type = "grocery";
    private SwipeRefreshLayout swipeContainer;

    // required empty constructor
    public GroceryListFragment() {}

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_grocery_list, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        rvGroceryList = (RecyclerView) view.findViewById(R.id.rvGroceryList);
        ibAddGroceryItem = view.findViewById(R.id.ibAddGroceryItem);
        swipeContainer = view.findViewById(R.id.swipeContainer);
        groceryList = new ArrayList<>();
        queryGroceryList();
        ImageButton ibHowToUse = view.findViewById(R.id.ibHowToUse);
        adapter = new FoodListAdapter(getContext(), groceryList, type);

        // set the adapter on the recycler view
        rvGroceryList.setAdapter(adapter);
        // set the layout manager on the recycler view
        rvGroceryList.setLayoutManager(new LinearLayoutManager(getActivity()));


        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                queryGroceryList();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        ibHowToUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo: open how to use dialog
            }
        });

        ibAddGroceryItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groceryList.size() >= 30){
                    Toast.makeText(getContext(), "Grocery list at maximum capacity. Delete old items to add new.", Toast.LENGTH_LONG).show();
                }
                else{
                    Intent intent = new Intent(getContext(), EditFoodItemActivity.class);
                    intent.putExtra("type", type);
                    intent.putExtra("process", "new");
                    editActivityResultLauncher.launch(intent);
                }
            }
        });
    }

    private void queryGroceryList() {
        // specify what type of data we want to query - FoodItem.class
        ParseQuery<FoodItem> query = ParseQuery.getQuery(FoodItem.class);
        // include data which matches given requirements
        query.whereEqualTo("type", type);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        // necessary to include non-primitive types
        query.include("user");
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<FoodItem>() {
            @Override
            public void done(List<FoodItem> objects, ParseException e) {
                if (e!=null){
                    Log.e(TAG, "error retrieving grocery list: " + e.toString());
                }
                else{
                    adapter.clear();
                    groceryList.addAll(objects);
                    adapter.notifyDataSetChanged();
                    if (swipeContainer.isRefreshing()){
                        swipeContainer.setRefreshing(false);
                    }
                }
            }
        });
    }

    // to get result from EditFoodItem activiity and update locally without refreshing
    public ActivityResultLauncher<Intent> editActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // If the user comes back to this activity from EditActivity
                    // with no error or cancellation
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        String process = data.getExtras().getString("process");
                        FoodItem foodItem = data.getParcelableExtra("foodItem");

                        if (Objects.equals(process, "new")){ // if creating new food item
                            Log.i(TAG, "new item id: " + foodItem.getObjectId());
                            groceryList.add(0, foodItem); // add it to recycler view
                            adapter.notifyDataSetChanged();
                        }
                        else { // if editing a food item
                            Log.i(TAG, "edited item id: " + foodItem.getObjectId());
                            for (int i=0; i<groceryList.size(); i++){
                                if (foodItem.hasSameId(groceryList.get(i))){
                                    Log.i(TAG, "grocery item id: " + groceryList.get(i).getObjectId());
                                    groceryList.set(i, foodItem); // update the food item in the recycler view
                                    adapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                        }
                    }
                }
            });
}