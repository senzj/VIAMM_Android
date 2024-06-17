package com.example.viamm.loadings
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.WindowManager
import com.example.viamm.R

class LoadingDialog(context: Context) : Dialog(context) {

    init {
        // Set the layout for the dialog (defined in dialog_loading.xml)
        setContentView(R.layout.dialog_loading)

        // Set dialog properties
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent background
        window?.setGravity(Gravity.CENTER) // Centered on screen
        window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT) // Wrap content size

        // Make the dialog not cancellable by touching outside of it
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}
