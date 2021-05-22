@file:Suppress("DEPRECATION")

package com.uhamka.bosm.home.dashboard.proposal.list

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.uhamka.bosm.R
import com.uhamka.bosm.utils.Preferences
import kotlinx.android.synthetic.main.activity_list_proposal.*

class ListProposalActivity : AppCompatActivity() {

    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_proposal)

        preferences = Preferences(this)

        val fragmentInformatics = ProposalTIFragment()
        val fragmentElectro = ProposalTEFragment()
        val fragmentMachine = ProposalTMFragment()

        tabs_main.addTab(tabs_main.newTab().setText("TI"))
        tabs_main.addTab(tabs_main.newTab().setText("TE"))
        tabs_main.addTab(tabs_main.newTab().setText("TM"))

        setFragment(fragmentInformatics)

        tabs_main.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> setFragment(fragmentInformatics)
                    1 -> setFragment(fragmentElectro)
                    else -> setFragment(fragmentMachine)
                }
            }

        })
    }

    private fun setFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
                .replace(R.id.layout_frame, fragment)
                .commit()
    }

    override fun onBackPressed() {
        preferences.setValues("tap", "0")
        super.onBackPressed()
    }
}
