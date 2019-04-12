package com.zp.zphoto_lib.util

import android.app.ProgressDialog
import android.content.Context
import android.database.Cursor
import android.os.AsyncTask
import java.lang.Exception
import java.lang.ref.SoftReference
import android.provider.MediaStore
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.content.*
import java.io.File


class ZPhotoImageTask(
    context: Context,
    private var listener: (HashMap<String, ArrayList<ZPhotoDetail>>) -> Unit,
    private var isShowLoading: Boolean = true
) : AsyncTask<Unit, Unit, HashMap<String, ArrayList<ZPhotoDetail>>>() {

    private val softReference by lazy {
        SoftReference<Context>(context)
    }

    private var dialog: ProgressDialog? = null

    override fun onPreExecute() {
        if (isShowLoading) {
            if (softReference.get() != null) {
                dialog = ProgressDialog(softReference.get()).run {
                    setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    setMessage("读取相册数据，请稍后...")
                    setCancelable(false)
                    this
                }
                dialog?.show()
            }
        }
    }
    override fun doInBackground(vararg params: Unit?) = getImagesData()
    override fun onPostExecute(result: HashMap<String, ArrayList<ZPhotoDetail>>) {
        val showVideo = ZPhotoHelp.getInstance().getShowVideo()
        if (showVideo) {
            ZPhotoVideoTask(softReference.get()!!, {
                result[Z_ALL_VIDEO_KEY] = it
                endData(result)

            }, false).execute()
        } else {
           endData(result)
        }
    }

    private fun getImagesData() = HashMap<String, ArrayList<ZPhotoDetail>>().apply {
        var cursor: Cursor? = null
        try {
            // 指定uri
            val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            // 查询指定的列
            val projImage = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED
            )

            val showGif = ZPhotoHelp.getInstance().getShowGif()
            // 查询条件  指定格式
            val whereArgs = if (showGif) arrayOf("image/$JPEG", "image/$PNG", "image/$JPG", "image/$GIF")
            else arrayOf("image/$JPEG", "image/$PNG", "image/$JPG")

            // 查询条件 取值
            val where = if (showGif) (MediaStore.Images.Media.MIME_TYPE + "=? or "
                    + MediaStore.Images.Media.MIME_TYPE + "=? or "
                    + MediaStore.Images.Media.MIME_TYPE + "=? or "
                    + MediaStore.Images.Media.MIME_TYPE + "=?")
            else (MediaStore.Images.Media.MIME_TYPE + "=? or "
                    + MediaStore.Images.Media.MIME_TYPE + "=? or "
                    + MediaStore.Images.Media.MIME_TYPE + "=?")

            // 排序方式
            val sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC"

            cursor = softReference.get()!!.contentResolver.query(imageUri, projImage, where, whereArgs, sortOrder)

            val list = ArrayList<ZPhotoDetail>()

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    // 图片的路径
                    val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                    // 图片的大小
                    val size = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.SIZE)).toLong()
                    // 图片的名称
                    var displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                    // 最后修改时间
                    val date_modified = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED))

                    if (displayName.isNullOrEmpty()) {
                        displayName = path.substring(path.lastIndexOf("/") + 1, path.length)
                    }

                    list.add(
                        ZPhotoDetail(
                            path,
                            displayName,
                            ZFile.formetFileSize(size, ZFile.SIZETYPE_MB),
                            checkGif(path), false, 0,
                            "",
                            date_modified
                        )
                    )

                    // 获取该图片的父路径名
                    val dirPath = File(path).parentFile.absolutePath
                    // 存储对应关系
                    if (this.containsKey(dirPath)) {
                        // 取出相对应的 List
                        val data = this[dirPath]
                        data?.add(
                            ZPhotoDetail(
                                path,
                                displayName,
                                ZFile.formetFileSize(size, ZFile.SIZETYPE_MB),
                                checkGif(path), false,0,
                                "",
                                date_modified
                            )
                        )
                        continue
                    } else {
                        val data = ArrayList<ZPhotoDetail>()
                        data.add(
                            ZPhotoDetail(
                                path,
                                displayName,
                                ZFile.formetFileSize(size, ZFile.SIZETYPE_MB),
                                checkGif(path),false,0,
                                "",
                                date_modified
                            )
                        )
                        this[dirPath] = data
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
    }

    private fun endData(result: HashMap<String, ArrayList<ZPhotoDetail>>) {
        if (isShowLoading) {
            dialog?.dismiss()
            dialog = null
        }
        softReference.clear()
        listener(result)
    }

}