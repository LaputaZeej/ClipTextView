package com.laputa.cliptextview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView

/**
 * 一个简易的可以动态改变字体颜色的View
 *
 *  涉及知识：
 *      绘制文字
 *      clip
 *
 *  扩展：闪光的TextView 不灵不灵的
 */
class ClipColorTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var text: String = "Google !?"
    var progress: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    var textSize: Int
    var textColor: Int
    var textClipColor: Int

    private val centerLinePaint: Paint by lazy {
        createBasePaint().apply {
            color = Color.GRAY
            style = Paint.Style.STROKE
        }
    }

    private val textRectPaint: Paint by lazy {
        createBasePaint().apply {
            color = Color.GRAY
            style = Paint.Style.STROKE
        }
    }

    private val textPaint: Paint by lazy {
        createBasePaint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
        }
    }

    private val textClipPaint: Paint by lazy {
        createBasePaint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
        }
    }

    private val auxiliaryPaint: Paint by lazy {
        createBasePaint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
        }
    }

    private val mTextBound: Rect by lazy { Rect() }
    private var mTextWidth = 0f
    private var mTextHeight = 0f
    private var mTextStartX = 0f
    private var mTextStartY = 0f

    init {
        val obtainStyledAttributes =
            context.obtainStyledAttributes(attrs, R.styleable.ClipColorTextView)
        textSize =
            obtainStyledAttributes.getDimensionPixelSize(R.styleable.ClipColorTextView_textSize, 18)
        textColor =
            obtainStyledAttributes.getColor(R.styleable.ClipColorTextView_textColor, Color.BLACK)
        textClipColor =
            obtainStyledAttributes.getColor(R.styleable.ClipColorTextView_textClipColor, Color.BLUE)
        progress =
            obtainStyledAttributes.getFloat(R.styleable.ClipColorTextView_progress, 0f)
        text = obtainStyledAttributes.getString(R.styleable.ClipColorTextView_text)?:"Google ?!"
        obtainStyledAttributes.recycle()

    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        textPaint.textSize = textSize.toFloat()
        textPaint.color = textColor
        textClipPaint.textSize = textSize.toFloat()
        textClipPaint.color = textClipColor
        auxiliaryPaint.textSize = textSize.toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureText()
        val finalWidth = measureWidth(widthMeasureSpec)
        val finalHeight = measureHeight(heightMeasureSpec)
        setMeasuredDimension(finalWidth, finalHeight)

        debug {
            Log.i(TAG, "view大小 = ($finalWidth ,$finalHeight)")
        }
        mTextStartX = measuredWidth / 2 - mTextWidth / 2
        mTextStartY = measuredHeight / 2 - mTextHeight / 2
    }

    private fun measureText() {
        textPaint.getTextBounds(text, 0, text.length, mTextBound)
        mTextWidth = textPaint.measureText(text)
        val fontMetrics = Paint.FontMetrics()
        textPaint.getFontMetrics(fontMetrics)
        mTextHeight = fontMetrics.bottom - fontMetrics.top

        debug {
            // 注意：字体大小和字体的Bond大小不一致，按照实际业务来
            // TODO getTextBounds从（0，0）开始测量，且比实际大小小
            Log.i(TAG, "字体大小 = ($mTextWidth ,$mTextHeight)")
            Log.i(TAG, "Bound大小 = (${mTextBound.width()},${mTextBound.height()})")
        }
    }

    private fun measureWidth(widthMeasureSpec: Int): Int {
        val mode = MeasureSpec.getMode(widthMeasureSpec)
        val size = MeasureSpec.getSize(widthMeasureSpec)
        val realSize = (mTextWidth + 0.5f + paddingLeft + paddingRight).toInt()
        return when (mode) {
            MeasureSpec.EXACTLY -> {
                size
            }
            MeasureSpec.AT_MOST -> {
                realSize.coerceAtMost(size)
            }
            else -> {
                realSize
            }
        }
    }

    private fun measureHeight(heightMeasureSpec: Int): Int {
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val size = MeasureSpec.getSize(heightMeasureSpec)
        val realSize = (mTextHeight + 0.5f + paddingTop + paddingBottom).toInt()
        return when (mode) {
            MeasureSpec.EXACTLY -> {
                size
            }
            MeasureSpec.AT_MOST -> {
                realSize.coerceAtMost(size)
            }
            else -> {
                realSize
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        debug {
            drawCenterLine(canvas)
            drawTextBond(canvas)
            drawAuxiliary(canvas)
        }

        drawText(canvas)
    }

    private fun drawAuxiliary(canvas: Canvas) {
        Log.i(TAG, "descent = ${textPaint.descent()}")
        Log.i(TAG, "ascent = ${textPaint.ascent()}")
        auxiliaryPaint.color = Color.GREEN
        canvas.drawLine(
            0f,
            -textPaint.ascent() + textPaint.descent(),
            width.toFloat(),
            -textPaint.ascent() + textPaint.descent(),
            auxiliaryPaint
        )

        auxiliaryPaint.color = Color.RED
        canvas.drawLine(
            0f,
            -textPaint.ascent(),
            width.toFloat(),
            -textPaint.ascent(),
            auxiliaryPaint
        )

        auxiliaryPaint.color = Color.BLUE
        canvas.drawLine(
            0f,
            textPaint.descent(),
            width.toFloat(),
            textPaint.descent(),
            auxiliaryPaint
        )
    }

    var colors: IntArray? = null
    private val pos = null


    private fun drawText(canvas: Canvas) {
        with(canvas) {
            //绘制颜色改变的层
            save()
            // 如果 需要 色彩渐变
            colors?.run {
                val shader = LinearGradient(
                    mTextStartX, mTextStartY, mTextStartX + mTextWidth, mTextStartY + mTextHeight,
                    this, pos, Shader.TileMode.CLAMP
                )
                textClipPaint.shader = shader
            } ?: kotlin.run {
                textClipPaint.color = textClipColor
            }

            clipRect(
                mTextStartX.toInt(),
                0,
                (mTextStartX + progress * mTextWidth).toInt(),
                measuredHeight
            )
            drawText(
                text,
                mTextStartX.toFloat(),
                measuredHeight / 2 - (textPaint.descent() / 2 + textPaint.ascent() / 2)
                ,
                textClipPaint
            )

            restore()

            //绘制底色层
            save()
            clipRect(
                (mTextStartX + progress * mTextWidth).toInt(),
                0,
                (mTextStartX + mTextWidth).toInt(),
                measuredHeight.toInt()
            )
            drawText(
                text,
                mTextStartX.toFloat(),
                measuredHeight / 2 - (textPaint.descent() / 2 + textPaint.ascent() / 2)
                ,
                textPaint
            )
            restore()
        }

    }

    private fun drawTextBond(canvas: Canvas) {
        //canvas.drawRect(mTextBound, centerLinePaint)
        canvas.drawRect(0f, 0f, mTextWidth, mTextHeight, textRectPaint)
    }

    private fun drawCenterLine(canvas: Canvas) {
        canvas.drawLine(
            0f,
            height / 2.toFloat(),
            width.toFloat(),
            height / 2.toFloat(),
            centerLinePaint
        )

        canvas.drawLine(
            width / 2.toFloat(),
            0f,
            width / 2.toFloat(),
            height.toFloat(),
            centerLinePaint
        )
    }

    private fun createBasePaint() = Paint().apply {

        isAntiAlias = true
    }

    companion object {
        const val TAG = "ClipColorTextView"
        private var DEBUG = true
         val DEFAULT_COLORS = intArrayOf(
            Color.argb(255, 255, 0, 0),
            Color.argb(255, 0, 0, 255),
            Color.argb(255, 0, 255, 0)
        )

        fun setDebug(debug: Boolean) {
            DEBUG = debug
        }

        private fun debug(block: () -> Unit) {
            if (DEBUG) {
                block()
            }
        }
    }
}