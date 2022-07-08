package com.example.groceriesmanager.Models;

import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.parse.DeleteCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;
import java.util.Objects;

@ParseClassName("FoodItem")
public class FoodItem extends ParseObject {
    private static final String TAG = "FoodItem";
    // these variables store the name that will be used to send queries to Parse database
    private static final String KEY_NAME = "name";
    private static final String KEY_QUANTITY = "quantity";
    private static final String KEY_USER = "user";
    private static final String KEY_MEASURE = "measure";
    private static final String KEY_TYPE = "type";
    private static final String KEY_CATEGORY= "foodCategory";

    public String getName(){
        try {
            return fetchIfNeeded().getString(KEY_NAME);
        } catch (ParseException e) {
            Log.v(TAG, e.toString());
            return null;
        }
    }
    public String getQuantity() {
        try {
            return fetchIfNeeded().getString(KEY_QUANTITY);
        } catch (ParseException e) {
            Log.v(TAG, e.toString());
            return null;
        }
    }
    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }
    public String getMeasure()
    {
        try {
            return fetchIfNeeded().getString(KEY_MEASURE);
        } catch (ParseException e) {
            Log.v(TAG, e.toString());
            return null;
        }
    }
    public String getType(){
        try {
            return fetchIfNeeded().getString(KEY_TYPE);
        } catch (ParseException e) {
            Log.v(TAG, e.toString());
            return null;
        }
    }
    public String getFoodCategory(){
        try {
            return fetchIfNeeded().getString(KEY_CATEGORY);
        } catch (ParseException e) {
            Log.v(TAG, e.toString());
            return null;
        }
    }

    public void setName(String name){ put(KEY_NAME, name); }
    public void setUser(ParseUser user){
        put(KEY_USER, user);
    }
    public void setQuantity(String quantity){ put(KEY_QUANTITY, quantity); }
    public void setMeasure(String measure){ put(KEY_MEASURE, measure); }
    public void setType(String type){ put(KEY_TYPE, type); }
    public void setCategory(String category){ put(KEY_CATEGORY, category); }

    public void switchList() {
        if (Objects.equals(getType(), "grocery")){
            setType("pantry");
        }
        else{
            setType("grocery");
        }
    }


    public void deleteFood(){
        deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null){
                    Log.e(TAG, "error deleting food item in database: " + e.toString());
                }
                else
                {
                    Log.i(TAG, "food item deleted");
                }
            }
        });
    }

}