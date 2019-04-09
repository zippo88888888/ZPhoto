package com.zp.zphoto_lib.util

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import java.lang.Exception
import java.lang.ref.SoftReference
import android.provider.MediaStore
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.content.*
import java.io.File


class ZPhotoTask(context: Context, private var listener: (HashMap<String, ArrayList<ImageDetail>>) -> Unit) :
    AsyncTask<Unit, Unit, HashMap<String, ArrayList<ImageDetail>>>() {

    private val softReference by lazy {
        SoftReference<Context>(context)
    }

    private var dialog: ProgressDialog? = null

    override fun onPreExecute() {
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

    override fun doInBackground(vararg params: Unit?) =
        try {
            getImagesData()
        } catch (e: Exception) {
            if (ZPhotoHelp.getInstance().isDebug()) e.printStackTrace()
            HashMap<String, ArrayList<ImageDetail>>()
        }

    override fun onPostExecute(result: HashMap<String, ArrayList<ImageDetail>>) {
        dialog?.dismiss()
        dialog = null
        softReference.clear()
        listener(result)
    }

    private fun getImagesData() = HashMap<String, ArrayList<ImageDetail>>().apply {
        // 指定uri
        val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        // 查询指定的列
        val projImage = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DISPLAY_NAME
        )

        // 指定格式
        val showGif = ZPhotoHelp.getInstance().getShowGif()

        val whereArgs = if (showGif) arrayOf("image/$JPEG", "image/$PNG", "image/$JPG", "image/$GIF")
        else arrayOf("image/$JPEG", "image/$PNG", "image/$JPG")

        val where = if (showGif) (MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=?")
        else (MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=?")

        // 排序方式
        val sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc"

        val cursor = softReference.get()!!.contentResolver.query(
            imageUri,
            projImage,
            where,
            whereArgs,
            sortOrder
        )

        val list = ArrayList<ImageDetail>()

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // 图片的路径
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                // 图片的大小
                val size = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.SIZE)).toLong()
                // 图片的名称
                var displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))

                if (displayName.isNullOrEmpty()) {
                    displayName = path.substring(path.indexOf("."), path.length)
                }

                list.add(
                    ImageDetail(
                        path,
                        displayName,
                        ZFile.formetFileSize(size, ZFile.SIZETYPE_MB),
                        checkGif(path),
                        "",
                        0L
                    )
                )

                // 获取该图片的父路径名
                val dirPath = File(path).parentFile.absolutePath
                // 存储对应关系
                if (this.containsKey(dirPath)) {
                    // 取出相对应的 List
                    val data = this[dirPath]
                    data?.add(
                        ImageDetail(
                            path,
                            displayName,
                            ZFile.formetFileSize(size, ZFile.SIZETYPE_MB),
                            checkGif(path),
                            "",
                            0L
                        )
                    )
                    continue
                } else {
                    val data = ArrayList<ImageDetail>()
                    data.add(
                        ImageDetail(
                            path,
                            displayName,
                            ZFile.formetFileSize(size, ZFile.SIZETYPE_MB),
                            checkGif(path),
                            "",
                            0L
                        )
                    )
                    this[dirPath] = data
                }
            }
            cursor.close()
        }

    }

    /**
     * 读取手机中所有图片信息
     */
    private fun getAllPhotoInfo() {
        val list = ArrayList<ImageDetail>()
        val allPhotosMap = HashMap<String, ArrayList<ImageDetail>>() // 所有照片

        // 指定uri
        val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        // 查询指定的列
        val projImage = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DISPLAY_NAME
        )

        // 指定格式
        val showGif = ZPhotoHelp.getInstance().getShowGif()

        val whereArgs = if (showGif) arrayOf("image/$JPEG", "image/$PNG", "image/$JPG", "image/$GIF")
        else arrayOf("image/$JPEG", "image/$PNG", "image/$JPG")

        val where = if (showGif) (MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=?")
        else (MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=?")

        // 排序方式
        val sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc"

        val cursor = softReference.get()!!.contentResolver.query(
            imageUri,
            projImage,
            where,
            whereArgs,
            sortOrder
        ) ?: return

        while (cursor.moveToNext()) {
            // 获取图片的路径
            val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            val size = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.SIZE)).toLong()
            val displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
            // 用于展示相册初始化界面
            list.add(
                ImageDetail(
                    path,
                    displayName,
                    ZFile.formetFileSize(size, ZFile.SIZETYPE_MB),
                    checkGif(path),
                    "",
                    0L
                )
            )

            // 获取该图片的父路径名
            val dirPath = File(path).parentFile.absolutePath
            // 存储对应关系
            if (allPhotosMap.containsKey(dirPath)) {
                // 取出相对应的 List
                val data = allPhotosMap[dirPath]
                data?.add(
                    ImageDetail(
                        path,
                        displayName,
                        ZFile.formetFileSize(size, ZFile.SIZETYPE_MB),
                        checkGif(path),
                        "",
                        0L
                    )
                )
                continue
            } else {
                val data = ArrayList<ImageDetail>()
                data.add(
                    ImageDetail(
                        path,
                        displayName,
                        ZFile.formetFileSize(size, ZFile.SIZETYPE_MB),
                        checkGif(path),
                        "",
                        0L
                    )
                )
                allPhotosMap[dirPath] = data
            }
        }
        cursor.close()
    }

}