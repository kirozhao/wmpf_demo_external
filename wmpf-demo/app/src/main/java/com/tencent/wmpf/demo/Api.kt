@file:Suppress("SpellCheckingInspection")

package com.tencent.wmpf.demo

import android.app.Application
import android.util.Log
import com.tencent.luggage.demo.wxapi.DeviceInfo
import com.tencent.mm.ipcinvoker.IPCInvokeCallbackEx
import com.tencent.wmpf.app.WMPFBoot
import com.tencent.wmpf.cli.task.*
import com.tencent.wmpf.cli.task.pb.WMPFBaseRequestHelper
import com.tencent.wmpf.cli.task.pb.WMPFIPCInvoker
import com.tencent.wmpf.cli.task.pb.proto.WMPFResponse
import com.tencent.wmpf.proto.*
import com.tencent.wmpf.utils.WMPFHelper
import io.reactivex.Observable
import io.reactivex.Single

object Api {

    private const val TAG = "Demo.Api"

    private fun isSuccess(response: WMPFResponse): Boolean {
        return response != null && response.baseResponse.errCode == TaskError.ErrType_OK
    }

    fun init(context: Application) {
        WMPFBoot.init(context)
        val invokeToken = getInvokeToken()
        WMPFIPCInvoker.initInvokeToken(invokeToken)
    }

    private fun getInvokeToken(): String {
        if (WMPFBoot.getAppContext() == null) {
            throw java.lang.Exception("need invoke Api.Init")
        }
        val pref = WMPFBoot.getAppContext()!!.getSharedPreferences("InvokeTokenHelper", 0)
        return pref?.getString(TAG, "")!!
    }

    private fun initInvokeToken(invokeToken: String) {
        if (WMPFBoot.getAppContext() == null) {
            throw java.lang.Exception("need invoke Api.Init")
        }
        val pref = WMPFBoot.getAppContext()!!.getSharedPreferences("InvokeTokenHelper", 0)
        val editor = pref?.edit()
        editor?.putString(TAG, invokeToken)?.apply()
        WMPFIPCInvoker.initInvokeToken(invokeToken)
    }

    private fun createTaskError(response: WMPFResponse?): TaskError {
        if (response == null) {
            return TaskError(TaskError.ErrType_NORMAL, -1, "response is null")
        }
        return TaskError(response.baseResponse.errType, response.baseResponse.errCode, response.baseResponse.errMsg)
    }

    class TaskErrorException(val taskError: TaskError): java.lang.Exception() {
        override fun toString(): String {
            return "TaskErrorException(taskError=$taskError)"
        }
    }

    fun activateDevice(productId: Int, keyVerion: Int,
                       deviceId: String, signature: String, hostAppId: String): Single<WMPFActivateDeviceResponse> {
        return Single.create {
            Log.i(TAG, "activateDevice: isInProductionEnv = " + DeviceInfo.isInProductionEnv)
            val request = WMPFActivateDeviceRequest().apply {
                this.baseRequest = WMPFBaseRequestHelper.checked()
                this.productId = productId
                this.keyVersion = keyVerion
                this.deviceId = deviceId
                this.signature = signature.replace(Regex("[\t\r\n]"), "")
                this.hostAppId = hostAppId
            }

            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_ActivateDevice, WMPFActivateDeviceRequest, WMPFActivateDeviceResponse>(
                    request,
                    IPCInvokerTask_ActivateDevice::class.java,
                    object : IPCInvokeCallbackEx<WMPFActivateDeviceResponse> {
                        override fun onBridgeNotFound() {
                            it.onError(Exception("bridge not found"))
                        }

                        override fun onCallback(response: WMPFActivateDeviceResponse) {
                            if (isSuccess(response)) {
                                if (response != null && !response.invokeToken.isNullOrEmpty()) {
                                    initInvokeToken(response.invokeToken)
                                }

                                it.onSuccess(response)
                            } else {
                                it.onError(TaskErrorException(createTaskError(response)))
                            }
                        }

                        override fun onCaughtInvokeException(exception: java.lang.Exception?) {
                            if (exception != null) {
                                it.onError(exception)
                            } else {
                                it.onError(java.lang.Exception("null"))
                            }
                        }
                    })

            if (!result) {
                it.onError(Exception("invoke activateDevice fail"))
            }
        }
    }

