package com.zp.zphoto.sample.java_sample;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.zp.zphoto.R;
import com.zp.zphoto_lib.common.ZPhotoHelp;
import com.zp.zphoto_lib.content.ZImageResultListener;
import com.zp.zphoto_lib.content.ZPhotoConfiguration;
import com.zp.zphoto_lib.content.ZPhotoDetail;
import com.zp.zphoto_lib.util.ZPermission;
import com.zp.zphoto_lib.util.ZToaster;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class JavaMainActivity extends AppCompatActivity implements ZImageResultListener {

    private TextView resultTxt;
    // 拍照保存的地址：为空使用默认路径
    private String outUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java_main);
        resultTxt = findViewById(R.id.java_main_txt);
    }

    public void toCamera(View view) {
        ZPhotoHelp.getInstance()
                .setZImageResultListener(this)
                .config(getConfig())
                .toCamera(this, outUri);
    }

    public void toPhoto(View view) {
        ZPhotoHelp.getInstance()
                .setZImageResultListener(this)
                .config(getConfig())
                .toPhoto(this);
    }

    private ZPhotoConfiguration getConfig() {
        ZPhotoConfiguration.Builder builder = new ZPhotoConfiguration.Builder();
        builder.allSelect(true).showVideo(true).maxVideoSelect(9).maxPicSelect(9);
        return builder.builder();
    }

    @Override
    public void selectSuccess(@Nullable ArrayList<ZPhotoDetail> list) {
        if (list != null && list.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (ZPhotoDetail item : list) {
                sb.append(item).append("\n\n");
            }
            resultTxt.setText(sb.toString());
        }
    }

    @Override
    public void selectFailure() {
        Log.e("java", "Failure");
    }

    @Override
    public void selectCancel() {
        Log.i("java", "Cancel");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ZPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, this, outUri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ZPhotoHelp.getInstance().onActivityResult(requestCode, resultCode, data, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZPhotoHelp.getInstance().reset();
    }
}
