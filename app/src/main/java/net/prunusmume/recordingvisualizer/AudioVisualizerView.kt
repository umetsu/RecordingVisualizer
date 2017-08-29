package net.prunusmume.recordingvisualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View


/**
 * これを参考にした。
 * https://github.com/newventuresoftware/WaveformControl
 */
class AudioVisualizerView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {

    companion object {
        private val OFFSET_X = 30
    }

    var samples: ShortArray = ShortArray(0)
        set(samples) {
            field = samples
            onSamplesChanged(field)
        }

    private val paint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorAccent)
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 1f
        alpha = (255 * 0.7).toInt()
        isAntiAlias = true
    }

    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var centerY: Float = 0f
    private var rectPoints: FloatArray = kotlin.FloatArray(0)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        viewWidth = measuredWidth
        viewHeight = measuredHeight
        centerY = viewHeight / 2f

        rectPoints = createRectPoints(samples)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        (0 until rectPoints.size step 4)
                .forEach { i ->
                    canvas.drawRect(rectPoints[i + 0], rectPoints[i + 1], rectPoints[i + 2], rectPoints[i + 3], paint)
                }
    }

    private fun onSamplesChanged(samples: ShortArray) {
        rectPoints = createRectPoints(samples)
        postInvalidate()
    }

    private fun createRectPoints(buffer: ShortArray): FloatArray {
        val max = Short.MAX_VALUE.toFloat()

        var pointIndex = 0
        val points = FloatArray(viewWidth / OFFSET_X * 4)
        (0 until viewWidth step OFFSET_X)
                .forEach { x ->
                    val y = if (samples.isEmpty()) {
                        centerY
                    } else {
                        val sample = buffer[(x * 1.0f / viewWidth * buffer.size).toInt()]
                        centerY - sample / max * centerY
                    }

                    points[pointIndex++] = x.toFloat() + 2              // left
                    points[pointIndex++] = y                            // top
                    points[pointIndex++] = x.toFloat() + OFFSET_X - 2   // right
                    points[pointIndex++] = viewHeight - y               // bottom
                }

        return points
    }
}
