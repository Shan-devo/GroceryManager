package com.android.grocerymanger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.grocerymanger.Model.GroceryListData
import com.android.grocerymanger.Model.GroceryListModel
import com.android.grocerymanger.Model.GroceryModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// GroceryListActivity.kt
class GroceryListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groceryListAdapter: GroceryTitleListAdapter
    private var groceryLists: List<GroceryListModel> = emptyList()
    private lateinit var loadingSpinner: ProgressBar

    // Handle the back button in the ActionBar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle the back navigation
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grocery_list)

        // Enabling the ActionBar and the back button
        supportActionBar?.apply {
            title = "Grocery Lists"
            setDisplayHomeAsUpEnabled(true) // Enable the up button
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadingSpinner = findViewById(R.id.loadingSpinner);

        // Initialize the adapter with the empty list
        groceryListAdapter = GroceryTitleListAdapter(groceryLists) { selectedList ->
            // When a grocery list is clicked, navigate to the detail activity
            val intent = Intent(this, GroceryDetailActivity::class.java)
            intent.putExtra("Grocery_Title", selectedList.title)
            startActivity(intent)
        }

        recyclerView.adapter = groceryListAdapter

        loadingSpinner.visibility = ProgressBar.VISIBLE;
        // Fetch data from Firebase
        fetchGroceryLists()
        loadingSpinner.visibility = ProgressBar.GONE;
    }

    private fun fetchGroceryLists() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val groceryListRef = FirebaseDatabase.getInstance().getReference("grocery_lists").child(userId)

        // Fetch the existing grocery lists from Firebase
        groceryListRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                Log.d("FirebaseData", "Data found for user: $userId")

                val groceryListData = dataSnapshot.getValue(GroceryListData::class.java)
                Log.d("FirebaseData", "Fetched data: $groceryListData")

                groceryListData?.groceryLists?.let { groceryListsFromDb ->
                    if (groceryListsFromDb.isEmpty()) {
                        Log.d("FirebaseData", "No grocery lists found in the database.")
                    }

                    // Convert the Firebase data into a list of GroceryListModel objects
                    val newGroceryLists = mutableListOf<GroceryListModel>()
                    for (groceryListData in groceryListsFromDb) {
                        val title = groceryListData["title"] as? String ?: "Untitled"
                        val listId = groceryListData["id"] as? String ?: ""
                        val itemsData = groceryListData["list"] as? List<Map<String, String>> ?: emptyList()

                        // Log the individual grocery list data
                        Log.d("FirebaseData", "Grocery List: $title, ID: $listId")

                        val groceryItems = itemsData.map {
                            GroceryModel(
                                itemName = it["itemName"] ?: "",
                                quantity = it["quantity"] ?: "",
                                barcode = it["barcode"] ?: ""
                            )
                        }

                        val groceryListModel = GroceryListModel(
                            title = title,
                            id = listId,
                            list = groceryItems
                        )

                        newGroceryLists.add(groceryListModel)
                    }

                    // Update the adapter's data
                    groceryLists = newGroceryLists
                    groceryListAdapter.updateData(groceryLists)  // Update the adapter with new data
                }
            } else {
                Log.d("FirebaseData", "No grocery lists found for user: $userId")
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Failed to fetch grocery lists: ${exception.message}", Toast.LENGTH_SHORT).show()
            exception.printStackTrace()  // Log the stack trace for debugging
        }
    }
}

