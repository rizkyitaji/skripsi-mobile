@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.event

import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
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
import com.uhamka.bosm.model.Event
import com.uhamka.bosm.utils.Preferences

class EventAdapter(private var data: List<Event>,
                   private var listener: (Event) -> Unit)
    : RecyclerView.Adapter<EventAdapter.LeagueViewHolder>() {

    lateinit var ContextAdapter: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ContextAdapter = parent.context
        val inflatedView = layoutInflater.inflate(R.layout.row_item_info, parent, false)

        return LeagueViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: LeagueViewHolder, position: Int) {
        holder.bindItem(data[position], listener, ContextAdapter)
    }

    override fun getItemCount(): Int = data.size

    class LeagueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val info = view.findViewById<TextView>(R.id.tv_info)
        val detail = view.findViewById<ImageView>(R.id.iv_detail)

        fun bindItem(data: Event, listener: (Event) -> Unit, context: Context) {
            val preferences = Preferences(context)
            val title = data.title.toString()
            val pdf = data.pdf.toString()
            val id = data.id.toString()
            info.text = title

            if (preferences.getValues("user").equals("admin")) {
                detail.setImageResource(R.drawable.ic_baseline_delete)
                detail.setOnClickListener {
                    if (isConnected(context)) {
                        val builder = AlertDialog.Builder(context)
                        builder.setMessage("Apakah Anda yakin ingin menghapus " + title + "?")
                                .setPositiveButton("Ya") { _, _ ->
                                    val ref = FirebaseDatabase.getInstance()
                                            .getReference("Informasi").child(id)
                                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            ref.removeValue()
                                            if (!pdf.equals("")){
                                                val db = FirebaseStorage.getInstance().getReference("pdf/" + title)
                                                db.delete()
                                                        .addOnSuccessListener {
                                                            Toast.makeText(context, title + " telah dihapus", Toast.LENGTH_SHORT).show()
                                                        }

                                                        .addOnFailureListener {
                                                            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                                                        }
                                            } else {
                                                Toast.makeText(context, title + " telah dihapus", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                }
                                .setNegativeButton("Tidak") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create()
                                .show()
                    } else {
                        Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                detail.setImageResource(R.drawable.ic_keyboard_arrow_right)
            }

            itemView.setOnClickListener {
                preferences.setValues("id", data.id.toString())
                preferences.setValues("pdf", data.pdf.toString())
                preferences.setValues("title", data.title.toString())
                preferences.setValues("detail", data.detail.toString())
                listener(data)
            }
        }

        private fun isConnected(context: Context): Boolean {
            val connectivity = context.getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = connectivity.activeNetworkInfo
            if (info != null) {
                if (info.state == NetworkInfo.State.CONNECTED) {
                    return true
                }
            }
            return false
        }
    }
}