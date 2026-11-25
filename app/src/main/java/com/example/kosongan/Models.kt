package com.example.kosongan

data class FinanceCategory(
    var id: Long = System.currentTimeMillis(),
    var name: String = ""
)

data class FinanceTransaction(
    var id: Long = System.currentTimeMillis(),
    var type: String = "INCOME",
    var title: String = "",
    var amount: Double = 0.0,
    var category: String = "",
    var date: String = ""
)

data class FinanceData(
    var categories: ArrayList<FinanceCategory> = arrayListOf(),
    var transactions: ArrayList<FinanceTransaction> = arrayListOf()
)
