package com.example.magicquill.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.example.magicquill.R;
import com.example.magicquill.ui.model.ToolModel;
import com.example.magicquill.ui.observer.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Side toolbar component (MVC View).
 * Contains a collapsible side panel with tool buttons.
 */
public class SideToolBar extends ViewGroup implements Observer {
    
    private ImageButton toggleButton;
    private ToolButton uploadButton;
    private View topDivider;
    private LinearLayout toolContainer;
    private View bottomDivider;
    private List<ToolButton> toolButtons;
    private ToolModel model;
    
    private boolean isExpanded = false;
    private int sheetWidth = 200; // Width when expanded
    private int collapsedWidth = 48; // Width when collapsed (just arrow button)
    private int currentWidth = collapsedWidth;
    private ValueAnimator widthAnimator;
    
    public SideToolBar(Context context) {
        super(context);
        init();
    }
    
    public SideToolBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public SideToolBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setVisibility(VISIBLE);
        setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
        setElevation(8f);
        
        // Create toggle button (arrow)
        toggleButton = new ImageButton(getContext());
        toggleButton.setImageResource(R.drawable.ic_arrow_right);
        toggleButton.setBackground(null);
        toggleButton.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
        toggleButton.setPadding(8, 8, 8, 8);
        toggleButton.setOnClickListener(v -> toggleSheet());
        addView(toggleButton);
        
        // Create upload button at the top (ToolButton following tool convention)
        uploadButton = new ToolButton(getContext());
        uploadButton.setAction(R.drawable.ic_upload);
        uploadButton.setPadding(8, 8, 8, 8); // Less padding for larger icon
        uploadButton.setScaleType(ImageButton.ScaleType.FIT_CENTER); // Better scaling for larger icon
        uploadButton.setVisibility(GONE);
        addView(uploadButton);
        
