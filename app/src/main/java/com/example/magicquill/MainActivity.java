package com.example.magicquill;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.magicquill.ui.view.BottomNavigationBar;
import com.example.magicquill.ui.view.BottomNavItem;
import com.example.magicquill.ui.CanvasFragment;
import com.example.magicquill.ui.Screen3Fragment;
import com.example.magicquill.ui.Screen4Fragment;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("magicquill");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(getResources().getColor(android.R.color.black, getTheme()));
            toolbar.setTitle("Magic Quill"); // Set title text (but hidden)
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide title visually
            }
        }

        NavController navController = null;
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        } else {
            try {
                navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            } catch (Exception e) {
                navController = null;
            }
        }

        // Create BottomNavigationBar object
        BottomNavigationView bottomNavView = findViewById(R.id.bottom_navigation);
        BottomNavigationBar bottomNavBar = null;
        if (bottomNavView != null) {
            bottomNavBar = new BottomNavigationBar(bottomNavView);
            
            // Create BottomNavItem objects for each screen
            bottomNavBar.addNavItem(new BottomNavItem(
                R.id.canvas_fragment,
                android.R.drawable.ic_menu_gallery,
                "Canvas",
                CanvasFragment.class
            ));
            
            bottomNavBar.addNavItem(new BottomNavItem(
                R.id.screen3_fragment,
                android.R.drawable.ic_menu_info_details,
                "Screen3",
                Screen3Fragment.class
            ));
            
            bottomNavBar.addNavItem(new BottomNavItem(
                R.id.screen4_fragment,
                android.R.drawable.ic_menu_manage,
                "Screen4",
                Screen4Fragment.class
            ));
            
            // Set up navigation controller
            if (navController != null) {
                bottomNavBar.setNavController(navController);
            }
            
            // Add bottom padding to fragment container to account for bottom navigation
            View fragmentContainer = findViewById(R.id.nav_host_fragment);
            if (fragmentContainer != null) {
                bottomNavBar.setupBottomPadding(fragmentContainer);
            }
        }
    }

    // Native method implemented in C/C++
    public native String stringFromJNI();
}