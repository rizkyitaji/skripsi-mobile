@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.bimbingan

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.uhamka.bosm.R
import com.uhamka.bosm.home.HomeActivity
import com.uhamka.bosm.home.profile.ProfileDosenFragment
import com.uhamka.bosm.model.Bimbingan
import com.uhamka.bosm.utils.DividerItemDecorator
import com.uhamka.bosm.utils.Preferences
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_home.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BimbinganMHSFragment : Fragment() {

    lateinit var nim: String
    lateinit var nidn: String
    lateinit var dosen: String
    lateinit var photo: String
    lateinit var pickDay: String
    lateinit var pickMonth: String
    lateinit var pickYear: String
    lateinit var setMonth: String
    lateinit var tv_upload: TextView
    lateinit var filePath: Uri
    lateinit var ref: DatabaseReference
    lateinit var preferences: Preferences
    private var dataList = ArrayList<Bimbingan>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bimbingan_mhs, container, false)

        preferences = Preferences(context!!)

        val tv_dosen = view.findViewById<TextView>(R.id.tv_dosen)
        val tv_title = view.findViewById<TextView>(R.id.tv_title)
        val view_profile = view.findViewById<TextView>(R.id.btn_view)
        val bimbingan = view.findViewById<TextView>(R.id.btn_bimbingan)

        nim = preferences.getValues("idLogin").toString()
        val prodi = preferences.getValues("prodiLogin").toString()

        val repository = FirebaseDatabase.getInstance()
                .getReference("Repository").child(prodi).child(nim)
        repository.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                nidn = snapshot.child("nidn").value.toString()
                dosen = snapshot.child("pembimbing").value.toString()
                val title = snapshot.child("judul").value.toString()

                if (!title.equals("null")) {
                    preferences.setValues("view", "1")
                    tv_dosen.text = dosen
                    tv_title.text = title
                } else {
                    preferences.setValues("view", "0")
                    tv_dosen.text = ""
                    tv_title.text = ""
                }

                val user = FirebaseDatabase.getInstance()
                        .getReference("Users").child("Dosen").child(nidn)
                user.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        photo = snapshot.child("photo").value.toString()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
            }
        })

        view_profile.setOnClickListener {
            if (preferences.getValues("view").equals("0")) {
                Toast.makeText(context, "Proposal Anda belum disetujui!", Toast.LENGTH_SHORT).show()
            } else {
                if (isConnected()) {
                    preferences.setValues("id", nidn)
                    preferences.setValues("name", dosen)
                    preferences.setValues("photo", photo)
                    preferences.setValues("action", "view")
                    val manager = fragmentManager!!
                    manager.beginTransaction()
                            .replace(R.id.layout_frame, ProfileDosenFragment())
                            .commit()
                    val home = activity as HomeActivity
                    home.iv_assignment.setImageResource(R.drawable.ic_assignment)
                    home.iv_profile.setImageResource(R.drawable.ic_person_visit)
                } else {
                    Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
                }
            }
        }

        bimbingan.setOnClickListener {
            if (preferences.getValues("view").equals("0")) {
                Toast.makeText(context, "Proposal Anda belum disetujui!", Toast.LENGTH_SHORT).show()
            } else {
                showDialog()
            }
        }

        val none = view.findViewById<TextView>(R.id.tv_none)
        val rv = view.findViewById<RecyclerView>(R.id.rv_bimbingan)
        rv.addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context!!, R.drawable.divider)))

        ref = FirebaseDatabase.getInstance().getReference("Bimbingan").child(nim)

        if (isConnected()) {
            ref.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    dataList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val item = dataSnapshot.getValue(Bimbingan::class.java)
                        if (item != null) {
                            none.visibility = View.GONE
                            dataList.add(item)
                        } else {
                            none.visibility = View.VISIBLE
                        }
                    }
                    rv.adapter = BimbinganAdapter(dataList) {
                        if (preferences.getValues("action").equals("note")) {
                            if (preferences.getValues("laporan").equals("")) {
                                startActivity(Intent(activity, CatatanBimbinganActivity::class.java)
                                        .putExtra("bimbingan", "offline"))
                            } else {
                                startActivity(Intent(activity, CatatanBimbinganActivity::class.java)
                                        .putExtra("bimbingan", "online"))
                            }
                        }

                    }
                }
            })
        } else {
            none.text = getString(R.string.tidak_ada_koneksi_internet)
        }

        val lastChild = ref.orderByKey().limitToLast(1)

        lastChild.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val day = dataSnapshot.child("day").value.toString()
                    val month = dataSnapshot.child("month").value.toString()
                    if (!day.equals("null")) {
                        compareDate(day, month)
                    }
                }
            }
        })

        return view
    }

    private fun showDialog() {
        val dialog = BottomSheetDialog(activity!!, R.style.BottomSheetDialogTheme)
        val v = layoutInflater.inflate(R.layout.layout_add_bimbingan, null)
        dialog.setContentView(v)
        dialog.show()

        val close = v.findViewById<ImageView>(R.id.iv_close)
        dialog.setCancelable(false)
        close.setOnClickListener {
            dialog.dismiss()
        }

        tv_upload = v.findViewById(R.id.tv_upload)
        val spaceRev = v.findViewById<TextView>(R.id.space4)
        val spaceRep = v.findViewById<TextView>(R.id.space5)
        val tv_date = v.findViewById<TextView>(R.id.tv_date)
        val et_detail = v.findViewById<EditText>(R.id.et_detail)
        val et_revisi = v.findViewById<EditText>(R.id.et_revisi)
        val tv_revisi = v.findViewById<TextView>(R.id.tv_revisi)
        val tv_laporan = v.findViewById<TextView>(R.id.tv_laporan)
        val online = v.findViewById<ImageView>(R.id.checkbox_two)
        val offline = v.findViewById<ImageView>(R.id.checkbox_one)
        val submit = v.findViewById<Button>(R.id.btn_submit)

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        tv_date.setOnClickListener {
            val pickDate = DatePickerDialog(context!!,
                    DatePickerDialog.OnDateSetListener { _, mYear, mMonth, mDay ->
                        if (mDay.equals(1) || mDay.equals(2) || mDay.equals(3) || mDay.equals(4) || mDay.equals(5)
                                || mDay.equals(6) || mDay.equals(7) || mDay.equals(8) || mDay.equals(9)) {
                            pickDay = "0" + mDay
                        } else {
                            pickDay = mDay.toString()
                        }

                        if (mMonth.equals(1) || mMonth.equals(2) || mMonth.equals(3) || mMonth.equals(4) || mMonth.equals(5)
                                || mMonth.equals(6) || mMonth.equals(7) || mMonth.equals(8)) {
                            pickMonth = "0" + (mMonth + 1)
                        } else {
                            pickMonth = (mMonth + 1).toString()
                        }

                        pickYear = mYear.toString()
                        val textDate = pickDay + "-" + pickMonth + "-" + pickYear
                        tv_date.text = textDate
                    }, year, month, day)
            pickDate.show()
        }

        preferences.setValues("offline", "0")
        offline.setOnClickListener {
            if (preferences.getValues("offline").equals("0")) {
                preferences.setValues("offline", "1")
                preferences.setValues("online", "0")
                preferences.setValues("data", "revisi")
                offline.setImageResource(R.drawable.ic_check_box)
                online.setImageResource(R.drawable.ic_check_box_outline)
                tv_revisi.visibility = View.VISIBLE
                spaceRev.visibility = View.VISIBLE
                et_revisi.visibility = View.VISIBLE
                tv_laporan.visibility = View.GONE
                spaceRep.visibility = View.GONE
                tv_upload.visibility = View.GONE
            } else {
                preferences.setValues("offline", "0")
                preferences.setValues("data", "0")
                offline.setImageResource(R.drawable.ic_check_box_outline)
                tv_revisi.visibility = View.GONE
                spaceRev.visibility = View.GONE
                et_revisi.visibility = View.GONE
            }
        }

        preferences.setValues("online", "0")
        online.setOnClickListener {
            if (preferences.getValues("online").equals("0")) {
                preferences.setValues("online", "1")
                preferences.setValues("offline", "0")
                preferences.setValues("data", "laporan")
                online.setImageResource(R.drawable.ic_check_box)
                offline.setImageResource(R.drawable.ic_check_box_outline)
                tv_revisi.visibility = View.GONE
                spaceRev.visibility = View.GONE
                et_revisi.visibility = View.GONE
                tv_laporan.visibility = View.VISIBLE
                spaceRep.visibility = View.VISIBLE
                tv_upload.visibility = View.VISIBLE
            } else {
                preferences.setValues("online", "0")
                preferences.setValues("data", "0")
                online.setImageResource(R.drawable.ic_check_box_outline)
                tv_laporan.visibility = View.GONE
                spaceRep.visibility = View.GONE
                tv_upload.visibility = View.GONE
            }
        }

        preferences.setValues("upload", "0")
        tv_upload.setOnClickListener {
            if (preferences.getValues("upload").equals("0")) {
                val intent = Intent().apply {
                    type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    action = Intent.ACTION_GET_CONTENT
                }
                startActivityForResult(Intent.createChooser(intent, "Select Document"), 1)
            } else {
                preferences.setValues("upload", "0")
                tv_upload.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_upload, 0)
                tv_upload.text = getString(R.string.upload_file)
            }
        }

        submit.setOnClickListener {
            val date = tv_date.text.toString()
            val detail = et_detail.text.toString()
            val revisi = et_revisi.text.toString()
            val upload = tv_upload.text.toString()

            if (date.equals("Select Date")) {
                Toast.makeText(context, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else if (detail.equals("")) {
                et_detail.error = "Lengkapi isian terlebih dahulu!"
                et_detail.requestFocus()
            } else if (preferences.getValues("offline").equals("0")
                    && preferences.getValues("online").equals("0")) {
                Toast.makeText(context, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else if (preferences.getValues("offline").equals("1")){
                if (revisi.equals("")){
                    et_revisi.error = "Lengkapi isian terlebih dahulu!"
                    et_revisi.requestFocus()
                } else {
                    if (isConnected()) {
                        saveData(date, detail, revisi)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                if (upload.equals("Upload File")){
                    Toast.makeText(context, "Lengkapi isian terlebih dahulu!", Toast.LENGTH_SHORT).show()
                } else {
                    if (isConnected()) {
                        saveData(date, detail, revisi)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                filePath = data?.data!!
                preferences.setValues("upload", "1")
                val success = "Laporan_" + nim + ".docx"
                tv_upload.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_delete_dark, 0)
                tv_upload.text = success
            }
        } else {
            Toast.makeText(context, "Dibatalkan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveData(date: String, detail: String, revisi: String) {
        if (preferences.getValues("data").equals("revisi")){
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    ref.child(date).child("nim").setValue(nim)
                    ref.child(date).child("date").setValue(date)
                    ref.child(date).child("day").setValue(pickDay)
                    ref.child(date).child("month").setValue(pickMonth)
                    ref.child(date).child("year").setValue(pickYear)
                    ref.child(date).child("title").setValue(detail)
                    ref.child(date).child("revisi").setValue(revisi)

                    Toast.makeText(context, "Anda telah melakukan bimbingan pada tanggal " + date, Toast.LENGTH_LONG).show()
                }
            })
        } else if (preferences.getValues("data").equals("laporan")){
            val dialog = SpotsDialog.Builder()
                    .setContext(context)
                    .setCancelable(false)
                    .build()
            dialog.show()

            val docsRef = FirebaseStorage.getInstance()
                    .getReference("docs/Laporan_Skripsi_" + pickDay + pickMonth + pickYear + "_" + nim)
            docsRef.putFile(filePath)
                    .addOnProgressListener { taskSnapshot ->
                        val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                        dialog.setMessage("Loading " + progress.toInt() + "%")
                    }

                    .addOnFailureListener { e ->
                        dialog.dismiss()
                        Toast.makeText(context, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                    }

                    .addOnSuccessListener {
                        docsRef.downloadUrl.addOnSuccessListener {
                            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                                }

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    ref.child(date).child("nim").setValue(nim)
                                    ref.child(date).child("date").setValue(date)
                                    ref.child(date).child("day").setValue(pickDay)
                                    ref.child(date).child("month").setValue(pickMonth)
                                    ref.child(date).child("year").setValue(pickYear)
                                    ref.child(date).child("title").setValue(detail)
                                    ref.child(date).child("laporan").setValue(it.toString())

                                    Toast.makeText(context, "Anda telah melakukan bimbingan pada tanggal " + date, Toast.LENGTH_LONG).show()
                                    dialog.dismiss()
                                }
                            })
                        }
                    }
        }
    }

    private fun compareDate(day: String, month: String) {
        val currentDay = SimpleDateFormat("dd", Locale.ENGLISH).format(Date())
        val currentMonth = SimpleDateFormat("MMM", Locale.ENGLISH).format(Date())

        if (currentMonth.equals("Jan")) {
            setMonth = "01"
        } else if (currentMonth.equals("Feb")) {
            setMonth = "02"
        } else if (currentMonth.equals("Mar")) {
            setMonth = "03"
        } else if (currentMonth.equals("Apr")) {
            setMonth = "04"
        } else if (currentMonth.equals("May")) {
            setMonth = "05"
        } else if (currentMonth.equals("Jun")) {
            setMonth = "06"
        } else if (currentMonth.equals("Jul")) {
            setMonth = "07"
        } else if (currentMonth.equals("Aug")) {
            setMonth = "08"
        } else if (currentMonth.equals("Sep")) {
            setMonth = "09"
        } else if (currentMonth.equals("Oct")) {
            setMonth = "10"
        } else if (currentMonth.equals("Nov")) {
            setMonth = "11"
        } else if (currentMonth.equals("Dec")) {
            setMonth = "12"
        }

        val currentMonthInt = setMonth.toInt()
        val currentDayInt = currentDay.toInt()
        val lastMonthInt = month.toInt()
        val lastDayInt = day.toInt()

        if (lastDayInt.equals(currentDayInt) && lastMonthInt < currentMonthInt) {
            val text = "Anda belum melakukan bimbingan selama sebulan!"
            sendNotification(text)
        } else if (lastMonthInt < currentMonthInt) {
            val text = "Anda belum melakukan bimbingan di bulan ini!"
            sendNotification(text)
        }
    }

    private fun sendNotification(text: String) {
        val id = "notif_bimbingan"
        val name = "KotlinApp"
        val description = "Notif_BOSM_APP"
        val NOTIFICATION_ID = 1
        val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance)
            channel.description = description
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(activity, HomeActivity::class.java).apply {
            putExtra("from", "notif")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0)

        val builder = NotificationCompat.Builder(context!!, id)
                .setSmallIcon(R.drawable.ic_school)
                .setContentTitle("AYO BIMBINGAN SEKARANG!")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
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


