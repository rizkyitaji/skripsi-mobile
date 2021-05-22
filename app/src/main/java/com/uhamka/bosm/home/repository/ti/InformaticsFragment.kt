@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.repository.ti

import android.app.AlertDialog
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.uhamka.bosm.R
import com.uhamka.bosm.home.dashboard.proposal.DetailSkripsiActivity
import com.uhamka.bosm.home.dashboard.proposal.add.ProposalActivity
import com.uhamka.bosm.home.repository.RepositoryActivity
import com.uhamka.bosm.home.repository.RepositoryAdapter
import com.uhamka.bosm.model.Skripsi
import com.uhamka.bosm.utils.DividerItemDecorator
import com.uhamka.bosm.utils.Preferences

class InformaticsFragment : Fragment() {

    lateinit var preferences: Preferences
    private var dataList = ArrayList<Skripsi>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_informatics, container, false)

        preferences = Preferences(context!!)

        val back = view.findViewById<ImageView>(R.id.btn_back)
        back.setOnClickListener {
            val activity = activity as RepositoryActivity
            activity.onBackPressed()
        }

        val none = view.findViewById<TextView>(R.id.tv_none)
        val rv = view.findViewById<RecyclerView>(R.id.rv_repository)
        rv.layoutManager = LinearLayoutManager(context)
        rv.addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context!!, R.drawable.divider)))

        val ref = FirebaseDatabase.getInstance().getReference("Repository")
                .child("Teknik Informatika").orderByChild("judul")

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
        rv.adapter = RepositoryAdapter(dataList) {
            if (preferences.getValues("user").equals("admin")) {
                moreAction()
            } else {
                startActivity(Intent(activity, DetailSkripsiActivity::class.java)
                        .putExtra("from", "repository"))
            }
        }
    }

    private fun moreAction() {
        val builder = AlertDialog.Builder(context)
        val v = layoutInflater.inflate(R.layout.layout_action, null)
        builder.setView(v)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.show()

        val close = v.findViewById<ImageView>(R.id.iv_close)
        close.setOnClickListener {
            dialog.dismiss()
        }

        val edit = v.findViewById<LinearLayout>(R.id.edit)
        edit.setOnClickListener {
            dialog.dismiss()
            preferences.setValues("action", "edit")
            startActivity(Intent(activity, ProposalActivity::class.java))
        }

        val view = v.findViewById<LinearLayout>(R.id.view)
        val textView = v.findViewById<TextView>(R.id.tv_view)
        val imageView = v.findViewById<ImageView>(R.id.iv_view)
        imageView.setImageResource(R.drawable.ic_assignment_white)
        textView.text = getString(R.string.view_detail)
        view.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(activity, DetailSkripsiActivity::class.java)
                    .putExtra("from", "repository"))
        }

        val delete = v.findViewById<LinearLayout>(R.id.delete)
        delete.setOnClickListener {
            if (isConnected()) {
                val title = preferences.getValues("title").toString()
                val build = AlertDialog.Builder(context)
                build.setMessage("Apakah Anda yakin ingin menghapus skripsi yang berjudul \"" + title + "\" ?")
                        .setPositiveButton("Ya") { _, _ ->
                            val id = preferences.getValues("id").toString()
                            val nim = preferences.getValues("nim").toString()
                            val proposal = FirebaseDatabase.getInstance()
                                    .getReference("Proposal").child(id)
                            proposal.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    proposal.removeValue()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                                }
                            })

                            val skripsi = FirebaseDatabase.getInstance().getReference("Repository")
                                    .child("Teknik Informatika").child(nim)
                            skripsi.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    skripsi.removeValue()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                                }
                            })

                            val db = FirebaseDatabase.getInstance()
                                    .getReference("Status").child(nim).child(id)
                            db.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    db.removeValue()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(context, "" + error.message, Toast.LENGTH_LONG).show()
                                }
                            })

                            Toast.makeText(context, "Data telah dihapus", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
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