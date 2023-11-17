package com.tencent.wmpf.demo.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import com.tencent.luggage.demo.wxapi.DeviceInfo
import com.tencent.mmkv.MMKV
import com.tencent.wmpf.cli.api.WMPF
import com.tencent.wmpf.cli.api.WMPFAccountApi
import com.tencent.wmpf.cli.api.WMPFApiException
import com.tencent.wmpf.cli.model.WMPFStartAppParams
import com.tencent.wmpf.cli.model.WMPFStartAppParams.WMPFAppType
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.utils.WMPFDemoLogger
import com.tencent.wmpf.demo.utils.WMPFDemoUtil.execute

class FastExperienceActivity : AppCompatActivity() {
    private lateinit var logger: WMPFDemoLogger

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fast_experience)

        if (!checkPermission(this)) {
            requestPermission(this)
        }

        logger = WMPFDemoLogger(TAG, this, findViewById(R.id.tv_device_info_resp))

        findViewById<EditText>(R.id.et_app_id).setText(DeviceInfo.APP_ID)


        val appIdView = findViewById<TextView>(R.id.et_launch_app_id)

        val kv = MMKV.mmkvWithID(TAG)
        val savedAppId = kv.getString("appId", "")
        if (!savedAppId.isNullOrBlank()) {
            appIdView.text = savedAppId
        }

        findViewById<Button>(R.id.btn_launch_wxa_app).setOnClickListener {
            val appId = appIdView.text.toString()
            kv.putString("appId", appId)
            execute {
                launchMiniProgram(appId)
            }
        }

        findViewById<Button>(R.id.btn_launch_login).setOnClickListener {
            logger.clear()
            execute {
                try {
                    WMPF.getInstance().accountApi.login(WMPFAccountApi.WMPFLoginUIStyle.FULLSCREEN)
                    logger.i("扫码登录成功")
                } catch (e: WMPFApiException) {
                    logger.e("扫码登录失败", e)
                }
            }
        }

        findViewById<Button>(R.id.btn_launch_wxa_dev_app).setOnClickListener {
            val appId = appIdView.text.toString()
            kv.putString("appId", appId)
            execute {
                launchMiniProgram(appId, WMPFAppType.APP_TYPE_DEV)
            }
        }

        findViewById<Button>(R.id.btn_launch_wxa_pre_app).setOnClickListener {
            val appId = appIdView.text.toString()
            kv.putString("appId", appId)
            execute {
                launchMiniProgram(appId, WMPFAppType.APP_TYPE_EXP)
            }
        }

        findViewById<Button>(R.id.btn_launch_remote_debug).setOnClickListener {
            logger.clear()
            execute {
                try {
                    WMPF.getInstance().miniProgramApi.launchByQRScanCode()
                } catch (e: WMPFApiException) {
                    logger.e("扫码打开小程序失败", e)
                }
            }
        }
    }

    private fun launchMiniProgram(appId: String, type: WMPFAppType = WMPFAppType.APP_TYPE_RELEASE) {
        logger.clear()
        if (type == WMPFAppType.APP_TYPE_EXP || type == WMPFAppType.APP_TYPE_DEV) {
            if (!WMPF.getInstance().accountApi.isLogin) {
                logger.e("启动${if (type === WMPFAppType.APP_TYPE_EXP) "体验" else "开发"}版小程序请先登录")
                return
            }
        }
        logger.i("启动小程序：$appId")
        try {
            WMPF.getInstance().miniProgramApi.launchMiniProgram(
                WMPFStartAppParams(appId, "", type)
            )
        } catch (e: WMPFApiException) {
            logger.e("启动小程序失败", e)
        }
    }

    private fun checkPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val ret0 = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            val ret1 = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val ret2 = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            val ret3 = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            val ret4 = context.checkSelfPermission(Manifest.permission.CAMERA)
            val ret5 = context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
            return ret0 == PackageManager.PERMISSION_GRANTED &&
                    ret1 == PackageManager.PERMISSION_GRANTED &&
                    ret2 == PackageManager.PERMISSION_GRANTED &&
                    ret3 == PackageManager.PERMISSION_GRANTED &&
                    ret4 == PackageManager.PERMISSION_GRANTED &&
                    ret5 == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    private fun requestPermission(context: Activity) {
        try {
            ActivityCompat.requestPermissions(
                context, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_PHONE_STATE
                ),
                0
            )
        } catch (e: Exception) {

        }

    }

    companion object {
        private const val TAG = "FastExperienceActivity"
    }
}
