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
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.*
import com.uhamka.bosm.R
import com.uhamka.bosm.home.dashboard.adapter.ProposalMHSAdapter
import com.uhamka.bosm.home.dashboard.dospem.DaftarDospemActivity
import com.uhamka.bosm.home.dashboard.proposal.DetailSkripsiActivity
import com.uhamka.bosm.home.dashboard.proposal.add.ProposalActivity
import com.uhamka.bosm.model.Skripsi
import com.uhamka.bosm.utils.Preferences
import java.util.*
import kotlin.collections.ArrayList

class HomeMHSFragment : Fragment() {

    private var dataList = ArrayList<Skripsi>()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home_mhs, container, false)

        val preferences = Preferences(context!!)

        val uri = preferences.getValues("uri").toString()
        val nim = preferences.getValues("idLogin").toString()
        val name = preferences.getValues("nameLogin").toString()

        val none = view.findViewById<TextView>(R.id.tv_none)
        val welcome = view.findViewById<TextView>(R.id.tv_name)
        val profile = view.findViewById<ImageView>(R.id.iv_profile)
        val proposal = view.findViewById<CardView>(R.id.cv_proposal)
        val list = view.findViewById<CardView>(R.id.cv_list_dospem)

        welcome.text = name

        if (isConnected()) {
            if (!uri.equals("null")) {
                Glide.with(context!!)
                        .load(uri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profile)
            }

            val rv = view.findViewById<RecyclerView>(R.id.rv_recent_proposal)
            rv.layoutManager = LinearLayoutManager(context!!)

            val mDatabase = FirebaseDatabase.getInstance().getReference("Proposal")
            mDatabase.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "" + error.message, Toast.LENGTH_LONG).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    dataList.clear()
                    for (snapshot in p0.children) {
                        val item = snapshot.getValue(Skripsi::class.java)!!
                        val reference = item.nim.toString()
                        if (reference.equals(nim)) {
                            dataList.add(item)
                        }
                    }
                    Collections.reverse(dataList)
                    rv.adapter = ProposalMHSAdapter(dataList) {
                        if (preferences.getValues("action").equals("detail")) {
                            startActivity(Intent(activity, DetailSkripsiActivity::class.java)
                                    .putExtra("from", "add"))
                        } else {
                            startActivity(Intent(activity, ProposalActivity::class.java))
                        }
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

        val ref = FirebaseDatabase.getInstance().getReference("Status")
                .child(nim).orderByKey().limitToLast(1)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val status = snapshot.child("status").value.toString()
                    if (!status.equals("null")) {
                        none.visibility = View.GONE
                        if (status.equals("Menunggu Disetujui")) {
                            preferences.setValues("proposal", "wait")
                        } else if (status.equals("Disetujui")) {
                            preferences.setValues("proposal", "ok")
                        } else if (status.equals("LULUS")) {
                            preferences.setValues("proposal", "lulus")
                        } else {
                            preferences.setValues("proposal", "no")
                        }
                    } else {
                        none.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "" + error.message, Toast.LENGTH_LONG).show()
            }
        })

        proposal.setOnClickListener {
            preferences.setValues("action", "add")
            if (preferences.getValues("proposal").equals("wait")) {
                Toast.makeText(context, "Anda sudah mengajukan proposal, silakan tunggu konfirmasi selanjutnya!", Toast.LENGTH_LONG).show()
            } else if (preferences.getValues("proposal").equals("ok")) {
                Toast.makeText(context, "Proposal Anda sudah disetujui, silakan bimbingan sekarang!", Toast.LENGTH_LONG).show()
            } else if (preferences.getValues("proposal").equals("lulus")) {
                Toast.makeText(context, "Selamat! Anda sudah menyelesaikan Tugas Akhir Skripsi dan " +
                        "dinyatakan lulus sebagai Sarjana S1 Teknik Informatika!", Toast.LENGTH_LONG).show()
            } else {
                startActivity(Intent(activity, ProposalActivity::class.java))
            }
        }

        list.setOnClickListener {
            startActivity(Intent(activity, DaftarDospemActivity::class.java))
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
