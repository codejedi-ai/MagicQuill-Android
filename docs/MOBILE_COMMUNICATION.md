# MagicQuill Mobile API Communication Guide

Complete guide for building mobile applications (Kotlin/Android and iOS/Swift) that replicate all Gradio UI functionality through the FastAPI backend. This document maps every Gradio UI interaction to its API equivalent.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Complete API Endpoint Reference](#complete-api-endpoint-reference)
3. [Gradio UI → API Mapping](#gradio-ui--api-mapping)
4. [Mobile Implementation Workflows](#mobile-implementation-workflows)
5. [Code Examples](#code-examples)
6. [Error Handling](#error-handling)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    Mobile App                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │   Canvas UI   │  │  Parameters  │  │   Controls   │ │
│  │  (Drawing)    │  │   (Settings) │  │  (Actions)   │ │
│  └──────┬────────┘  └──────┬───────┘  └──────┬───────┘ │
│         │                  │                  │          │
│         └──────────────────┼──────────────────┘         │
│                            │                            │
│                    ┌───────▼────────┐                    │
│                    │  API Client    │                    │
│                    │  (HTTP/JSON)  │                    │
│                    └───────┬────────┘                    │
└────────────────────────────┼────────────────────────────┘
                             │
                    HTTPS POST/GET
                             │
┌────────────────────────────▼────────────────────────────┐
│              FastAPI Backend (Port 7860)                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Endpoints  │  │   Models     │  │   Processing │  │
│  │  (FastAPI)   │  │ (LLaVA/SD)   │  │   (PyTorch)  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└──────────────────────────────────────────────────────────┘
```

**Key Points:**
- All Gradio UI functionality is accessible via HTTP API
- Mobile apps handle UI/UX locally, send data to backend for processing
- Backend returns processed results (images, prompts, configs)
- No Gradio dependency required for mobile clients

---

## Complete API Endpoint Reference

### Base URL
```
http://<server-ip>:7860
```

### All Available Endpoints

| Endpoint | Method | Purpose | Status |
|----------|--------|---------|--------|
| `/magic_quill/health` | GET | Health check & GPU status | ✅ Implemented |
| `/magic_quill/checkpoints` | GET | List available models | ✅ Implemented |
| `/magic_quill/config` | GET | Get server configuration | ✅ Implemented |
| `/magic_quill/config` | POST | Update server configuration | ✅ Implemented |
| `/magic_quill/guess_prompt` | POST | AI prompt suggestions | ✅ Implemented |
| `/magic_quill/process_background_img` | POST | Resize/normalize images | ✅ Implemented |
| `/magic_quill/generate` | POST | Generate edited images | ✅ Implemented |

---

## Gradio UI → API Mapping

### 1. Canvas Drawing Tools

#### 1.1 Load/Upload Image 📁
- **Gradio UI**: File picker uploads image to canvas
- **Mobile Equivalent**: Native image picker (gallery/camera)
- **API**: No direct API call needed
  - Image is processed client-side
  - Base64-encoded and sent in `generate` endpoint
- **Implementation**: 
  - Use native image picker
  - Convert to base64 PNG
  - Store as `original_image` in canvas state

#### 1.2 Add Edge Brush ➕
- **Gradio UI**: Draws on `add_edge_image` layer
- **Mobile Equivalent**: Canvas drawing on transparent layer
- **API**: Data sent in `POST /magic_quill/generate`
  - Field: `from_frontend.add_edge_image`
  - Format: Base64 PNG with alpha channel
- **Implementation**: Draw strokes → serialize to PNG → include in generate request

#### 1.3 Remove Edge Brush ➖
- **Gradio UI**: Draws on `remove_edge_image` layer
- **Mobile Equivalent**: Canvas drawing on separate layer
- **API**: Data sent in `POST /magic_quill/generate`
  - Field: `from_frontend.remove_edge_image`
  - Format: Base64 PNG with alpha channel
- **Implementation**: Same as Add Edge, different layer

#### 1.4 Color Brush 🎨
- **Gradio UI**: Draws RGB colors on `add_color_image` layer
- **Mobile Equivalent**: Color brush with color picker
- **API**: Data sent in `POST /magic_quill/generate`
  - Field: `from_frontend.add_color_image`
  - Format: Base64 PNG (full RGB, not just mask)
- **Implementation**: Draw with selected color → serialize → include in generate request

#### 1.5 Eraser 🧹
- **Gradio UI**: Creates white mask on `total_mask` layer
- **Mobile Equivalent**: Eraser tool creating white areas
- **API**: Data sent in `POST /magic_quill/generate`
  - Field: `from_frontend.total_mask` (REQUIRED)
  - Format: Base64 PNG with alpha channel (white = regenerate area)
- **Implementation**: Erase strokes → create white mask → serialize → include in generate request

#### 1.6 Select Tool 🖱️
- **Gradio UI**: Select/move/rotate/resize strokes
- **Mobile Equivalent**: Touch gestures for object manipulation
- **API**: No direct API call
  - Client-side operation only
  - Modified strokes are re-serialized and sent in next generate request
- **Implementation**: Handle touch events → modify canvas → update layer data

---

### 2. Canvas Controls

#### 2.1 Stroke Width Slider
- **Gradio UI**: Adjusts brush size (1-100px)
- **Mobile Equivalent**: Slider in toolbar
- **API**: No API call
  - Client-side UI control only
  - Affects drawing operations locally
- **Implementation**: Slider → update brush size → use in drawing operations

#### 2.2 Color Picker (Color Brush)
- **Gradio UI**: HTML color input
- **Mobile Equivalent**: Native color picker dialog
- **API**: No API call
  - Client-side UI control only
  - Selected color used in Color Brush drawing
- **Implementation**: Color picker → store selected color → use in Color Brush

#### 2.3 Alpha/Transparency Slider (Color Brush)
- **Gradio UI**: Opacity slider (0.1-1.0)
- **Mobile Equivalent**: Opacity slider control
- **API**: No API call
  - Client-side UI control only
  - Affects Color Brush stroke opacity
- **Implementation**: Slider → update opacity value → apply to Color Brush strokes

#### 2.4 Undo/Redo Buttons
- **Gradio UI**: History stack management
- **Mobile Equivalent**: Undo/Redo buttons with history
- **API**: No API call
  - Client-side operation only
  - Maintain history stack per layer
- **Implementation**: History stack → undo/redo → update canvas layers

#### 2.5 Download Button 💾
- **Gradio UI**: Downloads composite image
- **Mobile Equivalent**: Share/Save functionality
- **API**: No API call
  - Client-side operation
  - Composite layers locally and save/share
- **Implementation**: Composite all layers → save to device storage or share

#### 2.6 Prompt Input Box
- **Gradio UI**: Text input for generation prompt
- **Mobile Equivalent**: Text field in UI
- **API**: Data sent in `POST /magic_quill/generate`
  - Field: `from_backend.prompt` (REQUIRED)
  - Format: Plain text string
- **Implementation**: Text input → store value → include in generate request

#### 2.7 Magic Wand (Guess Prompt) ✨
- **Gradio UI**: Auto-generates prompt suggestions
- **Mobile Equivalent**: "Auto-suggest" button
- **API**: `POST /magic_quill/guess_prompt`
- **Request**:
```json
{
  "original_image": "data:image/png;base64,...",
  "add_color_image": "data:image/png;base64,...",  // Optional
  "add_edge_image": "data:image/png;base64,..."     // Optional
}
```
- **Response**: `"deer, forest animal, wildlife"` (plain text string)
- **Implementation**: 
  1. Collect current canvas layers
  2. POST to `/magic_quill/guess_prompt`
  3. Update prompt input field with response

#### 2.8 Eye Icon (Toggle Visibility)
- **Gradio UI**: Show/hide strokes
- **Mobile Equivalent**: Layer visibility toggle
- **API**: No API call
  - Client-side UI operation only
- **Implementation**: Toggle flag → show/hide layers in canvas rendering

#### 2.9 Delete Icon 🗑️
- **Gradio UI**: Delete selected stroke
- **Mobile Equivalent**: Delete button when object selected
- **API**: No API call
  - Client-side operation only
- **Implementation**: Remove from layer → update canvas

#### 2.10 Accept/Discard Icons ✅❌
- **Gradio UI**: Accept or reject generated result
- **Mobile Equivalent**: Accept/Reject buttons after generation
- **API**: No API call
  - Client-side operation only
  - Accept: Keep generated image, continue editing
  - Discard: Revert to previous state
- **Implementation**: Store previous state → accept/discard → update UI

---

### 3. Generation Parameters

All parameters are sent in `POST /magic_quill/generate` under the `params` object.

#### 3.1 Base Model Name (Dropdown)
- **Gradio UI**: Dropdown with available checkpoints
- **Mobile Equivalent**: Model selector dropdown
- **API**: 
  - **Get Models**: `GET /magic_quill/checkpoints`
  - **Response**: `{"checkpoints": ["SD1.5/model1.safetensors", ...]}`
  - **Use in Generate**: `params.ckpt_name`
- **Implementation**:
  1. Call `GET /magic_quill/checkpoints` on app startup
  2. Populate dropdown with results
  3. Send selected model in generate request

#### 3.2 Auto Save (Checkbox)
- **Gradio UI**: Toggle auto-save setting
- **Mobile Equivalent**: Settings toggle
- **API**: 
  - **Get Config**: `GET /magic_quill/config`
  - **Set Config**: `POST /magic_quill/config` with `{"auto_save": true/false}`
- **Implementation**: Toggle → POST to config endpoint → store preference

#### 3.3 Resolution (Slider)
- **Gradio UI**: Resolution slider (256-2048px)
- **Mobile Equivalent**: Resolution slider in settings
- **API**: 
  - **Get Config**: `GET /magic_quill/config` → `resolution` field
  - **Set Config**: `POST /magic_quill/config` with `{"resolution": 512}`
  - **Used By**: `POST /magic_quill/process_background_img` uses this value
- **Implementation**: 
  1. Set resolution before uploading image (recommended)
  2. POST to config endpoint to update
  3. Resolution affects image processing

#### 3.4 Negative Prompt (Textbox)
- **Gradio UI**: Multi-line text input
- **Mobile Equivalent**: Text input field
- **API**: Sent in `POST /magic_quill/generate`
  - Field: `params.negative_prompt`
  - Format: Plain text string
- **Implementation**: Text input → include in generate request

#### 3.5 Fine Edge (Radio)
- **Gradio UI**: Enable/Disable toggle
- **Mobile Equivalent**: Toggle switch
- **API**: Sent in `POST /magic_quill/generate`
  - Field: `params.fine_edge`
  - Values: `"enable"` or `"disable"`
  - Default: `"disable"`
- **Implementation**: Toggle → include in generate request

#### 3.6 Grow Size (Slider)
- **Gradio UI**: Slider (0-100px)
- **Mobile Equivalent**: Slider control
- **API**: Sent in `POST /magic_quill/generate`
  - Field: `params.grow_size`
  - Type: Integer (0-100)
  - Default: `15`
- **Implementation**: Slider → include integer value in generate request

#### 3.7 Edge Strength (Slider)
- **Gradio UI**: Slider (0.0-5.0)
- **Mobile Equivalent**: Slider control
- **API**: Sent in `POST /magic_quill/generate`
  - Field: `params.edge_strength`
  - Type: Float (0.0-5.0)
  - Default: `0.55`
- **Implementation**: Slider → include float value in generate request

#### 3.8 Color Strength (Slider)
- **Gradio UI**: Slider (0.0-5.0)
- **Mobile Equivalent**: Slider control
- **API**: Sent in `POST /magic_quill/generate`
  - Field: `params.color_strength`
  - Type: Float (0.0-5.0)
  - Default: `0.55`
- **Implementation**: Slider → include float value in generate request

#### 3.9 Inpaint Strength (Slider)
- **Gradio UI**: Slider (0.0-5.0)
- **Mobile Equivalent**: Slider control
- **API**: Sent in `POST /magic_quill/generate`
  - Field: `params.inpaint_strength`
  - Type: Float (0.0-5.0)
  - Default: `1.0`
- **Implementation**: Slider → include float value in generate request

#### 3.10 Seed (Number Input)
- **Gradio UI**: Number input (-1 for random)
- **Mobile Equivalent**: Number input with "Random" option
- **API**: Sent in `POST /magic_quill/generate`
  - Field: `params.seed`
  - Type: Integer
  - Default: `-1` (random, server generates)
  - Any positive integer = fixed seed (reproducible)
- **Implementation**: Input → include integer value (-1 for random)

#### 3.11 Steps (Slider)
- **Gradio UI**: Slider (1-50)
- **Mobile Equivalent**: Slider control
- **API**: Sent in `POST /magic_quill/generate`
  - Field: `params.steps`
  - Type: Integer (1-50)
  - Default: `20`
- **Implementation**: Slider → include integer value in generate request

#### 3.12 CFG (Slider)
- **Gradio UI**: Slider (0.0-100.0)
- **Mobile Equivalent**: Slider control
- **API**: Sent in `POST /magic_quill/generate`
  - Field: `params.cfg`
  - Type: Float (0.0-100.0)
  - Default: `5.0`
- **Implementation**: Slider → include float value in generate request

#### 3.13 Sampler Name (Dropdown)
- **Gradio UI**: Dropdown with sampler options
- **Mobile Equivalent**: Dropdown selector
- **API**: Sent in `POST /magic_quill/generate`
  - Field: `params.sampler_name`
  - Type: String
  - Options: `"euler"`, `"euler_ancestral"`, `"heun"`, `"heunpp2"`, `"dpm_2"`, `"dpm_2_ancestral"`, `"lms"`, `"dpm_fast"`, `"dpm_adaptive"`, `"dpmpp_2s_ancestral"`, `"dpmpp_sde"`, `"dpmpp_sde_gpu"`, `"dpmpp_2m"`, `"dpmpp_2m_sde"`, `"dpmpp_2m_sde_gpu"`, `"dpmpp_3m_sde"`, `"dpmpp_3m_sde_gpu"`, `"ddpm"`, `"lcm"`, `"ddim"`, `"uni_pc"`, `"uni_pc_bh2"`
  - Default: `"euler_ancestral"`
- **Implementation**: Dropdown → include selected string in generate request

#### 3.14 Scheduler (Dropdown)
- **Gradio UI**: Dropdown with scheduler options
- **Mobile Equivalent**: Dropdown selector
- **API**: Sent in `POST /magic_quill/generate`
  - Field: `params.scheduler`
  - Type: String
  - Options: `"normal"`, `"karras"`, `"exponential"`, `"sgm_uniform"`, `"simple"`, `"ddim_uniform"`
  - Default: `"karras"`
- **Implementation**: Dropdown → include selected string in generate request

#### 3.15 Run Button 🚀
- **Gradio UI**: Triggers image generation
- **Mobile Equivalent**: "Generate" button
- **API**: `POST /magic_quill/generate`
- **Full Request Example**: See [Code Examples](#code-examples) section
- **Response**:
```json
{
  "generated_image": "data:image/png;base64,...",
  "seed": 1234567890,
  "metadata": {
    "ckpt_name": "SD1.5/realisticVisionV60B1_v51VAE.safetensors",
    "steps": 20,
    "cfg": 5.0,
    "sampler_name": "euler_ancestral",
    "scheduler": "karras",
    "fine_edge": "disable",
    "grow_size": 15,
    "edge_strength": 0.55,
    "color_strength": 0.55,
    "inpaint_strength": 1.0
  }
}
```
- **Implementation**: 
  1. Collect all canvas layers
  2. Collect all parameters
  3. POST to `/magic_quill/generate`
  4. Display returned image
  5. Show Accept/Discard buttons

---

## Mobile Implementation Workflows

### Complete User Workflow

```
1. App Startup
   ├─ GET /magic_quill/health (verify server)
   ├─ GET /magic_quill/checkpoints (load models)
   └─ GET /magic_quill/config (load settings)

2. User Uploads Image
   ├─ Native image picker
   ├─ Convert to base64 PNG
   └─ Set as original_image in canvas

3. User Sets Resolution (Optional)
   └─ POST /magic_quill/config {"resolution": 512}

4. User Draws on Canvas
   ├─ Select brush tool (Add Edge/Remove Edge/Color/Eraser)
   ├─ Adjust stroke width (client-side)
   ├─ (If Color Brush) Select color & opacity (client-side)
   └─ Draw strokes (client-side, stored in layers)

5. User Gets Prompt Suggestions (Optional)
   ├─ POST /magic_quill/guess_prompt
   │   {
   │     "original_image": "...",
   │     "add_color_image": "...",  // Optional
   │     "add_edge_image": "..."     // Optional
   │   }
   └─ Update prompt input with response

6. User Configures Parameters (Optional)
   ├─ Adjust sliders/dropdowns (client-side)
   └─ Values stored for generate request

7. User Clicks Generate
   ├─ POST /magic_quill/generate
   │   {
   │     "from_frontend": {
   │       "total_mask": "...",
   │       "original_image": "...",
   │       "add_color_image": "...",
   │       "add_edge_image": "...",
   │       "remove_edge_image": "..."
   │     },
   │     "from_backend": {
   │       "prompt": "..."
   │     },
   │     "params": {
   │       "ckpt_name": "...",
   │       "negative_prompt": "...",
   │       "fine_edge": "...",
   │       "grow_size": ...,
   │       "edge_strength": ...,
   │       "color_strength": ...,
   │       "inpaint_strength": ...,
   │       "seed": ...,
   │       "steps": ...,
   │       "cfg": ...,
   │       "sampler_name": "...",
   │       "scheduler": "..."
   │     }
   │   }
   ├─ Wait for response (20-60 seconds)
   ├─ Display generated image
   └─ Show Accept/Discard buttons

8. User Accepts/Discards
   ├─ Accept: Keep image, continue editing
   └─ Discard: Revert to previous state (client-side)
```

---

## Code Examples

### Android (Kotlin) - Complete API Client

```kotlin
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MagicQuillApiClient(private val baseUrl: String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()
    
    private val jsonMediaType = "application/json".toMediaType()
    
    // Health Check
    suspend fun healthCheck(): Result<HealthStatus> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/magic_quill/health")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "{}")
                Result.success(HealthStatus(
                    status = json.getString("status"),
                    service = json.getString("service"),
                    gpuAvailable = json.getBoolean("gpu_available")
                ))
            } else {
                Result.failure(Exception("Health check failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get Available Checkpoints
    suspend fun getCheckpoints(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/magic_quill/checkpoints")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "{}")
                val checkpoints = json.getJSONArray("checkpoints")
                val list = mutableListOf<String>()
                for (i in 0 until checkpoints.length()) {
                    list.add(checkpoints.getString(i))
                }
                Result.success(list)
            } else {
                Result.failure(Exception("Failed to get checkpoints: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get Config
    suspend fun getConfig(): Result<ServerConfig> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/magic_quill/config")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "{}")
                Result.success(ServerConfig(
                    resolution = json.getInt("resolution"),
                    autoSave = json.getBoolean("auto_save")
                ))
            } else {
                Result.failure(Exception("Failed to get config: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Set Config
    suspend fun setConfig(resolution: Int? = null, autoSave: Boolean? = null): Result<ServerConfig> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                resolution?.let { put("resolution", it) }
                autoSave?.let { put("auto_save", it) }
            }
            
            val request = Request.Builder()
                .url("$baseUrl/magic_quill/config")
                .post(body.toString().toRequestBody(jsonMediaType))
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "{}")
                Result.success(ServerConfig(
                    resolution = json.getInt("resolution"),
                    autoSave = json.getBoolean("auto_save")
                ))
            } else {
                Result.failure(Exception("Failed to set config: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Guess Prompt
    suspend fun guessPrompt(
        originalImage: String,
        addColorImage: String? = null,
        addEdgeImage: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("original_image", originalImage)
                addColorImage?.let { put("add_color_image", it) }
                addEdgeImage?.let { put("add_edge_image", it) }
            }
            
            val request = Request.Builder()
                .url("$baseUrl/magic_quill/guess_prompt")
                .post(body.toString().toRequestBody(jsonMediaType))
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                // Response is plain text string, not JSON
                val prompt = response.body?.string()?.trim('"') ?: ""
                Result.success(prompt)
            } else {
                Result.failure(Exception("Failed to guess prompt: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Process Background Image
    suspend fun processBackgroundImage(imageBase64: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Request body is just the string, not a JSON object
            val request = Request.Builder()
                .url("$baseUrl/magic_quill/process_background_img")
                .post(imageBase64.toRequestBody(jsonMediaType))
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                // Response is plain text string, not JSON
                val resizedImage = response.body?.string()?.trim('"') ?: ""
                Result.success(resizedImage)
            } else {
                Result.failure(Exception("Failed to process image: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Generate Image
    suspend fun generateImage(
        canvasLayers: CanvasLayers,
        prompt: String,
        params: GenerationParams
    ): Result<GenerationResult> = withContext(Dispatchers.IO) {
        try {
            val requestBody = JSONObject().apply {
                put("from_frontend", JSONObject().apply {
                    put("total_mask", canvasLayers.totalMask)
                    put("original_image", canvasLayers.originalImage)
                    canvasLayers.addColorImage?.let { put("add_color_image", it) }
                    canvasLayers.addEdgeImage?.let { put("add_edge_image", it) }
                    canvasLayers.removeEdgeImage?.let { put("remove_edge_image", it) }
                })
                put("from_backend", JSONObject().apply {
                    put("prompt", prompt)
                })
                put("params", JSONObject().apply {
                    put("ckpt_name", params.ckptName)
                    put("negative_prompt", params.negativePrompt ?: "")
                    put("fine_edge", params.fineEdge ?: "disable")
                    put("grow_size", params.growSize ?: 15)
                    put("edge_strength", params.edgeStrength ?: 0.55)
                    put("color_strength", params.colorStrength ?: 0.55)
                    put("inpaint_strength", params.inpaintStrength ?: 1.0)
                    put("seed", params.seed ?: -1)
                    put("steps", params.steps ?: 20)
                    put("cfg", params.cfg ?: 5.0)
                    put("sampler_name", params.samplerName ?: "euler_ancestral")
                    put("scheduler", params.scheduler ?: "karras")
                })
            }
            
            val request = Request.Builder()
                .url("$baseUrl/magic_quill/generate")
                .post(requestBody.toString().toRequestBody(jsonMediaType))
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "{}")
                Result.success(GenerationResult(
                    generatedImage = json.getString("generated_image"),
                    seed = json.getInt("seed"),
                    metadata = json.getJSONObject("metadata")
                ))
            } else {
                Result.failure(Exception("Generation failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Data Classes
data class HealthStatus(
    val status: String,
    val service: String,
    val gpuAvailable: Boolean
)

data class ServerConfig(
    val resolution: Int,
    val autoSave: Boolean
)

data class CanvasLayers(
    val totalMask: String,
    val originalImage: String,
    val addColorImage: String? = null,
    val addEdgeImage: String? = null,
    val removeEdgeImage: String? = null
)

data class GenerationParams(
    val ckptName: String = "SD1.5/realisticVisionV60B1_v51VAE.safetensors",
    val negativePrompt: String? = null,
    val fineEdge: String? = null,
    val growSize: Int? = null,
    val edgeStrength: Double? = null,
    val colorStrength: Double? = null,
    val inpaintStrength: Double? = null,
    val seed: Int? = null,
    val steps: Int? = null,
    val cfg: Double? = null,
    val samplerName: String? = null,
    val scheduler: String? = null
)

data class GenerationResult(
    val generatedImage: String,
    val seed: Int,
    val metadata: JSONObject
)
```

### iOS (Swift) - Complete API Client

```swift
import Foundation

class MagicQuillApiClient {
    private let baseURL: String
    private let session: URLSession
    
    init(baseURL: String) {
        self.baseURL = baseURL
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30
        config.timeoutIntervalForResource = 120
        self.session = URLSession(configuration: config)
    }
    
    // Health Check
    func healthCheck() async throws -> HealthStatus {
        guard let url = URL(string: "\(baseURL)/magic_quill/health") else {
            throw APIError.invalidURL
        }
        
        let (data, response) = try await session.data(from: url)
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw APIError.serverError
        }
        
        let json = try JSONDecoder().decode(HealthStatus.self, from: data)
        return json
    }
    
    // Get Available Checkpoints
    func getCheckpoints() async throws -> [String] {
        guard let url = URL(string: "\(baseURL)/magic_quill/checkpoints") else {
            throw APIError.invalidURL
        }
        
        let (data, response) = try await session.data(from: url)
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw APIError.serverError
        }
        
        let json = try JSONSerialization.jsonObject(with: data) as? [String: Any]
        guard let checkpoints = json?["checkpoints"] as? [String] else {
            throw APIError.invalidResponse
        }
        
        return checkpoints
    }
    
    // Get Config
    func getConfig() async throws -> ServerConfig {
        guard let url = URL(string: "\(baseURL)/magic_quill/config") else {
            throw APIError.invalidURL
        }
        
        let (data, response) = try await session.data(from: url)
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw APIError.serverError
        }
        
        let config = try JSONDecoder().decode(ServerConfig.self, from: data)
        return config
    }
    
    // Set Config
    func setConfig(resolution: Int? = nil, autoSave: Bool? = nil) async throws -> ServerConfig {
        guard let url = URL(string: "\(baseURL)/magic_quill/config") else {
            throw APIError.invalidURL
        }
        
        var body: [String: Any] = [:]
        if let resolution = resolution { body["resolution"] = resolution }
        if let autoSave = autoSave { body["auto_save"] = autoSave }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw APIError.serverError
        }
        
        let config = try JSONDecoder().decode(ServerConfig.self, from: data)
        return config
    }
    
    // Guess Prompt
    func guessPrompt(
        originalImage: String,
        addColorImage: String? = nil,
        addEdgeImage: String? = nil
    ) async throws -> String {
        guard let url = URL(string: "\(baseURL)/magic_quill/guess_prompt") else {
            throw APIError.invalidURL
        }
        
        var body: [String: Any] = ["original_image": originalImage]
        if let addColorImage = addColorImage { body["add_color_image"] = addColorImage }
        if let addEdgeImage = addEdgeImage { body["add_edge_image"] = addEdgeImage }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw APIError.serverError
        }
        
        // Response is plain text string, not JSON
        if let prompt = String(data: data, encoding: .utf8)?.trimmingCharacters(in: CharacterSet(charactersIn: "\"")) {
            return prompt
        }
        throw APIError.invalidResponse
    }
    
    // Process Background Image
    func processBackgroundImage(_ imageBase64: String) async throws -> String {
        guard let url = URL(string: "\(baseURL)/magic_quill/process_background_img") else {
            throw APIError.invalidURL
        }
        
        // Request body is just the string, not a JSON object
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = imageBase64.data(using: .utf8)
        
        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw APIError.serverError
        }
        
        // Response is plain text string, not JSON
        if let resizedImage = String(data: data, encoding: .utf8)?.trimmingCharacters(in: CharacterSet(charactersIn: "\"")) {
            return resizedImage
        }
        throw APIError.invalidResponse
    }
    
    // Generate Image
    func generateImage(
        canvasLayers: CanvasLayers,
        prompt: String,
        params: GenerationParams
    ) async throws -> GenerationResult {
        guard let url = URL(string: "\(baseURL)/magic_quill/generate") else {
            throw APIError.invalidURL
        }
        
        let requestBody: [String: Any] = [
            "from_frontend": [
                "total_mask": canvasLayers.totalMask,
                "original_image": canvasLayers.originalImage,
                "add_color_image": canvasLayers.addColorImage as Any,
                "add_edge_image": canvasLayers.addEdgeImage as Any,
                "remove_edge_image": canvasLayers.removeEdgeImage as Any
            ],
            "from_backend": [
                "prompt": prompt
            ],
            "params": [
                "ckpt_name": params.ckptName,
                "negative_prompt": params.negativePrompt ?? "",
                "fine_edge": params.fineEdge ?? "disable",
                "grow_size": params.growSize ?? 15,
                "edge_strength": params.edgeStrength ?? 0.55,
                "color_strength": params.colorStrength ?? 0.55,
                "inpaint_strength": params.inpaintStrength ?? 1.0,
                "seed": params.seed ?? -1,
                "steps": params.steps ?? 20,
                "cfg": params.cfg ?? 5.0,
                "sampler_name": params.samplerName ?? "euler_ancestral",
                "scheduler": params.scheduler ?? "karras"
            ]
        ]
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONSerialization.data(withJSONObject: requestBody)
        
        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw APIError.serverError
        }
        
        let json = try JSONSerialization.jsonObject(with: data) as? [String: Any]
        guard let generatedImage = json?["generated_image"] as? String,
              let seed = json?["seed"] as? Int,
              let metadata = json?["metadata"] as? [String: Any] else {
            throw APIError.invalidResponse
        }
        
        return GenerationResult(
            generatedImage: generatedImage,
            seed: seed,
            metadata: metadata
        )
    }
}

// Data Structures
struct HealthStatus: Codable {
    let status: String
    let service: String
    let gpuAvailable: Bool
    
    enum CodingKeys: String, CodingKey {
        case status, service
        case gpuAvailable = "gpu_available"
    }
}

struct ServerConfig: Codable {
    let resolution: Int
    let autoSave: Bool
    
    enum CodingKeys: String, CodingKey {
        case resolution
        case autoSave = "auto_save"
    }
}

struct CanvasLayers {
    let totalMask: String
    let originalImage: String
    let addColorImage: String?
    let addEdgeImage: String?
    let removeEdgeImage: String?
}

struct GenerationParams {
    let ckptName: String
    let negativePrompt: String?
    let fineEdge: String?
    let growSize: Int?
    let edgeStrength: Double?
    let colorStrength: Double?
    let inpaintStrength: Double?
    let seed: Int?
    let steps: Int?
    let cfg: Double?
    let samplerName: String?
    let scheduler: String?
}

struct GenerationResult {
    let generatedImage: String
    let seed: Int
    let metadata: [String: Any]
}

enum APIError: Error {
    case invalidURL
    case serverError
    case invalidResponse
}
```

---

## Error Handling

### HTTP Status Codes

| Code | Meaning | Action |
|------|---------|--------|
| `200 OK` | Success | Process response |
| `400 Bad Request` | Invalid parameters | Show error message, validate inputs |
| `404 Not Found` | Resource not found | Check checkpoint/model paths |
| `500 Internal Server Error` | Server error | Retry or show error message |
| `503 Service Unavailable` | Service unavailable | Show "server starting" message |

### Error Response Format

```json
{
  "detail": "Error message describing what went wrong"
}
```

### Common Error Scenarios

1. **Network Timeout**: Generation can take 20-60 seconds
   - Solution: Set appropriate timeout values (120+ seconds for generate)

2. **Missing Required Fields**: `original_image`, `total_mask`, or `prompt` missing
   - Solution: Validate all required fields before sending request

3. **Invalid Image Format**: Base64 string malformed
   - Solution: Ensure proper data URI prefix: `data:image/png;base64,...`

4. **Checkpoint Not Found**: Model file doesn't exist
   - Solution: Verify checkpoint name matches available models from `/magic_quill/checkpoints`

5. **GPU Out of Memory**: Server ran out of VRAM
   - Solution: Reduce resolution or image size, retry request

---

## Summary

**All 31 Gradio UI interactions are now accessible via API:**

✅ **Canvas Tools**: All 6 drawing tools → Data sent in `generate` endpoint  
✅ **Canvas Controls**: All 8 controls → Mix of client-side and API calls  
✅ **Generation Parameters**: All 14 parameters → Sent in `generate` endpoint  
✅ **Utility Endpoints**: Health, checkpoints, config → Separate API endpoints  

**Mobile apps can now replicate 100% of Gradio UI functionality through the API!**
