package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartPantry.db";
    private static final int DATABASE_VERSION = 1;

    // Pantry Table Constants
    public static final String TABLE_PANTRY = "pantry";
    public static final String COLUMN_PANTRY_ID = "_id";
    public static final String COLUMN_PANTRY_NAME = "name";
    public static final String COLUMN_PANTRY_QTY = "quantity";

    // Recipe Table Constants
    public static final String TABLE_RECIPES = "recipes";
    public static final String COLUMN_RECIPE_ID = "_id";
    public static final String COLUMN_RECIPE_NAME = "title";
    public static final String COLUMN_RECIPE_INGREDIENTS = "ingredients"; // Comma-separated: "egg, butter, salt"
    public static final String COLUMN_RECIPE_INSTRUCTIONS = "instructions";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Pantry Table
        String createPantryTable = "CREATE TABLE " + TABLE_PANTRY + " (" +
                COLUMN_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PANTRY_NAME + " TEXT UNIQUE, " +
                COLUMN_PANTRY_QTY + " INTEGER);";
        db.execSQL(createPantryTable);

        // Create Recipe Table
        String createRecipeTable = "CREATE TABLE " + TABLE_RECIPES + " (" +
                COLUMN_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RECIPE_NAME + " TEXT, " +
                COLUMN_RECIPE_INGREDIENTS + " TEXT, " +
                COLUMN_RECIPE_INSTRUCTIONS + " TEXT);";
        db.execSQL(createRecipeTable);

        // Pre-seed 15 Initial Recipes
        seedInitialRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }

    // the database with 15 recipes upon creation
    private void seedInitialRecipes(SQLiteDatabase db) {
        insertRecipe(db, "Scrambled Eggs", "egg, butter, salt", "Whisk eggs with salt. Melt butter in pan and cook gently.");
        insertRecipe(db, "Omelette", "egg, butter, cheese, salt", "Beat eggs, pour into hot buttered pan, sprinkle cheese, and fold.");
        insertRecipe(db, "Grilled Cheese Sandwich", "bread, cheese, butter", "Butter bread, place cheese inside, and grill until golden.");
        insertRecipe(db, "Garlic Bread", "bread, butter, garlic", "Mix butter and minced garlic. Spread on bread and bake.");
        insertRecipe(db, "Toast with Butter", "bread, butter", "Toast bread and spread butter evenly.");
        insertRecipe(db, "French Toast", "bread, egg, milk, butter", "Dip bread in beaten egg and milk mixture. Fry in butter.");
        insertRecipe(db, "Pancakes", "flour, milk, egg, sugar, butter", "Mix dry and wet ingredients. Pour batter onto hot buttered skillet.");
        insertRecipe(db, "Boiled Eggs", "egg, water", "Place eggs in boiling water for 7-10 minutes.");
        insertRecipe(db, "Tomato Soup", "tomato, butter, onion, salt", "Sauté onion in butter, add tomatoes and salt, simmer and blend.");
        insertRecipe(db, "Fried Rice", "rice, egg, soy sauce, oil", "Heat oil, fry cooked rice with beaten egg and soy sauce.");
        insertRecipe(db, "Steamed Rice", "rice, water, salt", "Boil water with salt, add rice, cover and cook on low heat.");
        insertRecipe(db, "Pasta Margherita", "pasta, tomato, cheese, olive oil", "Boil pasta. Toss with crushed tomato, olive oil, and melted cheese.");
        insertRecipe(db, "Simple Salad", "lettuce, tomato, olive oil, salt", "Chop lettuce and tomato. Dress with olive oil and salt.");
        insertRecipe(db, "Mashed Potatoes", "potato, butter, milk, salt", "Boil potatoes until soft, mash with butter, milk, and salt.");
        insertRecipe(db, "Potato Chips", "potato, oil, salt", "Slice potatoes thinly, deep fry in oil, and season with salt.");
    }

    private void insertRecipe(SQLiteDatabase db, String name, String ingredients, String instructions) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_RECIPE_NAME, name);
        values.put(COLUMN_RECIPE_INGREDIENTS, ingredients);
        values.put(COLUMN_RECIPE_INSTRUCTIONS, instructions);
        db.insert(TABLE_RECIPES, null, values);
    }

    // PANTRY CRUD OPERATIONS

    // Create: Add new ingredient to pantry
    public boolean addPantryItem(String name, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PANTRY_NAME, name.trim().toLowerCase());
        cv.put(COLUMN_PANTRY_QTY, quantity);
        long result = db.insert(TABLE_PANTRY, null, cv);
        return result != -1;
    }

    // Retrieve all pantry items
    public Cursor getAllPantryItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PANTRY, null);
    }

    // Modify existing pantry item
    public boolean updatePantryItem(int id, String name, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PANTRY_NAME, name.trim().toLowerCase());
        cv.put(COLUMN_PANTRY_QTY, quantity);
        int result = db.update(TABLE_PANTRY, cv, COLUMN_PANTRY_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    //
    // Remove pantry item
    public boolean deletePantryItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_PANTRY, COLUMN_PANTRY_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    // --- STRICT MATCHING ALGORITHM ---

    // Retrieves only recipes where ALL required ingredients exist in the pantry
    public List<String> getMatchingRecipes() {
        List<String> matchingRecipes = new ArrayList<>();

        // Fetch set of available pantry ingredients
        Set<String> pantryIngredients = new HashSet<>();
        Cursor pantryCursor = getAllPantryItems();
        if (pantryCursor.moveToFirst()) {
            do {
                pantryIngredients.add(pantryCursor.getString(pantryCursor.getColumnIndexOrThrow(COLUMN_PANTRY_NAME)).toLowerCase());
            } while (pantryCursor.moveToNext());
        }
        pantryCursor.close();

        // Evaluate recipes against available pantry ingredients
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor recipeCursor = db.rawQuery("SELECT * FROM " + TABLE_RECIPES, null);
        if (recipeCursor.moveToFirst()) {
            do {
                String title = recipeCursor.getString(recipeCursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME));
                String rawIngredients = recipeCursor.getString(recipeCursor.getColumnIndexOrThrow(COLUMN_RECIPE_INGREDIENTS));

                List<String> requiredList = Arrays.asList(rawIngredients.toLowerCase().split("\\s*,\\s*"));

                // Strict matching check: pantry MUST contain ALL required ingredients
                if (pantryIngredients.containsAll(requiredList)) {
                    matchingRecipes.add(title);
                }
            } while (recipeCursor.moveToNext());
        }
        recipeCursor.close();

        return matchingRecipes;
    }
}