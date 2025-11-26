# MagicQuill API Documentation

Complete API reference for building a self-hosted MagicQuill server (mobile or web). This document details all required parameters, endpoints, model files, and response formats.

## ✅ Implementation Status

**All API endpoints are fully implemented and functional!** The backend provides complete API access to all MagicQuill functionalities, equivalent to using the Gradio web UI.

### Available Endpoints

| Endpoint | Method | Status | Description |
|----------|--------|--------|-------------|
| `/magic_quill/guess_prompt` | POST | ✅ Implemented | Get AI-generated prompt suggestions |
| `/magic_quill/process_background_img` | POST | ✅ Implemented | Resize and normalize images |
| `/magic_quill/generate` | POST | ✅ Implemented | Generate edited images (main endpoint) |
| `/magic_quill/checkpoints` | GET | ✅ Implemented | List available model checkpoints |
| `/magic_quill/config` | GET | ✅ Implemented | Get server configuration |
| `/magic_quill/config` | POST | ✅ Implemented | Update server configuration |
| `/magic_quill/health` | GET | ✅ Implemented | Health check and GPU status |

**No code modifications needed** - all endpoints are ready to use out of the box!

---

## Table of Contents

1. [Server Requirements](#server-requirements)
2. [Model Files & Dependencies](#model-files--dependencies)
3. [API Endpoints](#api-endpoints)
4. [Request/Response Formats](#requestresponse-formats)
5. [Getting the Final Image](#getting-the-final-image)
6. [Error Handling](#error-handling)
7. [Server Setup Guide](#server-setup-guide)

---

## Server Requirements

### Hardware Requirements

- **GPU**: Required (minimum 8GB VRAM, tested on RTX 4070 Laptop)
- **CPU**: Multi-core processor recommended
- **RAM**: 16GB+ recommended
- **Storage**: ~30GB for models and dependencies

### Software Requirements

- **Python**: 3.10
- **PyTorch**: 2.1.2 with CUDA 11.8 support
- **Operating System**: Linux (recommended) or Windows
- **CUDA**: 11.8+ (for GPU acceleration)

---

## Model Files & Dependencies

### Required Model Files Structure

All models must be placed in the `models/` directory relative to the server root:

```
models/
├── checkpoints/
│   └── SD1.5/
│       ├── realisticVisionV60B1_v51VAE.safetensors  (REQUIRED - default)
│       ├── DreamShaper.safetensors                  (optional)
│       ├── majicMIX_realistic.safetensors          (optional)
│       ├── MeinaMix.safetensors                     (optional)
│       └── ghostmix_v20Bakedvae.safetensors        (optional)
├── configs/
│   ├── v1-inference.yaml                            (REQUIRED)
│   ├── v1-inference_fp16.yaml                       (optional)
│   └── [other config files]                         (optional)
├── controlnet/
│   ├── control_v11p_sd15_scribble.safetensors     (REQUIRED)
│   └── color_finetune.safetensors                   (REQUIRED)
├── inpaint/
│   └── brushnet/
│       ├── random_mask_brushnet_ckpt/               (REQUIRED)
│       │   ├── config.json
│       │   └── diffusion_pytorch_model.safetensors
│       └── segmentation_mask_brushnet_ckpt/        (REQUIRED)
│           ├── config.json
│           └── diffusion_pytorch_model.safetensors
├── llava-v1.5-7b-finetune-clean/                  (REQUIRED)
│   ├── config.json
│   ├── generation_config.json
│   ├── model-00001-of-00003.safetensors
│   ├── model-00002-of-00003.safetensors
│   ├── model-00003-of-00003.safetensors
│   ├── model.safetensors.index.json
│   ├── special_tokens_map.json
│   ├── tokenizer_config.json
│   └── tokenizer.model
└── preprocessor/
    ├── sk_model.pth                                 (REQUIRED)
    ├── sk_model2.pth                                (REQUIRED)
    └── table5_pidinet.pth                           (REQUIRED)
```

**Total Size**: ~25GB (all models)

### Model Download Sources

1. **Official Download**: 
   - URL: `https://hkustconnect-my.sharepoint.com/:u:/g/personal/zliucz_connect_ust_hk/EWlGF0WfawJIrJ1Hn85_-3gB0MtwImAnYeWXuleVQcukMg?e=Gcjugg&download=1`
   - Format: ZIP file containing all models
   - Size: ~25GB

2. **HuggingFace**:
   - Repository: `https://huggingface.co/LiuZichen/MagicQuill-models`
   - Alternative mirror: `https://hf-mirror.com` (for mainland users)

### Python Dependencies

```txt
# Core dependencies (from requirements.txt)
webcolors==1.13
opencv-python==4.10.0.84
diffusers==0.31.0
torchsde==0.2.6
protobuf==4.25.4

# PyTorch (install separately)
torch==2.1.2
torchvision==0.16.2
torchaudio==2.1.2

# Additional required packages
gradio
fastapi
uvicorn
pillow
numpy
einops
scipy
```

### Environment Variables

- `CUDA_VISIBLE_DEVICES`: Set to GPU device ID (e.g., `0` for first GPU)
- `HF_ENDPOINT`: Optional, set to `https://hf-mirror.com` for mainland users

---

## API Endpoints

### Base URL

```
http://<server-ip>:7860
```

Default port: `7860` (configurable in `gradio_run.py`)

---

### 1. POST `/magic_quill/guess_prompt`

**Purpose**: Use LLaVA model to automatically generate prompt suggestions based on user drawings.

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "original_image": "data:image/png;base64,<base64_encoded_png>",
  "add_color_image": "data:image/png;base64,<base64_encoded_png>",  // Optional
  "add_edge_image": "data:image/png;base64,<base64_encoded_png>"     // Optional
}
```

**Parameters**:
- `original_image` (string, **REQUIRED**): Base64-encoded PNG image with data URI prefix. The original photo/canvas the user wants to edit.
- `add_color_image` (string, optional): Base64-encoded PNG with color overlays. If omitted, backend uses `original_image`.
- `add_edge_image` (string, optional): Base64-encoded PNG with alpha channel representing edge strokes. If omitted, backend creates empty mask.

**Image Format Requirements**:
- Must include data URI prefix: `data:image/png;base64,` or `data:image/jpeg;base64,` or `data:image/webp;base64,`
- PNG format recommended for transparency support
- All images must have matching dimensions

**Response**:
```json
"coat of arms, fantasy emblem, royal crest"
```

- **Type**: Plain text string (not JSON object)
- **Content**: Comma-separated prompt suggestions
- **Example**: `"deer, forest animal, wildlife"`

**Response Time**: 5-15 seconds (depends on GPU)

**Error Responses**:
- `400 Bad Request`: Invalid image format or missing `original_image`
- `500 Internal Server Error`: Model loading failed or inference error

---

### 2. POST `/magic_quill/process_background_img`

**Purpose**: Resize and normalize background image to match server's configured resolution.

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
"data:image/png;base64,<base64_encoded_png>"
```

**Parameters**:
- Request body is a **single string** (not JSON object)
- Must be base64-encoded PNG/JPEG/WebP with data URI prefix
- Server uses global `RES` variable (default: 512) to resize smaller dimension

**Response**:
```json
"data:image/png;base64,<resized_base64_png>"
```

- **Type**: Single string (not JSON object)
- **Format**: PNG data URI
- **Dimensions**: Resized to match server's `RES` setting (maintains aspect ratio)

**Response Time**: <1 second

**Error Responses**:
- `400 Bad Request`: Invalid image format
- `500 Internal Server Error`: Image processing failed

---

### 3. POST `/magic_quill/generate` ✅ **IMPLEMENTED**

**Status**: ✅ **Fully implemented and functional**. This endpoint provides complete API access to the image generation functionality, equivalent to using the Gradio web UI.

**Purpose**: Generate edited image based on user drawings, masks, and prompts.

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "from_frontend": {
    "total_mask": "data:image/png;base64,<base64_encoded_png>",
    "original_image": "data:image/png;base64,<base64_encoded_png>",
    "add_color_image": "data:image/png;base64,<base64_encoded_png>",
    "add_edge_image": "data:image/png;base64,<base64_encoded_png>",
    "remove_edge_image": "data:image/png;base64,<base64_encoded_png>"
  },
  "from_backend": {
    "prompt": "A royal crest with vibrant reds and golds"
  },
  "params": {
    "ckpt_name": "SD1.5/realisticVisionV60B1_v51VAE.safetensors",
    "negative_prompt": "",
    "fine_edge": "disable",
    "grow_size": 15,
    "edge_strength": 0.55,
    "color_strength": 0.55,
    "inpaint_strength": 1.0,
    "seed": -1,
    "steps": 20,
    "cfg": 5.0,
    "sampler_name": "euler_ancestral",
    "scheduler": "karras"
  }
}
```

**Required Parameters**:

#### `from_frontend` Object

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `total_mask` | string | **YES** | Base64 PNG with alpha channel. White pixels = area to regenerate (eraser brush). |
| `original_image` | string | **YES** | Base64 PNG of the original photo/canvas. |
| `add_color_image` | string | Optional | Base64 PNG with color overlays. If null/empty, uses `original_image`. |
| `add_edge_image` | string | Optional | Base64 PNG with alpha channel. Non-transparent areas = edges to add. |
| `remove_edge_image` | string | Optional | Base64 PNG with alpha channel. Non-transparent areas = edges to remove. |

#### `from_backend` Object

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `prompt` | string | **YES** | Positive prompt describing what to generate. Use `/magic_quill/guess_prompt` for suggestions. |

#### `params` Object

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `ckpt_name` | string | **YES** | `SD1.5/realisticVisionV60B1_v51VAE.safetensors` | Path to checkpoint file (relative to `models/checkpoints/`). Must exist in server's models directory. |
| `negative_prompt` | string | No | `""` | Things to avoid generating. |
| `fine_edge` | string | No | `"disable"` | `"enable"` or `"disable"` - enables fine edge control. |
| `grow_size` | integer | No | `15` | Pixel range affected around brush strokes (0-100). |
| `edge_strength` | float | No | `0.55` | Strength of add/subtract brush control (0.0-5.0). |
| `color_strength` | float | No | `0.55` | Strength of color brush control (0.0-5.0). |
| `inpaint_strength` | float | No | `1.0` | Inpainting strength (0.0-5.0). |
| `seed` | integer | No | `-1` | Random seed. `-1` = random seed (server generates). |
| `steps` | integer | No | `20` | Number of diffusion steps (1-50). More steps = better quality but slower. |
| `cfg` | float | No | `5.0` | Classifier-free guidance scale (0.0-100.0). Higher = more prompt adherence. |
| `sampler_name` | string | No | `"euler_ancestral"` | Sampler algorithm. Options: `"euler"`, `"euler_ancestral"`, `"heun"`, `"heunpp2"`, `"dpm_2"`, `"dpm_2_ancestral"`, `"lms"`, `"dpm_fast"`, `"dpm_adaptive"`, `"dpmpp_2s_ancestral"`, `"dpmpp_sde"`, `"dpmpp_sde_gpu"`, `"dpmpp_2m"`, `"dpmpp_2m_sde"`, `"dpmpp_2m_sde_gpu"`, `"dpmpp_3m_sde"`, `"dpmpp_3m_sde_gpu"`, `"ddpm"`, `"lcm"`, `"ddim"`, `"uni_pc"`, `"uni_pc_bh2"`. |
| `scheduler` | string | No | `"karras"` | Scheduler type. Options: `"normal"`, `"karras"`, `"exponential"`, `"sgm_uniform"`, `"simple"`, `"ddim_uniform"`. |

**Available Checkpoint Models**:

| `ckpt_name` Value | Style | Use Case |
|-------------------|-------|----------|
| `SD1.5/realisticVisionV60B1_v51VAE.safetensors` | Realistic | **Default** - General purpose, realistic images |
| `SD1.5/DreamShaper.safetensors` | Fantasy | Fantasy and artistic styles |
| `SD1.5/majicMIX_realistic.safetensors` | Realistic Portraits | Best for portraits |
| `SD1.5/MeinaMix.safetensors` | Anime | Anime-style images |
| `SD1.5/ghostmix_v20Bakedvae.safetensors` | Anime | Alternative anime model |

**Response**:
```json
{
  "generated_image": "data:image/png;base64,<base64_encoded_png>",
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

**Response Fields**:
- `generated_image` (string, **REQUIRED**): Base64-encoded PNG with data URI prefix. The final edited image.
- `seed` (integer): Random seed used (useful for reproducibility).
- `metadata` (object, optional): Generation parameters for reference.

**Response Time**: 20-60 seconds (depends on GPU, steps, and image size)

**Error Responses**:
- `400 Bad Request`: Missing required parameters, invalid image format, or invalid parameter values
- `404 Not Found`: Checkpoint file not found
- `500 Internal Server Error`: Model loading failed, GPU out of memory, or inference error

---

### 4. GET `/magic_quill/checkpoints`

**Purpose**: Get list of available checkpoint models on the server.

**Request Headers**: None required

**Response**:
```json
{
  "checkpoints": [
    "SD1.5/realisticVisionV60B1_v51VAE.safetensors",
    "SD1.5/DreamShaper.safetensors",
    "SD1.5/majicMIX_realistic.safetensors",
    "SD1.5/MeinaMix.safetensors",
    "SD1.5/ghostmix_v20Bakedvae.safetensors"
  ]
}
```

**Response Fields**:
- `checkpoints` (array of strings): List of available checkpoint file paths relative to `models/checkpoints/`

**Use Case**: Mobile apps can query this endpoint to show available models in a dropdown/selector UI.

---

### 5. GET `/magic_quill/config`

**Purpose**: Get current server configuration (resolution, auto_save settings).

**Request Headers**: None required

**Response**:
```json
{
  "resolution": 512,
  "auto_save": false
}
```

**Response Fields**:
- `resolution` (integer): Current server resolution setting (256-2048)
- `auto_save` (boolean): Whether auto-save is enabled

---

### 6. POST `/magic_quill/config`

**Purpose**: Update server configuration (resolution, auto_save settings).

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "resolution": 1024,
  "auto_save": true
}
```

**Parameters**:
- `resolution` (integer, optional): Set server resolution (256-2048). Affects image processing.
- `auto_save` (boolean, optional): Enable/disable auto-save of generated images.

**Response**:
```json
{
  "resolution": 1024,
  "auto_save": true,
  "message": "Configuration updated successfully"
}
```

**Note**: Changes persist only for the current server session. Restarting the server resets to defaults.

---

### 7. GET `/magic_quill/health`

**Purpose**: Health check endpoint to verify server is running and GPU is available.

**Request Headers**: None required

**Response**:
```json
{
  "status": "healthy",
  "service": "MagicQuill API",
  "gpu_available": true
}
```

**Response Fields**:
- `status` (string): Always `"healthy"` if server is running
- `service` (string): Service name
- `gpu_available` (boolean): Whether CUDA GPU is available

**Use Case**: Mobile apps can ping this endpoint on startup to verify server connectivity and GPU availability.

---

## Getting the Final Image

### API Implementation

The `/magic_quill/generate` endpoint returns the final image in the response JSON:

```json
{
  "generated_image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
}
```

### Client-Side Processing

**Android (Kotlin)**:
```kotlin
fun decodeBase64Image(base64String: String): Bitmap? {
    // Remove data URI prefix if present
    val base64 = if (base64String.startsWith("data:image")) {
        base64String.substringAfter(",")
    } else {
        base64String
    }
    
    val imageBytes = Base64.decode(base64, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}
```

**iOS (Swift)**:
```swift
func decodeBase64Image(_ base64String: String) -> UIImage? {
    // Remove data URI prefix if present
    let base64 = base64String.hasPrefix("data:image") 
        ? String(base64String.drop(while: { $0 != "," }).dropFirst())
        : base64String
    
    guard let imageData = Data(base64Encoded: base64),
          let image = UIImage(data: imageData) else {
        return nil
    }
    return image
}
```

### Image Format

- **Format**: PNG (lossless)
- **Encoding**: Base64 with data URI prefix
- **Dimensions**: Matches input image dimensions (or resized based on server `RES` setting)
- **Color Space**: RGB

---

## Error Handling

### HTTP Status Codes

| Code | Meaning | Common Causes |
|------|---------|---------------|
| `200 OK` | Success | Request processed successfully |
| `400 Bad Request` | Invalid request | Missing required parameters, invalid image format, invalid parameter values |
| `404 Not Found` | Resource not found | Checkpoint file doesn't exist, endpoint doesn't exist |
| `500 Internal Server Error` | Server error | Model loading failed, GPU out of memory, inference error, missing model files |
| `503 Service Unavailable` | Service unavailable | Server starting up, models loading |

### Error Response Format

```json
{
  "error": "Error message describing what went wrong",
  "code": 400,
  "details": {
    "parameter": "ckpt_name",
    "message": "Checkpoint file not found: SD1.5/invalid_model.safetensors"
  }
}
```

### Common Errors

1. **Missing Model Files**
   - **Error**: `500 Internal Server Error`
   - **Message**: `"Model file not found: models/checkpoints/..."`
   - **Solution**: Ensure all required model files are downloaded and placed in correct directories

2. **GPU Out of Memory**
   - **Error**: `500 Internal Server Error`
   - **Message**: `"CUDA out of memory"`
   - **Solution**: Reduce image resolution (`RES`), reduce `steps`, or use smaller checkpoint

3. **Invalid Image Format**
   - **Error**: `400 Bad Request`
   - **Message**: `"Unsupported image format"`
   - **Solution**: Ensure images are PNG/JPEG/WebP and include proper data URI prefix

4. **Checkpoint Not Found**
   - **Error**: `404 Not Found` or `500 Internal Server Error`
   - **Message**: `"Checkpoint file not found"`
   - **Solution**: Verify `ckpt_name` path is correct and file exists in `models/checkpoints/`

---

## Server Setup Guide

### Step 1: Clone Repository

```bash
git clone --recursive https://github.com/codejedi-ai/MagicQuill.git
cd MagicQuill
```

**Important**: Use `--recursive` flag to include LLaVA submodule.

### Step 2: Create Python Environment

```bash
conda create -n MagicQuill python=3.10 -y
conda activate MagicQuill
```

### Step 3: Install PyTorch with CUDA

```bash
pip install torch==2.1.2 torchvision==0.16.2 torchaudio==2.1.2 --index-url https://download.pytorch.org/whl/cu118
```

Verify installation:
```bash
python -c "import torch; print('CUDA available:', torch.cuda.is_available())"
```

### Step 4: Install Dependencies

```bash
# Install Gradio MagicQuill component
pip install gradio_magicquill-0.0.1-py3-none-any.whl

# Install LLaVA
cp pyproject.toml MagicQuill/LLaVA/
pip install -e MagicQuill/LLaVA/

# Install remaining dependencies
pip install -r requirements.txt
pip install fastapi uvicorn
```

### Step 5: Download Models

```bash
# Download models (25GB)
wget -O models.zip "https://hkustconnect-my.sharepoint.com/:u:/g/personal/zliucz_connect_ust_hk/EWlGF0WfawJIrJ1Hn85_-3gB0MtwImAnYeWXuleVQcukMg?e=Gcjugg&download=1"
unzip models.zip
```

Or download from HuggingFace:
```bash
# Set mirror for mainland users (optional)
export HF_ENDPOINT=https://hf-mirror.com

# Download from HuggingFace
# (Follow HuggingFace download instructions)
```

### Step 6: Verify Model Structure

Ensure your `models/` directory matches the structure in [Model Files & Dependencies](#model-files--dependencies).

### Step 7: Verify API Endpoints

The following endpoints are already implemented in `gradio_run.py`:
- ✅ `POST /magic_quill/guess_prompt` - Prompt suggestions
- ✅ `POST /magic_quill/process_background_img` - Image resizing
- ✅ `POST /magic_quill/generate` - Image generation
- ✅ `GET /magic_quill/checkpoints` - List available models
- ✅ `GET /magic_quill/config` - Get server config
- ✅ `POST /magic_quill/config` - Update server config
- ✅ `GET /magic_quill/health` - Health check

No additional code changes needed - all endpoints are ready to use!

### Step 8: Run Server

```bash
# Set GPU device (optional)
export CUDA_VISIBLE_DEVICES=0

# Run server
python gradio_run.py
```

Server will start on `http://127.0.0.1:7860` (or configure host/port in `uvicorn.run()`).

### Step 9: Test Endpoints

```bash
# Test health check
curl http://localhost:7860/magic_quill/health

# Test get available checkpoints
curl http://localhost:7860/magic_quill/checkpoints

# Test get config
curl http://localhost:7860/magic_quill/config

# Test guess_prompt
curl -X POST http://localhost:7860/magic_quill/guess_prompt \
  -H "Content-Type: application/json" \
  -d '{"original_image": "data:image/png;base64,..."}'

# Test process_background_img
curl -X POST http://localhost:7860/magic_quill/process_background_img \
  -H "Content-Type: application/json" \
  -d '"data:image/png;base64,..."'

# Test generate (full example)
curl -X POST http://localhost:7860/magic_quill/generate \
  -H "Content-Type: application/json" \
  -d '{
    "from_frontend": {
      "total_mask": "data:image/png;base64,...",
      "original_image": "data:image/png;base64,..."
    },
    "from_backend": {
      "prompt": "a beautiful landscape"
    },
    "params": {}
  }'
```

---

## Complete Request Example

### Full Generation Request

```json
{
  "from_frontend": {
    "total_mask": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
    "original_image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFBQIAX8jx0gAAAABJRU5ErkJggg==",
    "add_color_image": null,
    "add_edge_image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChAGA60e6kgAAAABJRU5ErkJggg==",
    "remove_edge_image": null
  },
  "from_backend": {
    "prompt": "a beautiful deer in a forest"
  },
  "params": {
    "ckpt_name": "SD1.5/realisticVisionV60B1_v51VAE.safetensors",
    "negative_prompt": "blurry, low quality",
    "fine_edge": "disable",
    "grow_size": 15,
    "edge_strength": 0.55,
    "color_strength": 0.55,
    "inpaint_strength": 1.0,
    "seed": -1,
    "steps": 20,
    "cfg": 5.0,
    "sampler_name": "euler_ancestral",
    "scheduler": "karras"
  }
}
```

### Minimal Generation Request

```json
{
  "from_frontend": {
    "total_mask": "data:image/png;base64,...",
    "original_image": "data:image/png;base64,...",
    "add_color_image": null,
    "add_edge_image": null,
    "remove_edge_image": null
  },
  "from_backend": {
    "prompt": "a beautiful landscape"
  },
  "params": {}
}
```

All `params` fields will use defaults if omitted.

---

## Summary Checklist

Before your server can run, ensure you have:

- [ ] Python 3.10 environment created
- [ ] PyTorch 2.1.2 with CUDA 11.8 installed
- [ ] All Python dependencies installed (`requirements.txt`, `gradio_magicquill`, LLaVA)
- [ ] All model files downloaded (~25GB) and placed in `models/` directory
- [ ] At least one checkpoint file in `models/checkpoints/SD1.5/`
- [ ] ControlNet models in `models/controlnet/`
- [ ] BrushNet models in `models/inpaint/brushnet/`
- [ ] LLaVA model in `models/llava-v1.5-7b-finetune-clean/`
- [ ] Preprocessor models in `models/preprocessor/`
- [ ] GPU with 8GB+ VRAM available
- [ ] All API endpoints are already implemented in `gradio_run.py` (no code changes needed)

Once all items are checked, your server is ready to accept API requests from mobile clients!

---

## References

- **GitHub Repository**: https://github.com/codejedi-ai/MagicQuill
- **Model Downloads**: https://huggingface.co/LiuZichen/MagicQuill-models
- **Original Paper**: MagicQuill: An Intelligent Interactive Image Editing System (CVPR 2025)

