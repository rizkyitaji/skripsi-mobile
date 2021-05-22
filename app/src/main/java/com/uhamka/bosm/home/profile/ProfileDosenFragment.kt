@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.profile

import android.Manifest
import android.app.AlertDialog
import android.app.Service
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.clans.fab.FloatingActionButton
import com.google.firebase.database.*
import com.uhamka.bosm.R
import com.uhamka.bosm.setting.SettingActivity
import com.uhamka.bosm.utils.Preferences
import java.util.*

class ProfileDosenFragment : Fragment() {

    lateinit var uri: String
    lateinit var name: String
    lateinit var nidn: String
    lateinit var getWA: String
    lateinit var getPhone: String
    lateinit var getEmail: String
    lateinit var setMonday: String
    lateinit var setTuesday: String
    lateinit var setWednesday: String
    lateinit var setThursday: String
    lateinit var setFriday: String
    lateinit var setSaturday: String
    lateinit var setSunday: String
    lateinit var ref: DatabaseReference
    lateinit var preferences: Preferences
    val REQUEST_CALL_PHONE = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_dosen, container, false)

        preferences = Preferences(context!!)

        /*set profile*/
        val profile = view.findViewById<ImageView>(R.id.iv_profile)
        val setting = view.findViewById<ImageView>(R.id.iv_setting)
        val tv_name = view.findViewById<TextView>(R.id.tv_name)
        val tv_nidn = view.findViewById<TextView>(R.id.tv_nidn)
        val fab = view.findViewById<FloatingActionButton>(R.id.btn_fab)

        if (preferences.getValues("action").equals("profile")) {
            uri = preferences.getValues("uri").toString()
            name = preferences.getValues("nameLogin").toString()
            nidn = preferences.getValues("idLogin").toString()
            fab.visibility = View.VISIBLE
        } else if (preferences.getValues("action").equals("view")) {
            uri = preferences.getValues("photo").toString()
            name = preferences.getValues("name").toString()
            nidn = preferences.getValues("id").toString()
            fab.visibility = View.INVISIBLE
            setting.visibility = View.INVISIBLE
        }

        /*goto setting*/
        setting.setOnClickListener {
            startActivity(Intent(activity, SettingActivity::class.java))
        }

        profile.setOnClickListener {
            showPhoto(uri)
        }

        /*set contact*/
        val phone = view.findViewById<ImageView>(R.id.iv_phone)
        val email = view.findViewById<ImageView>(R.id.iv_email)
        val whatsapp = view.findViewById<ImageView>(R.id.iv_whatsapp)

        val userDB = FirebaseDatabase.getInstance()
                .getReference("Users").child("Dosen").child(nidn)
        userDB.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                getPhone = snapshot.child("phone").getValue().toString()
                getWA = snapshot.child("whatsapp").getValue().toString()
                getEmail = snapshot.child("email").getValue().toString()
            }
        })

        if (preferences.getValues("action").equals("profile")) {
            phone.setOnClickListener {
                if (isConnected()) {
                    if (!getPhone.equals("null")) {
                        Toast.makeText(context, "Phone : " + getPhone, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Silakan tambahkan nomor telepon Anda terlebih dahulu!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
                }
            }

            whatsapp.setOnClickListener {
                if (isConnected()) {
                    if (!getWA.equals("null")) {
                        Toast.makeText(context, "Whatsapp : 0" + getWA, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Silakan tambahkan nomor whatsapp Anda terlebih dahulu!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
                }
            }

            email.setOnClickListener {
                if (isConnected()) {
                    if (!getEmail.equals("null")) {
                        Toast.makeText(context, "Email : " + getEmail, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Silakan tambahkan alamat email Anda terlebih dahulu!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (preferences.getValues("action").equals("view")) {
            phone.setOnClickListener {
                phoneCall()
            }

            whatsapp.setOnClickListener {
                if (!getWA.equals("null")) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/=62" + getWA)))
                } else {
                    Toast.makeText(context, "Nomor whatsapp tidak tersedia", Toast.LENGTH_LONG).show()
                }
            }

            email.setOnClickListener {
                if (!getEmail.equals("null")) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + getEmail)))
                } else {
                    Toast.makeText(context, "Alamat email tidak tersedia", Toast.LENGTH_LONG).show()
                }
            }
        }

        /*set content*/
        fab.setOnClickListener {
            addSchedule()
        }

        val monday = view.findViewById<TextView>(R.id.tv_mon)
        val tuesday = view.findViewById<TextView>(R.id.tv_tue)
        val wednesday = view.findViewById<TextView>(R.id.tv_wed)
        val thursday = view.findViewById<TextView>(R.id.tv_thu)
        val friday = view.findViewById<TextView>(R.id.tv_fri)
        val saturday = view.findViewById<TextView>(R.id.tv_sat)
        val sunday = view.findViewById<TextView>(R.id.tv_sun)

        if (isConnected()) {
            if (!uri.equals("null")) {
                Glide.with(context!!)
                        .load(uri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profile)
            }

            tv_name.text = name
            tv_nidn.text = nidn

            ref = FirebaseDatabase.getInstance().getReference("Jadwal Bimbingan").child(nidn)
            ref.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "" + error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val getMon = snapshot.child("monday").value.toString()
                    val getTue = snapshot.child("tuesday").value.toString()
                    val getWed = snapshot.child("wednesday").value.toString()
                    val getThu = snapshot.child("thursday").value.toString()
                    val getFri = snapshot.child("friday").value.toString()
                    val getSat = snapshot.child("saturday").value.toString()
                    val getSun = snapshot.child("sunday").value.toString()

                    if (getMon.equals("null")){
                        monday.text = ""
                        tuesday.text = ""
                        wednesday.text = ""
                        thursday.text = ""
                        friday.text = ""
                        saturday.text = ""
                        sunday.text = ""
                    } else {
                        monday.text = getMon
                        tuesday.text = getTue
                        wednesday.text = getWed
                        thursday.text = getThu
                        friday.text = getFri
                        saturday.text = getSat
                        sunday.text = getSun
                    }
                }
            })
        } else {
            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun addSchedule() {
        val builder = AlertDialog.Builder(context!!)
        val view = layoutInflater.inflate(R.layout.layout_add_schedule, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        dialog.setCancelable(false)
        val close = view.findViewById<ImageView>(R.id.iv_close)
        close.setOnClickListener {
            dialog.dismiss()
        }

        val resetMon = view.findViewById<TextView>(R.id.resetMon)
        val resetTue = view.findViewById<TextView>(R.id.resetTue)
        val resetWed = view.findViewById<TextView>(R.id.resetWed)
        val resetThu = view.findViewById<TextView>(R.id.resetThu)
        val resetFri = view.findViewById<TextView>(R.id.resetFri)
        val resetSat = view.findViewById<TextView>(R.id.resetSat)
        val resetSun = view.findViewById<TextView>(R.id.resetSun)
        val startMon = view.findViewById<TextView>(R.id.startMon)
        val startTue = view.findViewById<TextView>(R.id.startTue)
        val startWed = view.findViewById<TextView>(R.id.startWed)
        val startThu = view.findViewById<TextView>(R.id.startThu)
        val startFri = view.findViewById<TextView>(R.id.startFri)
        val startSat = view.findViewById<TextView>(R.id.startSat)
        val startSun = view.findViewById<TextView>(R.id.startSun)
        val endMon = view.findViewById<TextView>(R.id.endMon)
        val endTue = view.findViewById<TextView>(R.id.endTue)
        val endWed = view.findViewById<TextView>(R.id.endWed)
        val endThu = view.findViewById<TextView>(R.id.endThu)
        val endFri = view.findViewById<TextView>(R.id.endFri)
        val endSat = view.findViewById<TextView>(R.id.endSat)
        val endSun = view.findViewById<TextView>(R.id.endSun)
        val submit = view.findViewById<Button>(R.id.btn_submit)

        startMon.setOnClickListener { timePicker(startMon) }
        startTue.setOnClickListener { timePicker(startTue) }
        startWed.setOnClickListener { timePicker(startWed) }
        startThu.setOnClickListener { timePicker(startThu) }
        startFri.setOnClickListener { timePicker(startFri) }
        startSat.setOnClickListener { timePicker(startSat) }
        startSun.setOnClickListener { timePicker(startSun) }
        endMon.setOnClickListener { timePicker(endMon) }
        endTue.setOnClickListener { timePicker(endTue) }
        endWed.setOnClickListener { timePicker(endWed) }
        endThu.setOnClickListener { timePicker(endThu) }
        endFri.setOnClickListener { timePicker(endFri) }
        endSat.setOnClickListener { timePicker(endSat) }
        endSun.setOnClickListener { timePicker(endSun) }

        resetMon.setOnClickListener {
            startMon.text = "-"
            endMon.text = "-"
        }

        resetTue.setOnClickListener {
            startTue.text = "-"
            endTue.text = "-"
        }

        resetWed.setOnClickListener {
            startWed.text = "-"
            endWed.text = "-"
        }

        resetThu.setOnClickListener {
            startThu.text = "-"
            endThu.text = "-"
        }

        resetFri.setOnClickListener {
            startFri.text = "-"
            endFri.text = "-"
        }

        resetSat.setOnClickListener {
            startSat.text = "-"
            endSat.text = "-"
        }

        resetSun.setOnClickListener {
            startSun.text = "-"
            endSun.text = "-"
        }

        submit.setOnClickListener {
            val getStartMon = startMon.text.toString()
            val getStartTue = startTue.text.toString()
            val getStartWed = startWed.text.toString()
            val getStartThu = startThu.text.toString()
            val getStartFri = startFri.text.toString()
            val getStartSat = startSat.text.toString()
            val getStartSun = startSun.text.toString()
            val getEndMon = endMon.text.toString()
            val getEndTue = endTue.text.toString()
            val getEndWed = endWed.text.toString()
            val getEndThu = endThu.text.toString()
            val getEndFri = endFri.text.toString()
            val getEndSat = endSat.text.toString()
            val getEndSun = endSun.text.toString()

            if (getStartMon.equals("-") && !getEndMon.equals("-") || !getStartMon.equals("-") && getEndMon.equals("-")) {
                Toast.makeText(context, "Silakan lengkapi jadwal hari Senin!", Toast.LENGTH_LONG).show()
            } else if (getStartTue.equals("-") && !getEndTue.equals("-") || !getStartTue.equals("-") && getEndTue.equals("-")) {
                Toast.makeText(context, "Silakan lengkapi jadwal hari Selasa!", Toast.LENGTH_LONG).show()
            } else if (getStartWed.equals("-") && !getEndWed.equals("-") || !getStartWed.equals("-") && getEndWed.equals("-")) {
                Toast.makeText(context, "Silakan lengkapi jadwal hari Rabu!", Toast.LENGTH_LONG).show()
            } else if (getStartThu.equals("-") && !getEndThu.equals("-") || !getStartThu.equals("-") && getEndThu.equals("-")) {
                Toast.makeText(context, "Silakan lengkapi jadwal hari Kamis!", Toast.LENGTH_LONG).show()
            } else if (getStartFri.equals("-") && !getEndFri.equals("-") || !getStartFri.equals("-") && getEndFri.equals("-")) {
                Toast.makeText(context, "Silakan lengkapi jadwal hari Jum'at!", Toast.LENGTH_LONG).show()
            } else if (getStartSat.equals("-") && !getEndSat.equals("-") || !getStartSat.equals("-") && getEndSat.equals("-")) {
                Toast.makeText(context, "Silakan lengkapi jadwal hari Sabtu!", Toast.LENGTH_LONG).show()
            } else if (getStartSun.equals("-") && !getEndSun.equals("-") || !getStartSun.equals("-") && getEndSun.equals("-")) {
                Toast.makeText(context, "Silakan lengkapi jadwal hari Minggu!", Toast.LENGTH_LONG).show()
            } else {
                if (isConnected()) {
                    if (getStartMon.equals("-")) { setMonday = "-" } else { setMonday = getStartMon + " - " + getEndMon }
                    if (getStartTue.equals("-")) { setTuesday = "-" } else { setTuesday = getStartTue + " - " + getEndTue }
                    if (getStartWed.equals("-")) { setWednesday = "-" } else { setWednesday = getStartWed + " - " + getEndWed }
                    if (getStartThu.equals("-")) { setThursday = "-" } else { setThursday = getStartThu + " - " + getEndThu }
                    if (getStartFri.equals("-")) { setFriday = "-" } else { setFriday = getStartFri + " - " + getEndFri }
                    if (getStartSat.equals("-")) { setSaturday = "-" } else { setSaturday = getStartSat + " - " + getEndSat }
                    if (getStartSun.equals("-")) { setSunday = "-" } else { setSunday = getStartSun + " - " + getEndSun }

                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            ref.child("monday").setValue(setMonday)
                            ref.child("tuesday").setValue(setTuesday)
                            ref.child("wednesday").setValue(setWednesday)
                            ref.child("thursday").setValue(setThursday)
                            ref.child("friday").setValue(setFriday)
                            ref.child("saturday").setValue(setSaturday)
                            ref.child("sunday").setValue(setSunday)
                            dialog.dismiss()
                            Toast.makeText(context, "Jadwal bimbingan telah diperbarui", Toast.LENGTH_SHORT).show()
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

    private fun timePicker(mText: TextView) {
        val timePicker =  TimePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    val calendar = Calendar.getInstance()
                    calendar.set(0, 0, 0, hourOfDay, minute)
                    mText.text = DateFormat.format("HH:mm", calendar)
                }, 24, 0, true)
        timePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        timePicker.show()
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

    private fun phoneCall() {
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
        } else {
            if (!getPhone.equals("null")) {
                startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:" + getPhone)))
            } else {
                Toast.makeText(context, "Nomor telepon tidak tersedia", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                phoneCall()
            } else {
                Toast.makeText(context, "PERMISSION DENIED", Toast.LENGTH_SHORT).show()
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
