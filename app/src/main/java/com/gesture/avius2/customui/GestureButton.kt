package com.gesture.avius2.customui

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.core.view.marginBottom
import androidx.core.view.setPadding
import com.gesture.avius2.R

class GestureButton @JvmOverloads
constructor(private val ctx: Context, private val attrs: AttributeSet? = null) :
    RelativeLayout(ctx, attrs) {

    var progressBar: ProgressBar
    var circle: ImageView

    init {
        val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val root = inflater.inflate(R.layout.thumb_button, this)

        val imgShadow = root.findViewById<ImageView>(R.id.imageShadow)
        val thumb = root.findViewById<ImageView>(R.id.thumb)
        val container = root.findViewById<RelativeLayout>(R.id.container)
        val content = root.findViewById<RelativeLayout>(R.id.content)
        circle = root.findViewById(R.id.circle)

        progressBar = root.findViewById(R.id.progress)

        val res = ctx.obtainStyledAttributes(attrs, R.styleable.GestureButton, 0, 0)
        try {
            val thumbPadding = res.getDimensionPixelSize(R.styleable.GestureButton_thumbPadding, 30)
            val drawable = res.getDrawable(R.styleable.GestureButton_thumbDrawable)
            thumb.setImageDrawable(drawable)
            thumb.setPadding(thumbPadding)

            val padding = res.getDimensionPixelSize(R.styleable.GestureButton_layoutPadding, 0)
            container.setPadding(padding, padding, padding, padding)

            val marginContent = res.getDimensionPixelSize(R.styleable.GestureButton_marginContent, 0)
            content.setMargin(marginContent)

            val marginShadow = res.getDimensionPixelSize(R.styleable.GestureButton_marginShadow, 0)
            imgShadow.setMargin(marginShadow)

            val marginCircle = res.getDimensionPixelSize(R.styleable.GestureButton_marginCircle, 0)
            circle.setMargin(marginCircle)

            val shadowRadius = res.getDimension(R.styleable.GestureButton_shadowRadius, 0F)
            val d = imgShadow.drawable as GradientDrawable
            d.gradientRadius = shadowRadius

            val innerRadiusRatio =
                res.getFloat(R.styleable.GestureButton_progressInnerRadiusRatio, 4F)
            val thicknessRatio =
                res.getFloat(R.styleable.GestureButton_progressThicknessRatio, 30F)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                updateProgressParams(thicknessRatio, innerRadiusRatio)
                progressBar.invalidate()
            }

        } finally {
            res.recycle()
        }
    }

    private fun View.setMargin(marginValue: Int) {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        params.setMargins(marginValue, marginValue, marginValue, marginValue)
        layoutParams = params
    }

    private fun ViewGroup.setMargin(marginValue: Int) {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        params.setMargins(marginValue, marginValue, marginValue, marginValue)
        layoutParams = params
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateProgressParams(thicknessRatio: Float, innerRadiusRatio: Float) {
        val progressDrawable = progressBar.progressDrawable as LayerDrawable
        val dProgress = progressDrawable.findDrawableByLayerId(android.R.id.progress) as GradientDrawable
        val dSecondary = progressDrawable.findDrawableByLayerId(android.R.id.secondaryProgress) as GradientDrawable

        dProgress.thicknessRatio = thicknessRatio
        dProgress.innerRadiusRatio = innerRadiusRatio

        dSecondary.thicknessRatio = thicknessRatio
        dSecondary.innerRadiusRatio = innerRadiusRatio
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