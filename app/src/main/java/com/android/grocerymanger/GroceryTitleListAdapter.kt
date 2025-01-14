package com.android.grocerymanger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.grocerymanger.Model.GroceryListModel
import com.android.grocerymanger.R

class GroceryTitleListAdapter(
    private var groceryLists: List<GroceryListModel>,
    private val onItemClick: (GroceryListModel) -> Unit
) : RecyclerView.Adapter<GroceryTitleListAdapter.GroceryListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.activity_grocery_title_list, parent, false)
        return GroceryListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GroceryListViewHolder, position: Int) {
        val groceryList = groceryLists[position]
        holder.bind(groceryList)
    }

    override fun getItemCount(): Int = groceryLists.size

    fun updateData(newGroceryLists: List<GroceryListModel>) {
        groceryLists = newGroceryLists
        notifyDataSetChanged()  // Notify the adapter that the data has changed
    }

    inner class GroceryListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.list_title)

        fun bind(groceryList: GroceryListModel) {
            titleTextView.text = groceryList.title
            itemView.setOnClickListener {
                onItemClick(groceryList)
            }
        }
    }
}
