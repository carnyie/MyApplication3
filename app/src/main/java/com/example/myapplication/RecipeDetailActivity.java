package com.example.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RecipeDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvIngredients, tvInstructions;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvIngredients = findViewById(R.id.tvDetailIngredients);
        tvInstructions = findViewById(R.id.tvDetailInstructions);
        dbHelper = new DatabaseHelper(this);

        String recipeName = getIntent().getStringExtra("RECIPE_NAME");
        if (recipeName != null) {
            loadRecipeDetails(recipeName);
        }
    }

    private void loadRecipeDetails(String recipeName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_RECIPES +
                " WHERE " + DatabaseHelper.COLUMN_RECIPE_NAME + " = ?", new String[]{recipeName});

        if (cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECIPE_NAME));
            String ingredients = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECIPE_INGREDIENTS));
            String instructions = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECIPE_INSTRUCTIONS));

            tvTitle.setText(title);
            tvIngredients.setText("Ingredients Needed:\n• " + ingredients.replace(", ", "\n• "));
            tvInstructions.setText("Instructions:\n" + instructions);
        }
        cursor.close();
    }
}