    fun activateDeviceByIoT(hostAppId: String): Single<WMPFActivateDeviceByIoTResponse> {
        return Single.create {
            Log.i(TAG, "activateDeviceByIoT: isInProductionEnv = " + DeviceInfo.isInProductionEnv)
            val request = WMPFActivateDeviceByIoTRequest().apply {
                this.baseRequest = WMPFBaseRequestHelper.checked()
                this.hostAppId = hostAppId
            }

            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_ActivateDeviceByIoT,
                    WMPFActivateDeviceByIoTRequest, WMPFActivateDeviceByIoTResponse>(
                    request,
                    IPCInvokerTask_ActivateDeviceByIoT::class.java,
                    object : IPCInvokeCallbackEx<WMPFActivateDeviceByIoTResponse> {
                        override fun onBridgeNotFound() {
                            it.onError(Exception("bridge not found"))
                        }

                        override fun onCallback(response: WMPFActivateDeviceByIoTResponse) {
                            if (isSuccess(response)) {
                                if (response != null && !response.invokeToken.isNullOrEmpty()) {
                                    initInvokeToken(response.invokeToken)
                                }

                                it.onSuccess(response)
                            } else {
                                it.onError(TaskErrorException(createTaskError(response)))
                            }
                        }

                        override fun onCaughtInvokeException(exception: java.lang.Exception?) {
                            if (exception != null) {
                                it.onError(exception)
                            } else {
                                it.onError(java.lang.Exception("null"))
                            }
                        }
                    })

            if (!result) {
                it.onError(Exception("invoke activateDeviceByIoT fail"))
            }
        }
    }


    fun preloadRuntime(): Single<WMPFPreloadRuntimeResponse> {
        return Single.create {
            val request = WMPFPreloadRuntimeRequest().apply {
                baseRequest = WMPFBaseRequestHelper.checked()
            }

            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_PreloadRuntime,
                    WMPFPreloadRuntimeRequest, WMPFPreloadRuntimeResponse>(
                    request,
                    IPCInvokerTask_PreloadRuntime::class.java
            ) { response ->
                if (isSuccess(response)) {
                    it.onSuccess(response)
                } else {
                    it.onError(TaskErrorException(createTaskError(response)))
                }
            }

            if (!result) {
                it.onError(Exception("invoke preloadRuntime fail"))
            }
        }
    }

    // wmpf v1.0.3 后不再需要传入appid, ticket, scope入参
    fun authorize(needOauthCode: Boolean = false): Single<WMPFAuthorizeResponse> {
        return Single.create {
            val request = WMPFAuthorizeRequest()
            request.baseRequest = WMPFBaseRequestHelper.checked()
            // 需要OauthCode，将该变量置为true
            // OauthCode需要BuildConfig.HOST_APPID有开发者资质
            request.needOauthCode = needOauthCode

            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_Authorize,
                    WMPFAuthorizeRequest, WMPFAuthorizeResponse>(
                    request,
                    IPCInvokerTask_Authorize::class.java
            ) {
                response ->
                Log.i(TAG, ": $response")
                if (isSuccess(response)) {
                    it.onSuccess(response)
                } else {
                    it.onError(TaskErrorException(createTaskError(response)))
                }
            }

            if (!result) {
                it.onError(Exception("invoke authorize fail"))
            }
        }
    }

    fun initWxPayInfo(authInfoMap: Map<String, Object>): Single<WMPFInitWxFacePayInfoResponse> {
        return Single.create {

            val request = WMPFInitWxFacePayInfoRequest()
            request.baseRequest = WMPFBaseRequestHelper.checked()
            request.wxFacePayInfo = WMPFHelper.map2Json(authInfoMap)

            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_InitWxFacePayInfo,
                    WMPFInitWxFacePayInfoRequest, WMPFInitWxFacePayInfoResponse>(
                    request,
                    IPCInvokerTask_InitWxFacePayInfo::class.java
            ) { response ->
                if (isSuccess(response)) {
                    it.onSuccess(response)
                } else {
                    it.onError(TaskErrorException(createTaskError(response)))
                }
            }

            if (!result) {
                it.onError(Exception("invoke initWxPayInfoAuthInfo fail"))
            }
        }
    }

