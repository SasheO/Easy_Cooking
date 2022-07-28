package com.example.groceriesmanager.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.groceriesmanager.R;

// dialog fragment information gotten from https://guides.codepath.org/android/Using-DialogFragment
public class HowToUseFragment extends DialogFragment {
    public HowToUseFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static HowToUseFragment newInstance(String title) {
        HowToUseFragment frag = new HowToUseFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_how_to_use_food_list, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        VideoView vvSwitchList = view.findViewById(R.id.vvSwitchList);
        vvSwitchList.setVideoURI(Uri.parse("android.resource://" + getContext().getPackageName() +"/"+R.raw.how_to_use_switch_list));
        vvSwitchList.setMediaController(new MediaController(getContext()));
        vvSwitchList.requestFocus();
        vvSwitchList.start();

        VideoView vvEditItem = view.findViewById(R.id.vvEditItem);
        vvEditItem.setVideoURI(Uri.parse("android.resource://" + getContext().getPackageName() +"/"+R.raw.how_to_use_edit_item));
        vvEditItem.setMediaController(new MediaController(getContext()));
        vvEditItem.requestFocus();
        vvEditItem.start();


        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "How To Use");
        getDialog().setTitle(title);
        // Show soft keyboard automatically and request focus to field
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

}
