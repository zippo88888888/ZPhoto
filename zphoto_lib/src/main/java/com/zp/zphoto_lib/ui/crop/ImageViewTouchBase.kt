package com.zp.zphoto_lib.ui.crop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Handler
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.ImageView

abstract class ImageViewTouchBase : ImageView {

    private val SCALE_RATE = 1.25f

    // This is the base transformation which is used to show the image
    // initially.  The current computation for this shows the image in
    // it's entirety, letterboxing as needed.  One could choose to
    // show the image as cropped instead.
    //
    // This matrix is recomputed when we go from the thumbnail image to
    // the full size image.
    protected var baseMatrix = Matrix()

    // This is the supplementary transformation which reflects what
    // the user has done in terms of zooming and panning.
    //
    // This matrix remains the same when we go from the thumbnail image
    // to the full size image.
    protected var suppMatrix = Matrix()

    // This is the final matrix which is computed as the concatentation
    // of the base matrix and the supplementary matrix.
    private val displayMatrix = Matrix()

    // Temporary buffer used for getting the values out of a matrix.
    private val matrixValues = FloatArray(9)

    // The current bitmap being displayed.
    protected val bitmapDisplayed = RotateBitmap(null, 0)

    var thisWidth = -1
    var thisHeight = -1

    var maxZoom = 0f

    private var onLayoutRunnable: Runnable? = null

    private var picHandler = Handler()

    // ImageViewTouchBase will pass a Bitmap to the Recycler if it has finished
    // its use of that Bitmap
    interface Recycler {
        fun recycle(b: Bitmap)
    }

