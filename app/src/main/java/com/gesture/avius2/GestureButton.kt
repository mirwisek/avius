package com.gesture.avius2

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import com.google.android.material.circularreveal.CircularRevealLinearLayout
import com.google.android.material.circularreveal.CircularRevealRelativeLayout

class GestureButton @JvmOverloads
constructor(private val ctx: Context, private val attrs: AttributeSet? = null) :
    CircularRevealRelativeLayout(ctx, attrs) {


    init {
        val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val root = inflater.inflate(R.layout.thumb_button, this)

        val thumb = root.findViewById<ImageView>(R.id.thumb)
        val container = root.findViewById<CircularRevealRelativeLayout>(R.id.container)

        val res = ctx.obtainStyledAttributes(attrs, R.styleable.GestureButton, 0, 0)
        try {
            val drawable = res.getDrawable(R.styleable.GestureButton_thumbDrawable)
            thumb.setImageDrawable(drawable)
            val padding = res.getDimensionPixelSize(R.styleable.GestureButton_layoutPadding, 0)
            container.setPadding(padding, padding, padding, padding)
        } finally {
            res.recycle()
        }
    }

}