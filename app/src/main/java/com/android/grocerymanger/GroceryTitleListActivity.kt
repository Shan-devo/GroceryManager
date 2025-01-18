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
import com.android.grocerymanger.R.id.loadingTitleSpinner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// GroceryListActivity.kt
class GroceryListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groceryListAdapter: GroceryTitleListAdapter
    private var groceryLists: MutableList<GroceryListModel> = mutableListOf() // Make it mutable
    private lateinit var loadingSpinner: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grocery_list)

        supportActionBar?.apply {
            title = "Grocery Lists"
            setDisplayHomeAsUpEnabled(true) // Enable the up button
        }
        supportActionBar?.setBackgroundDrawable(resources.getDrawable(R.color.splashColor))

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadingSpinner = findViewById(loadingTitleSpinner)

        // Initialize the adapter with the empty list
        groceryListAdapter = GroceryTitleListAdapter(groceryLists, { selectedList ->
            // Handle item click
            val intent = Intent(this, GroceryDetailActivity::class.java)
            intent.putExtra("Grocery_Title", selectedList.title)
            startActivity(intent)
        }, { groceryList ->
            // Handle delete button click
            deleteItem(groceryList)
        })

        recyclerView.adapter = groceryListAdapter

        // Fetch data from Firebase
        fetchGroceryLists();
    }

    private fun fetchGroceryLists() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val groceryListRef = FirebaseDatabase.getInstance().getReference("grocery_lists").child(userId)

        // Show the progress bar before starting the data fetch operation
        loadingSpinner.visibility = ProgressBar.VISIBLE

        groceryListRef.get().addOnSuccessListener { dataSnapshot ->
            // Hide the progress bar once the data is fetched
            loadingSpinner.visibility = ProgressBar.GONE

            if (dataSnapshot.exists()) {
                try {
                    // Parse the grocery list data
                    val groceryListData = dataSnapshot.getValue(GroceryListData::class.java)
                    groceryListData?.groceryLists?.let { groceryListsFromDb ->

                        val newGroceryLists = mutableListOf<GroceryListModel>()
                        for (groceryListData in groceryListsFromDb) {
                            if(groceryListData.isNullOrEmpty()){
                                continue
                            }
                            val title = groceryListData["title"] as? String ?: "Untitled"
                            val listId = groceryListData["id"] as? String ?: ""
                            val itemsData = groceryListData["list"] as? List<Map<String, String>> ?: emptyList()

                            // Map the items correctly
                            val groceryItems = itemsData.map {
                                GroceryModel(
                                    itemName = it["itemName"] ?: "",
                                    quantity = it["quantity"] ?: "",
                                    barcode = it["barcode"] ?: ""
                                )
                            }

                            // Create a new GroceryListModel
                            val groceryListModel = GroceryListModel(
                                title = title,
                                id = listId,
                                list = groceryItems
                            )

                            // Add the new list to the list of grocery lists
                            newGroceryLists.add(groceryListModel)
                        }

                        // Update the local grocery lists
                        groceryLists = newGroceryLists
                        groceryListAdapter.updateData(groceryLists)  // Update the adapter with new data
                    }
                } catch (e: Exception) {
                    Log.e("GroceryListActivity", "Error parsing grocery list data: ${e.message}")
                    Toast.makeText(this, "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No grocery lists found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            // Hide the progress bar in case of a failure
            loadingSpinner.visibility = ProgressBar.GONE
            Toast.makeText(this, "Failed to fetch grocery lists: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteItem(groceryList: GroceryListModel) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val groceryListRef = FirebaseDatabase.getInstance().getReference("grocery_lists").child(userId).child("groceryLists")

        // Query the database to retrieve all grocery lists
        groceryListRef.get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    Log.d("FirebaseQuery", "Data found: ${dataSnapshot.value}")

                    // Loop through the grocery lists to find the one with the matching title
                    var found = false
                    for (snapshot in dataSnapshot.children) {
                        val list = snapshot.getValue(GroceryListModel::class.java)
                        if (list?.title == groceryList.title) {
                            Log.d("FirebaseQuery", "Deleting node: ${snapshot.key}")
                            snapshot.ref.removeValue()
                                .addOnSuccessListener {
                                    // On successful deletion
                                    Toast.makeText(this, "List deleted successfully", Toast.LENGTH_SHORT).show()
                                    // Update the local list and notify the adapter
                                    groceryLists = groceryLists.filterNot { it.id == groceryList.id }.toMutableList()
                                    groceryListAdapter.updateData(groceryLists)
                                }
                                .addOnFailureListener { exception ->
                                    // In case of failure
                                    Toast.makeText(this, "Failed to delete list: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            found = true
                            break
                        }
                    }

                    if (!found) {
                        Log.d("FirebaseQuery", "No matching grocery list found.")
                        Toast.makeText(this, "Grocery list not found", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Log.d("FirebaseQuery", "No grocery lists found.")
                    Toast.makeText(this, "No grocery lists found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                // Handle query failure
                Log.e("FirebaseQuery", "Error fetching data: ${exception.message}")
                Toast.makeText(this, "Failed to fetch lists: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Handle the back button in the ActionBar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}


