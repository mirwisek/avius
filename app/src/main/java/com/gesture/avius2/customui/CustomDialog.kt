package com.gesture.avius2.customui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.gesture.avius2.R
import com.google.android.material.button.MaterialButton

class CustomDialog(context: Context) : Dialog(context) {

    private var onYes: (() -> Unit)? = null
    private var onNo: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        findViewById<MaterialButton>(R.id.btn_yes).setOnClickListener {
            onYes?.invoke()
        }
        findViewById<MaterialButton>(R.id.btn_no).setOnClickListener {
            onNo?.invoke()
        }
    }

    fun setOnYesListener(onYes: () -> Unit) {
        this.onYes = onYes
    }

    fun setOnNoListener(onNo: () -> Unit) {
        this.onNo = onNo
    }

}