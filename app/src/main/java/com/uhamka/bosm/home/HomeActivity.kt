package com.uhamka.bosm.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.uhamka.bosm.R
import com.uhamka.bosm.home.bimbingan.BimbinganDosenFragment
import com.uhamka.bosm.home.bimbingan.BimbinganMHSFragment
import com.uhamka.bosm.home.dashboard.HomeAdminFragment
import com.uhamka.bosm.home.dashboard.HomeDosenFragment
import com.uhamka.bosm.home.dashboard.HomeMHSFragment
import com.uhamka.bosm.home.event.EventFragment
import com.uhamka.bosm.home.profile.ProfileDosenFragment
import com.uhamka.bosm.home.profile.ProfileMHSFragment
import com.uhamka.bosm.home.profile.VisitMHSFragment
import com.uhamka.bosm.home.repository.RepositoryActivity
import com.uhamka.bosm.utils.Preferences
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        preferences = Preferences(this)

        val fragmentHomeMHS = HomeMHSFragment()
        val fragmentHomeDosen = HomeDosenFragment()
        val fragmentHomeAdmin = HomeAdminFragment()
        val fragmentBimbinganMHS = BimbinganMHSFragment()
        val fragmentBimbinganDosen = BimbinganDosenFragment()
        val fragmentEvent = EventFragment()
        val fragmentVisitMHS = VisitMHSFragment()
        val fragmentProfileMHS = ProfileMHSFragment()
        val fragmentProfileDosen = ProfileDosenFragment()

        if (preferences.getValues("user").equals("mhs")) {
            iv_assignment.visibility = View.VISIBLE
            iv_profile.visibility = View.VISIBLE
            setWeight(iv_space, 2)
            setFragment(fragmentHomeMHS)

            iv_home.setOnClickListener {
                setFragment(fragmentHomeMHS)

                changeIcon(iv_home, R.drawable.ic_home_active)
                changeIcon(iv_assignment, R.drawable.ic_assignment)
                changeIcon(iv_event, R.drawable.ic_event)
                changeIcon(iv_profile, R.drawable.ic_person)
            }

            iv_assignment.setOnClickListener {
                setFragment(fragmentBimbinganMHS)

                changeIcon(iv_home, R.drawable.ic_home)
                changeIcon(iv_assignment, R.drawable.ic_assignment_active)
                changeIcon(iv_event, R.drawable.ic_event)
                changeIcon(iv_profile, R.drawable.ic_person)
            }

            iv_storage.setOnClickListener {
                startActivity(Intent(this, RepositoryActivity::class.java))
            }

            iv_event.setOnClickListener {
                setFragment(fragmentEvent)

                changeIcon(iv_home, R.drawable.ic_home)
                changeIcon(iv_assignment, R.drawable.ic_assignment)
                changeIcon(iv_event, R.drawable.ic_event_active)
                changeIcon(iv_profile, R.drawable.ic_person)
            }

            iv_profile.setOnClickListener {
                preferences.setValues("action", "profile")
                setFragment(fragmentProfileMHS)

                changeIcon(iv_home, R.drawable.ic_home)
                changeIcon(iv_assignment, R.drawable.ic_assignment)
                changeIcon(iv_event, R.drawable.ic_event)
                changeIcon(iv_profile, R.drawable.ic_person_active)
            }
        } else if (preferences.getValues("user").equals("dosen")) {
            iv_assignment.visibility = View.VISIBLE
            iv_profile.visibility = View.VISIBLE
            setWeight(iv_space, 2)
            setFragment(fragmentHomeDosen)

            iv_home.setOnClickListener {
                setFragment(fragmentHomeDosen)

                changeIcon(iv_home, R.drawable.ic_home_active)
                changeIcon(iv_assignment, R.drawable.ic_assignment)
                changeIcon(iv_event, R.drawable.ic_event)
                changeIcon(iv_profile, R.drawable.ic_person)
            }

            iv_assignment.setOnClickListener {
                setFragment(fragmentBimbinganDosen)

                changeIcon(iv_home, R.drawable.ic_home)
                changeIcon(iv_assignment, R.drawable.ic_assignment_active)
                changeIcon(iv_event, R.drawable.ic_event)
                changeIcon(iv_profile, R.drawable.ic_person)
            }

            iv_storage.setOnClickListener {
                startActivity(Intent(this, RepositoryActivity::class.java))
            }

            iv_event.setOnClickListener {
                setFragment(fragmentEvent)

                changeIcon(iv_home, R.drawable.ic_home)
                changeIcon(iv_assignment, R.drawable.ic_assignment)
                changeIcon(iv_event, R.drawable.ic_event_active)
                changeIcon(iv_profile, R.drawable.ic_person)
            }

            iv_profile.setOnClickListener {
                preferences.setValues("action", "profile")
                setFragment(fragmentProfileDosen)

                changeIcon(iv_home, R.drawable.ic_home)
                changeIcon(iv_assignment, R.drawable.ic_assignment)
                changeIcon(iv_event, R.drawable.ic_event)
                changeIcon(iv_profile, R.drawable.ic_person_active)
            }
        } else if (preferences.getValues("user").equals("admin")) {
            iv_assignment.visibility = View.GONE
            iv_profile.visibility = View.GONE
            setWeight(iv_space, 1)
            setFragment(fragmentHomeAdmin)

            iv_home.setOnClickListener {
                setFragment(fragmentHomeAdmin)

                changeIcon(iv_home, R.drawable.ic_home_active)
                changeIcon(iv_event, R.drawable.ic_event)
            }

            iv_storage.setOnClickListener {
                startActivity(Intent(this, RepositoryActivity::class.java))
            }

            iv_event.setOnClickListener {
                setFragment(fragmentEvent)

                changeIcon(iv_home, R.drawable.ic_home)
                changeIcon(iv_event, R.drawable.ic_event_active)
            }
        }

        val from = intent.getStringExtra("from")!!
        if (from.equals("listDosen")) {
            preferences.setValues("tap", "2")
            preferences.setValues("action", "view")
            setFragment(fragmentProfileDosen)

            if (preferences.getValues("user").equals("admin")) {
                changeIcon(iv_home, R.drawable.ic_home)
                changeIcon(iv_event, R.drawable.ic_event)
            } else {
                changeIcon(iv_home, R.drawable.ic_home)
                changeIcon(iv_assignment, R.drawable.ic_assignment)
                changeIcon(iv_event, R.drawable.ic_event)
                changeIcon(iv_profile, R.drawable.ic_person_visit)
            }
        } else if (from.equals("listMHS")) {
            preferences.setValues("tap", "2")
            setFragment(fragmentVisitMHS)

            if (preferences.getValues("user").equals("admin")) {
                preferences.setValues("action", "view")
                changeIcon(iv_home, R.drawable.ic_home)
                changeIcon(iv_event, R.drawable.ic_event)
            } else {
                changeIcon(iv_home, R.drawable.ic_home)
                changeIcon(iv_assignment, R.drawable.ic_assignment)
                changeIcon(iv_event, R.drawable.ic_event)
                if (preferences.getValues("idLogin").equals(preferences.getValues("nim"))) {
                    preferences.setValues("action", "profile")
                    changeIcon(iv_profile, R.drawable.ic_person_active)
                } else {
                    preferences.setValues("action", "view")
                    changeIcon(iv_profile, R.drawable.ic_person_visit)
                }
            }
        } else if (from.equals("setting_mhs")) {
            preferences.setValues("tap", "0")
            preferences.setValues("action", "profile")
            setFragment(fragmentProfileMHS)

            changeIcon(iv_home, R.drawable.ic_home)
            changeIcon(iv_assignment, R.drawable.ic_assignment)
            changeIcon(iv_event, R.drawable.ic_event)
            changeIcon(iv_profile, R.drawable.ic_person_active)
        } else if (from.equals("setting_dosen")) {
            preferences.setValues("tap", "0")
            preferences.setValues("action", "profile")
            setFragment(fragmentProfileDosen)

            changeIcon(iv_home, R.drawable.ic_home)
            changeIcon(iv_assignment, R.drawable.ic_assignment)
            changeIcon(iv_event, R.drawable.ic_event)
            changeIcon(iv_profile, R.drawable.ic_person_active)
        } else if (from.equals("notif")) {
            preferences.setValues("tap", "0")
            setFragment(fragmentBimbinganMHS)

            changeIcon(iv_home, R.drawable.ic_home)
            changeIcon(iv_assignment, R.drawable.ic_assignment_active)
            changeIcon(iv_event, R.drawable.ic_event)
            changeIcon(iv_profile, R.drawable.ic_person)
        }
    }

    private fun setFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.layout_frame, fragment)
        fragmentTransaction.commit()
    }

    private fun changeIcon(imageView: ImageView, int: Int) {
        imageView.setImageResource(int)
    }

    private fun setWeight(iv: ImageView, weight: Int) {
        val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        params.weight = weight.toFloat()
        iv.layoutParams = params
    }

    override fun onBackPressed() {
        if (preferences.getValues("tap").equals("0")) {
            preferences.setValues("tap", "1")
            Toast.makeText(this, "Tap again to exit", Toast.LENGTH_SHORT).show()
        } else if (preferences.getValues("tap").equals("1")) {
            preferences.setValues("tap", "0")
            finishAffinity()
        } else {
            preferences.setValues("tap", "0")
            super.onBackPressed()
        }
    }
}
