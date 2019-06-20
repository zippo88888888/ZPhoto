package com.zp.zphoto.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.zp.zphoto.R
import com.zp.zphoto.sample.java_sample.JavaMainActivity
import com.zp.zphoto_lib.common.BaseZPhotoAdapter
import com.zp.zphoto_lib.common.BaseZPhotoHolder
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.content.*
import com.zp.zphoto_lib.util.ZLog
import com.zp.zphoto_lib.util.ZPermission
import com.zp.zphoto_lib.util.ZPhotoUtil
import com.zp.zphoto_lib.util.ZToaster
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity(), ZImageResultListener {

    private var mainAdapter: MainApdater? = null
    private lateinit var config: ZPhotoConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        config = ZPhotoConfiguration()
        main_picCountCV.setValue(ZPHOTO_DEFAULT_MAX_PIC_SELECT)
        main_picSizeCV.setValue(ZPHOTO_DEFAULT_MAX_PIC_SIZE)
        main_videoCountCV.setValue(ZPHOTO_DEFAULT_MAX_VIDEO_SELECT)
        main_videoSizeCV.setValue(ZPHOTO_DEFAULT_MAX_VIDEO_SIZE)

        main_styleGroup.setOnCheckedChangeListener { _, checkedId ->
            config.selectedBoxStyle =  when (checkedId) {
                R.id.main_styleTwoRadio -> ZPHOTO_BOX_STYLE_DIY
                else -> ZPHOTO_BOX_STYLE_DEFAULT
            }
        }

        mainAdapter = MainApdater(this, com.zp.zphoto_lib.R.layout.item_zphoto_select_pic)
        main_recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
            isNestedScrollingEnabled = false
            adapter = mainAdapter
        }

        main_javaUserBtn.setOnClickListener { jumpActivity(JavaMainActivity::class.java) }

        main_photoBtn.setOnClickListener {
            ZPhotoHelp.getInstance()
                .setZImageResultListener(this)
                .setZImageCompress(MyImageCompress())
                .config(getConfig())
                .toPhoto(this)
        }
        main_cameraBtn.setOnClickListener {
            ZPhotoHelp.getInstance()
                .setZImageResultListener(this)
                .setZImageCompress(MyImageCompress())
                .config(getConfig())
                .toCamera(this)
        }
    }

    override fun selectSuccess(list: ArrayList<ZPhotoDetail>?) {
        ZLog.e("选中的数量：${list?.size}")
        mainAdapter?.setDatas(list)
        ZLog.e("当前缓存大小：${ZPhotoHelp.getInstance().getZPhotoCacheSize()}MB")

    }

    override fun selectFailure() {
        ZToaster.makeText("不能够获取图片信息", ZToaster.C)
    }

    override fun selectCancel() {
        ZToaster.makeTextS("用户取消")
    }

    private fun getConfig() = config.apply {
        showGif = main_gifBox.isChecked
        needCrop = main_cutBox.isChecked
        needCompress = main_compressBox.isChecked
        showVideo = main_videoBox.isChecked
        allSelect = main_allSelectBox.isChecked
        showCamera = main_cameraBox.isChecked

        maxPicSelect = main_picCountCV.getValue()
        maxPicSize = main_picSizeCV.getValue()

        maxVideoSelect = main_videoCountCV.getValue()
        maxVideoSize = main_videoSizeCV.getValue()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ZPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ZPhotoHelp.getInstance().onActivityResult(requestCode, resultCode, data, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ZPhotoHelp.getInstance().reset()
    }

    class MainApdater(context: Context, resID: Int) : BaseZPhotoAdapter<ZPhotoDetail>(context, resID) {
        private var wh = 0
        init {
            wh = (context.getDisplay()[0] - dip2px(1f) * 2 * 3) / 3
        }
        override fun bindView(holder: BaseZPhotoHolder, item: ZPhotoDetail, position: Int) {
            holder.apply {
                val durationTxt = getView<TextView>(com.zp.zphoto_lib.R.id.item_zphoto_select_videoDurationiTxt)
                setVisibility(com.zp.zphoto_lib.R.id.item_zphoto_select_box, false)
                val pic = getView<ImageView>(com.zp.zphoto_lib.R.id.item_zphoto_select_pic)
                if (item.isVideo) {
                    durationTxt.visibility = View.VISIBLE
                    durationTxt.text = ZPhotoUtil.videoDurationFormat(item.duration)
                } else {
                    durationTxt.visibility = View.GONE
                }

                pic.apply {
                    layoutParams = FrameLayout.LayoutParams(wh, wh).apply {
                        gravity = Gravity.CENTER
                    }
                    ZPhotoHelp.getInstance().getImageLoaderListener().loadImg(this, File(item.path))
                }
            }
        }

    }
}
