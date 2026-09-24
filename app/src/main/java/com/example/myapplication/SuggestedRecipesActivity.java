package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView listViewRecipes;
    private TextView tvEmptyState;
    private List<String> matchingRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        dbHelper = new DatabaseHelper(this);
        listViewRecipes = findViewById(R.id.listViewRecipes);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        loadSuggestedRecipes();

        // Tap recipe to open RecipeDetailActivity
        listViewRecipes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (matchingRecipes != null && position < matchingRecipes.size()) {
                    String selectedRecipe = matchingRecipes.get(position);
                    Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
                    intent.putExtra("RECIPE_NAME", selectedRecipe);
                    startActivity(intent);
                }
            }
        });
    }

    private void loadSuggestedRecipes() {
        matchingRecipes = dbHelper.getMatchingRecipes();

        if (matchingRecipes.isEmpty()) {
            tvEmptyState.setText("No recipes match your pantry yet.\nAdd more ingredients!");
            tvEmptyState.setVisibility(View.VISIBLE);
            listViewRecipes.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            listViewRecipes.setVisibility(View.VISIBLE);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    matchingRecipes
            );
            listViewRecipes.setAdapter(adapter);
        }
    }
}
