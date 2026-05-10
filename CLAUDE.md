# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AWS4Tasker (formerly OpenCV4Tasker) is an Android plugin for Tasker and MacroDroid that provides AI-powered image analysis capabilities. The app can detect humans in images and perform general-purpose image analysis using multiple AI engines including Claude AI, Google Gemini, OpenRouter, MediaPipe (local), and Gemma 4 E2B (local, multimodal VLM via LiteRT-LM, officially supported).

## Build Commands

If a `LOCAL_BUILD.md` file exists in the project root, follow the instructions there for building (e.g. cross-environment setups like WSL2 + Windows). Otherwise, use the standard Gradle commands:

```bash
# Build the project
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

## Architecture Overview

### Core Components

- **Application Class**: `OpenCV4TaskerApplication` - Main application entry point with singleton pattern
- **Activities**: 
  - `SplashActivity` - Launcher activity
  - `MainActivity` - Main UI for testing image analysis
  - `ConfigActivity` - General configuration
- **AI Engines**: Multiple implementations of image analysis:
  - `HumansDetectorClaudeAI` - Claude AI integration (default model: `claude-sonnet-4-6`)
  - `HumansDetectorGemini` - Google Gemini integration (default model: `gemini-2.5-flash`)
  - `HumansDetectorOpenRouter` - OpenRouter integration (user-configurable model)
  - `HumansDetectorTensorFlow` - Local processing via MediaPipe Tasks Vision (class name kept for backward compatibility)
  - `HumansDetectorGemma4` - Local on-device multimodal VLM (Gemma 4 E2B) via LiteRT-LM, model auto-downloaded on demand from `litert-community/gemma-4-E2B-it-litert-lm` on HuggingFace
  - `AIImageAnalyzer` - Common interface for AI-based image analysis

### Tasker Plugin System

The app integrates with Tasker/MacroDroid through:
- **Actions**:
  - `DetectHumansActionHelper` - Human detection in images
  - `AnalyzeImageActionHelper` - General AI image analysis
  - `CancelNotificationActionHelper` - Cancels a notification by its key
- **Events**:
  - `NotificationInterceptedEvent` - Intercepts notifications (with or without images)
- **Configuration Activities**:
  - `ActivityConfigDetectHumansAction`
  - `ActivityConfigAnalyzeImageAction`
  - `ActivityConfigCancelNotificationAction`
  - `ActivityConfigNotificationInterceptedEvent`

### Notification Interception

The notification interception system includes:
- `NotificationInterceptorService` - Core notification listener service
- `NotificationImageExtractor` - Extracts images from notifications
- `NotificationFileManager` - Manages temporary image files

### Key Dependencies

- Tasker Plugin Library: `com.joaomgcd:taskerpluginlibrary:0.4.10`
- MediaPipe Tasks Vision: `com.google.mediapipe:tasks-vision:0.10.21` (local object detection, replaces TensorFlow Lite)
- LiteRT-LM: `com.google.ai.edge.litertlm:litertlm-android:0.11.0` (on-device multimodal LLM inference for Gemma 4 E2B). Requires Kotlin ≥ 2.3.x because the AAR is compiled with Kotlin metadata 2.3.0; the project pins Kotlin 2.3.21.
- AndroidX libraries for modern Android development
- Kotlin support with Java interop

## Development Notes

- **Target SDK**: 36, **Min SDK**: 30 (Android 11+)
- **Language**: Mixed Java/Kotlin codebase
- **Permissions**: Requires storage, internet, notification access, and battery optimization bypass
- **Build Tools**: Android Gradle Plugin 8.9.0, Gradle 8.11.1, Build Tools 35.0.0
- The project uses view binding and data binding
- MediaPipe model is stored in `app/src/main/assets/`
- Package name: `online.avogadro.opencv4tasker`

## Engine Configuration

The app supports five AI engines selected via radio buttons:
- **CLAUDE**: Cloud-based Claude AI analysis (`claude-sonnet-4-6` by default, configurable)
- **GEMINI**: Google Gemini integration (`gemini-2.5-flash` by default, configurable)
- **OPENROUTER**: OpenRouter cloud proxy (user-configurable model)
- **TENSORFLOW**: Local MediaPipe processing (default for backward compatibility; class/key name kept as `TENSORFLOW`)
- **GEMMA4**: Local on-device Gemma 4 E2B multimodal VLM via LiteRT-LM. Model bundle (`gemma-4-E2B-it.litertlm`, ~2.4 GB, Apache-2.0) is downloaded on demand from `litert-community/gemma-4-E2B-it-litert-lm` on HuggingFace into the app's private `filesDir/models/`. Download is triggered from `ConfigActivity`; no file picker, no storage permissions required. The radio button is auto-disabled when the model is missing. **Why Gemma 4 and not Qwen 3.5**: LiteRT-LM 0.11.0 only supports image input via `gemma3_data_processor`, `gemma4_data_processor`, `fastvlm_data_processor`. The `qwen3_data_processor` is text-only ("only support text modality"). Community ports of Qwen 3.5 multimodal exist but use mismatched bundle metadata that triggers `INVALID_ARGUMENT: Provided more images than expected in the prompt` at `nativeSendMessage` (image marker token mismatch).

Engine selection is persisted using `SharedPreferencesHelper` and each engine implements the `AIImageAnalyzer` interface for consistency.

## Model File Handling

- `Util.getModelPathFromUri()` resolves content URIs to filesystem paths using 4 strategies (MediaStore DATA, DownloadsProvider msf: lookup, /proc/self/fd symlink, DISPLAY_NAME in Download dirs) — used by file pickers in older flows; not used by Qwen 3.5 (auto-download path stays in app-private storage).
- `Util.isModelFileAccessible()` uses `canRead() || exists()` to handle Android 11+ scoped storage
- `Gemma4ModelDownloader` (in `online.avogadro.opencv4tasker.gemma4`) downloads the multimodal `.litertlm` bundle into `context.filesDir/models/gemma-4-E2B-it.litertlm` via `HttpURLConnection`, with `*.partial` staging file and atomic rename on completion. Pre-checks free disk space (model size + 100 MB headroom) before writing any bytes. Constants `MODEL_URL` / `MODEL_FILENAME` and helpers `isModelPresent()`, `deleteModel()`, `formatBytes()` live in its companion object.
- ABI filters: `armeabi-v7a` and `arm64-v8a` only (no x86/x86_64)