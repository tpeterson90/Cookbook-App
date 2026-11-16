
package com.example.cookbook;

public class PantryItem {

    private String pantryItemID;
    private String userID;
    private String ingredientName;
    private int quantity;
    private String unit;

    // Required empty constructor for Firebase
    public PantryItem() {
    }

    public PantryItem(String pantryItemID,
                      String userID,
                      String ingredientName,
                      int quantity,
                      String unit) {

        this.pantryItemID = pantryItemID;
        this.userID = userID;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getPantryItemID() {
        return pantryItemID;
    }

    public void setPantryItemID(String pantryItemID) {
        this.pantryItemID = pantryItemID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
