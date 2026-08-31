# 🌟 LensFlow AI — Camera-First On-Device Document Productivity & Cross-Device Bridge

> **Platform:** Android (Jetpack Compose & Kotlin) | **Vision Engine:** Google ML Kit Text Recognition | **Database:** Room Local SQLite | **Architecture:** Clean MVVM + Repository Pattern

---

## 📌 Overview

**LensFlow AI** is a camera-first Android productivity application designed to convert physical documents, receipts, whiteboard notes, business cards, and invoices into structured, actionable digital workflows and checklists directly on your device.

By leveraging **Google ML Kit's on-device OCR engine** (`com.google.mlkit:text-recognition`) paired with real-time heuristic entity extraction, LensFlow dynamically identifies amounts, line items, dates, bulleted action items, emails, and phone numbers in sub-50ms inference time without relying on cloud services. Scanned documents and action items persist locally via **Android Jetpack Room**, with one-tap cross-device clipboard sync and PDF export.

---

## ✨ Key Features

### ⚡ 1. Real On-Device OCR & Dynamic Entity Extraction
- **Zero-Cloud Vision Pipeline:** Powered by Google ML Kit on-device Text Recognition, extracting text from camera frames and gallery images entirely on the client processor.
- **Dynamic Entity Parsing:** Real-time extraction of financial totals (`$`, `€`, `£`, `¥`), dates, bulleted checklist items (`•`, `-`, `1.`), business card contact information (emails and phone numbers), and invoice balance records.
- **Privacy by Default:** Sensitive expense receipts, internal whiteboards, and client documents remain local on your device.
- **Optional Cloud AI Mode:** Supports optional multimodal enrichment using Google Gemini Flash when an API key is provided.

### 🗄️ 2. Room Database Local Persistence
- **Offline-First Storage:** Scanned documents, OCR text, timestamps, latency metrics, and interactive checklist states persist locally in SQLite via Room.
- **Reactive State Flow:** Room DAOs stream updates directly to Jetpack Compose UI via Kotlin `Flow` and `StateFlow`.

### 📷 3. CameraX Viewfinder & Photo Import
- **Live Framing HUD:** Real-time alignment corners, animated laser scan overlay, and quick document aspect guide.
- **Hardware Controls:** Flashlight / torch toggle and front/back camera lens switching.
- **System Photo Picker:** Seamless import of existing photos and high-resolution document images from the device gallery.

### 💻 4. Cross-Device PC Bridge & Productivity Tools
- **Universal Clipboard Sync:** Copies formatted document checklists directly to the system clipboard for immediate desktop paste (`Ctrl+V` on laptop/PC).
- **Native PDF Report Generation:** Creates formatted PDF summary reports using Android's native `PdfDocument` framework for sharing via Android system sheets.
- **Intent Integrations:** One-tap export to native Google Calendar events and Email draft clients.

### ♿ 5. Accessibility, Contrast & Night Vision
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
│   ├── HomeScreen (Dashboard, Telemetry, Filter Chips, Recent Scans)
│   ├── CameraScreen (CameraX Viewfinder, Torch, Lenses, Photo Picker)
│   ├── ResultDetailsScreen (Interactive Checklists, Raw OCR Text, PDF & Clipboard Export)
│   ├── TasksScreen (Aggregated Action Item Checklist & Progress Metrics)
│   └── PcSettingsScreen (Clipboard Sync, Mirroring, Red-Light Mode, Engine Preferences)
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

## 🚀 Getting Started & Build Instructions

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17+
- Android SDK 35 (compileSdk 35, minSdk 26)

### Build & Run
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

The project includes unit, repository, and screenshot tests:
- **`ScanRepositoryTest.kt`**: Tests in-memory Room database insertion, retrieval, item checkbox toggles, and deletion.
- **`ExampleRobolectricTest.kt`**: Verifies dynamic parsing of receipts, whiteboards, business cards, and string resources.
- **`GreetingScreenshotTest.kt`**: Verifies UI layout rendering using native Roborazzi capture on Robolectric.

---

## 📄 License
Distributed under the Apache 2.0 License.