    constructor(context: Context?) : super(context){init()}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){init()}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private var recycler: Recycler? = null

    fun setRecycler(recycler: Recycler) {
        this.recycler = recycler
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        thisWidth = right - left
        thisHeight = bottom - top
        val r = onLayoutRunnable
        if (r != null) {
            onLayoutRunnable = null
            r.run()
        }
        if (bitmapDisplayed.bitmap != null) {
            getProperBaseMatrix(bitmapDisplayed, baseMatrix, true)
            imageMatrix = getImageViewMatrix()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            event.startTracking()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking && !event.isCanceled) {
            if (getScale() > 1.0f) {
                // If we're zoomed in, pressing Back jumps out to show the
                // entire image, otherwise Back returns the user to the gallery
                zoomTo(1.0f)
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun setImageBitmap(bitmap: Bitmap?) {
        setImageBitmap(bitmap, 0)
    }

    private fun setImageBitmap(bitmap: Bitmap?, rotation: Int) {
        super.setImageBitmap(bitmap)
        val d = drawable
        d?.setDither(true)

        val old = bitmapDisplayed.bitmap
        bitmapDisplayed.bitmap = bitmap
        bitmapDisplayed.rotation = rotation

        if (old != null && old != bitmap && recycler != null) {
            recycler!!.recycle(old)
        }
    }

    fun clear() {
        setImageBitmapResetBase(null, true)
    }


    // This function changes bitmap, reset base matrix according to the size
    // of the bitmap, and optionally reset the supplementary matrix
    fun setImageBitmapResetBase(bitmap: Bitmap?, resetSupp: Boolean) {
        setImageRotateBitmapResetBase(RotateBitmap(bitmap, 0), resetSupp)
    }

    fun setImageRotateBitmapResetBase(bitmap: RotateBitmap?, resetSupp: Boolean) {
        val viewWidth = width

        if (viewWidth <= 0) {
            onLayoutRunnable = Runnable { setImageRotateBitmapResetBase(bitmap, resetSupp) }
            return
        }

        if (bitmap?.bitmap != null) {
            getProperBaseMatrix(bitmap, baseMatrix, true)
            setImageBitmap(bitmap.bitmap, bitmap.rotation)
        } else {
            baseMatrix.reset()
            setImageBitmap(null)
        }

        if (resetSupp) {
            suppMatrix.reset()
        }
        imageMatrix = getImageViewMatrix()
        maxZoom = calculateMaxZoom()
    }

    // Center as much as possible in one or both axis.  Centering is defined as follows:
    // * If the image is scaled down below the view's dimensions then center it.
    // * If the image is scaled larger than the view and is translated out of view then translate it back into view.
    fun center() {
        val bitmap = bitmapDisplayed.bitmap ?: return
        val m = getImageViewMatrix()

        val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        m.mapRect(rect)

        val height = rect.height()
        val width = rect.width()

        var deltaX = 0f
        var deltaY = 0f

        deltaY = centerVertical(rect, height, deltaY)
        deltaX = centerHorizontal(rect, width, deltaX)

        postTranslate(deltaX, deltaY)
        imageMatrix = getImageViewMatrix()
    }

    private fun centerVertical(rect: RectF, height: Float, deltaY: Float): Float {
        var deltaY2 = deltaY
        val viewHeight = getHeight()
        when {
            height < viewHeight -> deltaY2 = (viewHeight - height) / 2 - rect.top
            rect.top > 0 -> deltaY2 = -rect.top
            rect.bottom < viewHeight -> deltaY2 = getHeight() - rect.bottom
        }
        return deltaY2
    }

    private fun centerHorizontal(rect: RectF, width: Float, deltaX: Float): Float {
        var deltaY2 = deltaX
        val viewWidth = getWidth()
        when {
            width < viewWidth -> deltaY2 = (viewWidth - width) / 2 - rect.left
            rect.left > 0 -> deltaY2 = -rect.left
            rect.right < viewWidth -> deltaY2 = viewWidth - rect.right
        }
        return deltaY2
    }

    private fun init() {
        scaleType = ImageView.ScaleType.MATRIX
    }

    protected fun getValue(matrix: Matrix, whichValue: Int): Float {
        matrix.getValues(matrixValues)
        return matrixValues[whichValue]
    }

    // Get the scale factor out of the matrix.
    protected fun getScale(matrix: Matrix) = getValue(matrix, Matrix.MSCALE_X)

    fun getScale() = getScale(suppMatrix)

    // Setup the base matrix so that the image is centered and scaled properly.
    private fun getProperBaseMatrix(bitmap: RotateBitmap, matrix: Matrix, includeRotation: Boolean) {
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        val w = bitmap.getWidth()
        val h = bitmap.getHeight()
        matrix.reset()

        // We limit up-scaling to 3x otherwise the result may look bad if it's a small icon
        val widthScale = Math.min(viewWidth / w, 3.0f)
        val heightScale = Math.min(viewHeight / h, 3.0f)
        val scale = Math.min(widthScale, heightScale)

        if (includeRotation) {
            matrix.postConcat(bitmap.getRotateMatrix())
        }
        matrix.postScale(scale, scale)
        matrix.postTranslate((viewWidth - w * scale) / 2f, (viewHeight - h * scale) / 2f)
    }

    // Combine the base matrix and the supp matrix to make the final matrix
    protected fun getImageViewMatrix(): Matrix {
        // The final matrix is computed as the concatentation of the base matrix
        // and the supplementary matrix
        displayMatrix.set(baseMatrix)
        displayMatrix.postConcat(suppMatrix)
        return displayMatrix
    }

    fun getUnrotatedMatrix() = Matrix().apply {
        getProperBaseMatrix(bitmapDisplayed, this, false)
        postConcat(suppMatrix)
    }

    protected fun calculateMaxZoom(): Float {
        if (bitmapDisplayed.bitmap == null) {
            return 1f
        }

        val fw = bitmapDisplayed.getWidth() / thisWidth.toFloat()
        val fh = bitmapDisplayed.getHeight() / thisHeight.toFloat()
        return Math.max(fw, fh) * 4 // 400%
    }

    protected open fun zoomTo(scale: Float, centerX: Float, centerY: Float) {
        var scale = scale
        if (scale > maxZoom) {
            scale = maxZoom
        }

        val oldScale = getScale()
        val deltaScale = scale / oldScale

        suppMatrix.postScale(deltaScale, deltaScale, centerX, centerY)
        imageMatrix = getImageViewMatrix()
        center()
    }

    protected fun zoomTo(
        scale: Float, centerX: Float,
        centerY: Float, durationMs: Float
    ) {
        val incrementPerMs = (scale - getScale()) / durationMs
        val oldScale = getScale()
        val startTime = System.currentTimeMillis()

        picHandler.post(object : Runnable {
            override fun run() {
                val now = System.currentTimeMillis()
                val currentMs = Math.min(durationMs, (now - startTime).toFloat())
                val target = oldScale + incrementPerMs * currentMs
                zoomTo(target, centerX, centerY)

                if (currentMs < durationMs) {
                    picHandler.post(this)
                }
            }
        })
    }

    protected open fun zoomTo(scale: Float) {
        val cx = width / 2f
        val cy = height / 2f
        zoomTo(scale, cx, cy)
    }

    protected open fun zoomIn() {
        zoomIn(SCALE_RATE)
    }

    protected open fun zoomOut() {
        zoomOut(SCALE_RATE)
    }

    protected open fun zoomIn(rate: Float) {
        if (getScale() >= maxZoom) {
            return  // Don't let the user zoom into the molecular level
        }
        if (bitmapDisplayed.bitmap == null) {
            return
        }

        val cx = width / 2f
        val cy = height / 2f

        suppMatrix.postScale(rate, rate, cx, cy)
        imageMatrix = getImageViewMatrix()
    }

    protected fun zoomOut(rate: Float) {
        if (bitmapDisplayed.bitmap == null) {
            return
        }

        val cx = width / 2f
        val cy = height / 2f

        // Zoom out to at most 1x
        val tmp = Matrix(suppMatrix)
        tmp.postScale(1f / rate, 1f / rate, cx, cy)

        if (getScale(tmp) < 1f) {
            suppMatrix.setScale(1f, 1f, cx, cy)
        } else {
            suppMatrix.postScale(1f / rate, 1f / rate, cx, cy)
        }
        imageMatrix = getImageViewMatrix()
        center()
    }

    protected open fun postTranslate(dx: Float, dy: Float) {
        suppMatrix.postTranslate(dx, dy)
    }

    protected fun panBy(dx: Float, dy: Float) {
        postTranslate(dx, dy)
        imageMatrix = getImageViewMatrix()
    }

}