# Calorie Tracker

A personal, offline-first calorie/food/water/exercise tracker for Android. No
backend, no account, no cloud sync — everything lives in a local Room database
and the app's private storage on the device.

## Features (MVP)

- **Food logging** via photo capture, free-text description (with a rule-based
  Thai/English dish estimator that asks a skippable portion follow-up
  question), or fully manual entry.
- **Daily calorie goal** with consumed / remaining / over display.
- **Favorite foods & exercises** for one-tap re-logging.
- **Water tracking** with quick-add buttons, custom amounts, and history.
- **Water & meal reminders** via local notifications (`AlarmManager`), with
  configurable times and quiet hours. Survive reboot via a boot receiver.
- **Exercise tracking** (activity, duration, estimated calories burned).
- **Simple rule-based recommendations** (suggest a favorite food that fits the
  remaining budget, or a favorite exercise to work off an overage).
- **Daily / weekly / monthly dashboards**, the latter two with lightweight
  in-app bar charts (no charting library dependency).

## Architecture

- **UI**: Jetpack Compose + Material 3, single-activity, `navigation-compose`
  for routing, one `ViewModel` per screen.
- **Data**: Room database (`AppDatabase`) with one entity/DAO per data type
  (`FoodLog`, `WaterLog`, `ExerciseLog`, `FavoriteFood`, `FavoriteExercise`,
  `DailyGoal`, `Reminder`, `ReminderSettings`) and a thin repository per
  domain area.
- **DI**: a small hand-rolled `AppContainer` (no DI framework) built once in
  `CalorieTrackerApp` and exposed to Compose via a `CompositionLocal`.
- **Notifications**: `ReminderScheduler` (AlarmManager) + `ReminderReceiver` +
  `BootCompletedReceiver`, all local — no push service involved.
- **NLP**: `FoodDescriptionParser` is a small keyword dictionary, not a model
  or network call — fully offline and deterministic.

Room's schema is versioned (`AppDatabase`, `exportSchema = true`, schemas
written to `app/schemas/`). Any future entity change must ship a
`Migration` in `AppDatabase.MIGRATIONS` so app updates never lose existing
logs, per the "update without losing data" MVP requirement.

## Building

Requires Android Studio (Koala+) or a Gradle setup with:

- JDK 17
- Android SDK with `compileSdk 34` / `minSdk 26` platform installed
- Network access to `google()` (Google's Maven) for AndroidX/Compose/Room —
  this is required by the Android toolchain itself, not something this
  project adds

```
./gradlew assembleDebug
```

> **Note on this repository's dev environment:** the sandbox this project was
> authored in only allows outbound access to Maven Central, not Google's Maven
> repository (`dl.google.com`), which is where AGP, Jetpack Compose, Room, and
> Navigation are published. That means the Gradle build could not be executed
> end-to-end in that sandbox. The Gradle wrapper is committed and the code was
> written and reviewed carefully by hand, but please run a first build in
> Android Studio (or any CI with normal internet access) before relying on it.

## Project layout

```
app/src/main/java/com/suthinee/calorietracker/
  data/local/          Room entities, DAOs, database, type converters
  data/repository/      Repositories wrapping DAOs with Flow
  domain/model/         MealType, ReminderType
  domain/nlp/            Food description parser
  domain/recommendation/ Rule-based daily recommendations
  notification/          AlarmManager scheduling + receivers
  ui/                    One package per screen + shared theme/nav/widgets
```
