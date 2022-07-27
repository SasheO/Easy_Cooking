package com.example.groceriesmanager.Fragments;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceriesmanager.Activities.MainActivity;
import com.example.groceriesmanager.Adapters.FoodListAdapter;
import com.example.groceriesmanager.Activities.EditFoodItemActivity;
import com.example.groceriesmanager.Models.FoodItem;
import com.example.groceriesmanager.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PantryListFragment extends Fragment {
    public List<FoodItem> pantryList;
    private static final String TAG = "PantryListFragment";
    public FoodListAdapter adapter;
    private static final String type = "pantry";
    private MainActivity currentActivity;

    // required empty constructor
    public PantryListFragment() {}
    public PantryListFragment(MainActivity currentActivity) {this.currentActivity = currentActivity;}

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_pantry_list, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        Spinner spinnerSortAccordingTo = view.findViewById(R.id.spinnerSortAccordingTo);
        RecyclerView  rvPantryList = view.findViewById(R.id.rvPantryList);
        ImageButton ibAddPantryItem = view.findViewById(R.id.ibAddPantryItem);
        FloatingActionButton fabtnSuggestRecipes = view.findViewById(R.id.fabtnSuggestRecipes);

        // populate pantry list
        pantryList = new ArrayList<>();
        queryPantryList();

        // create notification channel
        createNotificationChannel();

        adapter = new FoodListAdapter(currentActivity, pantryList, type);
        // set the adapter on the recycler view
        rvPantryList.setAdapter(adapter);
        // set the layout manager on the recycler view
        rvPantryList.setLayoutManager(new LinearLayoutManager(getActivity()));

        // spinner adapter to choose how to sort panty list
        ArrayAdapter<CharSequence> sortPantryListAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.pantry_list_sort_options, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        sortPantryListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerSortAccordingTo.setAdapter(sortPantryListAdapter);
        spinnerSortAccordingTo.setSelection(0);


        spinnerSortAccordingTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = spinnerSortAccordingTo.getItemAtPosition(position).toString();
                if (Objects.equals(selection, "default")){
                    queryPantryList();
                }
                if (Objects.equals(selection, "category")){
                    sortPantryAccordingToCategory();
                    // dataset not changed within sortPantryAccordingToCategory() because it is used in suggestRecipes() and we do not want the list to visually change there
                    adapter.notifyDataSetChanged();
                }
                if (Objects.equals(selection, "expiry date")){
                    sortPantryAccordingToExpiryDate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        fabtnSuggestRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suggestRecipes();
            }
        });


        ibAddPantryItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pantryList.size() >= 30){
                    Toast.makeText(currentActivity, "Pantry list at maximum capacity. Delete old items to add new.", Toast.LENGTH_LONG).show();
                }
                else{
                    Intent intent = new Intent(currentActivity, EditFoodItemActivity.class);
                    intent.putExtra("type", type);
                    intent.putExtra("process", "new");
                    editActivityResultLauncher.launch(intent);
                }
            }
        });
    }

    private void suggestRecipes() {

        FragmentTransaction ft = currentActivity.getSupportFragmentManager().beginTransaction();
        String userQuery = "";



        // if the user did not manually select what recipe ingredients to search, do smart search
        if(adapter.selected.size() == 0){

            // need at least two elements for smart search
            if (pantryList.size()<2){
                Toast.makeText(currentActivity, "not enough for random search", Toast.LENGTH_SHORT).show();
                return;
            }

        userQuery = getSmartSearchQuery();

        }
        else{
            for (FoodItem foodItem: adapter.selected){
                userQuery = userQuery + foodItem.getName() + " ";
            }
        }
        RecipeSearchFragment fragmentDemo = RecipeSearchFragment.newInstance(userQuery);
        ft.replace(R.id.frameLayout, fragmentDemo);
        ft.commit();
    }

    private String getSmartSearchQuery() {
        /*
        main for suggestions are grains/legumes, protein, veggies, canned food in that order
        order pantry list according to importance
        */
        String userQuery = "";

        sortPantryAccordingToCategory();

        // get random element from first third which is likely to be a prioritized category
        int index = (int)(Math.random() * pantryList.size()/3);
        userQuery = pantryList.get(index).getName();
        pantryList.remove(index);
        index = (int)(Math.random() * pantryList.size());
        userQuery = userQuery + " " + pantryList.get(index).getName();
        return userQuery;
    }

    private void sortPantryAccordingToCategory(){
        /*
        (pantry list max size is 30,)
        this shuffles list (to somewhat randomize searches) then repeatedly moves items to the front to rearrange them in order of relevance when searching
        */
        List<FoodItem> organizedPantryList = new ArrayList<>(pantryList);
        Collections.shuffle(organizedPantryList);
        for (FoodItem item: pantryList){
            if (item.getFoodCategory()==null){
                organizedPantryList.remove(organizedPantryList.indexOf(item));
                organizedPantryList.add(0, item);
            }
        }
        // types is ordered in s
        List<String> types = new ArrayList<>(Arrays.asList("other", "beverages/dairy", "fresh fruits", "canned food", "fresh vegetables", "protein", "grains/legumes"));
        for (int i=0; i<types.size(); i++){
            for (FoodItem item: pantryList){
                if (item.getFoodCategory()!=null){
                    if (Objects.equals(item.getFoodCategory(), types.get(i))){
                        organizedPantryList.remove(organizedPantryList.indexOf(item));
                        organizedPantryList.add(0, item);
                    }
                }
            }
        }

        pantryList.clear();
        pantryList.addAll(organizedPantryList);
    }

    private void sortPantryAccordingToExpiryDate() {
        // specify what type of data we want to query - FoodItem.class
        ParseQuery<FoodItem> query = ParseQuery.getQuery(FoodItem.class);
        // include data which matches given requirements
        query.whereEqualTo("type", type);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        // necessary to include non-primitive types
        query.include("user");
        // order posts by creation date (newest first)
        query.addDescendingOrder("expiryDate");
        query.findInBackground(new FindCallback<FoodItem>() {
            @Override
            public void done(List<FoodItem> objects, ParseException e) {
                if (e!=null){
                    Log.e(TAG, "error retrieving grocery list: " + e.toString());
                }
                else{
                    adapter.clear();
                    // to order the list in order of expiry dates (expired first, yet to expire next, then those with no expiry dates
                    Collections.reverse(objects);
                    pantryList.addAll(objects);
                    // doing this to avoid simultaneous modification error with ArrayList type
                    for (int i=0;i<objects.size(); i++){
                        if (objects.get(i).getExpiryDate() == null){
                            pantryList.remove(objects.get(i));
                            pantryList.add(objects.get(i));
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void queryPantryList() {
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
                    pantryList.addAll(objects);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

     private void createNotificationChannel(){
        // for sdk >= 26, a notification channel must be created to see notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "notifyExpiryDateChannel";
            String description = "Channel for expiring foods reminder";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyExpiryDate", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
     }

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
                        FoodItem foodItem = data.getParcelableExtra("fooditem");

                        if (Objects.equals(process, "new")){ // if creating new food item
                            pantryList.add(0, foodItem); // add it to recycler view
                            adapter.notifyDataSetChanged();
                        }
                        else { // if editing a food item
                            for (int i=0; i<pantryList.size(); i++){
                                if (foodItem.hasSameId(pantryList.get(i))){
                                    pantryList.set(i, foodItem); // update the food item in the recycler view
                                    adapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                        }
                    }
                }
            });
}
