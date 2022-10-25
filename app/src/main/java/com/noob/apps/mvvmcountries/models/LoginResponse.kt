package com.noob.apps.mvvmcountries.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class LoginResponse(
    var isSuccess: Boolean?,
    var error: String?,
    var error_description: String?,
    @SerializedName("refresh_token")
    val access_token: String?,
    val token_type: String?,
    @SerializedName("access_token")
    val refresh_token: String?,
    val expires_in: String?,
    val scope: String?,
    val user_id: String?,
    val user_email: String?,
    val user_on_boarded: Boolean?,
    val user_name: String?,
    val user_mobile_number: String?,
    val user_device_id: String?,
    val user_gender: String?,
    val jti: String?
) : Serializable
