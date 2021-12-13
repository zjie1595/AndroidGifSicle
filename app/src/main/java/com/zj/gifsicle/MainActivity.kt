package com.zj.gifsicle

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun selectGif(view: android.view.View) {
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofImage())
            .isCamera(false)
            .isGif(true)
            .selectionMode(PictureConfig.SINGLE)
            .imageEngine(GlideEngine.createGlideEngine())
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: List<LocalMedia>) {
                    val intent = Intent(this@MainActivity, CompressActivity::class.java)
                    intent.putExtra("local_media", result[0])
                    startActivity(intent)
                }

                override fun onCancel() {
                    // onCancel Callback
                }
            })
    }
}