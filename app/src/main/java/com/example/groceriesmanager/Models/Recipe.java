package com.example.groceriesmanager.Models;

import android.util.Log;

import com.example.groceriesmanager.Activities.AccountSettingsActivity;
import com.parse.DeleteCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

@ParseClassName("Recipe")
public class Recipe extends ParseObject {
    private String image_url;
    private static final String TAG = "Recipe";
    private static final String KEY_FILTER_VEGAN = "Vegan";
    private static final String KEY_FILTER_VEGETARIAN = "Vegetarian";
    private static final String KEY_FILTER_GLUTEN_FREE = "Gluten-Free";
    private static final String KEY_IMAGE_URL = "image_url";
    private static final String KEY_TITLE = "title";
    private static final String KEY_USER = "user";
//    private static final String KEY_INGREDIENT_LINES_STR = "ingredientLines";
    private static final String KEY_TYPE = "type";
    // these are set as public because they are referred to in other class (EditRecipeActivity)
    public static final String KEY_PROCEDURE = "procedure";
    public static final String KEY_FILTERS = "filters";
    public static final String KEY_HYPERLINK_URL = "hyperlink_url";
    public static final String KEY_INGREDIENTS = "ingredients";
    public static final Hashtable jsonFilterToEnum = new Hashtable();
//    jsonFilterToEnum.put("beverages/dairy", "dairy");

    public Recipe(){}

    public Recipe(JSONObject jsonObject) throws JSONException {
        this.image_url = jsonObject.getJSONObject("recipe").getJSONObject("images").getJSONObject("REGULAR").getString("url");
        put(KEY_IMAGE_URL, jsonObject.getJSONObject("recipe").getJSONObject("images").getJSONObject("REGULAR").getString("url"));
        put(KEY_TITLE, jsonObject.getJSONObject("recipe").getString("label"));
        put(KEY_HYPERLINK_URL, jsonObject.getJSONObject("recipe").getString("url"));
        JSONArray ingredientsJSONArray = jsonObject.getJSONObject("recipe").getJSONArray("ingredients");
        List<FoodItem> ingredientLines = new ArrayList<>();
        for (int i=0; i<ingredientsJSONArray.length(); i++){
            FoodItem ingredient = new FoodItem();
            JSONObject ingredientJSONObject = ingredientsJSONArray.getJSONObject(i);
            ingredient.setName(ingredientJSONObject.getString("food"));
            String measure = ingredientJSONObject.getString("measure");
            String qty = String.valueOf(ingredientJSONObject.getInt("quantity"));

            if (!Objects.equals(qty, "0")){
                ingredient.setQuantity(qty);
                if (Objects.equals(measure, "<unit>")){measure = "ct";}
                ingredient.setMeasure(measure);
            }

            ingredientLines.add(ingredient);
        }
        put(KEY_INGREDIENTS, ingredientLines);

       EnumSet<AccountSettingsActivity.dietFiltersEnum> filtersEnumSet = EnumSet.noneOf(AccountSettingsActivity.dietFiltersEnum.class);
        JSONArray filtersJSONArray = jsonObject.getJSONObject("recipe").getJSONArray("healthLabels");
        for (int i=0; i<filtersJSONArray.length(); i++){
            String filterStr =  filtersJSONArray.getString(i);
            // format the string title from the format returned json objects (lower-case-separated-with-hyphens) to format in AccountSettingsActivity.dietFiltersEnum (FirstLetterOfEachWordCapitalized)
            filterStr = filterStr.replace('-', ' '); // replace hyphen with space
            filterStr = StringUtils.capitalize(filterStr); // capitalize each first word
            filterStr = filterStr.replaceAll("\\s", ""); // remove spaces
            if (EnumUtils.isValidEnum(AccountSettingsActivity.dietFiltersEnum.class, filterStr)){
                filtersEnumSet.add(AccountSettingsActivity.dietFiltersEnum.valueOf(filterStr));
            }
        }
        setFilters(filtersEnumSet);
    }

    public String getImage_url() {
        try {
            return fetchIfNeeded().getString(KEY_IMAGE_URL);
        } catch (ParseException e) {
            Log.v(TAG, e.toString());
            return image_url;
        }
    }


    public String getTitle() {
        try {
            return fetchIfNeeded().getString(KEY_TITLE);
        } catch (ParseException e) {
            Log.v(TAG, e.toString());
            return null;
        }
    }


    public EnumSet<AccountSettingsActivity.dietFiltersEnum> getFilters() {
        EnumSet<AccountSettingsActivity.dietFiltersEnum> filters = EnumSet.noneOf(AccountSettingsActivity.dietFiltersEnum.class);
        List<String> filtersList = getList(KEY_FILTERS);
        if (filtersList!=null){
            for (String filter: filtersList){
                filters.add(AccountSettingsActivity.dietFiltersEnum.valueOf(filter));
            }
            return filters;
        }
        else {
            return null;
        }
    }


    public String getHyperlink_url() {
        try {
            return fetchIfNeeded().getString(KEY_HYPERLINK_URL);
        } catch (ParseException e) {
            Log.v(TAG, e.toString());
            return null;
        }
    }

    public List<FoodItem> getIngredients() {
        try {
            return fetchIfNeeded().getList(KEY_INGREDIENTS);
        } catch (ParseException e) {
            Log.v(TAG, e.toString());
            return null;
        }
    }

    public List<String> getProcedure() {
        try {
            return fetchIfNeeded().getList(KEY_PROCEDURE);
        } catch (ParseException e) {
            Log.v(TAG, e.toString());
            return null;
        }
    }

    public String getType(){
        return getString(KEY_TYPE);
    }
    public void setTitle(String title) {
        put(KEY_TITLE, title);
    }
    public void setFilters(EnumSet<AccountSettingsActivity.dietFiltersEnum> filtersEnum) {
        List<String> filtersArray = new ArrayList<>();
        for(Enum filter : filtersEnum) {
            filtersArray.add(filter.name());
        }
        put(KEY_FILTERS, filtersArray);
    }
    public void setHyperlink_url(String hyperlink_url) {
        put(KEY_HYPERLINK_URL, hyperlink_url);
    }
    public void setIngredients(List<FoodItem> ingredients) {
        put(KEY_INGREDIENTS, ingredients);
    }
    public void setUser(ParseUser user){
        put(KEY_USER, user);
    }
    public void setType(String type){
        put(KEY_TYPE, type);
    }
    public void setProcedure(List<String> procedure){
        put(KEY_PROCEDURE, procedure);
    }


    public static List<Recipe> fromJsonArray(JSONArray recipeJsonArray) throws JSONException {
        List<Recipe> recipeList = new ArrayList<>();
        for (int i=0; i<recipeJsonArray.length(); i++){
            recipeList.add(new Recipe(recipeJsonArray.getJSONObject(i)));
        }
        return recipeList;
    }

    public void deleteRecipe(){
        deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null){
                    Log.e(TAG, "error deleting recipe in database: " + e.toString());
                }
                else
                {
                    Log.i(TAG, "recipe deleted");
                }
            }
        });
    }

}
