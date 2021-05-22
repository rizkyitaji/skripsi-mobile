@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.dashboard

import android.app.AlertDialog
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.*
import com.uhamka.bosm.R
import com.uhamka.bosm.home.dashboard.adapter.ProposalDosenAdapter
import com.uhamka.bosm.home.dashboard.proposal.DetailSkripsiActivity
import com.uhamka.bosm.model.Skripsi
import com.uhamka.bosm.utils.Preferences
import java.util.*
import kotlin.collections.ArrayList

class HomeDosenFragment : Fragment() {

    lateinit var ref: DatabaseReference
    lateinit var preferences: Preferences
    private var dataList = ArrayList<Skripsi>()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home_dosen, container, false)

        preferences = Preferences(context!!)

        val uri = preferences.getValues("uri").toString()
        val nidn = preferences.getValues("idLogin").toString()
        val name = preferences.getValues("nameLogin").toString()

        val none = view.findViewById<TextView>(R.id.tv_none)
        val profile = view.findViewById<ImageView>(R.id.iv_profile)
        val welcome = view.findViewById<TextView>(R.id.tv_name)
        welcome.text = name

        if (isConnected()) {
            if (!uri.equals("null")) {
                Glide.with(context!!)
                        .load(uri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profile)
            }

            val rv = view.findViewById<RecyclerView>(R.id.rv_new_proposal)
            rv.layoutManager = LinearLayoutManager(context!!.applicationContext)

            val ref = FirebaseDatabase.getInstance().getReference("Proposal")
                    .orderByChild("nidn_fk").equalTo(nidn)
            ref.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "" + error.message, Toast.LENGTH_LONG).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    dataList.clear()
                    for (snapshot in p0.children) {
                        val item = snapshot.getValue(Skripsi::class.java)!!
                        val reference = item.nidn_fk!!
                        if (!reference.equals("null")) {
                            none.visibility = View.GONE
                            dataList.add(item)
                        } else {
                            none.visibility = View.VISIBLE
                        }
                    }
                    Collections.reverse(dataList)
                    rv.adapter = ProposalDosenAdapter(dataList) {
                        startActivity(Intent(activity, DetailSkripsiActivity::class.java)
                                .putExtra("from", "home"))
                    }
                }
            })
        } else {
            none.visibility = View.VISIBLE
            none.text = getString(R.string.tidak_ada_koneksi_internet)
        }

        profile.setOnClickListener {
            showPhoto(uri)
        }

        return view
    }

    private fun showPhoto(uri: String) {
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.layout_show_photo, null)
        builder.setView(view)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val profile = view.findViewById<ImageView>(R.id.iv_photo)
        if (isConnected()) {
            if (!uri.equals("null")) {
                Glide.with(context!!)
                        .load(uri)
                        .into(profile)
            }
        } else {
            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isConnected(): Boolean {
        val connectivity = activity!!.getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivity.activeNetworkInfo
        if (info != null) {
            if (info.state == NetworkInfo.State.CONNECTED) {
                return true
            }
        }
        return false
    }
}
