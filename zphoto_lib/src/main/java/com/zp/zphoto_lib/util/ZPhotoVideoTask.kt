package com.zp.zphoto_lib.util

import android.app.ProgressDialog
import android.content.Context
import android.database.Cursor
import android.os.AsyncTask
import android.provider.MediaStore
import com.zp.zphoto_lib.content.ZPhotoDetail
import java.lang.ref.SoftReference

class ZPhotoVideoTask(
    context: Context,
    private var listener: (ArrayList<ZPhotoDetail>) -> Unit,
    private var isShowLoading: Boolean = false
) : AsyncTask<Void, Void, ArrayList<ZPhotoDetail>>() {

    private val softReference: SoftReference<Context> by lazy {
        SoftReference<Context>(context)
    }

    private var dialog: ProgressDialog? = null

    override fun onPreExecute() {
        if (isShowLoading) {
            if (softReference.get() != null) {
                dialog = ProgressDialog(softReference.get()).run {
                    setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    setMessage("读取视频数据，请稍后...")
                    setCancelable(false)
                    this
                }
                dialog?.show()
            }
        }
    }

    override fun doInBackground(vararg params: Void?) = getVideoList()

    override fun onPostExecute(result: ArrayList<ZPhotoDetail>) {
        if (isShowLoading) {
            dialog?.dismiss()
            dialog = null
        }
        softReference.clear()
        listener(result)
    }

    private fun getVideoList() = ArrayList<ZPhotoDetail>().apply {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.SIZE
            )
            val where = (MediaStore.Images.Media.MIME_TYPE + "=?")
            val whereArgs = arrayOf("video/mp4")

            cursor = softReference.get()?.contentResolver?.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, where, whereArgs, MediaStore.Video.Media.DATE_ADDED + " DESC "
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    var displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
                    // 大小
                    val size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    // 路径
                    val path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    // 日期
                    val date = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED))
                    // 时长
                    val duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                    if (displayName.isNullOrEmpty()) {
                        displayName = path.substring(path.lastIndexOf("/") + 1, path.length)
                    }
                    add(
                        ZPhotoDetail(
                            path, displayName,
                            ZFile.formetFileSize(size, ZFile.SIZETYPE_MB),
                            false, true, duration, "", date
                        )
                    )
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
    }
}