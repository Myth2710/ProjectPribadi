package com.example.kosongan

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class Fragment01 : Fragment() {

    private lateinit var adapter: MyAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var emptyText: TextView

    private val mainActivity: MainActivity
        get() = requireActivity() as MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_01, container, false)

        recycler = v.findViewById(R.id.recyclerKategori)
        emptyText = v.findViewById(R.id.txtKosongKategori)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = MyAdapter(
            items = emptyList(),
            onClick = { position -> tampilkanDialogKategori(position) },
            onLongClick = { position -> konfirmasiHapus(position) }
        )
        recycler.adapter = adapter

        v.findViewById<MaterialButton>(R.id.btnTambahKategori).setOnClickListener {
            tampilkanDialogKategori(null)
        }

        refreshList()
        return v
    }

    private fun refreshList() {
        if (!isAdded) return
        val categories = mainActivity.financeData.categories
        adapter.updateItems(categories.map { it.name })
        emptyText.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun tampilkanDialogKategori(position: Int?) {
        val editText = EditText(requireContext()).apply {
            hint = "Nama kategori"
            setSingleLine(true)
            if (position != null) setText(mainActivity.financeData.categories[position].name)
            setPadding(48, 24, 48, 24)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (position == null) "Tambah Kategori" else "Ubah Kategori")
            .setView(editText)
            .setNegativeButton("Batal", null)
            .setPositiveButton("Simpan") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Nama kategori wajib diisi", Toast.LENGTH_SHORT).show()
                } else {
                    val categories = mainActivity.financeData.categories
                    val duplicate = categories.any {
                        it.name.equals(name, ignoreCase = true) &&
                            (position == null || categories.indexOf(it) != position)
                    }
                    if (duplicate) {
                        Toast.makeText(requireContext(), "Kategori sudah ada", Toast.LENGTH_SHORT).show()
                    } else {
                        if (position == null) {
                            categories.add(FinanceCategory(name = name))
                        } else {
                            val oldName = categories[position].name
                            categories[position].name = name
                            mainActivity.financeData.transactions
                                .filter { it.category == oldName }
                                .forEach { it.category = name }
                        }
                        mainActivity.simpanKeSP()
                        refreshList()
                    }
                }
            }
            .show()
    }

    private fun konfirmasiHapus(position: Int) {
        val category = mainActivity.financeData.categories[position]
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus kategori?")
            .setMessage("Kategori ${category.name} akan dihapus. Transaksi lama tetap tersimpan.")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ ->
                mainActivity.financeData.categories.removeAt(position)
                mainActivity.simpanKeSP()
                refreshList()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) refreshList()
    }
}
