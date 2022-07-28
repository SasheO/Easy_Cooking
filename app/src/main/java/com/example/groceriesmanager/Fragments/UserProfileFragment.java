package com.example.groceriesmanager.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceriesmanager.Activities.AccountSettingsActivity;
import com.example.groceriesmanager.Activities.EditRecipeActivity;
import com.example.groceriesmanager.Activities.LoginActivity;
import com.example.groceriesmanager.Adapters.RecipeSearchAdapter;
import com.example.groceriesmanager.Adapters.SavedRecipeAdapter;
import com.example.groceriesmanager.Adapters.VideoSearchAdapter;
import com.example.groceriesmanager.Models.Recipe;
import com.example.groceriesmanager.Models.Video;
import com.example.groceriesmanager.R;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UserProfileFragment extends Fragment {
    TextView tvProfileUsername;
    RelativeLayout rlMyRecipes;
    RelativeLayout rlSavedRecipes;
    RelativeLayout rlSavedVideos;
    RecyclerView rvMyRecipes;
    RecyclerView rvSavedRecipes;
    RecyclerView rvSavedVideos;
    ImageButton ibExpandMyRecipes;
    ImageButton ibCreateNewRecipe;
    ImageButton ibExpandSavedRecipes;
    ImageButton ibExpandSavedVideos;
    Spinner spinnerExpandSettings;
    public List<Recipe> savedRecipes;
    public List<Recipe> userRecipes;
    public List<Video> savedVideos;
    private static final String TAG = "UserProfileFragment";
    public SavedRecipeAdapter savedRecipeAdapter;
    public SavedRecipeAdapter userRecipeAdapter;
    public VideoSearchAdapter videoRecipeAdapter;

    // required empty constructor
    public UserProfileFragment() {}

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_user_profile, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);
        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        rlMyRecipes = view.findViewById(R.id.rlMyRecipes);
        rlSavedRecipes = view.findViewById(R.id.rlSavedRecipes);
        rlSavedVideos = view.findViewById(R.id.rlSavedVideos);
        rvMyRecipes = view.findViewById(R.id.rvMyRecipes);
        rvSavedRecipes = view.findViewById(R.id.rvSavedRecipes);
        rvSavedVideos = view.findViewById(R.id.rvSavedVideos);
        ibExpandSavedVideos = view.findViewById(R.id.ibExpandSavedVideos);
        ibExpandSavedRecipes = view.findViewById(R.id.ibExpandSavedRecipes);
        ibExpandMyRecipes = view.findViewById(R.id.ibExpandMyRecipes);
        ibCreateNewRecipe = view.findViewById(R.id.ibCreateNewRecipe);
        spinnerExpandSettings = view.findViewById(R.id.spinnerExpandSettings);

        tvProfileUsername.setText(ParseUser.getCurrentUser().getUsername());

        savedRecipes = new ArrayList<>();
        userRecipes = new ArrayList<>();
        savedVideos = new ArrayList<>();

        queryRecipes("saved");
        queryRecipes("user");
        queryVideos();

        savedRecipeAdapter = new SavedRecipeAdapter(getContext(), savedRecipes, "saved");
        userRecipeAdapter = new SavedRecipeAdapter(getContext(), userRecipes, "user");
        videoRecipeAdapter = new VideoSearchAdapter(getContext(), savedVideos, null);

        // spinner adapter for account dropdown
        List<String> settingsList = Arrays.asList(getContext().getResources().getStringArray((R.array.user_profile_settings)));
        ArrayAdapter<CharSequence> settingsAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.user_profile_settings, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
            settingsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerExpandSettings.setAdapter(settingsAdapter);
        spinnerExpandSettings.setSelection(0);

        // set the adapter on the recycler view
        rvSavedRecipes.setAdapter(savedRecipeAdapter);
        // set the layout manager on the recycler view
        rvSavedRecipes.setLayoutManager(new LinearLayoutManager(getActivity()));// set the adapter on the recycler view
        // set the adapter on the recycler view
        rvMyRecipes.setAdapter(userRecipeAdapter);
        // set the layout manager on the recycler view
        rvMyRecipes.setLayoutManager(new LinearLayoutManager(getActivity()));
        // set the adapter on the recycler view
        rvSavedVideos.setAdapter(videoRecipeAdapter);
        // set the layout manager on the recycler view
        rvSavedVideos.setLayoutManager(new LinearLayoutManager(getActivity()));


        ibCreateNewRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EditRecipeActivity.class);
                intent.putExtra("process", "new");
                editActivityResultLauncher.launch(intent);
            }
        });

        spinnerExpandSettings.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = spinnerExpandSettings.getItemAtPosition(position).toString();
                if (Objects.equals(selection, "Log Out")){
                    ParseUser.logOutInBackground(new LogOutCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e != null) {
                                Log.e(TAG, "Error signing out", e);
                                Toast.makeText(getContext(), "Error signing out", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Log.i(TAG, "Sign out successful");
                            goToLoginActivity();
                            Toast.makeText(getContext(), "Signed out", Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                        }
                    });
                }
                else if (Objects.equals(selection, "Account Settings")){
                    Intent intent;
                    intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    startActivity(intent);
                    spinnerExpandSettings.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        rlMyRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri;
                if (rvMyRecipes.getVisibility() == View.GONE){
                    rvMyRecipes.setVisibility(View.VISIBLE);
                    uri = "@drawable/collapse_arrow";
                }
                else{
                    rvMyRecipes.setVisibility(View.GONE);
                    uri = "@drawable/expand_arrow";
                }
                // set image resource
                changeExpandCollapseImageButton(uri, ibExpandMyRecipes);
            }
        });
        rlSavedRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri;
                if (rvSavedRecipes.getVisibility() == View.GONE){
                    rvSavedRecipes.setVisibility(View.VISIBLE);
                    uri = "@drawable/collapse_arrow";
                }
                else{
                    rvSavedRecipes.setVisibility(View.GONE);
                    uri = "@drawable/expand_arrow";
                }
                // set image resource
                changeExpandCollapseImageButton(uri, ibExpandSavedRecipes);
            }
        });
        rlSavedVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri;
                if (rvSavedVideos.getVisibility() == View.GONE){
                    rvSavedVideos.setVisibility(View.VISIBLE);
                    uri = "@drawable/collapse_arrow";
                }
                else {
                    rvSavedVideos.setVisibility(View.GONE);
                    uri = "@drawable/expand_arrow";
                }
                // set image resource
                changeExpandCollapseImageButton(uri, ibExpandSavedVideos);
            }
        });


    }

    public void changeExpandCollapseImageButton(String uri, ImageButton imageButton){
        int imageResource = getResources().getIdentifier(uri, null, getContext().getPackageName());
        Drawable res = getResources().getDrawable(imageResource);
        imageButton.setImageDrawable(res);
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }

    public void queryRecipes(String type) {
        // specify what type of data we want to query - Recipe.class
        ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
        // include data where recipe is given type and was saved/created by current user
        query.whereEqualTo("type", type);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        // necessary to include non-primitive types
        query.include("user");
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Recipe>() {
            @Override
            public void done(List<Recipe> objects, ParseException e) {
                if (e!=null){
                    Log.e(TAG, "error retrieving saved recipes: " + e.toString());
                }
                else{
                    if (Objects.equals(type, "saved")){
                        savedRecipeAdapter.clear();
                        savedRecipes.addAll(objects);
                        savedRecipeAdapter.notifyDataSetChanged();
                    }
                    else {
                        userRecipeAdapter.clear();
                        userRecipes.addAll(objects);
                        userRecipeAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

    }

    public void queryVideos() {
        // specify what type of data we want to query - Recipe.class
        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
        // include data where video was saved by current user
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        // necessary to include non-primitive types
        query.include("user");
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Video>() {
            @Override
            public void done(List<Video> objects, ParseException e) {
                if (e!=null){
                    Log.e(TAG, "error retrieving saved videos: " + e.toString());
                }
                else{
                    savedVideos.addAll(objects);
                    videoRecipeAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    // to enable us get recipe from edit recipe activity
    public ActivityResultLauncher<Intent> editActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // If the user comes back to this activity from EditActivity
                    // with no error or cancellation
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        /*
                        Get the data passed from EditRecipeActivity or RecipeDetailsActivity
                        process: edit, new or delete depending on what is being done to the recipe
                        type: recipe or video depending on what type of item is being edited
                         */
                        String process = data.getExtras().getString("process");
                        Log.i(TAG, "process: " + process);
                        String type = data.getExtras().getString("type");
                        Log.i(TAG, "type: " + type);
                        Recipe recipe = data.getParcelableExtra("recipe");

                        if (Objects.equals(type, "recipe")){
                            if (Objects.equals(process, "new")){ // if creating new recipe
                                userRecipes.add(0, recipe); // add recipe to recycler view
                                userRecipeAdapter.notifyDataSetChanged();
                            }
                            else if (Objects.equals(process, "edit")) { // if user is editing recipe
                                for (int i=0; i<userRecipes.size(); i++){
                                    if (recipe.hasSameId(userRecipes.get(i))){
                                        userRecipes.set(i, recipe); // update the recipe in the recycler view
                                        userRecipeAdapter.notifyDataSetChanged();
                                        break;
                                    }
                                }
                            }
                            else if (Objects.equals(process, "delete")){ // if user is deleting recipe
                                if (Objects.equals(recipe.getType(), "user")){
                                    for (int i=0; i<userRecipes.size(); i++){
                                        if (recipe.hasSameId(userRecipes.get(i))){
                                            userRecipes.remove(i); // update the recipe in the recycler view
                                            userRecipeAdapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                }
                                else{
                                    for (int i=0; i<savedRecipes.size(); i++){
                                        if (recipe.hasSameId(userRecipes.get(i))){
                                            savedRecipes.remove(i); // update the recipe in the recycler view
                                            savedRecipeAdapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                    savedRecipeAdapter.notifyDataSetChanged();
                                }
                            }
                        }

                    }
                }
            });

}
