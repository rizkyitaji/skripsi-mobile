@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.dashboard.proposal.list

import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.uhamka.bosm.R
import com.uhamka.bosm.home.dashboard.adapter.ProposalAdapter
import com.uhamka.bosm.home.dashboard.proposal.DetailSkripsiActivity
import com.uhamka.bosm.model.Skripsi

class ProposalTMFragment : Fragment() {

    lateinit var ref: DatabaseReference
    private var dataList = ArrayList<Skripsi>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_proposal_tm, container, false)

        val back = view.findViewById<ImageView>(R.id.btn_back)
        back.setOnClickListener {
            val activity = activity as ListProposalActivity
            activity.onBackPressed()
        }

        val none = view.findViewById<TextView>(R.id.tv_none)
        val rv = view.findViewById<RecyclerView>(R.id.rv_proposal)
        rv.layoutManager = LinearLayoutManager(context)

        ref = FirebaseDatabase.getInstance().getReference("Skripsi").child("Teknik Mesin")

        val search = view.findViewById<EditText>(R.id.et_search)
        val iv_search = view.findViewById<ImageView>(R.id.iv_search)

        search.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                iv_search.setImageResource(R.drawable.ic_close)
                iv_search.setOnClickListener {
                    search.text.clear()
                }
            }
        }

        if (isConnected()) {
            ref.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "" + error.message, Toast.LENGTH_LONG).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    dataList.clear()
                    for (snapshot in p0.children) {
                        dataList.add(snapshot.getValue(Skripsi::class.java)!!)
                    }
                    showData(rv)
                }
            })

            search.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    ref.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "" + error.message, Toast.LENGTH_LONG).show()
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            dataList.clear()
                            for (snapshot in p0.children) {
                                val item = snapshot.getValue(Skripsi::class.java)!!
                                val reference = item.judul!!.toLowerCase()
                                if (reference.contains(s.toString().toLowerCase())) {
                                    dataList.add(item)
                                }
                            }
                            showData(rv)
                        }
                    })
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        } else {
            none.visibility = View.VISIBLE
            none.text = getString(R.string.tidak_ada_koneksi_internet)
        }
        return view
    }

    private fun showData(rv: RecyclerView) {
        rv.adapter = ProposalAdapter(dataList) {
            startActivity(Intent(activity, DetailSkripsiActivity::class.java)
                    .putExtra("from", "proposal"))
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