package com.android.grocerymanger.Model

data class GroceryListModel(
    val title: String = "",
    val id: String = "",  // Unique ID to identify the list
    val list: List<GroceryModel> = listOf()
)