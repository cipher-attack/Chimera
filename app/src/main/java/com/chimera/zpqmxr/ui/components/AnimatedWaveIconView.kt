package com.chimera.zpqmxr.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.sin

class AnimatedWaveIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var phase = 0f

    private val brushPaintSolid = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.WHITE
    }

    private val brushPaintGhost = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.parseColor("#80FFFFFF")
    }

    private val brushPaintAmbient = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.parseColor("#33FFFFFF")
    }

    private var centerY = 0f
    private var viewWidth = 0f

    private val wavePath1 = Path()
    private val wavePath2 = Path()
    private val wavePath3 = Path()

    private var waveAnimator: ValueAnimator? = null

    fun setWaveColor(color: Int) {
        brushPaintSolid.color = color
        brushPaintGhost.color = Color.argb(128, Color.red(color), Color.green(color), Color.blue(color))
        brushPaintAmbient.color = Color.argb(51, Color.red(color), Color.green(color), Color.blue(color))
        invalidate()
    }

    init {
        startFluidAnimation()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerY = h / 2f
        viewWidth = w.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        generateBrushPath(wavePath1, phase, 1.0f)
        generateBrushPath(wavePath2, phase + 2f, 0.7f)
        generateBrushPath(wavePath3, phase + 4f, 0.4f)

        canvas.drawPath(wavePath3, brushPaintAmbient)
        canvas.drawPath(wavePath2, brushPaintGhost)
        canvas.drawPath(wavePath1, brushPaintSolid)
    }

    private fun generateBrushPath(path: Path, currentPhase: Float, amplitudeModifier: Float) {
        path.reset()

        val baseAmplitude = centerY * 0.7f
        val finalAmplitude = baseAmplitude * amplitudeModifier

        val startX = 0f
        val endX = viewWidth
        val step = viewWidth / 20f

        path.moveTo(startX, centerY)

        var x = startX
        while (x <= endX) {
            val progress = x / viewWidth
            val edgeDamping = sin(progress * Math.PI).toFloat()
            val yOffset = sin((progress * Math.PI * 2) + currentPhase).toFloat() * finalAmplitude * edgeDamping

            val nextX = x + step
            val nextProgress = nextX / viewWidth
            val nextEdgeDamping = sin(nextProgress * Math.PI).toFloat()
            val nextYOffset = sin((nextProgress * Math.PI * 2) + currentPhase).toFloat() * finalAmplitude * nextEdgeDamping

            val cpX = x + (step / 2f)
            path.quadTo(cpX, centerY + yOffset, nextX, centerY + nextYOffset)

            x += step
        }
    }

    private fun startFluidAnimation() {
        waveAnimator?.cancel()
        waveAnimator = ValueAnimator.ofFloat(0f, (Math.PI * 2).toFloat()).apply {
            duration = 4000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                phase = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        waveAnimator?.cancel()
    }
}
