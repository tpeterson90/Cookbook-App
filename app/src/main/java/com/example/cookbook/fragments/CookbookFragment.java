package com.example.cookbook.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cookbook.R;

public class CookbookFragment extends Fragment {

    public CookbookFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_cookbook, container, false);

// Find the Recipe Details button
        Button recipeButton = view.findViewById(R.id.recipeButton);

// Open the recipe details page when clicked
        recipeButton.setOnClickListener(v -> {

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .hide(this)
                    .add(R.id.fragmentContainer, new RecipeDetailFragment(), "recipe_detail")
                    .commit();

        });

        return view;
    }
}