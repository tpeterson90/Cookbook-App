package com.example.cookbook.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cookbook.R;

public class ProfileFragment extends Fragment {
    public ProfileFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, //Creates screen and loads XML layout
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
//  Initializes Dropdown Menu that provides options for the amount of people your cooking for
        Button peopleButton = view.findViewById(R.id.people_menu);
        peopleButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), peopleButton);
            popup.getMenuInflater().inflate(R.menu.menu_people, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                String selected = item.getTitle().toString();

                peopleButton.setText(selected);

                return true;
            });
            popup.show();
        });
// Initializes Dropdown Menu that provides dietary options
        Button dietButton = view.findViewById(R.id.dietary_menu);
        dietButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), dietButton);
            popup.getMenuInflater().inflate(R.menu.menu_dietary, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                String selected = item.getTitle().toString();

                dietButton.setText(selected);

                return true;
            });
            popup.show();
        });

// Initializes Dropdown Menu that provides measurement options
        Button metricButton = view.findViewById(R.id.metric_menu);
        metricButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), metricButton);
            popup.getMenuInflater().inflate(R.menu.menu_metric, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                String selected = item.getTitle().toString();

                metricButton.setText(selected);

                return true;
            });
            popup.show();
        });
        return view;
    }

}
