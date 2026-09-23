# Smart Pantry Manager 🛒📱

This is a  lightweight local-first Android application that has been designed to track household pantry inventory, also to reduce food waste and suggest different types of recipes based on available ingredients. Built with native Java and SQLite.

---

## 🌟 Key Features

* **Pantry Inventory Management (CRUD):**
    * **Create:** Add new items with specific quantities.
    * **Read:** Display all stocked items in a dynamic `ListView`.
    * **Update:** Tap any item in the list to update its name or quantity.
    * **Delete:** Long-press any list item with confirmation to remove it from the database.
* **Smart Recipe Matcher:** Algorithmic matching engine that evaluates pantry inventory against pre-defined recipes and returns options where all required ingredients exist.
* **Responsive UI/UX:** Form input controls optimized for touch interactions (`minHeight="48dp"`) and touch-target accessibility.

---

## 🛠 Tech Stack

* **Language:** Java (JDK 17)
* **Database:** SQLite via `SQLiteOpenHelper`
* **UI Components:** `LinearLayout`, `ListView`, `EditText`, `TextView`, `AlertDialog`, `Toast`
* **Development Environment:** Android Studio (API 34)
* **Version Control:** Git & GitHub

---

## 📁 Architecture & Database Design

The application separates UI presentation from persistence logic using a helper architectural pattern:

### 1. Database Schema (`DatabaseHelper.java`)

```sql
-- Pantry Table
CREATE TABLE pantry (
    _id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT UNIQUE,
    quantity INTEGER
);

-- Recipe Table
CREATE TABLE recipes (
    _id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT,
    ingredients TEXT
);

public List<String> getMatchingRecipes() {
    List<String> matchingRecipes = new ArrayList<>();
    Set<String> pantryIngredients = new HashSet<>();
    
    // 1. Fetch available items from SQLite into a HashSet
    Cursor cursor = getAllPantryItems();
    if (cursor.moveToFirst()) {
        do {
            pantryIngredients.add(cursor.getString(...).toLowerCase());
        } while (cursor.moveToNext());
    }
    cursor.close();

    // 2. Evaluate recipes against available ingredients
    Cursor recipeCursor = db.rawQuery("SELECT * FROM recipes", null);
    if (recipeCursor.moveToFirst()) {
        do {
            String title = recipeCursor.getString(...);
            List<String> required = Arrays.asList(rawIngredients.toLowerCase().split("\\s*,\\s*"));

            // Returns true only if pantry contains ALL required ingredients
            if (pantryIngredients.containsAll(required)) {
                matchingRecipes.add(title);
            }
        } while (recipeCursor.moveToNext());
    }
    recipeCursor.close();
    return matchingRecipes;
}