    fun authorizeByWxFacePay(): Single<WMPFAuthorizeByWxFacePayResponse> {
        return Single.create {

            val request = WMPFAuthorizeByWxFacePayRequest()
            request.baseRequest = WMPFBaseRequestHelper.checked()

            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_AuthorizeByWxFacePay,
                    WMPFAuthorizeByWxFacePayRequest, WMPFAuthorizeByWxFacePayResponse>(
                    request,
                    IPCInvokerTask_AuthorizeByWxFacePay::class.java
            ) { response ->
                if (isSuccess(response)) {
                    it.onSuccess(response)
                } else {
                    it.onError(TaskErrorException(createTaskError(response)))
                }
            }

            if (!result) {
                it.onError(Exception("invoke authorizeByWxFacePay fail"))
            }
        }
    }


    fun launchWxaApp(launchAppId: String, path: String, appType: Int = 0, landsapeMode: Int = 0): Single<WMPFLaunchWxaAppResponse> {
        return Single.create {
            val request = WMPFLaunchWxaAppRequest()
            request.baseRequest = WMPFBaseRequestHelper.checked()
            // Launch target(wxa appId)
            // WARNING: hostAppIds and wxaAppIds are binded sets.
            request.appId = launchAppId // 填入小程序AppId, 且需要与hostAppId有绑定关系
            request.path = path
            request.appType = appType // 0-正式版 1-开发版 2-体验版
            // mayRunInLandscapeCompatMode Deprecated
//            request.mayRunInLandscapeCompatMode = true
            request.forceRequestFullscreen = false
            request.landscapeMode = landsapeMode // 0:和微信行为保持一致;1:允许横屏铺满显示，忽略小程序的pageOrientation配置;2:强制横屏并居中以16:9显示，忽略pageOrientation配置
            request.displayId = 0 // 小程序想要显示的目标displayId，适用于某些双屏设备 DisplayManager.getDisplays()[0].getDisplayId()
            Log.i(TAG, "launchWxaApp: appId = " + launchAppId + ", hostAppID = " +
                    BuildConfig.HOST_APPID + ", deviceId = " + DeviceInfo.deviceId)
            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_LaunchWxaApp, WMPFLaunchWxaAppRequest,
                    WMPFLaunchWxaAppResponse>(
                    request,
                    IPCInvokerTask_LaunchWxaApp::class.java
            ) { response ->
                if (isSuccess(response)) {
                    it.onSuccess(response)
                } else {
                    it.onError(TaskErrorException(createTaskError(response)))
                }
            }

            if (!result) {
                it.onError(Exception("invoke launchWxaApp fail"))
            }
        }
    }

    fun launchWxaAppByScan(rawData: String): Single<WMPFLaunchWxaAppByQRCodeResponse> {
        return Single.create {
            val request = WMPFLaunchWxaAppByQRCodeRequest()
            request.baseRequest = WMPFBaseRequestHelper.checked()
            request.baseRequest.clientApplicationId = ""
            request.rawData = rawData // rawData from qrcode

            Log.i(TAG, "launchWxaApp: " + "hostAppID = " +
                    BuildConfig.HOST_APPID + ", deviceId = " + DeviceInfo.deviceId)

            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_LaunchWxaAppByQrCode,
                    WMPFLaunchWxaAppByQRCodeRequest, WMPFLaunchWxaAppByQRCodeResponse>(
                        request,
                        IPCInvokerTask_LaunchWxaAppByQrCode::class.java
            ) { response ->
                if (isSuccess(response)) {
                    it.onSuccess(response)
                } else {
                    it.onError(TaskErrorException(createTaskError(response)))
                }
            }

            if (!result) {
                it.onError(Exception("invoke launchWxaAppByScan fail"))
            }
        }
    }

