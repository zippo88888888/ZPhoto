package com.zp.zphoto_lib.content

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


/**
 * 一级文件夹实体类
 * @param dirPath           图片文件夹的路径
 * @param firstImagePath    第一张图片的路径
 * @param folderName        文件夹的名称
 * @param childs            该文件夹包含的图片
 */
@Parcelize
data class ZPhotoFolder(
        var dirPath: String,
        var firstImagePath: String,
        var folderName: String,
        var childs: ArrayList<ZPhotoDetail>
) : Parcelable

/**
 * 二级图片、视频实体类
 * @param path                  路径
 * @param name                  名称
 * @param size                  大小
 * @param isGif                 是否是Gif
 * @param isVideo               是否是视频
 * @param duration              视频时长
 * @param parentPath            父类路径
 * @param date_modified         最后修改时间
 */
@Parcelize
data class ZPhotoDetail(
        var path: String,
        var name: String,
        var size: Double,
        var isGif: Boolean,
        var isVideo: Boolean,
        var duration: Int,
        var parentPath: String,
        var date_modified: Long
) : Parcelable {

        /*constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readDouble(),
                parcel.readByte() != 0.toByte(),
                parcel.readByte() != 0.toByte(),
                parcel.readInt(),
                parcel.readString(),
                parcel.readLong()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {

        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<ZPhotoDetail> {
                override fun createFromParcel(parcel: Parcel): ZPhotoDetail {
                        return ZPhotoDetail(parcel)
                }

                override fun newArray(size: Int): Array<ZPhotoDetail?> {
                        return arrayOfNulls(size)
                }
        }*/

}
