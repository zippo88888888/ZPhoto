package com.zp.zphoto_lib.ui.crop

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.TypedValue
import android.view.View
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.content.getColorById

internal class HighlightView(context: View) {

    companion object {
        const val GROW_NONE = 1 shl 0
        const val GROW_LEFT_EDGE = 1 shl 1
        const val GROW_RIGHT_EDGE = 1 shl 2
        const val GROW_TOP_EDGE = 1 shl 3
        const val GROW_BOTTOM_EDGE = 1 shl 4
        const val MOVE = 1 shl 5

        private val DEFAULT_HIGHLIGHT_COLOR = getColorById(R.color.white)
        private const val HANDLE_RADIUS_DP = 12f
        private const val OUTLINE_DP = 1f
    }

    enum class ModifyMode {
        None, Move, Grow
    }

    enum class HandleMode {
        Changing, Always, Never
    }

    lateinit var cropRect: RectF // Image space
    lateinit var drawRect: Rect // Screen space
    lateinit var matrix: Matrix
    private var imageRect: RectF? = null // Image space

    private val outsidePaint = Paint()
    private val outlinePaint = Paint()
    private val handlePaint = Paint()

    private var viewContext = context // View displaying image
    private var showThirds = false
    private var showCircle = false
    private var highlightColor = 0

    private var modifyMode = ModifyMode.None
    private var handleMode = HandleMode.Changing
    private var maintainAspectRatio = false
    private var initialAspectRatio = 0f
    private var handleRadius = 0f
    private var outlineWidth = 0f
    private var isFocused = false

    init {
        initStyles(context.context)
    }

    private fun initStyles(context: Context) {
        val outValue = TypedValue()
        context.theme.resolveAttribute(R.attr.CropImageStyle, outValue, true)
        val attributes = context.obtainStyledAttributes(outValue.resourceId, R.styleable.CropImageView)
        try {
            showThirds = attributes.getBoolean(R.styleable.CropImageView_showThirds, true)
            showCircle = attributes.getBoolean(R.styleable.CropImageView_showCircle, false)
            highlightColor = attributes.getColor(
                R.styleable.CropImageView_highlightColor,
                DEFAULT_HIGHLIGHT_COLOR
            )
            handleMode = HandleMode.values()[attributes.getInt(R.styleable.CropImageView_showHandles, 1)]
        } finally {
            attributes.recycle()
        }
    }

    fun setup(m: Matrix, imageRect: Rect, cropRect: RectF, maintainAspectRatio: Boolean) {
        matrix = Matrix(m)

        this.cropRect = cropRect
        this.imageRect = RectF(imageRect)
        this.maintainAspectRatio = maintainAspectRatio

        initialAspectRatio = this.cropRect.width() / this.cropRect.height()
        drawRect = computeLayout()

        outsidePaint.setARGB(125, 50, 50, 50)
        outlinePaint.style = Paint.Style.STROKE
        outlinePaint.isAntiAlias = true
        outlineWidth = dpToPx(OUTLINE_DP)

        handlePaint.color = highlightColor
        handlePaint.style = Paint.Style.FILL
        handlePaint.isAntiAlias = true
        handleRadius = dpToPx(HANDLE_RADIUS_DP)

        modifyMode = ModifyMode.None
    }

    private fun dpToPx(dp: Float): Float {
        return dp * viewContext.resources.displayMetrics.density
    }

    fun draw(canvas: Canvas) {
        canvas.save()
        val path = Path()
        outlinePaint.strokeWidth = outlineWidth
        if (!hasFocus()) {
            outlinePaint.color = Color.BLACK
            canvas.drawRect(drawRect, outlinePaint)
        } else {
            val viewDrawingRect = Rect()
            viewContext.getDrawingRect(viewDrawingRect)

            path.addRect(RectF(drawRect), Path.Direction.CW)
            outlinePaint.color = highlightColor

            if (isClipPathSupported(canvas)) {
                canvas.clipPath(path, Region.Op.DIFFERENCE)
                canvas.drawRect(viewDrawingRect, outsidePaint)
            } else {
                drawOutsideFallback(canvas)
            }

            canvas.restore()
            canvas.drawPath(path, outlinePaint)

            if (showThirds) {
                drawThirds(canvas)
            }

            if (showCircle) {
                drawCircle(canvas)
            }

            if (handleMode == HandleMode.Always || handleMode == HandleMode.Changing && modifyMode == ModifyMode.Grow) {
                drawHandles(canvas)
            }
        }
    }

