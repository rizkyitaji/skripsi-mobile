@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.bimbingan

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.uhamka.bosm.R
import com.uhamka.bosm.model.Bimbingan
import com.uhamka.bosm.utils.Preferences

class BimbinganAdapter(private var data: List<Bimbingan>,
                       private var listener: (Bimbingan) -> Unit)
    : RecyclerView.Adapter<BimbinganAdapter.LeagueViewHolder>() {

    lateinit var ContextAdapter: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ContextAdapter = parent.context
        val inflatedView = layoutInflater.inflate(R.layout.row_item_bimbingan, parent, false)

        return LeagueViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: LeagueViewHolder, position: Int) {
        holder.bindItem(data[position], listener, ContextAdapter)
    }

    override fun getItemCount(): Int = data.size

    class LeagueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date = view.findViewById<TextView>(R.id.tv_date)
        val detail = view.findViewById<TextView>(R.id.tv_detail)
        val item = view.findViewById<ImageView>(R.id.iv_item)

        fun bindItem(data: Bimbingan, listener: (Bimbingan) -> Unit, context: Context) {
            val preferences = Preferences(context)
            val textDate = data.date.toString()
            val textNIM = data.nim.toString()
            val docs = data.docs.toString()
            date.text = textDate
            detail.text = data.title

            if (preferences.getValues("user").equals("mhs")) {
                item.setImageResource(R.drawable.ic_baseline_delete)
                item.setOnClickListener {
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage("Apakah Anda yakin ingin menghapus catatan bimbingan tanggal " + textDate + " ?")
                            .setPositiveButton("Ya"){ _, _ ->
                                val ref = FirebaseDatabase.getInstance()
                                        .getReference("Bimbingan").child(textNIM).child(textDate)
                                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        ref.removeValue()
                                        if (!docs.equals("")){
                                            val db = FirebaseStorage.getInstance()
                                                    .getReference("docs/Revisi_" + textDate + textNIM)
                                            db.delete()
                                                    .addOnSuccessListener {
                                                        Toast.makeText(context, "Catatan bimbingan telah dihapus", Toast.LENGTH_SHORT).show()
                                                    }

                                                    .addOnFailureListener {
                                                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                                                    }
                                        } else {
                                            Toast.makeText(context, "Catatan bimbingan telah dihapus", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                            .setNegativeButton("Tidak"){ dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                }
            } else {
                if (data.laporan.toString().equals("")) {
                    item.setImageResource(R.drawable.ic_keyboard_arrow_right)
                } else {
                    item.setImageResource(R.drawable.ic_save)
                    item.setOnClickListener {
                        preferences.setValues("action", "download")
                        preferences.setValues("date", data.date.toString())
                        preferences.setValues("laporan", data.laporan.toString())
                        listener(data)
                    }
                }
            }

            itemView.setOnClickListener {
                preferences.setValues("action", "note")
                preferences.setValues("nim", data.nim.toString())
                preferences.setValues("date", data.date.toString())
                preferences.setValues("docs", data.docs.toString())
                preferences.setValues("title", data.title.toString())
                preferences.setValues("revisi", data.revisi.toString())
                preferences.setValues("laporan", data.laporan.toString())
                listener(data)
            }
        }
    }
}