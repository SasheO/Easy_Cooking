package com.example.groceriesmanager.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceriesmanager.Activities.AddFoodItemActivity;
import com.example.groceriesmanager.Adapters.FoodListAdapter;
import com.example.groceriesmanager.Models.FoodItem;
import com.example.groceriesmanager.Models.User;
import com.example.groceriesmanager.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class GroceryListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    RecyclerView rvGroceryList;
    ImageButton ibAddGroceryItem;
    List<FoodItem> groceryList;
    private static final String TAG = "GroceryListFragment";
    public FoodListAdapter adapter;
    private static final String type = "grocery";

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
        groceryList = new ArrayList<>();
        queryGroceryList();
        adapter = new FoodListAdapter(getContext(), groceryList, type);

        // set the adapter on the recycler view
        rvGroceryList.setAdapter(adapter);
        // set the layout manager on the recycler view
        rvGroceryList.setLayoutManager(new LinearLayoutManager(getActivity()));


        ibAddGroceryItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AddFoodItemActivity.class);
                // todo: put extra that indicates that this is a new grocery list item
                intent.putExtra("type", type);
                intent.putExtra("process", "new");
                startActivity(intent);
//                addFoodItemActivityResultLauncher.launch(intent);
            }
        });
    }

//    ActivityResultLauncher<Intent> addFoodItemActivityResultLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    // If the user comes back to this activity from EditActivity
//                    // with no error or cancellation
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        Intent data_passed_back = result.getData();
//                        // Get the data passed from EditActivity
//                        // String editedString = data.getExtras().getString("newString");
//                        // todo: get food item from grocery list and pass it in
//                        FoodItem newFoodItem = data_passed_back.getParcelableExtra("newFoodItem");
////                        groceryList.add(0, newFoodItem);
////                        adapter.notifyDataSetChanged();
//                    }
//                }
//            });

    private void queryGroceryList() {
        // specify what type of data we want to query - FoodItem.class
        ParseQuery<FoodItem> query = ParseQuery.getQuery(FoodItem.class);
        // include data where post is current post
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
                }
            }
        });
    }
}