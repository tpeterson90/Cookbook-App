package com.example.cookbook;

/**
 * Represents one grocery list item.
 */
public class GroceryItem {

    private String id;
    private String name;
    private boolean checked;

    /**
     * Empty constructor required by Firebase.
     */
    public GroceryItem() {
    }

    /**
     * Creates a grocery item.
     */
    public GroceryItem(
            String id,
            String name,
            boolean checked) {

        this.id = id;
        this.name = name;
        this.checked = checked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}