@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.dashboard.dospem.tm

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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.uhamka.bosm.R
import com.uhamka.bosm.home.HomeActivity
import com.uhamka.bosm.home.dashboard.adapter.DosenListAdapter
import com.uhamka.bosm.home.dashboard.dospem.DaftarDospemActivity
import com.uhamka.bosm.model.Pembimbing
import com.uhamka.bosm.utils.Preferences

class DosenTMFragment : Fragment() {

    lateinit var preferences: Preferences
    private var dataList = ArrayList<Pembimbing>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dosen_tm, container, false)

        preferences = Preferences(context!!)

        val back = view.findViewById<ImageView>(R.id.btn_back)
        back.setOnClickListener {
            val activity = activity as DaftarDospemActivity
            activity.onBackPressed()
        }

        val none = view.findViewById<TextView>(R.id.tv_none)
        val rv = view.findViewById<RecyclerView>(R.id.rv_dosen)
        rv.layoutManager = LinearLayoutManager(context)

        val ref = FirebaseDatabase.getInstance().getReference("Pembimbing")
                .child("Teknik Mesin").orderByChild("name")

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
                        dataList.add(snapshot.getValue(Pembimbing::class.java)!!)
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
                                val item = snapshot.getValue(Pembimbing::class.java)!!
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
            none.text = getString(R.string.tidak_ada_koneksi_internet)
        }
        return view
    }

    private fun showData(rv: RecyclerView) {
        rv.adapter = DosenListAdapter(dataList) {
            if (preferences.getValues("user").equals("admin")) {
                moreAction()
            } else {
                startActivity(Intent(activity, HomeActivity::class.java)
                        .putExtra("from", "listDosen"))
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
            editData()
        }

        val view = v.findViewById<LinearLayout>(R.id.view)
        val textView = v.findViewById<TextView>(R.id.tv_view)
        textView.text = getString(R.string.view_profile)
        view.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(activity, HomeActivity::class.java)
                    .putExtra("from", "listDosen"))
        }

        val delete = v.findViewById<LinearLayout>(R.id.delete)
        delete.setOnClickListener {
            if (isConnected()) {
                val name = preferences.getValues("name").toString()
                val build = AlertDialog.Builder(context)
                build.setMessage("Apakah Anda yakin ingin menghapus data dosen pembimbing yang bernama " + name + "?")
                        .setPositiveButton("Ya") { _ , _ ->
                            val nidn = preferences.getValues("id").toString()
                            val ref = FirebaseDatabase.getInstance().getReference("Pembimbing")
                                    .child("Teknik Mesin").child(nidn)
                            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    ref.removeValue()
                                    Toast.makeText(context, "Data telah dihapus", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
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
    }

    private fun editData() {
        val dialog = BottomSheetDialog(context!!, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_add_dosen, null)
        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.show()

        val close = view.findViewById<ImageView>(R.id.iv_close)
        close.setOnClickListener {
            dialog.dismiss()
        }

        val submit = view.findViewById<Button>(R.id.btn_submit)
        val textProdi = view.findViewById<TextView>(R.id.tv_prodi)
        val btnProdi = view.findViewById<ConstraintLayout>(R.id.btn_prodi)

        val editName = view.findViewById<EditText>(R.id.et_name)
        val editNIDN = view.findViewById<EditText>(R.id.et_nidn)
        val editExp = view.findViewById<EditText>(R.id.et_expertise)


        val id = preferences.getValues("id").toString()
        val db = FirebaseDatabase.getInstance().getReference("Pembimbing")
                .child("Teknik Mesin").child(id)
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                textProdi.text = getString(R.string.teknik_mesin)
                editName.setText(snapshot.child("name").value.toString())
                editNIDN.setText(snapshot.child("nidn").value.toString())
                editExp.setText(snapshot.child("expertise").value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })

        btnProdi.setOnClickListener {
            optionProdi(textProdi)
        }

        submit.setOnClickListener {
            val name = editName.text.toString()
            val nidn = editNIDN.text.toString()
            val prodi = textProdi.text.toString()
            val expertise = editExp.text.toString()

            if (prodi.equals("Choose")) {
                Toast.makeText(context, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else if (name.equals("")) {
                editName.error = "Lengkapi isian terlebih dahulu!"
                editName.requestFocus()
            } else if (nidn.equals("")) {
                editNIDN.error = "Lengkapi isian terlebih dahulu!"
                editNIDN.requestFocus()
            } else if (expertise.equals("")) {
                editExp.error = "Lengkapi isian terlebih dahulu!"
                editExp.requestFocus()
            } else {
                if (isConnected()) {
                    if (!prodi.equals("Teknik Mesin")){
                        FirebaseDatabase.getInstance().getReference("Pembimbing")
                                .child("Teknik Mesin").child(nidn).removeValue()
                    }

                    val ref = FirebaseDatabase.getInstance()
                            .getReference("Pembimbing").child(prodi).child(nidn)
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            ref.child("nidn").setValue(nidn)
                            ref.child("name").setValue(name)
                            ref.child("expertise").setValue(expertise)

                            Toast.makeText(context, "Data telah diperbarui", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun optionProdi(mText: TextView) {
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.layout_program_studi, null)
        val ti = view.findViewById<TextView>(R.id.tv_ti)
        val te = view.findViewById<TextView>(R.id.tv_te)
        val tm = view.findViewById<TextView>(R.id.tv_tm)

        builder.setView(view)
        val alert = builder.create()
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alert.show()

        ti.setOnClickListener {
            alert.dismiss()
            val prodi = "Teknik Informatika"
            mText.text = prodi
        }

        te.setOnClickListener {
            alert.dismiss()
            val prodi = "Teknik Elektro"
            mText.text = prodi
        }

        tm.setOnClickListener {
            alert.dismiss()
            val prodi = "Teknik Mesin"
            mText.text = prodi
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