package com.example.magicquill.ui.observer;

/**
 * Observer interface for the Observer pattern.
 * Objects that want to be notified of changes should implement this interface.
 */
public interface Observer {
    /**
     * Called when the subject notifies observers of a change.
     * @param data The data passed from the subject (can be null)
     */
    void update(Object data);
}

