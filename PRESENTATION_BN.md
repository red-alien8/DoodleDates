# DoodleDates: Presentation Guide ও Bangla Script

এই document-টি Android-এ নতুন একজনের জন্য তৈরি। Presentation-এ code দেখানোর সময় file path-গুলো ব্যবহার করো। Project-টির current codebase অনুযায়ী explanation দেওয়া হয়েছে।

## ১. এক মিনিটে Project পরিচয়

**DoodleDates** একটি offline-first Android diary calendar। ব্যবহারকারী বছরের মাসগুলো দেখতে পারে, কোনো মাস খুলে calendar-এর ওপর handwriting/drawing করতে পারে, sticky note লিখতে পারে, note-এ reminder দিতে পারে, theme বদলাতে পারে এবং JSON backup export/import করতে পারে।

সব data মূলত device-এর local Room database-এ থাকে। Theme, ringtone ও vibration preference DataStore-এ থাকে। কোনো server/API বা user account বর্তমানে নেই।

এক লাইনের presentation pitch:

> “DoodleDates হলো calendar, handwritten canvas এবং sticky-note reminder-কে একসাথে করা একটি local-first digital diary।”

## ২. আগে Android-এর সহজ mental model

একটি Android app-কে এভাবে ভাবো:

```text
User touch/click
    ↓
Jetpack Compose UI
    ↓
ViewModel
    ↓
Repository
    ↓
Room DAO / DataStore / Android system
    ↓
Database, preferences, alarm বা notification
```

- **Activity**: Android app-এর entry point বা screen host।
- **Composable**: UI আঁকার Kotlin function। Compose state বদলালে UI আবার আঁকে।
- **ViewModel**: screen-এর state ও business action ধরে রাখে; rotation বা screen recreation-এর সময় data logic আলাদা রাখে।
- **Repository**: UI-কে database-এর query details থেকে আলাদা করে।
- **Room**: SQLite database ব্যবহারের type-safe Android layer।
- **DataStore**: ছোট preference/settings রাখার asynchronous storage।
- **Hilt**: object dependency কে তৈরি করবে এবং কোথায় inject হবে তা manage করে।
- **Flow/StateFlow**: data পরিবর্তন হলে UI-কে স্বয়ংক্রিয়ভাবে জানায়।

## ৩. Folder structure

```text
DoodleDates/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/spycodedoodledates/
│       │   ├── MainActivity.kt
│       │   ├── DiaryCalendarApp.kt
│       │   ├── di/
│       │   ├── data/
│       │   ├── domain/
│       │   ├── notifications/
│       │   ├── ui/
│       │   └── widgets/
│       └── res/
├── data/build.gradle.kts
├── domain/build.gradle.kts
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml
└── PRESENTATION_BN.md
```

### গুরুত্বপূর্ণ current-state note

`settings.gradle.kts`-এ `:data` এবং `:domain` module include করা আছে, কিন্তু তাদের `src/main`-এ বর্তমানে source নেই। বাস্তব database/domain source-গুলো `app/src/main/java/com/spycodedoodledates/data` এবং `.../domain` package-এর মধ্যে আছে। তাই presentation-এ বলবে:

> “Project-এ data এবং domain-এর package-layer রাখা হয়েছে, কিন্তু current implementation মূলত app module-এর ভেতরে আছে; এগুলো এখনো fully separated library module হিসেবে ব্যবহার হচ্ছে না।”

এটি clean architecture-এর intended direction, কিন্তু fully completed multi-module architecture নয়।

## ৪. App launch থেকে Home screen পর্যন্ত flow

### Entry files

- `app/src/main/AndroidManifest.xml`: launcher Activity, permissions, receiver ও alarm Activity register করে।
- `DiaryCalendarApp.kt`: `@HiltAndroidApp`; Hilt dependency injection চালু করে।
- `MainActivity.kt`: app শুরু হলে `setContent` দিয়ে Compose UI চালু করে।

Flow:

```text
Android launcher
    ↓
MainActivity.onCreate()
    ↓
enableEdgeToEdge()
    ↓
DoodleDatesTheme { MainNavigation() }
    ↓
NavHost-এর startDestination = "home"
    ↓
HomeScreen
```

`MainActivity.kt`-এর `MainNavigation()` তিনটি route define করে:

