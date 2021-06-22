package com.gesture.avius2.customui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.gesture.avius2.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class CustomDialog(
    context: Context,
    val inflateLayout: (parent: ViewGroup, dialog: CustomDialog) -> Unit
) : Dialog(context) {

    private var onYes: (() -> Unit)? = null
    private var onNo: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val parent = findViewById<MaterialCardView>(R.id.cardParentDialog)

        inflateLayout.invoke(parent, this)
    }

    fun setOnYesListener(onYes: () -> Unit) {
        this.onYes = onYes
    }

    fun setOnNoListener(onNo: () -> Unit) {
        this.onNo = onNo
    }

}