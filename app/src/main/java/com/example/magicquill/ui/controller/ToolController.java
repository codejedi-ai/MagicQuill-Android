package com.example.magicquill.ui.controller;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.magicquill.ui.model.ToolModel;
import com.example.magicquill.ui.observer.Observer;

/**
 * Controller class for tool management (MVC Controller).
 * Handles user interactions and updates the model.
 * Acts as an Observer to respond to model changes.
 */
public class ToolController implements Observer {
    
    private static final String TAG = "ToolController";
    
    private ToolModel model;
    private ToolControllerListener listener;
    
    public interface ToolControllerListener {
        void onToolChanged(ToolModel.ToolType tool);
        void onMenuExpansionChanged(boolean expanded);
        void onMenuPositionChanged(float x, float y);
    }
    
    public ToolController(ToolModel model) {
        this.model = model;
        // Register as observer to model changes
        this.model.attach(this);
    }
    
    /**
     * Set the listener for controller events.
     * @param listener The listener to set
     */
    public void setListener(ToolControllerListener listener) {
        this.listener = listener;
    }
    
    /**
     * Handle tool selection.
     * @param tool The tool to select
     */
    public void selectTool(ToolModel.ToolType tool) {
        Log.d(TAG, "Tool selected: " + tool);
        model.setCurrentTool(tool);
        
        // Collapse menu after tool selection
        if (model.isMenuExpanded()) {
            model.setMenuExpanded(false);
        }
    }
    
    /**
     * Handle menu toggle.
     */
    public void toggleMenu() {
        model.toggleMenuExpanded();
    }
    
    /**
     * Handle menu expansion.
     */
    public void expandMenu() {
        model.setMenuExpanded(true);
    }
    
    /**
     * Handle menu collapse.
     */
    public void collapseMenu() {
        model.setMenuExpanded(false);
    }
    
    /**
     * Handle menu drag.
     * @param x The new X position
     * @param y The new Y position
     */
    public void dragMenu(float x, float y) {
        model.setMenuPosition(x, y);
    }
    
    /**
     * Handle touch events for dragging.
     * @param event The motion event
     * @param viewWidth The width of the view
     * @param viewHeight The height of the view
     * @param buttonSize The size of the button
     * @return True if the event was handled
     */
    public boolean handleTouchEvent(MotionEvent event, float viewWidth, float viewHeight, int buttonSize) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (!model.isMenuExpanded()) {
                    // Constrain to screen bounds
                    float newX = Math.max(buttonSize / 2f, 
                                         Math.min(x, viewWidth - buttonSize / 2f));
                    float newY = Math.max(buttonSize / 2f, 
                                         Math.min(y, viewHeight - buttonSize / 2f));
                    dragMenu(newX, newY);
                    return true;
                }
                break;
        }
        return false;
    }
    
    /**
     * Get the current tool from the model.
     * @return The current tool type
     */
    public ToolModel.ToolType getCurrentTool() {
        return model.getCurrentTool();
    }
    
    /**
     * Check if menu is expanded.
     * @return True if expanded
     */
    public boolean isMenuExpanded() {
        return model.isMenuExpanded();
    }
    
    /**
     * Cleanup resources.
     */
    public void cleanup() {
        if (model != null) {
            model.detach(this);
        }
    }
    
    @Override
    public void update(Object data) {
        // Respond to model changes
        if (data instanceof ToolModel.ToolType) {
            ToolModel.ToolType tool = (ToolModel.ToolType) data;
            if (listener != null) {
                listener.onToolChanged(tool);
            }
        } else if (data instanceof Boolean) {
            boolean expanded = (Boolean) data;
            if (listener != null) {
                listener.onMenuExpansionChanged(expanded);
            }
        } else if (data instanceof float[]) {
            float[] position = (float[]) data;
            if (position.length >= 2 && listener != null) {
                listener.onMenuPositionChanged(position[0], position[1]);
            }
        } else {
            // Generic update
            if (listener != null) {
                listener.onToolChanged(model.getCurrentTool());
                listener.onMenuExpansionChanged(model.isMenuExpanded());
            }
        }
    }
}

