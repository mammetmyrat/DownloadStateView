package com.mammet.downloadstateview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.annotation.IntDef
import androidx.appcompat.content.res.AppCompatResources


class DownloadStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ProgressBar(context, attrs, defStyleAttr, defStyleRes) {
    @Target(AnnotationTarget.TYPE)
    @IntDef(
        STATE_DEF,
        STATE_DOWNLOADING,
        STATE_PAUSED,
        STATE_DONE,
        STATE_WAITING,
        STATE_ERROR,
    )

    annotation class State

    companion object {
        const val STATE_DEF = 0
        const val STATE_DOWNLOADING = 1
        const val STATE_PAUSED = 2
        const val STATE_DONE = 3
        const val STATE_WAITING = 4
        const val STATE_ERROR = 5
        const val TAG = "DownloadStateView"
    }


    private val defaultStrokePaint = Paint()
    private val textPaint = Paint()
    private var defaultStrokeRect = RectF()
    private val textBounds = Rect()

    private var diameter = 0f
    private var radius = 0f
    private var cx = 0f
    private var cy = 0f
    var state: @State Int = STATE_DEF
        get() = field
        set(value) {
            field = value
            invalidate()
        }


    private val gapDegrees = 15f // Угловой размер промежутка
    private val segmentDegrees = 90f - gapDegrees // Угловой размер сегмента
    private val rotationDegrees = 43f // Угол поворота круга
    private var centerDrawable: Drawable? = null
    private var downloadDrawable: Drawable? = null
    private val padding = 4f // Padding in pixels

    var text: String = ""
        get() = field
        set(value) {
            field = value
        }
    private var textX = 0f // X coordinate for text
    private var textY = 0f// Y coordinate for text (centered vertically)

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs, R.styleable.DownloadStateView, defStyleAttr, defStyleRes
        )
        downloadDrawable = typedArray.getDrawable(R.styleable.DownloadStateView_downloadIcon)
        centerDrawable = downloadDrawable
        state = typedArray.getInt(R.styleable.DownloadStateView_state, STATE_DEF)
        setDefaultStrokeParams(typedArray)
        setTextParams(typedArray)

    }

    private fun setDefaultStrokeParams(typedArray: TypedArray) {
        defaultStrokePaint.strokeWidth =
            typedArray.getDimension(R.styleable.DownloadStateView_strokeWidth, 10f)
        defaultStrokePaint.color =
            typedArray.getColor(R.styleable.DownloadStateView_strokeColor, Color.GRAY)
        defaultStrokePaint.style = Paint.Style.STROKE
    }

    private fun setTextParams(typedArray: TypedArray) {
        text = typedArray.getString(R.styleable.DownloadStateView_downloadPercentText) ?: ""
        textPaint.textSize = typedArray.getDimension(R.styleable.DownloadStateView_textSize, 14f)
        textPaint.color =
            typedArray.getColor(R.styleable.DownloadStateView_textColor, Color.WHITE)
    }

    override fun setProgressDrawable(d: Drawable?) {
        super.setProgressDrawable(
            AppCompatResources.getDrawable(
                context,
                R.drawable.circular_progress_bar
            )
        )
    }

    override fun invalidate() {
        when (state) {
            STATE_DEF -> {
                centerDrawable = downloadDrawable
            }

            STATE_DOWNLOADING -> {
                progressTintList = AppCompatResources.getColorStateList(context, R.color.white)
            }

            STATE_PAUSED -> {
                progressTintList = AppCompatResources.getColorStateList(context, R.color.neutral)
                centerDrawable = downloadDrawable
            }

            STATE_DONE -> {
                centerDrawable = AppCompatResources.getDrawable(context, R.drawable.icon_tick)

            }

            STATE_WAITING -> {
                centerDrawable = AppCompatResources.getDrawable(context, R.drawable.icon_stop)

            }

            STATE_ERROR -> {
                centerDrawable = AppCompatResources.getDrawable(context, R.drawable.icon_attention)
            }
        }
        updateCenterDrawableBounds()
        super.invalidate()
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateStrokeBounds()
        updateCenterDrawableBounds()
        updateCenterTextBounds()
    }

    private fun updateStrokeBounds() {
        diameter = width.coerceAtMost(height) - defaultStrokePaint.strokeWidth
        radius = diameter / 2f
        cx = width / 2f
        cy = height / 2f
        defaultStrokeRect.set(cx - radius, cy - radius, cx + radius, cy + radius)
    }

    private fun updateCenterDrawableBounds() {
        val drawableSize = diameter.toInt()
        centerDrawable?.setBounds(
            (cx - drawableSize / 3).toInt(),
            (cy - drawableSize / 3).toInt(),
            (cx + drawableSize / 3).toInt(),
            (cy + drawableSize / 3).toInt()
        )
    }

    private fun updateCenterTextBounds() {
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val textWidth = textBounds.width()
        val textHeight = textBounds.height()
        textX = cx - textWidth/2
        textY = cy + textHeight/2
    }

    override fun onDraw(canvas: Canvas) {
        when (state) {
            STATE_DEF -> {
                drawCenterIcon(canvas)
            }

            STATE_DOWNLOADING -> {
                drawText(canvas)
            }

            STATE_PAUSED -> {
                drawCenterIcon(canvas)
            }

            STATE_DONE -> {
                drawCenterIcon(canvas)
                drawCircleStroke(canvas)
            }

            STATE_WAITING -> {
                drawCenterIcon(canvas)
                drawBrokenCircleStroke(canvas)
            }

            STATE_ERROR -> {
                drawCenterIcon(canvas)
                drawBrokenCircleStroke(canvas)
            }
        }
        super.onDraw(canvas)

    }

    private fun drawBrokenCircleStroke(canvas: Canvas) {
        canvas.save()
        canvas.rotate(rotationDegrees, cx, cy)
        for (i in 0 until 4) {
            val startAngle = i * 90f + gapDegrees / 2
            canvas.drawArc(defaultStrokeRect, startAngle, segmentDegrees, false, defaultStrokePaint)
        }
        canvas.restore()
    }

    private fun drawCircleStroke(canvas: Canvas) {
        canvas.save()
        canvas.rotate(rotationDegrees, cx, cy)
        canvas.drawOval(defaultStrokeRect, defaultStrokePaint)
        canvas.restore()
    }

    private fun drawCenterIcon(canvas: Canvas) {
        centerDrawable?.draw(canvas)
    }

    private fun drawText(canvas: Canvas) {
        canvas.drawText(text, textX, textY, textPaint)
    }
}