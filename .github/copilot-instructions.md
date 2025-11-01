## Coffee Hub — AI assistant quick start

**Purpose:** Give an AI coding assistant the minimal, concrete knowledge to be productive in this Kotlin Android coffee shop app.

## Overview
- **Project Type**: Android app (Kotlin) for coffee shop sales with customer and admin features
- **Architecture**: MVVM + Repository pattern with clean separation: `domain`, `data`, `ui`, `viewmodel`, `di`
- **Backend**: **Firebase-only** (Auth, Firestore, Storage) — no external API server
- **DI**: Hilt/Dagger
- **Package**: `com.coffeehub` (namespace and applicationId)

## Key paths (open these first)
- **App entry**: `app/src/main/java/com/coffeehub/CoffeeHubApp.kt`
- **MainActivity**: `app/src/main/java/com/coffeehub/ui/MainActivity.kt`
- **DI modules**: `app/src/main/java/com/coffeehub/di/` (FirebaseModule.kt, AppModule.kt)
- **Domain models**: `app/src/main/java/com/coffeehub/domain/model/` (User.kt, Product.kt, Order.kt, OrderItem.kt)
- **Repositories**: `app/src/main/java/com/coffeehub/data/repository/` (AuthRepository.kt, ProductRepository.kt, OrderRepository.kt)
- **ViewModels**: `app/src/main/java/com/coffeehub/viewmodel/` (AuthViewModel.kt, ProductViewModel.kt, OrderViewModel.kt)
- **Navigation**: `app/src/main/res/navigation/nav_graph.xml`
- **UI**:
  - Auth: `app/src/main/java/com/coffeehub/ui/auth/` (LoginFragment, RegisterFragment)
  - Customer: `app/src/main/java/com/coffeehub/ui/customer/` (ProductListFragment, etc.)
  - Admin: `app/src/main/java/com/coffeehub/ui/admin/` (AdminDashboardFragment, etc.)

## Architecture details (why it matters)
- **Flow**: Fragment → ViewModel (StateFlow/Flow) → Repository → Firebase (Auth, Firestore, Storage)
- **Auth**: Firebase Authentication with email/password. User data stored in Firestore `users` collection with `isAdmin` field for role-based navigation.
- **Products**: Stored in Firestore `products` collection. Admin can CRUD products; customers can browse and search.
- **Orders**: Stored in Firestore `orders` collection. Customers create orders; admin updates order status (PENDING → PREPARING → READY → COMPLETED).
- **ViewBinding**: All fragments use ViewBinding (not Compose). Access views via `binding.viewId`.
- **No Retrofit/API layer**: Repositories directly interact with Firebase SDK (FirebaseAuth, FirebaseFirestore, FirebaseStorage).

## Developer workflows (Windows / PowerShell)
Open in Android Studio → File → Open → `D:/Coding/Mobile Projects/coffee_hub` → Gradle sync.

**CLI commands** (from project root):
```powershell
.\gradlew.bat clean                    # Clean build artifacts
.\gradlew.bat assembleDebug            # Build debug APK
.\gradlew.bat installDebug             # Install on device/emulator
.\gradlew.bat test                     # Run unit tests
.\gradlew.bat connectedAndroidTest     # Run instrumented tests
```

## Important build/codegen details
- **KAPT/Hilt**: Generates code under `app/build/generated/` and `app/build/tmp`. Run `.\gradlew.bat clean` after package/namespace changes to remove stale classes.
- **Namespace & applicationId**: Both set to `com.coffeehub` in `app/build.gradle.kts`. Changing requires moving source folders and updating all package declarations.
- **Firebase config**: `app/google-services.json` must be present (download from Firebase Console for package `com.coffeehub`).

## Firebase setup (required before first run)
1. Create Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add Android app with package name: `com.coffeehub`
3. Download `google-services.json` → place in `app/` directory
4. Enable **Authentication** (Email/Password)
5. Create **Firestore** database (test mode initially)
6. Enable **Firebase Storage**

## Firestore collections structure
- **users**: `{ id, email, name, isAdmin: boolean, createdAt }`
- **products**: `{ id, name, description, price, imageUrl, category, stock, isAvailable, createdAt }`
- **orders**: `{ id, customerId, customerName, items: [OrderItem], total, status, timestamp, notes }`

## Project-specific conventions & gotchas
- **ViewBinding**: Every fragment inflates binding in `onCreateView`, accesses views via `binding.viewId`, nulls `_binding` in `onDestroyView`.
- **Navigation**: `nav_graph.xml` references fully-qualified fragment names (`com.coffeehub.ui.auth.LoginFragment`). Update if packages change.
- **Role-based navigation**: After login, check `user.isAdmin` → navigate to admin dashboard or customer product list.
- **No local DB currently**: Room dependencies present but unused. All data fetched from Firebase in real-time.

