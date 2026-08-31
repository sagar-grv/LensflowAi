# 🌟 LensFlow AI — Camera Document Productivity & Cross-Device Bridge

> **Platform:** Android (Jetpack Compose & Kotlin) | **Vision Engine:** Google ML Kit On-Device OCR + Google Gemini 2.5 Flash Cloud AI | **Database:** Room Local SQLite | **Architecture:** Clean MVVM + Repository Pattern

---

## 📌 Overview

**LensFlow AI** is a camera-first Android productivity application designed to convert physical documents, receipts, whiteboard notes, business cards, and invoices into structured, actionable digital workflows and checklists directly on your device.

LensFlow features a **flexible dual-engine architecture**:
1. **On-Device Offline OCR** via Google ML Kit (`com.google.mlkit:text-recognition`) with zero cloud latency and complete data privacy.
2. **Online Cloud Multimodal AI** powered by Google Gemini 2.5 Flash via Google AI Studio API for advanced multi-category extraction and summaries.

Scanned documents and action items persist locally in SQLite via **Android Jetpack Room**, offering real-time search, interactive task tracking, cross-device PC clipboard sync, and native PDF document export.

---

## ✨ Key Features

### ⚡ 1. Dual AI Engine: On-Device ML Kit & Google Gemini 2.5 Flash
- **On-Device Privacy Engine:** Powered by Google ML Kit on-device Text Recognition, extracting text from camera frames and gallery images entirely on the client device.
- **Google Gemini Cloud AI:** Integrated Google AI Studio API support. Add your API key in **PC Link & Settings** or via the home screen mode switcher to unlock cloud AI extraction with Gemini 2.5 Flash.
- **Dynamic Entity Parsing:** Real-time extraction of financial totals (`$`, `€`, `£`, `¥`), dates, bulleted checklist items (`•`, `-`, `1.`), business card contact information (emails and phone numbers), and invoice balance records.
- **API Key Management:** Secure key storage with live connection testing, key visibility toggle, and one-tap access to Google AI Studio.

### 🗄️ 2. Room Database Local Persistence
- **Offline-First Storage:** Scanned documents, OCR text, timestamps, and interactive checklist states persist locally in SQLite via Room.
- **Reactive State Flow:** Room DAOs stream updates directly to Jetpack Compose UI via Kotlin `Flow` and `StateFlow`.
- **Full CRUD Support:** Create, read, update action item states, and delete scans with automatic state synchronization.

### 🔍 3. Interactive Productivity & Live Search
- **Instant Search:** Real-time query filtering across document titles, raw text, and extracted action items.
- **Interactive Checklists:** Tap checkboxes to check off tasks directly on the home screen, document detail cards, or the unified checklist screen.
- **Task Filtering:** Filter action items by status (*All*, *Pending*, *Completed*).
- **Batch Productivity Actions:** "Mark All Done", "Copy Checklist", and "Add Custom Action Task" affordances.

### 📷 4. CameraX Viewfinder & Photo Import
- **Live Framing HUD:** Real-time alignment corners, animated laser scan overlay, and quick document aspect guide.
- **Hardware Controls:** Flashlight / torch toggle and front/back camera lens switching.
- **System Photo Picker:** Seamless import of existing photos and high-resolution document images from the device gallery.
- **Quick Preset Samples:** Built-in one-tap samples for Receipts, Whiteboards, Business Cards, and Invoices.

### 💻 5. Cross-Device PC Bridge & Productivity Tools
- **Universal Clipboard Sync:** Copies formatted document checklists directly to the system clipboard for immediate desktop paste (`Ctrl+V` on laptop/PC).
- **Native PDF Report Generation:** Creates formatted PDF summary reports using Android's native `PdfDocument` framework for sharing via Android system sheets.
- **System Intent Integrations:** One-tap export to Google Calendar events and Email draft clients.

### ♿ 6. Accessibility, Contrast & Night Vision
- **Screen Reader Ready (TalkBack):** Full semantic annotations (`heading`, `contentDescription`, `role`, `testTag`) across all interactive components.
- **WCAG AA Contrast Compliant:** High-contrast Material Design 3 dark palette (>4.5:1 text contrast).
- **Accessible Touch Targets:** Minimum 48dp interactive touch targets across all buttons, chips, checkboxes, and navigation tabs.
- **Darkroom Red-Light Mode:** Specialized red-wavelength UI mode to preserve scotopic vision in dark environments.

---

## 🛠️ Architecture & Tech Stack

```
LensFlow App Architecture
│
├── 📱 UI Layer (Jetpack Compose + Material 3)
│   ├── HomeScreen (Dashboard, Search, Mode Switcher, Filter Chips, Scanned Documents)
│   ├── CameraScreen (CameraX Viewfinder, Torch, Lens Toggle, Photo Picker)
│   ├── ResultDetailsScreen (Interactive Checklists, Raw OCR Text, PDF & Clipboard Export)
│   ├── TasksScreen (Aggregated Action Item Checklist, Filter, Add Task & Progress Metrics)
│   └── PcSettingsScreen (Gemini API Key Setup, Clipboard Sync, Mirroring, Red-Light Mode)
│
├── 🧠 Domain & Vision Pipeline
│   ├── MlKitOcrEngine (Google ML Kit On-Device Text Recognition)
│   ├── SmartEntityParser (Dynamic Regex & Heuristic Action Item Extraction)
│   └── PdfExportService (Native Android PdfDocument Report Generator)
│
├── 🗄️ Data Layer (Room Persistence)
│   ├── LensFlowDatabase (Room Database with Type Converters)
│   ├── ScanDao (Reactive Flow queries, Insert, Update, Delete)
│   └── ScanRepository (Unidirectional Data Flow Repository)
│
└── 🧪 Testing Suite (Robolectric & Roborazzi)
    ├── ScanRepositoryTest (Room In-Memory Database CRUD & Action Toggle Tests)
    ├── ExampleRobolectricTest (SmartEntityParser Dynamic Extraction Validation)
    └── GreetingScreenshotTest (Roborazzi Visual Regression Verification)
```

---

## 🔑 Google Gemini API Configuration

To enable Gemini Cloud AI Mode:

1. Navigate to **PC Link & Settings** in the bottom navigation bar (or tap the engine chip on the Home screen).
2. Enter your API key from [Google AI Studio](https://aistudio.google.com/app/apikey).
3. Tap **"Test Connection"** to verify connectivity with Google AI Studio's Gemini 2.5 Flash endpoint.
4. Tap **"Save Key"** to activate Cloud AI extraction mode.

*Note: You can switch back to 100% offline on-device OCR at any time with the mode toggle.*

---

## 🚀 Getting Started & Build Instructions

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17+
- Android SDK 35 (`compileSdk = 35`, `minSdk = 26`)

### Build Commands
```bash
# 1. Build debug APK
gradle assembleDebug

# 2. Run automated test suite (Unit, Room DAO, Robolectric & Roborazzi)
gradle :app:testDebugUnitTest

# 3. Verify screenshot tests
gradle :app:verifyRoborazziDebug
```

---

## 🧪 Automated Testing

The codebase is covered by automated unit and Robolectric tests:
- **`ScanRepositoryTest.kt`**: Tests in-memory Room database insertion, retrieval, item checkbox toggles, and deletion.
- **`ExampleRobolectricTest.kt`**: Verifies dynamic parsing of receipts, whiteboards, business cards, and string resources.
- **`GreetingScreenshotTest.kt`**: Verifies UI layout rendering using native Roborazzi capture on Robolectric.

---

## 📄 License
Distributed under the Apache 2.0 License.

