package com.noob.apps.mvvmcountries.models

data class UserInfo(
    val id: String,
    val name: String,
    val mobileNumber: Int,
    val email: String,
    val gender: String,
    val enabled: Boolean=true,
    val verified: Boolean,
    val user_on_boarded: Boolean,
    val levelName: String,
    val studyFieldName: String,
    val departmentName: String,
    val deviceId: String,
    val role: String,
    val webView: String,
)