        // Create top divider
        topDivider = new View(getContext());
        topDivider.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.divider));
        topDivider.setVisibility(GONE);
        addView(topDivider);
        
        // Create tool container
        toolContainer = new LinearLayout(getContext());
        toolContainer.setOrientation(LinearLayout.VERTICAL);
        toolContainer.setVisibility(GONE);
        addView(toolContainer);
        
        // Create bottom divider
        bottomDivider = new View(getContext());
        bottomDivider.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.divider));
        bottomDivider.setVisibility(GONE);
        addView(bottomDivider);
        
        // Create tool buttons
        toolButtons = new ArrayList<>();
        int[] toolIcons = {
            R.drawable.ic_add_edge,
            R.drawable.ic_remove_edge,
            R.drawable.ic_color_brush,
            R.drawable.ic_eraser,
            R.drawable.ic_select,
            R.drawable.ic_undo
        };
        
        ToolModel.ToolType[] toolTypes = {
            ToolModel.ToolType.ADD_EDGE,
            ToolModel.ToolType.REMOVE_EDGE,
            ToolModel.ToolType.COLOR_BRUSH,
            ToolModel.ToolType.ERASER,
            ToolModel.ToolType.SELECT,
            ToolModel.ToolType.UNDO
        };
        
        // Create ToolButton objects
        for (int i = 0; i < toolIcons.length; i++) {
            ToolButton toolButton = new ToolButton(getContext());
            toolButton.setTool(toolTypes[i], toolIcons[i]);
            toolButton.setToolButtonListener(tool -> {
                if (model != null) {
                    model.setCurrentTool(tool);
                }
            });
            
            // Width and height will be set dynamically in onLayout to be 90% of sidebar width (square)
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, // Will be set in onLayout
                0  // Will be set in onLayout
            );
            params.setMargins(8, 8, 8, 8);
            toolButton.setLayoutParams(params);
            
            toolButtons.add(toolButton);
            toolContainer.addView(toolButton);
        }
    }
    
    /**
     * Get the upload button.
     * @return The upload ToolButton
     */
    public ToolButton getUploadButton() {
        return uploadButton;
    }
    
    /**
     * Set the model to observe.
     */
    public void setModel(ToolModel model) {
        if (this.model != null) {
            this.model.detach(this);
        }
        this.model = model;
        if (this.model != null) {
            this.model.attach(this);
            updateFromModel();
        }
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getWidth();
        int height = getHeight();
        
        if (width > 0 && height > 0) {
            int toggleSize = collapsedWidth;
            int buttonSize = 48;
            int dividerHeight = 1;
            int padding = 8;
            
            // Layout toggle button in the middle vertically, on the outer edge (right side)
            int toggleTop = (height - toggleSize) / 2;
            int toggleLeft = currentWidth - toggleSize;
            toggleButton.layout(toggleLeft, toggleTop, toggleLeft + toggleSize, toggleTop + toggleSize);
            
            if (isExpanded) {
                int containerWidth = currentWidth - toggleSize - padding;
                int currentTop = padding;
                
                // Calculate tool button size: 90% of sidebar width, square
                int toolButtonSize = (int)(containerWidth * 0.9f);
                int toolButtonLeft = (containerWidth - toolButtonSize) / 2; // Center horizontally
                
                // Layout upload button at the top (ToolButton) - same size as tools, square
                uploadButton.setVisibility(VISIBLE);
                uploadButton.layout(toolButtonLeft, currentTop, toolButtonLeft + toolButtonSize, currentTop + toolButtonSize);
                currentTop += toolButtonSize + padding;
                
                // Layout top divider
                topDivider.setVisibility(VISIBLE);
                topDivider.layout(0, currentTop, containerWidth, currentTop + dividerHeight);
                currentTop += dividerHeight + padding;
                
                // Layout tool container (6 tools) - tools are 90% width, square
                toolContainer.setVisibility(VISIBLE);
                int toolContainerHeight = height - currentTop - dividerHeight - padding - padding;
                toolContainer.layout(0, currentTop, containerWidth, currentTop + toolContainerHeight);
                
                // Update tool button sizes to be square (90% of container width)
                for (ToolButton toolButton : toolButtons) {
                    LinearLayout.LayoutParams toolParams = (LinearLayout.LayoutParams) toolButton.getLayoutParams();
                    toolParams.width = toolButtonSize;
                    toolParams.height = toolButtonSize;
                    toolButton.setLayoutParams(toolParams);
                }
                
                currentTop += toolContainerHeight + padding;
                
                // Layout bottom divider
                bottomDivider.setVisibility(VISIBLE);
                bottomDivider.layout(0, currentTop, containerWidth, currentTop + dividerHeight);
            } else {
                uploadButton.setVisibility(GONE);
                topDivider.setVisibility(GONE);
                bottomDivider.setVisibility(GONE);
            }
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        
        // Measure toggle button
        int toggleSize = collapsedWidth;
        toggleButton.measure(
            MeasureSpec.makeMeasureSpec(toggleSize, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(toggleSize, MeasureSpec.EXACTLY)
        );
        
        if (isExpanded) {
            int containerWidth = currentWidth - toggleSize - 8;
            // Tool button size: 90% of container width, square
            int toolButtonSize = (int)(containerWidth * 0.9f);
            int dividerHeight = 1;
            int padding = 8;
            
            // Measure upload button (ToolButton at top) - same size as tools, square
            uploadButton.measure(
                MeasureSpec.makeMeasureSpec(toolButtonSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(toolButtonSize, MeasureSpec.EXACTLY)
            );
            
            // Measure dividers
            topDivider.measure(
                MeasureSpec.makeMeasureSpec(containerWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dividerHeight, MeasureSpec.EXACTLY)
            );
            bottomDivider.measure(
                MeasureSpec.makeMeasureSpec(containerWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dividerHeight, MeasureSpec.EXACTLY)
            );
            
            // Measure tool container (6 tools) - all tools are square (90% width)
            int availableHeight = height - toolButtonSize - dividerHeight - dividerHeight - (padding * 4);
            toolContainer.measure(
                MeasureSpec.makeMeasureSpec(containerWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.AT_MOST)
            );
            
            // Measure each tool button to be square (90% of container width)
            for (ToolButton toolButton : toolButtons) {
                toolButton.measure(
                    MeasureSpec.makeMeasureSpec(toolButtonSize, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(toolButtonSize, MeasureSpec.EXACTLY)
                );
            }
        }
        
        setMeasuredDimension(currentWidth, height);
    }
    
    private void toggleSheet() {
        if (model != null) {
            model.toggleMenuExpanded();
        }
    }
    
    private void expand() {
        if (isExpanded) return;
        isExpanded = true;
        
        uploadButton.setVisibility(VISIBLE);
        topDivider.setVisibility(VISIBLE);
        toolContainer.setVisibility(VISIBLE);
        bottomDivider.setVisibility(VISIBLE);
        
        if (widthAnimator != null && widthAnimator.isRunning()) {
            widthAnimator.cancel();
        }
        
        widthAnimator = ValueAnimator.ofInt(currentWidth, sheetWidth);
        widthAnimator.setDuration(300);
        widthAnimator.setInterpolator(new DecelerateInterpolator());
        widthAnimator.addUpdateListener(animation -> {
            currentWidth = (Integer) animation.getAnimatedValue();
            requestLayout();
        });
        widthAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                toggleButton.setImageResource(R.drawable.ic_arrow_left);
            }
        });
        widthAnimator.start();
    }
    
    private void collapse() {
        if (!isExpanded) return;
        isExpanded = false;
        
        if (widthAnimator != null && widthAnimator.isRunning()) {
            widthAnimator.cancel();
        }
        
        widthAnimator = ValueAnimator.ofInt(currentWidth, collapsedWidth);
        widthAnimator.setDuration(300);
        widthAnimator.setInterpolator(new DecelerateInterpolator());
        widthAnimator.addUpdateListener(animation -> {
            currentWidth = (Integer) animation.getAnimatedValue();
            requestLayout();
        });
        widthAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                uploadButton.setVisibility(GONE);
                topDivider.setVisibility(GONE);
                toolContainer.setVisibility(GONE);
                bottomDivider.setVisibility(GONE);
                toggleButton.setImageResource(R.drawable.ic_arrow_right);
            }
        });
        widthAnimator.start();
    }
    
    private void updateFromModel() {
        if (model == null) return;
        
        boolean shouldBeExpanded = model.isMenuExpanded();
        if (shouldBeExpanded != isExpanded) {
            if (shouldBeExpanded) {
                expand();
            } else {
                collapse();
            }
        }
    }
    
    @Override
    public void update(Object data) {
        post(() -> updateFromModel());
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (widthAnimator != null) {
            widthAnimator.cancel();
        }
        if (model != null) {
            model.detach(this);
        }
    }
    
    /**
     * Get all tool buttons.
     * @return List of tool buttons
     */
    public List<ToolButton> getToolButtons() {
        return toolButtons;
    }
    
    /**
     * Get a specific tool button by tool type.
     * @param toolType The tool type
     * @return The tool button, or null if not found
     */
    public ToolButton getToolButton(ToolModel.ToolType toolType) {
        for (ToolButton button : toolButtons) {
            if (button.getToolType() == toolType) {
                return button;
            }
        }
        return null;
    }
}

