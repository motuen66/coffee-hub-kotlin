# Coffee Hub ☕

Modern Android coffee shop app with Firebase backend for customer ordering and admin management.

## 🚀 Quick Start

```powershell
# Clone repository
git clone https://github.com/motuen66/coffe-hub-kotlin.git
cd coffee_hub

# Setup Firebase (REQUIRED)
1. Create project: https://console.firebase.google.com
2. Add Android app (package: com.coffeehub)
3. Download google-services.json → app/google-services.json
4. Enable services:
   - Authentication → Email/Password
   - Firestore Database → Start in test mode
   - Storage → Start in test mode

# Build & Run
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

## 📱 Features

| Customer | Admin |
|----------|-------|
| Browse menu | Manage products (CRUD) |
| Add to cart | Update order status |
| Place orders | View dashboard stats |
| Track order history | Revenue reports |

## 🛠️ Tech Stack

- **Kotlin** • MVVM • Clean Architecture
- **Firebase**: Auth, Firestore, Storage, Analytics
- **Hilt** (DI) • **Navigation Component** • **ViewBinding**
- **Coroutines + Flow** • **Material Design** • **Glide**

## 📦 Project Structure

```
app/src/main/java/com/coffeehub/
├── domain/model/          # User, Product, Order, OrderItem
├── data/repository/       # AuthRepository, ProductRepository, OrderRepository
├── viewmodel/             # AuthViewModel, ProductViewModel, OrderViewModel
├── ui/
│   ├── auth/             # LoginFragment, RegisterFragment
│   ├── customer/         # ProductListFragment, CartFragment, OrderHistoryFragment
│   └── admin/            # AdminDashboardFragment, ManageProductsFragment, ManageOrdersFragment
└── di/                   # FirebaseModule, AppModule
```

## 🔥 Firestore Schema

```javascript
// Collections
users {
  userId: {
    id: string,
    email: string,
    name: string,
    isAdmin: boolean,
    createdAt: timestamp
  }
}

products {
  productId: {
    id: string,
    name: string,
    description: string,
    price: number,
    imageUrl: string,
    category: string,
    stock: number,
    isAvailable: boolean,
    createdAt: timestamp
  }
}

orders {
  orderId: {
    id: string,
    customerId: string,
    customerName: string,
    items: [OrderItem],
    total: number,
    status: "PENDING" | "PREPARING" | "READY" | "COMPLETED" | "CANCELLED",
    timestamp: timestamp,
    notes: string
  }
}
```

## 🔐 Security (gitignore)

**DO NOT COMMIT:**
- `app/google-services.json` (contains API keys)
- `local.properties` (SDK paths)
- `secrets.properties` (custom secrets)
- `*.keystore` (signing keys)

**Template provided:** `app/google-services.json.template`

## 🏗️ Build Commands (Windows PowerShell)

```powershell
.\gradlew.bat clean                    # Clean build artifacts
.\gradlew.bat assembleDebug            # Build debug APK
.\gradlew.bat installDebug             # Install on device/emulator
.\gradlew.bat test                     # Run unit tests
```

## � Import Sample Data (One-Time Setup)

Sample product data is included in `app/src/main/assets/database.json` (21 products: coffee, tea, drinks).

**To import automatically on first admin login:**

1. Open `AdminDashboardFragment.kt`
2. In `checkAndImportData()` function, **uncomment this line**:
   ```kotlin
   // importData()  // <-- Remove the "//" to enable
   ```
3. Rebuild and launch app
4. Login as admin → data will auto-import on dashboard load

**Manual import via code:**
```kotlin
// In AdminDashboardFragment or any admin screen
lifecycleScope.launch {
    val (success, failure) = databaseImporter.importProducts()
    Log.d("Import", "Success: $success, Failed: $failure")
}
```

**What gets imported:**
- 21 products from `database.json`
- Categories: Popular, Special, Cappuccino, Latte, Americano
- Prices converted USD → VND (1 USD = 24,000 VND)
- Firebase Storage URLs for product images (already uploaded)

## �🐛 Troubleshooting

**Build fails with "google-services.json not found":**
- Download from Firebase Console → Project Settings → Your apps → Download `google-services.json`
- Place in `app/` directory (same level as `build.gradle.kts`)

**Login shows "isAdmin: false" for admin user:**
- Go to Firestore Console → `users` collection → your user document
- Verify field name is exactly `isAdmin` (not `admin`)
- Verify type is **boolean** `true` (not string "true")

**App crashes on launch:**
```powershell
# Clear app data and rebuild
adb shell pm clear com.coffeehub
.\gradlew.bat clean installDebug
```

## 📄 License

MIT License - See LICENSE file for details

## 👤 Author

**Motuen66**  
GitHub: [@motuen66](https://github.com/motuen66)  
Repository: [coffe-hub-kotlin](https://github.com/motuen66/coffe-hub-kotlin)

---

**Note:** This project was refactored from a Finance Management app. All references to the old codebase have been removed. Focus is now Firebase-first for Coffee Hub sales application.
