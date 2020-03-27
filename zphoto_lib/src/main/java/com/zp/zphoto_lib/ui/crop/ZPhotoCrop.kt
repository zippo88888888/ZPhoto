package com.zp.zphoto_lib.ui.crop

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import com.zp.zphoto_lib.R
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.content.ZPHOTO_CROP_REQUEST_CODE
import com.zp.zphoto_lib.content.ZPHOTO_PICK_REQUEST_CODE
import com.zp.zphoto_lib.util.ZToaster

internal class ZPhotoCrop(source: Uri, destination: Uri) {

    private var cropIntent = Intent()

    init {
        cropIntent.data = source
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, destination)
    }

    internal interface Extra {
        companion object {
            const val ASPECT_X = "aspect_x"
            const val ASPECT_Y = "aspect_y"
            const val MAX_X = "max_x"
            const val MAX_Y = "max_y"
            const val AS_PNG = "as_png"
            const val ERROR = "error"
        }
    }

    companion object {
//        const val REQUEST_CROP = 6709
//        const val REQUEST_PICK = 9162
//        const val RESULT_ERROR = 404

        /**
         * Create a crop Intent builder with source and destination image Uris
         *
         * @param source      Uri for image to crop
         * @param destination Uri for saving the cropped image
         */
        fun of(source: Uri, destination: Uri) = ZPhotoCrop(source, destination)

        /**
         * Retrieve URI for cropped image, as set in the Intent builder
         *
         * @param result Output Image URI
         */
        fun getOutput(result: Intent): Uri {
            return result.getParcelableExtra(MediaStore.EXTRA_OUTPUT)
        }

        /**
         * Retrieve error that caused crop to fail
         *
         * @param result Result Intent
         * @return Throwable handled in CropImageActivity
         */
        fun getError(result: Intent) = result.getSerializableExtra(Extra.ERROR) as Throwable

        /**
         * Pick image from an Activity
         *
         * @param activity Activity to receive result
         */
        fun pickImage(activity: Activity) {
            pickImage(activity, ZPHOTO_PICK_REQUEST_CODE)
        }

        /**
         * Pick image from an Activity with a custom request code
         *
         * @param activity    Activity to receive result
         * @param requestCode requestCode for result
         */
        fun pickImage(activity: Activity, requestCode: Int) {
            try {
                activity.startActivityForResult(getImagePicker(), requestCode)
            } catch (e: ActivityNotFoundException) {
                showImagePickerError()
            }
        }

        /**
         * Pick image from a support library Fragment with a custom request code
         *
         * @param fragment    Fragment to receive result
         * @param requestCode requestCode for result
         */
        fun pickImage(fragment: Fragment, requestCode: Int) {
            try {
                fragment.startActivityForResult(getImagePicker(), requestCode)
            } catch (e: ActivityNotFoundException) {
                showImagePickerError()
            }
        }

        private fun getImagePicker(): Intent {
            return Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
        }

        private fun showImagePickerError() {
            ZToaster.makeTextS(R.string.zphoto_crop_pick_error)
        }
    }


    /**
     * Set fixed aspect ratio for crop area
     *
     * @param x Aspect X
     * @param y Aspect Y
     */
    fun withAspect(x: Int, y: Int): ZPhotoCrop {
        cropIntent.putExtra(Extra.ASPECT_X, x)
        cropIntent.putExtra(Extra.ASPECT_Y, y)
        return this
    }

    /**
     * Crop area with fixed 1:1 aspect ratio
     */
    fun asSquare(): ZPhotoCrop {
        cropIntent.putExtra(Extra.ASPECT_X, 1)
        cropIntent.putExtra(Extra.ASPECT_Y, 1)
        return this
    }

    /**
     * Set maximum crop size
     *
     * @param width  Max width
     * @param height Max height
     */
    fun withMaxSize(width: Int, height: Int): ZPhotoCrop {
        cropIntent.putExtra(Extra.MAX_X, width)
        cropIntent.putExtra(Extra.MAX_Y, height)
        return this
    }

    /**
     * Set whether to save the result as a PNG or not. Helpful to preserve alpha.
     * @param asPng whether to save the result as a PNG or not
     */
    fun asPng(asPng: Boolean): ZPhotoCrop {
        cropIntent.putExtra(Extra.AS_PNG, asPng)
        return this
    }

    /**
     * Send the crop Intent from an Activity
     *
     * @param activity Activity to receive result
     */
    fun start(activity: Activity) {
        start(activity, ZPHOTO_CROP_REQUEST_CODE)
    }

    /**
     * Send the crop Intent from an Activity with a custom request code
     *
     * @param activity    Activity to receive result
     * @param requestCode requestCode for result
     */
    private fun start(activity: Activity, requestCode: Int) {
        activity.startActivityForResult(getIntent(activity), requestCode)
    }

    /**
     * Send the crop Intent from a support library Fragment
     *
     * @param context  Context
     * @param fragment Fragment to receive result
     */
    fun start(context: Context, fragment: Fragment) {
        start(context, fragment, ZPHOTO_CROP_REQUEST_CODE)
    }

    /**
     * Send the crop Intent with a custom request code
     *
     * @param context     Context
     * @param fragment    Fragment to receive result
     * @param requestCode requestCode for result
     */
    private fun start(context: Context, fragment: Fragment, requestCode: Int) {
        fragment.startActivityForResult(getIntent(context), requestCode)
    }

    /**
     * Get Intent to start crop Activity
     *
     * @param context Context
     * @return Intent for CropImageActivity
     */
    private fun getIntent(context: Context): Intent {
        cropIntent.setClass(context, CropImageActivity::class.java)
        return cropIntent
    }
}