- `home`
- `settings`
- `workspace/{monthKey}`

যেমন January 2026 খুললে route হয় `workspace/2026-01`। `monthKey` হলো screen-এর input parameter।

### Home screen কী করে

`HomeScreen.kt`:

- year selector দেখায়, বর্তমানে 2026 থেকে 2030।
- 12টি month card grid-এ দেখায়।
- holiday এবং Friday/Saturday highlight করে।
- কোনো month-এ note বা stroke থাকলে ছোট indicator দেখায়।
- month card click করলে `WorkspaceScreen`-এ যায়।

`HomeViewModel.kt`:

- selected year রাখে।
- প্রতিটি মাসের জন্য `repository.hasDataForMonth(monthKey)` চালায়।
- database-এ data থাকলে month card-এ indicator দেখায়।

## ৫. Workspace screen-এর layer structure

`WorkspaceScreen.kt` একটি `Box`-এর মধ্যে কয়েকটি layer একসাথে রাখে:

```text
WorkspaceScreen
├── CalendarBackground
├── DigitalClock
├── DrawingLayer
├── NoteLayer
├── Full-screen note editor overlay
└── DatePicker / TimePicker dialog
```

Toolbar থেকে mode বদলায়:

- `PEN`: finger/stylus দিয়ে আঁকা
- `ERASER`: erase stroke তৈরি
- `TEXT`: canvas-এ tap করলে নতুন sticky note

Workspace-এর state `WorkspaceViewModel` থেকে আসে:

```kotlin
val strokes by viewModel.strokes.collectAsState()
val notes by viewModel.notes.collectAsState()
```

Room-এর data পরিবর্তন হলে Flow emit করে, ViewModel state update করে, Compose UI automatically redraw করে।

## ৬. Local database: Room কীভাবে কাজ করে

### Database objects

`DiaryDatabase.kt`:

```kotlin
@Database(
    entities = [StrokeEntity::class, NoteEntity::class],
    version = 1
)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
}
```

Database file-এর নাম `diary_database`। এর দুটি table আছে:

1. `strokes`
2. `notes`

### StrokeEntity

`data/local/entities/StrokeEntity.kt`-এ drawing stroke-এর metadata থাকে:

- `id`: auto-generated primary key
- `monthKey`: কোন মাসে stroke আঁকা হয়েছে, যেমন `2026-01`
- `type`: `pen` অথবা `erase`
- `colorHex`: stroke color
- `widthDp`: brush width
- `pointsJson`: সব drawing point JSON string হিসেবে
- `createdAt`: creation time

### NoteEntity

`NoteEntity.kt`-এ sticky note-এর data থাকে:

- position: `xFraction`, `yFraction`
- size: `widthDp`, `heightDp`
- text: `contentHtml` field-এ string
- paper/text color
- font size, bold, italic
- alarm time ও alarm label
- created/updated time

সতর্কতা: field-এর নাম `contentHtml`, কিন্তু current UI `BasicTextField` দিয়ে plain text রাখে। এটি full HTML editor নয়। Bold/italic UI style হিসেবে কাজ করে।

### DAO কী করে

`DiaryDao.kt` database query define করে:

```kotlin
@Query("SELECT * FROM strokes WHERE monthKey = :monthKey")
fun getStrokesForMonth(monthKey: String): Flow<List<StrokeEntity>>
```

এর ফলে screen open থাকা অবস্থায় month-এর strokes observe করা যায়। একইভাবে notes-ও observe হয়। Insert, update, delete, clear এবং count query আছে।

### Repository কেন আছে

`DiaryRepository.kt` UI থেকে DAO সরাসরি ব্যবহারের প্রয়োজন কমায়। ViewModel শুধু বলে:

```text
repository.insertStroke(stroke)
repository.getNotesForMonth(monthKey)
```

Repository ভিতরে DAO call করে। পরে database বদলালেও UI layer কম বদলাতে হয়।

### Hilt দিয়ে database তৈরি

`DatabaseModule.kt`:

```text
ApplicationContext
    ↓
Room.databaseBuilder(..., "diary_database")
    ↓
DiaryDatabase singleton
    ↓
DiaryDao
    ↓
DiaryRepository
    ↓
ViewModel
```