    /*
     * Fall back to naive method for darkening outside crop area
     */
    private fun drawOutsideFallback(canvas: Canvas) {
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), drawRect.top.toFloat(), outsidePaint)
        canvas.drawRect(0f, drawRect.bottom.toFloat(), canvas.width.toFloat(), canvas.height.toFloat(), outsidePaint)
        canvas.drawRect(0f, drawRect.top.toFloat(), drawRect.left.toFloat(), drawRect.bottom.toFloat(), outsidePaint)
        canvas.drawRect(
            drawRect.right.toFloat(),
            drawRect.top.toFloat(),
            canvas.width.toFloat(),
            drawRect.bottom.toFloat(),
            outsidePaint
        )
    }

    /*
     * Clip path is broken, unreliable or not supported on:
     * - JellyBean MR1
     * - ICS & ICS MR1 with hardware acceleration turned on
     */
    @SuppressLint("NewApi")
    private fun isClipPathSupported(canvas: Canvas): Boolean {
        return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR1) {
            false
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH || Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            true
        } else {
            !canvas.isHardwareAccelerated
        }
    }

    private fun drawHandles(canvas: Canvas) {
        val xMiddle = drawRect.left + (drawRect.right - drawRect.left) / 2
        val yMiddle = drawRect.top + (drawRect.bottom - drawRect.top) / 2

        canvas.drawCircle(drawRect.left.toFloat(), yMiddle.toFloat(), handleRadius, handlePaint)
        canvas.drawCircle(xMiddle.toFloat(), drawRect.top.toFloat(), handleRadius, handlePaint)
        canvas.drawCircle(drawRect.right.toFloat(), yMiddle.toFloat(), handleRadius, handlePaint)
        canvas.drawCircle(xMiddle.toFloat(), drawRect.bottom.toFloat(), handleRadius, handlePaint)
    }

    private fun drawThirds(canvas: Canvas) {
        outlinePaint.strokeWidth = 1f
        val xThird = ((drawRect.right - drawRect.left) / 3).toFloat()
        val yThird = ((drawRect.bottom - drawRect.top) / 3).toFloat()

        canvas.drawLine(
            drawRect.left + xThird, drawRect.top.toFloat(),
            drawRect.left + xThird, drawRect.bottom.toFloat(), outlinePaint
        )
        canvas.drawLine(
            drawRect.left + xThird * 2, drawRect.top.toFloat(),
            drawRect.left + xThird * 2, drawRect.bottom.toFloat(), outlinePaint
        )
        canvas.drawLine(
            drawRect.left.toFloat(), drawRect.top + yThird,
            drawRect.right.toFloat(), drawRect.top + yThird, outlinePaint
        )
        canvas.drawLine(
            drawRect.left.toFloat(), drawRect.top + yThird * 2,
            drawRect.right.toFloat(), drawRect.top + yThird * 2, outlinePaint
        )
    }

    private fun drawCircle(canvas: Canvas) {
        outlinePaint.strokeWidth = 1f
        canvas.drawOval(RectF(drawRect), outlinePaint)
    }

    fun setMode(mode: ModifyMode) {
        if (mode != modifyMode) {
            modifyMode = mode
            viewContext.invalidate()
        }
    }

    // Determines which edges are hit by touching at (x, y)
    fun getHit(x: Float, y: Float): Int {
        val r = computeLayout()
        val hysteresis = 20f
        var retval = GROW_NONE

        // verticalCheck makes sure the position is between the top and
        // the bottom edge (with some tolerance). Similar for horizCheck.
        val verticalCheck = y >= r.top - hysteresis && y < r.bottom + hysteresis
        val horizCheck = x >= r.left - hysteresis && x < r.right + hysteresis

        // Check whether the position is near some edge(s)
        if (Math.abs(r.left - x) < hysteresis && verticalCheck) {
            retval = retval or GROW_LEFT_EDGE
        }
        if (Math.abs(r.right - x) < hysteresis && verticalCheck) {
            retval = retval or GROW_RIGHT_EDGE
        }
        if (Math.abs(r.top - y) < hysteresis && horizCheck) {
            retval = retval or GROW_TOP_EDGE
        }
        if (Math.abs(r.bottom - y) < hysteresis && horizCheck) {
            retval = retval or GROW_BOTTOM_EDGE
        }

        // Not near any edge but inside the rectangle: move
        if (retval == GROW_NONE && r.contains(x.toInt(), y.toInt())) {
            retval = MOVE
        }
        return retval
    }

    // Handles motion (dx, dy) in screen space.
    // The "edge" parameter specifies which edges the user is dragging.
    fun handleMotion(edge: Int, dx: Float, dy: Float) {
        var dx = dx
        var dy = dy
        val r = computeLayout()
        if (edge == MOVE) {
            // Convert to image space before sending to moveBy()
            moveBy(
                dx * (cropRect.width() / r.width()),
                dy * (cropRect.height() / r.height())
            )
        } else {
            if (GROW_LEFT_EDGE or GROW_RIGHT_EDGE and edge == 0) {
                dx = 0f
            }

            if (GROW_TOP_EDGE or GROW_BOTTOM_EDGE and edge == 0) {
                dy = 0f
            }

            // Convert to image space before sending to growBy()
            val xDelta = dx * (cropRect.width() / r.width())
            val yDelta = dy * (cropRect.height() / r.height())
            growBy(
                (if (edge and GROW_LEFT_EDGE != 0) -1 else 1) * xDelta,
                (if (edge and GROW_TOP_EDGE != 0) -1 else 1) * yDelta
            )
        }
    }

    // Grows the cropping rectangle by (dx, dy) in image space
    fun moveBy(dx: Float, dy: Float) {
        val invalRect = Rect(drawRect)

        cropRect.offset(dx, dy)

        // Put the cropping rectangle inside image rectangle
        cropRect.offset(
            Math.max(0f, imageRect!!.left - cropRect.left),
            Math.max(0f, imageRect!!.top - cropRect.top)
        )

        cropRect.offset(
            Math.min(0f, imageRect!!.right - cropRect.right),
            Math.min(0f, imageRect!!.bottom - cropRect.bottom)
        )

        drawRect = computeLayout()
        invalRect.union(drawRect)
        invalRect.inset(-handleRadius.toInt(), -handleRadius.toInt())
        viewContext.invalidate(invalRect)
    }

    // Grows the cropping rectangle by (dx, dy) in image space.
    fun growBy(dx: Float, dy: Float) {
        var dx = dx
        var dy = dy
        if (maintainAspectRatio) {
            if (dx != 0f) {
                dy = dx / initialAspectRatio
            } else if (dy != 0f) {
                dx = dy * initialAspectRatio
            }
        }

        // Don't let the cropping rectangle grow too fast.
        // Grow at most half of the difference between the image rectangle and
        // the cropping rectangle.
        val r = RectF(cropRect)
        if (dx > 0f && r.width() + 2 * dx > imageRect!!.width()) {
            dx = (imageRect!!.width() - r.width()) / 2f
            if (maintainAspectRatio) {
                dy = dx / initialAspectRatio
            }
        }
        if (dy > 0f && r.height() + 2 * dy > imageRect!!.height()) {
            dy = (imageRect!!.height() - r.height()) / 2f
            if (maintainAspectRatio) {
                dx = dy * initialAspectRatio
            }
        }

        r.inset(-dx, -dy)

        // Don't let the cropping rectangle shrink too fast
        val widthCap = 25f
        if (r.width() < widthCap) {
            r.inset(-(widthCap - r.width()) / 2f, 0f)
        }
        val heightCap = if (maintainAspectRatio)
            widthCap / initialAspectRatio
        else
            widthCap
        if (r.height() < heightCap) {
            r.inset(0f, -(heightCap - r.height()) / 2f)
        }

        // Put the cropping rectangle inside the image rectangle
        if (r.left < imageRect!!.left) {
            r.offset(imageRect!!.left - r.left, 0f)
        } else if (r.right > imageRect!!.right) {
            r.offset(-(r.right - imageRect!!.right), 0f)
        }
        if (r.top < imageRect!!.top) {
            r.offset(0f, imageRect!!.top - r.top)
        } else if (r.bottom > imageRect!!.bottom) {
            r.offset(0f, -(r.bottom - imageRect!!.bottom))
        }

        cropRect.set(r)
        drawRect = computeLayout()
        viewContext.invalidate()
    }

    // Returns the cropping rectangle in image space with specified scale
    fun getScaledCropRect(scale: Float): Rect {
        return Rect(
            (cropRect.left * scale).toInt(), (cropRect.top * scale).toInt(),
            (cropRect.right * scale).toInt(), (cropRect.bottom * scale).toInt()
        )
    }

    // Maps the cropping rectangle from image space to screen space
    private fun computeLayout(): Rect {
        val r = RectF(
            cropRect.left, cropRect.top,
            cropRect.right, cropRect.bottom
        )
        matrix.mapRect(r)
        return Rect(
            Math.round(r.left), Math.round(r.top),
            Math.round(r.right), Math.round(r.bottom)
        )
    }

    fun invalidate() {
        drawRect = computeLayout()
    }

    fun hasFocus(): Boolean {
        return isFocused
    }

    fun setFocus(isFocused: Boolean) {
        this.isFocused = isFocused
    }

}