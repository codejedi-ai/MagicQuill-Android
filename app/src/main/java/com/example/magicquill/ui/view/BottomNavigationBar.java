package com.example.magicquill.ui.view;

import android.content.Context;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * Bottom navigation bar component.
 * Manages the bottom navigation bar and its items.
 */
public class BottomNavigationBar {
    
    private BottomNavigationView bottomNavView;
    private List<BottomNavItem> navItems;
    private NavController navController;
    
    /**
     * Constructor for the bottom navigation bar.
     * @param bottomNavView The BottomNavigationView from the layout
     */
    public BottomNavigationBar(BottomNavigationView bottomNavView) {
        this.bottomNavView = bottomNavView;
        this.navItems = new ArrayList<>();
    }
    
    /**
     * Set the navigation controller.
     * @param navController The NavController to use for navigation
     */
    public void setNavController(NavController navController) {
        this.navController = navController;
        if (navController != null && bottomNavView != null) {
            NavigationUI.setupWithNavController(bottomNavView, navController);
        }
    }
    
    /**
     * Add a navigation item to the bar.
     * @param item The BottomNavItem to add
     */
    public void addNavItem(BottomNavItem item) {
        if (item != null && !navItems.contains(item)) {
            navItems.add(item);
        }
    }
    
    /**
     * Get all navigation items.
     * @return List of BottomNavItem objects
     */
    public List<BottomNavItem> getNavItems() {
        return navItems;
    }
    
    /**
     * Get a navigation item by its ID.
     * @param itemId The item ID
     * @return The BottomNavItem, or null if not found
     */
    public BottomNavItem getNavItem(int itemId) {
        for (BottomNavItem item : navItems) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Get the BottomNavigationView.
     * @return The BottomNavigationView
     */
    public BottomNavigationView getBottomNavView() {
        return bottomNavView;
    }
    
    /**
     * Set up bottom padding for a view to account for the bottom navigation bar height.
     * @param view The view to add padding to
     */
    public void setupBottomPadding(View view) {
        if (bottomNavView != null && view != null) {
            bottomNavView.post(() -> {
                int bottomNavHeight = bottomNavView.getHeight();
                view.setPadding(0, 0, 0, bottomNavHeight);
            });
        }
    }
    
    /**
     * Get the height of the bottom navigation bar.
     * @return The height in pixels, or 0 if not measured yet
     */
    public int getHeight() {
        if (bottomNavView != null) {
            return bottomNavView.getHeight();
        }
        return 0;
    }
}