    fun closeWxaApp(appId: String): Single<WMPFCloseWxaAppResponse> {
        return Single.create {
            val request = WMPFCloseWxaAppRequest()
            request.baseRequest = WMPFBaseRequestHelper.checked()
            request.baseRequest.clientApplicationId = ""
            request.appId = appId

            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_CloseWxaApp,
                    WMPFCloseWxaAppRequest, WMPFCloseWxaAppResponse>(
                    request,
                    IPCInvokerTask_CloseWxaApp::class.java
            ) { response ->
                if (isSuccess(response)) {
                    it.onSuccess(response)
                } else {
                    it.onError(TaskErrorException(createTaskError(response)))
                }
            }

            if (!result) {
                it.onError(Exception("invoke closeWxaApp fail"))
            }
        }
    }

    fun manageBackgroundMusic(showManageUI: Boolean = true, forceRequestFullscreen: Boolean = false): Single<WMPFManageBackgroundMusicResponse> {
        return Single.create {
            val request = WMPFManageBackgroundMusicRequest()
            request.baseRequest = WMPFBaseRequestHelper.checked()
            request.showManageUI = showManageUI
            request.forceRequestFullscreen = forceRequestFullscreen
            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_ManageBackgroundMusic, WMPFManageBackgroundMusicRequest,
                    WMPFManageBackgroundMusicResponse>(
                    request,
                    IPCInvokerTask_ManageBackgroundMusic::class.java
            ) { response ->
                if (isSuccess(response)) {
                    it.onSuccess(response)
                } else {
                    it.onError(TaskErrorException(createTaskError(response)))
                }
            }

            if (!result) {
                it.onError(Exception("invoke manageBackgroundMusic fail"))
            }
        }
    }

    // 监听背景音频改变
    fun notifyBackgroundMusic(): Observable<WMPFNotifyBackgroundMusicResponse> {
        return Observable.create {
            val request = WMPFNotifyBackgroundMusicRequest()
            request.baseRequest = WMPFBaseRequestHelper.checked()
            request.notify = true
            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_NotifyBackgroundMusic,
                    WMPFNotifyBackgroundMusicRequest, WMPFNotifyBackgroundMusicResponse>(
                    request,
                    IPCInvokerTask_NotifyBackgroundMusic::class.java
            ) {
                response ->
                /**
                 * {@see com.tencent.wmpf.cli.task.IPCInvokerTask_NotifyBackgroundMusic}
                 * val START = 1
                 * val RESUME = 2
                 * val PAUSE = 3
                 * val STOP = 4
                 * val COMPLETE = 5
                 * val ERROR = 6
                 **/
                it.onNext(response)
            }

            if (!result) {
                it.onError(Exception("invoke notifyBackgroundMusic fail"))
            }
        }
    }

    fun deauthorize(): Single<WMPFDeauthorizeResponse> {
        return Single.create {
            val request = WMPFDeauthorizeRequest()
            request.baseRequest = WMPFBaseRequestHelper.checked()

            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_Deauthorize,
                    WMPFDeauthorizeRequest, WMPFDeauthorizeResponse>(
                    request,
                    IPCInvokerTask_Deauthorize::class.java
            ) { response ->
                if (isSuccess(response)) {
                    it.onSuccess(response)
                } else {
                    it.onError(TaskErrorException(createTaskError(response)))
                }
            }

            if (!result) {
                it.onError(Exception("invoke deauthorize fail"))
            }
        }
    }

    fun listeningPushMsg(): Observable<WMPFPushMsgResponse> {
        return Observable.create {
            val request = WMPFPushMsgRequest()
            request.baseRequest = WMPFBaseRequestHelper.checked()
            val result = WMPFIPCInvoker.invokeAsync<IPCInovkerTask_SetPushMsgCallback,
                    WMPFPushMsgRequest, WMPFPushMsgResponse>(request, IPCInovkerTask_SetPushMsgCallback::class.java) { response ->
                it.onNext(response)
            }
            if (!result) {
                it.onError(Exception("invoke listeningPushMsg fail"))
            }
        }
    }

