package com.android.grocerymanger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.grocerymanger.Model.GroceryModel

class GroceryAdapter(private val groceryList: MutableList<GroceryModel>) :
    RecyclerView.Adapter<GroceryAdapter.GroceryViewHolder>() {

    inner class GroceryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemTextView: TextView = itemView.findViewById(R.id.itemName)
        val quantityTextView: TextView = itemView.findViewById(R.id.quantity)
        val barcodeTextView: TextView = itemView.findViewById(R.id.barcode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grocery, parent, false)
        return GroceryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GroceryViewHolder, position: Int) {
        val groceryItem = groceryList[position]
        holder.itemTextView.text = groceryItem.itemName
        holder.quantityTextView.text = groceryItem.quantity
        holder.barcodeTextView.text = groceryItem.barcode
    }

    override fun getItemCount(): Int = groceryList.size

    fun addItem(itemName: String, quantity: String, barcode: String) {
        // Create a new GroceryItem and add it to the list
        val groceryItem = GroceryModel(itemName, quantity, barcode)
        groceryList.add(groceryItem)
        notifyItemInserted(groceryList.size - 1)
    }

    fun clearItems() {
        groceryList.clear()
        notifyDataSetChanged()
    }
}
