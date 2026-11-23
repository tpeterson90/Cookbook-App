package com.example.cookbook;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Displays grocery items inside the RecyclerView.
 */
public class GroceryAdapter
        extends RecyclerView.Adapter<GroceryAdapter.GroceryViewHolder> {

    /**
     * Listener used by the fragment for grocery actions.
     */
    public interface GroceryListener {

        void onCheckedChanged(
                GroceryItem item,
                boolean checked
        );

        void onDeleteClicked(
                GroceryItem item
        );
    }

    private final List<GroceryItem> items;
    private final GroceryListener listener;

    public GroceryAdapter(
            List<GroceryItem> items,
            GroceryListener listener) {

        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroceryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_grocery,
                                parent,
                                false
                        );

        return new GroceryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull GroceryViewHolder holder,
            int position) {

        GroceryItem item =
                items.get(position);

        holder.checkBox
                .setOnCheckedChangeListener(null);

        holder.checkBox.setText(
                item.getName()
        );

        holder.checkBox.setChecked(
                item.isChecked()
        );

        updateStrikeThrough(
                holder.checkBox,
                item.isChecked()
        );

        holder.checkBox
                .setOnCheckedChangeListener(
                        (buttonView, checked) -> {

                            updateStrikeThrough(
                                    holder.checkBox,
                                    checked
                            );

                            listener.onCheckedChanged(
                                    item,
                                    checked
                            );
                        });

        holder.buttonDelete
                .setOnClickListener(
                        view ->
                                listener.onDeleteClicked(
                                        item
                                )
                );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Shows completed groceries with a line through them.
     */
    private void updateStrikeThrough(
            CheckBox checkBox,
            boolean checked) {

        if (checked) {

            checkBox.setPaintFlags(
                    checkBox.getPaintFlags()
                            | Paint.STRIKE_THRU_TEXT_FLAG
            );

        } else {

            checkBox.setPaintFlags(
                    checkBox.getPaintFlags()
                            & ~Paint.STRIKE_THRU_TEXT_FLAG
            );
        }
    }

    /**
     * Holds one grocery row.
     */
    static class GroceryViewHolder
            extends RecyclerView.ViewHolder {

        private final CheckBox checkBox;
        private final ImageButton buttonDelete;

        GroceryViewHolder(
                @NonNull View itemView) {

            super(itemView);

            checkBox =
                    itemView.findViewById(
                            R.id.checkGrocery
                    );

            buttonDelete =
                    itemView.findViewById(
                            R.id.buttonDelete
                    );
        }
    }
}