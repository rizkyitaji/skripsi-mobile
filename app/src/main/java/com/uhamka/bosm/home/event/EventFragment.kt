@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.event

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.uhamka.bosm.R
import com.uhamka.bosm.model.Event
import com.uhamka.bosm.utils.DividerItemDecorator
import com.uhamka.bosm.utils.Preferences
import dmax.dialog.SpotsDialog
import java.util.*
import kotlin.collections.ArrayList

class EventFragment : Fragment() {

    lateinit var pickDay: String
    lateinit var pickMonth: String
    lateinit var pickYear: String
    lateinit var filePath: Uri
    lateinit var editUpload: TextView
    lateinit var ref: DatabaseReference
    lateinit var preferences: Preferences
    private var dataList = ArrayList<Event>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_event, container, false)

        preferences = Preferences(context!!)

        val none = view.findViewById<TextView>(R.id.tv_none)
        val rv = view.findViewById<RecyclerView>(R.id.rv_info)
        rv.addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context!!, R.drawable.divider)))

        ref = FirebaseDatabase.getInstance().getReference("Informasi")
        if (isConnected()) {
            ref.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    dataList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val item = dataSnapshot.getValue(Event::class.java)
                        if (item != null) {
                            none.visibility = View.GONE
                            dataList.add(item)
                        } else {
                            none.visibility = View.VISIBLE
                        }
                    }
                    Collections.reverse(dataList)
                    rv.adapter = EventAdapter(dataList) {
                        if (preferences.getValues("pdf").equals("")) {
                            startActivity(Intent(activity, DetailEventActivity::class.java)
                                    .putExtra("event", "detail"))
                        } else {
                            startActivity(Intent(activity, DetailEventActivity::class.java)
                                    .putExtra("event", "download"))
                        }
                    }
                }
            })
        } else {
            none.text = getString(R.string.tidak_ada_koneksi_internet)
        }

        val fab = view.findViewById<FloatingActionMenu>(R.id.btn_fab)
        if (preferences.getValues("user").equals("admin")) {
            fab.visibility = View.VISIBLE
        } else {
            fab.visibility = View.GONE
        }

        val add = view.findViewById<FloatingActionButton>(R.id.fab_add)
        add.setOnClickListener {
            newInfo("add")
        }

        val pdf = view.findViewById<FloatingActionButton>(R.id.fab_pdf)
        pdf.setOnClickListener {
            newInfo("pdf")
        }

        return view
    }

    private fun newInfo(mText: String){
        val builder = AlertDialog.Builder(context)
        val v = layoutInflater.inflate(R.layout.layout_add_info, null)
        builder.setView(v)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.show()

        editUpload = v.findViewById(R.id.tv_upload)
        val submit = v.findViewById<Button>(R.id.btn_submit)
        val editDate = v.findViewById<TextView>(R.id.tv_date)
        val close = v.findViewById<ImageView>(R.id.iv_close)
        val editTitle = v.findViewById<EditText>(R.id.et_title)
        val spaceUpload = v.findViewById<TextView>(R.id.space3)
        val spaceDetail = v.findViewById<TextView>(R.id.space4)
        val textDetail = v.findViewById<TextView>(R.id.textIsi)
        val editDetail = v.findViewById<EditText>(R.id.et_detail)
        val textUpload = v.findViewById<TextView>(R.id.textUpload)

        close.setOnClickListener {
            dialog.dismiss()
        }

        if (mText.equals("add")){
            textUpload.visibility = View.GONE
            spaceUpload.visibility = View.GONE
            editUpload.visibility = View.GONE
        } else {
            textDetail.visibility = View.GONE
            spaceDetail.visibility = View.GONE
            editDetail.visibility = View.GONE
        }

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        editDate.setOnClickListener {
            val pickDate = DatePickerDialog(activity!!,
                    DatePickerDialog.OnDateSetListener { _, mYear, mMonth, mDay ->
                        if (mDay.equals(1) || mDay.equals(2) || mDay.equals(3) || mDay.equals(4) || mDay.equals(5)
                                || mDay.equals(6) || mDay.equals(7) || mDay.equals(8) || mDay.equals(9)) {
                            pickDay = "0" + mDay
                        } else {
                            pickDay = mDay.toString()
                        }

                        if (mMonth.equals(1) || mMonth.equals(2) || mMonth.equals(3) || mMonth.equals(4) || mMonth.equals(5)
                                || mMonth.equals(6) || mMonth.equals(7) || mMonth.equals(8)) {
                            pickMonth = "0" + (mMonth + 1).toString()
                        } else {
                            pickMonth = (mMonth + 1).toString()
                        }

                        pickYear = mYear.toString()
                        val textDate = pickDay + "-" + pickMonth + "-" + pickYear
                        editDate.text = textDate
                    }, year, month, day)
            pickDate.show()
        }

        preferences.setValues("upload", "0")
        editUpload.setOnClickListener {
            if (editDate.text.toString().equals("Select Date")){
                Toast.makeText(context, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else {
                if (preferences.getValues("upload").equals("0")) {
                    val intent = Intent().apply {
                        type = "application/pdf"
                        action = Intent.ACTION_GET_CONTENT
                    }
                    startActivityForResult(Intent.createChooser(intent, "Select Document"), 1)
                } else {
                    preferences.setValues("upload", "1")
                    editUpload.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_upload, 0)
                    editUpload.text = getString(R.string.upload_file)
                }
            }
        }

        submit.setOnClickListener {
            val date = editDate.text.toString()
            val title = editTitle.text.toString()
            val detail = editDetail.text.toString()
            val upload = editUpload.text.toString()

            if (date.equals("Select Date")) {
                Toast.makeText(context, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else if (title.equals("")) {
                editTitle.error = "Lengkapi isian terlebih dahulu!"
                editTitle.requestFocus()
            } else {
                if (mText.equals("add")) {
                    if (detail.equals("")) {
                        editDetail.error = "Lengkapi isian terlebih dahulu!"
                        editDetail.requestFocus()
                    } else {
                        if (isConnected()) {
                            saveData(date, title, detail, "add")
                        } else {
                            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    if (upload.equals("Upload File")){
                        Toast.makeText(context, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
                    } else {
                        if (isConnected()) {
                            saveData(date, title, detail, "pdf")
                            dialog.dismiss()
                        } else {
                            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == 1){
                filePath = data?.data!!
                preferences.setValues("upload", "1")
                val success = "File_" + pickDay + pickMonth + pickYear + ".pdf"
                editUpload.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_delete_dark, 0)
                editUpload.text = success
            }
        } else {
            Toast.makeText(context, "Dibatalkan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveData(date: String, title: String, detail: String, mText: String){
        val id = ref.push().key.toString()
        if (mText.equals("add")) {
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    ref.child(id).child("id").setValue(id)
                    ref.child(id).child("date").setValue(date)
                    ref.child(id).child("title").setValue(title)
                    ref.child(id).child("detail").setValue(detail)

                    Toast.makeText(context, "Pengumuman telah ditampilkan", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            val dialog = SpotsDialog.Builder()
                    .setContext(context)
                    .setCancelable(false)
                    .build()
            dialog.show()

            val pdfRef = FirebaseStorage.getInstance().getReference("pdf/" + title)
            pdfRef.putFile(filePath)
                    .addOnProgressListener { taskSnapshot ->
                        val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                        dialog.setMessage("Uploaded " + progress.toInt() + "%")
                    }

                    .addOnFailureListener { e ->
                        dialog.dismiss()
                        Toast.makeText(context, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                    }

                    .addOnSuccessListener {
                        pdfRef.downloadUrl.addOnSuccessListener {
                            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                                }

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    ref.child(id).child("id").setValue(id)
                                    ref.child(id).child("date").setValue(date)
                                    ref.child(id).child("title").setValue(title)
                                    ref.child(id).child("pdf").setValue(it.toString())
                                    dialog.dismiss()
                                    Toast.makeText(context, "Pengumuman telah ditampilkan", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
        }
    }

    private fun isConnected(): Boolean {
        val connectivity = context!!.getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivity.activeNetworkInfo
        if (info != null) {
            if (info.state == NetworkInfo.State.CONNECTED) {
                return true
            }
        }
        return false
    }
}
