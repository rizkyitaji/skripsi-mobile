package com.uhamka.bosm.utils

import android.content.Context

class Preferences (val context: Context){

    companion object {
        const val MEETING_PREF = "USER_PREF"
    }

    val sharedPref = context.getSharedPreferences(MEETING_PREF, 0)

    fun setValues(key: String, value: String){
        val editor = sharedPref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getValues(key: String): String? {
        return sharedPref.getString(key, "")
    }
}