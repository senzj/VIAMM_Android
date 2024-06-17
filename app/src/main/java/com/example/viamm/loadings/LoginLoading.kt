package com.example.viamm.loadings

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.example.viamm.R

class LoginLoading(private val activity: Activity) {
    private lateinit var dialog: Dialog

    init {
        setupDialog()
    }

    private fun setupDialog() {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.login_loading, null)
        dialog = Dialog(activity)
        dialog.apply {
            setContentView(dialogView)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(false)
        }
    }

    fun show() {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}
