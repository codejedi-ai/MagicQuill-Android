package com.example.magicquill.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.example.magicquill.R;
import com.example.magicquill.ui.model.ToolModel;

/**
 * Individual tool button component.
 * Represents a single tool in the sidebar.
 */
public class ToolButton extends ImageButton {
    
    private ToolModel.ToolType toolType;
    private int iconResource;
    private ToolButtonListener listener;
    private boolean isActionButton = false; // For buttons that aren't tools (like upload)
    
    public interface ToolButtonListener {
        void onToolSelected(ToolModel.ToolType tool);
        default void onActionClicked() {
            // Default implementation for action buttons
        }
    }
    
    public ToolButton(Context context) {
        super(context);
        init();
    }
    
    public ToolButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ToolButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setBackgroundResource(R.drawable.tool_button_circle);
        setScaleType(ScaleType.CENTER_INSIDE);
        setPadding(12, 12, 12, 12);
        setAdjustViewBounds(true);
        
        setOnClickListener(v -> {
            if (listener != null) {
                if (isActionButton) {
                    listener.onActionClicked();
                } else if (toolType != null) {
                    listener.onToolSelected(toolType);
                }
            }
        });
    }
    
    /**
     * Set the tool type and icon for this button.
     * @param toolType The tool type (can be null for action buttons)
     * @param iconResource The icon resource ID
     */
    public void setTool(ToolModel.ToolType toolType, int iconResource) {
        this.toolType = toolType;
        this.iconResource = iconResource;
        this.isActionButton = (toolType == null);
        setImageResource(iconResource);
    }
    
    /**
     * Set this as an action button (not a tool).
     * @param iconResource The icon resource ID
     */
    public void setAction(int iconResource) {
        setTool(null, iconResource);
    }
    
    /**
     * Get the tool type associated with this button.
     * @return The tool type
     */
    public ToolModel.ToolType getToolType() {
        return toolType;
    }
    
    /**
     * Get the icon resource ID.
     * @return The icon resource ID
     */
    public int getIconResource() {
        return iconResource;
    }
    
    /**
     * Set the listener for tool selection events.
     * @param listener The listener
     */
    public void setToolButtonListener(ToolButtonListener listener) {
        this.listener = listener;
    }
}