## Integration points
- **Firebase Auth**: Injected via `FirebaseModule` → used in `AuthRepository` for login/register/logout.
- **Firestore**: Injected via `FirebaseModule` → repositories use snapshot listeners for real-time data (`Flow<List<T>>`).
- **Storage**: Injected for future image uploads (admin product images).
- **Glide**: For loading product images from Firebase Storage URLs.

## When to ask for clarification
- **Package rename**: Requires moving all source files, updating `app/build.gradle.kts`, AndroidManifest, navigation graph, and clean build.
- **Firestore schema changes**: Confirm migration strategy (existing data handling).
- **New features**: Ask about UI placement (customer vs admin) and Firestore collection structure.

## Where to look for examples
- **Auth flow**: `ui/auth/LoginFragment.kt` + `viewmodel/AuthViewModel.kt` + `data/repository/AuthRepository.kt`
- **Firebase reads**: `ProductRepository.getProducts()` uses `callbackFlow` with Firestore snapshot listener
- **Firebase writes**: `OrderRepository.createOrder()` uses `await()` with Firestore `add()`

## UI & Design Guidelines

### 🎨 Design Language
- **Theme:** Coffee-inspired Material Design 3 (brown tones)
  - **Primary:** #6F4E37 (coffee brown)
  - **Accent:** #DCC7AA (cream beige)
  - **Background:** #FFF9F0 (light cream)
  - **Text:** #2C1810 (dark coffee)
- **Typography:**  
  - Titles → `sans-serif-medium`  
  - Body → `sans-serif-regular`
- **Icons:** Material Icons, outlined, minimalistic
- **Spacing:**  
  - Card padding: 8–12dp  
  - List margin: 16dp horizontal
- **Buttons:**  
  - Primary: filled (brown background, white text)  
  - Secondary: outlined  
  - Link actions: text buttons
- **CardView style:** Rounded corners (12dp), subtle elevation (2dp)
- **Image loading:** Use Glide for Firebase Storage URLs with placeholder and error images

---

### 🧭 UI Navigation & Layout Flow

#### 👤 Customer Flow
Splash → Login / Register → Home (menu) → Product Detail → Cart → Checkout → Order History

#### 🧑‍💼 Admin Flow
Splash → Login → Dashboard → Manage Products → Manage Orders → Revenue Report


- After login, check `user.isAdmin`:
  - If `true` → navigate to AdminDashboardFragment
  - If `false` → navigate to CustomerHomeFragment

#### Common UI Components
- BottomNavigationView for customer with 4 tabs: **Home**, **Cart**, **Orders**, **Profile**
- Toolbar/AppBar with title and back button
- RecyclerView for lists (products, orders)
- SearchBar / TextField for searching products
- Dialog or BottomSheet for actions like “Add Product” or “Update Status”
- Use ViewBinding (no `findViewById`, no Jetpack Compose)

---

### 🧩 UI Component Mapping

| Role | Fragment / Screen | Description |
|------|--------------------|--------------|
| Common | `SplashFragment` | Checks login state and navigates by role |
| Common | `LoginFragment` / `RegisterFragment` | Firebase Auth screens |
| Customer | `HomeFragment` | Displays coffee menu with category filter |
| Customer | `ProductDetailFragment` | Shows selected coffee details, size, quantity, Add to Cart |
| Customer | `CartFragment` | Displays cart items, total, and checkout button |
| Customer | `OrderHistoryFragment` | Lists user’s previous orders with statuses |
| Admin | `AdminDashboardFragment` | Overview: total sales, order count |
| Admin | `ManageProductsFragment` | CRUD for coffee products |
| Admin | `ManageOrdersFragment` | Lists all orders and allows updating status |

---

### ⚙️ Copilot Behavior Rules
When generating or editing UI code:
- Follow fragment-based navigation (inside `MainActivity`).
- **Do not create new Activities** unless explicitly mentioned.
- Respect existing package structure and naming (`com.coffeehub.ui.*`).
- Generate XML layouts using Material Design 3 components.
- Use color palette and typography defined above for consistency.
- For data display, prefer `RecyclerView` + `ListAdapter` + `DiffUtil`.
- Use placeholder/dummy data **only** when Firebase data context is not available.
- Keep layouts responsive and visually consistent with the coffee theme UI reference.

---

### ✅ Design Intent Summary
Copilot should understand that this app’s interface should resemble a **modern coffee shop ordering UI** — elegant, brown-themed, and friendly.  
UI examples to emulate:
- Product cards with images, name, price, and “+” button  
- Bottom navigation for customers  
- Admin dashboard with simple statistics cards  
- Checkout screen with subtotal, delivery fee, total, and “Proceed to Checkout” button  

## If you update this file
Keep it concise. Preserve file paths. Use exact PowerShell commands (Windows wrapper: `.\gradlew.bat`).

---
**Note**: This project was refactored from a Finance Management app. Old code references to `com.example.financemanagement`, Retrofit, and .NET API have been removed. Focus is now **Firebase-first** for Coffee Hub sales app.
