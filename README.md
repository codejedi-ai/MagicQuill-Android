# MagicQuill Gradio UI - Complete User Interaction Guide

This document catalogs all ways users can interact with the MagicQuill application through the Gradio web interface. This serves as a reference for replicating the full user experience in mobile applications.

---

## Table of Contents

1. [Canvas Drawing Tools](#canvas-drawing-tools)
2. [Canvas Controls](#canvas-controls)
3. [Generation Parameters](#generation-parameters)
4. [Workflow Interactions](#workflow-interactions)
5. [Visual Feedback & States](#visual-feedback--states)

---

## Canvas Drawing Tools

The MagicQuill canvas component provides 6 primary tools for drawing and editing:

### 1. **Load/Upload Image** 📁
- **Icon**: Upload/Image icon
- **Action**: Opens file picker to upload a base image
- **Supported Formats**: JPEG, PNG, WebP
- **Behavior**: 
  - Sets the `original_image` layer
  - Canvas resizes to match image dimensions
  - Clears all previous drawings
- **Mobile Equivalent**: Image picker from gallery or camera

### 2. **Add Edge Brush** ➕
- **Icon**: Brush with plus/add symbol
- **Purpose**: Draw outlines/edges that will be added to the image
- **Layer**: Writes to `add_edge_image` (alpha channel mask)
- **Behavior**:
  - Draws black strokes on transparent background
  - Alpha channel indicates where edges should be added
  - Disabled when Color Brush mode is active
- **Use Case**: "I want to add a deer here" → draw deer outline
- **Mobile Equivalent**: Pen tool drawing on transparent layer

### 3. **Remove Edge Brush** ➖
- **Icon**: Brush with minus/remove symbol
- **Purpose**: Draw outlines/edges that will be removed from the image
- **Layer**: Writes to `remove_edge_image` (alpha channel mask)
- **Behavior**:
  - Draws black strokes on transparent background
  - Alpha channel indicates where edges should be removed
  - Disabled when Color Brush mode is active
- **Use Case**: "Remove this hat" → draw outline around hat
- **Mobile Equivalent**: Pen tool drawing on separate remove layer

### 4. **Color Brush** 🎨
- **Icon**: Paint brush icon
- **Purpose**: Apply color tints/overlays to guide color generation
- **Layer**: Writes to `add_color_image` (full RGB image)
- **Behavior**:
  - Draws with selected color and opacity
  - Full RGB image (not just mask)
  - Disables Add/Remove Edge brushes when active
  - Mutually exclusive with edge brushes
- **Use Case**: "Make the flowers blue" → paint blue over flowers
- **Mobile Equivalent**: Color brush with color picker and opacity control

### 5. **Eraser** 🧹
- **Icon**: Eraser icon
- **Purpose**: Define the inpainting region (area to be regenerated)
- **Layer**: Writes to `total_mask` (alpha channel mask)
- **Behavior**:
  - White pixels = area to regenerate
  - Black/transparent = area to preserve
  - Everything outside the mask is preserved
- **Use Case**: "Regenerate this area" → erase/white out the region
- **Mobile Equivalent**: Eraser tool that creates white mask areas

### 6. **Select Tool** 🖱️
- **Icon**: Cursor/pointer icon
- **Purpose**: Select, move, rotate, and resize drawn strokes
- **Behavior**:
  - Click to select individual strokes
  - Drag to move strokes
  - Rotate handles to rotate strokes
  - Resize handles to scale strokes
  - Similar to PowerPoint object manipulation
- **Use Case**: "Move that stroke a bit to the left"
- **Mobile Equivalent**: Touch selection with drag/rotate/scale gestures

---

## Canvas Controls

### Stroke Width Slider
- **Location**: Toolbar, below brush tools
- **Range**: 1 to maximum (typically 1-100 pixels)
- **Purpose**: Adjust brush stroke thickness
- **Behavior**: 
  - Real-time preview shows current value
  - Affects all brush tools (Add Edge, Remove Edge, Color, Eraser)
  - Visual indicator displays current size
- **Mobile Equivalent**: Slider control in toolbar

### Color Picker (Color Brush Only)
- **Location**: Toolbar, appears when Color Brush is selected
- **Type**: HTML color input (`<input type="color">`)
- **Purpose**: Select brush color for Color Brush tool
- **Behavior**:
  - Opens native color picker
  - Updates brush color immediately
  - Only visible when Color Brush is active
- **Mobile Equivalent**: Native color picker dialog

### Alpha/Transparency Slider (Color Brush Only)
- **Location**: Toolbar, next to color picker when Color Brush is selected
- **Range**: 0.1 to 1.0 (10% to 100% opacity)
- **Step**: 0.05 (5% increments)
- **Purpose**: Control color brush opacity
- **Behavior**:
  - Lower values = more transparent strokes
  - Higher values = more opaque strokes
  - Only visible when Color Brush is active
- **Mobile Equivalent**: Opacity slider control

### Undo Button ↩️
- **Icon**: Undo/backward arrow icon
- **Location**: Toolbar, in history panel
- **Keyboard Shortcut**: `Ctrl+Z` (Windows/Linux) or `Cmd+Z` (Mac)
- **Purpose**: Undo last drawing action
- **Behavior**:
  - Maintains history stack per layer
  - Can undo multiple actions
  - Disabled when no actions to undo
  - Visual indicator shows disabled state
- **Mobile Equivalent**: Undo button with history stack

### Redo Button ↪️
- **Icon**: Redo/forward arrow icon
- **Location**: Toolbar, in history panel
- **Keyboard Shortcut**: `Ctrl+Y` (Windows/Linux) or `Cmd+Shift+Z` (Mac)
- **Purpose**: Redo last undone action
- **Behavior**:
  - Maintains redo stack
  - Can redo multiple actions
  - Disabled when no actions to redo
  - Visual indicator shows disabled state
- **Mobile Equivalent**: Redo button with redo stack

### Download Button 💾
- **Icon**: Download icon
- **Location**: Toolbar, in download box
- **Purpose**: Download current canvas state
- **Behavior**:
  - Downloads composite image (all layers combined)
  - Saves as PNG file
  - Includes all drawings and original image
- **Mobile Equivalent**: Share/save functionality

### Prompt Input Box
- **Location**: Top bar, above canvas
- **Type**: Text input field
- **Purpose**: Enter or edit the generation prompt
- **Behavior**:
  - Can be manually typed
  - Can be auto-filled by "Guess Prompt" feature
  - Updates `from_backend.prompt` value
  - Required for generation
- **Mobile Equivalent**: Text input field in UI

### Magic Wand (Guess Prompt) ✨
- **Icon**: Magic wand/sparkle icon
- **Location**: Top bar, next to prompt input
- **Purpose**: Automatically generate prompt suggestions using LLaVA AI
- **Behavior**:
  - Analyzes current canvas state (original image + drawings)
  - Calls `/magic_quill/guess_prompt` API endpoint
  - Fills prompt input with suggestions
  - Shows loading state while processing
  - Flashing animation indicates processing
- **API Call**: `POST /magic_quill/guess_prompt`
- **Mobile Equivalent**: "Auto-suggest prompt" button

### Eye Icon (Toggle Visibility) 👁️
- **Icon**: Eye icon
- **Location**: Toolbar
- **Purpose**: Temporarily hide/show brush strokes
- **Behavior**:
  - Toggles visibility of drawn strokes
  - Does not delete strokes
  - Helps preview original image without strokes
  - Toggle on/off to show/hide
- **Mobile Equivalent**: Layer visibility toggle

### Delete/Trash Icon 🗑️
- **Icon**: Trash/delete icon
- **Location**: Toolbar
- **Purpose**: Delete selected stroke
- **Behavior**:
  - Only works when stroke is selected (Select tool)
  - Permanently removes the stroke
  - Cannot be undone (different from general undo)
- **Mobile Equivalent**: Delete button when object selected

### Accept/Discard Icons (Post-Generation) ✅❌
- **Icons**: Checkmark (accept) and X (discard)
- **Location**: Appear after image generation completes
- **Purpose**: Accept or reject generated result
- **Behavior**:
  - **Accept (✅)**: Keeps generated image, allows continued editing
  - **Discard (❌)**: Rejects result, returns to previous state
  - Only visible after generation completes
- **Mobile Equivalent**: Accept/reject buttons after generation

---

## Generation Parameters

Located in the collapsible "parameters" accordion panel (right side of UI):

### Base Model Name (Dropdown)
- **Type**: Dropdown/Select
- **Options**: Dynamically loaded from `models/checkpoints/` directory
- **Default**: `SD1.5/realisticVisionV60B1_v51VAE.safetensors`
- **Purpose**: Select the Stable Diffusion checkpoint model
- **Available Models**:
  - `SD1.5/realisticVisionV60B1_v51VAE.safetensors` - Realistic (default)
  - `SD1.5/DreamShaper.safetensors` - Fantasy
  - `SD1.5/majicMIX_realistic.safetensors` - Portraits
  - `SD1.5/MeinaMix.safetensors` - Anime
  - `SD1.5/ghostmix_v20Bakedvae.safetensors` - Anime alternative
- **Mobile Equivalent**: Model selector dropdown

### Auto Save (Checkbox)
- **Type**: Checkbox
- **Default**: `false` (unchecked)
- **Purpose**: Automatically save generated images to `output/` directory
- **Behavior**:
  - When enabled, saves every generated image
  - Filename format: `magicquill_YYYYMMDD_HHMMSS.png`
  - Saves to `output/` folder in server directory
- **Mobile Equivalent**: Auto-save toggle in settings

### Resolution (Slider)
- **Type**: Slider
- **Range**: 256 to 2048 pixels
- **Step**: 64 pixels
- **Default**: 512 pixels
- **Purpose**: Set target resolution for image processing
- **Important**: Should be set **before** uploading image
- **Behavior**:
  - Affects `process_background_img` endpoint
  - Resizes smaller dimension to this value
  - Maintains aspect ratio
- **Mobile Equivalent**: Resolution slider (set before upload)

### Negative Prompt (Textbox)
- **Type**: Multi-line text input
- **Default**: Empty string
- **Purpose**: Specify what the model should avoid generating
- **Examples**: 
  - "blurry, low quality, distorted"
  - "text, watermark, signature"
- **Mobile Equivalent**: Text input field

### Fine Edge (Radio Buttons)
- **Type**: Radio button group
- **Options**: `"enable"` | `"disable"`
- **Default**: `"disable"`
- **Purpose**: Enable fine edge control for more precise edge manipulation
- **Mobile Equivalent**: Toggle switch

### Grow Size (Slider)
- **Type**: Slider
- **Range**: 0 to 100 pixels
- **Step**: 1 pixel
- **Default**: 15 pixels
- **Purpose**: Expand/reduce the pixel range affected around brush strokes
- **Behavior**:
  - Higher values = larger influence area around strokes
  - Lower values = tighter control
- **Mobile Equivalent**: Slider control

### Edge Strength (Slider)
- **Type**: Slider
- **Range**: 0.0 to 5.0
- **Step**: 0.01
- **Default**: 0.55
- **Purpose**: Control strength of add/subtract edge brush effects
- **Behavior**:
  - Higher = stronger edge control (good for confident artists)
  - Lower = softer edge control (for less precise drawings)
- **Mobile Equivalent**: Slider control

### Color Strength (Slider)
- **Type**: Slider
- **Range**: 0.0 to 5.0
- **Step**: 0.01
- **Default**: 0.55
- **Purpose**: Control strength of color brush effects
- **Behavior**:
  - Higher = stronger color influence
  - Lower = subtle color hints
- **Mobile Equivalent**: Slider control

### Inpaint Strength (Slider)
- **Type**: Slider
- **Range**: 0.0 to 5.0
- **Step**: 0.01
- **Default**: 1.0
- **Purpose**: Control inpainting strength (how much the masked area is regenerated)
- **Behavior**:
  - Higher = more aggressive regeneration
  - Lower = subtle changes
- **Mobile Equivalent**: Slider control

### Seed (Number Input)
- **Type**: Number input (integer)
- **Default**: `-1` (random)
- **Purpose**: Set random seed for reproducible results
- **Behavior**:
  - `-1` = random seed (server generates)
  - Any positive integer = fixed seed (reproducible)
  - Same seed + same inputs = same output
- **Mobile Equivalent**: Number input with "Random" option

### Steps (Slider)
- **Type**: Slider
- **Range**: 1 to 50
- **Step**: 1
- **Default**: 20
- **Purpose**: Number of diffusion sampling steps
- **Behavior**:
  - More steps = better quality but slower
  - Fewer steps = faster but lower quality
  - Typical range: 20-30 steps
- **Mobile Equivalent**: Slider control

### CFG (Classifier-Free Guidance) (Slider)
- **Type**: Slider
- **Range**: 0.0 to 100.0
- **Step**: 0.1
- **Default**: 5.0
- **Purpose**: How strongly the model follows the prompt
- **Behavior**:
  - Higher = more prompt adherence (but can be over-saturated)
  - Lower = more creative freedom
  - Typical range: 4.0-7.0
- **Mobile Equivalent**: Slider control

### Sampler Name (Dropdown)
- **Type**: Dropdown/Select
- **Options**: 
  - `"euler"`, `"euler_ancestral"` (default)
  - `"heun"`, `"heunpp2"`
  - `"dpm_2"`, `"dpm_2_ancestral"`
  - `"lms"`, `"dpm_fast"`, `"dpm_adaptive"`
  - `"dpmpp_2s_ancestral"`, `"dpmpp_sde"`, `"dpmpp_sde_gpu"`
  - `"dpmpp_2m"`, `"dpmpp_2m_sde"`, `"dpmpp_2m_sde_gpu"`
  - `"dpmpp_3m_sde"`, `"dpmpp_3m_sde_gpu"`
  - `"ddpm"`, `"lcm"`, `"ddim"`
  - `"uni_pc"`, `"uni_pc_bh2"`
- **Default**: `"euler_ancestral"`
- **Purpose**: Select diffusion sampling algorithm
- **Mobile Equivalent**: Dropdown selector

### Scheduler (Dropdown)
- **Type**: Dropdown/Select
- **Options**: 
  - `"normal"`, `"karras"` (default)
  - `"exponential"`, `"sgm_uniform"`
  - `"simple"`, `"ddim_uniform"`
- **Default**: `"karras"`
- **Purpose**: Select noise scheduling algorithm
- **Mobile Equivalent**: Dropdown selector

### Run Button 🚀
- **Type**: Primary button
- **Location**: Left column, above parameters panel
- **Purpose**: Trigger image generation
- **Behavior**:
  - Collects all canvas layers and parameters
  - Calls `generate_image_handler` function
  - Shows loading state during generation
  - Updates canvas with generated image
  - Enables Accept/Discard buttons after completion
- **API Equivalent**: `POST /magic_quill/generate`
- **Mobile Equivalent**: "Generate" button

---

## Workflow Interactions

### Complete User Workflow

1. **Upload Base Image**
   - Click Load/Upload icon
   - Select image file
   - Image appears on canvas

2. **Set Resolution** (Optional, but recommended)
   - Open parameters panel
   - Adjust Resolution slider
   - Set before drawing for best results

3. **Select Drawing Tool**
   - Choose from: Add Edge, Remove Edge, Color Brush, or Eraser
   - Adjust stroke width slider
   - (If Color Brush) Select color and opacity

4. **Draw on Canvas**
   - Click and drag to draw strokes
   - Use Undo/Redo to correct mistakes
   - Use Select tool to move/resize strokes

5. **Get Prompt Suggestions** (Optional)
   - Click Magic Wand icon
   - Wait for AI analysis (5-15 seconds)
   - Prompt auto-fills with suggestions
   - Edit prompt manually if needed

6. **Configure Generation Parameters** (Optional)
   - Open parameters panel
   - Adjust model, strength, steps, etc.
   - Or use defaults

7. **Generate Image**
   - Click Run button
   - Wait for generation (20-60 seconds)
   - Generated image appears on canvas

8. **Accept or Discard Result**
   - Click ✅ to keep and continue editing
   - Click ❌ to discard and try again

9. **Download Result** (Optional)
   - Click Download icon
   - Saves composite image to downloads

---

## Visual Feedback & States

### Loading States

1. **Model Loading** (Initial)
   - Spinning icon in bottom-left corner
   - Appears when models are initializing
   - Wait for it to disappear before using

2. **Prompt Guessing** (Magic Wand)
   - Magic wand icon flashes/animated
   - Indicates LLaVA model is processing
   - Typically 5-15 seconds

3. **Image Generation** (Run Button)
   - Button shows loading state
   - Canvas may show progress indicator
   - Typically 20-60 seconds

### Tool States

1. **Active Tool**
   - Tool icon highlighted/colored
   - Indicates currently selected tool

2. **Disabled Tool**
   - Tool icon grayed out
   - Cannot be clicked
   - Example: Edge brushes disabled when Color Brush active

3. **Undo/Redo Disabled**
   - Icons grayed out
   - No actions in history to undo/redo

### Mode Restrictions

- **Edge Mode**: Color Brush is disabled
- **Color Mode**: Add Edge and Remove Edge brushes are disabled
- **Mutual Exclusivity**: Cannot use edge brushes and color brush simultaneously

---

## Keyboard Shortcuts

| Action | Windows/Linux | Mac |
|--------|---------------|-----|
| Undo | `Ctrl+Z` | `Cmd+Z` |
| Redo | `Ctrl+Y` | `Cmd+Shift+Z` |

---

## Summary: All Interactive Elements

### Canvas Tools (6)
1. Load/Upload Image
2. Add Edge Brush
3. Remove Edge Brush
4. Color Brush
5. Eraser
6. Select Tool

### Canvas Controls (8)
1. Stroke Width Slider
2. Color Picker (Color Brush only)
3. Alpha/Transparency Slider (Color Brush only)
4. Undo Button
5. Redo Button
6. Download Button
7. Prompt Input Box
8. Magic Wand (Guess Prompt)

### Canvas Actions (3)
1. Eye Icon (Toggle Visibility)
2. Delete Icon (Delete Selected)
3. Accept/Discard Icons (Post-Generation)

### Generation Controls (14)
1. Run Button
2. Base Model Name Dropdown
3. Auto Save Checkbox
4. Resolution Slider
5. Negative Prompt Textbox
6. Fine Edge Radio
7. Grow Size Slider
8. Edge Strength Slider
9. Color Strength Slider
10. Inpaint Strength Slider
11. Seed Number Input
12. Steps Slider
13. CFG Slider
14. Sampler Name Dropdown
15. Scheduler Dropdown

**Total Interactive Elements: 31**

---

## Mobile App Equivalents

For mobile app developers, here's how each interaction maps:

| Gradio UI Element | Mobile Equivalent |
|-------------------|-------------------|
| Load/Upload Image | Image picker (gallery/camera) |
| Drawing Tools | Toolbar with icon buttons |
| Stroke Width | Slider in toolbar |
| Color Picker | Native color picker dialog |
| Opacity Slider | Slider control |
| Undo/Redo | History stack with buttons |
| Prompt Input | Text field |
| Magic Wand | Button calling API |
| Parameters Panel | Settings/Advanced panel |
| Run Button | Primary action button |
| Accept/Discard | Action buttons after generation |

All these interactions should be replicated in mobile apps to provide feature parity with the web interface.

