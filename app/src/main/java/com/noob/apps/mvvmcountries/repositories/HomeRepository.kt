package com.noob.apps.mvvmcountries.repositories

import androidx.lifecycle.MutableLiveData
import com.noob.apps.mvvmcountries.data.DatabaseHelper
import com.noob.apps.mvvmcountries.interfaces.NetworkResponseCallback
import com.noob.apps.mvvmcountries.models.*
import com.noob.apps.mvvmcountries.network.RestClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeRepository private constructor() {
    private lateinit var mCallback: NetworkResponseCallback
    private var depResponse: MutableLiveData<DepartmentCourseResponse?> =
        MutableLiveData<DepartmentCourseResponse?>()
    var myCourseResponse: MutableLiveData<DepartmentCourseResponse?> =
        MutableLiveData<DepartmentCourseResponse?>()
    var infoResponse: MutableLiveData<UserInfoResponse?> =
        MutableLiveData<UserInfoResponse?>()
    var updateTokenResponse: MutableLiveData<LoginResponse?> =
        MutableLiveData<LoginResponse?>()
    var fcmResponse: MutableLiveData<BaseResponse?> =
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
    var deviceIdResponse: MutableLiveData<BaseResponse?> =
        MutableLiveData<BaseResponse?>()
    private lateinit var mUserCall: Call<DepartmentCourseResponse>
    private lateinit var mInfoCall: Call<UserInfoResponse>
    private lateinit var mTokenCall: Call<LoginResponse>
    private lateinit var fcmTokenCall: Call<BaseResponse>
    private lateinit var lecInfoCall: Call<LectureDetailsResponse>
    private lateinit var lecCourseCall: Call<CourseLectureResponse>
    private lateinit var addSessionCall: Call<SessionResponse>
    private lateinit var addWatchCall: Call<Any>
    private lateinit var mCoursesCall: Call<DepartmentCourseResponse>
    private lateinit var mDeviceIdCall: Call<BaseResponse>
    private lateinit var mAttachmentCall: Call<AttachmentResponse>


    var usersToInsertInDB = mutableListOf<User>()

    companion object {
        private var mInstance: HomeRepository? = null
        fun getInstance(): HomeRepository {
            if (mInstance == null) {
                synchronized(this) {
                    mInstance = HomeRepository()
                }
            }
            return mInstance!!
        }
    }


    fun getDepartmentCourses(
        token: String,
        callback: NetworkResponseCallback,
    ): MutableLiveData<DepartmentCourseResponse?> {
        mCallback = callback
        if (depResponse.value != null) {
            mCallback.onNetworkSuccess()
            depResponse = MutableLiveData()
        }
        val authToken = token.replace("Bearer ", "")
        mUserCall = RestClient.getInstance().getApiService().getDepartmentCourses(token, authToken)
        mUserCall.enqueue(object : Callback<DepartmentCourseResponse> {

            override fun onResponse(
                call: Call<DepartmentCourseResponse>,
                response: Response<DepartmentCourseResponse>
            ) {
                if (response.code() != 200) {
                    val error:String = response.errorBody()?.string().let {
                        try {
                            if (JSONObject(it).has("error"))
                                JSONObject(it).getJSONArray("error").join("\n").replace("\"", "")
                            else
                                "un Expected Error"
                        }catch (e:Exception){
                            "un Expected Error"
                        }
                    } ?: "un Expected Error"
                    mCallback.onResponseError(error)
                } else {
                    depResponse.value = response.body()
                    mCallback.onNetworkSuccess()
                }
            }

            override fun onFailure(call: Call<DepartmentCourseResponse>, t: Throwable) {
                mCallback.onNetworkFailure(t)
            }

        })
        return depResponse
    }

    fun getStudentCourses(
        token: String,
        callback: NetworkResponseCallback,
    ): MutableLiveData<DepartmentCourseResponse?> {
        mCallback = callback
        if (myCourseResponse.value != null) {
            mCallback.onNetworkSuccess()
            myCourseResponse = MutableLiveData()
        }
        val authToken = token.replace("Bearer ", "")
        mCoursesCall = RestClient.getInstance().getApiService().getStudentCourses(token, authToken)
        mCoursesCall.enqueue(object : Callback<DepartmentCourseResponse> {

            override fun onResponse(
                call: Call<DepartmentCourseResponse>,
                response: Response<DepartmentCourseResponse>
            ) {
                if (response.code() != 200) {
                    val error:String = response.errorBody()?.string().let {
                        try {
                            if (JSONObject(it).has("error"))
                                JSONObject(it).getJSONArray("error").join("\n").replace("\"", "")
                            else
                                "un Expected Error"
                        }catch (e:Exception){
                            "un Expected Error"
                        }
                    } ?: "un Expected Error"
                    mCallback.onResponseError(error)
                } else {
                    myCourseResponse.value = response.body()
                    mCallback.onNetworkSuccess()
                }
            }

            override fun onFailure(call: Call<DepartmentCourseResponse>, t: Throwable) {
                mCallback.onNetworkFailure(t)
            }

        })
        return myCourseResponse
    }

    fun getStudentInfo(
        token: String,
        callback: NetworkResponseCallback,
    ): MutableLiveData<UserInfoResponse?> {
        mCallback = callback
        if (infoResponse.value != null) {
            mCallback.onNetworkSuccess()
            infoResponse = MutableLiveData()
        }
        val authToken = token.replace("Bearer ", "")
        mInfoCall = RestClient.getInstance().getApiService().getStudentInfo(token, authToken)
        mInfoCall.enqueue(object : Callback<UserInfoResponse> {

            override fun onResponse(
                call: Call<UserInfoResponse>,
                response: Response<UserInfoResponse>
            ) {
                if (response.code() != 200) {
                    val error:String = response.errorBody()?.string().let {
                        try {
                            if (JSONObject(it).has("error"))
                                JSONObject(it).getJSONArray("error").join("\n").replace("\"", "")
                            else
                                "un Expected Error"
                        }catch (e:Exception){
                            "un Expected Error"
                        }
                    } ?: "un Expected Error"
                    mCallback.onResponseError(error)
                } else {
                    infoResponse.value = response.body()
                    mCallback.onNetworkSuccess()
                }
            }

            override fun onFailure(call: Call<UserInfoResponse>, t: Throwable) {
                mCallback.onNetworkFailure(t)
            }

        })
        return infoResponse
    }

    fun updateTokenResponse(
        dbHelper: DatabaseHelper,
        refreshTokenModel: RefreshTokenModel,
        callback: NetworkResponseCallback,
    ): MutableLiveData<LoginResponse?> {
        mCallback = callback
        if (updateTokenResponse.value != null) {
            mCallback.onNetworkSuccess()
            updateTokenResponse = MutableLiveData()
        }
        mTokenCall = RestClient.getInstance().getApiService()
            .updateToken(
//                "Basic U2lnbWEtTW9iaWxlOjEyMzQ1Ng==",
//                refreshTokenModel.grant_type,
                "Bearer ${refreshTokenModel.refresh_token}"
            )
        mTokenCall.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.code() != 200) {
                    val error:String = response.errorBody()?.string().let {
                        try {
                            if (JSONObject(it).has("error"))
                                JSONObject(it).getJSONArray("error").join("\n").replace("\"", "")
                            else
                                "un Expected Error"
                        }catch (e:Exception){
                            "un Expected Error"
                        }
                    } ?: "un Expected Error"
                    mCallback.onResponseError(error)
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        dbHelper.deleteAll()
                    }
                    val user =
                        response.body()?.user_id?.let {
                            User(
                                it,
                                response.body()?.access_token,
                                response.body()?.token_type,
                                response.body()?.refresh_token,
                                response.body()?.expires_in,
                                response.body()?.scope,
                                response.body()?.user_email,
                                response.body()?.user_on_boarded,
                                response.body()?.user_name,
                                response.body()?.user_mobile_number,
                                response.body()?.user_device_id,
                                response.body()!!.user_gender,
                                response.body()!!.jti
                            )
                        }
                    if (user != null) {
                        usersToInsertInDB.add(user)
                        CoroutineScope(Dispatchers.IO).launch {
                            dbHelper.insertAll(usersToInsertInDB)
                        }
                    }
                    updateTokenResponse.value = response.body()
                    mCallback.onNetworkSuccess()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                mCallback.onNetworkFailure(t)
            }

        })
        return updateTokenResponse
    }

    fun updateFCMToken(
        token: String, fcmToken: String,
        callback: NetworkResponseCallback,
    ): MutableLiveData<BaseResponse?> {
        mCallback = callback
        if (fcmResponse.value != null) {
            mCallback.onNetworkSuccess()
            fcmResponse = MutableLiveData()
        }
        val authToken = token.replace("Bearer ", "")
        fcmTokenCall = RestClient.getInstance().getApiService()
            .updateFCMToken(
                token,
                authToken,
                FcmTokenModel(fcmToken)
            )
        fcmTokenCall.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(
                call: Call<BaseResponse>,
                response: Response<BaseResponse>
            ) {
                if (response.code() != 200) {
                    val error:String = response.errorBody()?.string().let {
                        try {
                            if (JSONObject(it).has("error"))
                                JSONObject(it).getJSONArray("error").join("\n").replace("\"", "")
                            else
                                "un Expected Error"
                        }catch (e:Exception){
                            "un Expected Error"
                        }
                    } ?: "un Expected Error"
                    mCallback.onResponseError(error)
                } else {
                    fcmResponse.value = response.body()
                    mCallback.onNetworkSuccess()
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                mCallback.onNetworkFailure(t)
            }

        })
        return fcmResponse
    }

    fun getLectureInfo(
        token: String, lecId: String,
        callback: NetworkResponseCallback,
    ): MutableLiveData<LectureDetailsResponse?> {
        mCallback = callback
        if (lectureResponse.value != null) {
            mCallback.onNetworkSuccess()
            lectureResponse = MutableLiveData()
        }
        val authToken = token.replace("Bearer ", "")
        lecInfoCall = RestClient.getInstance().getApiService()
            .getLectureInfo(
                token,
                authToken,
                lecId
            )
        lecInfoCall.enqueue(object : Callback<LectureDetailsResponse> {
            override fun onResponse(
                call: Call<LectureDetailsResponse>,
                response: Response<LectureDetailsResponse>
            ) {
                if (response.code() != 200) {
                    val error:String = response.errorBody()?.string().let {
                        try {
                            if (JSONObject(it).has("error"))
                                JSONObject(it).getJSONArray("error").join("\n").replace("\"", "")
                            else
                                "un Expected Error"
                        }catch (e:Exception){
                            "un Expected Error"
                        }
                    } ?: "un Expected Error"
                    mCallback.onResponseError(error)
                } else {
                    lectureResponse.value = response.body()
                    mCallback.onNetworkSuccess()
                }
            }

            override fun onFailure(call: Call<LectureDetailsResponse>, t: Throwable) {
                mCallback.onNetworkFailure(t)
            }

        })
        return lectureResponse
    }

    fun getCourseLecture(
        token: String, lecId: String,
        callback: NetworkResponseCallback,
    ): MutableLiveData<CourseLectureResponse?> {
        mCallback = callback
        if (courseLectureResponse.value != null) {
            mCallback.onNetworkSuccess()
            courseLectureResponse = MutableLiveData()
        }
        val authToken = token.replace("Bearer ", "")
        lecCourseCall = RestClient.getInstance().getApiService()
            .getCourseLecture(
                token,
                authToken,
                lecId
            )
        lecCourseCall.enqueue(object : Callback<CourseLectureResponse> {
            override fun onResponse(
                call: Call<CourseLectureResponse>,
                response: Response<CourseLectureResponse>
            ) {
                if (response.code() != 200) {
                    val error:String = response.errorBody()?.string().let {
                        try {
                            if (JSONObject(it).has("error"))
                                JSONObject(it).getJSONArray("error").join("\n").replace("\"", "")
                            else
                                "un Expected Error"
                        }catch (e:Exception){
                            "un Expected Error"
                        }
                    } ?: "un Expected Error"
                    mCallback.onResponseError(error)
                } else {
                    courseLectureResponse.value = response.body()
                    mCallback.onNetworkSuccess()
                }
            }

            override fun onFailure(call: Call<CourseLectureResponse>, t: Throwable) {
                mCallback.onNetworkFailure(t)
            }

        })
        return courseLectureResponse
    }

    fun addSession(
        token: String, lecId: String,
        callback: NetworkResponseCallback,
    ): MutableLiveData<SessionResponse?> {
        mCallback = callback
        if (sessionResponse.value != null) {
            mCallback.onNetworkSuccess()
            sessionResponse = MutableLiveData()
        }
        val authToken = token.replace("Bearer ", "")
        addSessionCall = RestClient.getInstance().getApiService()
            .addSession(
                token,
                authToken,
                lecId
            )
        addSessionCall.enqueue(object : Callback<SessionResponse> {
            override fun onResponse(
                call: Call<SessionResponse>,
                response: Response<SessionResponse>
            ) {
                if (response.code() != 200) {
                    val error:String = response.errorBody()?.string().let {
                        try {
                            if (JSONObject(it).has("error"))
                                JSONObject(it).getJSONArray("error").join("\n").replace("\"", "")
                            else
                                "un Expected Error"
                        }catch (e:Exception){
                            "un Expected Error"
                        }
                    } ?: "un Expected Error"
                    mCallback.onResponseError(error)
                } else {
                    sessionResponse.value = response.body()
                    mCallback.onNetworkSuccess()
                }
            }

            override fun onFailure(call: Call<SessionResponse>, t: Throwable) {
                mCallback.onNetworkFailure(t)
            }

        })
        return sessionResponse
    }

    fun addWatch(
        token: String, lecId: String, progress:Int, position: Long,
        callback: NetworkResponseCallback,
    ): MutableLiveData<Any?> {
        mCallback = callback
        if (watchResponse.value != null) {
            mCallback.onNetworkSuccess()
            watchResponse = MutableLiveData()
        }
        val authToken = token.replace("Bearer ", "")
        addWatchCall = RestClient.getInstance().getApiService()
            .addWatch(
                token,
                authToken,
                lecId,
                progress,
                position
            )
        addWatchCall.enqueue(object : Callback<Any> {
            override fun onResponse(
                call: Call<Any>,
                response: Response<Any>
            ) {
                if (response.code() != 200) {
                    val error:String = response.errorBody()?.string().let {
                        try {
                            if (JSONObject(it).has("error"))
                                JSONObject(it).getJSONArray("error").join("\n").replace("\"", "")
                            else
                                "un Expected Error"
                        }catch (e:Exception){
                            "un Expected Error"
                        }
                    } ?: "un Expected Error"
                    mCallback.onResponseError(error)
                } else {
                    watchResponse.value = response.body()
                    mCallback.onNetworkSuccess()
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                mCallback.onNetworkFailure(t)
            }

        })
        return watchResponse
    }

    fun addDeviceId(
        token: String, deviceId: String,
        callback: NetworkResponseCallback,
    ): MutableLiveData<BaseResponse?> {
        mCallback = callback
        if (deviceIdResponse.value != null) {
            mCallback.onNetworkSuccess()
            deviceIdResponse = MutableLiveData()
        }
        val authToken = token.replace("Bearer ", "")
        mDeviceIdCall = RestClient.getInstance().getApiService()
            .changeDeviceId(
                token,
                authToken,
                DeviceIdModel(deviceId)
            )
        mDeviceIdCall.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(
                call: Call<BaseResponse>,
                response: Response<BaseResponse>
            ) {
                if (response.code() != 200) {
                    val error:String = response.errorBody()?.string().let {
                        try {
                            if (JSONObject(it).has("error"))
                                JSONObject(it).getJSONArray("error").join("\n").replace("\"", "")
                            else
                                "un Expected Error"
                        }catch (e:Exception){
                            "un Expected Error"
                        }
                    } ?: "un Expected Error"
                    mCallback.onResponseError(error)
                } else {
                    deviceIdResponse.value = response.body()
                    mCallback.onNetworkSuccess()
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                mCallback.onNetworkFailure(t)
            }

        })
        return deviceIdResponse
    }

    fun getCourseAttachment(
        token: String, lecId: String,
        callback: NetworkResponseCallback,
    ): MutableLiveData<AttachmentResponse?> {
        mCallback = callback
        if (attachmentResponse.value != null) {
            mCallback.onNetworkSuccess()
            attachmentResponse = MutableLiveData()
        }
        val authToken = token.replace("Bearer ", "")
        mAttachmentCall = RestClient.getInstance().getApiService()
            .getCourseAttachments(
                token,
                authToken,
                lecId
            )
        mAttachmentCall.enqueue(object : Callback<AttachmentResponse> {
            override fun onResponse(
                call: Call<AttachmentResponse>,
                response: Response<AttachmentResponse>
            ) {
                if (response.code() != 200) {
                    val error:String = response.errorBody()?.string().let {
                        try {
                            if (JSONObject(it).has("error"))
                                JSONObject(it).getJSONArray("error").join("\n").replace("\"", "")
                            else
                                "un Expected Error"
                        }catch (e:Exception){
                            "un Expected Error"
                        }
                    } ?: "un Expected Error"
                    mCallback.onResponseError(error)
                } else {
                    attachmentResponse.value = response.body()
                    mCallback.onNetworkSuccess()
                }
            }

            override fun onFailure(call: Call<AttachmentResponse>, t: Throwable) {
                mCallback.onNetworkFailure(t)
            }

        })
        return attachmentResponse
    }

}