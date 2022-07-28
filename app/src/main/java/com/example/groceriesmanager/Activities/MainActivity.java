package com.example.groceriesmanager.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.groceriesmanager.Fragments.GroceryListFragment;
import com.example.groceriesmanager.Fragments.PantryListFragment;
import com.example.groceriesmanager.Fragments.RecipeSearchFragment;
import com.example.groceriesmanager.Fragments.UserProfileFragment;
import com.example.groceriesmanager.Fragments.YoutubeSearchFragment;
import com.example.groceriesmanager.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import nl.joery.animatedbottombar.AnimatedBottomBar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public AnimatedBottomBar bottomNavigationView; // got AnimateBottomBar class from https://github.com/droppers/animatedbottombar
    // these lines below are necessary to be able to refer to the fragments from another fragment via the activity
    final FragmentManager fragmentManager = getSupportFragmentManager();
    public GroceryListFragment groceryListFragment = new GroceryListFragment();
    public PantryListFragment pantryListFragment = new PantryListFragment(MainActivity.this);
    public UserProfileFragment userProfileFragment = new UserProfileFragment();
    public RecipeSearchFragment recipeSearchFragment = RecipeSearchFragment.newInstance("");
    public YoutubeSearchFragment youtubeSearchFragment = new YoutubeSearchFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create notification channel for sending alerts
        createNotificationChannel();


        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // when you first open the main activity, the grocerylist fragment shows first
        fragmentManager.beginTransaction().replace(R.id.frameLayout, groceryListFragment).commit();

        bottomNavigationView.setOnTabSelectListener(new AnimatedBottomBar.OnTabSelectListener() {
            Fragment fragment;
            @Override
            public void onTabSelected(int formertabposition, @Nullable AnimatedBottomBar.Tab formertab, int selectedtabposition, @NonNull AnimatedBottomBar.Tab selectedTab) {
                if (selectedTab.getId() == R.id.miGroceryList){
                    fragment = groceryListFragment;
                }
                if (selectedTab.getId() == R.id.miPantryList){
                    fragment = pantryListFragment;
                }
                if (selectedTab.getId() == R.id.miRecipeSearch){
                    fragment = recipeSearchFragment;
                }
                if (selectedTab.getId() == R.id.miYoutubeSearch){
                    fragment = youtubeSearchFragment;
                }
                if (selectedTab.getId() == R.id.miUserProfile){
                    fragment = userProfileFragment;
                }
                // replace fragment here. the if else chooses which direction the custom animation goes depending on the former and current tab positions
                if (selectedtabposition < formertabposition){
                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right).replace(R.id.frameLayout, fragment).commit();
                }
                else{
                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left).replace(R.id.frameLayout, fragment).commit();
                }
            }

            @Override
            public void onTabReselected(int i, @NonNull AnimatedBottomBar.Tab selectedTab) {
                if (selectedTab.getId() == R.id.miGroceryList){
                    fragment = groceryListFragment;
                }
                if (selectedTab.getId() == R.id.miPantryList){
                    fragment = pantryListFragment;
                }
                if (selectedTab.getId() == R.id.miRecipeSearch){
                    fragment = recipeSearchFragment;
                }
                if (selectedTab.getId() == R.id.miYoutubeSearch){
                    fragment = youtubeSearchFragment;
                }
                if (selectedTab.getId() == R.id.miUserProfile){
                    fragment = userProfileFragment;
                }
                fragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit();
            }
        });

    }

    private void createNotificationChannel(){
        // for sdk >= 26, a notification channel must be created to see notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "notifyExpiryDateChannel";
            String description = "Expiry Date Reminders";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyExpiryDate", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}