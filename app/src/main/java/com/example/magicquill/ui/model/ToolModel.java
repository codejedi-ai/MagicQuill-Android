package com.example.magicquill.ui.model;

import com.example.magicquill.ui.observer.Observer;
import com.example.magicquill.ui.observer.Subject;

/**
 * Model class for tool state management (MVC Model).
 * Manages the current tool selection and tool states.
 * Implements the Observer pattern as a Subject.
 */
public class ToolModel extends Subject {
    
    public enum ToolType {
        ADD_EDGE,
        REMOVE_EDGE,
        COLOR_BRUSH,
        ERASER,
        SELECT,
        UNDO,
        NONE  // No tool selected
    }
    
    private ToolType currentTool;
    private boolean isMenuExpanded;
    private float menuPositionX;
    private float menuPositionY;
    
    public ToolModel() {
        this.currentTool = ToolType.NONE;
        this.isMenuExpanded = false;
        this.menuPositionX = 0f;
        this.menuPositionY = 0f;
    }
    
    /**
     * Get the currently selected tool.
     * @return The current tool type
     */
    public ToolType getCurrentTool() {
        return currentTool;
    }
    
    /**
     * Set the current tool and notify observers.
     * @param tool The tool to select
     */
    public void setCurrentTool(ToolType tool) {
        if (this.currentTool != tool) {
            this.currentTool = tool;
            notifyObservers(tool);
        }
    }
    
    /**
     * Check if the menu is expanded.
     * @return True if expanded, false otherwise
     */
    public boolean isMenuExpanded() {
        return isMenuExpanded;
    }
    
    /**
     * Set the menu expansion state and notify observers.
     * @param expanded True to expand, false to collapse
     */
    public void setMenuExpanded(boolean expanded) {
        if (this.isMenuExpanded != expanded) {
            this.isMenuExpanded = expanded;
            notifyObservers(expanded);
        }
    }
    
    /**
     * Toggle the menu expansion state.
     */
    public void toggleMenuExpanded() {
        setMenuExpanded(!isMenuExpanded);
    }
    
    /**
     * Get the menu X position.
     * @return The X coordinate
     */
    public float getMenuPositionX() {
        return menuPositionX;
    }
    
    /**
     * Get the menu Y position.
     * @return The Y coordinate
     */
    public float getMenuPositionY() {
        return menuPositionY;
    }
    
    /**
     * Set the menu position and notify observers.
     * @param x The X coordinate
     * @param y The Y coordinate
     */
    public void setMenuPosition(float x, float y) {
        if (this.menuPositionX != x || this.menuPositionY != y) {
            this.menuPositionX = x;
            this.menuPositionY = y;
            notifyObservers(new float[]{x, y});
        }
    }
    
    /**
     * Reset the model to initial state.
     */
    public void reset() {
        this.currentTool = ToolType.NONE;
        this.isMenuExpanded = false;
        notifyObservers();
    }
}

