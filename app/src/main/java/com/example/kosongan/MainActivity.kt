package com.example.kosongan

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    lateinit var sp: android.content.SharedPreferences
    var financeData = FinanceData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sp = getSharedPreferences("dataSP", MODE_PRIVATE)
        bacaDariSP()

        if (financeData.categories.isEmpty()) {
            financeData.categories.addAll(
                listOf(
                    FinanceCategory(name = "Makanan"),
                    FinanceCategory(name = "Transportasi"),
                    FinanceCategory(name = "Belanja"),
                    FinanceCategory(name = "Gaji"),
                    FinanceCategory(name = "Hiburan")
                )
            )
            simpanKeSP()
        }

        findViewById<Button>(R.id.btnKategori).setOnClickListener {
            bukaFragment(Fragment01())
            findViewById<Button>(R.id.btnKategori).isSelected = true
            findViewById<Button>(R.id.btnTransaksi).isSelected = false
        }

        findViewById<Button>(R.id.btnTransaksi).setOnClickListener {
            bukaFragment(Fragment02())
            findViewById<Button>(R.id.btnKategori).isSelected = false
            findViewById<Button>(R.id.btnTransaksi).isSelected = true
        }

        if (savedInstanceState == null) {
            bukaFragment(Fragment01())
            findViewById<Button>(R.id.btnKategori).isSelected = true
        }
    }

    private fun bukaFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.mylayout, fragment)
            .commit()
    }

    private fun bacaDariSP() {
        val gson = Gson()
        val isiSP = sp.getString("spFinance", null)
        val type = object : TypeToken<FinanceData>() {}.type
        if (isiSP != null) {
            try {
                financeData = gson.fromJson(isiSP, type) ?: FinanceData()
            } catch (_: Exception) {
                financeData = FinanceData()
                Toast.makeText(this, "Data penyimpanan tidak dapat dibaca", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun simpanKeSP() {
        val gson = Gson()
        val json = gson.toJson(financeData)
        sp.edit().putString("spFinance", json).apply()
    }
}
