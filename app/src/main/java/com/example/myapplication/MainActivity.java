package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etIngredientName, etQuantity;
    private Button btnAdd, btnSuggestRecipe, btnSuggested;
    private TextView tvRecipeSuggestion;
    private ListView lvPantry;
    private DatabaseHelper dblHelper;
    private ArrayList<String> pantryList;
    private ArrayList<Integer> pantryIds; // Stores DB IDs for updating/deleting
    private ArrayAdapter<String> adapter;
    private int selectedItemId = -1; // Tracks selected item for updates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        etIngredientName = findViewById(R.id.etIngredientName);
        etQuantity = findViewById(R.id.etQuantity);
        btnAdd = findViewById(R.id.btnAdd);
        btnSuggestRecipe = findViewById(R.id.btnSuggestRecipe);
        btnSuggested = findViewById(R.id.btnSuggestedRecipes);
        tvRecipeSuggestion = findViewById(R.id.tvRecipeSuggestion);
        lvPantry = findViewById(R.id.lvPantry);

        dblHelper = new DatabaseHelper(this);
        loadPantryItems();

        // Add or Update ingredient button listener
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etIngredientName.getText().toString().trim();
                String qtyStr = etQuantity.getText().toString().trim();

                if (name.isEmpty() || qtyStr.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                int qty = Integer.parseInt(qtyStr);

                if (selectedItemId == -1) {
                    // Create: Add new item
                    boolean inserted = dblHelper.addPantryItem(name, qty);
                    if (inserted) {
                        Toast.makeText(MainActivity.this, "Ingredient Added!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to Add Ingredient", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Update: Modify existing item
                    boolean updated = dblHelper.updatePantryItem(selectedItemId, name, qty);
                    if (updated) {
                        Toast.makeText(MainActivity.this, "Ingredient Updated!", Toast.LENGTH_SHORT).show();
                        btnAdd.setText("Add Item");
                        selectedItemId = -1;
                    } else {
                        Toast.makeText(MainActivity.this, "Update Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                etIngredientName.setText("");
                etQuantity.setText("");
                loadPantryItems();
            }
        });

        // Single click: Select item to Edit/Update
        lvPantry.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItemId = pantryIds.get(position);
                String fullText = pantryList.get(position);

                // Parse out name and quantity to populate input fields
                String name = fullText.substring(0, fullText.lastIndexOf(" (Qty:"));
                String qtyStr = fullText.substring(fullText.lastIndexOf(" (Qty: ") + 7, fullText.length() - 1);

                etIngredientName.setText(name);
                etQuantity.setText(qtyStr);
                btnAdd.setText("Update Item");
                Toast.makeText(MainActivity.this, "Editing: " + name, Toast.LENGTH_SHORT).show();
            }
        });

        // Long click: Confirm Delete item
        lvPantry.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int itemIdToDelete = pantryIds.get(position);
                String itemName = pantryList.get(position);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Item")
                        .setMessage("Are you sure you want to delete " + itemName + "?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean deleted = dblHelper.deletePantryItem(itemIdToDelete);
                                if (deleted) {
                                    Toast.makeText(MainActivity.this, "Item Deleted", Toast.LENGTH_SHORT).show();
                                    loadPantryItems();
                                } else {
                                    Toast.makeText(MainActivity.this, "Delete Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

                return true;
            }
        });

        // Recipe suggestion button listener
        btnSuggestRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suggestRecipes();
            }
        });

        // Navigate to dedicated Suggested Recipes Activity
        if (btnSuggested != null) {
            btnSuggested.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, SuggestedRecipesActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void loadPantryItems() {
        pantryList = new ArrayList<>();
        pantryIds = new ArrayList<>();

        Cursor cursor = dblHelper.getAllPantryItems();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PANTRY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PANTRY_NAME));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PANTRY_QTY));

                pantryIds.add(id);
                pantryList.add(name + " (Qty: " + qty + ")");
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pantryList);
        lvPantry.setAdapter(adapter);
    }

    private void suggestRecipes() {
        List<String> matchingRecipes = dblHelper.getMatchingRecipes();

        if (matchingRecipes.isEmpty()) {
            tvRecipeSuggestion.setText("No recipes matched your available pantry ingredients.");
        } else {
            StringBuilder builder = new StringBuilder("Suggested Recipes:\n");
            for (String recipe : matchingRecipes) {
                builder.append("• ").append(recipe).append("\n");
            }
            tvRecipeSuggestion.setText(builder.toString());
        }
    }
}