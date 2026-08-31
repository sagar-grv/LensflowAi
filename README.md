# 🌟 LensFlow AI — Camera-First On-Device Document Productivity & Cross-Device Bridge

> **Platform:** Android (Jetpack Compose & Kotlin) | **Vision Engine:** Google ML Kit Text Recognition | **Architecture:** Local Edge OCR + Cross-Device Bridge  

---

## 📌 Overview

**LensFlow AI** is a camera-first Android productivity tool that transforms physical documents, receipts, whiteboard brainstorms, business cards, and invoices into structured, actionable tasks and summaries directly on your device.

By utilizing **Google ML Kit's on-device OCR engine** paired with local heuristic and pattern-based entity extraction, LensFlow extracts dates, amounts, action items, and contacts locally in milliseconds without requiring mandatory internet connectivity. Extracted action items can be checked off, exported to clean PDF reports, or pushed directly to a paired PC's clipboard for seamless workflow continuation.

---

## ✨ Key Features

### ⚡ 1. Fast On-Device OCR & Entity Extraction
- **Local Text Recognition:** Powered by Google ML Kit (`com.google.mlkit:text-recognition`), running on-device for fast, zero-cloud text extraction.
- **Pattern-Based Entity Parsing:** Automatically recognizes currency totals, due dates, bulleted action items, email addresses, and phone numbers.
- **Privacy by Default:** Offline document processing ensures sensitive financial documents, business cards, and personal notes remain on-device.
- **Optional Cloud AI Mode:** Supports optional multimodal enrichment using the Google Gemini API when configured with an API key.

### 🎯 2. Structured Document Processing Modes
- **Receipts:** Identifies totals, tax lines, and generates expense reimbursement reminders.
- **Whiteboards & Brainstorms:** Converts bulleted lists and action items into actionable tasks.
- **Business Cards:** Parses names, email addresses, and phone numbers into one-tap follow-up items.
- **Invoices:** Highlights vendor info and invoice balances for approval workflows.
- **Notes & Agendas:** Structures meeting notes and handwritten lists into organized checklists.

### 💻 3. Cross-Device PC Bridge & Productivity Tools
- **Universal Clipboard Sync:** Copies formatted action items and summaries directly to the system clipboard for immediate desktop paste (`Ctrl+V` on laptop).
- **Standardized PDF Generation:** Creates formatted PDF summary reports using Android's native `PdfDocument` framework for easy sharing via system sheets.
- **Calendar & Email Export:** One-tap integration to create calendar reminders and draft emails from extracted action items.

### ♿ 4. Comprehensive Accessibility & Contrast Compliance
- **Screen Reader Ready (TalkBack):** Full semantic annotations (`heading`, `contentDescription`, `role`, `stateDescription`) across all interactive components.
- **WCAG AA Contrast Compliant:** Thoughtfully tuned Material 3 dark color scheme ensuring high-contrast readability (>4.5:1 for body text, >3:1 for graphical elements).
- **Accessible Touch Targets:** Minimum 48dp interactive touch targets across all buttons, chips, checkboxes, and navigation tabs.

### 🔴 5. Darkroom / Night Red-Light Mode
- Monochrome red-wavelength UI mode to reduce blue-light emission and preserve dark adaptation during low-light scanning sessions.

---

## 🛠️ Architecture & Tech Stack

```
LensFlow App Architecture
│
├── 📱 UI Layer (Jetpack Compose + Material 3)
│   ├── MD3 HomeScreen (Dashboard, Quick Presets, Filter Chips, Recent Scans)
│   ├── CameraX Viewfinder (Real-Time HUD, Corner Framing, Torch & Permissions)
│   ├── Action Items & Document Details (Interactive Checklists, PDF Generator)
│   ├── PC Link & Settings Hub (Clipboard Sync, Model & Offline Preferences)
│   └── System Performance & Scan Metrics
│
├── 🧠 Text & Action Extraction Pipeline
│   ├── Google ML Kit Text Recognition (On-Device Vision Pipeline)
│   ├── Regex & Rule-Based Heuristic Entity Parsers
│   └── Optional Cloud Gemini API Integration
│
└── 🔗 System & Hardware Integrations
    ├── CameraX Lifecycle & ImageCapture
    ├── AndroidX Print & PdfDocument Framework
    └── System Clipboard & Android Intent Services
```

### 🧩 Technologies Used:
- **Language:** 100% Kotlin
- **UI Toolkit:** Jetpack Compose with Material Design 3 (M3)
- **Computer Vision:** Google ML Kit Text Recognition (`com.google.mlkit:text-recognition:16.0.1`)
- **Camera Pipeline:** AndroidX CameraX (`camera-camera2`, `camera-lifecycle`, `camera-view`)
- **Accessibility:** Jetpack Compose Semantics, TalkBack optimization, 48dp touch targets
- **Testing:** Robolectric local JVM testing & automated unit test suite

---

## 🚀 Getting Started & Build Instructions

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17+
- Android SDK 35 (compileSdk 35, minSdk 26)

### Build Steps
```bash
# 1. Clone repository
git clone https://github.com/your-username/lensflow-ai.git
cd lensflow-ai

# 2. Build debug APK
./gradlew assembleDebug

# 3. Run automated unit & Robolectric tests
./gradlew testDebugUnitTest
```

---

## 🧪 Testing & Validation

The codebase includes an automated test suite verifying critical user journeys:
- `ExampleRobolectricTest.kt`: Validates string resources and on-device parsing algorithms across Receipts, Whiteboards, and Business Cards.
- Run tests: `./gradlew testDebugUnitTest`

---

## 📄 License
Distributed under the Apache 2.0 License. See `LICENSE` for more information.
