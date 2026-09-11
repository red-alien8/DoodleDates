# 📔 Diary Calendar 2026: Your Year as a Canvas

## 🚀 Overview
**Diary Calendar 2026** is a native Android application that redefines the traditional digital diary. Instead of rigid lists, it provides a free-form interactive canvas layered over your calendar. You can draw, write rich-text sticky notes, and set location-independent reminders—all while staying informed about local holidays and weekends.

---

## ✨ Key Features

### 🖌️ Interactive Drawing Layer
- **Pressure-Sensitive Ink:** Natural writing experience with finger or stylus support.
- **Precise Eraser:** Pixel-perfect removal of ink using `BlendMode.Clear`.
- **Resolution Independent:** All drawings are stored as relative fractions (0..1), ensuring they look perfect on any screen size or orientation.
- **Coarse Undo/Redo:** In-memory snapshot stack to quickly fix mistakes.

### 🗒️ Dynamic Sticky Notes
- **Free Placement:** Tap anywhere in the workspace to drop a note.
- **Movable & Resizable:** Dedicated headers for dragging and a robust corner handle for resizing.
- **Rich Text Formatting:** Support for **Bold**, *Italic*, and varied alignments.
- **Internal Scrolling:** Long content stays readable within the note without overflowing.

### ✍️ Full-Screen Editor
- **Distraction-Free:** A dedicated overlay for comfortable, long-form writing.
- **Real-time Sync:** Changes are instantly saved and reflected on the workspace canvas.
- **Auto-Focus:** Instantly brings up the keyboard for immediate input.

### ⏰ Advanced Alarms & Reminders
- **Contextual Reminders:** Tie an alarm directly to a specific note.
- **Immersive Alarm Screen:** A beautiful, full-screen alert with animated gradients and swipe-to-dismiss.
- **Customization:** Support for custom ringtones and vibration patterns.
- **Boot Persistence:** Alarms are automatically re-registered after a device reboot.

### 💾 Data Security & Portability
- **JSON Backup:** Export your entire diary (strokes and notes) to a JSON file for safe keeping.
- **Seamless Import:** Restore your backup anytime to recover your memories on a new device.
- **Offline First:** Your data never leaves your device unless you choose to export it.

### 🇧🇩 Regional Intelligence & multi-year
- **Bangladesh Holidays:** Automatically highlights government holidays (e.g., Shaheed Day, Eid-ul-Fitr) in rose/red.
- **Weekend Support:** Teal-tinted highlighting for Fridays and Saturdays.
- **Islamic Date Awareness:** Notations for moon-sighting dependent dates.
- **Yearly Coverage:** Full support for 2026, with an extended calendar up to 2030.

---

## 🛠️ Technology Stack (What's Under the Hood?)

### **Core Language & UI**
- **Kotlin:** The primary language for modern Android development.
- **Jetpack Compose:** A fully declarative UI toolkit (Material 3) for reactive and smooth interfaces.
- **Fraunces Serif Font:** Custom bundled font for a classic, diary-like aesthetic.

### **Architecture & DI**
- **MVVM (Model-View-ViewModel):** Separation of concerns for high maintainability.
- **Hilt (Dagger):** Dependency Injection for decoupled and testable components.
- **Repository Pattern:** Centralized data access layer wrapping Room and DataStore.

### **Data Persistence**
- **Room Database:** SQLite-backed local storage for strokes and notes (Source of Truth).
- **Jetpack DataStore:** Preferences storage for persistent theme settings and alarm preferences.
- **Kotlinx Serialization:** For JSON encoding/decoding of drawing data and backup files.

### **Navigation & Lifecycle**
- **Jetpack Navigation-Compose:** Type-safe routing between Dashboard, Workspace, and Theme screens.
- **Lifecycle Observers:** Auto-saving data on `onStop` to prevent data loss.

### **System Integration**
- **AlarmManager:** For exact and power-efficient scheduling of reminders.
- **Broadcast Receivers:** To handle system events like `BOOT_COMPLETED` and alarm triggers.
- **Notifications API:** High-priority alerts with full-screen intent support.
- **Content Resolver:** For interacting with the Android file system during import/export.

---

## 🎨 Visual Identity
- **Glassmorphic Toolbar:** Semi-transparent, frosted UI for drawing tools.
- **Immersive Alarms:** Dynamic background animations that grab your attention.
- **Dynamic Themes:**
    - **Bound Desk Diary:** Dark navy ink and warm paper.
    - **Morning Paper:** Clean white and light gray.
    - **Custom:** Full control over Background, Paper, and Accent colors.

---

## 🔒 Privacy & Performance
- **Zero Tracker Policy:** No account or internet connection required. 
- **Debounced Saving:** Optimized database writes (~700ms delay) to ensure performance.
- **Low Footprint:** Efficient rendering that cleans up off-screen components.
