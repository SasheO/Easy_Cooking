package com.example.groceriesmanager.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceriesmanager.Activities.AccountSettingsActivity;
import com.example.groceriesmanager.Adapters.RecipeSearchAdapter;
//import com.example.groceriesmanager.Lemma;
import com.example.groceriesmanager.Models.Recipe;
import com.example.groceriesmanager.Models.User;
import com.example.groceriesmanager.R;
import com.google.android.flexbox.FlexboxLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSTaggerME;

public class RecipeSearchFragment extends Fragment {
    private TextView tvExpandFilters;
    private TextView tvResetFilters;
    private ImageButton ibRecipeSearch;
    private ImageButton ibRecipeSearchClear;
    private EditText etRecipeLookup;
    private FlexboxLayout flexboxFilters;
    private TextView tvNoResultsMessage;
    private TextView tvNextPage;
    private static final String TAG = "RecipeSearchFragment";
    public static List<Recipe> recipeList;
    public static List<Recipe> savedRecipesList;
    public RecipeSearchAdapter adapter;
    String userQuery;
    RecyclerView rvRecipeSearch;
    User currentUser;
    EnumSet<AccountSettingsActivity.dietFiltersEnum> filters;
    InputStream dictLemmatizer = null;
    DictionaryLemmatizer lemmatizer;
    POSTaggerME tagger;
    private String next_page_url;
    OkHttpClient client;

    // required empty constructor
    public RecipeSearchFragment() {}

    public static RecipeSearchFragment newInstance(String userQuery) {
        RecipeSearchFragment fragmentDemo = new RecipeSearchFragment();
        Bundle args = new Bundle();
        args.putString("userQuery", userQuery);
        fragmentDemo.setArguments(args);
        return fragmentDemo;
    }

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_recipe_search, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        tvExpandFilters = view.findViewById(R.id.tvExpandFilters);
        tvResetFilters = view.findViewById(R.id.tvResetFilters);
        flexboxFilters = view.findViewById(R.id.flexboxFilters);
        etRecipeLookup = view.findViewById(R.id.etRecipeLookup);
        ibRecipeSearch = view.findViewById(R.id.ibRecipeSearch);
        ibRecipeSearchClear = view.findViewById(R.id.ibRecipeSearchClear);
        rvRecipeSearch = view.findViewById(R.id.rvRecipeSearch);
        tvNoResultsMessage = view.findViewById(R.id.tvNoResultsMessage);
        tvNextPage = view.findViewById(R.id.tvNextPage);
        tvNoResultsMessage.setVisibility(View.GONE);
        tvNextPage.setVisibility(View.GONE);
        recipeList = new ArrayList<>();
        savedRecipesList = new ArrayList<>();
        adapter = new RecipeSearchAdapter(getContext(), recipeList, savedRecipesList);
        client = new OkHttpClient();

        currentUser = (User) ParseUser.getCurrentUser();
        filters = currentUser.getDietFilters();
        setUserFilters();

        // in case user is opening this from pantryListFragment
        userQuery = getArguments().getString("userQuery", "");

        // get saved recipes which are passed into adapter
        getSavedRecipes();
        adapter.notifyDataSetChanged();

        // instantiate dictionary lemmatizer
        try {
            dictLemmatizer = getContext().getResources().openRawResource(R.raw.en_lemmatizer_dict);
            lemmatizer = new DictionaryLemmatizer(dictLemmatizer);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "dictLemmatizer error: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "lemmatizer error: " + e.toString());
            e.printStackTrace();
        }