`@Singleton` থাকার কারণে app process-এর মধ্যে একই database/repository instance reuse হয়।

## ৭. Handwriting কীভাবে কাজ করে

এটি image save করে না; drawing-কে vector-like point data হিসেবে save করে। এই design-এর সুবিধা হলো screen size বদলালেও drawing re-scale করা যায়।

### Drawing শুরু

`DrawingLayer.kt`-এ Compose `Canvas` আছে। `detectDragGestures` finger/stylus movement ধরে:

```text
onDragStart → প্রথম point
onDrag      → চলমান points
onDragEnd   → সম্পূর্ণ stroke ViewModel-এ পাঠানো
```

প্রতিটি point:

```kotlin
DrawingPoint(
    xFraction = x / canvasWidth,
    yFraction = y / canvasHeight,
    pressure = change.pressure
)
```

`xFraction` এবং `yFraction` 0 থেকে 1-এর মধ্যে normalized coordinate। তাই 1000px screen বা 2000px screen-এ একই relative position পাওয়া যায়। `pressure` stylus pressure capture করে, যদিও current renderer pressure দিয়ে stroke width পরিবর্তন করছে না।

### Stroke database-এ যাওয়া

`WorkspaceViewModel.addStroke()`:

1. current mode দেখে `pen` বা `erase` ঠিক করে।
2. color এবং width নেয়।
3. `DrawingPoint` list-কে `Json.encodeToString(points)` করে।
4. `StrokeEntity` তৈরি করে।
5. Repository-এর মাধ্যমে Room-এ insert করে।
6. undo stack-এ action যোগ করে।

### Stroke আবার আঁকা

Database থেকে `pointsJson` আসার পরে `DrawingLayer`:

1. `Json.decodeFromString` দিয়ে point list বানায়।
2. fraction-কে canvas width/height দিয়ে multiply করে real pixel position বের করে।
3. `createSmoothPath()` quadratic curve দিয়ে path smooth করে।
4. Compose Canvas-এ `drawPath()` করে।

এজন্য app বন্ধ করে আবার খুললেও stroke পুনরায় render হয়।

### Eraser

Eraser আলাদা `erase` stroke হিসেবে save হয়। Render করার সময়:

```text
color = Transparent
blendMode = BlendMode.Clear
```

অর্থাৎ eraser পুরনো row মুছে না; canvas redraw-এর সময় clear blend mode দিয়ে সেই path-এর অংশ transparent করে।

### Zoom ও pan

Drawing disabled থাকলে `detectTransformGestures` ব্যবহার করে pinch zoom এবং pan করা যায়। `scale` 1x থেকে 5x পর্যন্ত সীমাবদ্ধ। Draw করার সময় touch drawing gesture active থাকে।

### Smoothness

বর্তমানে `createSmoothPath()` nearby points-এর midpoint এবং quadratic curve ব্যবহার করে। `detectAndApplySmartShape()`-এর নাম smart shape হলেও current implementation closed shape detect করে point list অপরিবর্তিত রাখে; true circle/rectangle recognition এখনো নেই।

## ৮. Sticky note কীভাবে কাজ করে

`NoteLayer.kt` সব `NoteEntity`-কে `StickyNote` composable হিসেবে render করে।

### Note তৈরি

Mode `TEXT` হলে workspace-এ tap করা হয়:

```text
tap position / screen size
    ↓
xFraction, yFraction
    ↓
NoteEntity
    ↓
Room insert
    ↓
NoteLayer redraw
```

Default note size 150dp × 100dp এবং default paper color হল হলুদ `#FFF9C4`।

### Note move ও resize

- drag করলে local `xOffset` ও `yOffset` বদলায়। drag শেষ হলে fraction হিসেবে database-এ update হয়।
- bottom-right handle drag করলে width/height বদলায়। width 120–400dp এবং height 50–400dp-এর মধ্যে রাখা হয়।
- note position fraction হওয়ায় different screen size-এ position proportionally রাখা যায়।

### Text formatting

`BasicTextField` text input নেয়। toolbar থেকে:

- bold toggle
- italic toggle
- text color
- fullscreen edit
- alarm
- delete

Text change হলে `onUpdate(note.copy(contentHtml = it))` কল হয় এবং repository update করে। Current code-এ typing প্রতি change-এ database update হতে পারে; production app-এ debounce করা ভালো।

