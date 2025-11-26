package com.example.magicquill.ui.view;

import android.content.Context;

/**
 * Individual bottom navigation item component.
 * Represents a single item in the bottom navigation bar.
 */
public class BottomNavItem {
    
    private int itemId;
    private int iconResource;
    private String title;
    private Class<?> fragmentClass;
    
    public interface BottomNavItemListener {
        void onItemSelected(int itemId);
    }
    
    private BottomNavItemListener listener;
    
    /**
     * Constructor for a bottom navigation item.
     * @param itemId The resource ID of the menu item
     * @param iconResource The icon resource ID
     * @param title The title text
     * @param fragmentClass The fragment class this item navigates to
     */
    public BottomNavItem(int itemId, int iconResource, String title, Class<?> fragmentClass) {
        this.itemId = itemId;
        this.iconResource = iconResource;
        this.title = title;
        this.fragmentClass = fragmentClass;
    }
    
    /**
     * Get the item ID.
     * @return The item ID
     */
    public int getItemId() {
        return itemId;
    }
    
    /**
     * Get the icon resource ID.
     * @return The icon resource ID
     */
    public int getIconResource() {
        return iconResource;
    }
    
    /**
     * Get the title.
     * @return The title text
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Get the fragment class.
     * @return The fragment class
     */
    public Class<?> getFragmentClass() {
        return fragmentClass;
    }
    
    /**
     * Set the listener for item selection events.
     * @param listener The listener
     */
    public void setBottomNavItemListener(BottomNavItemListener listener) {
        this.listener = listener;
    }
    
    /**
     * Notify listener of item selection.
     */
    public void notifySelected() {
        if (listener != null) {
            listener.onItemSelected(itemId);
        }
    }
}

