@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.dashboard.user

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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uhamka.bosm.R
import com.uhamka.bosm.home.dashboard.adapter.UserListAdapter
import com.uhamka.bosm.model.Users
import com.uhamka.bosm.utils.Preferences

class DosenFragment : Fragment() {

    private var dataList = ArrayList<Users>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dosen, container, false)

        val preferences = Preferences(context!!)
        preferences.setValues("list", "dosen")

        val back = view.findViewById<ImageView>(R.id.btn_back)
        back.setOnClickListener {
            val activity = activity as UserListActivity
            activity.onBackPressed()
        }

        val none = view.findViewById<TextView>(R.id.tv_none)
        val rv = view.findViewById<RecyclerView>(R.id.rv_dosen)
        rv.layoutManager = LinearLayoutManager(context)

        val ref = FirebaseDatabase.getInstance().getReference("Users")
                .child("Dosen").orderByChild("name")

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
                        dataList.add(snapshot.getValue(Users::class.java)!!)
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
                                val item = snapshot.getValue(Users::class.java)!!
                                val name = item.name!!.toLowerCase()
                                if (name.contains(s.toString().toLowerCase())) {
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
        rv.adapter = UserListAdapter(dataList) {
            startActivity(Intent(activity, DetailUserActivity::class.java)
                    .putExtra("from", "listDosen"))
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