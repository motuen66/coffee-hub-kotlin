# 📦 Hướng dẫn Import Sample Data

## Cách 1: Auto-import khi vào Admin Dashboard (Khuyến nghị ⭐)

1. Mở file `app/src/main/java/com/coffeehub/ui/admin/AdminDashboardFragment.kt`

2. Tìm function `checkAndImportData()`, dòng 48:
   ```kotlin
   // UNCOMMENT THIS LINE TO AUTO-IMPORT DATA:
   // importData()
   ```

3. **Xóa `//`** để thành:
   ```kotlin
   // UNCOMMENT THIS LINE TO AUTO-IMPORT DATA:
   importData()
   ```

4. Rebuild app:
   ```powershell
   .\gradlew.bat assembleDebug
   .\gradlew.bat installDebug
   ```

5. Launch app → Login admin → Tự động import 21 products vào Firestore

6. **SAU KHI IMPORT XONG**, nhớ **comment lại dòng đó** để tránh import duplicate:
   ```kotlin
   // importData()  // Already imported
   ```

---

## Cách 2: Import thủ công qua code

Thêm đoạn code này vào bất kỳ đâu trong admin screen (ví dụ: button click):

```kotlin
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@Inject
lateinit var databaseImporter: DatabaseImporter

// In some function (e.g., button click):
lifecycleScope.launch {
    val (success, failure) = databaseImporter.importProducts()
    Log.d("Import", "Imported: $success products, Failed: $failure")
    Toast.makeText(context, "Imported $success products", Toast.LENGTH_LONG).show()
}
```

---

## Cách 3: Kiểm tra trước khi import

```kotlin
lifecycleScope.launch {
    // Check if data already exists
    val hasData = databaseImporter.hasExistingProducts()
    
    if (!hasData) {
        // Import only if database is empty
        val (success, failure) = databaseImporter.importProducts()
        Log.d("Import", "Success: $success, Failed: $failure")
    } else {
        Log.d("Import", "Database already has products. Skipping import.")
    }
}
```

---

## 📊 Dữ liệu sẽ được import

**File source:** `app/src/main/assets/database.json`

**Total products:** 21

**Categories:**
- Popular (3): Cappoccino, Espersso, Macchiato
- Special (3): Macchiato, Espersso, Cappoccino
- Cappuccino (5): Pumpkin Latte, Macchiato, Matcha Latte, Cortado, Affogato
- Latte (5): Naranja, Estra Astar, Mojito, Lemonade, Green Ginger
- Americano (5): Simple Tea, Green Tea, Victoria Sunset Tea, Queen Berry Tea, Apple Paradise Tea

**Giá:** USD → VND (1 USD = 24,000 VND)

**Hình ảnh:** Firebase Storage URLs (đã upload sẵn)

---

## 🧹 Xóa tất cả products (Cẩn thận!)

```kotlin
lifecycleScope.launch {
    val deletedCount = databaseImporter.clearAllProducts()
    Log.d("Import", "Deleted $deletedCount products")
}
```

---

## ✅ Xác nhận import thành công

1. Mở **Logcat** trong Android Studio
2. Filter by: `AdminDashboard` hoặc `DatabaseImporter`
3. Xem log:
   ```
   D/AdminDashboard: Import completed!
   Success: 21
   Failed: 0
   ```

4. Kiểm tra **Firestore Console:**
   - Vào Firebase Console → Firestore Database
   - Collection: `products`
   - Sẽ thấy 21 documents

---

## 🎯 Recommended Flow

1. **Lần đầu setup:** Uncomment `importData()` → Run app → Login admin → Auto-import
2. **Sau khi import:** Comment lại dòng `importData()` → Rebuild
3. **Nếu cần reset:** Clear Firestore manually hoặc dùng `clearAllProducts()` → Import lại

---

**Lưu ý:** Import chỉ cần chạy **1 lần duy nhất**. Không cần tạo UI phức tạp cho việc này.
