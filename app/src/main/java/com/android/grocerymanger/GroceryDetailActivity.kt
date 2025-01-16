package com.android.grocerymanger

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.grocerymanger.Model.GroceryListModel
import com.android.grocerymanger.Model.GroceryModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// GroceryDetailActivity.kt
class GroceryDetailActivity : AppCompatActivity() {

    private lateinit var groceryListTitle: TextView
    private lateinit var groceryListItems: RecyclerView
    private lateinit var groceryAdapter: GroceryAdapter
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
        setContentView(R.layout.activity_grocery_detail)

        // Enabling the ActionBar and the back button
        supportActionBar?.apply {
            title = "Grocery Lists"
            setDisplayHomeAsUpEnabled(true) // Enable the up button
        }

        groceryListTitle = findViewById(R.id.groceryListTitle)
        groceryListItems = findViewById(R.id.groceryListItems)
        loadingSpinner = findViewById(R.id.loadingSpinner)
        groceryListItems.layoutManager = LinearLayoutManager(this)

        val groceryTitle = intent.getStringExtra("Grocery_Title")

        // Show loading spinner
        loadingSpinner.visibility = ProgressBar.VISIBLE

        // Fetch the grocery list details asynchronously
        fetchGroceryListDetails(groceryTitle) { groceryList ->
            // Hide loading spinner once data is fetched
            loadingSpinner.visibility = ProgressBar.GONE

            if (groceryList != null) {
                // Update the UI with the fetched grocery list
                groceryListTitle.text = groceryList.title
                groceryAdapter = GroceryAdapter(groceryList.list.toMutableList())
                groceryListItems.adapter = groceryAdapter
            } else {
                // Handle the error case
                Toast.makeText(this, "Failed to load grocery list.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fetch grocery list details based on title or id
    private fun fetchGroceryListDetails(listId: String?, callback: (GroceryListModel?) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val groceryListRef = FirebaseDatabase.getInstance().getReference("grocery_lists").child(userId ?: "")

        // Perform a database query to find the grocery list by title or id
        groceryListRef.child("groceryLists").get().addOnSuccessListener { dataSnapshot ->
            var groceryList: GroceryListModel? = null

            if (dataSnapshot.exists()) {
                // Loop through the groceryLists and find the matching list by title or listId
                for (snapshot in dataSnapshot.children) {
                    val title = snapshot.child("title").getValue(String::class.java)
                    val id = snapshot.key // Assuming the key is the listId or another unique identifier

                    // Log data to check the structure
                    Log.d("GroceryDetailActivity", "Checking grocery list with title: $title and id: $id")

                    // Match by title or id
                    if (title == listId || id == listId) {
                        // Once the matching list is found, retrieve the items and populate the GroceryListModel
                        val list = snapshot.child("list").children.mapNotNull {
                            // Check if it has the correct data and deserialize safely
                            val item = it.getValue(GroceryModel::class.java)
                            if (item != null) {
                                item
                            } else {
                                Log.e("GroceryDetailActivity", "Failed to deserialize grocery item")
                                null
                            }
                        }

                        groceryList = GroceryListModel(title ?: "", listId ?: "", list)
                        break
                    }
                }
            } else {
                // Handle the case where no lists are found
                Log.e("GroceryDetailActivity", "No grocery lists found.")
                Toast.makeText(this, "No grocery lists found.", Toast.LENGTH_SHORT).show()
            }

            // Return the result through the callback
            callback(groceryList)
        }.addOnFailureListener { exception ->
            // Handle failure
            Toast.makeText(this, "Failed to fetch grocery list: ${exception.message}", Toast.LENGTH_SHORT).show()
            exception.printStackTrace()  // Log the stack trace for debugging

            // Return null in case of failure
            callback(null)
        }
    }
}
