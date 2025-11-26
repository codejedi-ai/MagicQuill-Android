package com.example.magicquill.ui.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Subject class for the Observer pattern.
 * Manages a list of observers and provides methods to notify them of changes.
 */
public class Subject {
    private List<Observer> observers;
    
    public Subject() {
        this.observers = new ArrayList<>();
    }
    
    /**
     * Attach an observer to this subject.
     * @param observer The observer to attach
     */
    public void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    /**
     * Detach an observer from this subject.
     * @param observer The observer to detach
     */
    public void detach(Observer observer) {
        observers.remove(observer);
    }
    
    /**
     * Notify all attached observers of a change.
     * @param data The data to pass to observers (can be null)
     */
    public void notifyObservers(Object data) {
        for (Observer observer : observers) {
            observer.update(data);
        }
    }
    
    /**
     * Notify all attached observers without data.
     */
    public void notifyObservers() {
        notifyObservers(null);
    }
    
    /**
     * Get the number of attached observers.
     * @return The count of observers
     */
    public int getObserverCount() {
        return observers.size();
    }
    
    /**
     * Clear all observers.
     */
    public void clearObservers() {
        observers.clear();
    }
}

