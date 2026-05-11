Plugin app for Tasker and MacroDroid to provide Humans Detection

Licensed under GPL v3

---

## ✨ NEW in v1.9.0 — Fully local multimodal AI on your phone

Run **Google Gemma 4 E2B** — a state-of-the-art multimodal Vision-Language Model — entirely on your device, with **no cloud, no API keys, no data leaving your phone**. The model (~2.4 GB) is downloaded on demand from HuggingFace into the app's private storage and runs via [LiteRT-LM](https://github.com/google-ai-edge/LiteRT-LM) on the CPU.

Why this matters:
* 🔒 **Privacy**: images never leave the device — ideal for security camera footage, family photos, anything sensitive
* 💸 **Zero cost**: no Anthropic / Google / OpenRouter subscription required
* 📡 **Works offline**: useful when your automation runs without connectivity
* 🧠 **Real multimodal understanding**: can answer arbitrary questions about an image, not just "is there a person?"

Requirements: ~3 GB of free RAM at inference time, ~2.5 GB of free storage, arm64-v8a device (any reasonably modern Android phone). Download is triggered with one tap from the app's Config screen.

---

V 1.9.0:
* ADDED: **Gemma 4 E2B local multimodal engine** via LiteRT-LM 0.11 — on-device VLM, no cloud required (see highlight above)
* ADDED: ConfigActivity now shows/hides the Download/Delete buttons based on whether the model bundle is already present
* CHANGED: bumped Kotlin to 2.3.21 (required by LiteRT-LM AAR metadata)

V 1.8.0:
* FIXED: Gemini deprecated model ID migration (gemini-3-pro-preview → gemini-3.1-pro-preview)
* CHANGED: removed x86/x86_64 ABI filters, reducing APK size (no Android devices use x86 since 2018)

V 1.7.0:
* CHANGED: updated to using Claude Sonnet 4.6
* CHANGED: local detection engine migrated from TensorFlow Lite to MediaPipe Tasks Vision (same model, better 16KB ELF alignment)
* FIXED: PNG images now supported by Claude and Gemini engines

V 1.6.1:
* FIXED: OpenRouter option not appearing in the Tasker/MacroDroid config screens

V 1.6.0:
* ADDED: Notification listener can now optionally intercept notifications without images. Useful when the app sends a notification without images but the image can be fetched via API.
* ADDED: "Cancel Notification" action: useful to remove false positives' notifications.
* CHANGED: updated to using Gemini Flash 2.5 instead of 2.0
* CHANGED: updated to using Claude Sonnet 4.5 since 3.5 is being retired

V 1.5.0:
* ADDED: support OpenRouter.AI for cloud queries: allowing completely free online image detection!
        This is a very powerful addition since it allows to:
        - use free models
        - use almost any model which is vision-capable (future proof)
        - get higher uptime (Openrouter will use fallback logic on different providers for some models)
* FIXED: issue where NotificationInterceptor will only apply appName filter for the last edited filter!

V 1.4.0:
* FEATURE: Generate a Tasker Event when a notification with images is generated

V 1.3.0
* ADDED: new event allowing to perform a general purpose image analysis using Gemini or Claude

V 1.2.0
* ADDED: Google Gemini support
* IMPROVED: test file selector will remember the last image (also for file picker)

V 1.1.0
* remove OpenCV support
* added TensorFlow Lite support
* added Claude.AI (cloud) support: much better reliability!

V 1.0.2
* added file picker

Features:
* Provides a Tasker Event that will trigger when an app (configurable) creates a notification with an image: useful to start a person detection macro that will add advanced AI-features to non-AI enabled cameras
* Provides a Tasker action capable to open an image and return a score 0-100 in terms of how likely the image contains a person
* Provides a Tasker action that you can use to ask generic questions to Claude/Gemini regarding an image/text (e.g. "is the garage door closed?" or "is there a dog in the image?")
* simple home screen to test it against local images
* can parse file names in the form of content://media/external/images/something or in form file:///sdcard/somewhere/file.jpg
* can parse PNG and JPG (all engines)

Supported person detection/image analysis engines:
* **Gemma 4 E2B (local, multimodal VLM via LiteRT-LM)** ⭐ NEW in 1.9.0: runs entirely on the phone with no cloud, no API key and no data leaving the device. Multimodal VLM with much better accuracy than MediaPipe. Model bundle (~2.4 GB) is auto-downloaded on demand from HuggingFace into private app storage; ~3 GB of RAM needed at inference time
* MediaPipe (local, uses TensorFlow model): runs entirely on the phone, limited accuracy but very privacy-savvy and very low resource usage
* Claude Sonnet 4.6 (online): will send the data to Anthropic's cloud LLM, which can perform many complex tasks. Very accurate
* Gemini Flash 2.5 (online): will send the data to Google's cloud LLM, which can perform many complex tasks. Accurate and cheap
* OpenRouter (online): will send the data to OpenRouter's cloud, which in turn will forward to the LLM you have selected. This way you can choose the cost (from free to very expensive) and accuracy of the model

Limitations:
* uses old APIs so no Play store version: you can download pre-built APK from github
* permission and battery management is still rudimentary

Ideas for future improvements:
* [DONE] ~~Support for PNG with Claude/Gemini~~
* [DONE] ~~Support for Openrouter.ai~~
* [DONE] ~~support for running locally Google Gemma 4 E2B (multimodal VLM via LiteRT-LM, downloaded on demand from HuggingFace)~~
* [DISCARDED] ~~Support for ChatGPT-Vision~~ (superseded by OpenRouter allowing the use of ChatGPT Vision!)
* [DONE] ~~Support for generic Claude/ChatGPT actions~~

IMPORTANT Caveats:
* will use more battery than you want, until I understand why, the default plugin mechanism is not working as expected (problems with Foreground service)

HOW-TO use it:
* install the APK (you can download it from the GitHub releases)
* start it: so that it's registered and available to Tasker/Macrodroid
* (optional): go to settings and add your Claude/Gemini/OpenRouter API Key (if you want to be able to use these online models), see <How to create API keys for Google and Claude.md>
* within Macrodroid/Tasker
    * go to the task you want to use
    * add action > external app > HumanDetection4Tasker > Human Recognition
    * you get a window where to enter the name of the image, usually you'll want to use a variable instead of a hard-coded value (e.g. %my_image )
    * then you get a window where you say where to save the result, usually another variable (e.g. detection_score)
    * then do whatever you want with the information :-)

HOW-TO test it:
* start the app
* grant the permission it requires
* use the file picker to choose an image
* press the "test recognition" button
* see what's the score

Example usage for the generic question:
* image: the image you want to analyze
* system prompt:
* user prompt: <empty>


E.g. my use case is simple: I want to reduce to almost zero the false positive alarms of some security cameras
* listen for alerts from (cheap?) security cameras
* download the alert image locally
* pass the image to this plugin
* if the detection_score>=50 then start the siren/lights
* otherwise just ignore the false positive


E.g. the AI Image Analysis opens the door to more sophisticated workflows:
* Task: check if I remembered to park the car in the backyard or left it parked in the road in front of the house. I use the backyard security cam to check that
* image: the image from your security camera (I use my plugin here to do that: https://github.com/SimoneAvogadro/CloudEdge4Tasker )
* system prompt: Respond with a single word (CAR or NO_CAR) because your response will be fed into an automation workflow
* user prompt: Please analyze the image and respond with a single word: CAR or NO_CAR. If there's a car then return CAR, otherwise return NO_CAR
