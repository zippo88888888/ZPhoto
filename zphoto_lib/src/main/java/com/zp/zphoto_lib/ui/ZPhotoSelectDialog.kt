package com.zp.zphoto_lib.ui

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.design.widget.BottomSheetDialog
import com.zp.zphoto_lib.R
import kotlinx.android.synthetic.main.dialog_zphoto_select.*


class ZPhotoSelectDialog : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.dialog_zphoto_select, container)!!

    override fun onCreateDialog(savedInstanceState: Bundle?) = BottomSheetDialog(context!!)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog_zphoto_select_recyclerView.apply {

        }
    }

}