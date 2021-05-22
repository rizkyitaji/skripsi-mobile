@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.bimbingan

import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uhamka.bosm.R
import com.uhamka.bosm.home.dashboard.adapter.StudentListAdapter
import com.uhamka.bosm.model.Skripsi
import com.uhamka.bosm.utils.Preferences

class BimbinganDosenFragment : Fragment() {

    lateinit var preferences: Preferences
    private var dataList = ArrayList<Skripsi>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bimbingan_dosen, container, false)

        preferences = Preferences(context!!)

        val none = view.findViewById<TextView>(R.id.tv_none)
        val rv = view.findViewById<RecyclerView>(R.id.rv_daftar_mhs)
        rv.layoutManager = LinearLayoutManager(context!!)

        val nidn = preferences.getValues("idLogin").toString()
        val ref = FirebaseDatabase.getInstance().getReference("Proposal").orderByChild("name")
        if (isConnected()) {
            ref.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "" + error.message, Toast.LENGTH_LONG).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    dataList.clear()
                    for (snapshot in p0.children) {
                        val item = snapshot.getValue(Skripsi::class.java)!!
                        val reference = item.nidn!!
                        val status = item.status!!
                        if (reference.contains(nidn) && status.equals("Disetujui")) {
                            none.visibility = View.GONE
                            dataList.add(item)
                        } else {
                            none.visibility = View.VISIBLE
                        }
                    }
                    rv.adapter = StudentListAdapter(dataList) {
                        startActivity(Intent(activity, DetailBimbinganActivity::class.java))
                    }
                }
            })
        } else {
            none.visibility = View.VISIBLE
            none.text = getString(R.string.tidak_ada_koneksi_internet)
        }

        return view
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
