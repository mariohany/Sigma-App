package com.noob.apps.mvvmcountries.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.noob.apps.mvvmcountries.data.DatabaseHelper
import com.noob.apps.mvvmcountries.interfaces.NetworkResponseCallback
import com.noob.apps.mvvmcountries.models.*
import com.noob.apps.mvvmcountries.repositories.HomeRepository
import com.noob.apps.mvvmcountries.utils.NetworkHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CourseViewModel(
    private val app: Application,
    private val dbHelper: DatabaseHelper
) : AndroidViewModel(app) {
    private var user: MutableLiveData<MutableList<User>> =
        MutableLiveData<MutableList<User>>()
    var depResponse: MutableLiveData<DepartmentCourseResponse?> =
        MutableLiveData<DepartmentCourseResponse?>()
    var myCourseResponse: MutableLiveData<DepartmentCourseResponse?> =
        MutableLiveData<DepartmentCourseResponse?>()
    var infoResponse: MutableLiveData<UserInfoResponse?> =
        MutableLiveData<UserInfoResponse?>()
    var updateTokenResponse: MutableLiveData<LoginResponse?> =
        MutableLiveData<LoginResponse?>()
    var fcmResponse: MutableLiveData<BaseResponse?> =
        MutableLiveData<BaseResponse?>()
    var deviceIdResponse: MutableLiveData<BaseResponse?> =
        MutableLiveData<BaseResponse?>()
    var lectureResponse: MutableLiveData<LectureDetailsResponse?> =
        MutableLiveData<LectureDetailsResponse?>()
    var courseLectureResponse: MutableLiveData<CourseLectureResponse?> =
        MutableLiveData<CourseLectureResponse?>()
    var attachmentResponse: MutableLiveData<AttachmentResponse?> =
        MutableLiveData<AttachmentResponse?>()
    var sessionResponse: MutableLiveData<SessionResponse?> =
        MutableLiveData<SessionResponse?>()
    var watchResponse: MutableLiveData<Any?> =
        MutableLiveData<Any?>()
    val mShowProgressBar = MutableLiveData(true)
    val mShowNetworkError: MutableLiveData<Boolean> = MutableLiveData()
    val mShowApiError = MutableLiveData<String>()
    val mShowResponseError = MutableLiveData<String>()
    private var mRepository = HomeRepository.getInstance()

    fun findUser(
        userId: String
    ): MutableLiveData<MutableList<User>> {
        var usersToInsertInDB: MutableList<User>?
        CoroutineScope(Dispatchers.IO).launch {
            usersToInsertInDB = dbHelper.findByUserId(userId) as MutableList<User>
            user.postValue(usersToInsertInDB!!)
        }
        return user
    }

    fun getDepartmentCourses(token: String) {
        if (NetworkHelper.isOnline(app.baseContext)) {
            mShowProgressBar.value = true
            depResponse =
                mRepository.getDepartmentCourses(token, object : NetworkResponseCallback {
                    override fun onNetworkFailure(th: Throwable) {
                        mShowApiError.value = th.message
                    }

                    override fun onNetworkSuccess() {
                        mShowProgressBar.value = false
                    }

                    override fun onResponseError(message: String) {
                        mShowProgressBar.value = false
                        mShowResponseError.value = message
                    }

                })
        } else {
            mShowProgressBar.value = false
            mShowNetworkError.value = true
        }
    }

    fun getStudentCourses(token: String) {
        if (NetworkHelper.isOnline(app.baseContext)) {
            mShowProgressBar.value = true
            myCourseResponse =
                mRepository.getStudentCourses(token, object : NetworkResponseCallback {
                    override fun onNetworkFailure(th: Throwable) {
                        mShowApiError.value = th.message
                    }

                    override fun onNetworkSuccess() {
                        mShowProgressBar.value = false
                    }

                    override fun onResponseError(message: String) {
                        mShowProgressBar.value = false
                        mShowResponseError.value = message
                    }

                })
        } else {
            mShowProgressBar.value = false
            mShowNetworkError.value = true
        }
    }

    fun getStudentInfo(token: String) {
        if (NetworkHelper.isOnline(app.baseContext)) {
            mShowProgressBar.value = true
            infoResponse =
                mRepository.getStudentInfo(token, object : NetworkResponseCallback {
                    override fun onNetworkFailure(th: Throwable) {
                        mShowApiError.value = th.message
                    }

                    override fun onNetworkSuccess() {
                        mShowProgressBar.value = false
                    }

                    override fun onResponseError(message: String) {
                        mShowProgressBar.value = false
                        mShowResponseError.value = message
                    }

                })
        } else {
            mShowProgressBar.value = false
            mShowNetworkError.value = true
        }
    }

    fun updateToken(refreshTokenModel: RefreshTokenModel) {
        if (NetworkHelper.isOnline(app.baseContext)) {
            mShowProgressBar.value = true
            updateTokenResponse =
                mRepository.updateTokenResponse(
                    dbHelper,
                    refreshTokenModel,
                    object : NetworkResponseCallback {
                        override fun onNetworkFailure(th: Throwable) {
                            mShowApiError.value = th.message
                        }

                        override fun onNetworkSuccess() {
                            mShowProgressBar.value = false
                        }

                        override fun onResponseError(message: String) {
                            mShowProgressBar.value = false
                            mShowResponseError.value = message
                        }

                    })
        } else {
            mShowProgressBar.value = false
            mShowNetworkError.value = true
        }
    }

    fun updateFCMToken(token: String, fcmToken: String) {
        if (NetworkHelper.isOnline(app.baseContext)) {
            mShowProgressBar.value = true
            fcmResponse =
                mRepository.updateFCMToken(token,
                    fcmToken,
                    object : NetworkResponseCallback {
                        override fun onNetworkFailure(th: Throwable) {
                            mShowApiError.value = th.message
                        }

                        override fun onNetworkSuccess() {
                            mShowProgressBar.value = false
                        }

                        override fun onResponseError(message: String) {
                            mShowProgressBar.value = false
                            mShowResponseError.value = message
                        }

                    })
        } else {
            mShowProgressBar.value = false
            mShowNetworkError.value = true
        }
    }

    fun getLectureInfo(token: String, lecId: String) {
        if (NetworkHelper.isOnline(app.baseContext)) {
            mShowProgressBar.value = true
            lectureResponse =
                mRepository.getLectureInfo(token,
                    lecId,
                    object : NetworkResponseCallback {
                        override fun onNetworkFailure(th: Throwable) {
                            mShowApiError.value = th.message
                        }

                        override fun onNetworkSuccess() {
                            mShowProgressBar.value = false
                        }

                        override fun onResponseError(message: String) {
                            mShowProgressBar.value = false
                            mShowResponseError.value = message
                        }

                    })
        } else {
            mShowProgressBar.value = false
            mShowNetworkError.value = true
        }
    }

    fun getCourseLecture(token: String, lecId: String) {
        if (NetworkHelper.isOnline(app.baseContext)) {
            mShowProgressBar.value = true
            courseLectureResponse =
                mRepository.getCourseLecture(token,
                    lecId,
                    object : NetworkResponseCallback {
                        override fun onNetworkFailure(th: Throwable) {
                            mShowApiError.value = th.message
                        }

                        override fun onNetworkSuccess() {
                            mShowProgressBar.value = false
                        }

                        override fun onResponseError(message: String) {
                            mShowProgressBar.value = false
                            mShowResponseError.value = message
                        }

                    })
        } else {
            mShowProgressBar.value = false
            mShowNetworkError.value = true
        }
    }

    fun addSession(token: String, lecId: String) {
        if (NetworkHelper.isOnline(app.baseContext)) {
            mShowProgressBar.value = true
            sessionResponse =
                mRepository.addSession(token,
                    lecId,
                    object : NetworkResponseCallback {
                        override fun onNetworkFailure(th: Throwable) {
                            mShowApiError.value = th.message
                        }

                        override fun onNetworkSuccess() {
                            mShowProgressBar.value = false
                        }

                        override fun onResponseError(message: String) {
                            mShowProgressBar.value = false
                            mShowResponseError.value = message
                        }

                    })
        } else {
            mShowProgressBar.value = false
            mShowNetworkError.value = true
        }
    }

    fun addWatch(token: String, lecId: String, progress:Int, position: Long) {
        if (NetworkHelper.isOnline(app.baseContext)) {
            mShowProgressBar.value = true
            watchResponse =
                mRepository.addWatch(token,
                    lecId, progress, position,
                    object : NetworkResponseCallback {
                        override fun onNetworkFailure(th: Throwable) {
                            mShowApiError.value = th.message
                        }

                        override fun onNetworkSuccess() {
                            mShowProgressBar.value = false
                        }

                        override fun onResponseError(message: String) {
                            mShowProgressBar.value = false
                            mShowResponseError.value = message
                        }

                    })
        } else {
            mShowProgressBar.value = false
            mShowNetworkError.value = true
        }
    }

    fun addDeviceId(token: String, deviceId: String) {
        if (NetworkHelper.isOnline(app.baseContext)) {
            mShowProgressBar.value = true
            deviceIdResponse =
                mRepository.addDeviceId(token,
                    deviceId,
                    object : NetworkResponseCallback {
                        override fun onNetworkFailure(th: Throwable) {
                            mShowApiError.value = th.message
                        }

                        override fun onNetworkSuccess() {
                            mShowProgressBar.value = false
                        }

                        override fun onResponseError(message: String) {
                            mShowProgressBar.value = false
                            mShowResponseError.value = message
                        }

                    })
        } else {
            mShowProgressBar.value = false
            mShowNetworkError.value = true
        }
    }

    fun getCourseAttachments(token: String, lecId: String) {
        if (NetworkHelper.isOnline(app.baseContext)) {
            mShowProgressBar.value = true
            attachmentResponse =
                mRepository.getCourseAttachment(token,
                    lecId,
                    object : NetworkResponseCallback {
                        override fun onNetworkFailure(th: Throwable) {
                            mShowApiError.value = th.message
                        }

                        override fun onNetworkSuccess() {
                            mShowProgressBar.value = false
                        }

                        override fun onResponseError(message: String) {
                            mShowProgressBar.value = false
                            mShowResponseError.value = message
                        }

                    })
        } else {
            mShowProgressBar.value = false
            mShowNetworkError.value = true
        }
    }
}