        // create tagger for getting parts of speech per word
        // here: https://opennlp.apache.org/docs/2.0.0/manual/opennlp.html#tools.postagger.tagging.cmdline
//        File f = new File("res/raw/en_pos_maxent.bin");
//        try (InputStream modelIn = new FileInputStream(f)) {
//            POSModel model = new POSModel(modelIn);
////            tagger = new POSTaggerME(model);
//        } catch (FileNotFoundException e){
//            Log.e(TAG, e.toString());
//            e.printStackTrace();
//        } catch (IOException e) {
//            Log.e(TAG, e.toString());
//            e.printStackTrace();
//        }

//        try {
//            InputStream tokenModelIn = getContext().getAssets().open("en_pos_maxent.bin");
////             keeps getting 'java.lang.String java.util.Properties.getProperty(java.lang.String)' on a null object reference
//            if (tokenModelIn!=null){
//                // constructor for POSmodel failing
//                POSModel model = new POSModel(tokenModelIn);
//                tagger = new POSTaggerME(model);
//            }
//            else{
//                Log.e(TAG, "tokenModelIn == null");
//            }
//
//        } catch (IOException e) {
//            Log.e(TAG, e.toString());
//            e.printStackTrace();
//        }

        // set the adapter on the recycler view
        rvRecipeSearch.setAdapter(adapter);
        // set the layout manager on the recycler view
        rvRecipeSearch.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (!Objects.equals(userQuery, "")){
            searchRecipes(userQuery);
            etRecipeLookup.setText(userQuery);
        }

        tvExpandFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flexboxFilters.getVisibility()==View.GONE){
                    flexboxFilters.setVisibility(View.VISIBLE);
                    tvExpandFilters.setText("Close filters");
                }
                else {
                    flexboxFilters.setVisibility(View.GONE);
                    tvExpandFilters.setText("Edit filters");}
            }
        });

        tvResetFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filters = currentUser.getDietFilters();
                setUserFilters();
            }
        });

        // when user clicks on the x to clear search results
        ibRecipeSearchClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etRecipeLookup.setText("");
                tvNoResultsMessage.setVisibility(View.GONE);
                adapter.clear();
                tvNextPage.setVisibility(View.GONE);
            }
        });

        ibRecipeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userQuery = etRecipeLookup.getText().toString();
                // check if user has already typed in something
                if (userQuery.replaceAll("\\s", "").length() == 0){
                    Toast.makeText(getContext(), "type in something!", Toast.LENGTH_LONG).show();
                }
                else {
                    userQuery = etRecipeLookup.getText().toString().trim(); // remove trailing and leading spaces
                    searchRecipes(userQuery);
                    tvNextPage.setVisibility(View.VISIBLE);
                }
            }
        });

        tvNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNextPage(next_page_url);
            }
        });

    }

    private void searchRecipes(String query){

        // todo: lemmatization
        // convert string to string array gotten from here: https://stackoverflow.com/questions/4674850/converting-a-sentence-string-to-a-string-array-of-words-in-java
//        String[] queryStringArray = query.split("\\s+");
//        for (int i = 0; i < queryStringArray.length; i++) {
//            // You may want to check for a non-word character before blindly
//            // performing a replacement
//            // It may also be necessary to adjust the character class
//            queryStringArray[i] = queryStringArray[i].replaceAll("[^\\w]", "");
//        }

//        String[] posTags = tagger.tag(queryStringArray);

//        String[] lemmas = lemmatizer.lemmatize(queryStringArray, posTags);

        // check if user has typed in something already
            adapter.clear(); // clear adapter, in case there are already results


            // this builder helps us to creates the request url
            HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.edamam.com/api/recipes/v2").newBuilder();
            urlBuilder.addQueryParameter("q", query);
            urlBuilder.addQueryParameter("type", "public");
            urlBuilder.addQueryParameter("app_id", getResources().getString(R.string.edamam_app_id));
            urlBuilder.addQueryParameter("app_key", getResources().getString(R.string.edamam_app_key));

            // check each textbox in flexbox filters if it is checkes, and add it to the query
            CheckBox v;
            for (int i = 0; i < flexboxFilters.getChildCount(); i++){
                v = (CheckBox) flexboxFilters.getChildAt(i);

                if (v.isChecked()){
                    // add text to url, checkbox text attributes are formatted already how it is in edamam documentation
                    urlBuilder.addQueryParameter("health", v.getText().toString());
                }

            }

            String url = urlBuilder.build().toString();
//            Log.i(TAG, "url: " + url);

            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.i(TAG, "request: " + request.toString());

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "error in executing okhttp call: "+ e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()){
                        String myResponse = response.body().string();
                        try {
                            JSONObject responsejson = new JSONObject(myResponse);
                            next_page_url = responsejson.getJSONObject("_links").getJSONObject("next").getString("href");
//                            Log.i(TAG, "next page:" + next_page_url);
                            JSONArray recipesJSONArray = responsejson.getJSONArray("hits");
                            recipeList.addAll(Recipe.fromJsonArray(recipesJSONArray));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "JSONException: " + e.toString());
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // edit the view here
                                adapter.notifyDataSetChanged();
                                if (recipeList.size()==0){
                                    // if no recipes gotten, show no results message
                                    tvNoResultsMessage.setVisibility(View.VISIBLE);
                                }
                                else{
                                    tvNoResultsMessage.setVisibility(View.GONE);
                                    tvNextPage.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                    else { // response is unsuccessful
                        Log.e(TAG, "response unsuccessful: " + response);
                    }


                }
            });
    }

    private void getNextPage(String next_page){
        Request request = new Request.Builder()
                .url(next_page)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "error in executing okhttp call: "+ e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()){
                    String myResponse = response.body().string();
                    try {
                        JSONObject responsejson = new JSONObject(myResponse);
                        next_page_url = responsejson.getJSONObject("_links").getJSONObject("next").getString("href");
                        Log.i(TAG, "next page:" + next_page_url);
                        JSONArray recipesJSONArray = responsejson.getJSONArray("hits");
                        recipeList.clear();
                        recipeList.addAll(Recipe.fromJsonArray(recipesJSONArray));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "JSONException: " + e.toString());
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // edit the view here
                            rvRecipeSearch.getLayoutManager().smoothScrollToPosition(rvRecipeSearch, null, 0);
                            adapter.notifyDataSetChanged();
                            if (recipeList.size()==0){
                                // if no recipes gotten, show no results message
                                tvNoResultsMessage.setVisibility(View.VISIBLE);
                            }
                            else{
                                tvNoResultsMessage.setVisibility(View.GONE);
                                tvNextPage.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
                else { // response is unsuccessful
                    Log.e(TAG, "response unsuccessful: " + response);
                }
            }
        });

    }

    public void getSavedRecipes(){
            // specify what type of data we want to query - FoodItem.class
            ParseQuery<Recipe> query = ParseQuery.getQuery(Recipe.class);
            // include data where post is current post
            query.whereEqualTo("type", "saved");
            query.whereEqualTo("user", ParseUser.getCurrentUser());
            // necessary to include non-primitive types
            query.include("user");
            // order posts by creation date (newest first)
            query.findInBackground(new FindCallback<Recipe>() {
                @Override
                public void done(List<Recipe> objects, ParseException e) {
                    if (e!=null){
                        Log.e(TAG, "error retrieving grocery list: " + e.toString());
                    }
                    else{
                        savedRecipesList.addAll(objects);
                        adapter.notifyDataSetChanged();
                    }
                }
            });

    }

    private void setUserFilters(){

        CheckBox v;
        if (filters==null){ // if user has not chosen any filters
            // uncheck all boxes
            for (int i = 0; i < flexboxFilters.getChildCount(); i++) {
                v = (CheckBox) flexboxFilters.getChildAt(i);
                v.setChecked(false);
            }
            return;
        }

        String enumStrValue;

        // check every checkbox in flexboxFilters layout if the enum value is in the given user diet filters
        for (int i = 0; i < flexboxFilters.getChildCount(); i++){
            v = (CheckBox) flexboxFilters.getChildAt(i);

            // format the text from lower-case-separated-with-hyphens to FirstLetterCapitalized
            enumStrValue = v.getText().toString().replaceAll("-", " ");
            enumStrValue = WordUtils.capitalize(enumStrValue);
            enumStrValue = enumStrValue.replaceAll("\\s", "");

            if (filters.contains(AccountSettingsActivity.dietFiltersEnum.valueOf(enumStrValue))) {
                // set checkbox checked upon opening page
                v.setChecked(true);
            }
            else {
                v.setChecked(false);
            }
        }
    }

}