    fun activeStatus(): Single<WMPFActiveStatusResponse> {
        return Single.create {
            val request = WMPFActiveStatusRequest().apply {
                this.baseRequest = WMPFBaseRequestHelper.checked()
            }

            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_ActiveStatus, WMPFActiveStatusRequest, WMPFActiveStatusResponse>(
                    request,
                    IPCInvokerTask_ActiveStatus::class.java,
                    object : IPCInvokeCallbackEx<WMPFActiveStatusResponse> {
                        override fun onBridgeNotFound() {
                            it.onError(Exception("bridge not found"))
                        }

                        override fun onCallback(response: WMPFActiveStatusResponse) {
                            if (isSuccess(response)) {
                                it.onSuccess(response)
                            } else {
                                it.onError(TaskErrorException(createTaskError(response)))
                            }
                        }

                        override fun onCaughtInvokeException(exception: java.lang.Exception?) {
                            if (exception != null) {
                                it.onError(exception)
                            } else {
                                it.onError(java.lang.Exception("null"))
                            }
                        }
                    })

            if (!result) {
                it.onError(Exception("invoke activeStatus fail"))
            }
        }
    }

    fun authorizeStatus(): Single<WMPFAuthorizeStatusResponse> {
        return Single.create {
            val request = WMPFAuthorizeStatusRequest().apply {
                this.baseRequest = WMPFBaseRequestHelper.checked()
            }

            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_AuthorizeStatus, WMPFAuthorizeStatusRequest, WMPFAuthorizeStatusResponse>(
                    request,
                    IPCInvokerTask_AuthorizeStatus::class.java,
                    object : IPCInvokeCallbackEx<WMPFAuthorizeStatusResponse> {
                        override fun onBridgeNotFound() {
                            it.onError(Exception("bridge not found"))
                        }

                        override fun onCallback(response: WMPFAuthorizeStatusResponse) {
                            if (isSuccess(response)) {
                                it.onSuccess(response)
                            } else {
                                it.onError(TaskErrorException(createTaskError(response)))
                            }
                        }

                        override fun onCaughtInvokeException(exception: java.lang.Exception?) {
                            if (exception != null) {
                                it.onError(exception)
                            } else {
                                it.onError(java.lang.Exception("null"))
                            }
                        }
                    })

            if (!result) {
                it.onError(Exception("invoke authorizeStatus fail"))
            }
        }
    }

    fun initGlobalConfig(config: String): Single<WMPFInitGlobalConfigResponse> {
        return Single.create {
            val request = WMPFInitGlobalConfigRequest().apply {
                this.baseRequest = WMPFBaseRequestHelper.checked()
                this.globalConfigJson = config
            }

            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_InitGlobalConfig,WMPFInitGlobalConfigRequest,WMPFInitGlobalConfigResponse>(
                    request,
                    IPCInvokerTask_InitGlobalConfig::class.java,
                    object :IPCInvokeCallbackEx<WMPFInitGlobalConfigResponse>{
                        override fun onCallback(response: WMPFInitGlobalConfigResponse) {
                            if (isSuccess(response)) {
                                it.onSuccess(response)
                            } else {
                                it.onError(TaskErrorException(createTaskError(response)))
                            }
                        }

                        override fun onBridgeNotFound() {
                            it.onError(Exception("bridge not found"))
                        }

                        override fun onCaughtInvokeException(exception: java.lang.Exception?) {
                            if (exception != null) {
                                it.onError(exception)
                            } else {
                                it.onError(java.lang.Exception("null"))
                            }
                        }

                    }
            )
            if (!result) {
                it.onError(Exception("invoke initGlobalConfig fail"))
            }
        }
    }

