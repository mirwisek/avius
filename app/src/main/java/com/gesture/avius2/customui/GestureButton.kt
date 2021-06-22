package com.gesture.avius2.customui

import android.content.Context
import android.graphics.BlendMode
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.marginBottom
import androidx.core.view.setPadding
import com.gesture.avius2.R
import com.google.android.material.circularreveal.CircularRevealRelativeLayout

class GestureButton @JvmOverloads
constructor(private val ctx: Context, private val attrs: AttributeSet? = null) :
    CircularRevealRelativeLayout(ctx, attrs) {

    var progressBar: ProgressBar
    var circle: ImageView

    init {
        val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val root = inflater.inflate(R.layout.thumb_button, this)

        val thumb = root.findViewById<ImageView>(R.id.thumb)
        val container = root.findViewById<CircularRevealRelativeLayout>(R.id.container)
        circle = root.findViewById(R.id.circle)

        progressBar = root.findViewById(R.id.progress)
        val progressMarginBottom = progressBar.marginBottom

        val res = ctx.obtainStyledAttributes(attrs, R.styleable.GestureButton, 0, 0)
        try {
            val thumbPadding = res.getDimensionPixelSize(R.styleable.GestureButton_thumbPadding, 30)
            val drawable = res.getDrawable(R.styleable.GestureButton_thumbDrawable)
            thumb.setImageDrawable(drawable)
            thumb.setPadding(thumbPadding)

            val padding = res.getDimensionPixelSize(R.styleable.GestureButton_layoutPadding, 0)
            container.setPadding(padding, padding, padding, padding)

            val marginCircle = res.getDimensionPixelSize(R.styleable.GestureButton_marginCircle, 0)
            val circleParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            circleParams.setMargins(marginCircle, marginCircle, marginCircle, marginCircle)
            circle.layoutParams = circleParams

            val marginProgress =
                res.getDimensionPixelSize(R.styleable.GestureButton_marginProgress, 0)
            val progressParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            progressParams.setMargins(
                marginProgress,
                marginProgress,
                marginProgress,
                marginProgress + progressMarginBottom
            )
            progressBar.layoutParams = progressParams
        } finally {
            res.recycle()
        }
    }

    fun changeCircleColor(color: Int) {
        val shape = circle.drawable as GradientDrawable
        shape.setColor(color)
//        circle.drawable.colorFilter =
//            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
//                color, BlendModeCompat.SRC_ATOP
//            )
    }

}