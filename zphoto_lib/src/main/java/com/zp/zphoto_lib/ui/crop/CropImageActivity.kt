package com.zp.zphoto_lib.ui.crop

import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.opengl.GLES10
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.content.ZPHOTO_CROP_ERROR_CODE
import com.zp.zphoto_lib.content.setStatusBarTransparent
import kotlinx.android.synthetic.main.activity_zphoto_crop.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.CountDownLatch

internal class CropImageActivity : MonitoredActivity() {

    private val SIZE_DEFAULT = 2048
    private val SIZE_LIMIT = 4096

    private val handler = Handler()

    private var aspectX = 0
    private var aspectY = 0

    // Output image
    private var maxX = 0
    private var maxY = 0
    private var exifRotation = 0
    private var saveAsPng = false

    private var sourceUri: Uri? = null
    private var saveUri: Uri? = null

    private var isSaving = false

    private var sampleSize = 0
    private var rotateBitmap: RotateBitmap? = null
    private var cropView: HighlightView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarTransparent()
        setupViews()
        loadInput()
        if (rotateBitmap == null) {
            finish()
            return
        }
        startCrop()
    }

    private fun setupViews() {
        setContentView(R.layout.activity_zphoto_crop)

        crop_image.context = this
        crop_image.setRecycler(object : ImageViewTouchBase.Recycler {
            override fun recycle(b: Bitmap) {
                b.recycle()
                System.gc()
            }
        })

        btn_cancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        btn_done.setOnClickListener { onSaveClicked() }
    }

    private fun loadInput() {
        val intent = intent
        val extras = intent.extras

        if (extras != null) {
            aspectX = extras.getInt(ZPhotoCrop.Extra.ASPECT_X)
            aspectY = extras.getInt(ZPhotoCrop.Extra.ASPECT_Y)
            maxX = extras.getInt(ZPhotoCrop.Extra.MAX_X)
            maxY = extras.getInt(ZPhotoCrop.Extra.MAX_Y)
            saveAsPng = extras.getBoolean(ZPhotoCrop.Extra.AS_PNG, false)
            saveUri = extras.getParcelable(MediaStore.EXTRA_OUTPUT)
        }

        sourceUri = intent.data
        if (sourceUri != null) {
            exifRotation = CropUtil.getExifRotation(CropUtil.getFromMediaUri(this, contentResolver, sourceUri))

            var inputStream: InputStream? = null
            try {
                sampleSize = calculateBitmapSampleSize(sourceUri!!)
                inputStream = contentResolver.openInputStream(sourceUri!!)
                val option = BitmapFactory.Options()
                option.inSampleSize = sampleSize
                rotateBitmap = RotateBitmap(BitmapFactory.decodeStream(inputStream, null, option), exifRotation)
            } catch (e: IOException) {
                e.printStackTrace()
                setResultException(e)
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
                setResultException(e)
            } finally {
                CropUtil.closeSilently(inputStream)
            }
        }
    }

    @Throws(IOException::class)
    private fun calculateBitmapSampleSize(bitmapUri: Uri): Int {
        var inputStream: InputStream? = null
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            inputStream = contentResolver.openInputStream(bitmapUri)
            BitmapFactory.decodeStream(inputStream, null, options) // Just get image size
        } finally {
            CropUtil.closeSilently(inputStream)
        }

        val maxSize = getMaxImageSize()
        var sampleSize = 1
        while (options.outHeight / sampleSize > maxSize || options.outWidth / sampleSize > maxSize) {
            sampleSize = sampleSize shl 1
        }
        return sampleSize
    }

    private fun getMaxImageSize(): Int {
        val textureLimit = getMaxTextureSize()
        return if (textureLimit == 0) {
            SIZE_DEFAULT
        } else {
            Math.min(textureLimit, SIZE_LIMIT)
        }
    }

    private fun getMaxTextureSize(): Int {
        // The OpenGL texture size is the maximum size that can be drawn in an ImageView
        val maxSize = IntArray(1)
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0)
        return maxSize[0]
    }

    private fun startCrop() {
        if (isFinishing) {
            return
        }
        crop_image.setImageRotateBitmapResetBase(rotateBitmap, true)
        CropUtil.startBackgroundJob(
            this, null, resources.getString(R.string.zphoto_crop_wait),
            Runnable {
                val latch = CountDownLatch(1)
                handler.post {
                    if (crop_image.getScale() == 1f) {
                        crop_image.center()
                    }
                    latch.countDown()
                }
                try {
                    latch.await()
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }

                Cropper().crop()
            }, handler
        )
    }

    private inner class Cropper {
        private fun makeDefault() {
            if (rotateBitmap == null) {
                return
            }

            val hv = HighlightView(crop_image)
            val width = rotateBitmap!!.getWidth()
            val height = rotateBitmap!!.getHeight()

            val imageRect = Rect(0, 0, width, height)

            // Make the default size about 4/5 of the width or height
            var cropWidth = Math.min(width, height) * 4 / 5
            var cropHeight = cropWidth

            if (aspectX != 0 && aspectY != 0) {
                if (aspectX > aspectY) {
                    cropHeight = cropWidth * aspectY / aspectX
                } else {
                    cropWidth = cropHeight * aspectX / aspectY
                }
            }

            val x = (width - cropWidth) / 2
            val y = (height - cropHeight) / 2

            val cropRect = RectF(x.toFloat(), y.toFloat(), (x + cropWidth).toFloat(), (y + cropHeight).toFloat())
            hv.setup(crop_image.getUnrotatedMatrix(), imageRect, cropRect, aspectX != 0 && aspectY != 0)
            crop_image.add(hv)
        }

        fun crop() {
            handler.post {
                makeDefault()
                crop_image.invalidate()
                if (crop_image.highlightViews.size == 1) {
                    cropView = crop_image.highlightViews[0]
                    cropView!!.setFocus(true)
                }
            }
        }
    }

    private fun onSaveClicked() {
        if (cropView == null || isSaving) {
            return
        }
        isSaving = true

        val croppedImage: Bitmap?
        val r = cropView!!.getScaledCropRect(sampleSize.toFloat())
        val width = r.width()
        val height = r.height()

        var outWidth = width
        var outHeight = height
        if (maxX > 0 && maxY > 0 && (width > maxX || height > maxY)) {
            val ratio = width.toFloat() / height.toFloat()
            if (maxX.toFloat() / maxY.toFloat() > ratio) {
                outHeight = maxY
                outWidth = (maxY.toFloat() * ratio + .5f).toInt()
            } else {
                outWidth = maxX
                outHeight = (maxX.toFloat() / ratio + .5f).toInt()
            }
        }

        try {
            croppedImage = decodeRegionCrop(r, outWidth, outHeight)
        } catch (e: IllegalArgumentException) {
            setResultException(e)
            finish()
            return
        }

        if (croppedImage != null) {
            crop_image.setImageRotateBitmapResetBase(RotateBitmap(croppedImage, exifRotation), true)
            crop_image.center()
            crop_image.highlightViews.clear()
        }
        saveImage(croppedImage)
    }

    private fun saveImage(croppedImage: Bitmap?) {
        if (croppedImage != null) {
            CropUtil.startBackgroundJob(
                this, null, resources.getString(R.string.zphoto_crop_saving),
                Runnable { saveOutput(croppedImage) }, handler
            )
        } else {
            finish()
        }
    }

    private fun decodeRegionCrop(rect: Rect, outWidth: Int, outHeight: Int): Bitmap? {
        var rect = rect
        // Release memory now
        clearImageView()

        var inputStream: InputStream? = null
        var croppedImage: Bitmap? = null
        try {
            inputStream = contentResolver.openInputStream(sourceUri!!)
            val decoder = BitmapRegionDecoder.newInstance(inputStream, false)
            val width = decoder.width
            val height = decoder.height

            if (exifRotation != 0) {
                // Adjust crop area to account for image rotation
                val matrix = Matrix()
                matrix.setRotate((-exifRotation).toFloat())

                val adjusted = RectF()
                matrix.mapRect(adjusted, RectF(rect))

                // Adjust to account for origin at 0,0
                adjusted.offset(
                    (if (adjusted.left < 0) width else 0).toFloat(),
                    (if (adjusted.top < 0) height else 0).toFloat()
                )
                rect =
                    Rect(adjusted.left.toInt(), adjusted.top.toInt(), adjusted.right.toInt(), adjusted.bottom.toInt())
            }

            try {
                croppedImage = decoder.decodeRegion(rect, BitmapFactory.Options())
                if (croppedImage != null && (rect.width() > outWidth || rect.height() > outHeight)) {
                    val matrix = Matrix()
                    matrix.postScale(outWidth.toFloat() / rect.width(), outHeight.toFloat() / rect.height())
                    croppedImage = Bitmap.createBitmap(
                        croppedImage,
                        0,
                        0,
                        croppedImage.width,
                        croppedImage.height,
                        matrix,
                        true
                    )
                }
            } catch (e: IllegalArgumentException) {
                // Rethrow with some extra information
                throw IllegalArgumentException(
                    "Rectangle " + rect + " is outside of the image ("
                            + width + "," + height + "," + exifRotation + ")", e
                )
            }

        } catch (e: IOException) {
            setResultException(e)
        } catch (e: OutOfMemoryError) {
            setResultException(e)
        } finally {
            CropUtil.closeSilently(inputStream)
        }
        return croppedImage
    }

    private fun clearImageView() {
        crop_image.clear()
        if (rotateBitmap != null) {
            rotateBitmap!!.recycle()
        }
        System.gc()
    }

    private fun saveOutput(croppedImage: Bitmap) {
        if (saveUri != null) {
            var outputStream: OutputStream? = null
            try {
                outputStream = contentResolver.openOutputStream(saveUri!!)
                if (outputStream != null) {
                    croppedImage.compress(
                        if (saveAsPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                        90, // note: quality is ignored when using PNG
                        outputStream
                    )
                }
            } catch (e: IOException) {
                setResultException(e)
            } finally {
                CropUtil.closeSilently(outputStream)
            }

            CropUtil.copyExifRotation(
                CropUtil.getFromMediaUri(this, contentResolver, sourceUri),
                CropUtil.getFromMediaUri(this, contentResolver, saveUri)
            )

            setResultUri(saveUri!!)
        }

        handler.post {
            crop_image.clear()
            croppedImage.recycle()
        }

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (rotateBitmap != null) {
            rotateBitmap!!.recycle()
        }
    }

    override fun onSearchRequested() = false

    fun isSaving() = isSaving

    private fun setResultUri(uri: Uri) {
        setResult(RESULT_OK, Intent().putExtra(MediaStore.EXTRA_OUTPUT, uri))
    }

    private fun setResultException(throwable: Throwable) {
        setResult(
            ZPHOTO_CROP_ERROR_CODE,
            Intent().putExtra(ZPhotoCrop.Extra.ERROR, throwable))
    }

}