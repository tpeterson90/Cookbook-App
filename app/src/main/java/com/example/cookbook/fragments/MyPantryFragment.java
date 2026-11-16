
package com.example.cookbook.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cookbook.R;
import com.example.cookbook.PantryItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyPantryFragment extends Fragment {

    private DatabaseReference pantryRef;

    private Button addIngredientButton;
    private EditText searchPantry;

    private LinearLayout ingredientList;

    private TextView emptyPantryMessage;
    private TextView pantryItemCount;

    private ArrayList<PantryItem> pantryItems =
            new ArrayList<>();

    private String currentUserID;

    private ValueEventListener pantryListener;


    // Create Pantry screen
    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.my_pantry,
                container,
                false
        );

        // Connect XML elements
        addIngredientButton =
                view.findViewById(R.id.addIngredientButton);

        searchPantry =
                view.findViewById(R.id.searchPantry);

        ingredientList =
                view.findViewById(R.id.ingredientList);

        emptyPantryMessage =
                view.findViewById(R.id.emptyPantryMessage);

        pantryItemCount =
                view.findViewById(R.id.pantryItemCount);


        // Check Firebase Authentication
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {

            Toast.makeText(
                    requireContext(),
                    "Please log in first",
                    Toast.LENGTH_SHORT
            ).show();

            addIngredientButton.setEnabled(false);

            return view;
        }

        // Get current user
        currentUserID = FirebaseAuth.getInstance()
                .getCurrentUser().getUid();

        // Initialize Firebase
        pantryRef = FirebaseDatabase.getInstance()
                .getReference("PantryItems");

        // Add button functionality
        addIngredientButton.setOnClickListener(v ->
                showAddIngredientDialog()
        );

        // Load pantry
        loadPantryItems();

        // Search functionality
        searchPantry.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                displayPantryItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }


    // Open Add Ingredient dialog
    private void showAddIngredientDialog() {

        LinearLayout layout =
                new LinearLayout(requireContext());

        layout.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (
                20 * getResources()
                        .getDisplayMetrics().density
        );

        layout.setPadding(
                padding, padding, padding, padding
        );

        // Ingredient name
        EditText ingredientInput =
                new EditText(requireContext());

        ingredientInput.setHint("Ingredient name");

        // Quantity
        EditText quantityInput =
                new EditText(requireContext());

        quantityInput.setHint("Quantity");

        quantityInput.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );

        // Measurement unit dropdown
        TextView unitLabel =
                new TextView(requireContext());

        unitLabel.setText("Measurement Unit");
        unitLabel.setTextSize(14);
        unitLabel.setPadding(0, 16, 0, 4);

        Spinner unitSpinner =
                new Spinner(requireContext());

        String[] units = {
                "Pieces",
                "Cups",
                "Tablespoons",
                "Teaspoons",
                "Grams (g)",
                "Kilograms (kg)",
                "Milliliters (ml)",
                "Liters (L)",
                "Ounces (oz)",
                "Pounds (lbs)",
                "Cans",
                "Bottles",
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        units
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        unitSpinner.setAdapter(adapter);

        // Add fields to dialog
        layout.addView(ingredientInput);
        layout.addView(quantityInput);
        layout.addView(unitLabel);
        layout.addView(unitSpinner);


        AlertDialog dialog =
                new AlertDialog.Builder(requireContext())
                        .setTitle("Add Ingredient")
                        .setView(layout)
                        .setPositiveButton("Add", null)
                        .setNegativeButton("Cancel", null)
                        .create();

        dialog.setOnShowListener(d -> {

            Button addButton =
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            addButton.setOnClickListener(v -> {

                String name = ingredientInput
                        .getText().toString().trim();

                String quantityText = quantityInput
                        .getText().toString().trim();

                String unit = unitSpinner
                        .getSelectedItem().toString();

                // Validate fields
                if (name.isEmpty() || quantityText.isEmpty()) {

                    Toast.makeText(
                            requireContext(),
                            "Please fill in all fields",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                int quantity;

                try {
                    quantity = Integer.parseInt(quantityText);

                } catch (NumberFormatException e) {

                    Toast.makeText(
                            requireContext(),
                            "Enter a valid quantity",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                if (quantity <= 0) {

                    Toast.makeText(
                            requireContext(),
                            "Quantity must be greater than zero",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                // Save ingredient
                addIngredient(name, quantity, unit);

                dialog.dismiss();
            });
        });

        dialog.show();
    }


    // Save ingredient to Firebase
    private void addIngredient(
            String name,
            int quantity,
            String unit) {

        if (currentUserID == null || pantryRef == null) {
            return;
        }

        String itemID = pantryRef.push().getKey();

        if (itemID == null) {
            return;
        }

        PantryItem item = new PantryItem(
                itemID,
                currentUserID,
                name,
                quantity,
                unit
        );

        pantryRef.child(itemID)
                .setValue(item)

                .addOnSuccessListener(unused -> {

                    if (getContext() != null) {

                        Toast.makeText(
                                getContext(),
                                "Ingredient added!",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })

                .addOnFailureListener(e -> {

                    if (getContext() != null) {

                        Toast.makeText(
                                getContext(),
                                "Failed to add: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }


    // Retrieve ingredients from Firebase
    private void loadPantryItems() {

        pantryListener = new ValueEventListener() {

            @Override
            public void onDataChange(
                    @NonNull DataSnapshot snapshot) {

                pantryItems.clear();

                for (DataSnapshot itemSnapshot :
                        snapshot.getChildren()) {

                    PantryItem item =
                            itemSnapshot.getValue(
                                    PantryItem.class
                            );

                    if (item != null) {
                        pantryItems.add(item);
                    }
                }

                String search = searchPantry
                        .getText().toString();

                displayPantryItems(search);
            }

            @Override
            public void onCancelled(
                    @NonNull DatabaseError error) {

                if (getContext() != null) {

                    Toast.makeText(
                            getContext(),
                            "Firebase error: "
                                    + error.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        };

        pantryRef.orderByChild("userID")
                .equalTo(currentUserID)
                .addValueEventListener(pantryListener);
    }


    // Display ingredients in Pantry
    private void displayPantryItems(String search) {

        if (getContext() == null) return;

        ingredientList.removeAllViews();

        int count = 0;

        for (PantryItem item : pantryItems) {

            if (item.getIngredientName() == null) {
                continue;
            }

            if (!item.getIngredientName()
                    .toLowerCase()
                    .contains(search.toLowerCase())) {

                continue;
            }

            count++;

            // Ingredient row
            LinearLayout row =
                    new LinearLayout(requireContext());

            row.setOrientation(LinearLayout.HORIZONTAL);

            row.setGravity(Gravity.CENTER_VERTICAL);

            row.setPadding(0, 16, 0, 16);

            // Ingredient name
            TextView ingredientName =
                    new TextView(requireContext());

            ingredientName.setText(
                    item.getIngredientName()
            );

            ingredientName.setTextSize(16);

            ingredientName.setTextColor(
                    android.graphics.Color.rgb(41, 51, 44)
            );

            ingredientName.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1
                    )
            );

            // Quantity and unit
            TextView quantityView =
                    new TextView(requireContext());

            quantityView.setText(
                    item.getQuantity()
                            + " "
                            + item.getUnit()
            );

            quantityView.setTextSize(14);

            quantityView.setTextColor(
                    android.graphics.Color.rgb(133, 139, 130)
            );

            row.addView(ingredientName);
            row.addView(quantityView);

            ingredientList.addView(row);

            // Divider
            View divider = new View(requireContext());

            divider.setBackgroundColor(
                    android.graphics.Color.rgb(230, 230, 223)
            );

            LinearLayout.LayoutParams dividerParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            1
                    );

            divider.setLayoutParams(dividerParams);

            ingredientList.addView(divider);
        }

        // Update item count
        pantryItemCount.setText(
                count + (count == 1 ? " item" : " items")
        );

        // Empty pantry message
        if (count == 0) {

            emptyPantryMessage.setVisibility(View.VISIBLE);

            if (search.isEmpty()) {

                emptyPantryMessage.setText(
                        "Your pantry is empty.\n"
                                + "Add ingredients to get started!"
                );

            } else {

                emptyPantryMessage.setText(
                        "No matching ingredients found."
                );
            }

        } else {

            emptyPantryMessage.setVisibility(View.GONE);
        }
    }


    // Remove Firebase listener
    @Override
    public void onDestroyView() {

        if (pantryRef != null && pantryListener != null) {

            pantryRef.removeEventListener(pantryListener);
        }

        super.onDestroyView();
    }
}
