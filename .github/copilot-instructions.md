## Coffee Hub — AI assistant quick start

Purpose: give an AI coding assistant the minimal, concrete knowledge to be productive in this Kotlin Android repo.

Overview
- Kotlin Android app using MVVM + Repository pattern. Clean-ish separation: `data`, `domain`, `ui`, `viewmodel`, `di`.
- DI: Hilt. Networking: Retrofit + OkHttp + Gson. Persistence: Room + DataStore + Firebase.

Key paths (examples to open immediately)
- App bootstrap: `app/src/main/java/com/example/financemanagement/App.kt`
- DI modules: `app/src/main/java/com/example/financemanagement/di/` (NetworkModule.kt, DatabaseModule.kt, RepositoryModule.kt)
- API interface: `app/src/main/java/com/example/financemanagement/data/remote/api/ApiService.kt`
- Local DB: `app/src/main/java/com/example/financemanagement/data/local/db/AppDatabase.kt`
- Constants & base URL: `app/src/main/java/com/example/financemanagement/utils/Constants.kt` and `secrets.properties`
- Navigation: `app/src/main/res/navigation/nav_graph.xml`
- UI entry: `app/src/main/java/com/example/financemanagement/ui/main/MainActivity.kt`

Concrete architecture notes (why it matters)
- Fragments emit UI events → ViewModels (in `viewmodel/`) own UI state as StateFlow/Flow.
- Repositories abstract data sources and are injected by Hilt. They mediate between `ApiService` and Room DAOs.
- Token handling: JWT saved in DataStore via `data/local/TokenManager.kt`. OkHttp interceptor (in NetworkModule) adds Authorization header.

Developer workflows (Windows / PowerShell)
- Open project: Android Studio → Open folder `D:/Coding/Mobile Projects/coffee_hub` → let Gradle sync.
- Quick CLI (from project root):
  - Clean: `.
    \gradlew.bat clean`
  - Build debug: `.
    \gradlew.bat assembleDebug`
  - Install on device/emulator: `.
    \gradlew.bat installDebug`
  - Unit tests: `.
    \gradlew.bat test`
  - Instrumented tests: `.
    \gradlew.bat connectedAndroidTest`

Important build/codegen details
- KAPT/Hilt and Room generate sources under `app/build/generated/` and `app/build/tmp`. If you change packages/namespaces, run `clean` to remove stale generated classes.
- Namespace & applicationId live in `app/build.gradle.kts` (fields `namespace` and `applicationId`) — changing them requires moving package folders and updating Kotlin package declarations.

Project-specific conventions & gotchas (actionable)
- UI: uses ViewBinding (not Compose). Prefer using `binding` from generated binding classes in fragments.
- Navigation graph references fully-qualified fragment class names (see `nav_graph.xml`) — update those if packages change.
- DB name literal: `"finance_management_database"` appears in `di/DatabaseModule.kt` — rename intentionally if migrating DB schema or app name.
- README contains examples and endpoints; `secrets.properties` contains BASE_URL for remote API. Confirm before changing.

Integration points & external deps
- Backend API: demo base URL in `secrets.properties` and `Constants.kt` — endpoints documented in `README.md`.
- Hilt + Room + Retrofit interplay: Hilt modules in `di/` provide singletons for Retrofit, OkHttp, Room DB and DAOs.

When to ask for clarification
- If a change touches package names / `applicationId`/`namespace` — ask before making it. This is invasive: requires moving many files, cleaning generated code, and updating tests.
- If modifying DB schema or table/entity names, confirm migration approach.

Where to look for more examples
- Auth flow: `app/src/main/java/com/example/financemanagement/ui/auth/*` + `viewmodel/AuthViewModel.kt` + `data/repository/AuthRepository*`.
- Dashboard & transactions: `ui/dashboard/`, `data/repository/TransactionRepository*` and `data/remote/models`.

If you update this file
- Keep it short. Preserve paths above. Provide concrete file references and exact Gradle commands (Windows wrapper shown above).

---
If anything in this file is unclear or you want the AI to perform a risky, mass refactor (package rename, applicationId rename, DB rename), ask the developer for an explicit confirmation and desired package/applicationId.
