package com.zj.gifsicle

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.CleanUtils
import com.blankj.utilcode.util.ConvertUtils
import com.google.android.material.slider.Slider
import com.luck.picture.lib.entity.LocalMedia
import com.zj.gifsicle.databinding.ActivityCompressBinding
import pl.droidsonroids.gif.GifDrawable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import kotlin.math.roundToInt

class CompressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompressBinding
    private lateinit var localMedia: LocalMedia

    private var lossy = 0
    private var colorNum = 256

    private var rawWidth = 0
    private var rawHeight = 0
    private var currentWidth: Int = 0
    private var currentHeight: Int = 0
    private lateinit var currentFilePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompressBinding.inflate(layoutInflater)
        supportActionBar?.run {
            title = "GIF压缩"
            setDisplayHomeAsUpEnabled(true)
        }
        setContentView(binding.root)

        localMedia = intent.getParcelableExtra("local_media") ?: return
        rawWidth = localMedia.width
        rawHeight = localMedia.height
        currentWidth = rawWidth
        currentHeight = rawHeight
        currentFilePath = localMedia.realPath
        val rawGifSizeText =
            "原GIF文件大小: ${formatFileSize(localMedia.realPath)}"
        updateUi(localMedia.realPath)
        with(binding) {
            rawGifSize.text = rawGifSizeText
            colorNumSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {

                }

                override fun onStopTrackingTouch(slider: Slider) {
                    val newColor = slider.value
                    adjustColorNum(newColor)
                }
            })
            colorNumSlider.addOnChangeListener { _, value, _ ->
                val currentColorNumText = "当前GIF颜色数量: ${value.roundToInt()}"
                currentColorNum.text = currentColorNumText
            }
            lossySlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {

                }

                override fun onStopTrackingTouch(slider: Slider) {
                    val newLossy = slider.value
                    adjustLossy(newLossy)
                }
            })
            lossySlider.addOnChangeListener { _, value, _ ->
                val currentLossyText = "当前GIF有损值: ${value.roundToInt()}"
                currentLossy.text = currentLossyText
            }
            resolutionSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {

                }

                override fun onStopTrackingTouch(slider: Slider) {
                    adjustResolution(slider.value)
                }
            })
            resolutionSlider.addOnChangeListener { _, value, _ ->
                currentWidth = (rawWidth * value).roundToInt()
                currentHeight = (rawHeight * value).roundToInt()
                val currentResolutionText = "当前GIF分辨率: ${currentWidth}x${currentHeight}"
                currentResolution.text = currentResolutionText
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.compress, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.export) {
            export()
        } else if (item.itemId == android.R.id.home) {
            CleanUtils.cleanExternalCache()
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun adjustColorNum(newColor: Float) {
        colorNum = newColor.roundToInt()
        binding.loadingView.visibility = View.VISIBLE
        val inputFilePath = localMedia.realPath
        val outputFilePath = File(externalCacheDir, "${UUID.randomUUID()}.gif").absolutePath
        GifHelper.compress(
            this,
            inputFilePath,
            outputFilePath,
            lossy,
            colorNum,
            currentWidth,
            currentHeight
        ) {
            updateUi(outputFilePath)
        }
    }

    private fun adjustLossy(newLossy: Float) {
        binding.loadingView.visibility = View.VISIBLE
        lossy = newLossy.roundToInt()
        val inputFilePath = localMedia.realPath
        val outputFilePath = File(externalCacheDir, "${UUID.randomUUID()}.gif").absolutePath
        GifHelper.compress(
            this,
            inputFilePath,
            outputFilePath,
            lossy,
            colorNum,
            currentWidth,
            currentHeight
        ) {
            updateUi(outputFilePath)
        }
    }

    private fun adjustResolution(newAspectRatio: Float) {
        binding.loadingView.visibility = View.VISIBLE
        currentWidth = (rawWidth * newAspectRatio).roundToInt()
        currentHeight = (rawHeight * newAspectRatio).roundToInt()

        val inputFilePath = localMedia.realPath
        val outputFilePath = File(externalCacheDir, "${UUID.randomUUID()}.gif").absolutePath
        GifHelper.compress(
            this,
            inputFilePath,
            outputFilePath,
            lossy,
            colorNum,
            currentWidth,
            currentHeight
        ) {
            updateUi(outputFilePath)
        }
    }

    private fun updateUi(outputPath: String) {
        currentFilePath = outputPath
        val currentGifSizeText =
            "当前GIF文件大小: ${formatFileSize(outputPath)}"
        val currentColorNumText = "当前GIF颜色数量: $colorNum"
        val currentLossyText = "当前GIF有损值: $lossy"
        val currentResolutionText = "当前GIF分辨率: ${currentWidth}x${currentHeight}"
        val currentGifDrawable = GifDrawable(outputPath)
        with(binding) {
            currentGifSize.text = currentGifSizeText
            currentColorNum.text = currentColorNumText
            currentLossy.text = currentLossyText
            currentResolution.text = currentResolutionText
            gifImageView.setImageDrawable(currentGifDrawable)
            binding.loadingView.visibility = View.GONE
        }
    }

    private fun export() {
        val gifCollection =
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${UUID.randomUUID()}.gif")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/gif")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val gifContentUri = contentResolver.insert(gifCollection, contentValues) ?: return
        contentResolver.openFileDescriptor(gifContentUri, "w", null)?.use { pfd ->
            FileOutputStream(pfd.fileDescriptor).use { fos ->
                FileInputStream(File(currentFilePath)).use { fis ->
                    fis.copyTo(fos)
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            contentResolver.update(gifContentUri, contentValues, null, null)
        }
        val cleanSuccess = CleanUtils.cleanExternalCache()
        Log.d("GifCompress", "cleanSuccess = $cleanSuccess")
        Toast.makeText(this, "导出成功", Toast.LENGTH_SHORT).show()
    }

    private fun formatFileSize(path: String): String {
        val fileLength = File(path).length()
        return ConvertUtils.byte2FitMemorySize(fileLength, 1)
    }

    companion object {
        fun start(activity: Activity, localMedia: LocalMedia) {
            val intent = Intent(activity, CompressActivity::class.java)
            intent.putExtra("local_media", localMedia)
            activity.startActivity(intent)
        }
    }
}