## ৯. Undo এবং redo

`WorkspaceViewModel`-এ দুটি in-memory list আছে:

- `undoStack`
- `redoStack`

Action type:

- `AddStroke`
- `AddNote`
- `DeleteNote`
- `UpdateNote`

উদাহরণ: নতুন stroke add করার পর undo করলে সেই stroke delete করা হয়। redo করলে আবার insert করা হয়।

গুরুত্বপূর্ণ limitation: stack memory-তে থাকে, তাই app process পুরোপুরি বন্ধ হলে undo history থাকে না। Database data থাকে, কিন্তু undo history থাকে না। Note update tracking current code-এ simplified; text update সবসময় পূর্ণ undo action হিসেবে যোগ হচ্ছে না।

## ১০. Reminder, notification ও alarm flow

### Alarm schedule

Note-এর alarm set করলে:

```text
DatePicker + TimePicker
    ↓
epochMillis
    ↓
NoteEntity.alarmEpochMillis update
    ↓
AlarmManager.setExactAndAllowWhileIdle()
    ↓
PendingIntent
    ↓
NoteAlarmReceiver.onReceive()
```

`WorkspaceViewModel.scheduleAlarm()` `note.id`-কে PendingIntent request code হিসেবে ব্যবহার করে। ফলে note-specific alarm রাখা যায়। Exact alarm permission Manifest-এ আছে।

### Alarm fire হলে

`NoteAlarmReceiver.kt`:

1. note id, month key, label intent থেকে নেয়।
2. notification channel তৈরি করে।
3. chosen ringtone বা default alarm sound নেয়।
4. vibration preference পড়ে।
5. `AlarmActivity` full-screen intent তৈরি করে।
6. notification-এ Snooze ও Dismiss action যোগ করে।

### Full-screen alarm

`AlarmActivity.kt`:

- lock screen-এর ওপর দেখানোর চেষ্টা করে।
- screen on রাখে।
- MediaPlayer দিয়ে ringtone loop করে।
- Vibrator দিয়ে vibration loop করে।
- note-এর content load করে immersive alarm UI দেখায়।
- dismiss করলে sound/vibration stop করে।
- snooze করলে 10 মিনিট পরে নতুন alarm schedule করে।

### গুরুত্বপূর্ণ honest caveat

Manifest-এ `BOOT_COMPLETED` receiver আছে, কিন্তু `BootReceiver.kt`-এর current `onReceive()` শুধু comment রাখে; reboot-এর পরে saved alarms বাস্তবে re-register করে না। Presentation-এ এটিকে “planned/incomplete behavior” বলবে, fully working feature বলবে না।

## ১১. Settings এবং DataStore

`ThemeSettings.kt` Android Preferences DataStore ব্যবহার করে। Database-এর মতো complex rows নয়; ছোট key-value data রাখে:

- `background_color`
- `paper_color`
- `accent_color`
- `vibration_enabled`
- `ringtone_uri`

Color `Color` object হিসেবে নয়, hex string হিসেবে save হয়। Flow map হয়ে `AppSettingsState` তৈরি করে। `DoodleDatesTheme` সেই state collect করে Material color scheme update করে।

```text
SettingsScreen
    ↓
SettingsViewModel
    ↓
ThemeSettings
    ↓
DataStore preferences
    ↓
settingsFlow
    ↓
DoodleDatesTheme
    ↓
সব Compose screen update
```

Theme presets যেমন Default, Vintage, Forest, Cyberpunk ইত্যাদি `CustomTheme` object। Reset করলে Default theme, vibration on এবং default ringtone হয়।

## ১২. Backup ও import/export

`SettingsScreen` Android document picker ব্যবহার করে:

- Export: `CreateDocument("application/json")`
- Import: `OpenDocument()`

`SettingsViewModel`:

```text
Room-এর strokes + notes
    ↓
BackupData
    ↓
Kotlin serialization JSON
    ↓
user-selected file
```

Import-এর সময় JSON decode করে প্রতিটি stroke ও note repository-তে insert হয়।

Current limitation: export function 2026–2030 মাসগুলো scan করে। ভবিষ্যতে অন্য year-এর data থাকলে export নাও হতে পারে। এছাড়া import duplicate primary key behavior-এর ওপর নির্ভর করে; production version-এ validation, versioning এবং duplicate strategy যোগ করা দরকার।

