
# 📞 Call Feedback Android App

Call Feedback is an Android app (built with Android Studio) that shows an overlay feedback form after phone calls so that users can quickly rate call quality and report issues like drops, echo, or background noise.
Feedback is sent to a backend server for logging/analysis.

## 💡 Overview

This repository contains two main components:

1. Android App Module (app/): The core application responsible for call state detection, overlay rendering, and data submission.

2. Simple Backend Server (server.py): A lightweight Python script intended to receive, log, and store the JSON feedback payloads.

## Key Features

1. Post-Call Trigger: Automatically detects when a call disconnects using READ_PHONE_STATE.

2. System Overlay: Displays a non-intrusive feedback form over any application using SYSTEM_ALERT_WINDOW.

3. Data Capture: Collects structured data on quality (Good/Fair/Poor), audio issues, environment, and free-form comments.

4. Local & Remote Logging: Supports local saving for offline operation and network posting to the backend.

## ⚙️ App Flow: From Call End to Data Submission

The application follows a defined lifecycle to ensure the feedback prompt is timely and reliable.

1. Overlay Permission Setup: The user uses MainActivity to navigate to OverlayPermissionActivity and grant the Draw over other apps (`SYSTEM_ALERT_WINDOW`) permission.

2. Call Detection: PhoneCallReceiver (a BroadcastReceiver listening to `android.intent.action.PHONE_STATE`) detects the transition from an active call state to a disconnected state.

3. Service Trigger: The PhoneCallReceiver starts the OverlayService.

4. Overlay Display: OverlayService inflates the UI defined in `overlay_feedback.xml` and displays it using the Android WindowManager.

5. Data Submission: Upon clicking the "Submit" button:

    - `FormLocalSaver` handles local storage (e.g., for retry logic).

    - `ServerPoster` packages the data into a JSON payload and sends an HTTP POST request to the backend.

## 📂 Project Structure

A high-level view of the repository layout:

```text

Call_Feedback/
├── app/                        # Main Android Application Module
│   ├── src/main/
│   │   ├── AndroidManifest.xml # Permissions & component declarations
│   │   ├── java/               # Java/Kotlin Source Code (Activities, Services, etc.)
│   │   └── res/                # Resources (Layouts, XML configs)
├── gradle/                     # Gradle Wrapper files
├── build.gradle                # Top-level Gradle configuration
├── settings.gradle             # Includes the app module
├── server.py                   # Simple Python backend server
└── feedbacks.jsonl             # Log file for received feedback data
```

## 📱 Android Module Details

`AndroidManifest.xml` (Permissions & Components)

Required Permissions:

| Permission                     | Purpose                                                                 |
|--------------------------------|-------------------------------------------------------------------------|
| `READ_PHONE_STATE`             | Mandatory for detecting call state changes.                              |       
| `SYSTEM_ALERT_WINDOW`          | Mandatory for drawing the overlay UI over other apps.                   |
| `INTERNET`                     | Required to post feedback data to the backend server.                   |
| `ACCESS_NETWORK_STATE`         | Recommended for checking network availability before posting.           |
| `ACCESS_COARSE_LOCATION`       | Optional: Captures approximate device location for feedback metadata.   |
| `ACCESS_FINE_LOCATION`         | Optional: Captures precise location metadata during feedback.           |
| `ACCESS_BACKGROUND_LOCATION`   | Optional: Needed if location is captured when the app is in background. |



## Key Components:

- Activities: `MainActivity`, `OverlayPermissionActivity`

- BroadcastReceiver: `PhoneCallReceiver`

- Service: `OverlayService` (handles the floating UI)

## Backend Integration Configuration

The application uses an inline Network Security Configuration to permit cleartext (HTTP) traffic to a specific development IP:

`android:networkSecurityConfig="@xml/network_security_config"`

This configuration is defined in `app/src/main/res/xml/network_security_config.xml` and currently targets: http://`IP ADDR`/. This must be updated to HTTPS for production environments.

## Example Feedback Payload

The `ServerPoster` utility constructs and sends a JSON object similar to the structure below:

```text
{
  "overall_quality": "Good",
  "audio_issues": "There was background noise",
  "environment": "In Vehicle",
  "comments": "",
  "location": "Location not available",
  "connection_type": "WiFi",
  "signal_strength": "Unavailable",
  "timestamp": 1763575301351
}
```

## 🌐 Backend (`server.py`)

The backend script is a simple Python server designed to run locally for development and logging purposes.

### Running the Server

To start the server and begin receiving feedback:

`python server.py`

> **Note:** Ensure that the IP address in your Android app's `network_security_config.xml`
and `OverlayService.java` matches the machine running `server.py`, and that necessary firewall ports are open.
> Received feedback is appended to the `feedbacks.jsonl`(Which will be present in the same directory as that of `server.py`) file in **JSON Lines** format.

---

## 🛠 Build & Run Instructions

### **Prerequisites**
- Android Studio (**Flamingo or newer** recommended)
- Android SDK installed
- A physical device or emulator with phone/call state capability
- Python 3 (for running `server.py` during testing)

---

## 🚀 Using Android Studio

1. **Open Project**  
   *File → Open → Select the `Call_Feedback` folder*

2. **Sync & Build**  
   Wait for Gradle sync to complete.  
   If required: *File → Sync Project with Gradle Files*

3. **Run**  
   Select the **app** run configuration → Click **Run ▶**

---

## 💻 Using Command Line (Gradle)

Run all commands from the project root directory (`Call_Feedback/`).

| Action                     | macOS/Linux             | Windows              | Output Location                                           |
|---------------------------|--------------------------|----------------------|-----------------------------------------------------------|
| **Clean Project**         | `./gradlew clean`        | `gradlew clean`      | N/A                                                       |
| **Build Debug APK**       | `./gradlew assembleDebug`| `gradlew assembleDebug` | `app/build/outputs/apk/debug/app-debug.apk`             |
| **Build Release APK**     | `./gradlew assembleRelease` | `gradlew assembleRelease` | `app/build/outputs/apk/release/app-release-unsigned.apk` |
| **Full Build (Tests + Assemble)** | `./gradlew build` | `gradlew build` | N/A |

---

## 📌 Notes & Future Roadmap

- **Security**  
  Cleartext (HTTP) traffic is currently enabled for development.  
  **MUST** migrate to **HTTPS** for production.

- **Backend Improvements**  
  Enhance `server.py` with:
  - request validation
  - structured error responses
  - database integration instead of `feedbacks.jsonl`

- **Accessibility**  
  Review `overlay_feedback.xml` for better accessibility & touch target sizing.
