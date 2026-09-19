package com.example.myapplication;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText etIngredientName, etQuantity;
    private Button btnAdd;
    private ListView lvPantry;
    private DatabaseHelper dbHelper;
    private ArrayList<String> pantryList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        etIngredientName = findViewById(R.id.etIngredientName);
        etQuantity = findViewById(R.id.etQuantity);
        btnAdd = findViewById(R.id.btnAdd);
        lvPantry = findViewById(R.id.lvPantry);

        // Initialize Database Helper
        dbHelper = new DatabaseHelper(this);

        pantryList = new ArrayList<>();
        loadPantryItems();

        // Handle Add Button Click
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etIngredientName.getText().toString().trim();
                String qtyStr = etQuantity.getText().toString().trim();

                if (name.isEmpty() || qtyStr.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                int quantity = Integer.parseInt(qtyStr);
                boolean isInserted = dbHelper.addPantryItem(name, quantity);

                if (isInserted) {
                    Toast.makeText(MainActivity.this, "Ingredient Added!", Toast.LENGTH_SHORT).show();
                    etIngredientName.setText("");
                    etQuantity.setText("");
                    loadPantryItems(); // Refresh list view
                } else {
                    Toast.makeText(MainActivity.this, "Failed to add ingredient", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Helper method to fetch all database records and show them in the ListView
    private void loadPantryItems() {
        pantryList.clear();
        Cursor cursor = dbHelper.getAllPantryItems();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PANTRY_NAME));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PANTRY_QTY));
                pantryList.add(name.substring(0, 1).toUpperCase() + name.substring(1) + " (Qty: " + qty + ")");
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pantryList);
        lvPantry.setAdapter(adapter);
    }
}