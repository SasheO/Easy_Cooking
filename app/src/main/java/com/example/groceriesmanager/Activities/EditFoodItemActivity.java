package com.example.groceriesmanager.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceriesmanager.Adapters.FoodCategorySpinnerAdapter;
import com.example.groceriesmanager.Models.FoodItem;
import com.example.groceriesmanager.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EditFoodItemActivity extends AppCompatActivity {
    private Spinner spinnerFoodMeasure;
    private Spinner spinnerFoodCategory;
    private EditText etFoodName;
    private EditText etFoodQty;
    private Button btnSave;
    FoodItem foodItem;
    private Button btnCancel;
    private TextView tvTitle;
    private static final String TAG = "EditFoodItemActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_food_item);

        spinnerFoodMeasure = findViewById(R.id.spinnerFoodMeasure);
        spinnerFoodCategory = findViewById(R.id.spinnerFoodCategory);
        etFoodName = findViewById(R.id.etFoodName);
        etFoodQty = findViewById(R.id.etFoodQty);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        tvTitle = findViewById(R.id.tvTitle);

        String process = getIntent().getStringExtra("process");


        // array adapter for rendering items into the food measure spinner
        ArrayAdapter<CharSequence> foodMeasureAdapter = ArrayAdapter.createFromResource(this, R.array.food_measures, android.R.layout.simple_spinner_item);
        foodMeasureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerFoodMeasure.setAdapter(foodMeasureAdapter);
        // todo: array adapter for rendering items into the food category spinner
        // todo: make this spinner a dialog box, not a dropdown
        // Our custom Adapter class that we created
        FoodCategorySpinnerAdapter adapter = new FoodCategorySpinnerAdapter(getApplicationContext(), Arrays.asList(getResources().getStringArray(R.array.food_categories)));
        adapter.setDropDownViewResource(R.layout.spinner_item_food_category);
        spinnerFoodCategory.setAdapter(adapter);

        // if intent process is edit, get the food item passed in and set the values in the edit text, etc
        if (Objects.equals(process, "edit")){
            tvTitle.setText("Edit Food Item"); // change title from "create food item"
            foodItem = getIntent().getParcelableExtra("foodItem");
            etFoodName.setText(foodItem.getName());
            etFoodQty.setText(foodItem.getQuantity());
            spinnerFoodMeasure.setSelection(foodMeasureAdapter.getPosition(foodItem.getMeasure()));
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String type = getIntent().getStringExtra("type");
                String foodName = etFoodName.getText().toString();
                String foodQty = etFoodQty.getText().toString();

                String foodMeasure = spinnerFoodMeasure.getSelectedItem().toString();
                // todo: fix error here. get selected item
                String foodCategory = Arrays.asList(getResources().getStringArray(R.array.food_categories)).get(spinnerFoodCategory.getSelectedItemPosition());
                Log.i(TAG, "food category selection: " + foodCategory);


                if (foodName.replaceAll("\\s+", "").length()==0){ // if the user did not type in a food name or types only spaces
                    Toast.makeText(EditFoodItemActivity.this, "type in the food name", Toast.LENGTH_LONG).show();

                }
                else{
                    if (Objects.equals(process, "new")){ // is user is creating new food item
                        addFoodItem(foodName, foodQty, foodMeasure, type, foodCategory);
                    }
                    else{ // process is edit
                        editFoodItem(foodName, foodQty, foodMeasure, type, foodCategory);
                    }
                }
        }
        });
    }

    private void editFoodItem(String foodName, String foodQty, String foodMeasure, String type, String foodCategory) {
        foodItem.setName(foodName);
        if (foodQty != ""){
            foodItem.setQuantity(foodQty);
            foodItem.setMeasure(foodMeasure);
        }
        else{
            foodItem.setQuantity("");
            foodItem.setMeasure("-");
        }
        if (!Objects.equals(foodCategory, "--no selection--")){
            foodItem.setCategory(foodCategory);
        }

        foodItem.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null){
                    Log.e(TAG, "error saving edited food item: " + e.toString());
                    Toast.makeText(EditFoodItemActivity.this, "Could not edit food item", Toast.LENGTH_SHORT).show();
                }
                else {
                    finish();
                }
            }
        });
    }

    private void addFoodItem(String foodName, String foodQty, String foodMeasure, String type, String foodCategory){

        FoodItem newFoodItem = new FoodItem();
        newFoodItem.setName(foodName.replaceAll("\n", ""));
        newFoodItem.setType(type);
        newFoodItem.setUser(ParseUser.getCurrentUser());
        if (foodQty != ""){
            newFoodItem.setQuantity(foodQty);
            newFoodItem.setMeasure(foodMeasure);
        }
        if (!Objects.equals(foodCategory, "--no selection--")){
            newFoodItem.setCategory(foodCategory);
        }
        // update info in parse server
        newFoodItem.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null){
                    Log.e(TAG, "error saving food item to server");
                }
                else{
                    Log.i(TAG, "food item saved successfully");
                    etFoodName.setText("");
                    etFoodQty.setText("");
                    finish();
                }
            }
        });
        }


}