## ১৩. Calendar ও holiday logic

`domain/model/Holiday.kt`-এ Bangladesh-এর fixed এবং lunar-related holiday list আছে।

- fixed holidays নির্দিষ্ট date-এ।
- Friday/Saturday weekend হিসেবে highlight।
- 2026-এর holiday data আলাদা base list।
- 2027–2030-এর কিছু Islamic holiday আনুমানিকভাবে প্রতি বছর 11 দিন আগে shift করা হয়েছে।

Presentation-এ বলবে:

> “Calendar highlighting local static data দিয়ে করা হয়েছে; lunar dates-এর 2027–2030 অংশ approximate, live government calendar বা Hijri calculation নয়।”

## ১৪. Widget ও Android Manifest

Manifest-এ register করা আছে:

- `MainActivity`: launcher screen
- `NoteAlarmReceiver`: alarm broadcast receiver
- `BootReceiver`: reboot broadcast receiver
- `DoodleWidgetReceiver`: home-screen widget
- `AlarmActivity`: full-screen alarm screen

Permissions:

- exact alarm
- notifications
- boot completed
- full-screen intent
- vibration

Widget code `widgets/DoodleWidget.kt`-এ আছে এবং Glance dependency app Gradle-এ আছে। Presentation-এ widget mention করতে পারো, তবে demo-তে নিশ্চিত না থাকলে “widget integration included” বলো, “fully featured widget” বলো না।

## ১৫. Build ও dependency-এর সহজ explanation

`settings.gradle.kts` project/module নাম declare করে। Root `build.gradle.kts` plugin versions-এর availability দেয়। `app/build.gradle.kts` app-এর Android configuration ও dependencies রাখে। Version catalog `gradle/libs.versions.toml` dependency alias রাখে।

প্রধান libraries:

- Kotlin + Jetpack Compose: UI
- Material 3: UI components
- Navigation Compose: screen navigation
- Room: SQLite local DB
- DataStore: preferences
- Hilt/Dagger: dependency injection
- Kotlin Serialization: points ও backup JSON
- KSP: Room/Hilt code generation
- Glance: widget

`compileSdk = 37`, `minSdk = 26`, `targetSdk = 35`, Java compatibility 11।

## ১৬. Tests এবং current limitations

বর্তমানে `app/src/test` এবং `app/src/androidTest`-এ Android template example test আছে; drawing, Room, alarm বা backup-এর meaningful automated test এখনো নেই।

Presentation-এর সময় honestভাবে বলবে:

- current local database schema version 1।
- migration code নেই, schema বদলালে migration দরকার হবে।
- BootReceiver alarm restore incomplete।
- holiday calculation partly approximate।
- `contentHtml` নাম থাকলেও full HTML editor নয়।
- undo/redo process-memory based এবং note editing undo incomplete।
- separate `data`/`domain` Gradle modules declared হলেও source app module-এর ভেতরে আছে।
- local SDK path ঠিক না থাকলে build হবে না; `local.properties`-এ valid `sdk.dir` দরকার।

এগুলো failure নয়; এগুলো current project-এর known next steps।

# ১৭. Slide-by-slide Bangla presentation script

## Slide 1: Title

**Screen-এ দেখাবে:** app icon বা Home screen screenshot।

**বলবে:**

> আসসালামু আলাইকুম। আজ আমি DoodleDates project উপস্থাপন করব। এটি একটি offline-first diary calendar application। সাধারণ calendar-এর পাশাপাশি এখানে handwritten drawing, sticky note এবং note-based reminder যোগ করা যায়। আমাদের লক্ষ্য হলো calendar-কে শুধু date দেখানোর জায়গা না রেখে personal memory canvas বানানো।

## Slide 2: Problem and solution

**Screen-এ দেখাবে:** Home screen এবং Workspace screen।

**বলবে:**

> সাধারণ calendar-এ date দেখা যায়, কিন্তু সেখানে নিজের handwriting, ছোট reminder বা visual memory রাখা কঠিন। DoodleDates একটি মাসকে interactive canvas হিসেবে ব্যবহার করে। User সরাসরি calendar-এর ওপর লিখতে, আঁকতে এবং note রাখতে পারে। সব data locally রাখা হয়, তাই internet account ছাড়াও app ব্যবহার করা যায়।

