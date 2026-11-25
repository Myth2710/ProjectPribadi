package com.example.kosongan

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Fragment02 : Fragment() {

    private lateinit var adapter: MyAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var emptyText: TextView
    private var selectedType = "INCOME"

    private val mainActivity: MainActivity
        get() = requireActivity() as MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_02, container, false)

        recycler = v.findViewById(R.id.recyclerTransaksi)
        emptyText = v.findViewById(R.id.txtKosongTransaksi)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = MyAdapter(
            items = emptyList(),
            onClick = { position ->
                tampilkanDialogTransaksi(position)
            },
            onLongClick = { position ->
                konfirmasiHapus(position)
            }
        )

        recycler.adapter = adapter

        val btnIncome = v.findViewById<MaterialButton>(R.id.btnIncome)
        val btnExpense = v.findViewById<MaterialButton>(R.id.btnExpense)

        btnIncome.setOnClickListener {
            selectedType = "INCOME"
            updateTypeButtons(btnIncome, btnExpense)
            refreshList()
        }

        btnExpense.setOnClickListener {
            selectedType = "EXPENSE"
            updateTypeButtons(btnIncome, btnExpense)
            refreshList()
        }

        v.findViewById<MaterialButton>(R.id.btnTambahTransaksi)
            .setOnClickListener {
                tampilkanDialogTransaksi(null)
            }

        updateTypeButtons(btnIncome, btnExpense)
        refreshList()

        return v
    }

    private fun updateTypeButtons(
        income: MaterialButton,
        expense: MaterialButton
    ) {
        income.alpha = if (selectedType == "INCOME") 1f else 0.55f
        expense.alpha = if (selectedType == "EXPENSE") 1f else 0.55f
    }

    private fun transaksiTerfilter() =
        mainActivity.financeData.transactions
            .filter { it.type == selectedType }
            .sortedByDescending { it.id }

    private fun formatRupiah(amount: Double): String =
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(amount)

    private fun refreshList() {
        if (!isAdded) return

        val data = transaksiTerfilter()

        adapter.updateItems(data.map {
            val tanda = if (it.type == "INCOME") "+" else "-"

            "$tanda${formatRupiah(it.amount)}  •  ${it.title}\n${it.category}  •  ${it.date}"
        })

        emptyText.visibility =
            if (data.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun tampilkanDialogTransaksi(position: Int?) {
        val categories = mainActivity.financeData.categories

        if (categories.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Tambahkan kategori terlebih dahulu",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val current =
            if (position == null) null else transaksiTerfilter()[position]

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
        }

        val titleInput = EditText(requireContext()).apply {
            hint = "Nama transaksi"
            setSingleLine(true)
            setText(current?.title ?: "")
        }

        val amountInput = EditText(requireContext()).apply {
            hint = "Jumlah (contoh: 50000)"
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

            setText(
                if (current != null) {
                    current.amount.toLong().toString()
                } else {
                    ""
                }
            )
        }

        val categorySpinner = Spinner(requireContext())

        categorySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories.map { it.name }
        )

        val currentIndex =
            categories.indexOfFirst { it.name == current?.category }

        if (currentIndex >= 0) {
            categorySpinner.setSelection(currentIndex)
        }

        layout.addView(titleInput)
        layout.addView(amountInput)
        layout.addView(categorySpinner)

        AlertDialog.Builder(requireContext())
            .setTitle(
                if (position == null) {
                    if (selectedType == "INCOME") {
                        "Tambah Pemasukan"
                    } else {
                        "Tambah Pengeluaran"
                    }
                } else {
                    "Ubah Transaksi"
                }
            )
            .setView(layout)
            .setNegativeButton("Batal", null)
            .setPositiveButton("Simpan", null)
            .create()
            .also { dialog ->

                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener {

                            val title =
                                titleInput.text.toString().trim()

                            val amountText =
                                amountInput.text.toString().trim()

                            val amount = amountText.toDoubleOrNull()

                            if (
                                title.isEmpty() ||
                                amount == null ||
                                amount <= 0
                            ) {
                                Toast.makeText(
                                    requireContext(),
                                    "Isi nama dan jumlah yang valid",
                                    Toast.LENGTH_SHORT
                                ).show()

                                return@setOnClickListener
                            }

                            val chosenCategory =
                                categorySpinner.selectedItem.toString()

                            val date = current?.date
                                ?: SimpleDateFormat(
                                    "dd/MM/yyyy",
                                    Locale("id", "ID")
                                ).format(Date())

                            val transactions =
                                mainActivity.financeData.transactions

                            if (current == null) {

                                transactions.add(
                                    FinanceTransaction(
                                        type = selectedType,
                                        title = title,
                                        amount = amount,
                                        category = chosenCategory,
                                        date = date
                                    )
                                )

                            } else {

                                val index = transactions.indexOfFirst {
                                    it.id == current.id
                                }

                                if (index >= 0) {
                                    transactions[index].title = title
                                    transactions[index].amount = amount
                                    transactions[index].category =
                                        chosenCategory
                                }
                            }

                            mainActivity.simpanKeSP()
                            refreshList()
                            dialog.dismiss()
                        }
                }

                dialog.show()
            }
    }

    private fun konfirmasiHapus(position: Int) {
        val transaction = transaksiTerfilter()[position]

        AlertDialog.Builder(requireContext())
            .setTitle("Hapus transaksi?")
            .setMessage(
                "Transaksi ${transaction.title} akan dihapus."
            )
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ ->

                mainActivity.financeData.transactions.removeAll {
                    it.id == transaction.id
                }

                mainActivity.simpanKeSP()
                refreshList()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()

        if (::adapter.isInitialized) {
            refreshList()
        }
    }
}
