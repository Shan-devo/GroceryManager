package com.android.grocerymanger

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.grocerymanger.Model.GroceryListModel
import com.android.grocerymanger.Model.GroceryModel

// GroceryDetailActivity.kt
class GroceryDetailActivity : AppCompatActivity() {

    private lateinit var groceryListTitle: TextView
    private lateinit var groceryListItems: RecyclerView
    private lateinit var groceryAdapter: GroceryAdapter
    private lateinit var groceryList: GroceryListModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grocery_detail)

        groceryListTitle = findViewById(R.id.groceryListTitle)
        groceryListItems = findViewById(R.id.groceryListItems)
        groceryListItems.layoutManager = LinearLayoutManager(this)

        val groceryListId = intent.getStringExtra("GROCERY_LIST_ID")

        // Fetch the grocery list details based on the ID
        groceryList = fetchGroceryListDetails(groceryListId)

        groceryListTitle.text = groceryList.title
        groceryAdapter = GroceryAdapter(groceryList.list.toMutableList())
        groceryListItems.adapter = groceryAdapter
    }

    private fun fetchGroceryListDetails(listId: String?): GroceryListModel {
        // Replace with actual fetching logic (from Firebase or other data source)
        return GroceryListModel("Home List", "1", listOf(
            GroceryModel("Apple", "2", "12345"),
            GroceryModel("Banana", "3", "67890")
        ))
    }
}