## Slide 3: Technology stack

**Screen-এ দেখাবে:** `app/build.gradle.kts` এবং dependency list।

**বলবে:**

> এই project Kotlin language এবং Jetpack Compose UI toolkit দিয়ে তৈরি। Navigation-এর জন্য Navigation Compose, local database-এর জন্য Room, settings-এর জন্য DataStore, dependency injection-এর জন্য Hilt এবং JSON serialization-এর জন্য Kotlin Serialization ব্যবহার হয়েছে। Drawing-এর জন্য Compose Canvas ব্যবহার করেছি।

## Slide 4: Project structure

**Screen-এ দেখাবে:** `app/src/main/java/com/spycodedoodledates` tree।

**বলবে:**

> Project-এর প্রধান implementation app module-এর মধ্যে। `ui` package presentation layer, `data` package database এবং repository layer, `domain` package model layer, `notifications` package alarm logic এবং `di` package Hilt providers রাখে। `MainActivity` app entry point।

> একটি technical note হলো data এবং domain আলাদা Gradle module হিসেবে declare করা থাকলেও source বর্তমানে app module-এর package-এর মধ্যে আছে। তাই এটিকে package-level separation বলা বেশি accurate।

## Slide 5: App launch flow

**Screen-এ দেখাবে:** `AndroidManifest.xml`, `MainActivity.kt`।

**বলবে:**

> Android launcher থেকে প্রথমে MainActivity চালু হয়। `onCreate()`-এ Compose content set করা হয়। তারপর `DoodleDatesTheme` app-এর theme apply করে এবং `MainNavigation()` navigation graph তৈরি করে। Home হলো start destination। এখান থেকে settings অথবা selected month-এর workspace-এ যাওয়া যায়।

## Slide 6: Home screen

**Screen-এ দেখাবে:** `HomeScreen.kt`, `HomeViewModel.kt`।

**বলবে:**

> Home screen 12টি month card দেখায়। প্রতিটি card-এর ভিতরে mini calendar আছে। holiday এবং Friday/Saturday আলাদা color-এ দেখানো হয়। HomeViewModel repository-কে জিজ্ঞেস করে কোন month-এ data আছে। data থাকলে card-এ একটি ছোট indicator দেখা যায়।

## Slide 7: Workspace layers

**Screen-এ দেখাবে:** `WorkspaceScreen.kt`।

**বলবে:**

> Workspace একটি layered Compose layout। নিচে CalendarBackground, তার ওপর clock, তারপর DrawingLayer এবং NoteLayer। Toolbar bottom bar হিসেবে থাকে। Current mode অনুযায়ী drawing বা text interaction সক্রিয় হয়। Compose state collect করে বলে database update হলে screen automatically update হয়।

## Slide 8: Handwriting capture

**Screen-এ দেখাবে:** `DrawingLayer.kt` এবং `DrawingPoint`।

**বলবে:**

> User finger বা stylus drag করলে Canvas pointer gesture listener movement record করে। Start point, মাঝের points এবং end point মিলে একটি stroke তৈরি হয়। প্রতিটি point-এর x এবং y pixel হিসেবে না রেখে canvas-এর fraction হিসেবে save করি। যেমন x fraction 0.5 মানে canvas-এর মাঝখান। এতে screen size বদলালেও drawing-এর relative position ঠিক থাকে। Pressure-ও capture করা হয়।

## Slide 9: Handwriting persistence and redraw

**Screen-এ দেখাবে:** `WorkspaceViewModel.addStroke()`, `StrokeEntity.kt`।

**বলবে:**

> Drawing শেষ হলে WorkspaceViewModel current color, width এবং mode নিয়ে StrokeEntity তৈরি করে। Points list JSON string হয়ে Room database-এ save হয়। পরে screen load হলে JSON decode করে Canvas path বানানো হয়। Nearby points-এর midpoint এবং quadratic curve ব্যবহার করে line smooth করা হয়। তাই আমরা bitmap save করছি না; data save করে redraw করছি।

## Slide 10: Eraser, zoom and undo

**Screen-এ দেখাবে:** `DrawingLayer.kt`, `WorkspaceViewModel.kt`।

