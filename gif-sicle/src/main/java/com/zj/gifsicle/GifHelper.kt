package com.zj.gifsicle

import android.content.Context
import android.util.Log
import com.blankj.utilcode.util.ShellUtils
import com.blankj.utilcode.util.Utils
import java.io.File
import java.util.*

object GifHelper {

    private const val TAG = "GifHelper"

    /**
     * 压缩GIF
     * @param lossy Int 改变图像颜色以减少输出文件的大小，但代价是失真和噪声。 Lossiness 决定了允许多少工件；较高的值会导致较小的文件大小，但会导致更多的工件。默认损失为 20。
     * @param colorNum Int  将每个输出 GIF 中不同颜色的数量减少到 num 或更少。 Num 必须介于 2 和 256 之间。这可用于缩小输出 GIF 或消除任何本地颜色表。
     * @param outputWidth Int   输出宽度
     * @param outputHeight Int  输出高度
     * @param callback Consumer<CommandResult> 结果回调
     */
    fun compress(
        context: Context, inputFilePath: String,
        outputFilePath: String,
        lossy: Int,
        colorNum: Int,
        outputWidth: Int,
        outputHeight: Int,
        callback: Utils.Consumer<ShellUtils.CommandResult>
    ) {
        val gifsicle = File(
            File(context.applicationInfo.nativeLibraryDir),
            "libgifsicle.so"
        )   //可执行文件地址安装后形如：/data/app/com.equationl.myapplication-wZxpZo7IgVPNv3jvY0S8QA==/lib/arm/libgifsicle.so
        if (!gifsicle.canExecute()) {   //无法执行该执行文件
            Log.e(TAG, "libgifsicle.so can't excute")
        }
        val envp =
            arrayOf("LD_LIBRARY_PATH=" + File(context.applicationInfo.nativeLibraryDir))  //设置环境
        val cmd = String.format(
            Locale.US,
            "%s -V %s --lossy=$lossy --colors $colorNum --resize ${outputWidth}x${outputHeight} -o %s",
            gifsicle.path,
            File(inputFilePath).toString(),
            File(outputFilePath).toString()
        )
        ShellUtils.execCmdAsync(cmd, false, callback)
        Log.i(TAG, "startCustomizeCompress: envp=${envp[0]}\ncmd=$cmd")
    }
}