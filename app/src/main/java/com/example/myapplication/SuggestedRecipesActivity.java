package com.example.myapplication;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView listViewRecipes;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        dbHelper = new DatabaseHelper(this);
        listViewRecipes = findViewById(R.id.listViewRecipes);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        loadSuggestedRecipes();
    }

    private void loadSuggestedRecipes() {
        List<String> matchingRecipes = dbHelper.getMatchingRecipes();

        if (matchingRecipes.isEmpty()) {
            tvEmptyState.setText("No recipes match your pantry yet.\nAdd more ingredients!");
            tvEmptyState.setVisibility(TextView.VISIBLE);
            listViewRecipes.setVisibility(ListView.GONE);
        } else {
            tvEmptyState.setVisibility(TextView.GONE);
            listViewRecipes.setVisibility(ListView.VISIBLE);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    matchingRecipes
            );
            listViewRecipes.setAdapter(adapter);
        }
    }
}
