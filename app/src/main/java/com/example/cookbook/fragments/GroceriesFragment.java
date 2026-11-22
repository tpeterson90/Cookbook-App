package com.example.cookbook.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookbook.GroceryAdapter;
import com.example.cookbook.GroceryItem;
import com.example.cookbook.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroceriesFragment
        extends Fragment
        implements GroceryAdapter.GroceryListener {

    private TextInputEditText inputGrocery;
    private MaterialButton buttonAdd;
    private MaterialButton buttonClearChecked;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textEmpty;

    private final List<GroceryItem> groceryItems =
            new ArrayList<>();

    private GroceryAdapter adapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore database;

    /**
     * Required empty constructor.
     */
    public GroceriesFragment() {
    }

    /**
     * Creates the grocery list screen.
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view =
                inflater.inflate(
                        R.layout.fragment_groceries,
                        container,
                        false
                );

        firebaseAuth =
                FirebaseAuth.getInstance();

        database =
                FirebaseFirestore.getInstance();

        inputGrocery =
                view.findViewById(
                        R.id.inputGrocery
                );

        buttonAdd =
                view.findViewById(
                        R.id.buttonAddGrocery
                );

        buttonClearChecked =
                view.findViewById(
                        R.id.buttonClearChecked
                );

        recyclerView =
                view.findViewById(
                        R.id.recyclerGroceries
                );

        progressBar =
                view.findViewById(
                        R.id.progressGroceries
                );

        textEmpty =
                view.findViewById(
                        R.id.textEmptyGroceries
                );

        adapter =
                new GroceryAdapter(
                        groceryItems,
                        this
                );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        recyclerView.setAdapter(adapter);

        buttonAdd.setOnClickListener(
                clickedView -> addGroceryItem()
        );

        buttonClearChecked
                .setOnClickListener(
                        clickedView ->
                                clearCheckedItems()
                );

        inputGrocery
                .setOnEditorActionListener(
                        (v, actionId, event) -> {

                            if (actionId
                                    == EditorInfo.IME_ACTION_DONE) {

                                addGroceryItem();
                                return true;
                            }

                            return false;
                        });

        listenForGroceries();

        return view;
    }

    /**
     * Adds a grocery item to Firestore.
     */
    private void addGroceryItem() {

        if (inputGrocery.getText() == null) {
            return;
        }

        String name =
                inputGrocery
                        .getText()
                        .toString()
                        .trim();

        if (name.isEmpty()) {

            showToast(
                    "Enter a grocery item."
            );

            return;
        }

        FirebaseUser user =
                firebaseAuth.getCurrentUser();

        if (user == null) {

            showToast(
                    "Please sign in again."
            );

            return;
        }

        String itemId =
                database
                        .collection("users")
                        .document(user.getUid())
                        .collection("groceries")
                        .document()
                        .getId();

        Map<String, Object> grocery =
                new HashMap<>();

        grocery.put(
                "id",
                itemId
        );

        grocery.put(
                "name",
                name
        );

        grocery.put(
                "checked",
                false
        );

        grocery.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        database
                .collection("users")
                .document(user.getUid())
                .collection("groceries")
                .document(itemId)
                .set(grocery)
                .addOnSuccessListener(unused -> {

                    inputGrocery.setText("");
                })
                .addOnFailureListener(error -> {

                    showToast(
                            "Unable to add item."
                    );
                });
    }

    /**
     * Listens for real-time grocery list updates.
     */
    private void listenForGroceries() {

        FirebaseUser user =
                firebaseAuth.getCurrentUser();

        if (user == null) {
            return;
        }

        progressBar.setVisibility(
                View.VISIBLE
        );

        database
                .collection("users")
                .document(user.getUid())
                .collection("groceries")
                .orderBy(
                        "createdAt",
                        Query.Direction.ASCENDING
                )
                .addSnapshotListener(
                        (value, error) -> {

                            progressBar.setVisibility(
                                    View.GONE
                            );

                            if (error != null) {

                                showToast(
                                        "Unable to load groceries."
                                );

                                return;
                            }

                            groceryItems.clear();

                            if (value != null) {

                                for (var document
                                        : value.getDocuments()) {

                                    GroceryItem item =
                                            document.toObject(
                                                    GroceryItem.class
                                            );

                                    if (item != null) {

                                        item.setId(
                                                document.getId()
                                        );

                                        groceryItems.add(item);
                                    }
                                }
                            }

                            adapter.notifyDataSetChanged();

                            updateEmptyState();
                        });
    }

    /**
     * Updates a grocery item's checked state.
     */
    @Override
    public void onCheckedChanged(
            GroceryItem item,
            boolean checked) {

        FirebaseUser user =
                firebaseAuth.getCurrentUser();

        if (user == null
                || item.getId() == null) {

            return;
        }

        database
                .collection("users")
                .document(user.getUid())
                .collection("groceries")
                .document(item.getId())
                .update(
                        "checked",
                        checked
                );
    }

    /**
     * Deletes a grocery item.
     */
    @Override
    public void onDeleteClicked(
            GroceryItem item) {

        FirebaseUser user =
                firebaseAuth.getCurrentUser();

        if (user == null
                || item.getId() == null) {

            return;
        }

        database
                .collection("users")
                .document(user.getUid())
                .collection("groceries")
                .document(item.getId())
                .delete()
                .addOnFailureListener(
                        error ->
                                showToast(
                                        "Unable to delete item."
                                )
                );
    }

    /**
     * Deletes every checked grocery item.
     */
    private void clearCheckedItems() {

        FirebaseUser user =
                firebaseAuth.getCurrentUser();

        if (user == null) {
            return;
        }

        boolean foundCheckedItem = false;

        for (GroceryItem item
                : new ArrayList<>(groceryItems)) {

            if (item.isChecked()
                    && item.getId() != null) {

                foundCheckedItem = true;

                database
                        .collection("users")
                        .document(user.getUid())
                        .collection("groceries")
                        .document(item.getId())
                        .delete();
            }
        }

        if (!foundCheckedItem) {

            showToast(
                    "No checked items to clear."
            );
        }
    }

    /**
     * Shows or hides the empty-list message.
     */
    private void updateEmptyState() {

        textEmpty.setVisibility(
                groceryItems.isEmpty()
                        ? View.VISIBLE
                        : View.GONE
        );

        recyclerView.setVisibility(
                groceryItems.isEmpty()
                        ? View.GONE
                        : View.VISIBLE
        );
    }

    /**
     * Displays a Toast message.
     */
    private void showToast(String message) {

        if (getContext() != null) {

            Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}