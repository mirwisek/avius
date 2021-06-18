package com.gesture.avius2.customui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.setPadding
import com.gesture.avius2.R
import com.google.android.material.circularreveal.CircularRevealRelativeLayout

class GestureButton @JvmOverloads
constructor(private val ctx: Context, private val attrs: AttributeSet? = null) :
    CircularRevealRelativeLayout(ctx, attrs) {

    var progressBar: ProgressBar

    init {
        val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val root = inflater.inflate(R.layout.thumb_button, this)

        val thumb = root.findViewById<ImageView>(R.id.thumb)
        val container = root.findViewById<CircularRevealRelativeLayout>(R.id.container)
        val circle = root.findViewById<ImageView>(R.id.circle)

        progressBar = root.findViewById(R.id.progress)

        val res = ctx.obtainStyledAttributes(attrs, R.styleable.GestureButton, 0, 0)
        try {
            val thumbPadding = res.getDimensionPixelSize(R.styleable.GestureButton_thumbPadding, 30)
            val drawable = res.getDrawable(R.styleable.GestureButton_thumbDrawable)
            thumb.setImageDrawable(drawable)
            thumb.setPadding(thumbPadding)

            val padding = res.getDimensionPixelSize(R.styleable.GestureButton_layoutPadding, 0)
            container.setPadding(padding, padding, padding, padding)

            val marginProgress = res.getDimensionPixelSize(R.styleable.GestureButton_marginProgressFit, 0)
            val circleParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            circleParams.setMargins(marginProgress,marginProgress,marginProgress,marginProgress)
            circle.layoutParams = circleParams
        } finally {
            res.recycle()
        }
    }

}