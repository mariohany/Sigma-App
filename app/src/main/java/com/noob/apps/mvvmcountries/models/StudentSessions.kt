package com.noob.apps.mvvmcountries.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class StudentSessions(
    val id:Int,
    @SerializedName("created_at")
    val createdAt: String,
    val expiredAt: String,
    val expired: Boolean,
    val progress:Int,
    val videoPosition:Long,
    @SerializedName("remaning_seconds")
    val remainingSeconds: Long?
) : Serializable
