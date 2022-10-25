package com.noob.apps.mvvmcountries.network

import com.noob.apps.mvvmcountries.models.*
import retrofit2.Call
import retrofit2.http.*

interface ApiServices {

    @GET("all")
    fun getCountries(): Call<List<Country>>

    @FormUrlEncoded
    @POST("login")
    fun userLogin(
        @Header("Authorization") Authorization: String?,
        @Field("grant_type") grant_type: String?,
        @Field("phone") username: String?,
        @Field("password") password: String?,
        @Field("deviceId") deviceId: String?
    ): Call<LoginResponse>

    @GET("categories/fields")
    fun getUNIVERSITY(
        @Header("Authorization") Authorization: String?,
        @Header("auth-token") auth: String?
    ): Call<BoardingResponse>

    @GET("fields/{id}/levels")
    fun getLevels(
        @Header("Authorization") Authorization: String?,
        @Header("auth-token") auth: String?,
        @Path("id") id: String
    ): Call<BoardingResponse>

    @GET("levels/{id}/departments")
    fun getDepartments(
        @Header("Authorization") Authorization: String?,
        @Header("auth-token") auth: String?,
        @Path("id") id: String
    ): Call<BoardingResponse>


    @POST("students/onboard")
    fun postBoardingData(
        @Header("Authorization") Authorization: String?,
        @Header("auth-token") auth: String?,
        @Body boardingRequest: BoardingRequest
    ): Call<BoardingResponse>

    @POST("students/signup")
    fun userSignUp(
        @Body registrationModel: RegistrationModel
    ): Call<RegistrationResponse>

    @POST("students/verifyOtp")
    fun verifyOtp(
        @Body otpModel: OtpModel
    ): Call<OtpResponse>

    @POST("students/resendOtp")
    fun resendOtp(
        @Body resendModel: ResendModel
    ): Call<OtpResponse>

    @GET("subject/courses")
    fun getDepartmentCourses(
        @Header("Authorization") Authorization: String?,
        @Header("auth-token") auth: String?
    ): Call<DepartmentCourseResponse>

    @GET("student/courses")
    fun getStudentCourses(
        @Header("Authorization") Authorization: String?,
        @Header("auth-token") auth: String?,
    ): Call<DepartmentCourseResponse>


    @GET("student/info")
    fun getStudentInfo(
        @Header("Authorization") Authorization: String?,
        @Header("auth-token") auth: String?,
    ): Call<UserInfoResponse>

    @FormUrlEncoded
    @POST("auth/refresh")
    fun updateToken(
        @Header("Authorization") Authorization: String?,
//        @Field("grant_type") grant_type: String?,
//        @Field("refresh_token") refresh_token: String?
    ): Call<LoginResponse>

    @POST("students/changeFirebaseToken")
    fun updateFCMToken(
        @Header("Authorization") Authorization: String?,
        @Header("auth-token") auth: String?,
        @Body otpModel: FcmTokenModel,
    ): Call<BaseResponse>

    @GET("course/lecture/info")
    fun getLectureInfo(
        @Header("Authorization") Authorization: String?,
        @Header("auth-token") auth: String?,
        @Header("lecture-id") id: String
    ): Call<LectureDetailsResponse>

    @POST("course/lecture/add-session")
    fun addSession(
        @Header("Authorization") Authorization: String?,
        @Header("auth-token") auth: String?,
        @Header("lecture-id") id: String,
    ): Call<SessionResponse>

    @FormUrlEncoded
    @POST("course/lecture/add-watch")
    fun addWatch(
        @Header("Authorization") Authorization: String?,
        @Header("auth-token") auth: String?,
        @Field("lectureId") lectureId: String,
        @Field("progress") progress: Int,
        @Field("videoPosition") videoPosition: Long,
    ): Call<Any>

    @GET("notifications")
    fun getNotifications(
        @Header("Authorization") Authorization: String?,
        @Header("auth-token") auth: String?,
    ): Call<NotificationResponse>

    @POST("students/forgetPassword")
    fun forgetPassword(
        @Body otpModel: ForgetPasswordModel
    ): Call<BaseResponse>

    @POST("students/changeDeviceId")
    fun changeDeviceId(
        @Header("Authorization") Authorization: String?,
        @Header("auth-token") auth: String?,
        @Body otpModel: DeviceIdModel,
    ): Call<BaseResponse>

    @GET("course/lectures")
    fun getCourseLecture(
        @Header("Authorization") Authorization: String?,
        @Header("auth-token") auth: String?,
        @Header("id") id: String
    ): Call<CourseLectureResponse>

    @GET("courses/{id}/attachments")
    fun getCourseAttachments(
        @Header("Authorization") Authorization: String?,
        @Header("auth-token") auth: String?,
        @Path("id") id: String
    ): Call<AttachmentResponse>

    @GET("app/version")
    fun checkForUpdate(): Call<UpdateVersionModel>

}