
[![Travis](https://img.shields.io/badge/ZPhoto-1.2-yellowgreen.svg)](https://github.com/zippo88888888/ZPhoto)
[![Travis](https://img.shields.io/badge/API-18%2B-green.svg)](https://github.com/zippo88888888/ZPhoto)
[![Travis](https://img.shields.io/badge/Apache-2.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)


有很多优秀的图片选择框架，但是大部分集成了许多第三方框架，对我来说不简洁！貌似还没看到用KT写的（Kotlin是这世上最好的语言<img src="http://www.fakutownee.cn/d/file/p/2017-07-21/bbd7df2efc2d550fc7ce84da03a72ae8.jpg" width=15px height=15px>）果断撸一个...<br><br>
**↓↓↓站在巨人的肩部上，你绝对能看得更远 鸣谢↓↓↓**<br>
[RxGalleryFinal](https://github.com/FinalTeam/RxGalleryFinal)&nbsp;&nbsp;&nbsp;&nbsp;
[android-crop](https://github.com/jdamcd/android-crop)&nbsp;&nbsp;&nbsp;&nbsp;
[JiaoZiVideoPlayer](https://github.com/lipangit/JiaoZiVideoPlayer)

<br>本库基于之前公司已完成的两个项目，基本上没啥大问题...

## 本库特点

1. **引用最基本的v4，v7，cardview，design官方库和基于 android-crop修改后的图片剪裁外无其他第三方库[查看lib gradle配置](https://github.com/zippo88888888/ZPhoto/blob/master/zphoto_lib/build.gradle)**；
2. 支持视频、图片、GIF查看，图片裁剪，压缩；
3. 支持图片、视频 --->>> 单选、多选&&数量、大小限制；
4. 支持样式自定义；


## 未来
1. 视频裁剪（核心代码--->>>[ZPhotoSuperVideoPlayer](https://github.com/zippo88888888/ZPhoto/blob/master/zphoto_lib/src/main/java/com/zp/zphoto_lib/ui/view/ZPhotoSuperVideoPlayer.kt)）


## 截图
<img src = "app/src/main/assets/ys1.jpg" width = 300px><br><br>

## 使用

Step 0. 添加依赖

gradle
```
implementation 'com.github.zp:zphoto_lib:1.2'
```

maven
```xml
<dependency>
	<groupId>com.github.zp</groupId>
	<artifactId>zphoto_lib</artifactId>
	<version>1.2</version>
</dependency>
```

或 aar --> [点击下载](https://github.com/zippo88888888/ZPhoto/blob/master/app/src/main/assets/zphoto_lib-1.2.aar)

**↓↓↓不要忘记权限↓↓↓**
``` xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
```

Step 1.  新建图片加载，继承自ZImageLoaderListener，实现自己的图片加载方式（以Glide为例）
``` kotlin
class MyImageLoaderListener : ZImageLoaderListener {

    override fun loadImg(imageView: ImageView, file: File) {
        loadImg(file, imageView)
    }

    override fun loadImg(imageView: ImageView, path: String) {
        loadImg(path, imageView, 0)
    }

    override fun loadImg(imageView: ImageView, res: Int) {
        loadImg(res, imageView)
    }

    /**
     * 加载 网络 路径图片
     */
    private fun loadImg(url: String, pic: ImageView, defaultPic: Int = 0) {
        var defaultPic = defaultPic
        if (defaultPic <= 0) {
            defaultPic = R.drawable.loading_pic
        }
        Glide.with(pic.context).load(url).asBitmap()
                .placeholder(defaultPic)
                .error(defaultPic)
                .dontAnimate() // 可以防止图片变形
                .into(pic)
    }

    /**
     * 加载 资源文件 路径图片
     */
    private fun loadImg(resID: Int, pic: ImageView) {
        Glide.with(pic.context)
                .load(resID)
                .dontAnimate()
                .into(pic)
    }

    /**
     * 加载 file 图片
     */
    private fun loadImg(file: File, pic: ImageView) {
        loadGifImg(file, pic)
    }

    /**
     * 加载Gif图
     */
    private fun loadGifImg(file: File, pic: ImageView) {
        val load = Glide.with(pic.context).load(file)
        if (checkGif(file.path)) {
            load.asGif()
                .placeholder(R.drawable.loading_pic)
                .error(R.drawable.loading_pic_error)
                .into(pic)
        } else { // 万一不是Gif图的处理
            load.asBitmap()
                .placeholder(R.drawable.loading_pic)
                .error(R.drawable.loading_pic_error)
                .dontAnimate()
                .into(pic)
        }
    }
}
```
Step 2. 在Application中初始化
``` kotlin
ZPhotoHelp.getInstance().init(this, MyImageLoaderListener())
```
Step 3. Activity 配置 实现 ZImageResultListener 接口，用于数据接收
``` kotlin

  // 图片选择成功
  override fun selectSuccess(list: ArrayList<ZPhotoDetail>?) {
        ZLog.e("选中的数量：${list?.size}")
    }

   // 图片选择失败
    override fun selectFailure() {
        ZToaster.makeText("不能够获取图片信息", ZToaster.C)
    }
    
    // 用户取消
    override fun selectCancel() {
        ZToaster.makeTextS("用户取消")
    }

  // 权限处理
  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ZPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
    // 相机拍照处理
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ZPhotoHelp.getInstance().onActivityResult(requestCode, resultCode, data, this)
    }


```
Step 4. 配置 FileProvider [详情戳我](http://yifeng.studio/2017/05/03/android-7-0-compat-fileprovider)
``` xml

<!-- 新建paths文件，如果已有，修改即可  -->
   <paths>
      <external-path
        name="z_photo_path"
        path="." />

  </paths>
    

    <!-- 在AndroidMainfest  -->
     <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="${applicationId}.FileProvider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/your xml" />
        </provider>  
        
```
``` kotlin

    // 配置ZPhotoConfiguration，里面包含了是否显示gif，视频等属性
    private fun getConfig() = ZPhotoConfiguration().apply {
        ... 
        // 具体请以自己的 authorities 为准
        authority = "your package name.FileProvider"
    }
    
```
Step 5. 使用
``` kotlin
      // 去相册
      main_photoBtn.setOnClickListener {
            ZPhotoHelp.getInstance()
                .setZImageResultListener(this)
                .config(getConfig()) // 配置信息 具体请查看 ZPhotoConfiguration
                .toPhoto(this)
        }
        // 去相机
        main_cameraBtn.setOnClickListener {
            ZPhotoHelp.getInstance()
                .setZImageResultListener(this)
                .config(getConfig()) // 配置信息 
                .toCamera(this)
        }
        
```
Step 6. 释放资源
``` kotlin
    
    // 及时释放
    override fun onDestroy() {
        super.onDestroy()
        ZPhotoHelp.getInstance().reset()
    }

```

**由于本库没有引用其他压缩库，但是已经将方法暴露出去了，所以压缩需要自己实现**

## 图片压缩
Step 1. 新建图片压缩，继承自ZImageCompress，实现压缩方法（以Luban为例）
``` kotlin
 class MyImageCompress : ZImageCompress() {

    private var dialog: ProgressDialog? = null

    override fun onPreExecute() {
        super.onPreExecute()
        dialog = ProgressDialog(softReference?.get()).run {
            setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER)
            setMessage("图片处理中")
            setCancelable(false)
            this
        }
        dialog?.show()
    }

    override fun doingCompressImage(arrayList: ArrayList<ZPhotoDetail>?): ArrayList<ZPhotoDetail>? {
        if (arrayList == null || softReference?.get() == null) {
            return ArrayList()
        }

        val list = ArrayList<File>()
        arrayList.forEach { list.add(File(it.path)) }

        val outDir = ZFile.getPathForPath(ZFile.PHOTO)

        val compactList = Luban.with(softReference?.get())
            .load(list)
            .ignoreBy(50)       // 小于50K不压缩
            .setTargetDir(outDir)    // 压缩后图片的路径
            .filter { filePath ->   // 设置压缩条件 gif、视频 不压缩
                !(TextUtils.isEmpty(filePath) ||
                        filePath.toLowerCase().endsWith(".$GIF") ||
                        filePath.toLowerCase().endsWith(".$MP4"))
            }.get()

        arrayList.indices.forEach {
            val path = compactList[it].path
            val size = ZFile.getFileOrFilesSize(path, ZFile.SIZETYPE_MB)
            Log.e("压缩图片", "原图大小：${arrayList[it].size}M <<<===>>>处理后的大小：${size}M")
            arrayList[it].path = path
            arrayList[it].parentPath = ""
            arrayList[it].size = size
            arrayList[it].isGif = checkGif(path)
        }
        return arrayList
    }

    override fun onPostExecute(list: ArrayList<ZPhotoDetail>?) {
        super.onPostExecute(list)
        dialog?.dismiss()
    }
}
```
Step 2. 使用
```kotlin
        ZPhotoHelp.getInstance()
                .setZImageResultListener(this)
                .setZImageCompress(MyImageCompress())
                .config(getConfig())
                .toCamera(this)
```

## 关于自定义<br>

**样式**<br>
ZPhoto_BaseTheme（activity 主题） <br>
ZPhoto_ToolbarTheme<br>
ZPhoto_Toolbar_TitleStyle<br><br>

**颜色**<br>
zphoto_baseColor  主体色<br>
zphoto_tool_bar_txt_color 标题颜色<br>
zphoto_red<br>
...<br><br>

**提示文字**<br>
```xml
<!-- lib -->
<string name="zphoto_video_size_tip">视频最大可选取 %1$d M</string>
<string name="zphoto_video_count_tip">视频最多可选 %1$d 个</string>
<string name="zphoto_pic_size_tip">图片最大可选取 %1$d M</string>
<string name="zphoto_pic_count_tip">图片最多可选 %1$d 张</string>	

<!-- 自定义的 %1$d 占位符必须要 -->
<string name="zphoto_pic_count_tip">bilibili( ゜- ゜)つロ 干杯 亲亲 图片最多能选 %1$d 张  bilibili( ゜- ゜)つロ 干杯</string>
...
```

搞定^_^ 如果觉得可以 star 一下哦

**↓↓↓再次鸣谢↓↓↓**<br>
[RxGalleryFinal](https://github.com/FinalTeam/RxGalleryFinal)&nbsp;&nbsp;&nbsp;&nbsp;
[android-crop](https://github.com/jdamcd/android-crop)&nbsp;&nbsp;&nbsp;&nbsp;
[JiaoZiVideoPlayer](https://github.com/lipangit/JiaoZiVideoPlayer)