    // 预热启动小程序，加快小程序启动
    fun warmLaunch(appId: String): Single<WMPFLaunchWxaAppResponse> {
        return Single.create {
            val request = WMPFLaunchWxaAppRequest().apply {
                this.baseRequest = WMPFBaseRequestHelper.checked()
                this.isForPreWarmLaunch = true
                this.appId = appId
            }

            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_LaunchWxaApp, WMPFLaunchWxaAppRequest, WMPFLaunchWxaAppResponse>(
                    request,
                    IPCInvokerTask_LaunchWxaApp::class.java,
                    object : IPCInvokeCallbackEx<WMPFLaunchWxaAppResponse> {
                        override fun onCallback(response: WMPFLaunchWxaAppResponse) {
                            if (isSuccess(response)) {
                                it.onSuccess(response)
                            } else {
                                it.onError(TaskErrorException(createTaskError(response)))
                            }
                        }

                        override fun onBridgeNotFound() {
                            it.onError(Exception("bridge not found"))
                        }

                        override fun onCaughtInvokeException(exception: java.lang.Exception?) {
                            if (exception != null) {
                                it.onError(exception)
                            } else {
                                it.onError(java.lang.Exception("null"))
                            }
                        }

                    }
            )
            if (!result) {
                it.onError(Exception("invoke warmLaunch fail"))
            }
        }
    }

    fun registerMiniprogramDevice(appId: String, modelId: String,
                                  deviceId: String, snTicket: String): Single<WMPFRegisterMiniProgramDeviceResponse> {
        return Single.create {
            val request = WMPFRegisterMiniProgramDeviceRequest().apply {
                this.baseRequest = WMPFBaseRequestHelper.checked()
                this.appId = appId
                this.modelId = modelId
                this.sn = deviceId
                this.snTicket = snTicket
            }

            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_RegisterMiniProgramDevice, WMPFRegisterMiniProgramDeviceRequest, WMPFRegisterMiniProgramDeviceResponse>(
                request,
                IPCInvokerTask_RegisterMiniProgramDevice::class.java,
                object : IPCInvokeCallbackEx<WMPFRegisterMiniProgramDeviceResponse> {
                    override fun onCallback(response: WMPFRegisterMiniProgramDeviceResponse) {
                        if (isSuccess(response)) {
                            it.onSuccess(response)
                        } else {
                            it.onError(TaskErrorException(createTaskError(response)))
                        }
                    }

                    override fun onBridgeNotFound() {
                        it.onError(Exception("bridge not found"))
                    }

                    override fun onCaughtInvokeException(exception: java.lang.Exception?) {
                        if (exception != null) {
                            it.onError(exception)
                        } else {
                            it.onError(java.lang.Exception("null"))
                        }
                    }
                })

            if (!result) {
                it.onError(Exception("invoke registerMiniprogramDevice fail"))
            }
        }
    }

    fun prefetchDeviceToken(): Single<WMPFPrefetchDeviceTokenResponse> {
        return Single.create {
            val request = WMPFPrefetchDeviceTokenRequest().apply {
                this.baseRequest = WMPFBaseRequestHelper.checked()
            }

            val result = WMPFIPCInvoker.invokeAsync<IPCInvokerTask_RegisterMiniProgramDevice, WMPFPrefetchDeviceTokenRequest, WMPFPrefetchDeviceTokenResponse>(
                request,
                IPCInvokerTask_RegisterMiniProgramDevice::class.java,
                object : IPCInvokeCallbackEx<WMPFPrefetchDeviceTokenResponse> {
                    override fun onCallback(response: WMPFPrefetchDeviceTokenResponse) {
                        if (isSuccess(response)) {
                            it.onSuccess(response)
                        } else {
                            it.onError(TaskErrorException(createTaskError(response)))
                        }
                    }

                    override fun onBridgeNotFound() {
                        it.onError(Exception("bridge not found"))
                    }

                    override fun onCaughtInvokeException(exception: java.lang.Exception?) {
                        if (exception != null) {
                            it.onError(exception)
                        } else {
                            it.onError(java.lang.Exception("null"))
                        }
                    }
                })

            if (!result) {
                it.onError(Exception("invoke prefetchDeviceToken fail"))
            }
        }
    }
}