**বলবে:**

> Eraser পুরনো row database থেকে মুছে না; erase type-এর একটি stroke save হয়। Render করার সময় `BlendMode.Clear` ব্যবহার করে ওই path clear করা হয়। Drawing disabled থাকলে pinch zoom এবং pan করা যায়। Undo এবং redo in-memory action stack ব্যবহার করে stroke বা note insert/delete reverse করে। তবে app process পুরোপুরি বন্ধ হলে undo history থাকে না।

## Slide 11: Sticky notes

**Screen-এ দেখাবে:** `NoteLayer.kt`, `NoteEntity.kt`।

**বলবে:**

> Text mode-এ canvas-এ tap করলে tap position fraction-এ convert হয়ে নতুন NoteEntity তৈরি হয়। Note drag করলে position update হয়, resize handle দিয়ে size update হয়। BasicTextField text নেয়। Bold, italic, text color, paper color এবং fullscreen edit আছে। Note-এর text, position, size এবং alarm information Room-এ save হয়।

## Slide 12: Room database flow

**Screen-এ দেখাবে:** `DiaryDatabase.kt`, `DiaryDao.kt`, `DiaryRepository.kt`, `DatabaseModule.kt`।

**বলবে:**

> Room database-এর দুটি table হলো strokes এবং notes। DAO SQL query define করে। Repository DAO-কে wrap করে। Hilt DatabaseModule database এবং DAO singleton হিসেবে provide করে। ViewModel repository ব্যবহার করে, তাই UI database implementation জানে না। Query result Flow হওয়ায় insert বা update হলে UI reactive ভাবে refresh হয়।

## Slide 13: Reminder system

**Screen-এ দেখাবে:** `WorkspaceViewModel.scheduleAlarm()`, `NoteAlarmReceiver.kt`, `AlarmActivity.kt`।

**বলবে:**

> Note-এর alarm set করলে DatePicker এবং TimePicker থেকে epoch time তৈরি হয়। AlarmManager সেই time-এ PendingIntent broadcast করে। NoteAlarmReceiver notification দেখায় এবং AlarmActivity খুলতে পারে। AlarmActivity ringtone loop করে, vibration চালায় এবং dismiss বা snooze action দেয়। Snooze করলে 10 মিনিট পরে নতুন alarm schedule হয়।

## Slide 14: Settings and theme

**Screen-এ দেখাবে:** `SettingsScreen.kt`, `ThemeSettings.kt`, `Theme.kt`।

**বলবে:**

> Settings screen থেকে preset theme, background, paper ও accent color বেছে নেওয়া যায়। Vibration এবং ringtone-ও configure করা যায়। এগুলো Room-এ নয়, Preferences DataStore-এ key-value হিসেবে save হয়। ThemeSettings Flow expose করে এবং DoodleDatesTheme সেই Flow collect করে MaterialTheme update করে।

## Slide 15: Backup and import

**Screen-এ দেখাবে:** `SettingsViewModel.kt`-এর `BackupData`, `exportBackup`, `importBackup`।

**বলবে:**

> User Android document picker দিয়ে JSON file export করতে পারে। App strokes এবং notes সংগ্রহ করে BackupData object বানায় এবং Kotlin Serialization দিয়ে JSON লেখে। Import-এর সময় file read, JSON decode এবং repository insert হয়। এটি local portability দেয়। Current implementation 2026 থেকে 2030-এর month scan করে, তাই future version-এ dynamic year support যোগ করা যেতে পারে।

## Slide 16: Security and offline behavior

**Screen-এ দেখাবে:** Manifest permissions এবং database code।

**বলবে:**

> App-এর diary data কোনো remote server-এ পাঠানো হয় না। Notes ও drawings local Room database-এ থাকে। External access মূলত alarm, notification, vibration এবং user-selected backup file-এর জন্য। Backup user নিজে export করলে তবেই file system-এ যায়।

## Slide 17: Limitations and future work

**Screen-এ দেখাবে:** `BootReceiver.kt`, test folders।

**বলবে:**

> Current version-এর কিছু known limitation আছে। Reboot-এর পরে BootReceiver এখনো saved alarm re-register করে না। 2027 থেকে 2030-এর lunar holiday approximate। Full HTML editor নেই। Meaningful unit/UI tests এখনও যোগ করা হয়নি। Data ও domain source fully separate Gradle module করা, Room migration যোগ করা এবং backup validation উন্নত করা future work।

