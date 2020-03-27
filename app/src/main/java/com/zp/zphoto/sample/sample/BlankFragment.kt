package com.zp.zphoto.sample.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.zp.zphoto.R
import com.zp.zphoto_lib.common.ZPhotoHelp
import com.zp.zphoto_lib.content.ZImageResultListener
import com.zp.zphoto_lib.content.ZPhotoConfiguration
import com.zp.zphoto_lib.content.ZPhotoDetail
import kotlinx.android.synthetic.main.fragment_blank.*
import java.lang.StringBuilder
import java.util.ArrayList

class BlankFragment : Fragment(), ZImageResultListener {

    private lateinit var config: ZPhotoConfiguration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        config = ZPhotoConfiguration()

        fragment_to_photoBtn.setOnClickListener {
            ZPhotoHelp.getInstance()
                .setZImageResultListener(this)
                .config(getConfig())
                .toPhoto(this)
        }

        fragment_to_cameraBtn.setOnClickListener {
            ZPhotoHelp.getInstance()
                .setZImageResultListener(this)
                .config(getConfig())
                .toCamera(this)
        }

    }

    override fun selectSuccess(list: ArrayList<ZPhotoDetail>?) {
        fragment_resultTxt.text = StringBuilder().run {
            list?.forEach {
                this.append(it.toString()).append("\n\n")
            }
            this.toString()
        }
    }

    override fun selectFailure() {
        Log.e("fragment", "Failure")
    }

    override fun selectCancel() {
        Log.i("fragment", "Cancel")
    }

    private fun getConfig() = config.apply {
        allSelect = true
        showVideo = true
        authority = "com.zp.zphoto.FileProvider"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ZPhotoHelp.getInstance().onActivityResult(requestCode, resultCode, data, this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ZPhotoHelp.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ZPhotoHelp.getInstance().reset()
    }

}
