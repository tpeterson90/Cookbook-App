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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays and manages the signed-in user's grocery list.
 */
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

    private ListenerRegistration groceryListener;

    private boolean isAddingItem = false;

    public GroceriesFragment() {
    }

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
                view1 -> addGroceryItem()
        );

        buttonClearChecked.setOnClickListener(
                view12 -> clearCheckedItems()
        );

        inputGrocery.setOnEditorActionListener(
                (textView, actionId, event) -> {

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
     * Adds one grocery item to Firestore.
     */
    private void addGroceryItem() {

        if (isAddingItem) {
            return;
        }

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

        /*
         * Prevent exact duplicate names.
         */
        for (GroceryItem existing
                : groceryItems) {

            if (existing.getName() != null
                    && existing.getName()
                    .equalsIgnoreCase(name)) {

                showToast(
                        "That item is already on your list."
                );

                return;
            }
        }

        FirebaseUser user =
                firebaseAuth.getCurrentUser();

        if (user == null) {

            showToast(
                    "Please sign in again."
            );

            return;
        }

        isAddingItem = true;
        buttonAdd.setEnabled(false);

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

                    /*
                     * Do NOT manually add the item to groceryItems here.
                     * The Firestore snapshot listener does that.
                     */
                    inputGrocery.setText("");

                    isAddingItem = false;
                    buttonAdd.setEnabled(true);
                })
                .addOnFailureListener(error -> {

                    isAddingItem = false;
                    buttonAdd.setEnabled(true);

                    showToast(
                            "Unable to add item."
                    );
                });
    }

    /**
     * Listens for grocery list changes from Firestore.
     */
    private void listenForGroceries() {

        FirebaseUser user =
                firebaseAuth.getCurrentUser();

        if (user == null) {
            return;
        }

        if (groceryListener != null) {
            groceryListener.remove();
        }

        progressBar.setVisibility(
                View.VISIBLE
        );

        groceryListener =
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

                                    if (!isAdded()) {
                                        return;
                                    }

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

                                        for (DocumentSnapshot document
                                                : value.getDocuments()) {

                                            String id =
                                                    document.getId();

                                            String name =
                                                    document.getString(
                                                            "name"
                                                    );

                                            Boolean checked =
                                                    document.getBoolean(
                                                            "checked"
                                                    );

                                            GroceryItem item =
                                                    new GroceryItem(
                                                            id,
                                                            name == null
                                                                    ? ""
                                                                    : name,
                                                            checked != null
                                                                    && checked
                                                    );

                                            groceryItems.add(item);
                                        }
                                    }

                                    adapter.notifyDataSetChanged();

                                    updateEmptyState();
                                });
    }

    /**
     * Updates a grocery item's checked status.
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
     * Deletes one grocery item.
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
     * Deletes all checked grocery items.
     */
    private void clearCheckedItems() {

        FirebaseUser user =
                firebaseAuth.getCurrentUser();

        if (user == null) {
            return;
        }

        boolean foundChecked = false;

        for (GroceryItem item
                : new ArrayList<>(groceryItems)) {

            if (item.isChecked()
                    && item.getId() != null) {

                foundChecked = true;

                database
                        .collection("users")
                        .document(user.getUid())
                        .collection("groceries")
                        .document(item.getId())
                        .delete();
            }
        }

        if (!foundChecked) {

            showToast(
                    "No checked items to clear."
            );
        }
    }

    /**
     * Updates the empty-list message.
     */
    private void updateEmptyState() {

        boolean empty =
                groceryItems.isEmpty();

        textEmpty.setVisibility(
                empty
                        ? View.VISIBLE
                        : View.GONE
        );

        recyclerView.setVisibility(
                empty
                        ? View.GONE
                        : View.VISIBLE
        );
    }

    /**
     * Removes the Firestore listener when the view closes.
     */
    @Override
    public void onDestroyView() {

        super.onDestroyView();

        if (groceryListener != null) {
            groceryListener.remove();
            groceryListener = null;
        }
    }

    /**
     * Shows a Toast message.
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