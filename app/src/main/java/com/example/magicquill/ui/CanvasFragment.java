package com.example.magicquill.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.magicquill.MainActivity;
import com.example.magicquill.R;
import com.example.magicquill.ui.controller.ToolController;
import com.example.magicquill.ui.model.ToolModel;
import com.example.magicquill.ui.view.SideToolBar;
import com.example.magicquill.ui.view.ToolButton;

/**
 * Canvas Fragment using MVC architecture.
 * Coordinates Model, View, and Controller.
 */
public class CanvasFragment extends Fragment {

    // MVC Components
    private ToolModel model;
    private SideToolBar view;
    private ToolController controller;
    
    private static final String TAG = "CanvasFragment";
    
    // Activity result launcher for image selection
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public CanvasFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_canvas, container, false);
        TextView tv = root.findViewById(R.id.canvas_text);

        String text = "(native text unavailable)";
        if (getActivity() instanceof MainActivity) {
            try {
                MainActivity ma = (MainActivity) getActivity();
                text = ma.stringFromJNI();
            } catch (UnsatisfiedLinkError e) {
                // leave fallback text
            }
        }

        tv.setText(text);
        
        // Initialize image picker launcher
        initializeImagePicker();
        
        // Initialize MVC components
        initializeMVC(root);
        
        return root;
    }
    
    /**
     * Initialize the image picker launcher.
     */
    private void initializeImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        handleImageSelected(imageUri);
                    }
                }
            }
        );
    }
    
    /**
     * Handle selected image.
     */
    private void handleImageSelected(Uri imageUri) {
        Log.d(TAG, "Image selected: " + imageUri.toString());
        Toast.makeText(getContext(), "Image selected: " + imageUri.toString(), Toast.LENGTH_SHORT).show();
        // TODO: Process the selected image
    }
    
    /**
     * Open image picker (gallery or file system).
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        // Try to use a picker that supports both gallery and file system
        Intent chooser = Intent.createChooser(intent, "Select Image");
        try {
            imagePickerLauncher.launch(chooser);
        } catch (Exception e) {
            Log.e(TAG, "Error opening image picker", e);
            Toast.makeText(getContext(), "Error opening image picker", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Initialize Model-View-Controller architecture.
     */
    private void initializeMVC(View root) {
        // 1. Create Model
        model = new ToolModel();
        
        // 2. Get View
        view = root.findViewById(R.id.side_tool_bar);
        if (view != null) {
            view.setModel(model);
            view.setVisibility(View.VISIBLE);
            
            // Set upload button listener (upload is a ToolButton like other tools)
            ToolButton uploadButton = view.getUploadButton();
            if (uploadButton != null) {
                uploadButton.setToolButtonListener(new ToolButton.ToolButtonListener() {
                    @Override
                    public void onToolSelected(ToolModel.ToolType tool) {
                        // Not used for upload button
                    }
                    
                    @Override
                    public void onActionClicked() {
                        openImagePicker();
                    }
                });
            }
            
            Log.d(TAG, "SideToolBar initialized and set to visible");
        } else {
            Log.e(TAG, "SideToolBar not found in layout!");
        }
        
        // 3. Create Controller
        controller = new ToolController(model);
        controller.setListener(new ToolController.ToolControllerListener() {
            @Override
            public void onToolChanged(ToolModel.ToolType tool) {
                handleToolChanged(tool);
            }
            
            @Override
            public void onMenuExpansionChanged(boolean expanded) {
                Log.d(TAG, "Menu expanded: " + expanded);
            }
            
            @Override
            public void onMenuPositionChanged(float x, float y) {
                Log.d(TAG, "Menu position: (" + x + ", " + y + ")");
            }
        });
    }
    
    /**
     * Handle tool change from controller.
     */
    private void handleToolChanged(ToolModel.ToolType tool) {
        String toolName = getToolName(tool);
        Log.d(TAG, "Tool changed: " + toolName);
        Toast.makeText(getContext(), "Selected: " + toolName, Toast.LENGTH_SHORT).show();
        
        // TODO: Implement actual tool functionality here
        // This is where you would update the canvas drawing mode, etc.
    }
    
    /**
     * Get human-readable tool name.
     */
    private String getToolName(ToolModel.ToolType tool) {
        switch (tool) {
            case ADD_EDGE:
                return "Add Edge";
            case REMOVE_EDGE:
                return "Remove Edge";
            case COLOR_BRUSH:
                return "Color Brush";
            case ERASER:
                return "Eraser";
            case SELECT:
                return "Select";
            case UNDO:
                return "Undo";
            case NONE:
                return "None";
            default:
                return "Unknown";
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Collapse menu when leaving canvas
        if (controller != null && controller.isMenuExpanded()) {
            controller.collapseMenu();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cleanup
        if (controller != null) {
            controller.cleanup();
        }
        if (model != null) {
            model.clearObservers();
        }
    }
}
