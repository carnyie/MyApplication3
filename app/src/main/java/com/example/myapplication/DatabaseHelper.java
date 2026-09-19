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

    // Table for Pantry
    public static final String TABLE_PANTRY = "pantry";
    public static final String COLUMN_PANTRY_ID = "_id";
    public static final String COLUMN_PANTRY_NAME = "name";
    public static final String COLUMN_PANTRY_QTY = "quantity";

    // Table for Receipt
    public static final String TABLE_RECIPES = "recipes";
    public static final String COLUMN_RECIPE_ID = "_id";
    public static final String COLUMN_RECIPE_NAME = "title";
    public static final String COLUMN_RECIPE_INGREDIENTS = "ingredients"; // Comma-separated: "egg,milk,flour"

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PANTRY = "CREATE TABLE " + TABLE_PANTRY + " (" +
                COLUMN_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PANTRY_NAME + " TEXT UNIQUE, " +
                COLUMN_PANTRY_QTY + " INTEGER);";

        String CREATE_RECIPES = "CREATE TABLE " + TABLE_RECIPES + " (" +
                COLUMN_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RECIPE_NAME + " TEXT, " +
                COLUMN_RECIPE_INGREDIENTS + " TEXT);";

        db.execSQL(CREATE_PANTRY);
        db.execSQL(CREATE_RECIPES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }

    // CRUD Operation

    // Add Ingredient creation
    public boolean addPantryItem(String name, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PANTRY_NAME, name.trim().toLowerCase());
        cv.put(COLUMN_PANTRY_QTY, quantity);
        long result = db.insert(TABLE_PANTRY, null, cv);
        return result != -1;
    }

    //  Get all the Ingridients
    public Cursor getAllPantryItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PANTRY, null);
    }

    //  Modify Quantity
    public boolean updatePantryItem(int id, String name, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PANTRY_NAME, name.trim().toLowerCase());
        cv.put(COLUMN_PANTRY_QTY, quantity);
        int result = db.update(TABLE_PANTRY, cv, COLUMN_PANTRY_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    // 4. Delete Ingridients
    public boolean deletePantryItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_PANTRY, COLUMN_PANTRY_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    //  MATCHING RECIPE ALGORITHM

    // Retrieves only recipes where ALL required ingredients exist in the pantry
    public List<String> getMatchingRecipes() {
        List<String> matchingRecipes = new ArrayList<>();

        // Get set of available pantry ingredients
        Set<String> pantryIngredients = new HashSet<>();
        Cursor pantryCursor = getAllPantryItems();
        if (pantryCursor.moveToFirst()) {
            do {
                pantryIngredients.add(pantryCursor.getString(pantryCursor.getColumnIndexOrThrow(COLUMN_PANTRY_NAME)).toLowerCase());
            } while (pantryCursor.moveToNext());
        }
        pantryCursor.close();

        // Check recipes against pantry
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
