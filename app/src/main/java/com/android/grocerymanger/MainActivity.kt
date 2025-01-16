package com.android.grocerymanger

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.recyclerview.widget.RecyclerView
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import com.android.grocerymanger.Model.GroceryListModel
import com.android.grocerymanger.Model.GroceryModel
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var groceryList: MutableList<GroceryModel>
    private lateinit var recyclerView: RecyclerView
    private lateinit var groceryAdapter: GroceryAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var glTitle : AppCompatEditText

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        glTitle = findViewById(R.id.gl_title);

        // Set up the drawer toggle
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.open_drawer, R.string.close_drawer
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Show the hamburger icon
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("Grocery Manager")
        supportActionBar?.setBackgroundDrawable(resources.getDrawable(R.color.splashColor))

        groceryList = mutableListOf()
        recyclerView = findViewById(R.id.recyclerView)
        groceryAdapter = GroceryAdapter(groceryList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = groceryAdapter

        // Add Item FAB
        val fabAdd: FloatingActionButton = findViewById(R.id.fabAdd)
        fabAdd.setOnClickListener {
            showAddItemDialog()
        }

        // Clear All Items FAB
        val fabClear: FloatingActionButton = findViewById(R.id.fabClear)
        fabClear.setOnClickListener {
            clearItems()
        }

        // Save Items FAB (Corrected)
        val fabSave: FloatingActionButton = findViewById(R.id.fabSave)
        fabSave.setOnClickListener {
            saveCurrentList()
        }

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_logout -> {
                    FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this, SignInActivity::class.java)
                        startActivity(intent)
                        finish()
                    true
                }
                R.id.grocery_list ->{
                    // Open the Grocery List Activity
                    val intent = Intent(this, GroceryListActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Handle "Done" action from soft keyboard
        glTitle.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Hide the keyboard and clear focus
                hideKeyboard(v)
                v.clearFocus()
                true
            } else {
                false
            }
        }

        // Handle tapping outside the EditText to dismiss the keyboard
        val rootLayout = findViewById<ConstraintLayout>(R.id.main_content)
        rootLayout.setOnTouchListener { _, _ ->
            hideKeyboard(glTitle)
            glTitle.clearFocus()
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent) // Navigate explicitly to HomeActivity
        finish() // Optional: Call finish() to close the current activity
        return true
    }

    // Function to hide the keyboard
    private fun hideKeyboard(view: EditText) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    // Handle the back button when the drawer is open
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Function to clear items
    private fun clearItems() {
        if (groceryList.isNotEmpty()) {
            groceryAdapter.clearItems()  // Only clear if there are items
            Toast.makeText(this, "All items cleared", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No items to clear", Toast.LENGTH_SHORT).show() // Inform user if list is empty
        }
    }

    private fun showAddItemDialog() {
        // Create EditText fields for Item Name, Quantity, and Barcode
        val itemNameEditText = EditText(this)
        itemNameEditText.hint = "Enter item name"
        itemNameEditText.gravity = android.view.Gravity.CENTER  // Center text inside EditText

        val quantityEditText = EditText(this)
        quantityEditText.hint = "Enter quantity"
        quantityEditText.inputType = InputType.TYPE_CLASS_TEXT
        quantityEditText.gravity = android.view.Gravity.CENTER  // Center text inside EditText

        val barcodeEditText = EditText(this)
        barcodeEditText.hint = "Enter barcode"
        barcodeEditText.inputType = InputType.TYPE_CLASS_NUMBER
        barcodeEditText.gravity = android.view.Gravity.CENTER  // Center text inside EditText

        // Create a layout to contain all the EditTexts
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(itemNameEditText)
        layout.addView(quantityEditText)
        layout.addView(barcodeEditText)

// Create and show the dialog with a custom title layout
        val titleLayout = layoutInflater.inflate(R.layout.custom_dialog_title, null) // Inflate custom layout

        MaterialAlertDialogBuilder(this)
            .setCustomTitle(titleLayout) // Set the custom title layout
            .setView(layout)
            .setPositiveButton("Add") { dialog, which ->
                // Get the text inputted by the user
                val itemName = itemNameEditText.text.toString().trim()
                val quantity = quantityEditText.text.toString().trim()
                val barcode = barcodeEditText.text.toString().trim()

                // Validate the inputs
                if (itemName.isNotEmpty()) {
                    // Add the new item to the adapter (assuming groceryAdapter is defined)
                    groceryAdapter.addItem(itemName, quantity , barcode)
                } else {
                    // Show a Toast if any of the fields are empty
                    Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()

    }


    // Save grocery list to file
    private fun saveCurrentList() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Reference to the grocery lists node for the current user
        val groceryListRef = FirebaseDatabase.getInstance().getReference("grocery_lists").child(userId)

        // Get the current title or use a default title
        var title = glTitle.text.toString().trim()
        if (title.isEmpty()) {
            title = "List 1"  // Default title if empty
        }

        // Create the new grocery list data with title and items
        val newGroceryList = hashMapOf<String, Any>(
            "title" to title,
            "list" to groceryList.map {
                hashMapOf(
                    "itemName" to it.itemName,
                    "quantity" to it.quantity,
                    "barcode" to it.barcode
                )
            }
        )

        // Use GenericTypeIndicator to handle the Firebase Map data properly
        val groceryListRef2 = FirebaseDatabase.getInstance().getReference("grocery_lists").child(userId)
        groceryListRef.get().addOnSuccessListener { dataSnapshot ->
            try {
                if (dataSnapshot.exists()) {
                    // Use GenericTypeIndicator to retrieve the data
                    val groceryListsType = object : GenericTypeIndicator<Map<String, Any>>() {}
                    val existingData = dataSnapshot.getValue(groceryListsType)

                    val groceryLists = (existingData?.get("groceryLists") as? ArrayList<HashMap<String, Any>>) ?: arrayListOf()

                    // Add the new grocery list to the existing list
                    groceryLists.add(newGroceryList)

                    // Save the updated grocery lists back to Firebase
                    groceryListRef.setValue(hashMapOf("groceryLists" to groceryLists))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Grocery list added to Realtime Database", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Failed to add grocery list: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // If there is no existing data, simply create a new list
                    val newData = hashMapOf("groceryLists" to arrayListOf(newGroceryList))
                    groceryListRef.setValue(newData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Grocery list saved to Realtime Database", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Failed to save grocery list: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } catch (e: Exception) {
                // Catch any unexpected error during data retrieval or processing
                Toast.makeText(this, "Error processing the data: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()  // Log the stack trace for debugging
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Failed to fetch existing grocery lists: ${exception.message}", Toast.LENGTH_SHORT).show()
            exception.printStackTrace()  // Log the stack trace for debugging
        }
    }

    // Save to text file using MediaStore (for Android Q and above)
    private fun saveToTextFileUsingMediaStore(data: String) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "grocery_list.txt")  // File name
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")  // MIME type
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")  // Save in the Downloads directory
            }

            val contentResolver = contentResolver
            val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

            uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(data)
                        writer.flush()
                    }
                }
            }

            Toast.makeText(this, "Grocery list saved to Downloads", Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save the list", Toast.LENGTH_SHORT).show()
        }
    }

    // Save to text file using FileOutputStream (for Android versions below API 29)
    private fun saveToTextFileUsingFileOutput(data: String) {
        try {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "grocery_list.txt")

            FileOutputStream(file).use { outputStream ->
                outputStream.write(data.toByteArray())
                outputStream.flush()
            }

            Toast.makeText(this, "Grocery list saved to Downloads", Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save the list", Toast.LENGTH_SHORT).show()
        }
    }
}