## Slide 18: Demo sequence

এই order-এ live demo করবে:

1. Home screen-এ year change দেখাও।
2. একটি month open করো।
3. Pen দিয়ে একটি signature বা ছোট drawing আঁকো।
4. Eraser select করে অংশ মুছো।
5. Text mode select করে tap করে note তৈরি করো।
6. Note লিখে drag এবং resize করো।
7. Note-এর alarm set করার dialog দেখাও।
8. Settings-এ theme ও vibration বদলাও।
9. Export Backup চাপো এবং JSON file save location দেখাও।
10. App বন্ধ করে reopen করে drawing/note persistence দেখাও।

## ১৯. শেষ করার script

> সবশেষে, DoodleDates-এর মূল strength হলো handwriting, structured calendar এবং reminder একসাথে পাওয়া। এর architecture reactive Compose UI, ViewModel, Repository এবং Room-এর ওপর তৈরি। Data locally থাকায় app offline-এ কাজ করে এবং JSON backup-এর মাধ্যমে user নিজের data বহন করতে পারে। Current version একটি working foundation; alarm restore, testing, migration এবং complete modularization পরের উন্নতির জায়গা। ধন্যবাদ।

# ২০. সম্ভাব্য প্রশ্নের সহজ উত্তর

**প্রশ্ন: Drawing image হিসেবে save না করে points হিসেবে কেন save করেছেন?**

উত্তর: Points save করলে resolution-independent redraw করা যায়, zoom করা যায় এবং future-এ editing/analytics করা সহজ হয়। Bitmap save করলে screen size বদলালে quality ও scaling সমস্যা হতে পারে।

**প্রশ্ন: Room এবং DataStore-এর পার্থক্য কী?**

উত্তর: Room structured relational data যেমন notes এবং strokes-এর জন্য। DataStore ছোট preference যেমন theme color, ringtone এবং vibration-এর জন্য।

**প্রশ্ন: Flow কেন ব্যবহার করা হয়েছে?**

উত্তর: Database change হলে Flow নতুন list emit করে। Compose সেই state observe করে এবং manually refresh না করেই UI update করে।

**প্রশ্ন: Repository কেন দরকার?**

উত্তর: ViewModel যেন SQL/DAO details না জানে। Repository data source-এর abstraction দেয় এবং future-এ database বা remote source বদলালে UI কম বদলাতে হয়।

**প্রশ্ন: Hilt কী করে?**

উত্তর: Hilt `DiaryDatabase`, `DiaryDao`, `DiaryRepository`, `ThemeSettings` ইত্যাদি object তৈরি করে সঠিক class-এ inject করে। ফলে manually constructor wiring কমে যায়।

**প্রশ্ন: App কি online?**

উত্তর: Current implementation offline-first। কোনো API বা server layer নেই। User চাইলে JSON backup file export/import করে।

**প্রশ্ন: Reboot-এর পরে alarm থাকবে?**

উত্তর: Manifest-এ BootReceiver declared আছে, কিন্তু current `BootReceiver` implementation এখনো alarm re-register করে না। এটি known next step।

**প্রশ্ন: `contentHtml` কি HTML editor?**

উত্তর: নামটি HTML-oriented, কিন্তু current UI BasicTextField ব্যবহার করে plain string save করছে। Bold/italic আলাদা boolean state। Full HTML/rich-text parser এখনো নেই।

**প্রশ্ন: Database file কোথায়?**

উত্তর: Android app-এর private internal storage-এ `diary_database` নামে থাকে। Source code-এ `Room.databaseBuilder(..., "diary_database")` দিয়ে নামটি define করা হয়েছে। সাধারণ file browser থেকে সরাসরি দেখা যায় না।

# ২১. Presentation-এর আগে মুখস্থ রাখার ১০টি keyword

`Compose`, `Activity`, `Navigation`, `ViewModel`, `Repository`, `Room`, `DAO`, `DataStore`, `Canvas`, `AlarmManager`।

এই দশটি শব্দের role বলতে পারলে project-এর প্রায় পুরো architecture explain করা যাবে।
