package com.uhamka.bosm.home.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.uhamka.bosm.R
import com.uhamka.bosm.home.HomeActivity
import com.uhamka.bosm.home.dashboard.dospem.DaftarDospemActivity
import com.uhamka.bosm.home.dashboard.user.UserListActivity
import com.uhamka.bosm.home.dashboard.proposal.list.ListProposalActivity
import com.uhamka.bosm.sign.signin.SignInActivity
import com.uhamka.bosm.utils.Preferences

class HomeAdminFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_admin, container, false)

        val preferences = Preferences(context!!)

        val dosen = view.findViewById<CardView>(R.id.cv_list_dospem)
        val proposal = view.findViewById<CardView>(R.id.cv_proposal)
        val user = view.findViewById<CardView>(R.id.cv_list_user)
        val logout = view.findViewById<CardView>(R.id.cv_logout)

        dosen.setOnClickListener {
            startActivity(Intent(activity, DaftarDospemActivity::class.java))
        }

        proposal.setOnClickListener {
            startActivity(Intent(activity, ListProposalActivity::class.java))
        }

        user.setOnClickListener {
            startActivity(Intent(activity, UserListActivity::class.java))
        }

        logout.setOnClickListener {
            preferences.setValues("status", "0")
            startActivity(Intent(activity, SignInActivity::class.java))
            val activity = activity as HomeActivity
            activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        return view
    }
}