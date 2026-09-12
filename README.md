_# 📔 Diary Calendar 2026: Your Year as a Canvas

## 🚀 Project Overview
**Diary Calendar 2026** is a native Android application designed to bridge the gap between traditional paper journaling and digital convenience. Built for the modern user, it provides a free-form interactive canvas layered over a calendar, allowing for handwritten notes, rich-text sticky notes, and precise reminders.

---

## ✨ Key Features & Functionality

### 🖌️ Interactive Drawing Engine
- **Free-hand Writing:** Capture thoughts naturally with finger or stylus support.
- **Precision Tools:** Switch between a high-fidelity Pen and a pixel-perfect Eraser.
- **Resolution Independence:** Ink is stored as relative fractions, ensuring your drawings look sharp on any device, from phones to tablets.

### 🗒️ Smart Sticky Notes
- **Movable & Resizable:** Drag notes to any date or margin; resize them to fit your content.
- **Rich Formatting:** Style your text with Bold, Italic, and varying alignments.
- **Pastel Palette:** Color-code your thoughts with soft, eye-pleasing background presets.

### ⏰ Advanced Reminder System
- **Immersive Alarms:** Full-screen animated alerts that demand attention without being intrusive.
- **Snooze & Deep Link:** Snooze reminders or tap to jump directly into the relevant workspace.
- **Persistence:** Alarms are automatically restored if your phone restarts.

### 🇧🇩 Regional Intelligence
- **Bangladesh Holidays:** Pre-configured with all 2026 government holidays and weekends (Fri/Sat).
- **Multi-Year Support:** Seamlessly navigate and journal from 2026 through 2030.

### 💾 Data Portability
- **JSON Export/Import:** Move your entire year of data between devices with simple backup files.
- **Offline First:** 100% private. No accounts, no internet, no trackers.

---

## 🛠️ Technology Stack

| Layer | Technology | Purpose |
| :--- | :--- | :--- |
| **Language** | **Kotlin** | Modern, concise, and safe native development. |
| **UI Framework** | **Jetpack Compose** | Material 3 declarative UI for reactive components. |
| **Architecture** | **MVVM** | Decouples business logic from the user interface. |
| **Dependency Injection** | **Hilt (Dagger)** | Manages object creation and scoping across the app. |
| **Persistence (Data)** | **Room (SQLite)** | High-performance local storage for strokes and notes. |
| **Persistence (Prefs)** | **DataStore** | Modern replacement for SharedPreferences for settings. |
| **Serialization** | **Kotlinx.Serialization** | Converts complex objects (like drawing points) to JSON. |
| **System APIs** | **AlarmManager** | Schedules exact reminders for sticky notes. |

---

## 📁 Project Structure

```text
com.spycodedoodledates/
├── data/
│   ├── local/              # Room Database, DAOs, and Entities
│   └── repository/         # Single source of truth for UI data
├── di/                     # Hilt Modules for dependency management
├── domain/
│   └── model/              # Pure data models and holiday logic
├── notifications/          # BroadcastReceivers for Alarms and Boot
├── ui/
│   ├── home/               # 12-month dashboard screens
│   ├── workspace/          # Canvas and drawing components
│   ├── settings/           # Data backup and theme selection
│   └── theme/              # Material 3 styling and color settings
└── MainActivity.kt         # App entry point and Navigation host
```

---

## 💡 Code Logic Explained

### 1. The "Canvas" Coordinate System
To ensure drawings aren't "squashed" when you rotate your screen, we don't store pixels. We store **fractions**.

```kotlin
// Instead of storing X=500px, Y=800px...
// We store values between 0.0 and 1.0 relative to screen size.
data class DrawingPoint(
    val xFraction: Float, // e.g. 0.5 (middle of screen)
    val yFraction: Float, // e.g. 0.2 (top of screen)
    val pressure: Float   // How hard you pressed the stylus
)
```

### 2. The Persistent Reminder Flow
When you set a reminder, the app "talks" to the Android System to ensure it wakes up even if the app is closed.

1. **Schedule:** `AlarmManager` registers a specific time.
2. **Trigger:** The system sends a signal to our `NoteAlarmReceiver`.
3. **Notify:** The receiver builds a `Notification` with a **Full Screen Intent**.
4. **Boot:** If the phone dies, `BootReceiver` re-reads the database and re-schedules every future alarm.

### 3. Reactive UI with Kotlin Flows
The UI never "asks" for data; it "observes" it.

```kotlin
// In the Repository
fun getNotesForMonth(monthKey: String): Flow<List<NoteEntity>> = dao.getNotes(monthKey)

// In the UI
val notes by viewModel.notes.collectAsState() 
// When the database changes, the UI updates instantly!
```

---

**Summary:** This APK is a robust, privacy-focused productivity tool that leverages the latest Android technologies to provide a premium, paper-like experience on a digital device._
