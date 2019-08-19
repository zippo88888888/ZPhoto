package com.zp.zphoto_lib.util

import android.app.ProgressDialog
import android.content.Context
import android.database.Cursor
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.content.*
import java.io.File
import java.lang.ref.SoftReference
import java.util.*
import kotlin.collections.ArrayList

/**
 * LoaderManager
 * 优化代码
 */
class ZPhotoImageAnsy(
    private var context: Context,
    private var listener: (dirs: ArrayList<ZPhotoFolder>, pics: ArrayList<ZPhotoDetail>) -> Unit,
    private var isShowLoading: Boolean = true
) {

    private var handler: ImageHandler? = null
    private var dialog: ProgressDialog? = null

    fun start() {
        if (handler == null) {
            handler = ImageHandler(this)
        }
        if (isShowLoading) {
            dialog = ProgressDialog(context).run {
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                setMessage("读取相册数据，请稍后...")
                setCancelable(false)
                this
            }
            dialog?.show()
        }
        ImageAnsy().start()
    }

    private inner class ImageAnsy : Thread() {

        override fun run() {
            // 所有数据
            val allList = ArrayList<ZPhotoDetail>()
            // 文件夹
            val dirs = ArrayList<ZPhotoFolder>()
            // 视频数据
            var videoList: ArrayList<ZPhotoDetail>? = null

            // 源图片数据
            val data = getImagesData()
            val showVideo = ZPhotoHelp.getInstance().getConfiguration().showVideo
            if (showVideo) {
                videoList = getVideoList()
                allList.addAll(videoList)
            }
            for ((k, v) in data) {
                // 添加到文件夹数据
                dirs.add(ZPhotoFolder(k, v[0].path, k.substring(k.lastIndexOf("/") + 1, k.length), v))
                // 添加到所有数据
                allList.addAll(v)
            }
            // 文件夹排序
            dirs.sortByDescending { it.childs[0].date_modified }
            if (!videoList.isNullOrEmpty()) {
                // 文件夹添加视频
                dirs.add(0, ZPhotoFolder(Z_ALL_VIDEO_KEY, videoList[0].path, "全部视频", videoList))
            }
            // 所有排序
            allList.sortByDescending { it.date_modified }
            val allFirstPath = allList[0].path
            // 将所有数据 添加 到文件夹数据

            // 是否要显示拍照图片
            val showCamera = ZPhotoHelp.getInstance().getConfiguration().showCamera
            if (showCamera) {
                allList.add(0, ZPhotoDetail("", ZPHOTO_SHOW_CAMEAR, 0.0,
                    false, false, 0, "", 0L))
            }
            dirs.add(0, ZPhotoFolder(Z_ALL_DATA_KEY, allFirstPath, "所有图片", allList))

            handler?.sendMessage(Message().apply {
                obj = arrayOf(dirs, allList)
                what = 0
            })
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

                val showGif = ZPhotoHelp.getInstance().getConfiguration().showGif
                val minPicSize = ZPhotoHelp.getInstance().getConfiguration().minPicSize

                // 指定格式
                val whereArgsArray = ArrayList<String>().run {
                    add("image/$JPEG")
                    add("image/$PNG")
                    add("image/$JPG")
                    if (showGif) add("image/$GIF")
                    add(" $minPicSize ")
                    this
                }

                // 查询条件
                val whereStr = StringBuilder().run {
                    val size = if (showGif) 4 else 3
                    append(" ( ")
                    for (i in 0 until size) {
                        append(MediaStore.Images.Media.MIME_TYPE)
                        if (i == size - 1) append("=?") else append(" =? OR ")
                    }
                    append(" ) AND ").append(MediaStore.MediaColumns.SIZE + ">?")
                    this.toString()
                }

                val whereArgs = arrayOfNulls<String>(whereArgsArray.size)
                whereArgsArray.toArray(whereArgs)

                // 排序方式
                val sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC"

                cursor = context.contentResolver.query(imageUri, projImage, whereStr, whereArgs, sortOrder)

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
                                ZFile.formetFileSize(size),
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
                                    ZFile.formetFileSize(size),
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
                                    ZFile.formetFileSize(size),
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
                val minVideoSize = ZPhotoHelp.getInstance().getConfiguration().minVideoSize
                val where = (MediaStore.Images.Media.MIME_TYPE + "=? AND " + MediaStore.Images.Media.SIZE + ">?")
                val whereArgs = arrayOf("video/$MP4", " $minVideoSize ")

                cursor = context.contentResolver?.query(
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
                                ZFile.formetFileSize(size),
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

    private class ImageHandler(ansy: ZPhotoImageAnsy) : Handler() {

        private val softReference by lazy {
            SoftReference<ZPhotoImageAnsy>(ansy)
        }

        override fun handleMessage(msg: Message?) {
            val ansy = softReference.get()
            if (ansy != null && msg != null) {
                val obj = msg.obj as Array<Any>
                val dirs = obj[0] as ArrayList<ZPhotoFolder>
                val allList = obj[1] as ArrayList<ZPhotoDetail>
                ansy.dialog!!.dismiss()
                ansy.listener.invoke(dirs, allList)
            }
        }
    }

}