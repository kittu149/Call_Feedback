# Call Feedback

Call Feedback is an Android app (built with Android Studio) that shows an overlay feedback form after phone calls so that users can quickly rate call quality and report issues like drops, echo, or background noise.  
Feedback is sent to a backend server for logging/analysis.

---

## Table of Contents

- [Overview](#overview)
- [App Flow](#app-flow)
- [Project Structure](#project-structure)
- [Android Module Structure](#android-module-structure)
  - [Manifest & Permissions](#manifest--permissions)
  - [Java/Kotlin Code](#javakotlin-code)
  - [XML Layouts](#xml-layouts)
  - [Network Security Configuration](#network-security-configuration)
- [Backend (server.py)](#backend-serverpy)
- [Build & Run](#build--run)
  - [Using Android Studio](#using-android-studio)
  - [Using Command Line (Gradle)](#using-command-line-gradle)
- [Notes & Future Improvements](#notes--future-improvements)

---

## Overview

This project contains:

- An **Android app module** (`app/`) that:
  - Listens to phone call state changes.
  - Shows a **system overlay** with a feedback form on top of other apps.
  - Captures:
    - Overall call quality (Good/Fair/Poor)
    - Common audio issues (dropped call, echo, background noise, etc.)
    - Environment (indoor/outdoor/vehicle/noisy/quiet)
    - Free-form comments :contentReference[oaicite:0]{index=0}
  - Sends feedback to a backend server.

- A **simple backend script** (`server.py`) at the repo root, intended to receive and store feedback from the app. :contentReference[oaicite:1]{index=1}

---

## App Flow

High-level flow of the Android app:

1. **User opens the app**  
   - `MainActivity` is the launcher activity. It likely shows a simple screen with a *“Configure Overlay”* button (bound to `activity_launcher.xml`) that starts the overlay permission/configuration flow.   

2. **Overlay Permission**  
   - `OverlayPermissionActivity` is used to request **Draw over other apps** permission (`SYSTEM_ALERT_WINDOW`) so the app can display an overlay feedback form. :contentReference[oaicite:3]{index=3}  

3. **Call Detection**  
   - `PhoneCallReceiver` is a broadcast receiver registered for `android.intent.action.PHONE_STATE` and uses `READ_PHONE_STATE` permission to detect when a phone call ends. :contentReference[oaicite:4]{index=4}  

4. **Overlay Feedback Form**  
   - On relevant call state changes (e.g., after call ends), `PhoneCallReceiver` triggers `OverlayService`.
   - `OverlayService` shows the overlay UI defined in `overlay_feedback.xml`, which contains:
     - Q1: Overall Quality (Good/Fair/Poor) via `RadioGroup`
     - Q2: Audio Issues (multiple `CheckBox` options)
     - Q3: Environment (indoor/outdoor/vehicle/noisy/quiet)
     - Q4: Additional comments (`EditText`)
     - Buttons: **Submit** and **Close** :contentReference[oaicite:5]{index=5}  

5. **Saving & Posting Feedback**
   - `FormLocalSaver`: helper class (based on its name) used to save filled feedback locally (e.g., SharedPreferences/file/DB).
   - `ServerPoster`: helper class intended to post the feedback to the backend server (likely using the IP configured in `network_security_config.xml`). :contentReference[oaicite:6]{index=6}  

---

## Project Structure

At the repo root you’ll see a typical Android/Gradle layout: :contentReference[oaicite:7]{index=7}  

```text
Call_Feedback/
├── .idea/                  # Android Studio project files
├── app/                    # Android app module
├── gradle/                 # Gradle wrapper files
├── .gitignore
├── build.gradle            # Top-level Gradle build script
├── gradle.properties
├── gradlew                 # Gradle wrapper (Unix)
├── gradlew.bat             # Gradle wrapper (Windows)
├── server.py               # Simple backend server to receive feedback
├── feedbacks.jsonl         # Sample/collected feedback data in JSON Lines format
└── settings.gradle         # Includes the app module
