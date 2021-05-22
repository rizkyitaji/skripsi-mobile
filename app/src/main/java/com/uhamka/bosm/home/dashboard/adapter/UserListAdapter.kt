package com.uhamka.bosm.home.dashboard.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uhamka.bosm.R
import com.uhamka.bosm.model.Users
import com.uhamka.bosm.utils.Preferences

class UserListAdapter(private var data: List<Users>,
                      private var listener: (Users) -> Unit)
    : RecyclerView.Adapter<UserListAdapter.LeagueViewHolder>() {

    lateinit var ContextAdapter: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeagueViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        ContextAdapter = parent.context
        val inflatedView = layoutInflater.inflate(R.layout.row_item_user, parent, false)

        return LeagueViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: LeagueViewHolder, position: Int) {
        holder.bindItem(data[position], listener, ContextAdapter)
    }

    override fun getItemCount(): Int = data.size

    class LeagueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var user: String
        val textID = view.findViewById<TextView>(R.id.id)
        val id = view.findViewById<TextView>(R.id.tv_id)
        val name = view.findViewById<TextView>(R.id.tv_name)
        val profile = view.findViewById<ImageView>(R.id.iv_profile)
        val delete = view.findViewById<ImageView>(R.id.iv_delete)

        fun bindItem(data: Users, listener: (Users) -> Unit, context: Context) {
            val preferences = Preferences(context)
            val getID = data.id.toString()
            val uri = data.photo.toString()
            val getName = data.name.toString()
            id.text = getID
            name.text = getName

            if (preferences.getValues("list").equals("mhs")) {
                textID.text = context.getString(R.string.npm)
                user = "Mahasiswa"
            } else {
                textID.text = context.getString(R.string.nidn)
                user = "Dosen"
            }

            if (!uri.equals("")) {
                Glide.with(context)
                        .load(uri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profile)
            }

            profile.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                val layoutInflater = LayoutInflater.from(context)
                val v = layoutInflater.inflate(R.layout.layout_show_photo, null)
                builder.setView(v)
                val dialog = builder.create()
                dialog.show()

                val photo = v.findViewById<ImageButton>(R.id.iv_photo)
                photo.setBackgroundResource(R.color.colorBlueCyan)
                photo.setImageResource(R.drawable.ic_profiles)
                if (!uri.equals("")) {
                    Glide.with(context)
                            .load(uri)
                            .into(photo)
                }
            }

            delete.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Apakah Anda yakin ingin menghapus data pengguna yang bernama " + getName + "?")
                        .setPositiveButton("Ya") { _, _ ->
                            val user = FirebaseDatabase.getInstance()
                                    .getReference("Users").child(user).child(getID)
                            user.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    user.removeValue()
                                    Toast.makeText(context, "Data telah dihapus", Toast.LENGTH_SHORT).show()
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
            }

            itemView.setOnClickListener {
                preferences.setValues("id", data.id.toString())
                listener(data)
            }
        }
    }
}