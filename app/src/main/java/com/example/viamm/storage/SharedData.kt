package com.example.viamm.storage

import android.content.Context
import com.example.viamm.models.Login.User

class SharedData private constructor(private val mCtx: Context) {

    // Change isLoggedIn from val to var
    var isLoggedIn: Boolean
        get() {
            val sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            return sharedPreferences.getString("username", null) != null
        }
        set(value) {
            val sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            if (!value) {
                editor.remove("username") // Remove username from SharedPreferences
                editor.remove("password") // Remove password from SharedPreferences
            }
            editor.apply()
        }

    val user: User?
        get() {
            val sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            val username = sharedPreferences.getString("username", null)
            val password = sharedPreferences.getString("password", null)
            return if (username != null && password != null) {
                User(username, password)
            } else {
                null
            }
        }

    fun saveUser(user: User) {
        val sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("username", user.username)
        editor.putString("password", user.password)

        editor.apply()
    }

    fun clear() {
        val sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    companion object {
        private const val SHARED_PREF_NAME = "my_shared_preff"
        private var mInstance: SharedData? = null

        @Synchronized
        fun getInstance(mCtx: Context): SharedData {
            if (mInstance == null) {
                mInstance = SharedData(mCtx)
            }
            return mInstance!!
        }
    }
}
